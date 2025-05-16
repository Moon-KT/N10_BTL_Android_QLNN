package com.example.qlnhahangsesan;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.qlnhahangsesan.adapter.OrderItemAdapter;
import com.example.qlnhahangsesan.database.DatabaseHelper;
import com.example.qlnhahangsesan.model.Order;
import com.example.qlnhahangsesan.model.OrderItem;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class OrderDetailsActivity extends AppCompatActivity {

    private TextView textViewOrderId;
    private TextView textViewOrderStatus;
    private TextView textViewTableName;
    private TextView textViewOrderDateTime;
    private ListView listViewOrderItems;
    private TextView textViewTotalAmount;
    
    private DatabaseHelper databaseHelper;
    private Order currentOrder;
    private List<OrderItem> orderItems;
    private NumberFormat currencyFormat;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_details);
        
        // Initialize database helper
        databaseHelper = DatabaseHelper.getInstance(this);
        
        // Initialize currency formatter
        currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        
        // Setup toolbar
        setSupportActionBar(findViewById(R.id.toolbar));
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Chi tiết đơn hàng");
        }
        
        // Initialize views
        textViewOrderId = findViewById(R.id.textViewOrderId);
        textViewOrderStatus = findViewById(R.id.textViewOrderStatus);
        textViewTableName = findViewById(R.id.textViewTableName);
        textViewOrderDateTime = findViewById(R.id.textViewOrderDateTime);
        listViewOrderItems = findViewById(R.id.listViewOrderItems);
        textViewTotalAmount = findViewById(R.id.textViewTotalAmount);
        
        // Get order ID from intent
        if (getIntent().hasExtra("orderId")) {
            long orderId = getIntent().getLongExtra("orderId", -1);
            if (orderId > 0) {
                loadOrderDetails(orderId);
            } else {
                Toast.makeText(this, "Không tìm thấy đơn hàng", Toast.LENGTH_SHORT).show();
                finish();
            }
        } else {
            Toast.makeText(this, "Không có thông tin đơn hàng", Toast.LENGTH_SHORT).show();
            finish();
        }
    }
    
    private void loadOrderDetails(long orderId) {
        // Get order details from database
        currentOrder = databaseHelper.getOrderById(orderId);
        
        if (currentOrder != null) {
            // Set order ID
            textViewOrderId.setText(String.valueOf(currentOrder.getId()));
            
            // Set order status
            String status = currentOrder.getStatus();
            textViewOrderStatus.setText(status);
            
            // Set color based on status
            if (status.equals("Chưa thanh toán")) {
                textViewOrderStatus.setTextColor(getResources().getColor(android.R.color.holo_red_light));
            } else {
                textViewOrderStatus.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
            }
            
            // Set table name
            String tableName = currentOrder.getTableName();
            if (tableName == null || tableName.isEmpty()) {
                // If table name is not available in the order, fetch it from the database
                tableName = databaseHelper.getTableNameById(currentOrder.getTableId());
            }
            textViewTableName.setText(tableName);
            
            // Set order date and time
            SimpleDateFormat dateTimeFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
            String dateTime = dateTimeFormat.format(currentOrder.getOrderDate());
            textViewOrderDateTime.setText(dateTime);
            
            // Get order items
            orderItems = currentOrder.getOrderItems();
            if (orderItems == null || orderItems.isEmpty()) {
                orderItems = databaseHelper.getOrderItemsForOrder(orderId);
            }
            
            // Set up list view with order items
            OrderItemAdapter adapter = new OrderItemAdapter(this, orderItems);
            listViewOrderItems.setAdapter(adapter);
            
            // Set total amount
            textViewTotalAmount.setText(currencyFormat.format(currentOrder.getTotalAmount()));
        } else {
            Toast.makeText(this, "Không tìm thấy đơn hàng", Toast.LENGTH_SHORT).show();
            finish();
        }
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
} 