package com.example.qlnhahangsesan.fragment;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.qlnhahangsesan.R;
import com.example.qlnhahangsesan.adapter.StatisticsAdapter;
import com.example.qlnhahangsesan.database.DatabaseHelper;
import com.example.qlnhahangsesan.model.StatisticItem;
import com.google.android.material.textfield.TextInputEditText;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class StatisticsFragment extends Fragment {

    private static final String ARG_STATISTIC_TYPE = "statistic_type";
    
    // Constants for statistic types - renamed to match with StatisticsDetailsActivity
    public static final int STATS_TYPE_FOOD_CATEGORY = 1;
    public static final int STATS_TYPE_REVENUE = 2;
    public static final int STATS_TYPE_TOP_FOODS = 3;
    public static final int STATS_TYPE_TABLE_STATUS = 4;
    public static final int STATS_TYPE_MENU_BY_DATE = 5;
    
    // Backward compatibility constants
    @Deprecated
    public static final int TYPE_FOOD_BY_CATEGORY = STATS_TYPE_FOOD_CATEGORY;
    @Deprecated
    public static final int TYPE_REVENUE_BY_DATE = STATS_TYPE_REVENUE;
    @Deprecated
    public static final int TYPE_TOP_FOODS = STATS_TYPE_TOP_FOODS;
    @Deprecated
    public static final int TYPE_TABLES_BY_STATUS = STATS_TYPE_TABLE_STATUS;
    @Deprecated
    public static final int TYPE_MENU_BY_DATE = STATS_TYPE_MENU_BY_DATE;
    
    private int statisticType;
    private DatabaseHelper databaseHelper;
    
    private RecyclerView recyclerViewStatistics;
    private TextView textViewEmpty;
    private ProgressBar progressBar;
    private LinearLayout layoutDateFilter;
    private LinearLayout layoutLimitFilter;
    private TextInputEditText editTextStartDate;
    private TextInputEditText editTextEndDate;
    private TextInputEditText editTextLimit;
    private Button buttonApply;
    
    private StatisticsAdapter adapter;
    private List<StatisticItem> statisticsList = new ArrayList<>();
    
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    private Calendar startDateCalendar = Calendar.getInstance();
    private Calendar endDateCalendar = Calendar.getInstance();

    public StatisticsFragment() {
        // Required empty public constructor
    }

    public static StatisticsFragment newInstance(int statisticType) {
        StatisticsFragment fragment = new StatisticsFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_STATISTIC_TYPE, statisticType);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            statisticType = getArguments().getInt(ARG_STATISTIC_TYPE);
        }
        databaseHelper = DatabaseHelper.getInstance(getContext());
        if (databaseHelper == null) {
            Log.e("StatisticsFragment", "DatabaseHelper is null");
            return;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_statistics, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        initViews(view);
        setupRecyclerView();
        setupFilterVisibility();
        setupListeners();
        
        // Set default dates (current month)
        startDateCalendar.set(Calendar.DAY_OF_MONTH, 1);
        updateDateDisplay();
        
        // Load initial data
        loadStatistics();
    }
    
    private void initViews(View view) {
        recyclerViewStatistics = view.findViewById(R.id.recyclerViewStatistics);
        textViewEmpty = view.findViewById(R.id.textViewEmpty);
        progressBar = view.findViewById(R.id.progressBar);
        layoutDateFilter = view.findViewById(R.id.layoutDateFilter);
        layoutLimitFilter = view.findViewById(R.id.layoutLimitFilter);
        editTextStartDate = view.findViewById(R.id.editTextStartDate);
        editTextEndDate = view.findViewById(R.id.editTextEndDate);
        editTextLimit = view.findViewById(R.id.editTextLimit);
        buttonApply = view.findViewById(R.id.buttonApply);
    }
    
    private void setupRecyclerView() {
        recyclerViewStatistics.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new StatisticsAdapter(getContext(), statisticsList, statisticType == STATS_TYPE_REVENUE);
        recyclerViewStatistics.setAdapter(adapter);
    }
    
    private void setupFilterVisibility() {
        switch (statisticType) {
            case STATS_TYPE_REVENUE:
            case STATS_TYPE_MENU_BY_DATE:
                layoutDateFilter.setVisibility(View.VISIBLE);
                layoutLimitFilter.setVisibility(View.GONE);
                break;
            case STATS_TYPE_TOP_FOODS:
                layoutDateFilter.setVisibility(View.GONE);
                layoutLimitFilter.setVisibility(View.VISIBLE);
                break;
            default:
                layoutDateFilter.setVisibility(View.GONE);
                layoutLimitFilter.setVisibility(View.GONE);
                break;
        }
    }
    
    private void setupListeners() {
        editTextStartDate.setOnClickListener(v -> showDatePickerDialog(true));
        editTextEndDate.setOnClickListener(v -> showDatePickerDialog(false));
        buttonApply.setOnClickListener(v -> loadStatistics());
    }
    
    private void showDatePickerDialog(boolean isStartDate) {
        Calendar calendar = isStartDate ? startDateCalendar : endDateCalendar;
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                getContext(),
                (view, year, month, dayOfMonth) -> {
                    if (isStartDate) {
                        startDateCalendar.set(year, month, dayOfMonth);
                    } else {
                        endDateCalendar.set(year, month, dayOfMonth);
                    }
                    updateDateDisplay();
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }
    
    private void updateDateDisplay() {
        editTextStartDate.setText(dateFormat.format(startDateCalendar.getTime()));
        editTextEndDate.setText(dateFormat.format(endDateCalendar.getTime()));
    }
    
    public void loadStatistics() {
        progressBar.setVisibility(View.VISIBLE);
        textViewEmpty.setVisibility(View.GONE);
        
        // Use a separate thread for database operations
        new Thread(() -> {
            List<StatisticItem> result = new ArrayList<>();
            
            switch (statisticType) {
                case STATS_TYPE_FOOD_CATEGORY:
                    result = databaseHelper.getFoodCountByCategory();
                    break;
                case STATS_TYPE_REVENUE:
                    String startDate = editTextStartDate.getText().toString();
                    String endDate = editTextEndDate.getText().toString();
                    result = databaseHelper.getRevenueByDate(startDate, endDate);
                    break;
                case STATS_TYPE_TOP_FOODS:
                    int limit = 10;
                    try {
                        limit = Integer.parseInt(editTextLimit.getText().toString());
                    } catch (NumberFormatException e) {
                        // Use default value
                    }
                    result = databaseHelper.getTopFoods(limit);
                    break;
                case STATS_TYPE_TABLE_STATUS:
                    result = databaseHelper.getTableCountByStatus();
                    break;
                case STATS_TYPE_MENU_BY_DATE:
                    String startDateMenu = editTextStartDate.getText().toString();
                    String endDateMenu = editTextEndDate.getText().toString();
                    result = databaseHelper.getDailyMenuCountByDate(startDateMenu, endDateMenu);
                    break;
            }
            
            List<StatisticItem> finalResult = result;
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    if (finalResult.isEmpty()) {
                        textViewEmpty.setVisibility(View.VISIBLE);
                        textViewEmpty.setText(R.string.no_statistics);
                        recyclerViewStatistics.setVisibility(View.GONE);
                    } else {
                        textViewEmpty.setVisibility(View.GONE);
                        recyclerViewStatistics.setVisibility(View.VISIBLE);
                        statisticsList.clear();
                        statisticsList.addAll(finalResult);
                        adapter.notifyDataSetChanged();
                    }
                });
            }
        }).start();
    }

    /**
     * Lấy danh sách thống kê hiện tại
     * @return Danh sách thống kê
     */
    public List<StatisticItem> getStatisticsList() {
        return statisticsList;
    }
    
    /**
     * Làm mới dữ liệu thống kê
     */
    public void refreshData() {
        loadStatistics();
    }
} 