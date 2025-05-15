package com.example.qlnhahangsesan.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.qlnhahangsesan.R;
import com.example.qlnhahangsesan.model.DailyMenu;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class DailyMenuAdapter extends RecyclerView.Adapter<DailyMenuAdapter.DailyMenuViewHolder> {

    private Context context;
    private List<DailyMenu> menuItems;
    private OnMenuItemClickListener listener;
    private NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));

    public interface OnMenuItemClickListener {
        void onMenuItemClick(DailyMenu menuItem);
    }

    public DailyMenuAdapter(Context context, List<DailyMenu> menuItems, OnMenuItemClickListener listener) {
        this.context = context;
        this.menuItems = menuItems;
        this.listener = listener;
    }

    @NonNull
    @Override
    public DailyMenuViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_daily_menu, parent, false);
        return new DailyMenuViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DailyMenuViewHolder holder, int position) {
        DailyMenu menuItem = menuItems.get(position);
        
        // Set food name
        holder.textViewFoodName.setText(menuItem.getFoodName());
        
        // Set food category
        holder.textViewCategory.setText(menuItem.getFoodCategory());
        
        // Set food price
        holder.textViewPrice.setText(currencyFormatter.format(menuItem.getFoodPrice()));
        
        // Set food quantity
        holder.textViewQuantity.setText("SL: " + menuItem.getQuantity());
        
        // Set featured badge visibility
        if (menuItem.isFeatured()) {
            holder.textViewFeatured.setVisibility(View.VISIBLE);
        } else {
            holder.textViewFeatured.setVisibility(View.GONE);
        }
        
        // Load food image
        String imageUrl = menuItem.getFoodImageUrl();
        if (imageUrl != null && !imageUrl.isEmpty()) {
            Glide.with(context)
                    .load(imageUrl)
                    .placeholder(R.drawable.placeholder_food)
                    .error(R.drawable.placeholder_food)
                    .into(holder.imageViewFood);
        } else {
            holder.imageViewFood.setImageResource(R.drawable.placeholder_food);
        }
        
        // Set click listener
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onMenuItemClick(menuItem);
            }
        });
    }

    @Override
    public int getItemCount() {
        return menuItems != null ? menuItems.size() : 0;
    }

    static class DailyMenuViewHolder extends RecyclerView.ViewHolder {
        ImageView imageViewFood;
        TextView textViewFoodName;
        TextView textViewCategory;
        TextView textViewPrice;
        TextView textViewQuantity;
        TextView textViewFeatured;

        public DailyMenuViewHolder(@NonNull View itemView) {
            super(itemView);
            imageViewFood = itemView.findViewById(R.id.imageViewFood);
            textViewFoodName = itemView.findViewById(R.id.textViewFoodName);
            textViewCategory = itemView.findViewById(R.id.textViewFoodCategory);
            textViewPrice = itemView.findViewById(R.id.textViewFoodPrice);
            textViewQuantity = itemView.findViewById(R.id.textViewQuantity);
            textViewFeatured = itemView.findViewById(R.id.textViewFeatured);
        }
    }
} 