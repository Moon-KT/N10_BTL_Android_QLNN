package com.example.qlnhahangsesan.fragment;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;
import android.widget.ArrayAdapter;
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
    
    // Constants for time range
    public static final int TIME_RANGE_ALL = 0;
    public static final int TIME_RANGE_TODAY = 1;
    public static final int TIME_RANGE_THIS_WEEK = 2;
    public static final int TIME_RANGE_THIS_MONTH = 3;
    public static final int TIME_RANGE_THIS_YEAR = 4;
    public static final int TIME_RANGE_CUSTOM = 5;
    
    private int statisticType;
    private DatabaseHelper databaseHelper;
    
    private RecyclerView recyclerViewStatistics;
    private TextView textViewEmpty;
    private ProgressBar progressBar;
    private LinearLayout layoutDateFilter;
    private LinearLayout layoutLimitFilter;
    private LinearLayout layoutTimeFilterTopFoods;
    private LinearLayout layoutDateRangeTopFoods;
    private TextInputEditText editTextStartDate;
    private TextInputEditText editTextEndDate;
    private TextInputEditText editTextLimit;
    private TextInputEditText editTextStartDateTopFoods;
    private TextInputEditText editTextEndDateTopFoods;
    private AutoCompleteTextView spinnerTimeRange;
    private Button buttonApply;
    private Button buttonApplyDateFilter;
    
    private StatisticsAdapter adapter;
    private List<StatisticItem> statisticsList = new ArrayList<>();
    
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    private SimpleDateFormat displayDateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
    private Calendar startDateCalendar = Calendar.getInstance();
    private Calendar endDateCalendar = Calendar.getInstance();
    private Calendar startDateTopFoodsCalendar = Calendar.getInstance();
    private Calendar endDateTopFoodsCalendar = Calendar.getInstance();
    
    private int selectedTimeRange = TIME_RANGE_ALL;

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
        layoutTimeFilterTopFoods = view.findViewById(R.id.layoutTimeFilterTopFoods);
        layoutDateRangeTopFoods = view.findViewById(R.id.layoutDateRangeTopFoods);
        editTextStartDate = view.findViewById(R.id.editTextStartDate);
        editTextEndDate = view.findViewById(R.id.editTextEndDate);
        editTextLimit = view.findViewById(R.id.editTextLimit);
        editTextStartDateTopFoods = view.findViewById(R.id.editTextStartDateTopFoods);
        editTextEndDateTopFoods = view.findViewById(R.id.editTextEndDateTopFoods);
        spinnerTimeRange = view.findViewById(R.id.spinnerTimeRange);
        buttonApply = view.findViewById(R.id.buttonApply);
        buttonApplyDateFilter = view.findViewById(R.id.buttonApplyDateFilter);
        
        // Thiết lập spinner cho khoảng thời gian
        String[] timeRangeOptions = {
                "Tất cả thời gian", 
                "Hôm nay",
                "Tuần này", 
                "Tháng này", 
                "Năm nay", 
                "Tùy chỉnh..."
        };
        
        ArrayAdapter<String> timeRangeAdapter = new ArrayAdapter<>(
                getContext(),
                android.R.layout.simple_dropdown_item_1line,
                timeRangeOptions
        );
        
        spinnerTimeRange.setAdapter(timeRangeAdapter);
        
        // Mặc định: Tất cả thời gian
        spinnerTimeRange.setText(timeRangeOptions[TIME_RANGE_ALL], false);
    }
    
    private void setupRecyclerView() {
        recyclerViewStatistics.setLayoutManager(new LinearLayoutManager(getContext()));
        boolean isMonetary = (statisticType == STATS_TYPE_REVENUE );
        adapter = new StatisticsAdapter(getContext(), statisticsList, isMonetary, statisticType);
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
        
        // Thêm xử lý cho nút Áp dụng lọc ngày
        if (buttonApplyDateFilter != null) {
            buttonApplyDateFilter.setOnClickListener(v -> {
                // Làm nổi bật nút khi được nhấn
                buttonApplyDateFilter.setBackgroundTintList(android.content.res.ColorStateList.valueOf(
                        getResources().getColor(android.R.color.holo_blue_dark)));
                
                // Tải lại dữ liệu với khoảng ngày đã chọn
                loadStatistics();
                
                // Đổi lại màu sau 300ms
                buttonApplyDateFilter.postDelayed(() -> {
                    buttonApplyDateFilter.setBackgroundTintList(android.content.res.ColorStateList.valueOf(
                            getResources().getColor(android.R.color.holo_blue_light)));
                }, 300);
            });
        }
        
        // Xử lý sự kiện chọn khoảng thời gian
        spinnerTimeRange.setOnItemClickListener((parent, view, position, id) -> {
            selectedTimeRange = position;
            
            // Hiển thị bộ chọn khoảng thời gian tùy chỉnh nếu chọn "Tùy chỉnh..."
            if (position == TIME_RANGE_CUSTOM) {
                layoutDateRangeTopFoods.setVisibility(View.VISIBLE);
                
                // Khởi tạo giá trị mặc định cho khoảng thời gian
                if (editTextStartDateTopFoods.getText().toString().isEmpty()) {
                    // Mặc định: từ đầu tháng hiện tại đến hiện tại
                    Calendar startCal = Calendar.getInstance();
                    startCal.set(Calendar.DAY_OF_MONTH, 1);
                    startDateTopFoodsCalendar = startCal;
                    endDateTopFoodsCalendar = Calendar.getInstance();
                    updateTopFoodsDateDisplay();
                }
                
                // Thiết lập sự kiện click để chọn ngày
                editTextStartDateTopFoods.setOnClickListener(v -> showTopFoodsDatePickerDialog(true));
                editTextEndDateTopFoods.setOnClickListener(v -> showTopFoodsDatePickerDialog(false));
            } else {
                layoutDateRangeTopFoods.setVisibility(View.GONE);
                
                // Cập nhật khoảng thời gian dựa vào lựa chọn
                updateTimeRangeBasedOnSelection(position);
            }
            
            // Tải lại dữ liệu sau khi chọn khoảng thời gian
            loadStatistics();
        });
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
                    
                    // Làm nổi bật nút áp dụng sau khi chọn ngày
                    if (buttonApplyDateFilter != null) {
                        buttonApplyDateFilter.setBackgroundTintList(android.content.res.ColorStateList.valueOf(
                                getResources().getColor(android.R.color.holo_blue_bright)));
                    }
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
    
    /**
     * Hiển thị dialog chọn ngày cho bộ lọc top món ăn
     */
    private void showTopFoodsDatePickerDialog(boolean isStartDate) {
        Calendar calendar = isStartDate ? startDateTopFoodsCalendar : endDateTopFoodsCalendar;
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                getContext(),
                (view, year, month, dayOfMonth) -> {
                    if (isStartDate) {
                        startDateTopFoodsCalendar.set(year, month, dayOfMonth);
                    } else {
                        endDateTopFoodsCalendar.set(year, month, dayOfMonth);
                    }
                    updateTopFoodsDateDisplay();
                    
                    // Tải lại danh sách sau khi chọn ngày
                    loadStatistics();
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }
    
    /**
     * Cập nhật hiển thị ngày cho bộ lọc top món ăn
     */
    private void updateTopFoodsDateDisplay() {
        editTextStartDateTopFoods.setText(displayDateFormat.format(startDateTopFoodsCalendar.getTime()));
        editTextEndDateTopFoods.setText(displayDateFormat.format(endDateTopFoodsCalendar.getTime()));
    }
    
    /**
     * Cập nhật khoảng thời gian dựa vào lựa chọn từ spinner
     */
    private void updateTimeRangeBasedOnSelection(int position) {
        Calendar startCal = Calendar.getInstance();
        Calendar endCal = Calendar.getInstance();
        
        switch (position) {
            case TIME_RANGE_TODAY:
                // Hôm nay: Từ 0h đến 23h59m59s
                startCal.set(Calendar.HOUR_OF_DAY, 0);
                startCal.set(Calendar.MINUTE, 0);
                startCal.set(Calendar.SECOND, 0);
                
                endCal.set(Calendar.HOUR_OF_DAY, 23);
                endCal.set(Calendar.MINUTE, 59);
                endCal.set(Calendar.SECOND, 59);
                break;
                
            case TIME_RANGE_THIS_WEEK:
                // Tuần này: Từ thứ 2 đến hiện tại
                startCal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
                break;
                
            case TIME_RANGE_THIS_MONTH:
                // Tháng này: Từ ngày 1 đến hiện tại
                startCal.set(Calendar.DAY_OF_MONTH, 1);
                break;
                
            case TIME_RANGE_THIS_YEAR:
                // Năm này: Từ ngày 1/1 đến hiện tại
                startCal.set(Calendar.DAY_OF_YEAR, 1);
                break;
                
            case TIME_RANGE_ALL:
            default:
                // Mặc định không đặt giới hạn thời gian
                startCal = null;
                endCal = null;
                break;
        }
        
        startDateTopFoodsCalendar = startCal;
        endDateTopFoodsCalendar = endCal;
    }
    
    public void loadStatistics() {
        progressBar.setVisibility(View.VISIBLE);
        textViewEmpty.setVisibility(View.GONE);
        
        // Use a separate thread for database operations
        new Thread(() -> {
            List<StatisticItem> result = new ArrayList<>();
            
            switch (statisticType) {
                case STATS_TYPE_FOOD_CATEGORY:
                    result = databaseHelper.getAllCategoryStatistics();
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
                    
                    // Nếu không phải lọc tất cả thời gian, truyền thêm khoảng thời gian
                    if (selectedTimeRange != TIME_RANGE_ALL) {
                        String startDateTopFoods = null;
                        String endDateTopFoods = null;
                        
                        if (selectedTimeRange == TIME_RANGE_CUSTOM) {
                            // Lấy từ ngày từ UI khi chọn tùy chỉnh
                            if (startDateTopFoodsCalendar != null) {
                                startDateTopFoods = dateFormat.format(startDateTopFoodsCalendar.getTime());
                            }
                            if (endDateTopFoodsCalendar != null) {
                                endDateTopFoods = dateFormat.format(endDateTopFoodsCalendar.getTime());
                            }
                        } else {
                            // Lấy từ ngày từ cài đặt theo lựa chọn spinner
                            if (startDateTopFoodsCalendar != null) {
                                startDateTopFoods = dateFormat.format(startDateTopFoodsCalendar.getTime());
                            }
                            if (endDateTopFoodsCalendar != null) {
                                endDateTopFoods = dateFormat.format(endDateTopFoodsCalendar.getTime());
                            }
                        }
                        
                        // Gọi phương thức có tham số thời gian
                        result = databaseHelper.getTopFoodsByDateRange(limit, startDateTopFoods, endDateTopFoods);
                    } else {
                        // Sử dụng phương thức hiện có cho tất cả thời gian
                        result = databaseHelper.getTopFoods(limit);
                    }
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
                        recyclerViewStatistics.setVisibility(View.GONE);
                        
                        // Hiển thị thông báo cụ thể cho từng loại thống kê
                        switch (statisticType) {
                            case STATS_TYPE_FOOD_CATEGORY:
                                textViewEmpty.setText("Không có dữ liệu về danh mục món ăn.\nHãy thêm món ăn vào các danh mục để xem thống kê.");
                                break;
                            case STATS_TYPE_REVENUE:
                                textViewEmpty.setText("Không có dữ liệu doanh thu trong khoảng thời gian đã chọn.\nHãy thực hiện một số đơn hàng để xem thống kê.");
                                break;
                            case STATS_TYPE_TOP_FOODS:
                                textViewEmpty.setText("Không có dữ liệu về các món ăn phổ biến.\nHãy thực hiện một số đơn hàng để xem thống kê.");
                                break;
                            case STATS_TYPE_TABLE_STATUS:
                                textViewEmpty.setText("Không có dữ liệu về trạng thái bàn.\nHãy thêm một số bàn để xem thống kê.");
                                break;
                            case STATS_TYPE_MENU_BY_DATE:
                                textViewEmpty.setText("Không có dữ liệu về menu hàng ngày trong khoảng thời gian đã chọn.\nHãy thêm món ăn vào menu hàng ngày để xem thống kê.");
                                break;
                            default:
                                textViewEmpty.setText(R.string.no_statistics);
                                break;
                        }
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

    /**
     * Lấy giá trị limit hiện tại cho top món ăn phổ biến
     * @return Số lượng món ăn hiển thị trong top
     */
    public int getTopFoodsLimit() {
        if (editTextLimit != null && editTextLimit.getText() != null && !editTextLimit.getText().toString().isEmpty()) {
            try {
                // Lấy giá trị từ EditText và đảm bảo nó là số dương
                int limit = Integer.parseInt(editTextLimit.getText().toString());
                if (limit > 0) {
                    return limit;
                }
            } catch (NumberFormatException e) {
                Log.e("StatisticsFragment", "Error parsing limit value: " + e.getMessage());
            }
        }
        // Giá trị mặc định
        return 10;
    }
    
    /**
     * Lấy thông tin khoảng thời gian đã chọn cho top món ăn phổ biến
     * @return Mảng 2 phần tử [startDate, endDate] hoặc null nếu không có lọc thời gian
     */
    public String[] getTopFoodsDateRange() {
        if (selectedTimeRange == TIME_RANGE_ALL) {
            return null; // Trả về null nếu chọn "Tất cả thời gian"
        }
        
        String startDate = null;
        String endDate = null;
        
        // Nếu chọn tùy chỉnh, lấy giá trị từ trường nhập liệu
        if (selectedTimeRange == TIME_RANGE_CUSTOM) {
            if (startDateTopFoodsCalendar != null) {
                startDate = dateFormat.format(startDateTopFoodsCalendar.getTime());
            }
            if (endDateTopFoodsCalendar != null) {
                endDate = dateFormat.format(endDateTopFoodsCalendar.getTime());
            }
        } else {
            // Lấy giá trị từ lựa chọn spinner đã được tính toán trước đó
            if (startDateTopFoodsCalendar != null) {
                startDate = dateFormat.format(startDateTopFoodsCalendar.getTime());
            }
            if (endDateTopFoodsCalendar != null) {
                endDate = dateFormat.format(endDateTopFoodsCalendar.getTime());
            }
        }
        
        return new String[] {startDate, endDate};
    }
} 