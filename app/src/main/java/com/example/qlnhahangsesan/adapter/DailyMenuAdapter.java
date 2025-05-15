package com.example.qlnhahangsesan.adapter;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.example.qlnhahangsesan.R;
import com.example.qlnhahangsesan.model.DailyMenu;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class DailyMenuAdapter extends RecyclerView.Adapter<DailyMenuAdapter.DailyMenuViewHolder> {

    private Context context;
    private List<DailyMenu> menuList;
    private OnMenuItemClickListener listener;
    private NumberFormat currencyFormat;

    public interface OnMenuItemClickListener {
        void onMenuItemClick(DailyMenu menuItem, int position);
    }

    public DailyMenuAdapter(Context context, List<DailyMenu> menuList, OnMenuItemClickListener listener) {
        this.context = context;
        this.menuList = menuList;
        this.listener = listener;
        this.currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
    }

    @NonNull
    @Override
    public DailyMenuViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_daily_menu, parent, false);
        return new DailyMenuViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull DailyMenuViewHolder holder, int position) {
        DailyMenu menuItem = menuList.get(position);
        
        holder.textViewName.setText(menuItem.getFoodName());
        holder.textViewCategory.setText(menuItem.getFoodCategory());
        holder.textViewPrice.setText(currencyFormat.format(menuItem.getFoodPrice()));
        holder.textViewQuantity.setText(context.getString(R.string.quantity_format, menuItem.getQuantity()));
        
        // Highlight featured items
        if (menuItem.isFeatured()) {
            holder.cardView.setCardBackgroundColor(ContextCompat.getColor(context, R.color.featured_item));
            holder.textViewFeatured.setVisibility(View.VISIBLE);
        } else {
            holder.cardView.setCardBackgroundColor(ContextCompat.getColor(context, android.R.color.white));
            holder.textViewFeatured.setVisibility(View.GONE);
        }
        
        // Load image with Glide
        loadFoodImage(holder.imageViewFood, menuItem.getFoodImageUrl());
        
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onMenuItemClick(menuItem, position);
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
        return menuList != null ? menuList.size() : 0;
    }

    public void updateData(List<DailyMenu> newMenuList) {
        this.menuList = newMenuList;
        notifyDataSetChanged();
    }

    public static class DailyMenuViewHolder extends RecyclerView.ViewHolder {
        public CardView cardView;
        public ImageView imageViewFood;
        public TextView textViewName;
        public TextView textViewCategory;
        public TextView textViewPrice;
        public TextView textViewQuantity;
        public TextView textViewFeatured;

        public DailyMenuViewHolder(View view) {
            super(view);
            cardView = view.findViewById(R.id.cardViewDailyMenu);
            imageViewFood = view.findViewById(R.id.imageViewFood);
            textViewName = view.findViewById(R.id.textViewFoodName);
            textViewCategory = view.findViewById(R.id.textViewFoodCategory);
            textViewPrice = view.findViewById(R.id.textViewFoodPrice);
            textViewQuantity = view.findViewById(R.id.textViewQuantity);
            textViewFeatured = view.findViewById(R.id.textViewFeatured);
        }
    }
} 