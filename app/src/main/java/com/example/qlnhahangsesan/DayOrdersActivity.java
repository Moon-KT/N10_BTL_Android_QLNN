package com.example.qlnhahangsesan;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.qlnhahangsesan.adapter.DayOrderAdapter;
import com.example.qlnhahangsesan.database.DatabaseHelper;
import com.example.qlnhahangsesan.model.Order;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DayOrdersActivity extends AppCompatActivity implements DayOrderAdapter.OnOrderClickListener {

    private static final String TAG = "DayOrdersActivity";
    
    private RecyclerView recyclerViewDayOrders;
    private TextView textViewDayTitle;
    private TextView textViewTotalOrders;
    private TextView textViewDayTotal;
    private TextView textViewEmptyOrders;
    
    private String date;
    private DatabaseHelper databaseHelper;
    private List<Order> ordersList;
    private NumberFormat currencyFormat;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_day_orders);
        
        Log.d(TAG, "onCreate: Activity started");
        
        // Initialize database helper
        databaseHelper = DatabaseHelper.getInstance(this);
        
        // Initialize currency formatter
        currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        
        // Setup toolbar
        setSupportActionBar(findViewById(R.id.toolbar));
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Thống kê đơn hàng theo ngày");
        }
        
        // Initialize views
        recyclerViewDayOrders = findViewById(R.id.recyclerViewDayOrders);
        textViewDayTitle = findViewById(R.id.textViewDayTitle);
        textViewTotalOrders = findViewById(R.id.textViewTotalOrders);
        textViewDayTotal = findViewById(R.id.textViewDayTotal);
        textViewEmptyOrders = findViewById(R.id.textViewEmptyOrders);
        
        // Get date from intent
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("date")) {
            date = intent.getStringExtra("date");
            Log.d(TAG, "onCreate: Received date from intent: " + date);
            
            // Load orders for this date
            loadOrders();
        } else {
            // Show error and finish activity
            Log.e(TAG, "onCreate: No date provided in intent");
            Toast.makeText(this, "Lỗi: Không có ngày được chọn", Toast.LENGTH_SHORT).show();
            finish();
        }
    }
    
    private void loadOrders() {
        Log.d(TAG, "loadOrders: Loading orders for date: " + date);
        
        // Get orders for the specified date
        ordersList = databaseHelper.getOrdersByDate(date);
        
        Log.d(TAG, "loadOrders: Found " + ordersList.size() + " orders for date: " + date);
        
        // Format the date for display
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            SimpleDateFormat outputFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            Date dateObj = inputFormat.parse(date);
            String formattedDate = outputFormat.format(dateObj);
            textViewDayTitle.setText("Đơn hàng ngày " + formattedDate);
            Log.d(TAG, "loadOrders: Formatted date for display: " + formattedDate);
        } catch (Exception e) {
            Log.e(TAG, "loadOrders: Error formatting date", e);
            textViewDayTitle.setText("Đơn hàng ngày " + date);
        }
        
        // Update UI based on results
        if (ordersList.isEmpty()) {
            Log.d(TAG, "loadOrders: No orders found, showing empty state");
            recyclerViewDayOrders.setVisibility(View.GONE);
            textViewEmptyOrders.setVisibility(View.VISIBLE);
            textViewTotalOrders.setText("Tổng số đơn hàng: 0");
            textViewDayTotal.setText("Tổng doanh thu: " + currencyFormat.format(0));
        } else {
            Log.d(TAG, "loadOrders: Displaying " + ordersList.size() + " orders");
            recyclerViewDayOrders.setVisibility(View.VISIBLE);
            textViewEmptyOrders.setVisibility(View.GONE);
            
            // Set up the RecyclerView
            recyclerViewDayOrders.setLayoutManager(new LinearLayoutManager(this));
            DayOrderAdapter adapter = new DayOrderAdapter(this, ordersList, this);
            recyclerViewDayOrders.setAdapter(adapter);
            
            // Calculate and display totals
            textViewTotalOrders.setText("Tổng số đơn hàng: " + ordersList.size());
            
            double totalRevenue = 0;
            for (Order order : ordersList) {
                totalRevenue += order.getTotalAmount();
            }
            Log.d(TAG, "loadOrders: Total revenue: " + totalRevenue);
            textViewDayTotal.setText("Tổng doanh thu: " + currencyFormat.format(totalRevenue));
        }
    }
    
    @Override
    public void onOrderClick(Order order) {
        // Open OrderDetailsActivity with the selected order
        Log.d(TAG, "onOrderClick: Opening details for order ID: " + order.getId());
        Intent intent = new Intent(this, OrderDetailsActivity.class);
        intent.putExtra("orderId", order.getId());
        startActivity(intent);
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