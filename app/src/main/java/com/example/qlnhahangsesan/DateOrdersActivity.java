package com.example.qlnhahangsesan;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.qlnhahangsesan.adapter.OrderItemAdapter;
import com.example.qlnhahangsesan.database.DatabaseHelper;
import com.example.qlnhahangsesan.model.Order;
import com.example.qlnhahangsesan.model.OrderItem;

import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DateOrdersActivity extends AppCompatActivity {

    private TextView textViewDateHeader;
    private TextView textViewTotalRevenue;
    private RecyclerView recyclerViewOrders;
    private TextView textViewEmpty;

    private DatabaseHelper databaseHelper;
    private List<Order> orderList;
    private String selectedDate;
    private NumberFormat currencyFormat;
    private SimpleDateFormat dbDateFormat;
    private SimpleDateFormat displayDateFormat;
    private SimpleDateFormat timeFormat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_date_orders);

        // Initialize date formatters
        dbDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        displayDateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
        currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));

        // Get the date from intent
        if (getIntent().hasExtra("date")) {
            selectedDate = getIntent().getStringExtra("date");
        } else {
            Toast.makeText(this, "Không có thông tin ngày", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialize database helper
        databaseHelper = DatabaseHelper.getInstance(this);

        // Initialize views
        textViewDateHeader = findViewById(R.id.textViewDateHeader);
        textViewTotalRevenue = findViewById(R.id.textViewTotalRevenue);
        recyclerViewOrders = findViewById(R.id.recyclerViewOrders);
        textViewEmpty = findViewById(R.id.textViewEmpty);

        // Set up RecyclerView
        recyclerViewOrders.setLayoutManager(new LinearLayoutManager(this));

        // Set up action bar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Đơn hàng theo ngày");
        }

        // Format the date for display
        String displayDate = selectedDate;
        try {
            Date date = dbDateFormat.parse(selectedDate);
            displayDate = displayDateFormat.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        // Set header text
        textViewDateHeader.setText("Đơn hàng ngày " + displayDate);

        // Load orders
        loadOrders();
    }

    private void loadOrders() {
        // Get orders for the selected date
        orderList = databaseHelper.getOrdersByDate(selectedDate);

        if (orderList.isEmpty()) {
            // Show empty message
            textViewEmpty.setVisibility(View.VISIBLE);
            recyclerViewOrders.setVisibility(View.GONE);
            textViewTotalRevenue.setText("Tổng doanh thu: " + currencyFormat.format(0));
        } else {
            // Calculate total revenue
            double totalRevenue = 0;
            for (Order order : orderList) {
                if (order.getStatus().equals("Đã thanh toán")) {
                    totalRevenue += order.getTotalAmount();
                }
            }

            // Show orders
            textViewEmpty.setVisibility(View.GONE);
            recyclerViewOrders.setVisibility(View.VISIBLE);
            textViewTotalRevenue.setText("Tổng doanh thu: " + currencyFormat.format(totalRevenue));

            // Set up adapter
            OrderAdapter adapter = new OrderAdapter(orderList);
            recyclerViewOrders.setAdapter(adapter);
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

    // Adapter for the orders
    private class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.OrderViewHolder> {

        private final List<Order> orders;

        public OrderAdapter(List<Order> orders) {
            this.orders = orders;
        }

        @NonNull
        @Override
        public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_order, parent, false);
            return new OrderViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
            Order order = orders.get(position);

            // Set order ID
            holder.textViewOrderId.setText("#" + order.getId());

            // Set order time
            holder.textViewOrderTime.setText(timeFormat.format(order.getOrderDate()));

            // Set table info
            String tableName = order.getTableName() != null ? order.getTableName() : "Bàn " + order.getTableId();
            holder.textViewTableInfo.setText(tableName);

            // Set order status with colored background
            if (order.getStatus().equals("Đã thanh toán")) {
                holder.textViewOrderStatus.setBackgroundResource(R.color.colorPrimary);
            } else {
                holder.textViewOrderStatus.setBackgroundResource(android.R.color.holo_orange_dark);
            }
            holder.textViewOrderStatus.setText(order.getStatus());

            // Set order amount
            holder.textViewOrderAmount.setText(currencyFormat.format(order.getTotalAmount()));

            // Set click listener
            holder.itemView.setOnClickListener(v -> {
                showOrderDetails(order);
            });
        }

        @Override
        public int getItemCount() {
            return orders.size();
        }

        class OrderViewHolder extends RecyclerView.ViewHolder {
            TextView textViewOrderId;
            TextView textViewOrderTime;
            TextView textViewTableInfo;
            TextView textViewOrderStatus;
            TextView textViewOrderAmount;

            public OrderViewHolder(@NonNull View itemView) {
                super(itemView);
                textViewOrderId = itemView.findViewById(R.id.textViewOrderId);
                textViewOrderTime = itemView.findViewById(R.id.textViewOrderTime);
                textViewTableInfo = itemView.findViewById(R.id.textViewTableInfo);
                textViewOrderStatus = itemView.findViewById(R.id.textViewOrderStatus);
                textViewOrderAmount = itemView.findViewById(R.id.textViewOrderAmount);
            }
        }
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
} 