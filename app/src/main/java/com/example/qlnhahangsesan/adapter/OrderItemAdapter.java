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

public class OrderItemAdapter extends ArrayAdapter<OrderItem> {
    private final Context context;
    private final List<OrderItem> orderItems;
    private final NumberFormat currencyFormat;

    public OrderItemAdapter(Context context, List<OrderItem> orderItems) {
        super(context, R.layout.item_order, orderItems);
        this.context = context;
        this.orderItems = orderItems;
        this.currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_order, parent, false);
        }

        OrderItem item = orderItems.get(position);

        TextView textViewName = convertView.findViewById(R.id.textViewOrderItemName);
        TextView textViewQuantity = convertView.findViewById(R.id.textViewOrderItemQuantity);
        TextView textViewPrice = convertView.findViewById(R.id.textViewOrderItemPrice);
        TextView textViewTotal = convertView.findViewById(R.id.textViewOrderItemTotal);

        textViewName.setText(item.getName());
        textViewQuantity.setText(String.valueOf(item.getQuantity()));
        textViewPrice.setText(currencyFormat.format(item.getPrice()));
        textViewTotal.setText(currencyFormat.format(item.getTotalPrice()));

        return convertView;
    }
} 