package com.example.qlnhahangsesan.adapter;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.example.qlnhahangsesan.R;
import com.example.qlnhahangsesan.model.Food;
import com.example.qlnhahangsesan.model.FoodCategory;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class FoodAdapter extends RecyclerView.Adapter<FoodAdapter.FoodViewHolder> {

    private Context context;
    private List<Food> foodList;
    private OnFoodClickListener listener;
    private NumberFormat currencyFormat;

    public interface OnFoodClickListener {
        void onFoodClick(Food food, int position);
    }

    public FoodAdapter(Context context, List<Food> foodList, OnFoodClickListener listener) {
        this.context = context;
        this.foodList = foodList;
        this.listener = listener;
        this.currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
    }

    @NonNull
    @Override
    public FoodViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_food, parent, false);
        return new FoodViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull FoodViewHolder holder, int position) {
        Food food = foodList.get(position);
        
        holder.textViewName.setText(food.getName());
        
        // Display categories text
        if (holder.textViewCategories != null) {
            holder.textViewCategories.setText("Loại: " + food.getCategoryString());
            holder.textViewCategories.setVisibility(View.VISIBLE);
        }
        
        holder.textViewPrice.setText(currencyFormat.format(food.getPrice()));
        
        // Set availability status
        if (food.isAvailable()) {
            holder.textViewAvailability.setText("Còn món");
            holder.textViewAvailability.setTextColor(context.getResources().getColor(android.R.color.holo_green_dark));
        } else {
            holder.textViewAvailability.setText("Hết món");
            holder.textViewAvailability.setTextColor(context.getResources().getColor(android.R.color.holo_red_dark));
        }
        
        // Load image with Glide
        loadFoodImage(holder.imageViewFood, food.getImageUrl());
        
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onFoodClick(food, position);
            }
        });
    }

    private void loadFoodImage(ImageView imageView, String imageUrl) {
        RequestOptions requestOptions = new RequestOptions()
                .placeholder(R.mipmap.ic_launcher)
                .error(R.mipmap.ic_launcher)
                .diskCacheStrategy(DiskCacheStrategy.ALL);

        if (imageUrl != null && !imageUrl.isEmpty()) {
            try {
                // Try to load from URI
                Uri uri = Uri.parse(imageUrl);
                Glide.with(context)
                        .load(uri)
                        .apply(requestOptions)
                        .centerCrop()
                        .into(imageView);
            } catch (Exception e) {
                // If URI parsing fails, load placeholder
                Glide.with(context)
                        .load(R.mipmap.ic_launcher)
                        .apply(requestOptions)
                        .centerCrop()
                        .into(imageView);
            }
        } else {
            // If no image URL, load placeholder
            Glide.with(context)
                    .load(R.mipmap.ic_launcher)
                    .apply(requestOptions)
                    .centerCrop()
                    .into(imageView);
        }
    }

    @Override
    public int getItemCount() {
        return foodList != null ? foodList.size() : 0;
    }

    public void updateData(List<Food> newFoodList) {
        this.foodList = newFoodList;
        notifyDataSetChanged();
    }

    public static class FoodViewHolder extends RecyclerView.ViewHolder {
        public ImageView imageViewFood;
        public TextView textViewName;
        public TextView textViewPrice;
        public TextView textViewAvailability;
        public TextView textViewCategories;

        public FoodViewHolder(View view) {
            super(view);
            imageViewFood = view.findViewById(R.id.imageViewFood);
            textViewName = view.findViewById(R.id.textViewFoodName);
            textViewPrice = view.findViewById(R.id.textViewFoodPrice);
            textViewAvailability = view.findViewById(R.id.textViewFoodAvailability);
            textViewCategories = view.findViewById(R.id.textViewFoodCategories);
        }
    }
} 