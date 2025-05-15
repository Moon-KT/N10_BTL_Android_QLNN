package com.example.qlnhahangsesan.model;

import java.io.Serializable;

public class OrderItem implements Serializable {
    private long id;
    private long orderId;
    private long foodId;
    private String name;
    private int quantity;
    private double price;

    // Default constructor
    public OrderItem() {
        this.quantity = 0;
        this.price = 0;
    }

    // Constructor
    public OrderItem(String name, int quantity, double price) {
        this.name = name;
        this.quantity = quantity;
        this.price = price;
    }
    
    // Full constructor
    public OrderItem(long foodId, String name, int quantity, double price) {
        this.foodId = foodId;
        this.name = name;
        this.quantity = quantity;
        this.price = price;
    }

    // Getter và Setter
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }
    
    public long getOrderId() {
        return orderId;
    }

    public void setOrderId(long orderId) {
        this.orderId = orderId;
    }
    
    public long getFoodId() {
        return foodId;
    }

    public void setFoodId(long foodId) {
        this.foodId = foodId;
    }
    
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    // Tính tổng giá trị của món ăn
    public double getTotalPrice() {
        return price * quantity;
    }
}
