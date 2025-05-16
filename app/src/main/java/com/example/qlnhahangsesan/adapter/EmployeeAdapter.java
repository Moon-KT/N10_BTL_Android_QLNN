package com.example.qlnhahangsesan.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.qlnhahangsesan.R;
import com.example.qlnhahangsesan.model.Employee;
import com.example.qlnhahangsesan.model.Position;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class EmployeeAdapter extends RecyclerView.Adapter<EmployeeAdapter.EmployeeViewHolder> {

    private Context context;
    private List<Employee> employeeList;
    private OnEmployeeClickListener listener;
    private NumberFormat currencyFormat;

    public interface OnEmployeeClickListener {
        void onEmployeeClick(Employee employee, int position);
    }

    public EmployeeAdapter(Context context, List<Employee> employeeList, OnEmployeeClickListener listener) {
        this.context = context;
        this.employeeList = employeeList;
        this.listener = listener;
        this.currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
    }

    @NonNull
    @Override
    public EmployeeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_employee, parent, false);
        return new EmployeeViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull EmployeeViewHolder holder, int position) {
        Employee employee = employeeList.get(position);
        
        holder.textViewName.setText(employee.getName());
        
        // Display the position enum display name
        Position employeePosition = employee.getPosition();
        holder.textViewPosition.setText(employeePosition != null ? 
                employeePosition.getDisplayName() : "");
        
        holder.textViewPhone.setText(employee.getPhone());
        holder.textViewSalary.setText(currencyFormat.format(employee.getSalary()));
        
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onEmployeeClick(employee, position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return employeeList != null ? employeeList.size() : 0;
    }

    public void updateData(List<Employee> newEmployeeList) {
        this.employeeList = newEmployeeList;
        notifyDataSetChanged();
    }

    public static class EmployeeViewHolder extends RecyclerView.ViewHolder {
        public TextView textViewName;
        public TextView textViewPosition;
        public TextView textViewPhone;
        public TextView textViewSalary;

        public EmployeeViewHolder(View view) {
            super(view);
            textViewName = view.findViewById(R.id.textViewEmployeeName);
            textViewPosition = view.findViewById(R.id.textViewEmployeePosition);
            textViewPhone = view.findViewById(R.id.textViewEmployeePhone);
            textViewSalary = view.findViewById(R.id.textViewEmployeeSalary);
        }
    }
} 