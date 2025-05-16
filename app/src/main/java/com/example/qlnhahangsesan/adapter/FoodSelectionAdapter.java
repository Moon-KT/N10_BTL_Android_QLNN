package com.example.qlnhahangsesan.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.qlnhahangsesan.R;
import com.example.qlnhahangsesan.model.Food;
import com.example.qlnhahangsesan.model.OrderItem;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class FoodSelectionAdapter extends BaseAdapter {
    
    private Context context;
    private List<Food> foodList;
    private List<Food> filteredList;
    private Map<Long, Integer> quantityMap = new HashMap<>();
    private NumberFormat currencyFormat;
    
    public FoodSelectionAdapter(Context context, List<Food> foodList) {
        this.context = context;
        this.foodList = foodList;
        this.filteredList = new ArrayList<>(foodList);
        this.currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
    }
    
    public void setFilteredList(List<Food> filteredList) {
        this.filteredList = filteredList;
        notifyDataSetChanged();
    }
    
    @Override
    public int getCount() {
        return filteredList.size();
    }
    
    @Override
    public Object getItem(int position) {
        return filteredList.get(position);
    }
    
    @Override
    public long getItemId(int position) {
        return filteredList.get(position).getId();
    }
    
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_food_selection, parent, false);
            
            holder = new ViewHolder();
            holder.textViewFoodName = convertView.findViewById(R.id.textViewFoodName);
            holder.textViewFoodCategory = convertView.findViewById(R.id.textViewFoodCategory);
            holder.textViewFoodPrice = convertView.findViewById(R.id.textViewFoodPrice);
            holder.textViewQuantity = convertView.findViewById(R.id.textViewQuantity);
            holder.buttonDecrease = convertView.findViewById(R.id.buttonDecrease);
            holder.buttonIncrease = convertView.findViewById(R.id.buttonIncrease);
            
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        
        Food food = filteredList.get(position);
        
        // Set food details
        holder.textViewFoodName.setText(food.getName());
        holder.textViewFoodCategory.setText(food.getCategoryString());
        holder.textViewFoodPrice.setText(currencyFormat.format(food.getPrice()));
        
        // Set quantity
        int quantity = quantityMap.getOrDefault(food.getId(), 0);
        holder.textViewQuantity.setText(String.valueOf(quantity));
        
        // Set button actions
        holder.buttonDecrease.setOnClickListener(v -> {
            int currentQuantity = quantityMap.getOrDefault(food.getId(), 0);
            if (currentQuantity > 0) {
                quantityMap.put(food.getId(), currentQuantity - 1);
                holder.textViewQuantity.setText(String.valueOf(currentQuantity - 1));
            }
        });
        
        holder.buttonIncrease.setOnClickListener(v -> {
            int currentQuantity = quantityMap.getOrDefault(food.getId(), 0);
            quantityMap.put(food.getId(), currentQuantity + 1);
            holder.textViewQuantity.setText(String.valueOf(currentQuantity + 1));
        });
        
        return convertView;
    }
    
    /**
     * Get selected food items with quantity > 0
     */
    public List<OrderItem> getSelectedItems() {
        List<OrderItem> selectedItems = new ArrayList<>();
        
        for (Food food : foodList) {
            int quantity = quantityMap.getOrDefault(food.getId(), 0);
            if (quantity > 0) {
                OrderItem item = new OrderItem();
                item.setFoodId(food.getId());
                item.setName(food.getName());
                item.setQuantity(quantity);
                item.setPrice(food.getPrice());
                selectedItems.add(item);
            }
        }
        
        return selectedItems;
    }
    
    private static class ViewHolder {
        TextView textViewFoodName;
        TextView textViewFoodCategory;
        TextView textViewFoodPrice;
        TextView textViewQuantity;
        ImageButton buttonDecrease;
        ImageButton buttonIncrease;
    }
} 