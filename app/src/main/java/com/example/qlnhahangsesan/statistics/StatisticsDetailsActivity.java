package com.example.qlnhahangsesan.statistics;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.qlnhahangsesan.R;
import com.example.qlnhahangsesan.database.DatabaseHelper;
import com.example.qlnhahangsesan.model.StatisticItem;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class StatisticsDetailsActivity extends AppCompatActivity {
    private DatabaseHelper databaseHelper;
    private TextView textViewTitle, textViewDescription;
    private PieChart pieChart;
    private BarChart barChart;
    private LineChart lineChart;
    private LinearLayout layoutDateFilter;
    private com.google.android.material.textfield.TextInputEditText editTextStartDate, editTextEndDate;
    private Button buttonApplyDateFilter;
    
    // Calendar for date range selection
    private Calendar startDateCalendar = Calendar.getInstance();
    private Calendar endDateCalendar = Calendar.getInstance();
    
    private static final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
    private static final SimpleDateFormat dbDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    // Constants for statistic types
    public static final String STATS_TYPE = "stats_type";
    public static final String STATS_LIMIT = "stats_limit";
    public static final String STATS_START_DATE = "stats_start_date";
    public static final String STATS_END_DATE = "stats_end_date";
    public static final int STATS_DEMO_DATA = 0;
    public static final int STATS_FOOD_BY_CATEGORY = 1;
    public static final int STATS_REVENUE_BY_DATE = 2;
    public static final int STATS_TOP_FOODS = 3;
    public static final int STATS_TABLE_BY_STATUS = 4; 
    public static final int STATS_MENU_BY_DATE = 5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistics_details);

        // Initialize views
        textViewTitle = findViewById(R.id.textViewStatsTitle);
        textViewDescription = findViewById(R.id.textViewStatsDescription);
        pieChart = findViewById(R.id.pieChart);
        barChart = findViewById(R.id.barChart);
        lineChart = findViewById(R.id.lineChart);
        
        // Initialize date filter components
        layoutDateFilter = findViewById(R.id.layoutDateFilter);
        editTextStartDate = findViewById(R.id.editTextStartDate);
        editTextEndDate = findViewById(R.id.editTextEndDate);
        buttonApplyDateFilter = findViewById(R.id.buttonApplyDateFilter);
        
        // Set up date selection listeners
        editTextStartDate.setOnClickListener(v -> showDatePickerDialog(true));
        editTextEndDate.setOnClickListener(v -> showDatePickerDialog(false));
        
        // Set up apply button listener
        buttonApplyDateFilter.setOnClickListener(v -> {
            // Get selected date range and reload data
            String startDate = dbDateFormat.format(startDateCalendar.getTime());
            String endDate = dbDateFormat.format(endDateCalendar.getTime());
            
            // Get current statistics type from title
            String currentTitle = textViewTitle.getText().toString();
            
            // Apply filter based on current statistic type
            if (currentTitle.contains("doanh thu")) {
                loadRevenueForDateRange(startDate, endDate);
            } else if (currentTitle.contains("menu")) {
                loadMenuDataForDateRange(startDate, endDate);
            }
        });
        
        // Set default dates - start date is 30 days ago, end date is today
        startDateCalendar.add(Calendar.DAY_OF_MONTH, -30);
        updateDateDisplay();

        // Set up action bar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Chi tiết thống kê");
        }

        // Initialize database helper
        databaseHelper = DatabaseHelper.getInstance(this);

        // Get statistics type from intent
        int statsType = getIntent().getIntExtra(STATS_TYPE, STATS_DEMO_DATA);
        loadStatistics(statsType);
    }

    private void loadStatistics(int statsType) {
        switch (statsType) {
            case STATS_FOOD_BY_CATEGORY:
                showFoodByCategory();
                break;
            case STATS_REVENUE_BY_DATE:
                showRevenueByDate();
                break;
            case STATS_TOP_FOODS:
                showTopFoods();
                break;
            case STATS_TABLE_BY_STATUS:
                showTableByStatus();
                break;
            case STATS_MENU_BY_DATE:
                showMenuByDate();
                break;
            case STATS_DEMO_DATA:
                // Show demo data when specifically requested
                showDemoData();
                break;
            default:
                // Show no data message
                showNoDataMessage();
                break;
        }
    }

    private void showFoodByCategory() {
        textViewTitle.setText("Thống kê món ăn theo danh mục");
        textViewDescription.setText("Biểu đồ thể hiện phần trăm số lượng món ăn trong mỗi danh mục");
        
        // Hide date filter and other charts
        layoutDateFilter.setVisibility(View.GONE);
        pieChart.setVisibility(View.VISIBLE);
        barChart.setVisibility(View.GONE);
        lineChart.setVisibility(View.GONE);
        
        List<StatisticItem> statistics = databaseHelper.getAllCategoryStatistics();
        
        // If no data, show message
        if (statistics.isEmpty()) {
            pieChart.setVisibility(View.GONE);
            textViewDescription.setText("Không có dữ liệu về các danh mục món ăn. Hãy thêm một số món ăn để xem thống kê.");
            return;
        }
        
        List<PieEntry> entries = new ArrayList<>();
        for (StatisticItem item : statistics) {
            // Only add categories that have foods (value > 0)
            if (item.getValue() > 0) {
                entries.add(new PieEntry((float) item.getValue(), item.getName()));
            }
        }
        
        // If no entries with values > 0
        if (entries.isEmpty()) {
            pieChart.setVisibility(View.GONE);
            textViewDescription.setText("Không có dữ liệu về các danh mục món ăn. Hãy thêm một số món ăn để xem thống kê.");
            return;
        }
        
        PieDataSet dataSet = new PieDataSet(entries, "Danh mục món ăn");
        dataSet.setColors(ColorTemplate.COLORFUL_COLORS);
        dataSet.setValueTextSize(14f);
        dataSet.setValueTextColor(Color.WHITE);
        
        PieData data = new PieData(dataSet);
        data.setValueFormatter(new PercentFormatter(pieChart));
        
        Description description = new Description();
        description.setText("");
        pieChart.setDescription(description);
        pieChart.setData(data);
        pieChart.setCenterText("Tổng số món: " + countTotalValue(statistics));
        pieChart.setCenterTextSize(16f);
        pieChart.setEntryLabelTextSize(14f);
        pieChart.setEntryLabelColor(Color.WHITE);
        pieChart.setUsePercentValues(true);
        pieChart.setHoleRadius(35f);
        pieChart.setTransparentCircleRadius(40f);
        pieChart.getLegend().setEnabled(true);
        pieChart.invalidate();
    }

    private void showRevenueByDate() {
        textViewTitle.setText("Thống kê doanh thu theo ngày");
        textViewDescription.setText("Biểu đồ thể hiện doanh thu theo ngày\n(Nhấn vào điểm trên biểu đồ để xem chi tiết đơn hàng)");
        
        // Show date filter and hide other charts
        layoutDateFilter.setVisibility(View.VISIBLE);
        pieChart.setVisibility(View.GONE);
        barChart.setVisibility(View.GONE);
        lineChart.setVisibility(View.VISIBLE);
        
        // Get selected date range from date pickers
        String startDate = dbDateFormat.format(startDateCalendar.getTime());
        String endDate = dbDateFormat.format(endDateCalendar.getTime());
        
        // Load data with current date range
        loadRevenueForDateRange(startDate, endDate);
    }

    // Helper method to load revenue data for a specific date range
    private void loadRevenueForDateRange(String startDate, String endDate) {
        final List<StatisticItem> statistics = databaseHelper.getRevenueByDate(startDate, endDate);
        
        // If no data, show message
        if (statistics.isEmpty()) {
            lineChart.setVisibility(View.GONE);
            textViewDescription.setText("Không có dữ liệu doanh thu cho khoảng thời gian đã chọn. Hãy thực hiện một số đơn hàng để xem thống kê.");
            return;
        }
        
        // Show chart if there is data
        lineChart.setVisibility(View.VISIBLE);
        setupLineChart(statistics);
    }

    // Show date picker dialog for date selection
    private void showDatePickerDialog(boolean isStartDate) {
        Calendar calendar = isStartDate ? startDateCalendar : endDateCalendar;
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    if (isStartDate) {
                        startDateCalendar.set(year, month, dayOfMonth);
                    } else {
                        endDateCalendar.set(year, month, dayOfMonth);
                    }
                    updateDateDisplay();
                    
                    // Hiển thị nút Áp dụng và làm nổi bật để người dùng biết cần nhấn để cập nhật
                    if (buttonApplyDateFilter != null) {
                        buttonApplyDateFilter.setEnabled(true);
                        // Làm nổi bật nút
                        buttonApplyDateFilter.setBackgroundTintList(ColorStateList.valueOf(
                                getResources().getColor(android.R.color.holo_blue_bright)));
                    }

                    // Hiển thị thông báo nhỏ hướng dẫn người dùng
                    Toast.makeText(this, "Nhấn nút Áp dụng để cập nhật dữ liệu", Toast.LENGTH_SHORT).show();
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }
    
    // Update the date display in text fields
    private void updateDateDisplay() {
        editTextStartDate.setText(dateFormat.format(startDateCalendar.getTime()));
        editTextEndDate.setText(dateFormat.format(endDateCalendar.getTime()));
    }

    private void showTopFoods() {
        // Lấy limit từ Intent, mặc định là 10 nếu không có
        int limit = getIntent().getIntExtra(STATS_LIMIT, 10);
        
        // Lấy thông tin khoảng thời gian (nếu có)
        String startDate = getIntent().getStringExtra(STATS_START_DATE);
        String endDate = getIntent().getStringExtra(STATS_END_DATE);
        
        // Debug log để xác nhận giá trị nhận được
        Log.d("StatisticsDetailsActivity", "Nhận limit = " + limit + " từ Intent");
        Log.d("StatisticsDetailsActivity", "Khoảng thời gian: " + startDate + " đến " + endDate);
        
        // Tạo tiêu đề dựa vào thông tin lọc
        StringBuilder titleBuilder = new StringBuilder("Top " + limit + " món ăn bán chạy nhất");
        if (startDate != null && endDate != null) {
            try {
                Date startDateObj = dbDateFormat.parse(startDate);
                Date endDateObj = dbDateFormat.parse(endDate);
                
                titleBuilder.append(" từ ")
                        .append(dateFormat.format(startDateObj))
                        .append(" đến ")
                        .append(dateFormat.format(endDateObj));
            } catch (Exception e) {
                Log.e("StatisticsDetailsActivity", "Lỗi parse date", e);
            }
        } else if (startDate != null) {
            try {
                Date startDateObj = dbDateFormat.parse(startDate);
                titleBuilder.append(" từ ").append(dateFormat.format(startDateObj));
            } catch (Exception e) {
                Log.e("StatisticsDetailsActivity", "Lỗi parse date", e);
            }
        } else if (endDate != null) {
            try {
                Date endDateObj = dbDateFormat.parse(endDate);
                titleBuilder.append(" đến ").append(dateFormat.format(endDateObj));
            } catch (Exception e) {
                Log.e("StatisticsDetailsActivity", "Lỗi parse date", e);
            }
        }
        
        textViewTitle.setText(titleBuilder.toString());
        textViewDescription.setText("Biểu đồ thể hiện số lượng bán ra của các món ăn phổ biến nhất");
        
        // Hide date filter and other charts
        layoutDateFilter.setVisibility(View.GONE);
        pieChart.setVisibility(View.GONE);
        barChart.setVisibility(View.VISIBLE);
        lineChart.setVisibility(View.GONE);
        
        // Lấy dữ liệu theo khoảng thời gian (nếu có)
        List<StatisticItem> statistics;
        if (startDate != null || endDate != null) {
            statistics = databaseHelper.getTopFoodsByDateRange(limit, startDate, endDate);
        } else {
            statistics = databaseHelper.getTopFoods(limit);
        }
        
        // If no data, show message instead of demo data
        if (statistics.isEmpty()) {
            barChart.setVisibility(View.GONE);
            textViewDescription.setText("Không có dữ liệu về các món ăn phổ biến. Hãy thực hiện một số đơn hàng để xem thống kê.");
            return;
        }
        
        List<BarEntry> entries = new ArrayList<>();
        List<String> xAxisLabels = new ArrayList<>();
        
        for (int i = 0; i < statistics.size(); i++) {
            StatisticItem item = statistics.get(i);
            entries.add(new BarEntry(i, (float) item.getValue()));
            xAxisLabels.add(item.getName());
        }
        
        BarDataSet dataSet = new BarDataSet(entries, "Số lượng bán ra");
        dataSet.setColors(ColorTemplate.MATERIAL_COLORS);
        dataSet.setValueTextSize(12f);
        
        BarData barData = new BarData(dataSet);
        barData.setBarWidth(0.7f);
        
        Description description = new Description();
        description.setText("");
        barChart.setDescription(description);
        barChart.setData(barData);
        
        // Customize X Axis to show food names
        XAxis xAxis = barChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setValueFormatter(new IndexAxisValueFormatter(xAxisLabels));
        xAxis.setGranularity(1f);
        xAxis.setLabelRotationAngle(45f);
        xAxis.setLabelCount(statistics.size());
        
        barChart.getAxisRight().setEnabled(false);
        barChart.setFitBars(true);
        barChart.getLegend().setEnabled(true);
        barChart.invalidate();
    }

    private void showTableByStatus() {
        textViewTitle.setText("Thống kê bàn theo trạng thái");
        textViewDescription.setText("Biểu đồ thể hiện tỷ lệ bàn theo từng trạng thái");
        
        // Hide date filter and other charts
        layoutDateFilter.setVisibility(View.GONE);
        pieChart.setVisibility(View.VISIBLE);
        barChart.setVisibility(View.GONE);
        lineChart.setVisibility(View.GONE);
        
        List<StatisticItem> statistics = databaseHelper.getTableCountByStatus();
        
        // If no data, show message instead of demo data
        if (statistics.isEmpty()) {
            pieChart.setVisibility(View.GONE);
            textViewDescription.setText("Không có dữ liệu về trạng thái bàn. Hãy thêm một số bàn để xem thống kê.");
            return;
        }
        
        List<PieEntry> entries = new ArrayList<>();
        for (StatisticItem item : statistics) {
            entries.add(new PieEntry((float) item.getValue(), item.getName()));
        }
        
        PieDataSet dataSet = new PieDataSet(entries, "Trạng thái bàn");
        
        // Set colors for different statuses
        ArrayList<Integer> colors = new ArrayList<>();
        for (StatisticItem item : statistics) {
            if (item.getName().equals("Trống")) {
                colors.add(Color.GREEN);
            } else if (item.getName().equals("Đang phục vụ")) {
                colors.add(Color.RED);
            } else if (item.getName().equals("Đã đặt")) {
                colors.add(Color.YELLOW);
            } else {
                colors.add(ColorTemplate.COLORFUL_COLORS[colors.size() % 5]);
            }
        }
        
        dataSet.setColors(colors);
        dataSet.setValueTextSize(14f);
        dataSet.setValueTextColor(Color.WHITE);
        
        PieData data = new PieData(dataSet);
        data.setValueFormatter(new PercentFormatter(pieChart));
        
        Description description = new Description();
        description.setText("");
        pieChart.setDescription(description);
        pieChart.setData(data);
        pieChart.setCenterText("Tổng số bàn: " + countTotalValue(statistics));
        pieChart.setCenterTextSize(16f);
        pieChart.setEntryLabelTextSize(14f);
        pieChart.setEntryLabelColor(Color.WHITE);
        pieChart.setUsePercentValues(true);
        pieChart.setHoleRadius(35f);
        pieChart.setTransparentCircleRadius(40f);
        pieChart.getLegend().setEnabled(true);
        pieChart.invalidate();
    }

    private void showMenuByDate() {
        textViewTitle.setText("Thống kê số lượng món ăn trong menu");
        textViewDescription.setText("Biểu đồ thể hiện số lượng món ăn trong menu theo ngày");
        
        // Show date filter and hide other charts
        layoutDateFilter.setVisibility(View.VISIBLE);
        pieChart.setVisibility(View.GONE);
        barChart.setVisibility(View.VISIBLE);
        lineChart.setVisibility(View.GONE);
        
        // Get selected date range from date pickers
        String startDate = dbDateFormat.format(startDateCalendar.getTime());
        String endDate = dbDateFormat.format(endDateCalendar.getTime());
        
        // Load data with current date range
        loadMenuDataForDateRange(startDate, endDate);
    }
    
    // Helper method to load menu data for a specific date range
    private void loadMenuDataForDateRange(String startDate, String endDate) {
        List<StatisticItem> statistics = databaseHelper.getDailyMenuCountByDate(startDate, endDate);
        
        // If no data, show message
        if (statistics.isEmpty()) {
            barChart.setVisibility(View.GONE);
            textViewDescription.setText("Không có dữ liệu về menu hàng ngày trong khoảng thời gian đã chọn. Hãy thêm một số món vào menu hàng ngày để xem thống kê.");
            return;
        }
        
        // Show barChart if there is data
        barChart.setVisibility(View.VISIBLE);
        
        List<BarEntry> entries = new ArrayList<>();
        List<String> xAxisLabels = new ArrayList<>();
        
        for (int i = 0; i < statistics.size(); i++) {
            StatisticItem item = statistics.get(i);
            entries.add(new BarEntry(i, (float) item.getValue()));
            
            // Convert database date format to display format
            try {
                Date date = dbDateFormat.parse(item.getName());
                xAxisLabels.add(dateFormat.format(date));
            } catch (Exception e) {
                xAxisLabels.add(item.getName());
            }
        }
        
        BarDataSet dataSet = new BarDataSet(entries, "Số lượng món ăn");
        dataSet.setColors(ColorTemplate.JOYFUL_COLORS);
        dataSet.setValueTextSize(12f);
        
        BarData barData = new BarData(dataSet);
        barData.setBarWidth(0.7f);
        
        Description description = new Description();
        description.setText("");
        barChart.setDescription(description);
        barChart.setData(barData);
        
        // Customize X Axis to show date labels
        XAxis xAxis = barChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setValueFormatter(new IndexAxisValueFormatter(xAxisLabels));
        xAxis.setGranularity(1f);
        xAxis.setLabelRotationAngle(45f);
        
        barChart.getAxisRight().setEnabled(false);
        barChart.setFitBars(true);
        barChart.getLegend().setEnabled(true);
        barChart.invalidate();
    }

    private void showDemoData() {
        textViewTitle.setText("Biểu đồ thống kê mẫu");
        textViewDescription.setText("Đây là biểu đồ mẫu với dữ liệu giả lập để minh họa. Không phải dữ liệu thực tế từ cơ sở dữ liệu.");
        
        // Hide date filter and show all charts for demo
        layoutDateFilter.setVisibility(View.GONE);
        pieChart.setVisibility(View.VISIBLE);
        barChart.setVisibility(View.VISIBLE);
        lineChart.setVisibility(View.VISIBLE);
        
        // Demo pie chart
        List<StatisticItem> pieData = createDemoFoodCategoryData();
        List<PieEntry> pieEntries = new ArrayList<>();
        for (StatisticItem item : pieData) {
            pieEntries.add(new PieEntry((float) item.getValue(), item.getName()));
        }
        
        PieDataSet pieDataSet = new PieDataSet(pieEntries, "Danh mục món ăn (DỮ LIỆU MẪU)");
        pieDataSet.setColors(ColorTemplate.COLORFUL_COLORS);
        pieDataSet.setValueTextSize(14f);
        pieDataSet.setValueTextColor(Color.WHITE);
        
        PieData pieChartData = new PieData(pieDataSet);
        pieChartData.setValueFormatter(new PercentFormatter(pieChart));
        
        Description pieDescription = new Description();
        pieDescription.setText("Dữ liệu mẫu");
        pieChart.setDescription(pieDescription);
        pieChart.setData(pieChartData);
        pieChart.setCenterText("DỮ LIỆU MẪU\nTổng số món: " + countTotalValue(pieData));
        pieChart.setCenterTextSize(16f);
        pieChart.setEntryLabelTextSize(14f);
        pieChart.setEntryLabelColor(Color.WHITE);
        pieChart.setUsePercentValues(true);
        pieChart.setHoleRadius(35f);
        pieChart.setTransparentCircleRadius(40f);
        pieChart.getLegend().setEnabled(true);
        pieChart.invalidate();
        
        // Demo bar chart
        List<StatisticItem> barData = createDemoTopFoodData();
        List<BarEntry> barEntries = new ArrayList<>();
        List<String> barXAxisLabels = new ArrayList<>();
        
        for (int i = 0; i < barData.size(); i++) {
            StatisticItem item = barData.get(i);
            barEntries.add(new BarEntry(i, (float) item.getValue()));
            barXAxisLabels.add(item.getName());
        }
        
        BarDataSet barDataSet = new BarDataSet(barEntries, "Số lượng bán ra (DỮ LIỆU MẪU)");
        barDataSet.setColors(ColorTemplate.MATERIAL_COLORS);
        barDataSet.setValueTextSize(12f);
        
        BarData barChartData = new BarData(barDataSet);
        barChartData.setBarWidth(0.7f);
        
        Description barDescription = new Description();
        barDescription.setText("Dữ liệu mẫu");
        barChart.setDescription(barDescription);
        barChart.setData(barChartData);
        
        XAxis barXAxis = barChart.getXAxis();
        barXAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        barXAxis.setValueFormatter(new IndexAxisValueFormatter(barXAxisLabels));
        barXAxis.setGranularity(1f);
        barXAxis.setLabelRotationAngle(45f);
        
        barChart.getAxisRight().setEnabled(false);
        barChart.setFitBars(true);
        barChart.getLegend().setEnabled(true);
        barChart.invalidate();
        
        // Demo line chart
        Calendar endCalendar = Calendar.getInstance();
        Calendar startCalendar = Calendar.getInstance();
        startCalendar.add(Calendar.DAY_OF_MONTH, -6);
        
        List<StatisticItem> lineData = createDemoRevenueData(startCalendar, endCalendar);
        List<Entry> lineEntries = new ArrayList<>();
        List<String> lineXAxisLabels = new ArrayList<>();
        
        for (int i = 0; i < lineData.size(); i++) {
            StatisticItem item = lineData.get(i);
            lineEntries.add(new Entry(i, (float) item.getValue()));
            lineXAxisLabels.add(item.getName());
        }
        
        LineDataSet lineDataSet = new LineDataSet(lineEntries, "Doanh thu (VND) - DỮ LIỆU MẪU");
        lineDataSet.setColor(Color.BLUE);
        lineDataSet.setValueTextColor(Color.BLACK);
        lineDataSet.setValueTextSize(12f);
        lineDataSet.setLineWidth(2f);
        lineDataSet.setCircleRadius(4f);
        lineDataSet.setCircleColor(Color.RED);
        lineDataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        
        LineData lineChartData = new LineData(lineDataSet);
        
        Description lineDescription = new Description();
        lineDescription.setText("Dữ liệu mẫu");
        lineChart.setDescription(lineDescription);
        lineChart.setData(lineChartData);
        
        XAxis lineXAxis = lineChart.getXAxis();
        lineXAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        lineXAxis.setValueFormatter(new IndexAxisValueFormatter(lineXAxisLabels));
        lineXAxis.setGranularity(1f);
        lineXAxis.setLabelRotationAngle(45f);
        
        lineChart.getAxisRight().setEnabled(false);
        lineChart.getLegend().setEnabled(true);
        lineChart.invalidate();
    }

    private void setupLineChart(List<StatisticItem> statistics) {
        if (statistics == null || statistics.isEmpty()) {
            lineChart.setVisibility(View.GONE);
            return;
        }
        
        lineChart.setVisibility(View.VISIBLE);
        
        // Format dates
        SimpleDateFormat dbDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM", Locale.getDefault());
        
        List<Entry> entries = new ArrayList<>();
        List<String> xAxisLabels = new ArrayList<>();
        List<String> originalDates = new ArrayList<>();
        
        for (int i = 0; i < statistics.size(); i++) {
            StatisticItem item = statistics.get(i);
            entries.add(new Entry(i, (float) item.getValue()));
            
            // Convert database date format to display format
            try {
                Date date = dbDateFormat.parse(item.getName());
                xAxisLabels.add(dateFormat.format(date));
                originalDates.add(item.getName()); // Store original date for click handling
            } catch (Exception e) {
                xAxisLabels.add(item.getName());
                originalDates.add(item.getName());
            }
        }
        
        LineDataSet dataSet = new LineDataSet(entries, "Doanh thu (VNĐ)");
        dataSet.setColor(Color.BLUE);
        dataSet.setValueTextColor(Color.BLACK);
        dataSet.setValueTextSize(12f);
        dataSet.setLineWidth(2f);
        dataSet.setCircleRadius(4f);
        dataSet.setCircleColor(Color.RED);
        dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        
        LineData lineData = new LineData(dataSet);
        
        Description description = new Description();
        description.setText("");
        lineChart.setDescription(description);
        lineChart.setData(lineData);
        
        // Customize X Axis to show date labels
        XAxis xAxis = lineChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setValueFormatter(new IndexAxisValueFormatter(xAxisLabels));
        xAxis.setGranularity(1f);
        xAxis.setLabelRotationAngle(45f);
        
        lineChart.getAxisRight().setEnabled(false);
        lineChart.getLegend().setEnabled(true);
        
        // Enable user interaction with the chart
        lineChart.setTouchEnabled(true);
        lineChart.setDragEnabled(true);
        lineChart.setScaleEnabled(true);
        lineChart.setPinchZoom(true);
        
        // Set up click listener for data points
        lineChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, Highlight h) {
                int index = (int) e.getX();
                Log.d("StatisticsDetailsActivity", "onValueSelected: Entry selected at index " + index + " with value " + e.getY());
                
                if (index >= 0 && index < originalDates.size()) {
                    String selectedDate = originalDates.get(index);
                    Log.d("StatisticsDetailsActivity", "onValueSelected: Selected date: " + selectedDate);
                    
                    // Create intent to open DayOrdersActivity
                    Intent intent = new Intent(StatisticsDetailsActivity.this, 
                            com.example.qlnhahangsesan.DayOrdersActivity.class);
                    intent.putExtra("date", selectedDate);
                    Log.d("StatisticsDetailsActivity", "onValueSelected: Starting DayOrdersActivity with date: " + selectedDate);
                    startActivity(intent);
                } else {
                    Log.e("StatisticsDetailsActivity", "onValueSelected: Index out of bounds - index: " + index + ", size: " + originalDates.size());
                }
            }

            @Override
            public void onNothingSelected() {
                // Do nothing
            }
        });
        
        lineChart.invalidate();
    }

    // Helper methods to create demo data
    private List<StatisticItem> createDemoFoodCategoryData() {
        List<StatisticItem> demoData = new ArrayList<>();
        demoData.add(new StatisticItem("Món khai vị", 12));
        demoData.add(new StatisticItem("Món chính", 25));
        demoData.add(new StatisticItem("Món tráng miệng", 8));
        demoData.add(new StatisticItem("Đồ uống", 15));
        demoData.add(new StatisticItem("Món đặc biệt", 5));
        return demoData;
    }

    private List<StatisticItem> createDemoRevenueData(Calendar startCalendar, Calendar endCalendar) {
        List<StatisticItem> demoData = new ArrayList<>();
        
        Calendar cal = (Calendar) startCalendar.clone();
        
        // Create demo revenue data for each day
        while (!cal.after(endCalendar)) {
            String date = dateFormat.format(cal.getTime());
            double revenue = 1000000 + Math.random() * 5000000; // Random revenue between 1M and 6M VND
            demoData.add(new StatisticItem(date, revenue));
            cal.add(Calendar.DAY_OF_MONTH, 1);
        }
        
        return demoData;
    }

    private List<StatisticItem> createDemoTopFoodData() {
        List<StatisticItem> demoData = new ArrayList<>();
        demoData.add(new StatisticItem("Cơm chiên hải sản", 85));
        demoData.add(new StatisticItem("Gà nướng muối ớt", 72));
        demoData.add(new StatisticItem("Bò lúc lắc", 68));
        demoData.add(new StatisticItem("Cá hồi nướng", 65));
        demoData.add(new StatisticItem("Lẩu thái hải sản", 60));
        demoData.add(new StatisticItem("Gỏi cuốn tôm thịt", 52));
        demoData.add(new StatisticItem("Nước ép cam", 45));
        demoData.add(new StatisticItem("Trái cây thập cẩm", 40));
        demoData.add(new StatisticItem("Chả giò hải sản", 38));
        demoData.add(new StatisticItem("Cà phê đen", 35));
        return demoData;
    }

    private List<StatisticItem> createDemoTableStatusData() {
        List<StatisticItem> demoData = new ArrayList<>();
        demoData.add(new StatisticItem("Trống", 15));
        demoData.add(new StatisticItem("Đang phục vụ", 8));
        demoData.add(new StatisticItem("Đã đặt", 5));
        return demoData;
    }

    private List<StatisticItem> createDemoMenuData(Calendar startCalendar, Calendar endCalendar) {
        List<StatisticItem> demoData = new ArrayList<>();
        
        Calendar cal = (Calendar) startCalendar.clone();
        
        // Create demo menu data for each day
        while (!cal.after(endCalendar)) {
            String date = dbDateFormat.format(cal.getTime());
            int menuCount = 8 + (int)(Math.random() * 10); // Random menu count between 8 and 17
            demoData.add(new StatisticItem(date, menuCount));
            cal.add(Calendar.DAY_OF_MONTH, 1);
        }
        
        return demoData;
    }

    private int countTotalValue(List<StatisticItem> items) {
        int total = 0;
        for (StatisticItem item : items) {
            total += item.getValue();
        }
        return total;
    }

    private void showNoDataMessage() {
        textViewTitle.setText("Không có dữ liệu thống kê");
        textViewDescription.setText("Không có dữ liệu thống kê phù hợp với yêu cầu của bạn.");
        
        // Hide date filter and all charts
        layoutDateFilter.setVisibility(View.GONE);
        pieChart.setVisibility(View.GONE);
        barChart.setVisibility(View.GONE);
        lineChart.setVisibility(View.GONE);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
} 