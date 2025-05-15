package com.example.qlnhahangsesan.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Order implements Serializable {
    private long id;
    private long tableId;
    private Date orderDate;
    private double totalAmount;
    private String status; // "Đang xử lý", "Đã thanh toán", "Đã hủy"
    private List<OrderItem> orderItems;

    public Order() {
        this.orderItems = new ArrayList<>();
        this.orderDate = new Date();
        this.status = "Đang xử lý";
    }

    public Order(long tableId) {
        this();
        this.tableId = tableId;
    }

    public Order(long id, long tableId, Date orderDate, double totalAmount, String status) {
        this.id = id;
        this.tableId = tableId;
        this.orderDate = orderDate;
        this.totalAmount = totalAmount;
        this.status = status;
        this.orderItems = new ArrayList<>();
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getTableId() {
        return tableId;
    }

    public void setTableId(long tableId) {
        this.tableId = tableId;
    }

    public Date getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(Date orderDate) {
        this.orderDate = orderDate;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<OrderItem> getOrderItems() {
        return orderItems;
    }

    public void setOrderItems(List<OrderItem> orderItems) {
        this.orderItems = orderItems;
    }

    public void addOrderItem(OrderItem item) {
        this.orderItems.add(item);
        calculateTotal();
    }

    public void removeOrderItem(OrderItem item) {
        this.orderItems.remove(item);
        calculateTotal();
    }

    public void calculateTotal() {
        double total = 0;
        for (OrderItem item : orderItems) {
            total += item.getTotalPrice();
        }
        this.totalAmount = total;
    }
} 