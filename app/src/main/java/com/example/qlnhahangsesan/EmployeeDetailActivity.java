package com.example.qlnhahangsesan;

import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.qlnhahangsesan.database.DatabaseHelper;
import com.example.qlnhahangsesan.model.Employee;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class EmployeeDetailActivity extends AppCompatActivity {

    private TextView textViewTitle;
    private EditText editTextName;
    private EditText editTextPosition;
    private EditText editTextPhone;
    private EditText editTextEmail;
    private EditText editTextAddress;
    private EditText editTextSalary;
    private EditText editTextStartDate;
    private Button buttonCancel;
    private Button buttonSave;
    private Button buttonDelete;

    private DatabaseHelper databaseHelper;
    private Employee employee;
    private boolean isEditMode = false;
    private Calendar calendar;
    private SimpleDateFormat dateFormatter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_employee_detail);

        // Initialize database helper
        databaseHelper = DatabaseHelper.getInstance(this);

        // Initialize date formatter
        dateFormatter = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        calendar = Calendar.getInstance();

        // Initialize views
        textViewTitle = findViewById(R.id.textViewTitle);
        editTextName = findViewById(R.id.editTextName);
        editTextPosition = findViewById(R.id.editTextPosition);
        editTextPhone = findViewById(R.id.editTextPhone);
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextAddress = findViewById(R.id.editTextAddress);
        editTextSalary = findViewById(R.id.editTextSalary);
        editTextStartDate = findViewById(R.id.editTextStartDate);
        buttonCancel = findViewById(R.id.buttonCancel);
        buttonSave = findViewById(R.id.buttonSave);
        buttonDelete = findViewById(R.id.buttonDelete);

        // Set up date picker for start date
        editTextStartDate.setOnClickListener(v -> showDatePicker());

        // Set default date to today
        editTextStartDate.setText(dateFormatter.format(calendar.getTime()));

        // Check if we are in edit mode
        if (getIntent().hasExtra("employee")) {
            isEditMode = true;
            employee = (Employee) getIntent().getSerializableExtra("employee");
            if (employee != null) {
                populateEmployeeData();
            }
        }

        // Set up action bar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(isEditMode ? "Sửa thông tin nhân viên" : "Thêm nhân viên mới");
        }

        // Set up title
        textViewTitle.setText(isEditMode ? "Sửa thông tin nhân viên" : "Thêm nhân viên mới");

        // Set up buttons
        buttonCancel.setOnClickListener(v -> finish());
        
        buttonSave.setOnClickListener(v -> saveEmployee());
        
        buttonDelete.setOnClickListener(v -> confirmDelete());

        // Show delete button only in edit mode
        buttonDelete.setVisibility(isEditMode ? View.VISIBLE : View.GONE);
    }

    private void populateEmployeeData() {
        editTextName.setText(employee.getName());
        editTextPosition.setText(employee.getPosition());
        editTextPhone.setText(employee.getPhone());
        editTextEmail.setText(employee.getEmail());
        editTextAddress.setText(employee.getAddress());
        editTextSalary.setText(String.valueOf(employee.getSalary()));
        editTextStartDate.setText(employee.getStartDate());
    }

    private void showDatePicker() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    calendar.set(Calendar.YEAR, year);
                    calendar.set(Calendar.MONTH, month);
                    calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    editTextStartDate.setText(dateFormatter.format(calendar.getTime()));
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }

    private void saveEmployee() {
        // Get input values
        String name = editTextName.getText().toString().trim();
        String position = editTextPosition.getText().toString().trim();
        String phone = editTextPhone.getText().toString().trim();
        String email = editTextEmail.getText().toString().trim();
        String address = editTextAddress.getText().toString().trim();
        String salaryStr = editTextSalary.getText().toString().trim();
        String startDate = editTextStartDate.getText().toString().trim();

        // Validate inputs
        if (TextUtils.isEmpty(name)) {
            editTextName.setError("Vui lòng nhập tên nhân viên");
            editTextName.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(position)) {
            editTextPosition.setError("Vui lòng nhập chức vụ");
            editTextPosition.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(phone)) {
            editTextPhone.setError("Vui lòng nhập số điện thoại");
            editTextPhone.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(salaryStr)) {
            editTextSalary.setError("Vui lòng nhập lương");
            editTextSalary.requestFocus();
            return;
        }

        double salary;
        try {
            salary = Double.parseDouble(salaryStr);
        } catch (NumberFormatException e) {
            editTextSalary.setError("Lương không hợp lệ");
            editTextSalary.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(startDate)) {
            editTextStartDate.setError("Vui lòng chọn ngày vào làm");
            editTextStartDate.requestFocus();
            return;
        }

        if (isEditMode) {
            // Update existing employee
            employee.setName(name);
            employee.setPosition(position);
            employee.setPhone(phone);
            employee.setEmail(email);
            employee.setAddress(address);
            employee.setSalary(salary);
            employee.setStartDate(startDate);

            if (databaseHelper.updateEmployee(employee)) {
                Toast.makeText(this, "Cập nhật thông tin thành công", Toast.LENGTH_SHORT).show();
                setResult(RESULT_OK);
                finish();
            } else {
                Toast.makeText(this, "Không thể cập nhật thông tin", Toast.LENGTH_SHORT).show();
            }
        } else {
            // Add new employee
            Employee newEmployee = new Employee(name, position, phone, email, address, salary, startDate);
            long id = databaseHelper.addEmployee(newEmployee);

            if (id > 0) {
                Toast.makeText(this, "Thêm nhân viên thành công", Toast.LENGTH_SHORT).show();
                setResult(RESULT_OK);
                finish();
            } else {
                Toast.makeText(this, "Không thể thêm nhân viên", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void confirmDelete() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Xác nhận xóa");
        builder.setMessage("Bạn có chắc chắn muốn xóa nhân viên này?");
        builder.setPositiveButton("Xóa", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                deleteEmployee();
            }
        });
        builder.setNegativeButton("Hủy", null);
        builder.show();
    }

    private void deleteEmployee() {
        if (employee != null) {
            if (databaseHelper.deleteEmployee(employee.getId())) {
                Toast.makeText(this, "Xóa nhân viên thành công", Toast.LENGTH_SHORT).show();
                setResult(RESULT_OK);
                finish();
            } else {
                Toast.makeText(this, "Không thể xóa nhân viên", Toast.LENGTH_SHORT).show();
            }
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