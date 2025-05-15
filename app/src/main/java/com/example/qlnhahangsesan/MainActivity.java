package com.example.qlnhahangsesan;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.qlnhahangsesan.utils.DataGenerator;
import com.example.qlnhahangsesan.utils.SessionManager;

public class MainActivity extends AppCompatActivity {
    
    private SessionManager sessionManager;
    private TextView textViewWelcome;
    private CardView cardViewEmployees;
    private CardView cardViewFoods;
    private CardView cardViewTables;
    private CardView cardViewStatistics;
    private CardView cardViewDailyMenu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        
        // Initialize session manager
        sessionManager = new SessionManager(this);
        
        // Check if user is logged in
        if (!sessionManager.isLoggedIn()) {
            // User is not logged in, go to LoginActivity
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
            return;
        }
        
        // Set up welcome message
        textViewWelcome = findViewById(R.id.textViewWelcome);
        String fullname = sessionManager.getFullname();
        if (fullname != null) {
            textViewWelcome.setText("Xin chào, " + fullname + "!");
        }
        
        // Initialize card views
        cardViewEmployees = findViewById(R.id.cardViewEmployees);
        cardViewFoods = findViewById(R.id.cardViewFoods);
        cardViewTables = findViewById(R.id.cardViewTables);
        cardViewStatistics = findViewById(R.id.cardViewStatistics);
        cardViewDailyMenu = findViewById(R.id.cardViewDailyMenu);
        
        // Set click listeners for cards
        cardViewEmployees.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, EmployeeListActivity.class);
            startActivity(intent);
        });
        
        cardViewFoods.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, FoodListActivity.class);
            startActivity(intent);
        });
        
        cardViewTables.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, TableListActivity.class);
            startActivity(intent);
        });
        
        cardViewStatistics.setOnClickListener(v -> {
            try {
                Intent intent = new Intent(MainActivity.this, StatisticsActivity.class);
                startActivity(intent);
            } catch (Exception e) {
                Toast.makeText(MainActivity.this, "Lỗi khi mở thống kê: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        });
        
        cardViewDailyMenu.setOnClickListener(v -> {
            try {
                Intent intent = new Intent(MainActivity.this, DailyMenuActivity.class);
                startActivity(intent);
            } catch (Exception e) {
                Toast.makeText(MainActivity.this, "Lỗi khi mở menu theo ngày: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        });
        
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        // Add a debug menu item for generating mock data
        menu.add(Menu.NONE, 999, Menu.NONE, "Tạo dữ liệu mẫu");
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        
        if (id == R.id.action_logout) {
            // Logout user
            sessionManager.logoutUser();
            
            // Navigate to LoginActivity
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
            return true;
        } else if (id == R.id.action_statistics) {
            // Mở màn hình thống kê
            Intent intent = new Intent(MainActivity.this, StatisticsActivity.class);
            startActivity(intent);
            return true;
        } else if (id == 999) {
            // Generate mock data
            generateMockData();
            return true;
        }
        
        return super.onOptionsItemSelected(item);
    }
    
    /**
     * Generate mock data for testing
     */
    private void generateMockData() {
        new Thread(() -> {
            DataGenerator dataGenerator = new DataGenerator(MainActivity.this);
            dataGenerator.generateMockData();
        }).start();
    }
}