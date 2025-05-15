package com.example.qlnhahangsesan;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.qlnhahangsesan.adapter.OrderItemAdapter;
import com.example.qlnhahangsesan.database.DatabaseHelper;
import com.example.qlnhahangsesan.model.Order;
import com.example.qlnhahangsesan.model.OrderItem;
import com.example.qlnhahangsesan.model.Table;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class TableDetailActivity extends AppCompatActivity {

    private static final int REQUEST_ORDER_ACTIVITY = 100;
    private static final int REQUEST_CHECKOUT_ACTIVITY = 200;
    
    private EditText editTextName;
    private EditText editTextCapacity;
    private Spinner spinnerStatus;
    private EditText editTextNote;
    private Button buttonSave;
    private Button buttonDelete;
    private Button buttonOrder;
    private Button buttonOrderHistory;
    private LinearLayout orderDetailsLayout;
    private ListView listViewOrderItems;
    private TextView textViewOrderTotal;
    private Button buttonCheckout;
    private Button buttonCheckoutStandalone;
    private TextView textViewOrderStatus;

    private DatabaseHelper databaseHelper;
    private Table currentTable;
    private Order currentOrder;
    private List<OrderItem> orderItems;
    private OrderItemAdapter orderItemAdapter;
    private boolean isEditMode = false;
    private NumberFormat currencyFormat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_table_detail);

        // Initialize database helper
        databaseHelper = DatabaseHelper.getInstance(this);
        
        // Initialize currency formatter
        currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));

        // Initialize views
        initViews();

        // Set up spinner for status
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.table_status_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerStatus.setAdapter(adapter);

        // Check if we're in edit mode
        if (getIntent().hasExtra("table")) {
            isEditMode = true;
            currentTable = (Table) getIntent().getSerializableExtra("table");
            populateFields();
            loadCurrentOrder();
            buttonDelete.setVisibility(View.VISIBLE);
            updateOrderButtonVisibility();
        } else {
            isEditMode = false;
            currentTable = new Table();
            buttonDelete.setVisibility(View.GONE);
            buttonOrder.setVisibility(View.GONE);
            orderDetailsLayout.setVisibility(View.GONE);
            buttonCheckoutStandalone.setVisibility(View.GONE);
        }

        // Set up action bar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(isEditMode ? R.string.edit_table : R.string.add_table);
        }

        // Set up button click listeners
        buttonSave.setOnClickListener(v -> saveTable());
        buttonDelete.setOnClickListener(v -> confirmDelete());
        buttonOrder.setOnClickListener(v -> onOrderTable());
        buttonCheckout.setOnClickListener(v -> confirmCheckout());
        buttonCheckoutStandalone.setOnClickListener(v -> confirmCheckout());
        buttonOrderHistory.setOnClickListener(v -> showOrderHistory());
        
        // Cập nhật giao diện khi người dùng thay đổi trạng thái
        spinnerStatus.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                if (isEditMode) {
                    updateOrderButtonVisibility();
                }
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {
            }
        });
    }
    
    private void initViews() {
        editTextName = findViewById(R.id.editTextTableName);
        editTextCapacity = findViewById(R.id.editTextTableCapacity);
        spinnerStatus = findViewById(R.id.spinnerTableStatus);
        editTextNote = findViewById(R.id.editTextTableNote);
        buttonSave = findViewById(R.id.buttonSave);
        buttonDelete = findViewById(R.id.buttonDelete);
        buttonOrder = findViewById(R.id.buttonOrder);
        buttonOrderHistory = findViewById(R.id.buttonOrderHistory);
        orderDetailsLayout = findViewById(R.id.orderDetailsLayout);
        listViewOrderItems = findViewById(R.id.listViewOrderItems);
        textViewOrderTotal = findViewById(R.id.textViewOrderTotal);
        buttonCheckout = findViewById(R.id.buttonCheckout);
        buttonCheckoutStandalone = findViewById(R.id.buttonCheckoutStandalone);
        textViewOrderStatus = findViewById(R.id.textViewOrderStatus);
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        if (isEditMode && currentTable != null && currentTable.getId() > 0) {
            // Cập nhật thông tin bàn từ database khi quay lại từ màn hình khác
            Table updatedTable = databaseHelper.getTableById(currentTable.getId());
            if (updatedTable != null) {
                currentTable = updatedTable;
                populateFields();
                updateOrderButtonVisibility();
            }
            
            // Cập nhật thông tin đơn hàng hiện tại
            loadCurrentOrder();
        }
    }
    
    private void loadCurrentOrder() {
        if (currentTable != null && currentTable.getId() > 0) {
            // Lấy đơn hàng hiện tại của bàn (đơn hàng chưa thanh toán)
            currentOrder = databaseHelper.getCurrentOrderForTable(currentTable.getId());
            
            if (currentOrder != null) {
                Log.d("TableDetailActivity", "Found current order ID: " + currentOrder.getId());
                
                // Cập nhật trạng thái đơn hàng
                String status = currentOrder.getStatus();
                if (status.equals("Chưa thanh toán")) {
                    textViewOrderStatus.setText("Trạng thái: Chưa thanh toán");
                    textViewOrderStatus.setTextColor(getResources().getColor(android.R.color.holo_red_light));
                    
                    // Hiển thị nút thanh toán rõ ràng
                    buttonCheckout.setVisibility(View.VISIBLE);
                    buttonCheckout.setTextSize(16);
                    buttonCheckout.setPadding(0, 20, 0, 20);
                    
                    // Hiển thị nút thanh toán độc lập
                    buttonCheckoutStandalone.setVisibility(View.VISIBLE);
                    Log.d("TableDetailActivity", "Order is unpaid, making checkout buttons visible and prominent");
                    
                    // Hiện thị Toast thông báo để nhắc người dùng
                    Toast.makeText(this, "Đơn hàng chưa thanh toán, nhấn 'THANH TOÁN ĐƠN HÀNG' để hoàn tất", Toast.LENGTH_SHORT).show();
                } else {
                    textViewOrderStatus.setText("Trạng thái: Đã thanh toán");
                    textViewOrderStatus.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
                    buttonCheckout.setVisibility(View.GONE);
                    buttonCheckoutStandalone.setVisibility(View.GONE);
                    Log.d("TableDetailActivity", "Order is paid, hiding checkout buttons");
                }
                
                // Hiển thị thông tin đơn hàng
                orderItems = currentOrder.getOrderItems();
                
                // Nếu không có mục nào, lấy từ database
                if (orderItems == null || orderItems.isEmpty()) {
                    orderItems = databaseHelper.getOrderItemsForOrder(currentOrder.getId());
                    currentOrder.setOrderItems(orderItems);
                }
                
                // Hiển thị danh sách món ăn đã đặt
                if (orderItems != null && !orderItems.isEmpty()) {
                    // Đảm bảo orderDetailsLayout hiển thị
                    orderDetailsLayout.setVisibility(View.VISIBLE);
                    
                    // Cập nhật adapter cho listview
                    orderItemAdapter = new OrderItemAdapter(this, orderItems);
                    listViewOrderItems.setAdapter(orderItemAdapter);
                    
                    // Hiển thị tổng tiền
                    double totalAmount = currentOrder.getTotalAmount();
                    textViewOrderTotal.setText(currencyFormat.format(totalAmount));
                    Log.d("TableDetailActivity", "Showing order details with total: " + totalAmount);
                    
                    // Đảm bảo đơn hàng hiển thị trên màn hình - scroll đến orderDetailsLayout
                    final ScrollView scrollView = findViewById(R.id.scrollViewTableDetail);
                    if (scrollView != null) {
                        scrollView.post(() -> scrollView.smoothScrollTo(0, orderDetailsLayout.getTop()));
                    }
                } else {
                    // Không có món nào được đặt
                    orderDetailsLayout.setVisibility(View.GONE);
                    Log.d("TableDetailActivity", "Order has no items, hiding order details");
                }
            } else {
                // Không có đơn hàng hiện tại
                orderDetailsLayout.setVisibility(View.GONE);
                buttonCheckoutStandalone.setVisibility(View.GONE);
                Log.d("TableDetailActivity", "No current order found for this table");
            }
        }
    }
    
    // Hiển thị hoặc ẩn nút gọi món dựa trên trạng thái bàn
    private void updateOrderButtonVisibility() {
        if (spinnerStatus != null && buttonOrder != null) {
            String currentStatus = spinnerStatus.getSelectedItem().toString();
            String availableStatus = getString(R.string.status_available);
            
            // Chỉ hiển thị nút Order khi bàn đang trống
            if (currentStatus.equals(availableStatus)) {
                buttonOrder.setVisibility(View.VISIBLE);
            } else {
                buttonOrder.setVisibility(View.GONE);
            }
        }
    }

    private void populateFields() {
        if (currentTable != null) {
            editTextName.setText(currentTable.getName());
            editTextCapacity.setText(String.valueOf(currentTable.getCapacity()));
            editTextNote.setText(currentTable.getNote());
            
            // Set spinner position based on status
            String status = currentTable.getStatus();
            ArrayAdapter adapter = (ArrayAdapter) spinnerStatus.getAdapter();
            for (int i = 0; i < adapter.getCount(); i++) {
                if (adapter.getItem(i).toString().equals(status)) {
                    spinnerStatus.setSelection(i);
                    break;
                }
            }
        }
    }

    private void saveTable() {
        // Validate input
        String name = editTextName.getText().toString().trim();
        String capacityStr = editTextCapacity.getText().toString().trim();
        String status = spinnerStatus.getSelectedItem().toString();
        String note = editTextNote.getText().toString().trim();

        if (name.isEmpty()) {
            editTextName.setError("Vui lòng nhập tên bàn");
            editTextName.requestFocus();
            return;
        }

        if (capacityStr.isEmpty()) {
            editTextCapacity.setError("Vui lòng nhập sức chứa");
            editTextCapacity.requestFocus();
            return;
        }

        int capacity;
        try {
            capacity = Integer.parseInt(capacityStr);
            if (capacity <= 0) {
                editTextCapacity.setError("Sức chứa phải lớn hơn 0");
                editTextCapacity.requestFocus();
                return;
            }
        } catch (NumberFormatException e) {
            editTextCapacity.setError("Sức chứa không hợp lệ");
            editTextCapacity.requestFocus();
            return;
        }

        // Update or create table
        currentTable.setName(name);
        currentTable.setCapacity(capacity);
        currentTable.setStatus(status);
        currentTable.setNote(note);

        boolean success;
        if (isEditMode) {
            success = databaseHelper.updateTable(currentTable);
        } else {
            long id = databaseHelper.addTable(currentTable);
            success = id > 0;
            if (success) {
                currentTable.setId(id);
            }
        }

        if (success) {
            Toast.makeText(this, R.string.table_saved, Toast.LENGTH_SHORT).show();
            setResult(RESULT_OK);
            finish();
        } else {
            Toast.makeText(this, "Lỗi khi lưu thông tin bàn", Toast.LENGTH_SHORT).show();
        }
    }

    private void confirmDelete() {
        new AlertDialog.Builder(this)
                .setTitle("Xóa bàn")
                .setMessage(R.string.confirm_delete_table)
                .setPositiveButton(R.string.delete, (dialog, which) -> deleteTable())
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void deleteTable() {
        if (currentTable != null && currentTable.getId() > 0) {
            boolean success = databaseHelper.deleteTable(currentTable.getId());
            if (success) {
                Toast.makeText(this, R.string.table_deleted, Toast.LENGTH_SHORT).show();
                setResult(RESULT_OK);
                finish();
            } else {
                Toast.makeText(this, "Lỗi khi xóa bàn", Toast.LENGTH_SHORT).show();
            }
        }
    }
    
    private void onOrderTable() {
        if (currentTable == null || currentTable.getId() <= 0) {
            Toast.makeText(this, "Không thể đặt bàn. Vui lòng lưu thông tin bàn trước.", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Hiển thị xác nhận đặt bàn
        new AlertDialog.Builder(this)
                .setTitle("Đặt bàn")
                .setMessage("Bạn có muốn đặt bàn này và chuyển sang gọi món?")
                .setPositiveButton("Đồng ý", (dialog, which) -> {
                    processOrderTable();
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }
    
    private void processOrderTable() {
        // Đổi trạng thái bàn thành "Đang phục vụ"
        String occupiedStatus = getString(R.string.status_occupied);
        currentTable.setStatus(occupiedStatus);

        // Lưu lại thay đổi vào database
        boolean success = databaseHelper.updateTable(currentTable);
        if (success) {
            Toast.makeText(this, "Đặt bàn thành công", Toast.LENGTH_SHORT).show();
            
            // Cập nhật spinner để hiển thị trạng thái mới
            ArrayAdapter adapter = (ArrayAdapter) spinnerStatus.getAdapter();
            for (int i = 0; i < adapter.getCount(); i++) {
                if (adapter.getItem(i).toString().equals(occupiedStatus)) {
                    spinnerStatus.setSelection(i);
                    break;
                }
            }
            
            // Cập nhật hiển thị nút
            updateOrderButtonVisibility();

            // Chuyển sang màn hình gọi món
            Intent intent = new Intent(TableDetailActivity.this, OrderActivity.class);
            intent.putExtra("tableId", currentTable.getId());
            intent.putExtra("tableName", currentTable.getName());
            startActivityForResult(intent, REQUEST_ORDER_ACTIVITY);
        } else {
            Toast.makeText(this, "Lỗi khi đặt bàn", Toast.LENGTH_SHORT).show();
        }
    }
    
    private void confirmCheckout() {
        if (currentOrder == null || currentOrder.getId() <= 0) {
            Toast.makeText(this, "Không có đơn hàng nào để thanh toán", Toast.LENGTH_SHORT).show();
            Log.d("TableDetailActivity", "No order to checkout");
            return;
        }
        
        Log.d("TableDetailActivity", "Confirming checkout for order ID: " + currentOrder.getId());
        
        // Hiển thị xác nhận thanh toán
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Xác nhận thanh toán");
        
        // Tạo nội dung dialog với thông tin chi tiết đơn hàng
        StringBuilder message = new StringBuilder();
        message.append("Thông tin đơn hàng: \n\n");
        message.append("Bàn: ").append(currentTable.getName()).append("\n");
        message.append("Tổng tiền: ").append(currencyFormat.format(currentOrder.getTotalAmount())).append("\n\n");
        message.append("Bạn có chắc chắn muốn thanh toán không?");
        
        builder.setMessage(message.toString());
        builder.setPositiveButton("Thanh toán", (dialog, which) -> {
            // Hiển thị dialog tiến trình
            AlertDialog progressDialog = new AlertDialog.Builder(this)
                .setTitle("Đang xử lý")
                .setMessage("Đang cập nhật trạng thái thanh toán...")
                .setCancelable(false)
                .create();
            progressDialog.show();
            
            // Cập nhật trạng thái đơn hàng thành "Đã thanh toán"
            Log.d("TableDetailActivity", "User confirmed payment, updating order status");
            boolean success = databaseHelper.updateOrderStatus(currentOrder.getId(), "Đã thanh toán");
            
            // Đóng dialog tiến trình sau 1 giây để hiển thị rõ quá trình
            new Handler().postDelayed(() -> {
                progressDialog.dismiss();
                
                if (success) {
                    Log.d("TableDetailActivity", "Order status updated successfully");
                    // Cập nhật UI để hiển thị trạng thái mới
                    textViewOrderStatus.setText("Trạng thái: Đã thanh toán");
                    textViewOrderStatus.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
                    buttonCheckout.setVisibility(View.GONE);
                    buttonCheckoutStandalone.setVisibility(View.GONE);
                    
                    // Cập nhật trạng thái bàn về "Trống"
                    String availableStatus = getString(R.string.status_available);
                    currentTable.setStatus(availableStatus);
                    databaseHelper.updateTable(currentTable);
                    
                    // Cập nhật spinner
                    ArrayAdapter adapter = (ArrayAdapter) spinnerStatus.getAdapter();
                    for (int i = 0; i < adapter.getCount(); i++) {
                        if (adapter.getItem(i).toString().equals(availableStatus)) {
                            spinnerStatus.setSelection(i);
                            break;
                        }
                    }
                    
                    // Hiển thị thông báo thành công
                    new AlertDialog.Builder(this)
                        .setTitle("Thanh toán thành công")
                        .setMessage("Đơn hàng đã được thanh toán. Trạng thái bàn đã chuyển về 'Trống'.")
                        .setPositiveButton("OK", (d, w) -> {
                            // Sau khi ấn OK, ẩn chi tiết đơn hàng
                            orderDetailsLayout.setVisibility(View.GONE);
                            // Cập nhật nút gọi món
                            updateOrderButtonVisibility();
                        })
                        .show();
                    
                } else {
                    Log.d("TableDetailActivity", "Failed to update order status");
                    Toast.makeText(this, "Lỗi khi cập nhật trạng thái đơn hàng", Toast.LENGTH_SHORT).show();
                }
            }, 1000);
        });
        builder.setNegativeButton("Hủy", null);
        builder.show();
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (requestCode == REQUEST_ORDER_ACTIVITY) {
            if (resultCode == RESULT_OK && data != null) {
                // Nhận thông tin đơn hàng từ OrderActivity
                if (data.hasExtra("order")) {
                    currentOrder = (Order) data.getSerializableExtra("order");
                    
                    // Thông báo đặt món thành công
                    Toast.makeText(this, "Đặt món thành công! Vui lòng thanh toán khi xong.", Toast.LENGTH_LONG).show();
                    
                    // Cập nhật giao diện để hiển thị thông tin đơn hàng
                    loadCurrentOrder();
                    
                    // Hiển thị dialog hướng dẫn
                    new AlertDialog.Builder(this)
                        .setTitle("Đặt món thành công")
                        .setMessage("Đơn hàng đã được tạo. Bạn có thể xem chi tiết đơn hàng và nhấn nút 'Thanh toán' khi muốn thanh toán.")
                        .setPositiveButton("OK", null)
                        .show();
                }
            }
            // Cập nhật lại thông tin bàn
            if (currentTable != null && currentTable.getId() > 0) {
                Table updatedTable = databaseHelper.getTableById(currentTable.getId());
                if (updatedTable != null) {
                    currentTable = updatedTable;
                    populateFields();
                    updateOrderButtonVisibility();
                }
            }
        }
    }

    private void showOrderHistory() {
        if (currentTable == null || currentTable.getId() <= 0) {
            Toast.makeText(this, "Không thể xem lịch sử. Vui lòng lưu thông tin bàn trước.", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Lấy tất cả đơn hàng của bàn này
        List<Order> orderHistory = databaseHelper.getOrderHistoryForTable(currentTable.getId());
        
        if (orderHistory.isEmpty()) {
            Toast.makeText(this, "Bàn này chưa có lịch sử đơn hàng nào", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Tạo dialog để hiển thị lịch sử
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Lịch sử đơn hàng - " + currentTable.getName());
        
        // Tạo adapter để hiển thị danh sách đơn hàng
        ArrayAdapter<Order> adapter = new ArrayAdapter<Order>(this, android.R.layout.simple_list_item_1, orderHistory) {
            @NonNull
            @Override
            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                if (convertView == null) {
                    convertView = LayoutInflater.from(getContext()).inflate(android.R.layout.simple_list_item_1, parent, false);
                }
                
                Order order = getItem(position);
                TextView textView = convertView.findViewById(android.R.id.text1);
                
                SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
                
                String orderInfo = "Đơn #" + order.getId() + " - " + dateFormat.format(order.getOrderDate()) + 
                                  "\nTrạng thái: " + order.getStatus() +
                                  "\nTổng tiền: " + currencyFormat.format(order.getTotalAmount());
                textView.setText(orderInfo);
                
                return convertView;
            }
        };
        
        builder.setAdapter(adapter, (dialog, which) -> {
            // Khi người dùng chọn một đơn hàng, hiển thị chi tiết đơn hàng đó
            Order selectedOrder = orderHistory.get(which);
            showOrderDetails(selectedOrder);
        });
        
        builder.setNegativeButton("Đóng", null);
        builder.show();
    }
    
    private void showOrderDetails(Order order) {
        // Lấy chi tiết các món trong đơn hàng
        List<OrderItem> items = databaseHelper.getOrderItemsForOrder(order.getId());
        
        if (items.isEmpty()) {
            Toast.makeText(this, "Đơn hàng này không có món nào", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Tạo dialog để hiển thị chi tiết đơn hàng
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Chi tiết đơn hàng #" + order.getId());
        
        // Tạo view để hiển thị chi tiết
        View view = getLayoutInflater().inflate(R.layout.dialog_order_details, null);
        ListView listViewItems = view.findViewById(R.id.listViewOrderItemsDialog);
        TextView textViewTotal = view.findViewById(R.id.textViewOrderTotalDialog);
        
        // Tạo adapter cho danh sách món
        OrderItemAdapter adapter = new OrderItemAdapter(this, items);
        listViewItems.setAdapter(adapter);
        
        // Hiển thị tổng tiền
        textViewTotal.setText(currencyFormat.format(order.getTotalAmount()));
        
        builder.setView(view);
        builder.setPositiveButton("Đóng", null);
        builder.show();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
} 