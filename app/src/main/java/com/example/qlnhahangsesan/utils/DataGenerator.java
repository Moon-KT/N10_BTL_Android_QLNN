package com.example.qlnhahangsesan.utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.widget.Toast;

import com.example.qlnhahangsesan.database.DatabaseHelper;
import com.example.qlnhahangsesan.model.Food;
import com.example.qlnhahangsesan.model.Table;
import com.example.qlnhahangsesan.model.DailyMenu;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;

/**
 * Utility class to generate mock data for the application
 */
public class DataGenerator {
    
    // Database table names and columns (copied from DatabaseHelper)
    private static final String TABLE_FOODS = "foods";
    private static final String KEY_FOOD_ID = "id";
    private static final String KEY_FOOD_NAME = "name";
    private static final String KEY_FOOD_CATEGORY = "category";
    private static final String KEY_FOOD_PRICE = "price";
    private static final String KEY_FOOD_DESCRIPTION = "description";
    private static final String KEY_FOOD_IMAGE_URL = "image_url";
    private static final String KEY_FOOD_AVAILABLE = "available";
    
    private static final String TABLE_TABLES = "tables";
    private static final String KEY_TABLE_ID = "id";
    private static final String KEY_TABLE_NAME = "name";
    private static final String KEY_TABLE_CAPACITY = "capacity";
    private static final String KEY_TABLE_STATUS = "status";
    private static final String KEY_TABLE_NOTE = "note";
    
    private static final String TABLE_DAILY_MENU = "daily_menu";
    private static final String KEY_DAILY_MENU_ID = "id";
    private static final String KEY_DAILY_MENU_DATE = "date";
    private static final String KEY_DAILY_MENU_FOOD_ID = "food_id";
    private static final String KEY_DAILY_MENU_FEATURED = "featured";
    private static final String KEY_DAILY_MENU_QUANTITY = "quantity";
    
    // Needed for the revenue data
    private static final String TABLE_ORDERS = "orders";
    private static final String KEY_ORDER_ID = "id";
    private static final String KEY_ORDER_DATE = "order_date";
    private static final String KEY_ORDER_TABLE_ID = "table_id";
    private static final String KEY_ORDER_TOTAL_AMOUNT = "total_amount";
    private static final String KEY_ORDER_STATUS = "status";
    
    private static final String TABLE_ORDER_ITEMS = "order_items";
    private static final String KEY_ORDER_ITEM_ID = "id";
    private static final String KEY_ORDER_ITEM_ORDER_ID = "order_id";
    private static final String KEY_ORDER_ITEM_FOOD_ID = "food_id";
    private static final String KEY_ORDER_ITEM_QUANTITY = "quantity";
    private static final String KEY_ORDER_ITEM_PRICE = "price";
    
    private Context context;
    private DatabaseHelper databaseHelper;
    private Random random;
    private SimpleDateFormat dateFormat;
    
    public DataGenerator(Context context) {
        this.context = context;
        this.databaseHelper = DatabaseHelper.getInstance(context);
        this.random = new Random();
        this.dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    }
    
    /**
     * Generate all mock data needed for statistics
     */
    public void generateMockData() {
        SQLiteDatabase db = databaseHelper.getWritableDatabase();
        
        // Create tables if they don't exist
        createOrderTables(db);
        
        // Generate mock data
        generateFoodData();
        generateTableData();
        generateDailyMenuData();
        generateOrderData();
        
        Toast.makeText(context, "Dữ liệu mẫu đã được tạo thành công!", Toast.LENGTH_SHORT).show();
    }
    
    private void createOrderTables(SQLiteDatabase db) {
        // Create orders table if it doesn't exist
        String CREATE_ORDERS_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE_ORDERS +
                "(" +
                KEY_ORDER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                KEY_ORDER_DATE + " TEXT," +
                KEY_ORDER_TABLE_ID + " INTEGER," +
                KEY_ORDER_TOTAL_AMOUNT + " REAL," +
                KEY_ORDER_STATUS + " TEXT," +
                "FOREIGN KEY(" + KEY_ORDER_TABLE_ID + ") REFERENCES " + TABLE_TABLES + "(" + KEY_TABLE_ID + ")" +
                ")";
        
        // Create order_items table if it doesn't exist
        String CREATE_ORDER_ITEMS_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE_ORDER_ITEMS +
                "(" +
                KEY_ORDER_ITEM_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                KEY_ORDER_ITEM_ORDER_ID + " INTEGER," +
                KEY_ORDER_ITEM_FOOD_ID + " INTEGER," +
                KEY_ORDER_ITEM_QUANTITY + " INTEGER," +
                KEY_ORDER_ITEM_PRICE + " REAL," +
                "FOREIGN KEY(" + KEY_ORDER_ITEM_ORDER_ID + ") REFERENCES " + TABLE_ORDERS + "(" + KEY_ORDER_ID + ")," +
                "FOREIGN KEY(" + KEY_ORDER_ITEM_FOOD_ID + ") REFERENCES " + TABLE_FOODS + "(" + KEY_FOOD_ID + ")" +
                ")";
        
        try {
            db.execSQL(CREATE_ORDERS_TABLE);
            db.execSQL(CREATE_ORDER_ITEMS_TABLE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Generate sample food data
     */
    private void generateFoodData() {
        List<Food> foods = databaseHelper.getAllFoods();
        if (!foods.isEmpty()) {
            // Foods already exist, no need to generate
            return;
        }
        
        // Categories
        String[] categories = {"Món khai vị", "Món chính", "Món tráng miệng", "Đồ uống", "Món đặc biệt"};
        
        // Sample foods
        String[][] foodData = {
            // {name, category, price, description}
            {"Gỏi cuốn tôm thịt", "Món khai vị", "45000", "Gỏi cuốn tươi ngon với tôm, thịt và rau sống"},
            {"Chả giò hải sản", "Món khai vị", "55000", "Chả giò giòn rụm với nhân hải sản"},
            {"Súp cua", "Món khai vị", "60000", "Súp cua thơm ngon, bổ dưỡng"},
            {"Salad trộn kiểu Âu", "Món khai vị", "65000", "Salad tươi mát với sốt đặc biệt"},
            {"Cơm chiên hải sản", "Món chính", "85000", "Cơm chiên với hải sản tươi ngon"},
            {"Bò lúc lắc", "Món chính", "125000", "Bò xào với hành tây và ớt chuông"},
            {"Cá hồi nướng", "Món chính", "155000", "Cá hồi Na Uy nướng với sốt chanh dây"},
            {"Gà nướng muối ớt", "Món chính", "145000", "Gà ta nướng với muối ớt đặc biệt"},
            {"Lẩu thái hải sản", "Món chính", "250000", "Lẩu thái chua cay với hải sản tươi sống"},
            {"Rau muống xào tỏi", "Món chính", "45000", "Rau muống xào với tỏi"},
            {"Chè trôi nước", "Món tráng miệng", "35000", "Chè trôi nước truyền thống"},
            {"Bánh flan", "Món tráng miệng", "30000", "Bánh flan caramen mềm mịn"},
            {"Trái cây thập cẩm", "Món tráng miệng", "55000", "Đĩa trái cây theo mùa"},
            {"Nước ép cam", "Đồ uống", "35000", "Nước cam tươi ép nguyên chất"},
            {"Sinh tố bơ", "Đồ uống", "40000", "Sinh tố bơ đặc"},
            {"Cà phê đen", "Đồ uống", "25000", "Cà phê đen truyền thống"},
            {"Trà gừng", "Đồ uống", "25000", "Trà gừng nóng"},
            {"Cua hoàng đế hấp", "Món đặc biệt", "750000", "Cua hoàng đế hấp với gừng và hành"},
            {"Tôm hùm nướng phô mai", "Món đặc biệt", "950000", "Tôm hùm nướng với phô mai"},
            {"Bò Wagyu", "Món đặc biệt", "1250000", "Bò Wagyu A5 nướng"}
        };
        
        // Add foods
        for (String[] food : foodData) {
            Food newFood = new Food();
            newFood.setName(food[0]);
            newFood.setCategory(food[1]);
            newFood.setPrice(Double.parseDouble(food[2]));
            newFood.setDescription(food[3]);
            newFood.setImageUrl(""); // No image for mock data
            newFood.setAvailable(true);
            
            databaseHelper.addFood(newFood);
        }
    }
    
    /**
     * Generate sample table data
     */
    private void generateTableData() {
        List<Table> tables = databaseHelper.getAllTables();
        if (!tables.isEmpty()) {
            // Tables already exist, just update some statuses
            updateTableStatuses();
            return;
        }
        
        // Status options
        String[] statuses = {"Trống", "Đã đặt", "Đang phục vụ"};
        
        // Generate 20 tables
        for (int i = 1; i <= 20; i++) {
            Table table = new Table();
            table.setName("Bàn " + i);
            table.setCapacity(random.nextInt(6) + 2); // 2-8 people
            
            // Randomize status, but make most tables available
            int statusIndex = random.nextInt(100) < 70 ? 0 : random.nextInt(3);
            table.setStatus(statuses[statusIndex]);
            
            table.setNote(statusIndex == 0 ? "" : "Khách đặt lúc " + 
                    String.format("%02d:%02d", random.nextInt(12) + 8, random.nextInt(6) * 10));
            
            databaseHelper.addTable(table);
        }
    }
    
    /**
     * Update some table statuses to ensure variety in statistics
     */
    private void updateTableStatuses() {
        List<Table> tables = databaseHelper.getAllTables();
        if (tables.isEmpty()) return;
        
        String[] statuses = {"Trống", "Đã đặt", "Đang phục vụ"};
        
        // Update about half the tables with random statuses
        for (int i = 0; i < tables.size() / 2; i++) {
            Table table = tables.get(random.nextInt(tables.size()));
            String newStatus = statuses[random.nextInt(statuses.length)];
            databaseHelper.updateTableStatus(table.getId(), newStatus);
        }
    }
    
    /**
     * Generate daily menu data
     */
    private void generateDailyMenuData() {
        // Generate menu for the last 30 days
        List<Food> foods = databaseHelper.getAllFoods();
        if (foods.isEmpty()) return;
        
        Calendar calendar = Calendar.getInstance();
        
        // Check if we already have menu data
        List<String> existingDates = databaseHelper.getAvailableDailyMenuDates();
        if (!existingDates.isEmpty()) {
            // We already have some menu data, just generate for new dates if needed
            String today = dateFormat.format(calendar.getTime());
            if (existingDates.contains(today)) {
                return;
            }
        }
        
        // Generate menu for the last 30 days
        for (int day = 0; day < 30; day++) {
            calendar.add(Calendar.DAY_OF_MONTH, -1);
            String date = dateFormat.format(calendar.getTime());
            
            // Skip if we already have menu for this date
            if (existingDates.contains(date)) {
                continue;
            }
            
            // Add 8-15 random foods to the menu for this day
            int menuSize = random.nextInt(8) + 8;
            List<Long> selectedFoodIds = new ArrayList<>();
            
            for (int i = 0; i < menuSize; i++) {
                Food food = foods.get(random.nextInt(foods.size()));
                if (selectedFoodIds.contains(food.getId())) {
                    continue; // Skip duplicates
                }
                
                selectedFoodIds.add(food.getId());
                
                DailyMenu menuItem = new DailyMenu();
                menuItem.setDate(date);
                menuItem.setFoodId(food.getId());
                menuItem.setFeatured(random.nextInt(10) < 2); // 20% chance to be featured
                menuItem.setQuantity(random.nextInt(20) + 10); // 10-30 quantity
                
                databaseHelper.addDailyMenuItem(menuItem);
            }
        }
    }
    
    /**
     * Generate order data (needed for revenue statistics and top foods)
     */
    private void generateOrderData() {
        SQLiteDatabase db = databaseHelper.getWritableDatabase();
        
        // Check if we already have order data
        Cursor cursor = (Cursor) db.rawQuery("SELECT COUNT(*) FROM " + TABLE_ORDERS, null);
        try {
            if (cursor.moveToFirst() && cursor.getInt(0) > 0) {
                // We already have order data, just add a few more recent orders
                generateRecentOrders(db, 5);
                return;
            }
        } catch (Exception e) {
            // Table probably doesn't exist
        } finally {
            if (cursor != null) cursor.close();
        }
        
        // Get foods and tables
        List<Food> foods = databaseHelper.getAllFoods();
        List<Table> tables = databaseHelper.getAllTables();
        
        if (foods.isEmpty() || tables.isEmpty()) return;
        
        // Generate orders for the last 30 days
        Calendar calendar = Calendar.getInstance();
        String[] orderStatuses = {"Đã thanh toán", "Đã hủy"};
        
        for (int day = 0; day < 30; day++) {
            calendar.add(Calendar.DAY_OF_MONTH, -1);
            String date = dateFormat.format(calendar.getTime());
            
            // Generate 5-15 orders per day
            int ordersPerDay = random.nextInt(11) + 5;
            
            for (int i = 0; i < ordersPerDay; i++) {
                // Insert order
                ContentValues orderValues = new ContentValues();
                orderValues.put(KEY_ORDER_DATE, date);
                orderValues.put(KEY_ORDER_TABLE_ID, tables.get(random.nextInt(tables.size())).getId());
                orderValues.put(KEY_ORDER_STATUS, orderStatuses[random.nextInt(10) < 9 ? 0 : 1]); // 90% completed
                
                // We'll calculate total after adding items
                double totalAmount = 0;
                
                long orderId = db.insert(TABLE_ORDERS, null, orderValues);
                
                // Add 1-6 items to the order
                int itemCount = random.nextInt(6) + 1;
                List<Long> selectedFoodIds = new ArrayList<>();
                
                for (int j = 0; j < itemCount; j++) {
                    Food food = foods.get(random.nextInt(foods.size()));
                    
                    // Avoid duplicates
                    if (selectedFoodIds.contains(food.getId())) {
                        continue;
                    }
                    
                    selectedFoodIds.add(food.getId());
                    
                    int quantity = random.nextInt(3) + 1; // 1-3 items per food
                    double price = food.getPrice();
                    
                    ContentValues itemValues = new ContentValues();
                    itemValues.put(KEY_ORDER_ITEM_ORDER_ID, orderId);
                    itemValues.put(KEY_ORDER_ITEM_FOOD_ID, food.getId());
                    itemValues.put(KEY_ORDER_ITEM_QUANTITY, quantity);
                    itemValues.put(KEY_ORDER_ITEM_PRICE, price);
                    
                    db.insert(TABLE_ORDER_ITEMS, null, itemValues);
                    
                    totalAmount += price * quantity;
                }
                
                // Update order with total amount
                ContentValues updateValues = new ContentValues();
                updateValues.put(KEY_ORDER_TOTAL_AMOUNT, totalAmount);
                db.update(TABLE_ORDERS, updateValues, KEY_ORDER_ID + " = ?", 
                        new String[]{String.valueOf(orderId)});
            }
        }
    }
    
    /**
     * Generate a few more recent orders (for today)
     */
    private void generateRecentOrders(SQLiteDatabase db, int count) {
        // Get foods and tables
        List<Food> foods = databaseHelper.getAllFoods();
        List<Table> tables = databaseHelper.getAllTables();
        
        if (foods.isEmpty() || tables.isEmpty()) return;
        
        String today = dateFormat.format(new Date());
        String[] orderStatuses = {"Đã thanh toán", "Đã hủy"};
        
        for (int i = 0; i < count; i++) {
            // Insert order
            ContentValues orderValues = new ContentValues();
            orderValues.put(KEY_ORDER_DATE, today);
            orderValues.put(KEY_ORDER_TABLE_ID, tables.get(random.nextInt(tables.size())).getId());
            orderValues.put(KEY_ORDER_STATUS, orderStatuses[random.nextInt(10) < 9 ? 0 : 1]); // 90% completed
            
            double totalAmount = 0;
            long orderId = db.insert(TABLE_ORDERS, null, orderValues);
            
            // Add 1-6 items to the order
            int itemCount = random.nextInt(6) + 1;
            List<Long> selectedFoodIds = new ArrayList<>();
            
            for (int j = 0; j < itemCount; j++) {
                Food food = foods.get(random.nextInt(foods.size()));
                
                // Avoid duplicates
                if (selectedFoodIds.contains(food.getId())) {
                    continue;
                }
                
                selectedFoodIds.add(food.getId());
                
                int quantity = random.nextInt(3) + 1; // 1-3 items per food
                double price = food.getPrice();
                
                ContentValues itemValues = new ContentValues();
                itemValues.put(KEY_ORDER_ITEM_ORDER_ID, orderId);
                itemValues.put(KEY_ORDER_ITEM_FOOD_ID, food.getId());
                itemValues.put(KEY_ORDER_ITEM_QUANTITY, quantity);
                itemValues.put(KEY_ORDER_ITEM_PRICE, price);
                
                db.insert(TABLE_ORDER_ITEMS, null, itemValues);
                
                totalAmount += price * quantity;
            }
            
            // Update order with total amount
            ContentValues updateValues = new ContentValues();
            updateValues.put(KEY_ORDER_TOTAL_AMOUNT, totalAmount);
            db.update(TABLE_ORDERS, updateValues, KEY_ORDER_ID + " = ?", 
                    new String[]{String.valueOf(orderId)});
        }
    }
    
    // Inner class for compatibility with different database versions
    private class Cursor implements AutoCloseable {
        private android.database.Cursor cursor;
        
        Cursor(android.database.Cursor cursor) {
            this.cursor = cursor;
        }
        
        boolean moveToFirst() {
            return cursor.moveToFirst();
        }
        
        int getInt(int columnIndex) {
            return cursor.getInt(columnIndex);
        }
        
        @Override
        public void close() {
            cursor.close();
        }
    }
} 