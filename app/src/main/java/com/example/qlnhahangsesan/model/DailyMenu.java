package com.example.qlnhahangsesan.model;

import java.io.Serializable;
import java.util.Date;

public class DailyMenu implements Serializable {
    private long id;
    private String date; // Định dạng "yyyy-MM-dd"
    private long foodId;
    private String foodName; // Để tiện hiển thị, không lưu vào DB
    private String foodCategory; // Để tiện hiển thị, không lưu vào DB
    private double foodPrice; // Để tiện hiển thị, không lưu vào DB
    private String foodImageUrl; // Để tiện hiển thị, không lưu vào DB
    private boolean featured; // Món đặc biệt trong ngày
    private int quantity; // Số lượng chuẩn bị

    public DailyMenu() {
    }

    public DailyMenu(String date, long foodId, boolean featured, int quantity) {
        this.date = date;
        this.foodId = foodId;
        this.featured = featured;
        this.quantity = quantity;
    }

    public DailyMenu(long id, String date, long foodId, boolean featured, int quantity) {
        this.id = id;
        this.date = date;
        this.foodId = foodId;
        this.featured = featured;
        this.quantity = quantity;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public long getFoodId() {
        return foodId;
    }

    public void setFoodId(long foodId) {
        this.foodId = foodId;
    }

    public String getFoodName() {
        return foodName;
    }

    public void setFoodName(String foodName) {
        this.foodName = foodName;
    }

    public String getFoodCategory() {
        return foodCategory;
    }

    public void setFoodCategory(String foodCategory) {
        this.foodCategory = foodCategory;
    }

    public double getFoodPrice() {
        return foodPrice;
    }

    public void setFoodPrice(double foodPrice) {
        this.foodPrice = foodPrice;
    }

    public String getFoodImageUrl() {
        return foodImageUrl;
    }

    public void setFoodImageUrl(String foodImageUrl) {
        this.foodImageUrl = foodImageUrl;
    }

    public boolean isFeatured() {
        return featured;
    }

    public void setFeatured(boolean featured) {
        this.featured = featured;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
} 