package com.example.qlnhahangsesan;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.example.qlnhahangsesan.adapter.DayOrderAdapter;
import com.example.qlnhahangsesan.database.DatabaseHelper;
import com.example.qlnhahangsesan.model.Order;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DayOrdersActivity extends AppCompatActivity implements DayOrderAdapter.OnOrderClickListener {

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
        if (getIntent().hasExtra("date")) {
            date = getIntent().getStringExtra("date");
            
            // Load orders for this date
            loadOrders();
        } else {
            // Show error and finish activity
            finish();
        }
    }
    
    private void loadOrders() {
        // Get orders for the specified date
        ordersList = databaseHelper.getOrdersByDate(date);
        
        // Format the date for display
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            SimpleDateFormat outputFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            Date dateObj = inputFormat.parse(date);
            String formattedDate = outputFormat.format(dateObj);
            textViewDayTitle.setText("Đơn hàng ngày " + formattedDate);
        } catch (Exception e) {
            textViewDayTitle.setText("Đơn hàng ngày " + date);
        }
        
        // Update UI based on results
        if (ordersList.isEmpty()) {
            recyclerViewDayOrders.setVisibility(View.GONE);
            textViewEmptyOrders.setVisibility(View.VISIBLE);
            textViewTotalOrders.setText("Tổng số đơn hàng: 0");
            textViewDayTotal.setText("Tổng doanh thu: " + currencyFormat.format(0));
        } else {
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
            textViewDayTotal.setText("Tổng doanh thu: " + currencyFormat.format(totalRevenue));
        }
    }
    
    @Override
    public void onOrderClick(Order order) {
        // Open OrderDetailsActivity with the selected order
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