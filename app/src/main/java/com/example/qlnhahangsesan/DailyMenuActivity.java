package com.example.qlnhahangsesan;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.qlnhahangsesan.adapter.DailyMenuAdapter;
import com.example.qlnhahangsesan.database.DatabaseHelper;
import com.example.qlnhahangsesan.model.DailyMenu;
import com.example.qlnhahangsesan.model.Food;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DailyMenuActivity extends AppCompatActivity {

    private static final String TAG = "DailyMenuActivity";
    private static final int REQUEST_ADD_FOODS = 1001;

    private TextView textViewSelectedDate;
    private Button buttonSelectDate;
    private Button buttonAddToMenu;
    private Button buttonDeleteMenu;
    private RecyclerView recyclerViewDailyMenu;
    private LinearLayout layoutNoMenu;

    private DatabaseHelper databaseHelper;
    private DailyMenuAdapter adapter;
    private List<DailyMenu> menuItems = new ArrayList<>();
    private String currentDate;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_daily_menu);

        // Initialize views
        textViewSelectedDate = findViewById(R.id.textViewSelectedDate);
        buttonSelectDate = findViewById(R.id.buttonSelectDate);
        buttonAddToMenu = findViewById(R.id.buttonAddToMenu);
        buttonDeleteMenu = findViewById(R.id.buttonDeleteMenu);
        recyclerViewDailyMenu = findViewById(R.id.recyclerViewDailyMenu);
        layoutNoMenu = findViewById(R.id.layoutNoMenu);

        // Initialize database helper
        databaseHelper = DatabaseHelper.getInstance(this);

        // Initialize RecyclerView
        recyclerViewDailyMenu.setLayoutManager(new LinearLayoutManager(this));
        adapter = new DailyMenuAdapter(this, menuItems, item -> {
            // Handle item click for edit
            showEditMenuItemDialog(item);
        });
        recyclerViewDailyMenu.setAdapter(adapter);

        // Set up back button in action bar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Menu theo ngày");
        }
        // Set current date
        currentDate = dateFormat.format(new Date());
        textViewSelectedDate.setText(currentDate);

        // Load menu for current date
        loadMenuForDate(currentDate);

        // Set up date picker
        buttonSelectDate.setOnClickListener(v -> showDatePicker());

        // Set up add button
        buttonAddToMenu.setOnClickListener(v -> {
            // Show food selection activity
            Intent intent = new Intent(DailyMenuActivity.this, FoodListActivity.class);
            intent.putExtra("forDailyMenu", true);
            intent.putExtra("selectedDate", currentDate);
            startActivityForResult(intent, REQUEST_ADD_FOODS);
        });

        // Set up delete button
        buttonDeleteMenu.setOnClickListener(v -> {
            if (menuItems.isEmpty()) {
                Toast.makeText(this, "Không có menu nào để xóa", Toast.LENGTH_SHORT).show();
                return;
            }

            new AlertDialog.Builder(this)
                    .setTitle("Xóa menu")
                    .setMessage("Bạn có chắc chắn muốn xóa tất cả các món ăn trong menu này không?")
                    .setPositiveButton("Xóa", (dialog, which) -> {
                        deleteCurrentMenu();
                    })
                    .setNegativeButton("Hủy", null)
                    .show();
        });
    }

    private void showDatePicker() {
        final Calendar calendar = Calendar.getInstance();
        try {
            calendar.setTime(dateFormat.parse(currentDate));
        } catch (Exception e) {
            Log.e(TAG, "Error parsing date", e);
        }

        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    calendar.set(selectedYear, selectedMonth, selectedDay);
                    currentDate = dateFormat.format(calendar.getTime());
                    textViewSelectedDate.setText(currentDate);
                    loadMenuForDate(currentDate);
                },
                year, month, day
        );
        datePickerDialog.show();
    }

    private void loadMenuForDate(String date) {
        menuItems.clear();
        menuItems.addAll(databaseHelper.getDailyMenuByDate(date));
        adapter.notifyDataSetChanged();

        // Show empty state if no menu items
        if (menuItems.isEmpty()) {
            layoutNoMenu.setVisibility(View.VISIBLE);
        } else {
            layoutNoMenu.setVisibility(View.GONE);
        }
    }

    private void deleteCurrentMenu() {
        boolean success = databaseHelper.deleteAllDailyMenuItemsByDate(currentDate);
        if (success) {
            Toast.makeText(this, "Đã xóa menu ngày " + currentDate, Toast.LENGTH_SHORT).show();
            menuItems.clear();
            adapter.notifyDataSetChanged();
            layoutNoMenu.setVisibility(View.VISIBLE);
        } else {
            Toast.makeText(this, "Có lỗi khi xóa menu", Toast.LENGTH_SHORT).show();
        }
    }

    private void showEditMenuItemDialog(DailyMenu menuItem) {
        // Show dialog to edit quantity and featured status
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_edit_menu_item, null);
        androidx.appcompat.widget.SwitchCompat switchFeatured = dialogView.findViewById(R.id.switchFeatured);
        com.google.android.material.slider.Slider sliderQuantity = dialogView.findViewById(R.id.sliderQuantity);
        TextView textViewQuantity = dialogView.findViewById(R.id.textViewQuantity);

        // Set current values
        switchFeatured.setChecked(menuItem.isFeatured());
        sliderQuantity.setValue(menuItem.getQuantity());
        textViewQuantity.setText(String.format(Locale.getDefault(), "Số lượng: %d", menuItem.getQuantity()));

        // Update quantity text when slider changes
        sliderQuantity.addOnChangeListener((slider, value, fromUser) -> {
            textViewQuantity.setText(String.format(Locale.getDefault(), "Số lượng: %d", (int) value));
        });

        new AlertDialog.Builder(this)
                .setTitle("Chỉnh sửa " + menuItem.getFoodName())
                .setView(dialogView)
                .setPositiveButton("Lưu", (dialog, which) -> {
                    // Update menu item
                    menuItem.setFeatured(switchFeatured.isChecked());
                    menuItem.setQuantity((int) sliderQuantity.getValue());
                    boolean success = databaseHelper.updateDailyMenuItem(menuItem);
                    
                    if (success) {
                        Toast.makeText(this, "Đã cập nhật món ăn", Toast.LENGTH_SHORT).show();
                        loadMenuForDate(currentDate);
                    } else {
                        Toast.makeText(this, "Có lỗi khi cập nhật món ăn", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Hủy", null)
                .setNeutralButton("Xóa khỏi menu", (dialog, which) -> {
                    // Delete menu item
                    boolean success = databaseHelper.deleteDailyMenuItem(menuItem.getId());
                    if (success) {
                        Toast.makeText(this, "Đã xóa món ăn khỏi menu", Toast.LENGTH_SHORT).show();
                        loadMenuForDate(currentDate);
                    } else {
                        Toast.makeText(this, "Có lỗi khi xóa món ăn", Toast.LENGTH_SHORT).show();
                    }
                })
                .show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_ADD_FOODS && resultCode == RESULT_OK) {
            // Reload menu after adding items
            loadMenuForDate(currentDate);
        }
    }
} 