package com.example.qlnhahangsesan.model;

import java.io.Serializable;

public class Table implements Serializable {
    private long id;
    private String name;
    private int capacity;
    private String status; // "Trống", "Đã đặt", "Đang phục vụ"
    private String note;
    private String tableType; // "VIP", "Thường"

    public Table() {
    }

    public Table(String name, int capacity, String status, String note, String tableType) {
        this.name = name;
        this.capacity = capacity;
        this.status = status;
        this.note = note;
        this.tableType = tableType;
    }

    public Table(long id, String name, int capacity, String status, String note, String tableType) {
        this.id = id;
        this.name = name;
        this.capacity = capacity;
        this.status = status;
        this.note = note;
        this.tableType = tableType;
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

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }
    
    public String getTableType() {
        return tableType;
    }
    
    public void setTableType(String tableType) {
        this.tableType = tableType;
    }

    @Override
    public String toString() {
        return name;
    }
} 