package com.example.qlnhahangsesan;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.example.qlnhahangsesan.adapter.CategoryChecklistAdapter;
import com.example.qlnhahangsesan.database.DatabaseHelper;
import com.example.qlnhahangsesan.model.Food;
import com.example.qlnhahangsesan.model.FoodCategory;

import java.util.ArrayList;
import java.util.List;

public class FoodDetailActivity extends AppCompatActivity implements CategoryChecklistAdapter.OnCategorySelectionChangedListener {

    private static final int REQUEST_IMAGE_PICK = 1;

    private TextView textViewTitle;
    private ImageView imageViewFoodPhoto;
    private Button buttonChooseImage;
    private EditText editTextName;
    private RecyclerView recyclerViewCategories;
    private EditText editTextPrice;
    private EditText editTextDescription;
    private CheckBox checkBoxAvailable;
    private Button buttonCancel;
    private Button buttonSave;
    private Button buttonDelete;

    private DatabaseHelper databaseHelper;
    private Food food;
    private boolean isEditMode = false;
    private String imageUrl = "";
    private CategoryChecklistAdapter categoryAdapter;
    private List<FoodCategory> selectedCategories = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food_detail);

        // Initialize database helper
        databaseHelper = DatabaseHelper.getInstance(this);

        // Initialize views
        textViewTitle = findViewById(R.id.textViewTitle);
        imageViewFoodPhoto = findViewById(R.id.imageViewFoodPhoto);
        buttonChooseImage = findViewById(R.id.buttonChooseImage);
        editTextName = findViewById(R.id.editTextName);
        recyclerViewCategories = findViewById(R.id.recyclerViewCategories);
        editTextPrice = findViewById(R.id.editTextPrice);
        editTextDescription = findViewById(R.id.editTextDescription);
        checkBoxAvailable = findViewById(R.id.checkBoxAvailable);
        buttonCancel = findViewById(R.id.buttonCancel);
        buttonSave = findViewById(R.id.buttonSave);
        buttonDelete = findViewById(R.id.buttonDelete);

        // Set up category recycler view
        setupCategoryRecyclerView();

        // Set up image picker
        buttonChooseImage.setOnClickListener(v -> openImagePicker());

        // Check if we are in edit mode
        if (getIntent().hasExtra("food")) {
            isEditMode = true;
            food = (Food) getIntent().getSerializableExtra("food");
            if (food != null) {
                populateFoodData();
            }
        }

        // Set up action bar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(isEditMode ? "Sửa thông tin món ăn" : "Thêm món ăn mới");
        }

        // Set up title
        textViewTitle.setText(isEditMode ? "Sửa thông tin món ăn" : "Thêm món ăn mới");

        // Set up buttons
        buttonCancel.setOnClickListener(v -> finish());
        
        buttonSave.setOnClickListener(v -> saveFood());
        
        buttonDelete.setOnClickListener(v -> confirmDelete());

        // Show delete button only in edit mode
        buttonDelete.setVisibility(isEditMode ? View.VISIBLE : View.GONE);
    }

    private void setupCategoryRecyclerView() {
        categoryAdapter = new CategoryChecklistAdapter(this, this);
        recyclerViewCategories.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewCategories.setAdapter(categoryAdapter);
    }

    @Override
    public void onCategorySelectionChanged(List<FoodCategory> selectedCategories) {
        this.selectedCategories = selectedCategories;
    }

    private void populateFoodData() {
        editTextName.setText(food.getName());
        
        // Set selected categories
        if (food.getCategories() != null && !food.getCategories().isEmpty()) {
            selectedCategories = new ArrayList<>(food.getCategories());
            categoryAdapter.setSelectedCategories(selectedCategories);
        }
        
        editTextPrice.setText(String.valueOf(food.getPrice()));
        editTextDescription.setText(food.getDescription());
        checkBoxAvailable.setChecked(food.isAvailable());
        
        // Set image if available
        imageUrl = food.getImageUrl();
        if (imageUrl != null && !imageUrl.isEmpty()) {
            try {
                // Use Glide to load image
                Uri uri = Uri.parse(imageUrl);
                RequestOptions requestOptions = new RequestOptions()
                    .placeholder(R.mipmap.ic_launcher)
                    .error(R.mipmap.ic_launcher)
                    .diskCacheStrategy(DiskCacheStrategy.ALL);
                
                Glide.with(this)
                    .load(uri)
                    .apply(requestOptions)
                    .centerCrop()
                    .into(imageViewFoodPhoto);
            } catch (Exception e) {
                // If URI parsing fails, load placeholder
                imageViewFoodPhoto.setImageResource(R.mipmap.ic_launcher);
            }
        } else {
            // If no image URL, load placeholder
            imageViewFoodPhoto.setImageResource(R.mipmap.ic_launcher);
        }
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, REQUEST_IMAGE_PICK);
    }

    private void saveFood() {
        // Get input values
        String name = editTextName.getText().toString().trim();
        String priceStr = editTextPrice.getText().toString().trim();
        String description = editTextDescription.getText().toString().trim();
        boolean available = checkBoxAvailable.isChecked();

        // Validate inputs
        if (TextUtils.isEmpty(name)) {
            editTextName.setError("Vui lòng nhập tên món ăn");
            editTextName.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(priceStr)) {
            editTextPrice.setError("Vui lòng nhập giá");
            editTextPrice.requestFocus();
            return;
        }

        double price;
        try {
            price = Double.parseDouble(priceStr);
        } catch (NumberFormatException e) {
            editTextPrice.setError("Giá không hợp lệ");
            editTextPrice.requestFocus();
            return;
        }

        // Validate categories
        if (selectedCategories.isEmpty()) {
            Toast.makeText(this, "Vui lòng chọn ít nhất một loại món", Toast.LENGTH_SHORT).show();
            return;
        }

        if (isEditMode) {
            // Update existing food
            food.setName(name);
            food.setCategories(selectedCategories);
            food.setPrice(price);
            food.setDescription(description);
            food.setImageUrl(imageUrl);
            food.setAvailable(available);

            if (databaseHelper.updateFood(food)) {
                Toast.makeText(this, "Cập nhật thông tin thành công", Toast.LENGTH_SHORT).show();
                setResult(RESULT_OK);
                finish();
            } else {
                Toast.makeText(this, "Không thể cập nhật thông tin", Toast.LENGTH_SHORT).show();
            }
        } else {
            // Add new food
            Food newFood = new Food(name, selectedCategories, price, description, imageUrl, available);
            long id = databaseHelper.addFood(newFood);

            if (id > 0) {
                Toast.makeText(this, "Thêm món ăn thành công", Toast.LENGTH_SHORT).show();
                setResult(RESULT_OK);
                finish();
            } else {
                Toast.makeText(this, "Không thể thêm món ăn", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void confirmDelete() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Xác nhận xóa");
        builder.setMessage("Bạn có chắc chắn muốn xóa món ăn này?");
        builder.setPositiveButton("Xóa", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                deleteFood();
            }
        });
        builder.setNegativeButton("Hủy", null);
        builder.show();
    }

    private void deleteFood() {
        if (food != null) {
            if (databaseHelper.deleteFood(food.getId())) {
                Toast.makeText(this, "Xóa món ăn thành công", Toast.LENGTH_SHORT).show();
                setResult(RESULT_OK);
                finish();
            } else {
                Toast.makeText(this, "Không thể xóa món ăn", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (requestCode == REQUEST_IMAGE_PICK && resultCode == RESULT_OK && data != null) {
            Uri selectedImageUri = data.getData();
            if (selectedImageUri != null) {
                // Save the URI string
                imageUrl = selectedImageUri.toString();
                
                // Use Glide to load and display the image
                RequestOptions requestOptions = new RequestOptions()
                    .placeholder(R.mipmap.ic_launcher)
                    .error(R.mipmap.ic_launcher)
                    .diskCacheStrategy(DiskCacheStrategy.ALL);
                
                Glide.with(this)
                    .load(selectedImageUri)
                    .apply(requestOptions)
                    .centerCrop()
                    .into(imageViewFoodPhoto);
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