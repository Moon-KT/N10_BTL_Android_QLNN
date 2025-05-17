package com.example.qlnhahangsesan.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.qlnhahangsesan.DayOrdersActivity;
import com.example.qlnhahangsesan.FoodListActivity;
import com.example.qlnhahangsesan.R;
import com.example.qlnhahangsesan.model.StatisticItem;

import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class StatisticsAdapter extends RecyclerView.Adapter<StatisticsAdapter.StatisticsViewHolder> {

    private Context context;
    private List<StatisticItem> statisticsList;
    private boolean isMonetary;
    private NumberFormat numberFormat;
    private int statisticType;
    
    private SimpleDateFormat dbDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    private SimpleDateFormat displayDateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

    // Statistic types from StatisticsFragment
    public static final int STATS_TYPE_FOOD_CATEGORY = 1;
    public static final int STATS_TYPE_REVENUE = 2;

    public StatisticsAdapter(Context context, List<StatisticItem> statisticsList, boolean isMonetary, int statisticType) {
        this.context = context;
        this.statisticsList = statisticsList;
        this.isMonetary = isMonetary;
        this.statisticType = statisticType;
        
        if (isMonetary) {
            this.numberFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        } else {
            this.numberFormat = NumberFormat.getNumberInstance(new Locale("vi", "VN"));
        }
    }

    @NonNull
    @Override
    public StatisticsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_statistic, parent, false);
        return new StatisticsViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull StatisticsViewHolder holder, int position) {
        StatisticItem item = statisticsList.get(position);
        
        // Format the name if it's a date
        String displayName = item.getName();
        try {
            if (displayName.matches("\\d{4}-\\d{2}-\\d{2}")) {
                Date date = dbDateFormat.parse(displayName);
                displayName = displayDateFormat.format(date);
            }
        } catch (ParseException e) {
            // Use original name if parsing fails
        }
        
        holder.textViewName.setText(displayName);
        
        if (isMonetary) {
            holder.textViewValue.setText(numberFormat.format(item.getValue()));
        } else {
            holder.textViewValue.setText(numberFormat.format(item.getIntValue()));
        }
        
        // Set click listeners based on statistic type
        if (statisticType == STATS_TYPE_REVENUE) {
            holder.itemView.setOnClickListener(v -> {
                // Open the DayOrdersActivity with the date
                Intent intent = new Intent(context, DayOrdersActivity.class);
                intent.putExtra("date", item.getName()); // Pass the original date format (yyyy-MM-dd)
                context.startActivity(intent);
            });
        } else if (statisticType == STATS_TYPE_FOOD_CATEGORY) {
            holder.itemView.setOnClickListener(v -> {
                // Open the FoodListActivity with the category
                Intent intent = new Intent(context, FoodListActivity.class);
                intent.putExtra("category", item.getName());
                context.startActivity(intent);
            });
        }
    }

    @Override
    public int getItemCount() {
        return statisticsList != null ? statisticsList.size() : 0;
    }

    public void updateData(List<StatisticItem> newStatisticsList) {
        this.statisticsList = newStatisticsList;
        notifyDataSetChanged();
    }

    public static class StatisticsViewHolder extends RecyclerView.ViewHolder {
        public TextView textViewName;
        public TextView textViewValue;

        public StatisticsViewHolder(View view) {
            super(view);
            textViewName = view.findViewById(R.id.textViewStatName);
            textViewValue = view.findViewById(R.id.textViewStatValue);
        }
    }
} 