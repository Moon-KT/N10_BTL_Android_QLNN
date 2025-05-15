package com.example.qlnhahangsesan;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.qlnhahangsesan.adapter.EmployeeAdapter;
import com.example.qlnhahangsesan.database.DatabaseHelper;
import com.example.qlnhahangsesan.model.Employee;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class EmployeeListActivity extends AppCompatActivity implements EmployeeAdapter.OnEmployeeClickListener {

    private static final int REQUEST_ADD_EMPLOYEE = 1;
    private static final int REQUEST_EDIT_EMPLOYEE = 2;

    private RecyclerView recyclerViewEmployees;
    private TextView textViewEmpty;
    private ProgressBar progressBar;
    private FloatingActionButton fabAddEmployee;

    private DatabaseHelper databaseHelper;
    private EmployeeAdapter employeeAdapter;
    private List<Employee> employeeList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_employee_list);

        // Initialize database helper
        databaseHelper = DatabaseHelper.getInstance(this);

        // Initialize views
        recyclerViewEmployees = findViewById(R.id.recyclerViewEmployees);
        textViewEmpty = findViewById(R.id.textViewEmpty);
        progressBar = findViewById(R.id.progressBar);
        fabAddEmployee = findViewById(R.id.fabAddEmployee);

        // Set up RecyclerView
        recyclerViewEmployees.setLayoutManager(new LinearLayoutManager(this));
        employeeList = new ArrayList<>();
        employeeAdapter = new EmployeeAdapter(this, employeeList, this);
        recyclerViewEmployees.setAdapter(employeeAdapter);

        // Set up FAB
        fabAddEmployee.setOnClickListener(v -> {
            Intent intent = new Intent(EmployeeListActivity.this, EmployeeDetailActivity.class);
            startActivityForResult(intent, REQUEST_ADD_EMPLOYEE);
        });

        // Set up back button in action bar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Quản lý nhân viên");
        }

        // Load employee data
        loadEmployees();
    }

    private void loadEmployees() {
        progressBar.setVisibility(View.VISIBLE);
        
        // Get employees from database
        List<Employee> employees = databaseHelper.getAllEmployees();
        
        employeeList.clear();
        if (employees != null && !employees.isEmpty()) {
            employeeList.addAll(employees);
            textViewEmpty.setVisibility(View.GONE);
        } else {
            textViewEmpty.setVisibility(View.VISIBLE);
        }
        
        employeeAdapter.notifyDataSetChanged();
        progressBar.setVisibility(View.GONE);
    }

    @Override
    public void onEmployeeClick(Employee employee, int position) {
        // Open employee detail for editing
        Intent intent = new Intent(EmployeeListActivity.this, EmployeeDetailActivity.class);
        intent.putExtra("employee", employee);
        startActivityForResult(intent, REQUEST_EDIT_EMPLOYEE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (resultCode == RESULT_OK) {
            // Reload employee list
            loadEmployees();
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
} 