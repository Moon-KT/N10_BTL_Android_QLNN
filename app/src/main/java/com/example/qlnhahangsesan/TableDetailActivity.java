package com.example.qlnhahangsesan;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.qlnhahangsesan.adapter.OrderFoodAdapter;
import com.example.qlnhahangsesan.database.DatabaseHelper;
import com.example.qlnhahangsesan.model.Food;
import com.example.qlnhahangsesan.model.Order;
import com.example.qlnhahangsesan.model.OrderItem;
import com.example.qlnhahangsesan.model.Table;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TableDetailActivity extends AppCompatActivity {
    
    private static final String TAG = "TableDetailActivity";
    private static final int REQUEST_FOOD_SELECTION = 100;
    
    // Table information views
    private EditText editTextName;
    private EditText editTextCapacity;
    private Spinner spinnerStatus;
    private Spinner spinnerType;
    private EditText editTextNote;
    private Button buttonSave;
    private Button buttonCancel;
    private LinearLayout buttonContainer;
    
    // Order section views
    private TextView textViewOrderSectionTitle;
    private Button buttonOrder;
    private LinearLayout orderListHeaderContainer;
    private ListView listViewOrderItems;
    private TextView textViewEmptyOrderList;
    private LinearLayout totalContainer;
    private TextView textViewTotalAmount;
    private CheckBox checkBoxPrintReceipt;
    private Button buttonCheckout;
    
    // Data and state
    private DatabaseHelper databaseHelper;
    private Table currentTable;
    private Order currentOrder;
    private ArrayList<OrderItem> orderItems;
    private OrderFoodAdapter orderFoodAdapter;
    private boolean isEditMode = false;
    private boolean isViewMode = false;
    private NumberFormat currencyFormat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_table_detail);

        // Initialize database helper
        databaseHelper = DatabaseHelper.getInstance(this);
        
        // Initialize currency formatter
        currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));

        // Initialize views
        initViews();
        setupSpinners();
        
        // Determine the mode (add, edit, or view)
        determineMode();
        
        // Set up action bar
        setupActionBar();

        // Set up button click listeners
        setupClickListeners();
    }
    
    private void initViews() {
        // Table information views
        editTextName = findViewById(R.id.editTextTableName);
        editTextCapacity = findViewById(R.id.editTextTableCapacity);
        spinnerStatus = findViewById(R.id.spinnerTableStatus);
        spinnerType = findViewById(R.id.spinnerTableType);
        editTextNote = findViewById(R.id.editTextTableNote);
        buttonSave = findViewById(R.id.buttonSave);
        buttonCancel = findViewById(R.id.buttonCancel);
        buttonContainer = findViewById(R.id.buttonContainer);
        
        // Order section views
        textViewOrderSectionTitle = findViewById(R.id.textViewOrderSectionTitle);
        buttonOrder = findViewById(R.id.buttonOrder);
        orderListHeaderContainer = findViewById(R.id.orderListHeaderContainer);
        listViewOrderItems = findViewById(R.id.listViewOrderItems);
        textViewEmptyOrderList = findViewById(R.id.textViewEmptyOrderList);
        totalContainer = findViewById(R.id.totalContainer);
        textViewTotalAmount = findViewById(R.id.textViewTotalAmount);
        checkBoxPrintReceipt = findViewById(R.id.checkBoxPrintReceipt);
        buttonCheckout = findViewById(R.id.buttonCheckout);
    }
    
    private void setupSpinners() {
        // Set up spinner for status
        ArrayAdapter<CharSequence> statusAdapter = ArrayAdapter.createFromResource(this,
                R.array.table_status_array, android.R.layout.simple_spinner_item);
        statusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerStatus.setAdapter(statusAdapter);

        // Set up spinner for table type
        ArrayAdapter<CharSequence> typeAdapter = ArrayAdapter.createFromResource(this,
                R.array.table_type_array, android.R.layout.simple_spinner_item);
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerType.setAdapter(typeAdapter);
    }
    
    private void determineMode() {
        if (getIntent().hasExtra("table")) {
            currentTable = (Table) getIntent().getSerializableExtra("table");
            
            // Check if we are in view mode or edit mode
            if (getIntent().getBooleanExtra("viewMode", false)) {
                isViewMode = true;
                isEditMode = false;
                setupViewMode();
            } else {
                isViewMode = false;
                isEditMode = true;
                setupEditMode();
            }
        } else {
            // New table mode
            isViewMode = false;
            isEditMode = false;
            currentTable = new Table();
            setupAddMode();
        }
    }
    
    private void setupActionBar() {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            if (isViewMode) {
                getSupportActionBar().setTitle("Chi tiết bàn");
            } else if (isEditMode) {
                getSupportActionBar().setTitle(R.string.edit_table);
            } else {
                getSupportActionBar().setTitle(R.string.add_table);
            }
        }
    }
    
    private void setupClickListeners() {
        buttonSave.setOnClickListener(v -> saveTable());
        buttonCancel.setOnClickListener(v -> finish());
        buttonOrder.setOnClickListener(v -> openFoodSelection());
        buttonCheckout.setOnClickListener(v -> showCheckoutConfirmation());
    }
    
    private void setupAddMode() {
        // Just show table information fields and save/cancel buttons
        buttonContainer.setVisibility(View.VISIBLE);
        
        // Hide order section
        textViewOrderSectionTitle.setVisibility(View.GONE);
        buttonOrder.setVisibility(View.GONE);
        orderListHeaderContainer.setVisibility(View.GONE);
        listViewOrderItems.setVisibility(View.GONE);
        textViewEmptyOrderList.setVisibility(View.GONE);
        totalContainer.setVisibility(View.GONE);
        checkBoxPrintReceipt.setVisibility(View.GONE);
        buttonCheckout.setVisibility(View.GONE);
    }
    
    private void setupEditMode() {
        // Enable editing of table information
        populateFields();
        buttonContainer.setVisibility(View.VISIBLE);
        
        // Hide order section
        textViewOrderSectionTitle.setVisibility(View.GONE);
        buttonOrder.setVisibility(View.GONE);
        orderListHeaderContainer.setVisibility(View.GONE);
        listViewOrderItems.setVisibility(View.GONE);
        textViewEmptyOrderList.setVisibility(View.GONE);
        totalContainer.setVisibility(View.GONE);
        checkBoxPrintReceipt.setVisibility(View.GONE);
        buttonCheckout.setVisibility(View.GONE);
    }
    
    private void setupViewMode() {
        // Populate fields
        populateFields();
        
        // Disable editing
        setFieldsReadOnly(true);
        
        // Hide save/cancel buttons
        buttonContainer.setVisibility(View.GONE);
        
        // Show order section
        textViewOrderSectionTitle.setVisibility(View.VISIBLE);
        buttonOrder.setVisibility(View.VISIBLE);
        
        // Initialize order items list
        orderItems = new ArrayList<>();
        orderFoodAdapter = new OrderFoodAdapter(this, orderItems);
        listViewOrderItems.setAdapter(orderFoodAdapter);
        
        // Load current order if exists
        loadCurrentOrder();
    }
    
    private void setFieldsReadOnly(boolean readOnly) {
        editTextName.setEnabled(!readOnly);
        editTextCapacity.setEnabled(!readOnly);
        spinnerStatus.setEnabled(!readOnly);
        spinnerType.setEnabled(!readOnly);
        editTextNote.setEnabled(!readOnly);
    }

    private void populateFields() {
        editTextName.setText(currentTable.getName());
        editTextCapacity.setText(String.valueOf(currentTable.getCapacity()));
        editTextNote.setText(currentTable.getNote());
        
        // Set status spinner
        String tableStatus = currentTable.getStatus();
        ArrayAdapter<CharSequence> statusAdapter = (ArrayAdapter<CharSequence>) spinnerStatus.getAdapter();
        if (statusAdapter != null && tableStatus != null) {
            for (int i = 0; i < statusAdapter.getCount(); i++) {
                if (statusAdapter.getItem(i).toString().equals(tableStatus)) {
                    spinnerStatus.setSelection(i);
                    break;
                }
            }
        }
        
        // Set type spinner
        String tableType = currentTable.getTableType();
        if (tableType == null) {
            tableType = "Thường"; // Default to "Thường" if null
        }
        ArrayAdapter<CharSequence> typeAdapter = (ArrayAdapter<CharSequence>) spinnerType.getAdapter();
        if (typeAdapter != null) {
            for (int i = 0; i < typeAdapter.getCount(); i++) {
                if (typeAdapter.getItem(i).toString().equals(tableType)) {
                    spinnerType.setSelection(i);
                    break;
                }
            }
        }
    }

    private void saveTable() {
        // Validate input
        String name = editTextName.getText().toString().trim();
        String capacityStr = editTextCapacity.getText().toString().trim();
        String status = spinnerStatus.getSelectedItem().toString();
        String type = spinnerType.getSelectedItem().toString();
        String note = editTextNote.getText().toString().trim();
        
        if (name.isEmpty()) {
            editTextName.setError("Số bàn không được để trống");
            editTextName.requestFocus();
            return;
        }
        
        if (capacityStr.isEmpty()) {
            editTextCapacity.setError(getString(R.string.capacity) + " không được để trống");
            editTextCapacity.requestFocus();
            return;
        }
        
        int capacity = Integer.parseInt(capacityStr);
        if (capacity <= 0) {
            editTextCapacity.setError(getString(R.string.capacity) + " phải lớn hơn 0");
            editTextCapacity.requestFocus();
            return;
        }
        
        // Kiểm tra số bàn đã tồn tại chưa
        if (databaseHelper.isTableNumberExists(name, isEditMode ? currentTable.getId() : 0)) {
            editTextName.setError("Số bàn đã tồn tại");
            editTextName.requestFocus();
            return;
        }
        
        // Update table object
        currentTable.setName(name);
        currentTable.setCapacity(capacity);
        currentTable.setStatus(status);
        currentTable.setNote(note);
        currentTable.setTableType(type);
        
        // Save to database
        if (isEditMode) {
            boolean success = databaseHelper.updateTable(currentTable);
            if (success) {
                Toast.makeText(this, getString(R.string.table_saved), Toast.LENGTH_SHORT).show();
                setResult(RESULT_OK);
                finish();
            } else {
                Toast.makeText(this, "Lỗi khi cập nhật thông tin bàn", Toast.LENGTH_SHORT).show();
            }
        } else {
            long id = databaseHelper.addTable(currentTable);
            if (id > 0) {
                currentTable.setId(id);
                Toast.makeText(this, getString(R.string.table_saved), Toast.LENGTH_SHORT).show();
                setResult(RESULT_OK);
                finish();
            } else {
                Toast.makeText(this, "Lỗi khi thêm bàn mới", Toast.LENGTH_SHORT).show();
            }
        }
    }
    
    private void loadCurrentOrder() {
        if (currentTable.getId() <= 0) {
            return;
        }
        
        // Get the current order for this table
        currentOrder = databaseHelper.getCurrentOrderForTable(currentTable.getId());
        
        if (currentOrder != null) {
            // Get order items
            orderItems.clear();
            List<OrderItem> items = databaseHelper.getOrderItemsForOrder(currentOrder.getId());
            if (items != null && !items.isEmpty()) {
                orderItems.addAll(items);
                
                // Show order list
                orderListHeaderContainer.setVisibility(View.VISIBLE);
                listViewOrderItems.setVisibility(View.VISIBLE);
                textViewEmptyOrderList.setVisibility(View.GONE);
                totalContainer.setVisibility(View.VISIBLE);
                
                // Update total amount
                updateTotalAmount();
                
                // Show checkout button and print receipt checkbox
                checkBoxPrintReceipt.setVisibility(View.VISIBLE);
                buttonCheckout.setVisibility(View.VISIBLE);
            } else {
                showEmptyOrderList();
            }
        } else {
            showEmptyOrderList();
        }
        
        // Notify adapter about data changes
        if (orderFoodAdapter != null) {
            orderFoodAdapter.notifyDataSetChanged();
        }
    }
    
    private void showEmptyOrderList() {
        orderListHeaderContainer.setVisibility(View.GONE);
        listViewOrderItems.setVisibility(View.GONE);
        textViewEmptyOrderList.setVisibility(View.VISIBLE);
        totalContainer.setVisibility(View.GONE);
        checkBoxPrintReceipt.setVisibility(View.GONE);
        buttonCheckout.setVisibility(View.GONE);
    }
    
    private void updateTotalAmount() {
        double total = 0;
        for (OrderItem item : orderItems) {
            total += item.getPrice() * item.getQuantity();
        }
        
        textViewTotalAmount.setText(currencyFormat.format(total));
        
        // Update the order's total amount
        if (currentOrder != null) {
            currentOrder.setTotalAmount(total);
            currentOrder.setOrderItems(orderItems);
            
            // Update in database if order already exists
            if (currentOrder.getId() > 0) {
                databaseHelper.updateOrder(currentOrder);
            }
        }
    }
    
    private void openFoodSelection() {
        // Set table status to "Đang phục vụ" if it's currently "Trống"
        String currentStatus = spinnerStatus.getSelectedItem().toString();
        String availableStatus = getString(R.string.status_available);
        String occupiedStatus = getString(R.string.status_occupied);
        
        if (currentStatus.equals(availableStatus)) {
            // Update table status
            currentTable.setStatus(occupiedStatus);
            databaseHelper.updateTable(currentTable);
            
            // Update spinner to show new status
            ArrayAdapter<CharSequence> adapter = (ArrayAdapter<CharSequence>) spinnerStatus.getAdapter();
            for (int i = 0; i < adapter.getCount(); i++) {
                if (adapter.getItem(i).toString().equals(occupiedStatus)) {
                    spinnerStatus.setSelection(i);
                    break;
                }
            }
        }
        
        // Open food selection activity to select menu items for today
        Intent intent = new Intent(this, FoodSelectionActivity.class);
        intent.putExtra("tableId", currentTable.getId());
        intent.putExtra("tableName", currentTable.getName());
        intent.putExtra("todayOnly", true); // Force selecting items from today's menu only
        startActivityForResult(intent, REQUEST_FOOD_SELECTION);
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (requestCode == REQUEST_FOOD_SELECTION && resultCode == RESULT_OK && data != null) {
            // Get selected food items from the result
            ArrayList<OrderItem> selectedItems = null;
            try {
                selectedItems = (ArrayList<OrderItem>) data.getSerializableExtra("selectedItems");
            } catch (ClassCastException e) {
                Log.e(TAG, "Error casting selectedItems", e);
                Toast.makeText(this, "Lỗi khi nhận dữ liệu món ăn", Toast.LENGTH_SHORT).show();
                return;
            }
            
            if (selectedItems != null && !selectedItems.isEmpty()) {
                // If no order exists yet, create a new one
                if (currentOrder == null) {
                    currentOrder = new Order();
                    currentOrder.setTableId(currentTable.getId());
                    currentOrder.setOrderDate(new Date());
                    currentOrder.setStatus("Chưa thanh toán");
                    currentOrder.setOrderItems(new ArrayList<>());
                    
                    // Save the new order to database
                    long orderId = databaseHelper.saveOrder(currentOrder);
                    currentOrder.setId(orderId);
                }
                
                // Process selected items - merge with existing items or add new ones
                for (OrderItem selectedItem : selectedItems) {
                    // Set the order ID for the selected item
                    selectedItem.setOrderId(currentOrder.getId());
                    
                    // Check if this item already exists
                    boolean itemExists = false;
                    for (OrderItem existingItem : orderItems) {
                        if (existingItem.getFoodId() == selectedItem.getFoodId()) {
                            // Increase quantity of existing item
                            existingItem.setQuantity(existingItem.getQuantity() + selectedItem.getQuantity());
                            itemExists = true;
                            
                            // Update in database
                            databaseHelper.updateOrderItem(existingItem);
                            break;
                        }
                    }
                    
                    // If item doesn't exist, add it
                    if (!itemExists) {
                        // Add to database
                        long itemId = databaseHelper.addOrderItem(selectedItem);
                        selectedItem.setId(itemId);
                        
                        // Add to our list
                        orderItems.add(selectedItem);
                    }
                }
                
                // Show order list
                orderListHeaderContainer.setVisibility(View.VISIBLE);
                listViewOrderItems.setVisibility(View.VISIBLE);
                textViewEmptyOrderList.setVisibility(View.GONE);
                totalContainer.setVisibility(View.VISIBLE);
                
                // Show checkout button and print receipt checkbox
                checkBoxPrintReceipt.setVisibility(View.VISIBLE);
                buttonCheckout.setVisibility(View.VISIBLE);
                
                // Update total amount
                updateTotalAmount();
                
                // Update the adapter
                orderFoodAdapter.notifyDataSetChanged();
            }
        }
    }
    
    private void showCheckoutConfirmation() {
        if (orderItems.isEmpty()) {
            Toast.makeText(this, "Không có món ăn nào để thanh toán", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Create dialog for checkout confirmation
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Xác nhận thanh toán");
        
        // Inflate checkout confirmation layout
        View confirmView = getLayoutInflater().inflate(R.layout.dialog_checkout_confirmation, null);
        
        // Set table information
        TextView tableNumber = confirmView.findViewById(R.id.textViewTableNumberDialog);
        TextView tableType = confirmView.findViewById(R.id.textViewTableTypeDialog);
        TextView tableCapacity = confirmView.findViewById(R.id.textViewTableCapacityDialog);
        
        tableNumber.setText(currentTable.getName());
        tableType.setText(currentTable.getTableType());
        tableCapacity.setText(currentTable.getCapacity() + " người");
        
        // Set up the order items list
        ListView listViewItems = confirmView.findViewById(R.id.listViewOrderItemsDialog);
        OrderFoodAdapter adapter = new OrderFoodAdapter(this, orderItems);
        listViewItems.setAdapter(adapter);
        
        // Set total amount
        TextView totalAmount = confirmView.findViewById(R.id.textViewTotalAmountDialog);
        totalAmount.setText(textViewTotalAmount.getText());
        
        builder.setView(confirmView);
        
        // Add checkout confirmation buttons
        builder.setPositiveButton("Thanh toán", (dialog, which) -> {
            processCheckout();
        });
        
        builder.setNegativeButton("Hủy", null);
        
        builder.show();
    }
    
    private void processCheckout() {
        if (currentOrder == null || currentOrder.getId() <= 0) {
            Toast.makeText(this, "Không có đơn hàng để thanh toán", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Update order status to "Đã thanh toán"
        currentOrder.setStatus("Đã thanh toán");
        boolean success = databaseHelper.updateOrderStatus(currentOrder.getId(), "Đã thanh toán");
        
        if (success) {
            // Check if receipt should be printed
            if (checkBoxPrintReceipt.isChecked()) {
                generateReceipt();
            }
            
            // Update table status to "Trống"
            String availableStatus = getString(R.string.status_available);
            currentTable.setStatus(availableStatus);
            databaseHelper.updateTable(currentTable);
            
            // Update spinner to show new status
            ArrayAdapter<CharSequence> adapter = (ArrayAdapter<CharSequence>) spinnerStatus.getAdapter();
            for (int i = 0; i < adapter.getCount(); i++) {
                if (adapter.getItem(i).toString().equals(availableStatus)) {
                    spinnerStatus.setSelection(i);
                    break;
                }
            }
            
            // Show success message
            Toast.makeText(this, "Thanh toán thành công!", Toast.LENGTH_SHORT).show();
            
            // Clear the order UI
            currentOrder = null;
            orderItems.clear();
            orderFoodAdapter.notifyDataSetChanged();
            showEmptyOrderList();
        } else {
            Toast.makeText(this, "Lỗi khi thanh toán", Toast.LENGTH_SHORT).show();
        }
    }
    
    private void generateReceipt() {
        if (currentOrder == null || orderItems.isEmpty()) {
            return;
        }
        
        // Create a PDF document
        PdfDocument document = new PdfDocument();
        
        // Create a page
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(595, 842, 1).create();
        PdfDocument.Page page = document.startPage(pageInfo);
        
        // Draw content on the page
        Canvas canvas = page.getCanvas();
        Paint paint = new Paint();
        
        // Set up text paint
        paint.setColor(Color.BLACK);
        paint.setTextSize(12);
        
        // Draw header
        paint.setTextSize(18);
        paint.setFakeBoldText(true);
        canvas.drawText("HÓA ĐƠN", 250, 50, paint);
        
        // Draw restaurant info
        paint.setTextSize(12);
        paint.setFakeBoldText(false);
        canvas.drawText("Nhà hàng Sesan", 50, 80, paint);
        canvas.drawText("Địa chỉ: 123 Đường ABC, Thành phố XYZ", 50, 100, paint);
        canvas.drawText("SĐT: 0123456789", 50, 120, paint);
        
        // Draw date
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault());
        String date = dateFormat.format(new Date());
        canvas.drawText("Ngày: " + date, 450, 80, paint);
        
        // Draw table info
        paint.setFakeBoldText(true);
        canvas.drawText("Thông tin bàn:", 50, 160, paint);
        paint.setFakeBoldText(false);
        canvas.drawText("Số bàn: " + currentTable.getName(), 70, 180, paint);
        canvas.drawText("Loại bàn: " + currentTable.getTableType(), 70, 200, paint);
        canvas.drawText("Sức chứa: " + currentTable.getCapacity() + " người", 70, 220, paint);
        
        // Draw order items header
        paint.setFakeBoldText(true);
        canvas.drawText("Danh sách món ăn:", 50, 260, paint);
        canvas.drawText("STT", 50, 280, paint);
        canvas.drawText("Tên món", 100, 280, paint);
        canvas.drawText("SL", 300, 280, paint);
        canvas.drawText("Đơn giá", 350, 280, paint);
        canvas.drawText("Thành tiền", 450, 280, paint);
        
        // Draw divider
        paint.setFakeBoldText(false);
        canvas.drawLine(50, 290, 545, 290, paint);
        
        // Draw order items
        double total = 0;
        int yPos = 310;
        for (int i = 0; i < orderItems.size(); i++) {
            OrderItem item = orderItems.get(i);
            double itemTotal = item.getPrice() * item.getQuantity();
            total += itemTotal;
            
            canvas.drawText(String.valueOf(i + 1), 50, yPos, paint);
            canvas.drawText(item.getName(), 100, yPos, paint);
            canvas.drawText(String.valueOf(item.getQuantity()), 300, yPos, paint);
            canvas.drawText(currencyFormat.format(item.getPrice()), 350, yPos, paint);
            canvas.drawText(currencyFormat.format(itemTotal), 450, yPos, paint);
            
            yPos += 20;
        }
        
        // Draw divider
        canvas.drawLine(50, yPos, 545, yPos, paint);
        yPos += 20;
        
        // Draw total
        paint.setFakeBoldText(true);
        canvas.drawText("Tổng cộng:", 350, yPos, paint);
        canvas.drawText(currencyFormat.format(total), 450, yPos, paint);
        
        // Draw footer
        yPos += 50;
        paint.setFakeBoldText(false);
        paint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText("Cảm ơn quý khách đã sử dụng dịch vụ!", 297, yPos, paint);
        
        // Finish the page
        document.finishPage(page);
        
        // Save the document
        String fileName = "hoadon_" + currentTable.getName() + "_" + new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date()) + ".pdf";
        File file = new File(getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), fileName);
        
        try {
            document.writeTo(new FileOutputStream(file));
            Toast.makeText(this, "Đã lưu hóa đơn: " + file.getAbsolutePath(), Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            Log.e(TAG, "Error generating receipt", e);
            Toast.makeText(this, "Lỗi khi tạo hóa đơn", Toast.LENGTH_SHORT).show();
        }
        
        // Close the document
        document.close();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
} 