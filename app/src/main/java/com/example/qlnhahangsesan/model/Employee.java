package com.example.qlnhahangsesan.model;

import java.io.Serializable;

public class Employee implements Serializable {
    private long id;
    private String name;
    private Position position;
    private String phone;
    private String email;
    private String address;
    private double salary;
    private String startDate;

    public Employee() {
        // Default to waiter position
        this.position = Position.WAITER;
    }

    public Employee(String name, Position position, String phone, String email, String address, double salary, String startDate) {
        this.name = name;
        this.position = position;
        this.phone = phone;
        this.email = email;
        this.address = address;
        this.salary = salary;
        this.startDate = startDate;
    }

    public Employee(long id, String name, Position position, String phone, String email, String address, double salary, String startDate) {
        this.id = id;
        this.name = name;
        this.position = position;
        this.phone = phone;
        this.email = email;
        this.address = address;
        this.salary = salary;
        this.startDate = startDate;
    }

    // Legacy constructor accepting string position (for backward compatibility)
    public Employee(String name, String positionString, String phone, String email, String address, double salary, String startDate) {
        this.name = name;
        this.position = Position.fromString(positionString);
        this.phone = phone;
        this.email = email;
        this.address = address;
        this.salary = salary;
        this.startDate = startDate;
    }

    // Legacy constructor accepting string position (for backward compatibility)
    public Employee(long id, String name, String positionString, String phone, String email, String address, double salary, String startDate) {
        this.id = id;
        this.name = name;
        this.position = Position.fromString(positionString);
        this.phone = phone;
        this.email = email;
        this.address = address;
        this.salary = salary;
        this.startDate = startDate;
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

    public Position getPosition() {
        return position;
    }

    public void setPosition(Position position) {
        this.position = position;
    }

    // For backward compatibility with database
    public String getPositionString() {
        return position != null ? position.getDisplayName() : "";
    }

    // For backward compatibility with database
    public void setPosition(String positionString) {
        this.position = Position.fromString(positionString);
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public double getSalary() {
        return salary;
    }

    public void setSalary(double salary) {
        this.salary = salary;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    @Override
    public String toString() {
        return name;
    }
} 