package com.example.qlnhahangsesan.model;

import java.io.Serializable;

public class Food implements Serializable {
    private long id;
    private String name;
    private FoodCategory category;
    private double price;
    private String description;
    private String imageUrl;
    private boolean available;

    public Food() {
        this.category = FoodCategory.MON_CHINH; // Default category
    }

    public Food(String name, FoodCategory category, double price, String description, String imageUrl, boolean available) {
        this.name = name;
        this.category = category;
        this.price = price;
        this.description = description;
        this.imageUrl = imageUrl;
        this.available = available;
    }

    public Food(long id, String name, FoodCategory category, double price, String description, String imageUrl, boolean available) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.price = price;
        this.description = description;
        this.imageUrl = imageUrl;
        this.available = available;
    }
    
    // Constructor with String category for compatibility with existing code
    public Food(long id, String name, String categoryStr, double price, String description, String imageUrl, boolean available) {
        this.id = id;
        this.name = name;
        this.category = FoodCategory.fromString(categoryStr);
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

    public FoodCategory getCategory() {
        return category;
    }
    
    public String getCategoryString() {
        return category != null ? category.getDisplayName() : "";
    }

    public void setCategory(FoodCategory category) {
        this.category = category;
    }
    
    // For compatibility with existing code
    public void setCategory(String categoryStr) {
        this.category = FoodCategory.fromString(categoryStr);
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
} 