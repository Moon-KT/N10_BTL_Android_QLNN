package com.example.qlnhahangsesan.model;

public enum FoodCategory {
    KHAI_VI("Khai vị"),
    MON_CHINH("Món chính"),
    MON_CHAY("Món chay"),
    LAU("Lẩu"),
    MON_NUONG("Món nướng"),
    MON_XAO("Món xào"),
    MON_HAP("Món hấp"),
    TRANG_MIENG("Tráng miệng"),
    DO_UONG("Đồ uống"),
    DAC_SAN("Đặc sản");

    private final String displayName;

    FoodCategory(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static FoodCategory fromString(String text) {
        for (FoodCategory category : FoodCategory.values()) {
            if (category.displayName.equalsIgnoreCase(text)) {
                return category;
            }
        }
        return MON_CHINH; // Default value
    }

    @Override
    public String toString() {
        return displayName;
    }
} 