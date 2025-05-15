package com.example.qlnhahangsesan.statistics;

import android.graphics.Color;
import android.os.Bundle;
import android.view.MenuItem;
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

    public static final String STATS_TYPE = "stats_type";
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
        int statsType = getIntent().getIntExtra(STATS_TYPE, 0);
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
            default:
                // Show default data
                showDemoData();
                break;
        }
    }

    private void showFoodByCategory() {
        textViewTitle.setText("Thống kê món ăn theo danh mục");
        textViewDescription.setText("Biểu đồ thể hiện phần trăm số lượng món ăn trong mỗi danh mục");
        
        // Hide other charts
        pieChart.setVisibility(android.view.View.VISIBLE);
        barChart.setVisibility(android.view.View.GONE);
        lineChart.setVisibility(android.view.View.GONE);
        
        List<StatisticItem> statistics = databaseHelper.getFoodCountByCategory();
        
        // If no data, create demo data
        if (statistics.isEmpty()) {
            statistics = createDemoFoodCategoryData();
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
        textViewDescription.setText("Biểu đồ thể hiện doanh thu của từng ngày trong 7 ngày qua");
        
        // Hide other charts
        pieChart.setVisibility(android.view.View.GONE);
        barChart.setVisibility(android.view.View.GONE);
        lineChart.setVisibility(android.view.View.VISIBLE);
        
        // Get date range (last 7 days)
        Calendar endCalendar = Calendar.getInstance();
        Calendar startCalendar = Calendar.getInstance();
        startCalendar.add(Calendar.DAY_OF_MONTH, -6);
        
        String startDate = dbDateFormat.format(startCalendar.getTime());
        String endDate = dbDateFormat.format(endCalendar.getTime());
        
        List<StatisticItem> statistics = databaseHelper.getRevenueByDate(startDate, endDate);
        
        // If no data, create demo data
        if (statistics.isEmpty()) {
            statistics = createDemoRevenueData(startCalendar, endCalendar);
        }
        
        List<Entry> entries = new ArrayList<>();
        List<String> xAxisLabels = new ArrayList<>();
        
        for (int i = 0; i < statistics.size(); i++) {
            StatisticItem item = statistics.get(i);
            entries.add(new Entry(i, (float) item.getValue()));
            xAxisLabels.add(item.getName());
        }
        
        LineDataSet dataSet = new LineDataSet(entries, "Doanh thu (VND)");
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
        lineChart.invalidate();
    }

    private void showTopFoods() {
        textViewTitle.setText("Top 10 món ăn bán chạy nhất");
        textViewDescription.setText("Biểu đồ thể hiện số lượng bán ra của 10 món ăn bán chạy nhất");
        
        // Hide other charts
        pieChart.setVisibility(android.view.View.GONE);
        barChart.setVisibility(android.view.View.VISIBLE);
        lineChart.setVisibility(android.view.View.GONE);
        
        List<StatisticItem> statistics = databaseHelper.getTopFoods(10);
        
        // If no data, create demo data
        if (statistics.isEmpty()) {
            statistics = createDemoTopFoodData();
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
        pieChart.setVisibility(android.view.View.VISIBLE);
        barChart.setVisibility(android.view.View.GONE);
        lineChart.setVisibility(android.view.View.GONE);
        
        List<StatisticItem> statistics = databaseHelper.getTableCountByStatus();
        
        // If no data, create demo data
        if (statistics.isEmpty()) {
            statistics = createDemoTableStatusData();
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
        pieChart.setVisibility(android.view.View.GONE);
        barChart.setVisibility(android.view.View.VISIBLE);
        lineChart.setVisibility(android.view.View.GONE);
        
        // Get date range (last 7 days)
        Calendar endCalendar = Calendar.getInstance();
        Calendar startCalendar = Calendar.getInstance();
        startCalendar.add(Calendar.DAY_OF_MONTH, -6);
        
        String startDate = dbDateFormat.format(startCalendar.getTime());
        String endDate = dbDateFormat.format(endCalendar.getTime());
        
        List<StatisticItem> statistics = databaseHelper.getDailyMenuCountByDate(startDate, endDate);
        
        // If no data, create demo data
        if (statistics.isEmpty()) {
            statistics = createDemoMenuData(startCalendar, endCalendar);
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
        textViewTitle.setText("Thống kê biểu đồ mẫu");
        textViewDescription.setText("Đây là biểu đồ mẫu với dữ liệu giả lập");
        
        // Hiển thị tất cả biểu đồ để demo
        pieChart.setVisibility(android.view.View.VISIBLE);
        barChart.setVisibility(android.view.View.VISIBLE);
        lineChart.setVisibility(android.view.View.VISIBLE);
        
        // Demo pie chart
        List<StatisticItem> pieData = createDemoFoodCategoryData();
        List<PieEntry> pieEntries = new ArrayList<>();
        for (StatisticItem item : pieData) {
            pieEntries.add(new PieEntry((float) item.getValue(), item.getName()));
        }
        
        PieDataSet pieDataSet = new PieDataSet(pieEntries, "Danh mục món ăn");
        pieDataSet.setColors(ColorTemplate.COLORFUL_COLORS);
        pieDataSet.setValueTextSize(14f);
        pieDataSet.setValueTextColor(Color.WHITE);
        
        PieData pieChartData = new PieData(pieDataSet);
        pieChartData.setValueFormatter(new PercentFormatter(pieChart));
        
        Description pieDescription = new Description();
        pieDescription.setText("");
        pieChart.setDescription(pieDescription);
        pieChart.setData(pieChartData);
        pieChart.setCenterText("Tổng số món: " + countTotalValue(pieData));
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
        
        BarDataSet barDataSet = new BarDataSet(barEntries, "Số lượng bán ra");
        barDataSet.setColors(ColorTemplate.MATERIAL_COLORS);
        barDataSet.setValueTextSize(12f);
        
        BarData barChartData = new BarData(barDataSet);
        barChartData.setBarWidth(0.7f);
        
        Description barDescription = new Description();
        barDescription.setText("");
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
        
        LineDataSet lineDataSet = new LineDataSet(lineEntries, "Doanh thu (VND)");
        lineDataSet.setColor(Color.BLUE);
        lineDataSet.setValueTextColor(Color.BLACK);
        lineDataSet.setValueTextSize(12f);
        lineDataSet.setLineWidth(2f);
        lineDataSet.setCircleRadius(4f);
        lineDataSet.setCircleColor(Color.RED);
        lineDataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        
        LineData lineChartData = new LineData(lineDataSet);
        
        Description lineDescription = new Description();
        lineDescription.setText("");
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

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
} 