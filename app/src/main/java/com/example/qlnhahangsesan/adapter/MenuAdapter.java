package com.example.qlnhahangsesan.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.qlnhahangsesan.R;
import com.example.qlnhahangsesan.model.Food;

import java.text.NumberFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MenuAdapter extends ArrayAdapter<Food> {
    // Define the interface for quantity change events
    public interface OnQuantityChangedListener {
        void onQuantityChanged(Food food, int quantity);
    }
    
    private Context context;
    private List<Food> foodList;
    private NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
    private OnQuantityChangedListener listener;
    private Map<Long, Integer> selectedQuantities = new HashMap<>();

    public MenuAdapter(@NonNull Context context, List<Food> foodList) {
        super(context, 0, foodList);
        this.context = context;
        this.foodList = foodList;
    }
    
    // Constructor with listener
    public MenuAdapter(@NonNull Context context, List<Food> foodList, OnQuantityChangedListener listener) {
        super(context, 0, foodList);
        this.context = context;
        this.foodList = foodList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_menu_order, parent, false);
            holder = new ViewHolder();
            holder.foodNameTextView = convertView.findViewById(R.id.textViewFoodName);
            holder.foodPriceTextView = convertView.findViewById(R.id.textViewFoodPrice);
            holder.foodImageView = convertView.findViewById(R.id.imageViewFood);
            holder.quantityTextView = convertView.findViewById(R.id.textViewQuantity);
            holder.decreaseButton = convertView.findViewById(R.id.buttonDecrease);
            holder.increaseButton = convertView.findViewById(R.id.buttonIncrease);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Food currentFood = foodList.get(position);
        
        holder.foodNameTextView.setText(currentFood.getName());
        holder.foodPriceTextView.setText(currencyFormat.format(currentFood.getPrice()));
        
        // Hiển thị số lượng đã chọn hoặc 0 nếu chưa chọn
        int quantity = selectedQuantities.containsKey(currentFood.getId()) ? 
                selectedQuantities.get(currentFood.getId()) : 0;
        holder.quantityTextView.setText(String.valueOf(quantity));
        
        // Using final to access from inner classes
        final ViewHolder finalHolder = holder;
        final Food finalFood = currentFood;
        
        holder.decreaseButton.setOnClickListener(v -> {
            int currentQuantity = Integer.parseInt(finalHolder.quantityTextView.getText().toString());
            if (currentQuantity > 0) {
                int newQuantity = currentQuantity - 1;
                finalHolder.quantityTextView.setText(String.valueOf(newQuantity));
                
                // Lưu số lượng đã chọn
                selectedQuantities.put(finalFood.getId(), newQuantity);
                
                // Notify through the listener
                if (listener != null) {
                    listener.onQuantityChanged(finalFood, newQuantity);
                }
            }
        });
        
        holder.increaseButton.setOnClickListener(v -> {
            int currentQuantity = Integer.parseInt(finalHolder.quantityTextView.getText().toString());
            int newQuantity = currentQuantity + 1;
            finalHolder.quantityTextView.setText(String.valueOf(newQuantity));
            
            // Lưu số lượng đã chọn
            selectedQuantities.put(finalFood.getId(), newQuantity);
            
            // Notify through the listener
            if (listener != null) {
                listener.onQuantityChanged(finalFood, newQuantity);
            }
        });

        return convertView;
    }
    
    // Phương thức để cập nhật số lượng món đã chọn từ bên ngoài
    public void updateQuantity(long foodId, int quantity) {
        selectedQuantities.put(foodId, quantity);
        notifyDataSetChanged();
    }
    
    // Phương thức để cập nhật danh sách món ăn
    public void updateFoodList(List<Food> newFoodList) {
        this.foodList = newFoodList;
        // Xóa các số lượng đã chọn cho các món không còn trong danh sách mới
        Map<Long, Integer> updatedQuantities = new HashMap<>();
        for (Food food : newFoodList) {
            if (selectedQuantities.containsKey(food.getId())) {
                updatedQuantities.put(food.getId(), selectedQuantities.get(food.getId()));
            }
        }
        selectedQuantities = updatedQuantities;
        clear();
        addAll(newFoodList);
        notifyDataSetChanged();
    }
    
    private static class ViewHolder {
        TextView foodNameTextView;
        TextView foodPriceTextView;
        ImageView foodImageView;
        TextView quantityTextView;
        Button decreaseButton;
        Button increaseButton;
    }
} 