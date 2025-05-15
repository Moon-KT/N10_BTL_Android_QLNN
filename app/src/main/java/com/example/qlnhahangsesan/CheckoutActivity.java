package com.example.qlnhahangsesan;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.example.qlnhahangsesan.adapter.OrderItemAdapter;
import com.example.qlnhahangsesan.database.DatabaseHelper;
import com.example.qlnhahangsesan.model.Order;
import com.example.qlnhahangsesan.model.OrderItem;
import com.example.qlnhahangsesan.model.Table;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CheckoutActivity extends AppCompatActivity {
    private static final int PERMISSION_REQUEST_CODE = 200;

    private TextView textViewTableInfo;
    private TextView textViewOrderItemsLabel;
    private ListView listViewOrderItems;
    private TextView textViewTotalLabel;
    private TextView textViewTotalAmount;
    private Button buttonCancel;
    private Button buttonConfirmPayment;

    private DatabaseHelper databaseHelper;
    private Order currentOrder;
    private Table currentTable;
    private long tableId;
    private String tableName;
    private NumberFormat currencyFormat;
    private SimpleDateFormat dateFormat;
    private File pdfFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkout);

        // Khởi tạo DatabaseHelper
        databaseHelper = DatabaseHelper.getInstance(this);
        
        // Định dạng tiền tệ và ngày
        currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault());

        // Lấy thông tin từ Intent
        if (getIntent().hasExtra("order")) {
            currentOrder = (Order) getIntent().getSerializableExtra("order");
        } else {
            Toast.makeText(this, "Không tìm thấy thông tin đơn hàng", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        tableId = getIntent().getLongExtra("tableId", -1);
        tableName = getIntent().getStringExtra("tableName");

        if (tableId <= 0) {
            Toast.makeText(this, "Không tìm thấy thông tin bàn", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Lấy thông tin bàn từ database
        currentTable = databaseHelper.getTableById(tableId);
        if (currentTable == null) {
            Toast.makeText(this, "Không tìm thấy thông tin bàn", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Ánh xạ views
        initViews();

        // Hiển thị thông tin bàn
        textViewTableInfo.setText("Bàn: " + currentTable.getName());

        // Hiển thị danh sách món ăn đã đặt
        setupListView();

        // Hiển thị tổng tiền
        textViewTotalAmount.setText(currencyFormat.format(currentOrder.getTotalAmount()));

        // Thiết lập nút bấm
        setupButtons();

        // Thiết lập ActionBar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.checkout);
        }
    }

    private void initViews() {
        textViewTableInfo = findViewById(R.id.textViewTableInfo);
        textViewOrderItemsLabel = findViewById(R.id.textViewOrderItemsLabel);
        listViewOrderItems = findViewById(R.id.listViewOrderItems);
        textViewTotalLabel = findViewById(R.id.textViewTotalLabel);
        textViewTotalAmount = findViewById(R.id.textViewTotalAmount);
        buttonCancel = findViewById(R.id.buttonCancel);
        buttonConfirmPayment = findViewById(R.id.buttonConfirmPayment);
    }

    private void setupListView() {
        List<OrderItem> orderItems = currentOrder.getOrderItems();
        OrderItemAdapter adapter = new OrderItemAdapter(this, orderItems);
        listViewOrderItems.setAdapter(adapter);
    }

    private void setupButtons() {
        buttonConfirmPayment.setOnClickListener(v -> {
            confirmPayment();
        });

        buttonCancel.setOnClickListener(v -> {
            setResult(RESULT_CANCELED);
            finish();
        });
    }

    private void confirmPayment() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Xác nhận thanh toán");
        builder.setMessage("Bạn có chắc chắn muốn thanh toán cho đơn hàng này?");
        builder.setPositiveButton("Đồng ý", (dialog, which) -> {
            processPayment();
        });
        builder.setNegativeButton("Hủy", null);
        builder.show();
    }

    private void processPayment() {
        // Lưu đơn hàng vào database
        long orderId = databaseHelper.addOrder(currentOrder);
        
        if (orderId > 0) {
            currentOrder.setId(orderId);
            
            // Lưu các món đã đặt
            for (OrderItem item : currentOrder.getOrderItems()) {
                item.setOrderId(orderId);
                databaseHelper.addOrderItem(item);
            }

            // Cập nhật trạng thái bàn thành "Trống"
            currentTable.setStatus(getString(R.string.status_available));
            boolean updated = databaseHelper.updateTable(currentTable);
            
            if (updated) {
                // Tạo file PDF
                if (checkPermission()) {
                    generatePdfInvoice();
                } else {
                    requestPermission();
                }
                
                // Hiển thị thông báo thành công
                Toast.makeText(this, "Thanh toán thành công!", Toast.LENGTH_SHORT).show();
                
                // Trả kết quả về cho OrderActivity
                Intent resultIntent = new Intent();
                resultIntent.putExtra("finish_table_detail", true);
                setResult(RESULT_OK, resultIntent);
                
                // Kết thúc activity
                finish();
            } else {
                Toast.makeText(this, "Lỗi khi cập nhật trạng thái bàn", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Lỗi khi lưu đơn hàng", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean checkPermission() {
        int permission = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        return permission == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                PERMISSION_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                generatePdfInvoice();
            } else {
                Toast.makeText(this, "Cần quyền ghi file để xuất hóa đơn PDF", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void generatePdfInvoice() {
        // Tạo document PDF
        PdfDocument document = new PdfDocument();
        
        // Cấu hình trang
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(595, 842, 1).create();
        PdfDocument.Page page = document.startPage(pageInfo);
        Canvas canvas = page.getCanvas();
        
        // Tạo đối tượng Paint để vẽ
        Paint paint = new Paint();
        int y = 80; // Vị trí bắt đầu
        
        // Tiêu đề
        paint.setTextSize(22);
        paint.setFakeBoldText(true);
        String title = "NHÀ HÀNG SESAN";
        canvas.drawText(title, (pageInfo.getPageWidth() - paint.measureText(title)) / 2, y, paint);
        y += 40;
        
        // Thông tin cơ bản
        paint.setTextSize(16);
        paint.setFakeBoldText(false);
        canvas.drawText("Địa chỉ: 123 Đường ABC, Quận XYZ, TP.HCM", 40, y, paint);
        y += 30;
        canvas.drawText("SĐT: 0123 456 789", 40, y, paint);
        y += 30;
        canvas.drawText("Email: contact@sesan.com", 40, y, paint);
        y += 40;
        
        // Tiêu đề hóa đơn
        paint.setTextSize(20);
        paint.setFakeBoldText(true);
        String invoiceTitle = "HÓA ĐƠN THANH TOÁN";
        canvas.drawText(invoiceTitle, (pageInfo.getPageWidth() - paint.measureText(invoiceTitle)) / 2, y, paint);
        y += 40;
        
        // Thông tin đơn hàng
        paint.setTextSize(14);
        paint.setFakeBoldText(false);
        String orderId = "Mã đơn hàng: " + currentOrder.getId();
        canvas.drawText(orderId, 40, y, paint);
        y += 25;
        
        String date = "Ngày: " + dateFormat.format(new Date());
        canvas.drawText(date, 40, y, paint);
        y += 25;
        
        String table = "Bàn: " + tableName;
        canvas.drawText(table, 40, y, paint);
        y += 40;
        
        // Tiêu đề bảng
        paint.setFakeBoldText(true);
        canvas.drawText("STT", 40, y, paint);
        canvas.drawText("Tên món", 100, y, paint);
        canvas.drawText("Đơn giá", 300, y, paint);
        canvas.drawText("SL", 420, y, paint);
        canvas.drawText("Thành tiền", 470, y, paint);
        y += 20;
        
        // Vẽ đường kẻ
        paint.setStrokeWidth(1);
        canvas.drawLine(40, y, 555, y, paint);
        y += 20;
        
        // Danh sách món ăn
        paint.setFakeBoldText(false);
        List<OrderItem> items = currentOrder.getOrderItems();
        int count = 1;
        for (OrderItem item : items) {
            canvas.drawText(String.valueOf(count), 40, y, paint);
            canvas.drawText(item.getName(), 100, y, paint);
            canvas.drawText(currencyFormat.format(item.getPrice()), 300, y, paint);
            canvas.drawText(String.valueOf(item.getQuantity()), 420, y, paint);
            canvas.drawText(currencyFormat.format(item.getTotalPrice()), 470, y, paint);
            y += 30;
            count++;
        }
        
        // Vẽ đường kẻ
        canvas.drawLine(40, y, 555, y, paint);
        y += 30;
        
        // Tổng tiền
        paint.setFakeBoldText(true);
        canvas.drawText("Tổng tiền:", 350, y, paint);
        canvas.drawText(currencyFormat.format(currentOrder.getTotalAmount()), 470, y, paint);
        y += 50;
        
        // Chân trang
        paint.setFakeBoldText(false);
        paint.setTextSize(12);
        canvas.drawText("Cảm ơn quý khách đã sử dụng dịch vụ!", (pageInfo.getPageWidth() - paint.measureText("Cảm ơn quý khách đã sử dụng dịch vụ!")) / 2, y, paint);
        
        // Kết thúc trang
        document.finishPage(page);
        
        // Lưu file PDF
        try {
            String fileName = "Invoice_" + currentOrder.getId() + "_" + System.currentTimeMillis() + ".pdf";
            File directory = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "Sesan");
            if (!directory.exists()) {
                directory.mkdirs();
            }
            
            pdfFile = new File(directory, fileName);
            FileOutputStream fos = new FileOutputStream(pdfFile);
            document.writeTo(fos);
            document.close();
            fos.close();
            
            // Hiển thị dialog để mở file PDF
            showPdfDialog();
            
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Lỗi khi tạo file PDF: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void showPdfDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Hóa đơn PDF");
        builder.setMessage("Đã tạo hóa đơn PDF thành công. Bạn có muốn xem ngay bây giờ?");
        builder.setPositiveButton("Xem", (dialog, which) -> {
            openPdfFile();
        });
        builder.setNegativeButton("Đóng", null);
        builder.show();
    }

    private void openPdfFile() {
        if (pdfFile != null && pdfFile.exists()) {
            Uri uri = FileProvider.getUriForFile(this, getPackageName() + ".provider", pdfFile);
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(uri, "application/pdf");
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            try {
                startActivity(intent);
            } catch (Exception e) {
                Toast.makeText(this, "Không tìm thấy ứng dụng đọc PDF", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            setResult(RESULT_CANCELED);
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
} 