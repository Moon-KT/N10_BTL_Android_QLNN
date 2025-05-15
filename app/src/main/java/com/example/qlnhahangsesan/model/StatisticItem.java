package com.example.qlnhahangsesan.model;

import java.io.Serializable;

public class StatisticItem implements Serializable {
    private String name;
    private double value;

    public StatisticItem() {
    }

    public StatisticItem(String name, int value) {
        this.name = name;
        this.value = value;
    }

    public StatisticItem(String name, double value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }

    public int getIntValue() {
        return (int) value;
    }
} 