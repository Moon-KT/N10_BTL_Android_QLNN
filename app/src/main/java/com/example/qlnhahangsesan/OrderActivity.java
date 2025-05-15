package com.example.qlnhahangsesan;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.qlnhahangsesan.adapter.MenuAdapter;
import com.example.qlnhahangsesan.database.DatabaseHelper;
import com.example.qlnhahangsesan.model.Food;
import com.example.qlnhahangsesan.model.Order;
import com.example.qlnhahangsesan.model.OrderItem;
import com.example.qlnhahangsesan.model.Table;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import android.annotation.SuppressLint;
import android.app.AlertDialog;

public class OrderActivity extends AppCompatActivity implements MenuAdapter.OnQuantityChangedListener {
    private static final int REQUEST_CHECKOUT = 101;

    private ListView listViewMenu;
    private TextView textViewTableInfo;
    private TextView textViewTotalAmount;
    private Button buttonCheckout;
    private Button buttonCancel;

    private DatabaseHelper databaseHelper;
    private MenuAdapter menuAdapter;
    private List<Food> foodList;
    private List<OrderItem> orderItems;
    private Order currentOrder;
    private Table currentTable;
    private long tableId;
    private String tableName;
    private NumberFormat currencyFormat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order);

        // Khởi tạo DatabaseHelper
        databaseHelper = DatabaseHelper.getInstance(this);
        
        // Định dạng tiền tệ
        currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));

        // Lấy thông tin bàn từ Intent
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

        // Khởi tạo đơn hàng mới
        currentOrder = new Order(tableId);
        orderItems = new ArrayList<>();

        // Ánh xạ views
        initViews();

        // Hiển thị thông tin bàn
        String tableInfoText = "Bàn: " + currentTable.getName();
        textViewTableInfo.setText(tableInfoText);

        // Thiết lập ListView
        setupListView();

        // Thiết lập nút bấm
        setupButtons();

        // Thiết lập ActionBar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(getString(R.string.order_food));
        }
    }

    private void initViews() {
        listViewMenu = findViewById(R.id.listViewMenu);
        textViewTableInfo = findViewById(R.id.textViewTableInfo);
        textViewTotalAmount = findViewById(R.id.textViewTotalAmount);
        buttonCheckout = findViewById(R.id.buttonCheckout);
        buttonCancel = findViewById(R.id.buttonCancel);
    }

    private void setupListView() {
        // Lấy danh sách món ăn từ menu hôm nay
        foodList = databaseHelper.getMenuItemsForToday();
        
        // Nếu không có menu hôm nay, lấy tất cả món ăn
        if (foodList.isEmpty()) {
            foodList = databaseHelper.getAllFoods();
        }

        // Khởi tạo adapter
        menuAdapter = new MenuAdapter(this, foodList, this);
        listViewMenu.setAdapter(menuAdapter);
        
        // Cập nhật giao diện để hiển thị rõ ràng danh sách các món
        listViewMenu.setDivider(new android.graphics.drawable.ColorDrawable(getResources().getColor(R.color.colorDivider)));
        listViewMenu.setDividerHeight(1);
        listViewMenu.setSelector(android.R.color.transparent);
        
        // Cập nhật hiển thị tổng tiền ban đầu
        updateTotalAmount();
    }

    private void setupButtons() {
        buttonCheckout.setOnClickListener(v -> {
            // Kiểm tra xem có món nào được chọn không
            if (orderItems.isEmpty()) {
                Toast.makeText(this, "Vui lòng chọn ít nhất một món", Toast.LENGTH_SHORT).show();
                return;
            }

            // Xác nhận gọi món và quay lại màn hình chi tiết bàn
            confirmOrder();
        });

        buttonCancel.setOnClickListener(v -> {
            // Hủy đơn hàng và trở về màn hình trước
            Toast.makeText(this, "Đã hủy đơn hàng", Toast.LENGTH_SHORT).show();
            
            // Đổi trạng thái bàn về "Trống"
            currentTable.setStatus(getString(R.string.status_available));
            databaseHelper.updateTable(currentTable);
            
            setResult(RESULT_CANCELED);
            finish();
        });
    }

    @Override
    public void onQuantityChanged(Food food, int quantity) {
        // Xử lý khi người dùng thay đổi số lượng món
        if (quantity > 0) {
            // Tìm orderItem đã tồn tại
            OrderItem existingItem = null;
            for (OrderItem item : orderItems) {
                if (item.getFoodId() == food.getId()) {
                    existingItem = item;
                    break;
                }
            }

            if (existingItem != null) {
                // Cập nhật số lượng nếu đã tồn tại
                existingItem.setQuantity(quantity);
            } else {
                // Thêm mới nếu chưa tồn tại
                OrderItem newItem = new OrderItem();
                newItem.setFoodId(food.getId());
                newItem.setName(food.getName());
                newItem.setPrice(food.getPrice());
                newItem.setQuantity(quantity);
                orderItems.add(newItem);
            }
        } else {
            // Xóa orderItem nếu số lượng = 0
            OrderItem itemToRemove = null;
            for (OrderItem item : orderItems) {
                if (item.getFoodId() == food.getId()) {
                    itemToRemove = item;
                    break;
                }
            }
            if (itemToRemove != null) {
                orderItems.remove(itemToRemove);
            }
        }

        // Cập nhật tổng tiền
        updateTotalAmount();
    }

    private void updateTotalAmount() {
        double total = 0;
        for (OrderItem item : orderItems) {
            total += item.getTotalPrice();
        }
        
        // Cập nhật hiển thị tổng tiền
        textViewTotalAmount.setText(currencyFormat.format(total));
        
        // Cập nhật tổng tiền trong đơn hàng
        currentOrder.setTotalAmount(total);
    }

    private void confirmOrder() {
        // Cập nhật thông tin đơn hàng
        currentOrder.setOrderDate(new Date());
        currentOrder.setOrderItems(orderItems);

        // Hiển thị dialog xác nhận
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Xác nhận đặt món");
        
        // Tạo thông báo với danh sách món đã đặt
        StringBuilder message = new StringBuilder("Bạn đã chọn:\n\n");
        for (OrderItem item : orderItems) {
            message.append("- ").append(item.getName())
                   .append(" (").append(item.getQuantity()).append(")")
                   .append(" x ").append(currencyFormat.format(item.getPrice()))
                   .append(" = ").append(currencyFormat.format(item.getTotalPrice()))
                   .append("\n");
        }
        message.append("\nTổng tiền: ").append(currencyFormat.format(currentOrder.getTotalAmount()));
        message.append("\n\nXác nhận đặt món?");
        
        builder.setMessage(message.toString());
        builder.setPositiveButton("Xác nhận", (dialog, which) -> {
            // Lưu đơn hàng vào database
            long orderId = databaseHelper.saveOrder(currentOrder);
            
            if (orderId > 0) {
                // Đơn hàng đã được lưu thành công
                currentOrder.setId(orderId);
                
                // Đảm bảo trạng thái bàn là "Đang phục vụ"
                currentTable.setStatus(getString(R.string.status_occupied));
                databaseHelper.updateTable(currentTable);

                // Hiển thị dialog tiến trình
                AlertDialog progressDialog = new AlertDialog.Builder(this)
                    .setTitle("Đang xử lý")
                    .setMessage("Đang lưu đơn hàng...")
                    .setCancelable(false)
                    .create();
                progressDialog.show();
                
                // Trì hoãn 1 giây để hiển thị rõ quá trình
                new Handler().postDelayed(() -> {
                    progressDialog.dismiss();
                    
                    // Truyền dữ liệu đơn hàng về TableDetailActivity
                    Intent resultIntent = new Intent();
                    resultIntent.putExtra("order", currentOrder);
                    resultIntent.putExtra("tableId", tableId);
                    
                    setResult(RESULT_OK, resultIntent);
                    Toast.makeText(this, "Đã gọi món thành công", Toast.LENGTH_SHORT).show();
                    finish();
                }, 1000);
            } else {
                Toast.makeText(this, "Lỗi khi lưu đơn hàng", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Hủy", null);
        builder.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (requestCode == REQUEST_CHECKOUT) {
            if (resultCode == RESULT_OK) {
                // Thanh toán thành công, trở về màn hình trước
                setResult(RESULT_OK, data);
                finish();
            }
            // Nếu người dùng hủy thanh toán, không làm gì cả
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            // Hiển thị dialog xác nhận hủy khi người dùng nhấn nút back
            showCancelConfirmationDialog();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    @SuppressLint("MissingSuperCall")
    public void onBackPressed() {
        // We show a confirmation dialog instead of the default back action
        // Deliberately not calling super.onBackPressed() to prevent the activity from finishing immediately
        showCancelConfirmationDialog();
    }

    private void showCancelConfirmationDialog() {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        builder.setTitle("Hủy đơn hàng");
        builder.setMessage("Bạn có chắc chắn muốn hủy đơn hàng này?");
        builder.setPositiveButton("Đồng ý", (dialog, which) -> {
            // Đổi trạng thái bàn về "Trống"
            currentTable.setStatus(getString(R.string.status_available));
            databaseHelper.updateTable(currentTable);
            
            setResult(RESULT_CANCELED);
            finish();
        });
        builder.setNegativeButton("Hủy", null);
        builder.show();
    }
}

