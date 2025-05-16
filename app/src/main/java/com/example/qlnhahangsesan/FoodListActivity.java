package com.example.qlnhahangsesan;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.qlnhahangsesan.adapter.FoodAdapter;
import com.example.qlnhahangsesan.database.DatabaseHelper;
import com.example.qlnhahangsesan.model.DailyMenu;
import com.example.qlnhahangsesan.model.Food;
import com.example.qlnhahangsesan.model.FoodCategory;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;

public class FoodListActivity extends AppCompatActivity implements FoodAdapter.OnFoodClickListener {

    private static final int REQUEST_ADD_FOOD = 1;
    private static final int REQUEST_EDIT_FOOD = 2;

    private RecyclerView recyclerViewFoods;
    private TextView textViewEmpty;
    private ProgressBar progressBar;
    private FloatingActionButton fabAddFood;
    private TabLayout tabLayoutCategories;
    private SearchView searchView;

    private DatabaseHelper databaseHelper;
    private FoodAdapter foodAdapter;
    private List<Food> foodList;
    private List<String> categoryList;
    private String currentCategory = "Tất cả";
    private String currentQuery = "";
    
    // Sort variables
    private String currentSortBy = "name"; // Default sort by name
    private boolean isAscending = true; // Default ascending order
    private String currentSortLabel = "";
    
    // Daily menu mode
    private boolean forDailyMenu = false;
    private String selectedDate = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food_list);

        // Check if opened for daily menu
        Intent intent = getIntent();
        if (intent != null) {
            forDailyMenu = intent.getBooleanExtra("forDailyMenu", false);
            selectedDate = intent.getStringExtra("selectedDate");
        }

        // Initialize database helper
        databaseHelper = DatabaseHelper.getInstance(this);

        // Initialize views
        recyclerViewFoods = findViewById(R.id.recyclerViewFoods);
        textViewEmpty = findViewById(R.id.textViewEmpty);
        progressBar = findViewById(R.id.progressBar);
        fabAddFood = findViewById(R.id.fabAddFood);
        tabLayoutCategories = findViewById(R.id.tabLayoutCategories);

        // Set up RecyclerView
        recyclerViewFoods.setLayoutManager(new LinearLayoutManager(this));
        foodList = new ArrayList<>();
        foodAdapter = new FoodAdapter(this, foodList, this);
        recyclerViewFoods.setAdapter(foodAdapter);

        // Set up FAB visibility based on mode
        if (forDailyMenu) {
            fabAddFood.setVisibility(View.GONE);
        } else {
            // Set up FAB
            fabAddFood.setOnClickListener(v -> {
                Intent foodIntent = new Intent(FoodListActivity.this, FoodDetailActivity.class);
                startActivityForResult(foodIntent, REQUEST_ADD_FOOD);
            });
        }

        // Set up back button in action bar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            if (forDailyMenu) {
                getSupportActionBar().setTitle("Chọn món cho menu");
            } else {
                getSupportActionBar().setTitle("Quản lý món ăn");
            }
        }
        
        // Set default sort label
        currentSortLabel = getString(R.string.sort_name_asc);

        // Set up category tabs
        setupCategoryTabs();

        // Load food data
        loadFoodsSorted();
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.food_list_menu, menu);
        
        MenuItem searchItem = menu.findItem(R.id.action_search);
        searchView = (SearchView) searchItem.getActionView();
        searchView.setQueryHint(getString(R.string.search_hint));
        
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                currentQuery = query;
                performSearch(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText.isEmpty() && !currentQuery.isEmpty()) {
                    // Reset search when query is cleared
                    currentQuery = "";
                    loadFoodsSorted();
                } else if (newText.length() >= 2) {
                    // Search when at least 2 characters are typed
                    currentQuery = newText;
                    performSearch(newText);
                }
                return true;
            }
        });
        
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        
        if (id == android.R.id.home) {
            finish();
            return true;
        } else if (id == R.id.sort_name_asc) {
            currentSortBy = "name";
            isAscending = true;
            currentSortLabel = getString(R.string.sort_name_asc);
            loadFoodsSorted();
            showCurrentSortToast();
            return true;
        } else if (id == R.id.sort_name_desc) {
            currentSortBy = "name";
            isAscending = false;
            currentSortLabel = getString(R.string.sort_name_desc);
            loadFoodsSorted();
            showCurrentSortToast();
            return true;
        } else if (id == R.id.sort_price_asc) {
            currentSortBy = "price";
            isAscending = true;
            currentSortLabel = getString(R.string.sort_price_asc);
            loadFoodsSorted();
            showCurrentSortToast();
            return true;
        } else if (id == R.id.sort_price_desc) {
            currentSortBy = "price";
            isAscending = false;
            currentSortLabel = getString(R.string.sort_price_desc);
            loadFoodsSorted();
            showCurrentSortToast();
            return true;
        } else if (id == R.id.sort_category) {
            currentSortBy = "category";
            isAscending = true;
            currentSortLabel = getString(R.string.sort_category);
            loadFoodsSorted();
            showCurrentSortToast();
            return true;
        }
        
        return super.onOptionsItemSelected(item);
    }
    
    private void showCurrentSortToast() {
        Toast.makeText(this, 
                String.format(getString(R.string.current_sort), currentSortLabel), 
                Toast.LENGTH_SHORT).show();
    }

    private void performSearch(String query) {
        progressBar.setVisibility(View.VISIBLE);
        
        // Search foods from database
        List<Food> searchResults = databaseHelper.searchFoods(query);
        
        // Filter by current category if not "All"
        if (!currentCategory.equals("Tất cả")) {
            List<Food> filteredResults = new ArrayList<>();
            for (Food food : searchResults) {
                if (food.getCategoryString().equals(currentCategory)) {
                    filteredResults.add(food);
                }
            }
            searchResults = filteredResults;
        }
        
        // Update UI
        foodList.clear();
        if (searchResults != null && !searchResults.isEmpty()) {
            foodList.addAll(searchResults);
            textViewEmpty.setVisibility(View.GONE);
        } else {
            textViewEmpty.setText(getString(R.string.no_search_results));
            textViewEmpty.setVisibility(View.VISIBLE);
        }
        
        foodAdapter.notifyDataSetChanged();
        progressBar.setVisibility(View.GONE);
    }

    private void setupCategoryTabs() {
        // Add "All" tab
        tabLayoutCategories.addTab(tabLayoutCategories.newTab().setText("Tất cả"));
        
        // Add a tab for each FoodCategory value
        for (FoodCategory category : FoodCategory.values()) {
            tabLayoutCategories.addTab(tabLayoutCategories.newTab().setText(category.getDisplayName()));
        }
        
        // Set tab selected listener
        tabLayoutCategories.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                String selectedCategory = tab.getText().toString();
                currentCategory = selectedCategory;
                
                // Load foods based on selected category
                loadFoodsByCategory();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                // Do nothing
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                // Do nothing
            }
        });
    }
    
    private void loadFoodsByCategory() {
        progressBar.setVisibility(View.VISIBLE);
        
        List<Food> foods;
        
        if (currentCategory.equals("Tất cả")) {
            foods = databaseHelper.getFoodsSorted(currentSortBy, isAscending);
        } else {
            // Find the matching enum
            FoodCategory selectedCategory = null;
            for (FoodCategory category : FoodCategory.values()) {
                if (category.getDisplayName().equals(currentCategory)) {
                    selectedCategory = category;
                    break;
                }
            }
            
            if (selectedCategory != null) {
                foods = databaseHelper.getFoodsByCategorySorted(selectedCategory.getDisplayName(), currentSortBy, isAscending);
            } else {
                foods = new ArrayList<>();
            }
        }
        
        updateFoodList(foods);
    }

    private void updateFoodList(List<Food> foods) {
        foodList.clear();
        if (foods != null && !foods.isEmpty()) {
            foodList.addAll(foods);
            textViewEmpty.setVisibility(View.GONE);
        } else {
            textViewEmpty.setText(getString(R.string.no_foods));
            textViewEmpty.setVisibility(View.VISIBLE);
        }
        
        foodAdapter.notifyDataSetChanged();
        progressBar.setVisibility(View.GONE);
    }

    @Override
    public void onFoodClick(Food food, int position) {
        if (forDailyMenu) {
            // Kiểm tra trạng thái available của món ăn
            if (!food.isAvailable()) {
                // Hiển thị thông báo nếu món ăn đã hết
                Toast.makeText(this, "Không thể thêm món " + food.getName() + " vào menu vì đã hết món!", Toast.LENGTH_SHORT).show();
                return;
            }
            
            // Kiểm tra xem món đã tồn tại trong menu của ngày này chưa
            if (databaseHelper.isFoodInDailyMenu(food.getId(), selectedDate)) {
                Toast.makeText(this, "Món " + food.getName() + " đã tồn tại trong menu ngày này!", Toast.LENGTH_SHORT).show();
                return;
            }
            
            // Show dialog to add to daily menu
            showAddToDailyMenuDialog(food);
        } else {
            // Open food detail for editing
            Intent intent = new Intent(FoodListActivity.this, FoodDetailActivity.class);
            intent.putExtra("food", food);
            startActivityForResult(intent, REQUEST_EDIT_FOOD);
        }
    }

    private void showAddToDailyMenuDialog(Food food) {
        // Create a dialog to set featured status only
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_edit_menu_item, null);
        androidx.appcompat.widget.SwitchCompat switchFeatured = dialogView.findViewById(R.id.switchFeatured);

        // Set default values
        switchFeatured.setChecked(false);

        new AlertDialog.Builder(this)
                .setTitle("Thêm " + food.getName() + " vào menu")
                .setView(dialogView)
                .setPositiveButton("Thêm", (dialog, which) -> {
                    // Create DailyMenu object
                    DailyMenu dailyMenu = new DailyMenu();
                    dailyMenu.setDate(selectedDate);
                    dailyMenu.setFoodId(food.getId());
                    dailyMenu.setFeatured(switchFeatured.isChecked());
                    // Default quantity to 0 or remove if DB schema allows
                    dailyMenu.setQuantity(0);

                    // Add to database
                    long id = databaseHelper.addDailyMenuItem(dailyMenu);
                    if (id > 0) {
                        Toast.makeText(this, "Đã thêm món vào menu", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Có lỗi khi thêm món vào menu", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_ADD_FOOD || requestCode == REQUEST_EDIT_FOOD) {
                // Reload food list
                loadFoodsSorted();
                
                // Also reload category tabs in case new categories were added
                tabLayoutCategories.removeAllTabs();
                setupCategoryTabs();
            }
        }
    }
    
    @Override
    public void finish() {
        if (forDailyMenu) {
            // Return OK result to DailyMenuActivity
            setResult(RESULT_OK);
        }
        super.finish();
    }

    private void loadFoodsSorted() {
        if (currentQuery.isEmpty()) {
            loadFoodsByCategory();
        } else {
            performSearch(currentQuery);
        }
    }
} 