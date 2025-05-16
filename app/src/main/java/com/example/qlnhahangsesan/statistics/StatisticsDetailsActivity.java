package com.example.qlnhahangsesan.statistics;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

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
    private static final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
    private static final SimpleDateFormat dbDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    // Constants for statistic types
    public static final String STATS_TYPE = "stats_type";
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
        
        // Hide other charts
        pieChart.setVisibility(View.VISIBLE);
        barChart.setVisibility(View.GONE);
        lineChart.setVisibility(View.GONE);
        
        List<StatisticItem> statistics = databaseHelper.getFoodCountByCategory();
        
        // If no data, show message instead of demo data
        if (statistics.isEmpty()) {
            pieChart.setVisibility(View.GONE);
            textViewDescription.setText("Không có dữ liệu về các danh mục món ăn. Hãy thêm một số món ăn để xem thống kê.");
            return;
        }
        
        List<PieEntry> entries = new ArrayList<>();
        for (StatisticItem item : statistics) {
            entries.add(new PieEntry((float) item.getValue(), item.getName()));
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
        textViewDescription.setText("Biểu đồ thể hiện doanh thu của từng ngày trong 7 ngày qua\n(Nhấn vào điểm trên biểu đồ để xem chi tiết đơn hàng)");
        
        // Hide other charts
        pieChart.setVisibility(View.GONE);
        barChart.setVisibility(View.GONE);
        lineChart.setVisibility(View.VISIBLE);
        
        // Get date range (last 7 days)
        Calendar endCalendar = Calendar.getInstance();
        Calendar startCalendar = Calendar.getInstance();
        startCalendar.add(Calendar.DAY_OF_MONTH, -6);
        
        String startDate = dbDateFormat.format(startCalendar.getTime());
        String endDate = dbDateFormat.format(endCalendar.getTime());
        
        final List<StatisticItem> statistics = databaseHelper.getRevenueByDate(startDate, endDate);
        
        // If no data, show message instead of demo data
        if (statistics.isEmpty()) {
            lineChart.setVisibility(View.GONE);
            textViewDescription.setText("Không có dữ liệu doanh thu cho 7 ngày qua. Hãy thực hiện một số đơn hàng để xem thống kê.");
            return;
        }
        
        setupLineChart(statistics);
    }

    private void showTopFoods() {
        textViewTitle.setText("Top 10 món ăn bán chạy nhất");
        textViewDescription.setText("Biểu đồ thể hiện số lượng bán ra của 10 món ăn bán chạy nhất");
        
        // Hide other charts
        pieChart.setVisibility(View.GONE);
        barChart.setVisibility(View.VISIBLE);
        lineChart.setVisibility(View.GONE);
        
        List<StatisticItem> statistics = databaseHelper.getTopFoods(10);
        
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
        
        // Hide other charts
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
        textViewDescription.setText("Biểu đồ thể hiện số lượng món ăn trong menu theo ngày trong 7 ngày qua");
        
        // Hide other charts
        pieChart.setVisibility(View.GONE);
        barChart.setVisibility(View.VISIBLE);
        lineChart.setVisibility(View.GONE);
        
        // Get date range (last 7 days)
        Calendar endCalendar = Calendar.getInstance();
        Calendar startCalendar = Calendar.getInstance();
        startCalendar.add(Calendar.DAY_OF_MONTH, -6);
        
        String startDate = dbDateFormat.format(startCalendar.getTime());
        String endDate = dbDateFormat.format(endCalendar.getTime());
        
        List<StatisticItem> statistics = databaseHelper.getDailyMenuCountByDate(startDate, endDate);
        
        // If no data, show message instead of demo data
        if (statistics.isEmpty()) {
            barChart.setVisibility(View.GONE);
            textViewDescription.setText("Không có dữ liệu về menu hàng ngày. Hãy thêm một số món vào menu hàng ngày để xem thống kê.");
            return;
        }
        
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
        
        // Customize X Axis to show dates
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

    private void showDemoData() {
        textViewTitle.setText("Biểu đồ thống kê mẫu");
        textViewDescription.setText("Đây là biểu đồ mẫu với dữ liệu giả lập để minh họa. Không phải dữ liệu thực tế từ cơ sở dữ liệu.");
        
        // Hiển thị tất cả biểu đồ để demo
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
                if (index >= 0 && index < originalDates.size()) {
                    String selectedDate = originalDates.get(index);
                    
                    // Create intent to open DayOrdersActivity
                    Intent intent = new Intent(StatisticsDetailsActivity.this, 
                            com.example.qlnhahangsesan.DayOrdersActivity.class);
                    intent.putExtra("date", selectedDate);
                    startActivity(intent);
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
        
        // Hide all charts
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