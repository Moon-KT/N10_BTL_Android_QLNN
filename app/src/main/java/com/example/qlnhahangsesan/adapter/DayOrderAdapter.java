package com.example.qlnhahangsesan.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.qlnhahangsesan.R;
import com.example.qlnhahangsesan.model.Order;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class DayOrderAdapter extends RecyclerView.Adapter<DayOrderAdapter.OrderViewHolder> {

    private Context context;
    private List<Order> orderList;
    private OnOrderClickListener listener;
    private NumberFormat currencyFormat;
    private SimpleDateFormat timeFormat;

    public interface OnOrderClickListener {
        void onOrderClick(Order order);
    }

    public DayOrderAdapter(Context context, List<Order> orderList, OnOrderClickListener listener) {
        this.context = context;
        this.orderList = orderList;
        this.listener = listener;
        this.currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        this.timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_day_order, parent, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        Order order = orderList.get(position);
        
        // Set order ID
        holder.textViewOrderId.setText(String.valueOf(order.getId()));
        
        // Set table name
        holder.textViewTableName.setText(order.getTableName());
        
        // Set order status
        String status = order.getStatus();
        holder.textViewOrderStatus.setText(status);
        
        // Set color based on status
        if (status.equals("Chưa thanh toán")) {
            holder.textViewOrderStatus.setTextColor(context.getResources().getColor(android.R.color.holo_red_light));
        } else {
            holder.textViewOrderStatus.setTextColor(context.getResources().getColor(android.R.color.holo_green_dark));
        }
        
        // Set time
        String time = timeFormat.format(order.getOrderDate());
        holder.textViewOrderTime.setText(time);
        
        // Set total amount
        holder.textViewOrderTotal.setText(currencyFormat.format(order.getTotalAmount()));
        
        // Set click listener
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onOrderClick(order);
            }
        });
    }

    @Override
    public int getItemCount() {
        return orderList.size();
    }

    static class OrderViewHolder extends RecyclerView.ViewHolder {
        TextView textViewOrderId;
        TextView textViewTableName;
        TextView textViewOrderStatus;
        TextView textViewOrderTime;
        TextView textViewOrderTotal;

        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewOrderId = itemView.findViewById(R.id.textViewOrderId);
            textViewTableName = itemView.findViewById(R.id.textViewTableName);
            textViewOrderStatus = itemView.findViewById(R.id.textViewOrderStatus);
            textViewOrderTime = itemView.findViewById(R.id.textViewOrderTime);
            textViewOrderTotal = itemView.findViewById(R.id.textViewOrderTotal);
        }
    }
} 