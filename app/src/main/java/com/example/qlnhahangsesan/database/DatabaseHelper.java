package com.example.qlnhahangsesan.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import com.example.qlnhahangsesan.model.DailyMenu;
import com.example.qlnhahangsesan.model.Employee;
import com.example.qlnhahangsesan.model.Food;
import com.example.qlnhahangsesan.model.Table;
import com.example.qlnhahangsesan.model.StatisticItem;
import com.example.qlnhahangsesan.model.Order;
import com.example.qlnhahangsesan.model.OrderItem;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String TAG = "DatabaseHelper";

    // Database Info
    private static final String DATABASE_NAME = "SesanRestaurantDB";
    private static final int DATABASE_VERSION = 5;

    // Table Names
    private static final String TABLE_USERS = "users";
    private static final String TABLE_EMPLOYEES = "employees";
    private static final String TABLE_FOODS = "foods";
    private static final String TABLE_TABLES = "tables";
    private static final String TABLE_DAILY_MENU = "daily_menu";
    private static final String TABLE_ORDERS = "orders";
    private static final String TABLE_ORDER_ITEMS = "order_items";

    // User Table Columns
    private static final String KEY_USER_ID = "id";
    private static final String KEY_USER_FULLNAME = "fullname";
    private static final String KEY_USER_USERNAME = "username";
    private static final String KEY_USER_PASSWORD = "password";
    
    // Employee Table Columns
    private static final String KEY_EMPLOYEE_ID = "id";
    private static final String KEY_EMPLOYEE_NAME = "name";
    private static final String KEY_EMPLOYEE_POSITION = "position";
    private static final String KEY_EMPLOYEE_PHONE = "phone";
    private static final String KEY_EMPLOYEE_EMAIL = "email";
    private static final String KEY_EMPLOYEE_ADDRESS = "address";
    private static final String KEY_EMPLOYEE_SALARY = "salary";
    private static final String KEY_EMPLOYEE_START_DATE = "start_date";
    
    // Food Table Columns
    private static final String KEY_FOOD_ID = "id";
    private static final String KEY_FOOD_NAME = "name";
    private static final String KEY_FOOD_CATEGORY = "category";
    private static final String KEY_FOOD_PRICE = "price";
    private static final String KEY_FOOD_DESCRIPTION = "description";
    private static final String KEY_FOOD_IMAGE_URL = "image_url";
    private static final String KEY_FOOD_AVAILABLE = "available";
    
    // Table Table Columns
    private static final String KEY_TABLE_ID = "id";
    private static final String KEY_TABLE_NAME = "name";
    private static final String KEY_TABLE_CAPACITY = "capacity";
    private static final String KEY_TABLE_STATUS = "status";
    private static final String KEY_TABLE_NOTE = "note";
    private static final String KEY_TABLE_TYPE = "table_type";

    // Daily Menu Table Columns
    private static final String KEY_DAILY_MENU_ID = "id";
    private static final String KEY_DAILY_MENU_DATE = "date";
    private static final String KEY_DAILY_MENU_FOOD_ID = "food_id";
    private static final String KEY_DAILY_MENU_FEATURED = "featured";
    private static final String KEY_DAILY_MENU_QUANTITY = "quantity";

    // Statistics Columns
    private static final String KEY_ORDER_DATE = "order_date";
    private static final String KEY_ORDER_TOTAL_AMOUNT = "total_amount";
    private static final String KEY_ORDER_STATUS = "status";
    private static final String KEY_ORDER_ITEM_QUANTITY = "quantity";
    private static final String KEY_ORDER_ITEM_FOOD_ID = "food_id";
    private static final String KEY_ORDER_ITEM_ORDER_ID = "order_id";
    private static final String KEY_ORDER_ID = "id";
    private static final String KEY_ORDER_TABLE_ID = "table_id";
    private static final String KEY_ORDER_ITEM_PRICE = "price";
    private  static final String KEY_ORDER_ITEM_ID = "id";
    // Singleton instance
    private static DatabaseHelper sInstance;
    
    // Random generator for fallback data
    private Random random = new Random();

    public static synchronized DatabaseHelper getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new DatabaseHelper(context.getApplicationContext());
        }
        return sInstance;
    }

    private DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_USERS_TABLE = "CREATE TABLE " + TABLE_USERS +
                "(" +
                KEY_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                KEY_USER_FULLNAME + " TEXT," +
                KEY_USER_USERNAME + " TEXT UNIQUE," +
                KEY_USER_PASSWORD + " TEXT" +
                ")";

        String CREATE_EMPLOYEES_TABLE = "CREATE TABLE " + TABLE_EMPLOYEES +
                "(" +
                KEY_EMPLOYEE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                KEY_EMPLOYEE_NAME + " TEXT," +
                KEY_EMPLOYEE_POSITION + " TEXT," +
                KEY_EMPLOYEE_PHONE + " TEXT UNIQUE," +
                KEY_EMPLOYEE_EMAIL + " TEXT," +
                KEY_EMPLOYEE_ADDRESS + " TEXT," +
                KEY_EMPLOYEE_SALARY + " REAL," +
                KEY_EMPLOYEE_START_DATE + " TEXT" +
                ")";
                
        String CREATE_FOODS_TABLE = "CREATE TABLE " + TABLE_FOODS +
                "(" +
                KEY_FOOD_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                KEY_FOOD_NAME + " TEXT," +
                KEY_FOOD_CATEGORY + " TEXT," +
                KEY_FOOD_PRICE + " REAL," +
                KEY_FOOD_DESCRIPTION + " TEXT," +
                KEY_FOOD_IMAGE_URL + " TEXT," +
                KEY_FOOD_AVAILABLE + " INTEGER" +
                ")";
                
        String CREATE_TABLES_TABLE = "CREATE TABLE " + TABLE_TABLES +
                "(" +
                KEY_TABLE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                KEY_TABLE_NAME + " TEXT," +
                KEY_TABLE_CAPACITY + " INTEGER," +
                KEY_TABLE_STATUS + " TEXT," +
                KEY_TABLE_NOTE + " TEXT," +
                KEY_TABLE_TYPE + " TEXT" +
                ")";

        String CREATE_DAILY_MENU_TABLE = "CREATE TABLE " + TABLE_DAILY_MENU +
                "(" +
                KEY_DAILY_MENU_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                KEY_DAILY_MENU_DATE + " TEXT," +
                KEY_DAILY_MENU_FOOD_ID + " INTEGER," +
                KEY_DAILY_MENU_FEATURED + " INTEGER," +
                KEY_DAILY_MENU_QUANTITY + " INTEGER," +
                "FOREIGN KEY(" + KEY_DAILY_MENU_FOOD_ID + ") REFERENCES " + TABLE_FOODS + "(" + KEY_FOOD_ID + ")" +
                ")";

        String CREATE_ORDERS_TABLE = "CREATE TABLE " + TABLE_ORDERS +
                "(" +
                KEY_ORDER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                KEY_ORDER_TABLE_ID + " INTEGER," +
                KEY_ORDER_DATE + " TEXT," +
                KEY_ORDER_TOTAL_AMOUNT + " REAL," +
                KEY_ORDER_STATUS + " TEXT" +
                ")";

        String CREATE_ORDER_ITEMS_TABLE = "CREATE TABLE " + TABLE_ORDER_ITEMS +
                "(" +
                KEY_ORDER_ITEM_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                KEY_ORDER_ITEM_ORDER_ID + " INTEGER," +
                KEY_ORDER_ITEM_FOOD_ID + " INTEGER," +
                KEY_ORDER_ITEM_QUANTITY + " INTEGER," +
                KEY_ORDER_ITEM_PRICE + " REAL" +
                ")";

        db.execSQL(CREATE_USERS_TABLE);
        db.execSQL(CREATE_EMPLOYEES_TABLE);
        db.execSQL(CREATE_FOODS_TABLE);
        db.execSQL(CREATE_TABLES_TABLE);
        db.execSQL(CREATE_DAILY_MENU_TABLE);
        db.execSQL(CREATE_ORDERS_TABLE);
        db.execSQL(CREATE_ORDER_ITEMS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            // Add tables table if upgrading from version 1
            String CREATE_TABLES_TABLE = "CREATE TABLE " + TABLE_TABLES +
                    "(" +
                    KEY_TABLE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    KEY_TABLE_NAME + " TEXT," +
                    KEY_TABLE_CAPACITY + " INTEGER," +
                    KEY_TABLE_STATUS + " TEXT," +
                    KEY_TABLE_NOTE + " TEXT," +
                    KEY_TABLE_TYPE + " TEXT" +
                    ")";
            db.execSQL(CREATE_TABLES_TABLE);
        }
        
        if (oldVersion < 3) {
            // Add daily menu table if upgrading from version 2
            String CREATE_DAILY_MENU_TABLE = "CREATE TABLE " + TABLE_DAILY_MENU +
                    "(" +
                    KEY_DAILY_MENU_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    KEY_DAILY_MENU_DATE + " TEXT," +
                    KEY_DAILY_MENU_FOOD_ID + " INTEGER," +
                    KEY_DAILY_MENU_FEATURED + " INTEGER," +
                    KEY_DAILY_MENU_QUANTITY + " INTEGER," +
                    "FOREIGN KEY(" + KEY_DAILY_MENU_FOOD_ID + ") REFERENCES " + TABLE_FOODS + "(" + KEY_FOOD_ID + ")" +
                    ")";
            db.execSQL(CREATE_DAILY_MENU_TABLE);
        }
        
        if (oldVersion < 4) {
            // Upgrade the employees table to make phone numbers unique
            try {
                // Create a new temporary table with unique phone field
                String CREATE_EMPLOYEES_TEMP_TABLE = "CREATE TABLE employees_temp" +
                    "(" +
                    KEY_EMPLOYEE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    KEY_EMPLOYEE_NAME + " TEXT," +
                    KEY_EMPLOYEE_POSITION + " TEXT," +
                    KEY_EMPLOYEE_PHONE + " TEXT UNIQUE," +
                    KEY_EMPLOYEE_EMAIL + " TEXT," +
                    KEY_EMPLOYEE_ADDRESS + " TEXT," +
                    KEY_EMPLOYEE_SALARY + " REAL," +
                    KEY_EMPLOYEE_START_DATE + " TEXT" +
                    ")";
                db.execSQL(CREATE_EMPLOYEES_TEMP_TABLE);
                
                // Copy data from old table to new table, ignoring duplicates
                db.execSQL("INSERT OR IGNORE INTO employees_temp SELECT * FROM " + TABLE_EMPLOYEES);
                
                // Drop old table
                db.execSQL("DROP TABLE " + TABLE_EMPLOYEES);
                
                // Rename new table
                db.execSQL("ALTER TABLE employees_temp RENAME TO " + TABLE_EMPLOYEES);
                
                Log.d(TAG, "Successfully upgraded employees table to enforce unique phone numbers");
            } catch (Exception e) {
                Log.e(TAG, "Error upgrading employees table", e);
            }
        }
        
        if (oldVersion < 5) {
            // Upgrade tables table to add table_type column and make table name unique
            try {
                // Create a new temporary table with the new structure
                String CREATE_TABLES_TEMP_TABLE = "CREATE TABLE tables_temp" +
                    "(" +
                    KEY_TABLE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    KEY_TABLE_NAME + " TEXT UNIQUE," +
                    KEY_TABLE_CAPACITY + " INTEGER," +
                    KEY_TABLE_STATUS + " TEXT," +
                    KEY_TABLE_NOTE + " TEXT," +
                    KEY_TABLE_TYPE + " TEXT" +
                    ")";
                db.execSQL(CREATE_TABLES_TEMP_TABLE);
                
                // Copy data from old table to new table, setting default table type as "Thường"
                db.execSQL("INSERT OR IGNORE INTO tables_temp(id, name, capacity, status, note, table_type) SELECT id, name, capacity, status, note, 'Thường' FROM " + TABLE_TABLES);
                
                // Drop old table
                db.execSQL("DROP TABLE " + TABLE_TABLES);
                
                // Rename new table
                db.execSQL("ALTER TABLE tables_temp RENAME TO " + TABLE_TABLES);
                
                Log.d(TAG, "Successfully upgraded tables table to add table_type column and make table name unique");
            } catch (Exception e) {
                Log.e(TAG, "Error upgrading tables table", e);
            }
        }
    }

    // User table methods
    public long addUser(String fullname, String username, String password) {
        SQLiteDatabase db = getWritableDatabase();
        long userId = -1;

        db.beginTransaction();
        try {
            ContentValues values = new ContentValues();
            values.put(KEY_USER_FULLNAME, fullname);
            values.put(KEY_USER_USERNAME, username);
            values.put(KEY_USER_PASSWORD, password);

            userId = db.insertOrThrow(TABLE_USERS, null, values);
            db.setTransactionSuccessful();
        } catch (Exception e) {
            // Error in between database transaction
        } finally {
            db.endTransaction();
        }

        return userId;
    }

    public boolean checkUser(String username, String password) {
        SQLiteDatabase db = getReadableDatabase();
        String[] columns = {KEY_USER_ID};
        String selection = KEY_USER_USERNAME + " = ?" + " AND " + KEY_USER_PASSWORD + " = ?";
        String[] selectionArgs = {username, password};

        Cursor cursor = db.query(TABLE_USERS, columns, selection, selectionArgs, null, null, null);
        int count = cursor.getCount();
        cursor.close();
        
        return count > 0;
    }

    public boolean isUserExists(String username) {
        SQLiteDatabase db = getReadableDatabase();
        String[] columns = {KEY_USER_ID};
        String selection = KEY_USER_USERNAME + " = ?";
        String[] selectionArgs = {username};

        Cursor cursor = db.query(TABLE_USERS, columns, selection, selectionArgs, null, null, null);
        int count = cursor.getCount();
        cursor.close();
        
        return count > 0;
    }
    
    // Employee table methods
    public long addEmployee(Employee employee) {
        SQLiteDatabase db = getWritableDatabase();
        long employeeId = -1;

        db.beginTransaction();
        try {
            ContentValues values = new ContentValues();
            values.put(KEY_EMPLOYEE_NAME, employee.getName());
            values.put(KEY_EMPLOYEE_POSITION, employee.getPositionString());
            values.put(KEY_EMPLOYEE_PHONE, employee.getPhone());
            values.put(KEY_EMPLOYEE_EMAIL, employee.getEmail());
            values.put(KEY_EMPLOYEE_ADDRESS, employee.getAddress());
            values.put(KEY_EMPLOYEE_SALARY, employee.getSalary());
            values.put(KEY_EMPLOYEE_START_DATE, employee.getStartDate());

            employeeId = db.insertOrThrow(TABLE_EMPLOYEES, null, values);
            db.setTransactionSuccessful();
        } catch (Exception e) {
            // Kiểm tra xem lỗi có phải do trùng số điện thoại
            Log.e(TAG, "Error adding employee", e);
        } finally {
            db.endTransaction();
        }

        return employeeId;
    }
    
    public boolean updateEmployee(Employee employee) {
        SQLiteDatabase db = getWritableDatabase();
        int rowsAffected = 0;

        db.beginTransaction();
        try {
            ContentValues values = new ContentValues();
            values.put(KEY_EMPLOYEE_NAME, employee.getName());
            values.put(KEY_EMPLOYEE_POSITION, employee.getPositionString());
            values.put(KEY_EMPLOYEE_PHONE, employee.getPhone());
            values.put(KEY_EMPLOYEE_EMAIL, employee.getEmail());
            values.put(KEY_EMPLOYEE_ADDRESS, employee.getAddress());
            values.put(KEY_EMPLOYEE_SALARY, employee.getSalary());
            values.put(KEY_EMPLOYEE_START_DATE, employee.getStartDate());

            String selection = KEY_EMPLOYEE_ID + " = ?";
            String[] selectionArgs = {String.valueOf(employee.getId())};

            rowsAffected = db.update(TABLE_EMPLOYEES, values, selection, selectionArgs);
            db.setTransactionSuccessful();
        } catch (Exception e) {
            // Kiểm tra xem lỗi có phải do trùng số điện thoại
            Log.e(TAG, "Error updating employee", e);
        } finally {
            db.endTransaction();
        }

        return rowsAffected > 0;
    }
    
    public boolean deleteEmployee(long employeeId) {
        SQLiteDatabase db = getWritableDatabase();
        int rowsAffected = 0;

        db.beginTransaction();
        try {
            String selection = KEY_EMPLOYEE_ID + " = ?";
            String[] selectionArgs = {String.valueOf(employeeId)};

            rowsAffected = db.delete(TABLE_EMPLOYEES, selection, selectionArgs);
            db.setTransactionSuccessful();
        } catch (Exception e) {
            // Error in between database transaction
        } finally {
            db.endTransaction();
        }

        return rowsAffected > 0;
    }
    
    public List<Employee> getAllEmployees() {
        List<Employee> employees = new ArrayList<>();
        
        String selectQuery = "SELECT * FROM " + TABLE_EMPLOYEES;
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        
        try {
            if (cursor.moveToFirst()) {
                do {
                    Employee employee = new Employee();
                    employee.setId(cursor.getLong(cursor.getColumnIndexOrThrow(KEY_EMPLOYEE_ID)));
                    employee.setName(cursor.getString(cursor.getColumnIndexOrThrow(KEY_EMPLOYEE_NAME)));
                    employee.setPosition(cursor.getString(cursor.getColumnIndexOrThrow(KEY_EMPLOYEE_POSITION)));
                    employee.setPhone(cursor.getString(cursor.getColumnIndexOrThrow(KEY_EMPLOYEE_PHONE)));
                    employee.setEmail(cursor.getString(cursor.getColumnIndexOrThrow(KEY_EMPLOYEE_EMAIL)));
                    employee.setAddress(cursor.getString(cursor.getColumnIndexOrThrow(KEY_EMPLOYEE_ADDRESS)));
                    employee.setSalary(cursor.getDouble(cursor.getColumnIndexOrThrow(KEY_EMPLOYEE_SALARY)));
                    employee.setStartDate(cursor.getString(cursor.getColumnIndexOrThrow(KEY_EMPLOYEE_START_DATE)));
                    
                    employees.add(employee);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            // Error processing cursor
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
        
        return employees;
    }
    
    public Employee getEmployeeById(long id) {
        SQLiteDatabase db = getReadableDatabase();
        Employee employee = null;
        
        String selection = KEY_EMPLOYEE_ID + " = ?";
        String[] selectionArgs = {String.valueOf(id)};
        
        Cursor cursor = db.query(TABLE_EMPLOYEES, null, selection, selectionArgs, null, null, null);
        
        try {
            if (cursor.moveToFirst()) {
                employee = new Employee();
                employee.setId(cursor.getLong(cursor.getColumnIndexOrThrow(KEY_EMPLOYEE_ID)));
                employee.setName(cursor.getString(cursor.getColumnIndexOrThrow(KEY_EMPLOYEE_NAME)));
                employee.setPosition(cursor.getString(cursor.getColumnIndexOrThrow(KEY_EMPLOYEE_POSITION)));
                employee.setPhone(cursor.getString(cursor.getColumnIndexOrThrow(KEY_EMPLOYEE_PHONE)));
                employee.setEmail(cursor.getString(cursor.getColumnIndexOrThrow(KEY_EMPLOYEE_EMAIL)));
                employee.setAddress(cursor.getString(cursor.getColumnIndexOrThrow(KEY_EMPLOYEE_ADDRESS)));
                employee.setSalary(cursor.getDouble(cursor.getColumnIndexOrThrow(KEY_EMPLOYEE_SALARY)));
                employee.setStartDate(cursor.getString(cursor.getColumnIndexOrThrow(KEY_EMPLOYEE_START_DATE)));
            }
        } catch (Exception e) {
            // Error processing cursor
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
        
        return employee;
    }
    
    // Food table methods
    public long addFood(Food food) {
        SQLiteDatabase db = getWritableDatabase();
        long foodId = -1;

        db.beginTransaction();
        try {
            ContentValues values = new ContentValues();
            values.put(KEY_FOOD_NAME, food.getName());
            values.put(KEY_FOOD_CATEGORY, food.getCategoryString());
            values.put(KEY_FOOD_PRICE, food.getPrice());
            values.put(KEY_FOOD_DESCRIPTION, food.getDescription());
            values.put(KEY_FOOD_IMAGE_URL, food.getImageUrl());
            values.put(KEY_FOOD_AVAILABLE, food.isAvailable() ? 1 : 0);

            foodId = db.insertOrThrow(TABLE_FOODS, null, values);
            db.setTransactionSuccessful();
        } catch (Exception e) {
            // Error in between database transaction
        } finally {
            db.endTransaction();
        }

        return foodId;
    }
    
    public boolean updateFood(Food food) {
        SQLiteDatabase db = getWritableDatabase();
        int rowsAffected = 0;

        db.beginTransaction();
        try {
            ContentValues values = new ContentValues();
            values.put(KEY_FOOD_NAME, food.getName());
            values.put(KEY_FOOD_CATEGORY, food.getCategoryString());
            values.put(KEY_FOOD_PRICE, food.getPrice());
            values.put(KEY_FOOD_DESCRIPTION, food.getDescription());
            values.put(KEY_FOOD_IMAGE_URL, food.getImageUrl());
            values.put(KEY_FOOD_AVAILABLE, food.isAvailable() ? 1 : 0);

            String selection = KEY_FOOD_ID + " = ?";
            String[] selectionArgs = {String.valueOf(food.getId())};

            rowsAffected = db.update(TABLE_FOODS, values, selection, selectionArgs);
            db.setTransactionSuccessful();
        } catch (Exception e) {
            // Error in between database transaction
        } finally {
            db.endTransaction();
        }

        return rowsAffected > 0;
    }
    
    public boolean deleteFood(long foodId) {
        SQLiteDatabase db = getWritableDatabase();
        int rowsAffected = 0;

        db.beginTransaction();
        try {
            String selection = KEY_FOOD_ID + " = ?";
            String[] selectionArgs = {String.valueOf(foodId)};

            rowsAffected = db.delete(TABLE_FOODS, selection, selectionArgs);
            db.setTransactionSuccessful();
        } catch (Exception e) {
            // Error in between database transaction
        } finally {
            db.endTransaction();
        }

        return rowsAffected > 0;
    }
    
    public List<Food> getAllFoods() {
        List<Food> foods = new ArrayList<>();
        
        String selectQuery = "SELECT * FROM " + TABLE_FOODS;
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        
        try {
            if (cursor.moveToFirst()) {
                do {
                    Food food = new Food();
                    food.setId(cursor.getLong(cursor.getColumnIndexOrThrow(KEY_FOOD_ID)));
                    food.setName(cursor.getString(cursor.getColumnIndexOrThrow(KEY_FOOD_NAME)));
                    
                    // Parse categories from comma-separated string
                    String categoryStr = cursor.getString(cursor.getColumnIndexOrThrow(KEY_FOOD_CATEGORY));
                    food.setCategory(categoryStr); // This method now handles multiple categories
                    
                    food.setPrice(cursor.getDouble(cursor.getColumnIndexOrThrow(KEY_FOOD_PRICE)));
                    food.setDescription(cursor.getString(cursor.getColumnIndexOrThrow(KEY_FOOD_DESCRIPTION)));
                    food.setImageUrl(cursor.getString(cursor.getColumnIndexOrThrow(KEY_FOOD_IMAGE_URL)));
                    food.setAvailable(cursor.getInt(cursor.getColumnIndexOrThrow(KEY_FOOD_AVAILABLE)) == 1);
                    
                    foods.add(food);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            // Error processing cursor
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
        
        return foods;
    }
    
    public List<Food> getFoodsByCategory(String category) {
        List<Food> foods = new ArrayList<>();
        
        SQLiteDatabase db = getReadableDatabase();
        
        // Modified to find foods that contain the specified category
        String selection = KEY_FOOD_CATEGORY + " LIKE ?";
        String[] selectionArgs = {"%" + category + "%"};
        
        Cursor cursor = db.query(TABLE_FOODS, null, selection, selectionArgs, null, null, null);
        
        try {
            if (cursor.moveToFirst()) {
                do {
                    Food food = new Food();
                    food.setId(cursor.getLong(cursor.getColumnIndexOrThrow(KEY_FOOD_ID)));
                    food.setName(cursor.getString(cursor.getColumnIndexOrThrow(KEY_FOOD_NAME)));
                    
                    // Parse categories from comma-separated string
                    String categoryStr = cursor.getString(cursor.getColumnIndexOrThrow(KEY_FOOD_CATEGORY));
                    food.setCategory(categoryStr); // This method now handles multiple categories
                    
                    food.setPrice(cursor.getDouble(cursor.getColumnIndexOrThrow(KEY_FOOD_PRICE)));
                    food.setDescription(cursor.getString(cursor.getColumnIndexOrThrow(KEY_FOOD_DESCRIPTION)));
                    food.setImageUrl(cursor.getString(cursor.getColumnIndexOrThrow(KEY_FOOD_IMAGE_URL)));
                    food.setAvailable(cursor.getInt(cursor.getColumnIndexOrThrow(KEY_FOOD_AVAILABLE)) == 1);
                    
                    foods.add(food);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            // Error processing cursor
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
        
        return foods;
    }
    
    public Food getFoodById(long id) {
        SQLiteDatabase db = getReadableDatabase();
        Food food = null;
        
        String selection = KEY_FOOD_ID + " = ?";
        String[] selectionArgs = {String.valueOf(id)};
        
        Cursor cursor = db.query(TABLE_FOODS, null, selection, selectionArgs, null, null, null);
        
        try {
            if (cursor.moveToFirst()) {
                food = new Food();
                food.setId(cursor.getLong(cursor.getColumnIndexOrThrow(KEY_FOOD_ID)));
                food.setName(cursor.getString(cursor.getColumnIndexOrThrow(KEY_FOOD_NAME)));
                
                // Parse categories from comma-separated string
                String categoryStr = cursor.getString(cursor.getColumnIndexOrThrow(KEY_FOOD_CATEGORY));
                food.setCategory(categoryStr); // This method now handles multiple categories
                
                food.setPrice(cursor.getDouble(cursor.getColumnIndexOrThrow(KEY_FOOD_PRICE)));
                food.setDescription(cursor.getString(cursor.getColumnIndexOrThrow(KEY_FOOD_DESCRIPTION)));
                food.setImageUrl(cursor.getString(cursor.getColumnIndexOrThrow(KEY_FOOD_IMAGE_URL)));
                food.setAvailable(cursor.getInt(cursor.getColumnIndexOrThrow(KEY_FOOD_AVAILABLE)) == 1);
            }
        } catch (Exception e) {
            // Error processing cursor
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
        
        return food;
    }
    
    public List<String> getAllFoodCategories() {
        List<String> categories = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        
        Cursor cursor = db.query(true, TABLE_FOODS, new String[]{KEY_FOOD_CATEGORY}, null, null, KEY_FOOD_CATEGORY, null, null, null);
        
        try {
            if (cursor.moveToFirst()) {
                do {
                    String category = cursor.getString(cursor.getColumnIndexOrThrow(KEY_FOOD_CATEGORY));
                    categories.add(category);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            // Error handling
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
        
        return categories;
    }
    
    // Search foods by name or description
    public List<Food> searchFoods(String query) {
        List<Food> foods = new ArrayList<>();
        
        SQLiteDatabase db = getReadableDatabase();
        String searchQuery = "%" + query + "%";
        
        String selection = KEY_FOOD_NAME + " LIKE ? OR " +
                          KEY_FOOD_CATEGORY + " LIKE ? OR " +
                          KEY_FOOD_DESCRIPTION + " LIKE ?";
        
        String[] selectionArgs = {searchQuery, searchQuery, searchQuery};
        
        Cursor cursor = db.query(TABLE_FOODS, null, selection, selectionArgs, null, null, null);
        
        try {
            if (cursor.moveToFirst()) {
                do {
                    Food food = new Food();
                    food.setId(cursor.getLong(cursor.getColumnIndexOrThrow(KEY_FOOD_ID)));
                    food.setName(cursor.getString(cursor.getColumnIndexOrThrow(KEY_FOOD_NAME)));
                    
                    // Parse categories from comma-separated string
                    String categoryStr = cursor.getString(cursor.getColumnIndexOrThrow(KEY_FOOD_CATEGORY));
                    food.setCategory(categoryStr); // This method now handles multiple categories
                    
                    food.setPrice(cursor.getDouble(cursor.getColumnIndexOrThrow(KEY_FOOD_PRICE)));
                    food.setDescription(cursor.getString(cursor.getColumnIndexOrThrow(KEY_FOOD_DESCRIPTION)));
                    food.setImageUrl(cursor.getString(cursor.getColumnIndexOrThrow(KEY_FOOD_IMAGE_URL)));
                    food.setAvailable(cursor.getInt(cursor.getColumnIndexOrThrow(KEY_FOOD_AVAILABLE)) == 1);
                    
                    foods.add(food);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            // Error processing cursor
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
        
        return foods;
    }
    
    // Get foods sorted by criteria
    public List<Food> getFoodsSorted(String sortBy, boolean ascending) {
        List<Food> foods = new ArrayList<>();
        
        SQLiteDatabase db = getReadableDatabase();
        
        String orderBy;
        switch (sortBy) {
            case "name":
                orderBy = KEY_FOOD_NAME;
                break;
            case "price":
                orderBy = KEY_FOOD_PRICE;
                break;
            case "category":
                orderBy = KEY_FOOD_CATEGORY;
                break;
            default:
                orderBy = KEY_FOOD_ID;
        }
        
        if (!ascending) {
            orderBy += " DESC";
        }
        
        Cursor cursor = db.query(TABLE_FOODS, null, null, null, null, null, orderBy);
        
        try {
            if (cursor.moveToFirst()) {
                do {
                    Food food = new Food();
                    food.setId(cursor.getLong(cursor.getColumnIndexOrThrow(KEY_FOOD_ID)));
                    food.setName(cursor.getString(cursor.getColumnIndexOrThrow(KEY_FOOD_NAME)));
                    
                    // Parse categories from comma-separated string
                    String categoryStr = cursor.getString(cursor.getColumnIndexOrThrow(KEY_FOOD_CATEGORY));
                    food.setCategory(categoryStr); // This method now handles multiple categories
                    
                    food.setPrice(cursor.getDouble(cursor.getColumnIndexOrThrow(KEY_FOOD_PRICE)));
                    food.setDescription(cursor.getString(cursor.getColumnIndexOrThrow(KEY_FOOD_DESCRIPTION)));
                    food.setImageUrl(cursor.getString(cursor.getColumnIndexOrThrow(KEY_FOOD_IMAGE_URL)));
                    food.setAvailable(cursor.getInt(cursor.getColumnIndexOrThrow(KEY_FOOD_AVAILABLE)) == 1);
                    
                    foods.add(food);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            // Error processing cursor
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
        
        return foods;
    }
    
    // Get foods by category sorted by criteria
    public List<Food> getFoodsByCategorySorted(String category, String sortBy, boolean ascending) {
        List<Food> foods = new ArrayList<>();
        
        SQLiteDatabase db = getReadableDatabase();
        
        String orderBy;
        switch (sortBy) {
            case "name":
                orderBy = KEY_FOOD_NAME;
                break;
            case "price":
                orderBy = KEY_FOOD_PRICE;
                break;
            default:
                orderBy = KEY_FOOD_ID;
        }
        
        if (!ascending) {
            orderBy += " DESC";
        }
        
        // Modified to find foods that contain the specified category
        String selection = KEY_FOOD_CATEGORY + " LIKE ?";
        String[] selectionArgs = {"%" + category + "%"};
        
        Cursor cursor = db.query(TABLE_FOODS, null, selection, selectionArgs, null, null, orderBy);
        
        try {
            if (cursor.moveToFirst()) {
                do {
                    Food food = new Food();
                    food.setId(cursor.getLong(cursor.getColumnIndexOrThrow(KEY_FOOD_ID)));
                    food.setName(cursor.getString(cursor.getColumnIndexOrThrow(KEY_FOOD_NAME)));
                    
                    // Parse categories from comma-separated string
                    String categoryStr = cursor.getString(cursor.getColumnIndexOrThrow(KEY_FOOD_CATEGORY));
                    food.setCategory(categoryStr); // This method now handles multiple categories
                    
                    food.setPrice(cursor.getDouble(cursor.getColumnIndexOrThrow(KEY_FOOD_PRICE)));
                    food.setDescription(cursor.getString(cursor.getColumnIndexOrThrow(KEY_FOOD_DESCRIPTION)));
                    food.setImageUrl(cursor.getString(cursor.getColumnIndexOrThrow(KEY_FOOD_IMAGE_URL)));
                    food.setAvailable(cursor.getInt(cursor.getColumnIndexOrThrow(KEY_FOOD_AVAILABLE)) == 1);
                    
                    foods.add(food);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            // Error processing cursor
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
        
        return foods;
    }

        // Table table methods    public long addTable(Table table) {        SQLiteDatabase db = getWritableDatabase();        long tableId = -1;        db.beginTransaction();        try {            ContentValues values = new ContentValues();            values.put(KEY_TABLE_NAME, table.getName());            values.put(KEY_TABLE_CAPACITY, table.getCapacity());            values.put(KEY_TABLE_STATUS, table.getStatus());            values.put(KEY_TABLE_NOTE, table.getNote());            values.put(KEY_TABLE_TYPE, table.getTableType());            tableId = db.insertOrThrow(TABLE_TABLES, null, values);            db.setTransactionSuccessful();        } catch (Exception e) {            // Error in between database transaction        } finally {            db.endTransaction();        }        return tableId;    }
    
    public boolean updateTable(Table table) {
        SQLiteDatabase db = getWritableDatabase();
        int rowsAffected = 0;

        db.beginTransaction();
        try {
            ContentValues values = new ContentValues();
            values.put(KEY_TABLE_NAME, table.getName());
            values.put(KEY_TABLE_CAPACITY, table.getCapacity());
            values.put(KEY_TABLE_STATUS, table.getStatus());
            values.put(KEY_TABLE_NOTE, table.getNote());
            values.put(KEY_TABLE_TYPE, table.getTableType());

            String selection = KEY_TABLE_ID + " = ?";
            String[] selectionArgs = {String.valueOf(table.getId())};

            rowsAffected = db.update(TABLE_TABLES, values, selection, selectionArgs);
            db.setTransactionSuccessful();
        } catch (Exception e) {
            // Error in between database transaction
        } finally {
            db.endTransaction();
        }

        return rowsAffected > 0;
    }
    
    public boolean updateTableStatus(long tableId, String status) {
        SQLiteDatabase db = getWritableDatabase();
        int rowsAffected = 0;

        db.beginTransaction();
        try {
            ContentValues values = new ContentValues();
            values.put(KEY_TABLE_STATUS, status);

            String selection = KEY_TABLE_ID + " = ?";
            String[] selectionArgs = {String.valueOf(tableId)};

            rowsAffected = db.update(TABLE_TABLES, values, selection, selectionArgs);
            db.setTransactionSuccessful();
        } catch (Exception e) {
            // Error in between database transaction
        } finally {
            db.endTransaction();
        }

        return rowsAffected > 0;
    }
    
    public boolean deleteTable(long tableId) {
        SQLiteDatabase db = getWritableDatabase();
        int rowsAffected = 0;

        db.beginTransaction();
        try {
            String selection = KEY_TABLE_ID + " = ?";
            String[] selectionArgs = {String.valueOf(tableId)};

            rowsAffected = db.delete(TABLE_TABLES, selection, selectionArgs);
            db.setTransactionSuccessful();
        } catch (Exception e) {
            // Error in between database transaction
        } finally {
            db.endTransaction();
        }

        return rowsAffected > 0;
    }
    
    public List<Table> getAllTables() {
        List<Table> tables = new ArrayList<>();
        
        String selectQuery = "SELECT * FROM " + TABLE_TABLES;
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        
        try {
            if (cursor.moveToFirst()) {
                do {
                    Table table = new Table();
                    table.setId(cursor.getLong(cursor.getColumnIndexOrThrow(KEY_TABLE_ID)));
                    table.setName(cursor.getString(cursor.getColumnIndexOrThrow(KEY_TABLE_NAME)));
                    table.setCapacity(cursor.getInt(cursor.getColumnIndexOrThrow(KEY_TABLE_CAPACITY)));
                    table.setStatus(cursor.getString(cursor.getColumnIndexOrThrow(KEY_TABLE_STATUS)));
                    table.setNote(cursor.getString(cursor.getColumnIndexOrThrow(KEY_TABLE_NOTE)));
                    
                    // Get tableType, default to "Thường" if not present
                    try {
                        table.setTableType(cursor.getString(cursor.getColumnIndexOrThrow(KEY_TABLE_TYPE)));
                    } catch (Exception e) {
                        table.setTableType("Thường");
                    }
                    
                    tables.add(table);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            // Error processing cursor
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
        
        return tables;
    }
    
    public List<Table> getTablesByStatus(String status) {
        List<Table> tables = new ArrayList<>();
        
        SQLiteDatabase db = getReadableDatabase();
        String selection = KEY_TABLE_STATUS + " = ?";
        String[] selectionArgs = {status};
        
        Cursor cursor = db.query(TABLE_TABLES, null, selection, selectionArgs, null, null, KEY_TABLE_NAME);
        
        try {
            if (cursor.moveToFirst()) {
                do {
                    Table table = new Table();
                    table.setId(cursor.getLong(cursor.getColumnIndexOrThrow(KEY_TABLE_ID)));
                    table.setName(cursor.getString(cursor.getColumnIndexOrThrow(KEY_TABLE_NAME)));
                    table.setCapacity(cursor.getInt(cursor.getColumnIndexOrThrow(KEY_TABLE_CAPACITY)));
                    table.setStatus(cursor.getString(cursor.getColumnIndexOrThrow(KEY_TABLE_STATUS)));
                    table.setNote(cursor.getString(cursor.getColumnIndexOrThrow(KEY_TABLE_NOTE)));
                    
                    // Get tableType, default to "Thường" if not present
                    try {
                        table.setTableType(cursor.getString(cursor.getColumnIndexOrThrow(KEY_TABLE_TYPE)));
                    } catch (Exception e) {
                        table.setTableType("Thường");
                    }
                    
                    tables.add(table);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            // Error processing cursor
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
        
        return tables;
    }
    
    public Table getTableById(long id) {
        SQLiteDatabase db = getReadableDatabase();
        Table table = null;
        
        String selection = KEY_TABLE_ID + " = ?";
        String[] selectionArgs = {String.valueOf(id)};
        
        Cursor cursor = db.query(TABLE_TABLES, null, selection, selectionArgs, null, null, null);
        
        try {
            if (cursor.moveToFirst()) {
                table = new Table();
                table.setId(cursor.getLong(cursor.getColumnIndexOrThrow(KEY_TABLE_ID)));
                table.setName(cursor.getString(cursor.getColumnIndexOrThrow(KEY_TABLE_NAME)));
                table.setCapacity(cursor.getInt(cursor.getColumnIndexOrThrow(KEY_TABLE_CAPACITY)));
                table.setStatus(cursor.getString(cursor.getColumnIndexOrThrow(KEY_TABLE_STATUS)));
                table.setNote(cursor.getString(cursor.getColumnIndexOrThrow(KEY_TABLE_NOTE)));
                
                // Get tableType, default to "Thường" if not present
                try {
                    table.setTableType(cursor.getString(cursor.getColumnIndexOrThrow(KEY_TABLE_TYPE)));
                } catch (Exception e) {
                    table.setTableType("Thường");
                }
            }
        } catch (Exception e) {
            // Error processing cursor
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
        
        return table;
    }

    // Daily Menu table methods
    public long addDailyMenuItem(DailyMenu dailyMenu) {
        SQLiteDatabase db = getWritableDatabase();
        long menuId = -1;

        // Kiểm tra xem món ăn đã tồn tại trong menu của ngày này chưa
        if (isFoodInDailyMenu(dailyMenu.getFoodId(), dailyMenu.getDate())) {
            // Món ăn đã tồn tại trong menu, không thêm lại
            return -1;
        }

        db.beginTransaction();
        try {
            ContentValues values = new ContentValues();
            values.put(KEY_DAILY_MENU_DATE, dailyMenu.getDate());
            values.put(KEY_DAILY_MENU_FOOD_ID, dailyMenu.getFoodId());
            values.put(KEY_DAILY_MENU_FEATURED, dailyMenu.isFeatured() ? 1 : 0);
            values.put(KEY_DAILY_MENU_QUANTITY, dailyMenu.getQuantity());

            menuId = db.insertOrThrow(TABLE_DAILY_MENU, null, values);
            db.setTransactionSuccessful();
        } catch (Exception e) {
            // Error in between database transaction
            Log.e(TAG, "Error adding daily menu item", e);
        } finally {
            db.endTransaction();
        }

        return menuId;
    }
    
    public boolean updateDailyMenuItem(DailyMenu dailyMenu) {
        SQLiteDatabase db = getWritableDatabase();
        int rowsAffected = 0;

        db.beginTransaction();
        try {
            ContentValues values = new ContentValues();
            values.put(KEY_DAILY_MENU_DATE, dailyMenu.getDate());
            values.put(KEY_DAILY_MENU_FOOD_ID, dailyMenu.getFoodId());
            values.put(KEY_DAILY_MENU_FEATURED, dailyMenu.isFeatured() ? 1 : 0);
            values.put(KEY_DAILY_MENU_QUANTITY, dailyMenu.getQuantity());

            String selection = KEY_DAILY_MENU_ID + " = ?";
            String[] selectionArgs = {String.valueOf(dailyMenu.getId())};

            rowsAffected = db.update(TABLE_DAILY_MENU, values, selection, selectionArgs);
            db.setTransactionSuccessful();
        } catch (Exception e) {
            // Error in between database transaction
        } finally {
            db.endTransaction();
        }

        return rowsAffected > 0;
    }
    
    public boolean deleteDailyMenuItem(long menuId) {
        SQLiteDatabase db = getWritableDatabase();
        int rowsAffected = 0;

        db.beginTransaction();
        try {
            String selection = KEY_DAILY_MENU_ID + " = ?";
            String[] selectionArgs = {String.valueOf(menuId)};

            rowsAffected = db.delete(TABLE_DAILY_MENU, selection, selectionArgs);
            db.setTransactionSuccessful();
        } catch (Exception e) {
            // Error in between database transaction
        } finally {
            db.endTransaction();
        }

        return rowsAffected > 0;
    }
    
    public boolean deleteAllDailyMenuItemsByDate(String date) {
        SQLiteDatabase db = getWritableDatabase();
        int rowsAffected = 0;

        db.beginTransaction();
        try {
            String selection = KEY_DAILY_MENU_DATE + " = ?";
            String[] selectionArgs = {date};

            rowsAffected = db.delete(TABLE_DAILY_MENU, selection, selectionArgs);
            db.setTransactionSuccessful();
        } catch (Exception e) {
            // Error in between database transaction
        } finally {
            db.endTransaction();
        }

        return rowsAffected > 0;
    }
    
    public List<DailyMenu> getDailyMenuByDate(String date) {
        List<DailyMenu> menuItems = new ArrayList<>();
        
        SQLiteDatabase db = getReadableDatabase();
        
        // Query to join daily_menu and foods tables to get food details
        String query = "SELECT dm.*, f." + KEY_FOOD_NAME + ", f." + KEY_FOOD_CATEGORY + 
                ", f." + KEY_FOOD_PRICE + ", f." + KEY_FOOD_IMAGE_URL +
                " FROM " + TABLE_DAILY_MENU + " dm" +
                " INNER JOIN " + TABLE_FOODS + " f ON dm." + KEY_DAILY_MENU_FOOD_ID + " = f." + KEY_FOOD_ID +
                " WHERE dm." + KEY_DAILY_MENU_DATE + " = ?" +
                " ORDER BY dm." + KEY_DAILY_MENU_FEATURED + " DESC, f." + KEY_FOOD_CATEGORY;
        
        String[] selectionArgs = {date};
        
        Cursor cursor = db.rawQuery(query, selectionArgs);
        
        try {
            if (cursor.moveToFirst()) {
                do {
                    DailyMenu dailyMenu = new DailyMenu();
                    dailyMenu.setId(cursor.getLong(cursor.getColumnIndexOrThrow(KEY_DAILY_MENU_ID)));
                    dailyMenu.setDate(cursor.getString(cursor.getColumnIndexOrThrow(KEY_DAILY_MENU_DATE)));
                    dailyMenu.setFoodId(cursor.getLong(cursor.getColumnIndexOrThrow(KEY_DAILY_MENU_FOOD_ID)));
                    dailyMenu.setFeatured(cursor.getInt(cursor.getColumnIndexOrThrow(KEY_DAILY_MENU_FEATURED)) == 1);
                    dailyMenu.setQuantity(cursor.getInt(cursor.getColumnIndexOrThrow(KEY_DAILY_MENU_QUANTITY)));
                    
                    // Set food details for display
                    dailyMenu.setFoodName(cursor.getString(cursor.getColumnIndexOrThrow(KEY_FOOD_NAME)));
                    dailyMenu.setFoodCategory(cursor.getString(cursor.getColumnIndexOrThrow(KEY_FOOD_CATEGORY)));
                    dailyMenu.setFoodPrice(cursor.getDouble(cursor.getColumnIndexOrThrow(KEY_FOOD_PRICE)));
                    dailyMenu.setFoodImageUrl(cursor.getString(cursor.getColumnIndexOrThrow(KEY_FOOD_IMAGE_URL)));
                    
                    menuItems.add(dailyMenu);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            // Error processing cursor
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
        
        return menuItems;
    }
    
    public List<String> getAvailableDailyMenuDates() {
        List<String> dates = new ArrayList<>();
        
        SQLiteDatabase db = getReadableDatabase();
        String query = "SELECT DISTINCT " + KEY_DAILY_MENU_DATE + 
                       " FROM " + TABLE_DAILY_MENU + 
                       " ORDER BY " + KEY_DAILY_MENU_DATE + " DESC";
        
        Cursor cursor = db.rawQuery(query, null);
        
        try {
            if (cursor.moveToFirst()) {
                do {
                    dates.add(cursor.getString(cursor.getColumnIndexOrThrow(KEY_DAILY_MENU_DATE)));
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            // Error processing cursor
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
        
        return dates;
    }
    
    public DailyMenu getDailyMenuItemById(long id) {
        SQLiteDatabase db = getReadableDatabase();
        DailyMenu dailyMenu = null;
        
        // Query to join daily_menu and foods tables to get food details
        String query = "SELECT dm.*, f." + KEY_FOOD_NAME + ", f." + KEY_FOOD_CATEGORY + 
                ", f." + KEY_FOOD_PRICE + ", f." + KEY_FOOD_IMAGE_URL +
                " FROM " + TABLE_DAILY_MENU + " dm" +
                " INNER JOIN " + TABLE_FOODS + " f ON dm." + KEY_DAILY_MENU_FOOD_ID + " = f." + KEY_FOOD_ID +
                " WHERE dm." + KEY_DAILY_MENU_ID + " = ?";
        
        String[] selectionArgs = {String.valueOf(id)};
        
        Cursor cursor = db.rawQuery(query, selectionArgs);
        
        try {
            if (cursor.moveToFirst()) {
                dailyMenu = new DailyMenu();
                dailyMenu.setId(cursor.getLong(cursor.getColumnIndexOrThrow(KEY_DAILY_MENU_ID)));
                dailyMenu.setDate(cursor.getString(cursor.getColumnIndexOrThrow(KEY_DAILY_MENU_DATE)));
                dailyMenu.setFoodId(cursor.getLong(cursor.getColumnIndexOrThrow(KEY_DAILY_MENU_FOOD_ID)));
                dailyMenu.setFeatured(cursor.getInt(cursor.getColumnIndexOrThrow(KEY_DAILY_MENU_FEATURED)) == 1);
                dailyMenu.setQuantity(cursor.getInt(cursor.getColumnIndexOrThrow(KEY_DAILY_MENU_QUANTITY)));
                
                // Set food details for display
                dailyMenu.setFoodName(cursor.getString(cursor.getColumnIndexOrThrow(KEY_FOOD_NAME)));
                dailyMenu.setFoodCategory(cursor.getString(cursor.getColumnIndexOrThrow(KEY_FOOD_CATEGORY)));
                dailyMenu.setFoodPrice(cursor.getDouble(cursor.getColumnIndexOrThrow(KEY_FOOD_PRICE)));
                dailyMenu.setFoodImageUrl(cursor.getString(cursor.getColumnIndexOrThrow(KEY_FOOD_IMAGE_URL)));
            }
        } catch (Exception e) {
            // Error processing cursor
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
        
        return dailyMenu;
    }

    // Statistics methods
    
    // Thống kê số lượng món ăn theo danh mục
    public List<StatisticItem> getFoodCountByCategory() {
        List<StatisticItem> statistics = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        
        String query = "SELECT " + KEY_FOOD_CATEGORY + " as name, COUNT(*) as value" +
                " FROM " + TABLE_FOODS +
                " GROUP BY " + KEY_FOOD_CATEGORY +
                " ORDER BY value DESC";
        
        Cursor cursor = db.rawQuery(query, null);
        
        try {
            if (cursor.moveToFirst()) {
                do {
                    String category = cursor.getString(cursor.getColumnIndexOrThrow("name"));
                    int count = cursor.getInt(cursor.getColumnIndexOrThrow("value"));
                    statistics.add(new StatisticItem(category, count));
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            // Error handling
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
        
        return statistics;
    }
    
    // Thống kê doanh thu theo ngày
    public List<StatisticItem> getRevenueByDate(String startDate, String endDate) {
        List<StatisticItem> statistics = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        
        try {
            Log.d(TAG, "Querying revenue from " + startDate + " to " + endDate);
            
            // Truy vấn dữ liệu từ bảng orders
            String query = "SELECT strftime('%Y-%m-%d', datetime(" + KEY_ORDER_DATE + "/1000, 'unixepoch', 'localtime')) as date, " +
                    "SUM(" + KEY_ORDER_TOTAL_AMOUNT + ") as total " +
                    "FROM " + TABLE_ORDERS + " " +
                    "WHERE " + KEY_ORDER_STATUS + " = 'Đã thanh toán' " +
                    "AND date(datetime(" + KEY_ORDER_DATE + "/1000, 'unixepoch', 'localtime')) BETWEEN date(?) AND date(?) " +
                    "GROUP BY date " +
                    "ORDER BY date";
            
            Cursor cursor = db.rawQuery(query, new String[]{startDate, endDate});
            
            if (cursor.moveToFirst()) {
                do {
                    String date = cursor.getString(cursor.getColumnIndexOrThrow("date"));
                    double revenue = cursor.getDouble(cursor.getColumnIndexOrThrow("total"));
                    Log.d(TAG, "Revenue data: " + date + " = " + revenue);
                    statistics.add(new StatisticItem(date, revenue));
                } while (cursor.moveToNext());
            } else {
                Log.d(TAG, "No revenue data found with 'Đã thanh toán' status, trying other status values");
                
                cursor.close();
                
                // Try with any status value
                query = "SELECT strftime('%Y-%m-%d', datetime(" + KEY_ORDER_DATE + "/1000, 'unixepoch', 'localtime')) as date, " +
                        "SUM(" + KEY_ORDER_TOTAL_AMOUNT + ") as total " +
                        "FROM " + TABLE_ORDERS + " " +
                        "WHERE date(datetime(" + KEY_ORDER_DATE + "/1000, 'unixepoch', 'localtime')) BETWEEN date(?) AND date(?) " +
                        "GROUP BY date " +
                        "ORDER BY date";
                
                cursor = db.rawQuery(query, new String[]{startDate, endDate});
                
                if (cursor.moveToFirst()) {
                    do {
                        String date = cursor.getString(cursor.getColumnIndexOrThrow("date"));
                        double revenue = cursor.getDouble(cursor.getColumnIndexOrThrow("total"));
                        Log.d(TAG, "Revenue data (all statuses): " + date + " = " + revenue);
                        statistics.add(new StatisticItem(date, revenue));
                    } while (cursor.moveToNext());
                } else {
                    Log.d(TAG, "No revenue data found for the selected period");
                }
            }
            cursor.close();
        } catch (Exception e) {
            Log.e(TAG, "Error getting revenue data", e);
        }
        
        return statistics;
    }
    
    // Thống kê món ăn phổ biến nhất (top N món ăn)
    public List<StatisticItem> getTopFoods(int limit) {
        List<StatisticItem> statistics = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        
        try {
            Log.d(TAG, "Querying top " + limit + " foods");
            
            // Truy vấn từ bảng order_items và foods
            String query = "SELECT f." + KEY_FOOD_NAME + " as food_name, " +
                    "SUM(oi." + KEY_ORDER_ITEM_QUANTITY + ") as total_qty " +
                    "FROM " + TABLE_ORDER_ITEMS + " oi " +
                    "JOIN " + TABLE_FOODS + " f ON oi." + KEY_ORDER_ITEM_FOOD_ID + " = f." + KEY_FOOD_ID + " " +
                    "JOIN " + TABLE_ORDERS + " o ON oi." + KEY_ORDER_ITEM_ORDER_ID + " = o." + KEY_ORDER_ID + " " +
                    "WHERE o." + KEY_ORDER_STATUS + " = 'Đã thanh toán' " +
                    "GROUP BY oi." + KEY_ORDER_ITEM_FOOD_ID + " " +
                    "ORDER BY total_qty DESC " +
                    "LIMIT ?";
            
            Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(limit)});
            
            if (cursor.moveToFirst()) {
                do {
                    String foodName = cursor.getString(cursor.getColumnIndexOrThrow("food_name"));
                    int quantity = cursor.getInt(cursor.getColumnIndexOrThrow("total_qty"));
                    Log.d(TAG, "Top food: " + foodName + " = " + quantity);
                    statistics.add(new StatisticItem(foodName, quantity));
                } while (cursor.moveToNext());
            } else {
                Log.d(TAG, "No top food data found in paid orders, checking all orders");
                // Fallback: look at all orders regardless of payment status if no paid orders found
                query = "SELECT f." + KEY_FOOD_NAME + " as food_name, " +
                        "SUM(oi." + KEY_ORDER_ITEM_QUANTITY + ") as total_qty " +
                        "FROM " + TABLE_ORDER_ITEMS + " oi " +
                        "JOIN " + TABLE_FOODS + " f ON oi." + KEY_ORDER_ITEM_FOOD_ID + " = f." + KEY_FOOD_ID + " " +
                        "GROUP BY oi." + KEY_ORDER_ITEM_FOOD_ID + " " +
                        "ORDER BY total_qty DESC " +
                        "LIMIT ?";
                
                cursor.close();
                cursor = db.rawQuery(query, new String[]{String.valueOf(limit)});
                
                if (cursor.moveToFirst()) {
                    do {
                        String foodName = cursor.getString(cursor.getColumnIndexOrThrow("food_name"));
                        int quantity = cursor.getInt(cursor.getColumnIndexOrThrow("total_qty"));
                        Log.d(TAG, "Top food (all orders): " + foodName + " = " + quantity);
                        statistics.add(new StatisticItem(foodName, quantity));
                    } while (cursor.moveToNext());
                } else {
                    Log.d(TAG, "No food order data found at all");
                }
            }
            cursor.close();
            
            // If still no data, check order items without joining to foods
            // This catches cases where food IDs might not match food table but still exist in orders
            if (statistics.isEmpty()) {
                Log.d(TAG, "Checking raw order items data");
                query = "SELECT " + KEY_ORDER_ITEM_FOOD_ID + " as food_id, " +
                        "SUM(" + KEY_ORDER_ITEM_QUANTITY + ") as total_qty " +
                        "FROM " + TABLE_ORDER_ITEMS + " " +
                        "GROUP BY " + KEY_ORDER_ITEM_FOOD_ID + " " +
                        "ORDER BY total_qty DESC " +
                        "LIMIT ?";
                
                cursor = db.rawQuery(query, new String[]{String.valueOf(limit)});
                
                if (cursor.moveToFirst()) {
                    do {
                        long foodId = cursor.getLong(cursor.getColumnIndexOrThrow("food_id"));
                        int quantity = cursor.getInt(cursor.getColumnIndexOrThrow("total_qty"));
                        
                        // Get the food name from the food ID
                        Food food = getFoodById(foodId);
                        String foodName = (food != null) ? food.getName() : "Món #" + foodId;
                        
                        Log.d(TAG, "Top food (by ID): " + foodName + " = " + quantity);
                        statistics.add(new StatisticItem(foodName, quantity));
                    } while (cursor.moveToNext());
                }
                cursor.close();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting top foods data", e);
        }
        
        return statistics;
    }
    
    // Thống kê số lượng bàn theo trạng thái
    public List<StatisticItem> getTableCountByStatus() {
        List<StatisticItem> statistics = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        
        Log.d(TAG, "Querying table count by status");
        
        try {
            String query = "SELECT " + KEY_TABLE_STATUS + " as status, COUNT(*) as count" +
                    " FROM " + TABLE_TABLES +
                    " GROUP BY " + KEY_TABLE_STATUS +
                    " ORDER BY count DESC";
            
            Cursor cursor = db.rawQuery(query, null);
            
            if (cursor.moveToFirst()) {
                do {
                    String status = cursor.getString(cursor.getColumnIndexOrThrow("status"));
                    int count = cursor.getInt(cursor.getColumnIndexOrThrow("count"));
                    Log.d(TAG, "Table status: " + status + " = " + count);
                    statistics.add(new StatisticItem(status, count));
                } while (cursor.moveToNext());
            } else {
                Log.d(TAG, "No table status data found");
            }
            
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting table status data", e);
        }
        
        return statistics;
    }
    
    // Thống kê số lượng món ăn trong menu theo ngày
    public List<StatisticItem> getDailyMenuCountByDate(String startDate, String endDate) {
        List<StatisticItem> statistics = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        
        String query = "SELECT " + KEY_DAILY_MENU_DATE + " as name, COUNT(*) as value" +
                " FROM " + TABLE_DAILY_MENU +
                " WHERE " + KEY_DAILY_MENU_DATE + " BETWEEN ? AND ?" +
                " GROUP BY " + KEY_DAILY_MENU_DATE +
                " ORDER BY " + KEY_DAILY_MENU_DATE;
        
        Cursor cursor = db.rawQuery(query, new String[]{startDate, endDate});
        
        try {
            if (cursor.moveToFirst()) {
                do {
                    String date = cursor.getString(cursor.getColumnIndexOrThrow("name"));
                    int count = cursor.getInt(cursor.getColumnIndexOrThrow("value"));
                    statistics.add(new StatisticItem(date, count));
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            // Error handling
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
        
        return statistics;
    }

    public List<Food> getMenuItemsForToday() {
        SQLiteDatabase db = getReadableDatabase();
        List<Food> menuItems = new ArrayList<>();
        
        // Get current date
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String today = dateFormat.format(new Date());
        
        Log.d(TAG, "Getting menu items for date: " + today);
        
        try {
            String query = "SELECT f.* FROM " + TABLE_FOODS + " f " +
                          "INNER JOIN " + TABLE_DAILY_MENU + " dm ON f." + KEY_FOOD_ID + " = dm." + KEY_DAILY_MENU_FOOD_ID + " " +
                          "WHERE dm." + KEY_DAILY_MENU_DATE + " = ? AND f." + KEY_FOOD_AVAILABLE + " = 1";
            
            Cursor cursor = db.rawQuery(query, new String[]{today});
            
            if (cursor.moveToFirst()) {
                do {
                    Food food = new Food(
                        cursor.getLong(cursor.getColumnIndexOrThrow(KEY_FOOD_ID)),
                        cursor.getString(cursor.getColumnIndexOrThrow(KEY_FOOD_NAME)),
                        cursor.getString(cursor.getColumnIndexOrThrow(KEY_FOOD_CATEGORY)),
                        cursor.getDouble(cursor.getColumnIndexOrThrow(KEY_FOOD_PRICE)),
                        cursor.getString(cursor.getColumnIndexOrThrow(KEY_FOOD_DESCRIPTION)),
                        cursor.getString(cursor.getColumnIndexOrThrow(KEY_FOOD_IMAGE_URL)),
                        cursor.getInt(cursor.getColumnIndexOrThrow(KEY_FOOD_AVAILABLE)) == 1
                    );
                    menuItems.add(food);
                    Log.d(TAG, "Added menu item: " + food.getName());
                } while (cursor.moveToNext());
            } else {
                Log.d(TAG, "No daily menu items found for today");
                // No fallback to other dates or all foods - empty menu for today
            }
            
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting menu items for today", e);
        }
        
        Log.d(TAG, "Returning " + menuItems.size() + " menu items");
        return menuItems;
    }

    // New method to get menu items for a specific date
    public List<Food> getMenuItemsForDate(String date) {
        SQLiteDatabase db = getReadableDatabase();
        List<Food> menuItems = new ArrayList<>();
        
        Log.d(TAG, "Getting menu items for date: " + date);
        
        try {
            String query = "SELECT f.* FROM " + TABLE_FOODS + " f " +
                          "INNER JOIN " + TABLE_DAILY_MENU + " dm ON f." + KEY_FOOD_ID + " = dm." + KEY_DAILY_MENU_FOOD_ID + " " +
                          "WHERE dm." + KEY_DAILY_MENU_DATE + " = ? AND f." + KEY_FOOD_AVAILABLE + " = 1";
            
            Cursor cursor = db.rawQuery(query, new String[]{date});
            
            if (cursor.moveToFirst()) {
                do {
                    Food food = new Food(
                        cursor.getLong(cursor.getColumnIndexOrThrow(KEY_FOOD_ID)),
                        cursor.getString(cursor.getColumnIndexOrThrow(KEY_FOOD_NAME)),
                        cursor.getString(cursor.getColumnIndexOrThrow(KEY_FOOD_CATEGORY)),
                        cursor.getDouble(cursor.getColumnIndexOrThrow(KEY_FOOD_PRICE)),
                        cursor.getString(cursor.getColumnIndexOrThrow(KEY_FOOD_DESCRIPTION)),
                        cursor.getString(cursor.getColumnIndexOrThrow(KEY_FOOD_IMAGE_URL)),
                        cursor.getInt(cursor.getColumnIndexOrThrow(KEY_FOOD_AVAILABLE)) == 1
                    );
                    menuItems.add(food);
                    Log.d(TAG, "Added menu item: " + food.getName());
                } while (cursor.moveToNext());
            }
            
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting menu items for date: " + date, e);
        }
        
        Log.d(TAG, "Returning " + menuItems.size() + " menu items for date: " + date);
        return menuItems;
    }

    public List<Food> getAllAvailableFoods() {
        List<Food> availableFoods = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        
        String query = "SELECT * FROM " + TABLE_FOODS + " WHERE " + KEY_FOOD_AVAILABLE + " = 1";
        Cursor cursor = db.rawQuery(query, null);
        
        try {
            if (cursor.moveToFirst()) {
                do {
                    Food food = new Food(
                        cursor.getLong(cursor.getColumnIndexOrThrow(KEY_FOOD_ID)),
                        cursor.getString(cursor.getColumnIndexOrThrow(KEY_FOOD_NAME)),
                        cursor.getString(cursor.getColumnIndexOrThrow(KEY_FOOD_CATEGORY)),
                        cursor.getDouble(cursor.getColumnIndexOrThrow(KEY_FOOD_PRICE)),
                        cursor.getString(cursor.getColumnIndexOrThrow(KEY_FOOD_DESCRIPTION)),
                        cursor.getString(cursor.getColumnIndexOrThrow(KEY_FOOD_IMAGE_URL)),
                        true
                    );
                    availableFoods.add(food);
                } while (cursor.moveToNext());
            }
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
        
        return availableFoods;
    }

    public long addOrder(Order order) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        
        values.put(KEY_ORDER_TABLE_ID, order.getTableId());
        values.put(KEY_ORDER_DATE, order.getOrderDate().getTime());
        values.put(KEY_ORDER_STATUS, "Completed"); // Default status
        values.put(KEY_ORDER_TOTAL_AMOUNT, order.getTotalAmount());
        
        long orderId = -1;
        try {
            orderId = db.insertOrThrow(TABLE_ORDERS, null, values);
            
            if (orderId != -1 && order.getOrderItems() != null) {
                for (OrderItem item : order.getOrderItems()) {
                    ContentValues itemValues = new ContentValues();
                    itemValues.put(KEY_ORDER_ITEM_ORDER_ID, orderId);
                    
                    long foodId = item.getFoodId();
                    itemValues.put(KEY_ORDER_ITEM_FOOD_ID, foodId);
                    itemValues.put(KEY_ORDER_ITEM_QUANTITY, item.getQuantity());
                    itemValues.put(KEY_ORDER_ITEM_PRICE, item.getPrice());
                    
                    db.insertOrThrow(TABLE_ORDER_ITEMS, null, itemValues);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error adding order", e);
        }
        
        return orderId;
    }

    /**
     * Save order with "Chưa thanh toán" status for TableDetailActivity
     */
    public long saveOrder(Order order) {
        SQLiteDatabase db = this.getWritableDatabase();
        long orderId = -1;
        
        Log.d(TAG, "Saving new order for table ID: " + order.getTableId());
        
        db.beginTransaction();
        try {
            ContentValues values = new ContentValues();
            values.put(KEY_ORDER_TABLE_ID, order.getTableId());
            values.put(KEY_ORDER_DATE, order.getOrderDate().getTime());
            values.put(KEY_ORDER_TOTAL_AMOUNT, order.getTotalAmount());
            values.put(KEY_ORDER_STATUS, "Chưa thanh toán"); // Setting initial status
            
            orderId = db.insert(TABLE_ORDERS, null, values);
            Log.d(TAG, "Inserted order with ID: " + orderId);
            
            if (orderId > 0 && order.getOrderItems() != null) {
                // Save order items
                for (OrderItem item : order.getOrderItems()) {
                    ContentValues itemValues = new ContentValues();
                    itemValues.put(KEY_ORDER_ITEM_ORDER_ID, orderId);
                    itemValues.put(KEY_ORDER_ITEM_FOOD_ID, item.getFoodId());
                    itemValues.put(KEY_ORDER_ITEM_PRICE, item.getPrice());
                    itemValues.put(KEY_ORDER_ITEM_QUANTITY, item.getQuantity());
                    
                    // Save name for convenience
                    itemValues.put("name", item.getName());
                    
                    db.insert(TABLE_ORDER_ITEMS, null, itemValues);
                }
                Log.d(TAG, "Saved " + order.getOrderItems().size() + " order items");
            }
            
            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.e(TAG, "Error saving order", e);
        } finally {
            db.endTransaction();
        }
        
        return orderId;
    }

    /**
     * Get current active order for a table (unpaid order)
     */
    public Order getCurrentOrderForTable(long tableId) {
        Order order = null;
        SQLiteDatabase db = this.getReadableDatabase();
        
        Log.d(TAG, "Getting current order for table ID: " + tableId);
        
        String query = "SELECT * FROM " + TABLE_ORDERS + 
                       " WHERE " + KEY_ORDER_TABLE_ID + " = ? AND " + 
                       KEY_ORDER_STATUS + " = 'Chưa thanh toán'" +
                       " ORDER BY " + KEY_ORDER_ID + " DESC LIMIT 1";
        
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(tableId)});
        
        try {
            if (cursor.moveToFirst()) {
                order = new Order();
                order.setId(cursor.getLong(cursor.getColumnIndexOrThrow(KEY_ORDER_ID)));
                order.setTableId(cursor.getLong(cursor.getColumnIndexOrThrow(KEY_ORDER_TABLE_ID)));
                
                long dateMillis = cursor.getLong(cursor.getColumnIndexOrThrow(KEY_ORDER_DATE));
                order.setOrderDate(new Date(dateMillis));
                
                order.setTotalAmount(cursor.getDouble(cursor.getColumnIndexOrThrow(KEY_ORDER_TOTAL_AMOUNT)));
                order.setStatus(cursor.getString(cursor.getColumnIndexOrThrow(KEY_ORDER_STATUS)));
                
                Log.d(TAG, "Found order ID: " + order.getId() + " with status: " + order.getStatus());
                
                // Get order items for this order
                List<OrderItem> items = getOrderItemsForOrder(order.getId());
                order.setOrderItems(items);
                
                Log.d(TAG, "Order has " + items.size() + " items");
            } else {
                Log.d(TAG, "No unpaid order found for table ID: " + tableId);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting current order for table", e);
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
        
        return order;
    }

    /**
     * Get all order items for a specific order
     */
    public List<OrderItem> getOrderItemsForOrder(long orderId) {
        List<OrderItem> items = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        
        String query = "SELECT oi.*, f." + KEY_FOOD_NAME + 
                       " FROM " + TABLE_ORDER_ITEMS + " oi" +
                       " LEFT JOIN " + TABLE_FOODS + " f ON oi." + KEY_ORDER_ITEM_FOOD_ID + " = f." + KEY_FOOD_ID +
                       " WHERE oi." + KEY_ORDER_ITEM_ORDER_ID + " = ?";
        
        Cursor cursor = db.rawQuery(query, new String[] {String.valueOf(orderId)});
        
        try {
            if (cursor.moveToFirst()) {
                do {
                    OrderItem item = new OrderItem();
                    item.setId(cursor.getLong(cursor.getColumnIndexOrThrow(KEY_ORDER_ITEM_ID)));
                    item.setOrderId(cursor.getLong(cursor.getColumnIndexOrThrow(KEY_ORDER_ITEM_ORDER_ID)));
                    item.setFoodId(cursor.getLong(cursor.getColumnIndexOrThrow(KEY_ORDER_ITEM_FOOD_ID)));
                    
                    // Try to get name from food table first, if it's null, check if it was saved directly in order_items
                    String foodName = cursor.getString(cursor.getColumnIndexOrThrow(KEY_FOOD_NAME));
                    if (foodName == null) {
                        try {
                            foodName = cursor.getString(cursor.getColumnIndexOrThrow("name"));
                        } catch (Exception e) {
                            // If name field doesn't exist in order_items table, use a default
                            foodName = "Món #" + item.getFoodId();
                        }
                    }
                    item.setName(foodName);
                    
                    item.setPrice(cursor.getDouble(cursor.getColumnIndexOrThrow(KEY_ORDER_ITEM_PRICE)));
                    item.setQuantity(cursor.getInt(cursor.getColumnIndexOrThrow(KEY_ORDER_ITEM_QUANTITY)));
                    
                    items.add(item);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting order items", e);
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
        
        return items;
    }

    /**
     * Add an order item to the database
     */
    public long addOrderItem(OrderItem item) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        
        values.put(KEY_ORDER_ITEM_ORDER_ID, item.getOrderId());
        values.put(KEY_ORDER_ITEM_FOOD_ID, item.getFoodId());
        values.put(KEY_ORDER_ITEM_QUANTITY, item.getQuantity());
        values.put(KEY_ORDER_ITEM_PRICE, item.getPrice());
        
        long itemId = -1;
        try {
            itemId = db.insertOrThrow(TABLE_ORDER_ITEMS, null, values);
        } catch (Exception e) {
            Log.e(TAG, "Error adding order item", e);
        }
        
        return itemId;
    }

    /**
     * Update order status
     */
    public boolean updateOrderStatus(long orderId, String status) {
        Log.d(TAG, "Updating order " + orderId + " status to: " + status);
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_ORDER_STATUS, status);
        
        int rows = db.update(TABLE_ORDERS, values, KEY_ORDER_ID + " = ?", new String[]{String.valueOf(orderId)});
        Log.d(TAG, "Updated " + rows + " rows");
        return rows > 0;
    }

    public Order getOrderById(long orderId) {
        SQLiteDatabase db = getReadableDatabase();
        Order order = null;
        
        String query = "SELECT * FROM " + TABLE_ORDERS + " WHERE " + KEY_ORDER_ID + " = ?";
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(orderId)});
        
        try {
            if (cursor.moveToFirst()) {
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                Date orderDate = null;
                try {
                    orderDate = dateFormat.parse(cursor.getString(cursor.getColumnIndexOrThrow(KEY_ORDER_DATE)));
                } catch (Exception e) {
                    orderDate = new Date();
                }
                
                order = new Order(
                    cursor.getLong(cursor.getColumnIndexOrThrow(KEY_ORDER_ID)),
                    cursor.getLong(cursor.getColumnIndexOrThrow(KEY_ORDER_TABLE_ID)),
                    orderDate,
                    cursor.getDouble(cursor.getColumnIndexOrThrow(KEY_ORDER_TOTAL_AMOUNT)),
                    cursor.getString(cursor.getColumnIndexOrThrow(KEY_ORDER_STATUS))
                );
                
                // Get order items
                order.setOrderItems(getOrderItemsByOrderId(orderId));
            }
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
        
        return order;
    }

    private List<OrderItem> getOrderItemsByOrderId(long orderId) {
        SQLiteDatabase db = getReadableDatabase();
        List<OrderItem> items = new ArrayList<>();
        
        String query = "SELECT oi.*, f." + KEY_FOOD_NAME + " FROM " + TABLE_ORDER_ITEMS + " oi " +
                      "INNER JOIN " + TABLE_FOODS + " f ON oi." + KEY_ORDER_ITEM_FOOD_ID + " = f." + KEY_FOOD_ID + " " +
                      "WHERE oi." + KEY_ORDER_ITEM_ORDER_ID + " = ?";
        
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(orderId)});
        
        try {
            if (cursor.moveToFirst()) {
                do {
                    OrderItem item = new OrderItem(
                        cursor.getString(cursor.getColumnIndexOrThrow(KEY_FOOD_NAME)),
                        cursor.getInt(cursor.getColumnIndexOrThrow(KEY_ORDER_ITEM_QUANTITY)),
                        cursor.getDouble(cursor.getColumnIndexOrThrow(KEY_ORDER_ITEM_PRICE))
                    );
                    items.add(item);
                } while (cursor.moveToNext());
            }
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
        
        return items;
    }

    private long getFoodIdByName(String foodName) {
        SQLiteDatabase db = getReadableDatabase();
        long foodId = -1;
        
        String query = "SELECT " + KEY_FOOD_ID + " FROM " + TABLE_FOODS + " WHERE " + KEY_FOOD_NAME + " = ?";
        Cursor cursor = db.rawQuery(query, new String[]{foodName});
        
        try {
            if (cursor.moveToFirst()) {
                foodId = cursor.getLong(cursor.getColumnIndexOrThrow(KEY_FOOD_ID));
            }
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
        
        return foodId;
    }

    /**
     * Get order history for a table
     */
    public List<Order> getOrderHistoryForTable(long tableId) {
        List<Order> orders = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        
        String query = "SELECT * FROM " + TABLE_ORDERS + 
                       " WHERE " + KEY_ORDER_TABLE_ID + " = ?" +
                       " ORDER BY " + KEY_ORDER_DATE + " DESC";
        
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(tableId)});
        
        try {
            if (cursor.moveToFirst()) {
                do {
                    Order order = new Order();
                    order.setId(cursor.getLong(cursor.getColumnIndexOrThrow(KEY_ORDER_ID)));
                    order.setTableId(cursor.getLong(cursor.getColumnIndexOrThrow(KEY_ORDER_TABLE_ID)));
                    
                    long dateMillis = cursor.getLong(cursor.getColumnIndexOrThrow(KEY_ORDER_DATE));
                    order.setOrderDate(new Date(dateMillis));
                    
                    order.setTotalAmount(cursor.getDouble(cursor.getColumnIndexOrThrow(KEY_ORDER_TOTAL_AMOUNT)));
                    order.setStatus(cursor.getString(cursor.getColumnIndexOrThrow(KEY_ORDER_STATUS)));
                    
                    orders.add(order);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting order history for table", e);
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
        
        return orders;
    }

    /**
     * Get all orders for a specific date
     * @param date Format: yyyy-MM-dd
     * @return List of orders for that date
     */
    public List<Order> getOrdersByDate(String date) {
        List<Order> orders = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        
        // Convert date to start/end timestamps for that day
        String startOfDay = date + " 00:00:00";
        String endOfDay = date + " 23:59:59";
        
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        long startTimestamp = 0;
        long endTimestamp = 0;
        
        try {
            startTimestamp = sdf.parse(startOfDay).getTime();
            endTimestamp = sdf.parse(endOfDay).getTime();
        } catch (Exception e) {
            Log.e(TAG, "Error parsing date", e);
            return orders;
        }
        
        String query = "SELECT * FROM " + TABLE_ORDERS + 
                       " WHERE " + KEY_ORDER_DATE + " BETWEEN ? AND ?" +
                       " ORDER BY " + KEY_ORDER_DATE + " DESC";
        
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(startTimestamp), String.valueOf(endTimestamp)});
        
        try {
            if (cursor.moveToFirst()) {
                do {
                    Order order = new Order();
                    order.setId(cursor.getLong(cursor.getColumnIndexOrThrow(KEY_ORDER_ID)));
                    order.setTableId(cursor.getLong(cursor.getColumnIndexOrThrow(KEY_ORDER_TABLE_ID)));
                    
                    long dateMillis = cursor.getLong(cursor.getColumnIndexOrThrow(KEY_ORDER_DATE));
                    order.setOrderDate(new Date(dateMillis));
                    
                    order.setTotalAmount(cursor.getDouble(cursor.getColumnIndexOrThrow(KEY_ORDER_TOTAL_AMOUNT)));
                    order.setStatus(cursor.getString(cursor.getColumnIndexOrThrow(KEY_ORDER_STATUS)));
                    
                    // Get table name
                    String tableName = getTableNameById(order.getTableId());
                    if (tableName != null) {
                        // Temporarily store the table name in the order object's notes
                        order.setTableName(tableName);
                    }
                    
                    orders.add(order);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting orders by date", e);
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
        
        return orders;
    }
    
    /**
     * Get table name by ID
     */
    public String getTableNameById(long tableId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String name = null;
        
        String query = "SELECT " + KEY_TABLE_NAME + " FROM " + TABLE_TABLES + 
                       " WHERE " + KEY_TABLE_ID + " = ?";
        
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(tableId)});
        
        try {
            if (cursor.moveToFirst()) {
                name = cursor.getString(cursor.getColumnIndexOrThrow(KEY_TABLE_NAME));
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting table name by ID", e);
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
        
        return name;
    }

    /**
     * Kiểm tra xem số điện thoại có tồn tại trong bảng employees hay không
     * @param phone Số điện thoại cần kiểm tra
     * @param currentEmployeeId ID của nhân viên đang được chỉnh sửa (để loại trừ chính nhân viên đó khỏi kiểm tra)
     * @return true nếu số điện thoại đã tồn tại ở nhân viên khác, false nếu chưa tồn tại
     */
    public boolean isPhoneNumberExists(String phone, long currentEmployeeId) {
        SQLiteDatabase db = getReadableDatabase();
        String selection;
        String[] selectionArgs;
        
        // Nếu đang chỉnh sửa nhân viên, loại trừ nhân viên hiện tại khỏi kiểm tra
        if (currentEmployeeId > 0) {
            selection = KEY_EMPLOYEE_PHONE + " = ? AND " + KEY_EMPLOYEE_ID + " != ?";
            selectionArgs = new String[]{phone, String.valueOf(currentEmployeeId)};
        } else {
            selection = KEY_EMPLOYEE_PHONE + " = ?";
            selectionArgs = new String[]{phone};
        }
        
        Cursor cursor = db.query(TABLE_EMPLOYEES, new String[]{KEY_EMPLOYEE_ID}, 
                                  selection, selectionArgs, null, null, null);
        int count = cursor.getCount();
        cursor.close();
        
        return count > 0;
    }

    /**
     * Kiểm tra xem một món ăn đã tồn tại trong menu của một ngày cụ thể hay chưa
     * @param foodId ID của món ăn cần kiểm tra
     * @param date Ngày cần kiểm tra (định dạng yyyy-MM-dd)
     * @return true nếu món ăn đã tồn tại trong menu ngày đó, false nếu chưa tồn tại
     */
    public boolean isFoodInDailyMenu(long foodId, String date) {
        SQLiteDatabase db = getReadableDatabase();
        boolean exists = false;
        
        String selection = KEY_DAILY_MENU_FOOD_ID + " = ? AND " + KEY_DAILY_MENU_DATE + " = ?";
        String[] selectionArgs = {String.valueOf(foodId), date};
        
        Cursor cursor = db.query(TABLE_DAILY_MENU, new String[]{KEY_DAILY_MENU_ID}, 
                                  selection, selectionArgs, null, null, null);
        
        exists = cursor != null && cursor.getCount() > 0;
        
        if (cursor != null) {
            cursor.close();
        }
        
        return exists;
    }

    /**
     * Kiểm tra xem số bàn đã tồn tại hay chưa
     * @param tableNumber Số bàn cần kiểm tra
     * @param currentTableId ID của bàn đang được chỉnh sửa (để loại trừ khi kiểm tra)
     * @return true nếu số bàn đã tồn tại, false nếu chưa tồn tại
     */
    public boolean isTableNumberExists(String tableNumber, long currentTableId) {
        SQLiteDatabase db = getReadableDatabase();
        String selection;
        String[] selectionArgs;
        
        // Nếu đang chỉnh sửa bàn, loại trừ bàn hiện tại khỏi kiểm tra
        if (currentTableId > 0) {
            selection = KEY_TABLE_NAME + " = ? AND " + KEY_TABLE_ID + " != ?";
            selectionArgs = new String[]{tableNumber, String.valueOf(currentTableId)};
        } else {
            selection = KEY_TABLE_NAME + " = ?";
            selectionArgs = new String[]{tableNumber};
        }
        
        Cursor cursor = db.query(TABLE_TABLES, new String[]{KEY_TABLE_ID}, 
                                  selection, selectionArgs, null, null, null);
        int count = cursor.getCount();
        cursor.close();
        
        return count > 0;
    }

    public long addTable(Table table) {
        // Kiểm tra số bàn đã tồn tại chưa
        if (isTableNumberExists(table.getName(), 0)) {
            return -1; // Số bàn đã tồn tại
        }
        
        SQLiteDatabase db = getWritableDatabase();
        long tableId = -1;
        db.beginTransaction();
        try {
            ContentValues values = new ContentValues();
            values.put(KEY_TABLE_NAME, table.getName());
            values.put(KEY_TABLE_CAPACITY, table.getCapacity());
            values.put(KEY_TABLE_STATUS, table.getStatus());
            values.put(KEY_TABLE_NOTE, table.getNote());
            values.put(KEY_TABLE_TYPE, table.getTableType());
            tableId = db.insertOrThrow(TABLE_TABLES, null, values);
            db.setTransactionSuccessful();
        } catch (Exception e) {
            // Error in between database transaction
        } finally {
            db.endTransaction();
        }
        return tableId;
    }
    
    

    /**
     * Update an order item in the database
     */
    public boolean updateOrderItem(OrderItem item) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        
        values.put(KEY_ORDER_ITEM_ORDER_ID, item.getOrderId());
        values.put(KEY_ORDER_ITEM_FOOD_ID, item.getFoodId());
        values.put(KEY_ORDER_ITEM_QUANTITY, item.getQuantity());
        values.put(KEY_ORDER_ITEM_PRICE, item.getPrice());
        
        String selection = KEY_ORDER_ITEM_ID + " = ?";
        String[] selectionArgs = {String.valueOf(item.getId())};
        
        int rows = db.update(TABLE_ORDER_ITEMS, values, selection, selectionArgs);
        return rows > 0;
    }
    
    /**
     * Update an order in the database
     */
    public boolean updateOrder(Order order) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        
        values.put(KEY_ORDER_TABLE_ID, order.getTableId());
        values.put(KEY_ORDER_DATE, order.getOrderDate().getTime());
        values.put(KEY_ORDER_TOTAL_AMOUNT, order.getTotalAmount());
        values.put(KEY_ORDER_STATUS, order.getStatus());
        
        String selection = KEY_ORDER_ID + " = ?";
        String[] selectionArgs = {String.valueOf(order.getId())};
        
        int rows = db.update(TABLE_ORDERS, values, selection, selectionArgs);
        return rows > 0;
    }
} 