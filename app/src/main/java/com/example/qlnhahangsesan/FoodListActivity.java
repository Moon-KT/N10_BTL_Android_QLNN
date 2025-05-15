package com.example.qlnhahangsesan;

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
import com.example.qlnhahangsesan.model.Food;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food_list);

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

        // Set up FAB
        fabAddFood.setOnClickListener(v -> {
            Intent intent = new Intent(FoodListActivity.this, FoodDetailActivity.class);
            startActivityForResult(intent, REQUEST_ADD_FOOD);
        });

        // Set up back button in action bar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Quản lý món ăn");
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
                if (food.getCategory().equals(currentCategory)) {
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
        
        // Get all categories from database
        categoryList = databaseHelper.getAllFoodCategories();
        
        // Add a tab for each category
        for (String category : categoryList) {
            tabLayoutCategories.addTab(tabLayoutCategories.newTab().setText(category));
        }
        
        // Set tab selected listener
        tabLayoutCategories.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getPosition() == 0) {
                    currentCategory = "Tất cả";
                    if (currentQuery.isEmpty()) {
                        loadFoodsSorted();
                    } else {
                        performSearch(currentQuery);
                    }
                } else {
                    currentCategory = tab.getText().toString();
                    if (currentQuery.isEmpty()) {
                        loadFoodsSorted();
                    } else {
                        performSearch(currentQuery);
                    }
                }
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

    private void loadFoodsSorted() {
        progressBar.setVisibility(View.VISIBLE);
        
        List<Food> foods;
        
        if (currentCategory.equals("Tất cả")) {
            // Get all foods sorted
            foods = databaseHelper.getFoodsSorted(currentSortBy, isAscending);
        } else {
            // Get foods by category sorted
            foods = databaseHelper.getFoodsByCategorySorted(currentCategory, currentSortBy, isAscending);
        }
        
        foodList.clear();
        if (foods != null && !foods.isEmpty()) {
            foodList.addAll(foods);
            textViewEmpty.setVisibility(View.GONE);
        } else {
            if (currentCategory.equals("Tất cả")) {
                textViewEmpty.setText(getString(R.string.no_foods));
            } else {
                textViewEmpty.setText(getString(R.string.no_foods_in_category));
            }
            textViewEmpty.setVisibility(View.VISIBLE);
        }
        
        foodAdapter.notifyDataSetChanged();
        progressBar.setVisibility(View.GONE);
    }

    @Override
    public void onFoodClick(Food food, int position) {
        // Open food detail for editing
        Intent intent = new Intent(FoodListActivity.this, FoodDetailActivity.class);
        intent.putExtra("food", food);
        startActivityForResult(intent, REQUEST_EDIT_FOOD);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (resultCode == RESULT_OK) {
            // Refresh category tabs
            tabLayoutCategories.removeAllTabs();
            setupCategoryTabs();
            
            // Reload food list
            currentQuery = "";
            if (searchView != null) {
                searchView.setQuery("", false);
                searchView.clearFocus();
            }
            
            loadFoodsSorted();
        }
    }
} 