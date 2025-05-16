package com.example.qlnhahangsesan.model;

public enum Position {
    CHEF("Đầu bếp"),
    WAITER("Bồi bàn"),
    CASHIER("Thu ngân");

    private final String displayName;

    Position(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }

    public static Position fromString(String positionString) {
        for (Position position : Position.values()) {
            if (position.displayName.equalsIgnoreCase(positionString)) {
                return position;
            }
        }
        // Default to WAITER if no match found
        return WAITER;
    }
} 