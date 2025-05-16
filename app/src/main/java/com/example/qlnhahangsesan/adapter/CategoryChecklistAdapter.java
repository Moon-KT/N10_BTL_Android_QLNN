package com.example.qlnhahangsesan.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.qlnhahangsesan.R;
import com.example.qlnhahangsesan.model.Food;
import com.example.qlnhahangsesan.model.FoodCategory;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CategoryChecklistAdapter extends RecyclerView.Adapter<CategoryChecklistAdapter.CategoryViewHolder> {

    private Context context;
    private List<FoodCategory> allCategories;
    private Set<FoodCategory> selectedCategories;
    private OnCategorySelectionChangedListener listener;

    public interface OnCategorySelectionChangedListener {
        void onCategorySelectionChanged(List<FoodCategory> selectedCategories);
    }

    public CategoryChecklistAdapter(Context context, OnCategorySelectionChangedListener listener) {
        this.context = context;
        this.allCategories = new ArrayList<>();
        for (FoodCategory category : FoodCategory.values()) {
            allCategories.add(category);
        }
        this.selectedCategories = new HashSet<>();
        this.listener = listener;
    }

    public void setSelectedCategories(List<FoodCategory> selectedCategories) {
        this.selectedCategories.clear();
        if (selectedCategories != null) {
            this.selectedCategories.addAll(selectedCategories);
        }
        notifyDataSetChanged();
    }

    public List<FoodCategory> getSelectedCategories() {
        return new ArrayList<>(selectedCategories);
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_category_checkbox, parent, false);
        return new CategoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        FoodCategory category = allCategories.get(position);
        holder.checkBox.setText(category.getDisplayName());
        
        // Set checked state without triggering listener
        holder.checkBox.setOnCheckedChangeListener(null);
        holder.checkBox.setChecked(selectedCategories.contains(category));
        
        // Set listener for user interaction
        holder.checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    selectedCategories.add(category);
                } else {
                    selectedCategories.remove(category);
                }
                
                if (listener != null) {
                    listener.onCategorySelectionChanged(getSelectedCategories());
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return allCategories.size();
    }

    static class CategoryViewHolder extends RecyclerView.ViewHolder {
        CheckBox checkBox;

        CategoryViewHolder(View itemView) {
            super(itemView);
            checkBox = itemView.findViewById(R.id.checkBoxCategory);
        }
    }
} 