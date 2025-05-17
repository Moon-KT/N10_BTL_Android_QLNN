package com.example.qlnhahangsesan.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Food implements Serializable {
    private long id;
    private String name;
    private List<FoodCategory> categories;
    private double price;
    private String description;
    private String imageUrl;
    private boolean available;

    public Food() {
        this.categories = new ArrayList<>();
        this.categories.add(FoodCategory.MON_CHINH); // Default category
    }

    public Food(String name, List<FoodCategory> categories, double price, String description, String imageUrl, boolean available) {
        this.name = name;
        this.categories = categories != null ? categories : new ArrayList<>();
        this.price = price;
        this.description = description;
        this.imageUrl = imageUrl;
        this.available = available;
    }

    public Food(long id, String name, List<FoodCategory> categories, double price, String description, String imageUrl, boolean available) {
        this.id = id;
        this.name = name;
        this.categories = categories != null ? categories : new ArrayList<>();
        this.price = price;
        this.description = description;
        this.imageUrl = imageUrl;
        this.available = available;
    }
    
    // Constructor with String category for compatibility with existing code
    public Food(long id, String name, String categoryStr, double price, String description, String imageUrl, boolean available) {
        this.id = id;
        this.name = name;
        this.categories = new ArrayList<>();
        if (categoryStr != null && !categoryStr.isEmpty()) {
            // Handle comma-separated categories
            if (categoryStr.contains(",")) {
                String[] categoryStrings = categoryStr.split(",");
                for (String catStr : categoryStrings) {
                    FoodCategory cat = FoodCategory.fromString(catStr.trim());
                    if (cat != null) {
                        this.categories.add(cat);
                    }
                }
            } else {
                this.categories.add(FoodCategory.fromString(categoryStr));
            }
        }
        this.price = price;
        this.description = description;
        this.imageUrl = imageUrl;
        this.available = available;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<FoodCategory> getCategories() {
        return categories;
    }
    
    public void setCategories(List<FoodCategory> categories) {
        this.categories = categories != null ? categories : new ArrayList<>();
    }
    
    // For backward compatibility
    public FoodCategory getCategory() {
        return categories != null && !categories.isEmpty() ? categories.get(0) : FoodCategory.MON_CHINH;
    }
    
    // For backward compatibility
    public void setCategory(FoodCategory category) {
        if (this.categories == null) {
            this.categories = new ArrayList<>();
        }
        if (category != null) {
            if (this.categories.isEmpty()) {
                this.categories.add(category);
            } else {
                this.categories.set(0, category);
            }
        }
    }
    
    public String getCategoryString() {
        if (categories == null || categories.isEmpty()) {
            return "";
        }
        
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < categories.size(); i++) {
            sb.append(categories.get(i).getDisplayName());
            if (i < categories.size() - 1) {
                sb.append(", ");
            }
        }
        return sb.toString();
    }
    
    // For compatibility with existing code
    public void setCategory(String categoryStr) {
        if (this.categories == null) {
            this.categories = new ArrayList<>();
        } else {
            this.categories.clear();
        }
        
        if (categoryStr != null && !categoryStr.isEmpty()) {
            // Handle comma-separated categories
            if (categoryStr.contains(",")) {
                String[] categoryStrings = categoryStr.split(",");
                for (String catStr : categoryStrings) {
                    FoodCategory cat = FoodCategory.fromString(catStr.trim());
                    if (cat != null) {
                        this.categories.add(cat);
                    }
                }
            } else {
                this.categories.add(FoodCategory.fromString(categoryStr));
            }
        }
    }
    
    public void addCategory(FoodCategory category) {
        if (this.categories == null) {
            this.categories = new ArrayList<>();
        }
        if (category != null && !this.categories.contains(category)) {
            this.categories.add(category);
        }
    }
    
    public void removeCategory(FoodCategory category) {
        if (this.categories != null && category != null) {
            this.categories.remove(category);
        }
    }
    
    public boolean hasCategory(FoodCategory category) {
        return this.categories != null && category != null && this.categories.contains(category);
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    @Override
    public String toString() {
        return name;
    }
    
    // Static test method for debugging
    public static void testCategoryParsing(String categoryStr) {
        Food testFood = new Food();
        testFood.setCategory(categoryStr);
        System.out.println("Input string: " + categoryStr);
        System.out.println("Parsed categories: " + testFood.getCategories().size());
        System.out.println("Category string: " + testFood.getCategoryString());
        for (FoodCategory category : testFood.getCategories()) {
            System.out.println(" - " + category.getDisplayName());
        }
    }
} 