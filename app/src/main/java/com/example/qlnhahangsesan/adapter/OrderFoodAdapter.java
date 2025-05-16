package com.example.qlnhahangsesan.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.qlnhahangsesan.R;
import com.example.qlnhahangsesan.model.OrderItem;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class OrderFoodAdapter extends ArrayAdapter<OrderItem> {
    
    private Context context;
    private List<OrderItem> orderItems;
    private NumberFormat currencyFormat;
    
    public OrderFoodAdapter(@NonNull Context context, List<OrderItem> orderItems) {
        super(context, 0, orderItems);
        this.context = context;
        this.orderItems = orderItems;
        this.currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
    }
    
    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_order_food, parent, false);
        }
        
        OrderItem item = orderItems.get(position);
        
        // Set index (position + 1 for human-readable index starting from 1)
        TextView textViewIndex = convertView.findViewById(R.id.textViewIndex);
        textViewIndex.setText(String.valueOf(position + 1));
        
        // Set food name
        TextView textViewFoodName = convertView.findViewById(R.id.textViewFoodName);
        textViewFoodName.setText(item.getName());
        
        // Set quantity
        TextView textViewQuantity = convertView.findViewById(R.id.textViewQuantity);
        textViewQuantity.setText(String.valueOf(item.getQuantity()));
        
        // Set price
        TextView textViewPrice = convertView.findViewById(R.id.textViewPrice);
        double totalPrice = item.getPrice() * item.getQuantity();
        textViewPrice.setText(currencyFormat.format(totalPrice));
        
        return convertView;
    }
} 