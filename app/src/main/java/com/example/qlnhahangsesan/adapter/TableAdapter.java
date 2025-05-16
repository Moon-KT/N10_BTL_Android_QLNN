package com.example.qlnhahangsesan.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.qlnhahangsesan.R;
import com.example.qlnhahangsesan.database.DatabaseHelper;
import com.example.qlnhahangsesan.model.Order;
import com.example.qlnhahangsesan.model.Table;

import java.util.List;

public class TableAdapter extends RecyclerView.Adapter<TableAdapter.TableViewHolder> {

    private Context context;
    private List<Table> tableList;
    private OnTableClickListener listener;
    private DatabaseHelper databaseHelper;

    public interface OnTableClickListener {
        void onTableClick(Table table, int position);
    }

    public TableAdapter(Context context, List<Table> tableList, OnTableClickListener listener) {
        this.context = context;
        this.tableList = tableList;
        this.listener = listener;
        this.databaseHelper = DatabaseHelper.getInstance(context);
    }

    @NonNull
    @Override
    public TableViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_table, parent, false);
        return new TableViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull TableViewHolder holder, int position) {
        Table table = tableList.get(position);
        
        holder.textViewName.setText(table.getName());
        holder.textViewCapacity.setText(context.getString(R.string.table_capacity, table.getCapacity()));
        holder.textViewStatus.setText(table.getStatus());
        
        // Set card color based on status
        int cardColor;
        int textColor;
        
        switch (table.getStatus()) {
            case "Trống":
                cardColor = R.color.table_available;
                textColor = android.R.color.black;
                break;
            case "Đã đặt":
                cardColor = R.color.table_reserved;
                textColor = android.R.color.black;
                break;
            case "Đang phục vụ":
                cardColor = R.color.table_occupied;
                textColor = android.R.color.white;
                break;
            default:
                cardColor = android.R.color.white;
                textColor = android.R.color.black;
                break;
        }
        
        holder.cardView.setCardBackgroundColor(ContextCompat.getColor(context, cardColor));
        holder.textViewStatus.setTextColor(ContextCompat.getColor(context, textColor));
        
        // Check if table has unpaid order
        if (table.getStatus().equals("Đang phục vụ")) {
            Order currentOrder = databaseHelper.getCurrentOrderForTable(table.getId());
            if (currentOrder != null && currentOrder.getStatus().equals("Chưa thanh toán")) {
                holder.textViewUnpaidStatus.setVisibility(View.VISIBLE);
            } else {
                holder.textViewUnpaidStatus.setVisibility(View.GONE);
            }
        } else {
            holder.textViewUnpaidStatus.setVisibility(View.GONE);
        }
        
        // Set note if available
        if (table.getNote() != null && !table.getNote().isEmpty()) {
            holder.textViewNote.setVisibility(View.VISIBLE);
            holder.textViewNote.setText(table.getNote());
        } else {
            holder.textViewNote.setVisibility(View.GONE);
        }
        
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onTableClick(table, position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return tableList != null ? tableList.size() : 0;
    }

    public void updateData(List<Table> newTableList) {
        this.tableList = newTableList;
        notifyDataSetChanged();
    }

    public static class TableViewHolder extends RecyclerView.ViewHolder {
        public CardView cardView;
        public TextView textViewName;
        public TextView textViewCapacity;
        public TextView textViewStatus;
        public TextView textViewNote;
        public TextView textViewUnpaidStatus;

        public TableViewHolder(View view) {
            super(view);
            cardView = view.findViewById(R.id.cardViewTable);
            textViewName = view.findViewById(R.id.textViewTableName);
            textViewCapacity = view.findViewById(R.id.textViewTableCapacity);
            textViewStatus = view.findViewById(R.id.textViewTableStatus);
            textViewNote = view.findViewById(R.id.textViewTableNote);
            textViewUnpaidStatus = view.findViewById(R.id.textViewUnpaidStatus);
        }
    }
} 