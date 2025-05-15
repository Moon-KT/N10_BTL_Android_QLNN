package com.example.qlnhahangsesan;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.qlnhahangsesan.adapter.TableAdapter;
import com.example.qlnhahangsesan.database.DatabaseHelper;
import com.example.qlnhahangsesan.model.Table;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;

public class TableListActivity extends AppCompatActivity implements TableAdapter.OnTableClickListener {

    private static final int REQUEST_ADD_TABLE = 1;
    private static final int REQUEST_EDIT_TABLE = 2;

    private RecyclerView recyclerViewTables;
    private TextView textViewEmpty;
    private ProgressBar progressBar;
    private FloatingActionButton fabAddTable;
    private TabLayout tabLayoutStatus;

    private DatabaseHelper databaseHelper;
    private TableAdapter tableAdapter;
    private List<Table> tableList;
    private String currentStatus = "Tất cả";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_table_list);

        // Initialize database helper
        databaseHelper = DatabaseHelper.getInstance(this);

        // Initialize views
        recyclerViewTables = findViewById(R.id.recyclerViewTables);
        textViewEmpty = findViewById(R.id.textViewEmpty);
        progressBar = findViewById(R.id.progressBar);
        fabAddTable = findViewById(R.id.fabAddTable);
        tabLayoutStatus = findViewById(R.id.tabLayoutStatus);

        // Set up RecyclerView with grid layout (2 columns)
        recyclerViewTables.setLayoutManager(new GridLayoutManager(this, 2));
        tableList = new ArrayList<>();
        tableAdapter = new TableAdapter(this, tableList, this);
        recyclerViewTables.setAdapter(tableAdapter);

        // Set up FAB
        fabAddTable.setOnClickListener(v -> {
            Intent intent = new Intent(TableListActivity.this, TableDetailActivity.class);
            startActivityForResult(intent, REQUEST_ADD_TABLE);
        });

        // Set up back button in action bar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.table_management);
        }

        // Set up status tabs
        setupStatusTabs();

        // Load table data
        loadAllTables();
    }

    private void setupStatusTabs() {
        // Add tabs for filtering by status
        tabLayoutStatus.addTab(tabLayoutStatus.newTab().setText(R.string.all_tables));
        tabLayoutStatus.addTab(tabLayoutStatus.newTab().setText(R.string.status_available));
        tabLayoutStatus.addTab(tabLayoutStatus.newTab().setText(R.string.status_reserved));
        tabLayoutStatus.addTab(tabLayoutStatus.newTab().setText(R.string.status_occupied));
        
        // Set tab selected listener
        tabLayoutStatus.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                switch (tab.getPosition()) {
                    case 0:
                        currentStatus = "Tất cả";
                        loadAllTables();
                        break;
                    case 1:
                        currentStatus = getString(R.string.status_available);
                        loadTablesByStatus(currentStatus);
                        break;
                    case 2:
                        currentStatus = getString(R.string.status_reserved);
                        loadTablesByStatus(currentStatus);
                        break;
                    case 3:
                        currentStatus = getString(R.string.status_occupied);
                        loadTablesByStatus(currentStatus);
                        break;
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                // Do nothing
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                // Do nothing
            }
        });
    }

    private void loadAllTables() {
        progressBar.setVisibility(View.VISIBLE);
        
        // Get all tables from database
        List<Table> tables = databaseHelper.getAllTables();
        
        updateTableList(tables);
    }
    
    private void loadTablesByStatus(String status) {
        progressBar.setVisibility(View.VISIBLE);
        
        // Get tables by status from database
        List<Table> tables = databaseHelper.getTablesByStatus(status);
        
        updateTableList(tables);
    }
    
    private void updateTableList(List<Table> tables) {
        tableList.clear();
        if (tables != null && !tables.isEmpty()) {
            tableList.addAll(tables);
            textViewEmpty.setVisibility(View.GONE);
        } else {
            textViewEmpty.setText(R.string.no_tables);
            textViewEmpty.setVisibility(View.VISIBLE);
        }
        
        tableAdapter.notifyDataSetChanged();
        progressBar.setVisibility(View.GONE);
    }

    @Override
    public void onTableClick(Table table, int position) {
        // Open table detail for editing
        Intent intent = new Intent(TableListActivity.this, TableDetailActivity.class);
        intent.putExtra("table", table);
        startActivityForResult(intent, REQUEST_EDIT_TABLE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (resultCode == RESULT_OK) {
            // Reload table list
            if (currentStatus.equals("Tất cả")) {
                loadAllTables();
            } else {
                loadTablesByStatus(currentStatus);
            }
        }
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