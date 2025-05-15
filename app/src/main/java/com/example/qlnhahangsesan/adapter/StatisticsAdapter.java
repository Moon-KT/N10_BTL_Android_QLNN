package com.example.qlnhahangsesan.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.qlnhahangsesan.R;
import com.example.qlnhahangsesan.model.StatisticItem;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class StatisticsAdapter extends RecyclerView.Adapter<StatisticsAdapter.StatisticsViewHolder> {

    private Context context;
    private List<StatisticItem> statisticsList;
    private boolean isMonetary;
    private NumberFormat numberFormat;

    public StatisticsAdapter(Context context, List<StatisticItem> statisticsList, boolean isMonetary) {
        this.context = context;
        this.statisticsList = statisticsList;
        this.isMonetary = isMonetary;
        
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
        
        holder.textViewName.setText(item.getName());
        
        if (isMonetary) {
            holder.textViewValue.setText(numberFormat.format(item.getValue()));
        } else {
            holder.textViewValue.setText(numberFormat.format(item.getIntValue()));
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