package com.example.qlnhahangsesan;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.example.qlnhahangsesan.fragment.StatisticsFragment;
import com.example.qlnhahangsesan.model.StatisticItem;
import com.example.qlnhahangsesan.statistics.StatisticsDetailsActivity;
import com.example.qlnhahangsesan.utils.PdfExportHelper;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class StatisticsActivity extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_CODE = 100;
    private static final int NUM_PAGES = 5;
    
    private Toolbar toolbar;
    private TabLayout tabLayout;
    private ViewPager2 viewPager;
    private FloatingActionButton fabShowChart;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
    private Uri lastExportedPdfUri;  // Lưu URI của file PDF vừa xuất

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistics);
        
        initViews();
        setupToolbar();
        setupViewPager();
        setupFabButton();
    }
    
    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        tabLayout = findViewById(R.id.tabLayout);
        viewPager = findViewById(R.id.viewPager);
        fabShowChart = findViewById(R.id.fabShowChart);
    }
    
    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle(R.string.statistics);
        }
    }
    
    private void setupViewPager() {
        StatisticsPagerAdapter pagerAdapter = new StatisticsPagerAdapter(this);
        viewPager.setAdapter(pagerAdapter);
        
        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            switch (position) {
                case 0:
                    tab.setText(R.string.food_by_category);
                    break;
                case 1:
                    tab.setText(R.string.revenue_by_date);
                    break;
                case 2:
                    tab.setText(R.string.top_foods);
                    break;
                case 3:
                    tab.setText(R.string.tables_by_status);
                    break;
                case 4:
                    tab.setText(R.string.menu_by_date);
                    break;
            }
        }).attach();
    }
    
    private void setupFabButton() {
        fabShowChart.setOnClickListener(view -> {
            // Mở màn hình biểu đồ chi tiết dựa trên tab hiện tại
            int statsType = getStatsTypeFromCurrentTab();
            Intent intent = new Intent(StatisticsActivity.this, StatisticsDetailsActivity.class);
            intent.putExtra(StatisticsDetailsActivity.STATS_TYPE, statsType);
            
            // Nếu là thống kê món ăn phổ biến, truyền thêm limit và khoảng thời gian
            if (statsType == StatisticsDetailsActivity.STATS_TOP_FOODS) {
                StatisticsFragment currentFragment = getCurrentStatisticsFragment();
                if (currentFragment != null) {
                    // Lấy giá trị limit hiện tại từ fragment
                    int limit = currentFragment.getTopFoodsLimit();
                    intent.putExtra(StatisticsDetailsActivity.STATS_LIMIT, limit);
                    
                    // Lấy thông tin khoảng thời gian
                    String[] dateRange = currentFragment.getTopFoodsDateRange();
                    if (dateRange != null) {
                        if (dateRange[0] != null) {
                            intent.putExtra(StatisticsDetailsActivity.STATS_START_DATE, dateRange[0]);
                        }
                        if (dateRange[1] != null) {
                            intent.putExtra(StatisticsDetailsActivity.STATS_END_DATE, dateRange[1]);
                        }
                    }
                    
                    // Log để debug
                    Log.d("StatisticsActivity", "FAB: Truyền limit = " + limit + " sang màn hình biểu đồ chi tiết");
                    Log.d("StatisticsActivity", "FAB: Truyền khoảng thời gian: " + 
                            (dateRange != null ? (dateRange[0] + " đến " + dateRange[1]) : "tất cả thời gian"));
                } else {
                    Log.e("StatisticsActivity", "FAB: Không thể lấy StatisticsFragment hiện tại");
                }
            }
            
            startActivity(intent);
        });
    }
    
    private int getStatsTypeFromCurrentTab() {
        int currentTab = viewPager.getCurrentItem();
        switch (currentTab) {
            case 0:
                return StatisticsDetailsActivity.STATS_FOOD_BY_CATEGORY;
            case 1:
                return StatisticsDetailsActivity.STATS_REVENUE_BY_DATE;
            case 2:
                return StatisticsDetailsActivity.STATS_TOP_FOODS;
            case 3:
                return StatisticsDetailsActivity.STATS_TABLE_BY_STATUS;
            case 4:
                return StatisticsDetailsActivity.STATS_MENU_BY_DATE;
            default:
                return 0;
        }
    }
    
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.statistics_menu, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        
        if (id == android.R.id.home) {
            onBackPressed();
            return true;
        } else if (id == R.id.action_share) {
            // Xử lý chia sẻ thống kê
            shareStatistics();
            return true;
        } else if (id == R.id.action_export_pdf) {
            // Xuất báo cáo ra file PDF
            exportToPdf();
            return true;
        } else if (id == R.id.action_refresh) {
            // Làm mới dữ liệu thống kê
            refreshStatistics();
            return true;
        } else if (id == R.id.action_view_chart) {
            // Mở màn hình biểu đồ chi tiết
            int statsType = getStatsTypeFromCurrentTab();
            Intent intent = new Intent(StatisticsActivity.this, StatisticsDetailsActivity.class);
            intent.putExtra(StatisticsDetailsActivity.STATS_TYPE, statsType);
            
            // Nếu là thống kê món ăn phổ biến, truyền thêm limit và khoảng thời gian
            if (statsType == StatisticsDetailsActivity.STATS_TOP_FOODS) {
                StatisticsFragment currentFragment = getCurrentStatisticsFragment();
                if (currentFragment != null) {
                    // Lấy giá trị limit hiện tại từ fragment
                    int limit = currentFragment.getTopFoodsLimit();
                    intent.putExtra(StatisticsDetailsActivity.STATS_LIMIT, limit);
                    
                    // Lấy thông tin khoảng thời gian
                    String[] dateRange = currentFragment.getTopFoodsDateRange();
                    if (dateRange != null) {
                        if (dateRange[0] != null) {
                            intent.putExtra(StatisticsDetailsActivity.STATS_START_DATE, dateRange[0]);
                        }
                        if (dateRange[1] != null) {
                            intent.putExtra(StatisticsDetailsActivity.STATS_END_DATE, dateRange[1]);
                        }
                    }
                    
                    // Log để debug
                    Log.d("StatisticsActivity", "Truyền limit = " + limit + " sang màn hình biểu đồ chi tiết");
                    Log.d("StatisticsActivity", "Truyền khoảng thời gian: " + 
                            (dateRange != null ? (dateRange[0] + " đến " + dateRange[1]) : "tất cả thời gian"));
                } else {
                    Log.e("StatisticsActivity", "Không thể lấy StatisticsFragment hiện tại");
                }
            }
            
            startActivity(intent);
            return true;
        } else if (id == R.id.action_settings) {
            // Mở cài đặt thống kê
            Toast.makeText(this, getString(R.string.feature_in_development), Toast.LENGTH_SHORT).show();
            return true;
        }
        
        return super.onOptionsItemSelected(item);
    }
    
    private void shareStatistics() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.statistics));
        shareIntent.putExtra(Intent.EXTRA_TEXT, "Báo cáo thống kê nhà hàng Sesan: " + 
                dateFormat.format(new Date()));
        startActivity(Intent.createChooser(shareIntent, getString(R.string.share_via)));
    }
    
    private void exportToPdf() {
        if (checkPermission()) {
            performPdfExport();
        } else {
            requestPermission();
        }
    }
    
    private boolean checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            return true; // Android 10+ dùng Scoped Storage không cần xin quyền
        }
        
        int result = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        return result == PackageManager.PERMISSION_GRANTED;
    }
    
    private void requestPermission() {
        ActivityCompat.requestPermissions(
                this,
                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                PERMISSION_REQUEST_CODE
        );
    }
    
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                performPdfExport();
            } else {
                Toast.makeText(this, getString(R.string.storage_permission_required), Toast.LENGTH_SHORT).show();
            }
        }
    }
    
    private void performPdfExport() {
        // Lấy fragment hiện tại
        StatisticsFragment currentFragment = getCurrentStatisticsFragment();
        
        if (currentFragment != null) {
            List<StatisticItem> statisticsList = currentFragment.getStatisticsList();
            
            if (statisticsList != null && !statisticsList.isEmpty()) {
                // Lấy tiêu đề báo cáo dựa trên tab đang hiển thị
                final String title;
                switch (viewPager.getCurrentItem()) {
                    case 0:
                        title = getString(R.string.food_by_category);
                        break;
                    case 1:
                        title = getString(R.string.revenue_by_date);
                        break;
                    case 2:
                        title = getString(R.string.top_foods);
                        break;
                    case 3:
                        title = getString(R.string.tables_by_status);
                        break;
                    case 4:
                        title = getString(R.string.menu_by_date);
                        break;
                    default:
                        title = getString(R.string.statistics); // fallback an toàn
                        break;
                }

                // Kiểm tra xem là dữ liệu tiền tệ hay không
                boolean isMonetary = viewPager.getCurrentItem() == 1; // Chỉ có tab 1 (Doanh thu theo ngày) là dữ liệu tiền tệ
                
                // Xuất PDF
                lastExportedPdfUri = PdfExportHelper.exportToPdf(this, statisticsList, title, isMonetary);
                
                if (lastExportedPdfUri != null) {
                    Toast.makeText(this, getString(R.string.pdf_exported), Toast.LENGTH_SHORT).show();
                    
                    // Hiển thị dialog hỏi người dùng có muốn mở file PDF vừa xuất hay không
                    new AlertDialog.Builder(this)
                            .setTitle(getString(R.string.export_pdf))
                            .setMessage(getString(R.string.pdf_exported))
                            .setPositiveButton(getString(R.string.open_pdf), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    PdfExportHelper.openPdf(StatisticsActivity.this, lastExportedPdfUri);
                                }
                            })
                            .setNegativeButton(android.R.string.cancel, null)
                            .setNeutralButton(R.string.share, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    PdfExportHelper.sharePdf(StatisticsActivity.this, lastExportedPdfUri, title);
                                }
                            })
                            .show();
                } else {
                    Toast.makeText(this, getString(R.string.pdf_export_error), Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, getString(R.string.no_statistics), Toast.LENGTH_SHORT).show();
            }
        }
    }
    
    private StatisticsFragment getCurrentStatisticsFragment() {
        try {
            // Lấy adapter và truy cập trực tiếp vào fragment hiện tại
            StatisticsPagerAdapter adapter = (StatisticsPagerAdapter) viewPager.getAdapter();
            if (adapter != null) {
                Fragment fragment = adapter.getFragment(viewPager.getCurrentItem());
                if (fragment instanceof StatisticsFragment) {
                    return (StatisticsFragment) fragment;
                }
            }
            
            // Phương pháp dự phòng: tìm fragment bằng tag
            String fragmentTag = "f" + viewPager.getCurrentItem();
            Fragment fragment = getSupportFragmentManager().findFragmentByTag(fragmentTag);
            if (fragment instanceof StatisticsFragment) {
                return (StatisticsFragment) fragment;
            }
            
            Log.e("StatisticsActivity", "Không thể tìm thấy StatisticsFragment hiện tại");
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    private void refreshStatistics() {
        StatisticsFragment currentFragment = getCurrentStatisticsFragment();
        if (currentFragment != null) {
            currentFragment.refreshData();
            Toast.makeText(this, getString(R.string.statistics_refreshed), Toast.LENGTH_SHORT).show();
        }
    }
    
    private class StatisticsPagerAdapter extends FragmentStateAdapter {
        // Mảng lưu trữ các fragment đã tạo
        private final Fragment[] mFragments = new Fragment[NUM_PAGES];
        
        public StatisticsPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
            super(fragmentActivity);
        }
        
        @NonNull
        @Override
        public Fragment createFragment(int position) {
            // Kiểm tra nếu fragment đã được tạo trước đó
            if (mFragments[position] != null) {
                return mFragments[position];
            }
            
            // Tạo mới fragment nếu chưa có
            Fragment fragment;
            switch (position) {
                case 0:
                    fragment = StatisticsFragment.newInstance(StatisticsFragment.STATS_TYPE_FOOD_CATEGORY);
                    break;
                case 1:
                    fragment = StatisticsFragment.newInstance(StatisticsFragment.STATS_TYPE_REVENUE);
                    break;
                case 2:
                    fragment = StatisticsFragment.newInstance(StatisticsFragment.STATS_TYPE_TOP_FOODS);
                    break;
                case 3:
                    fragment = StatisticsFragment.newInstance(StatisticsFragment.STATS_TYPE_TABLE_STATUS);
                    break;
                case 4:
                    fragment = StatisticsFragment.newInstance(StatisticsFragment.STATS_TYPE_MENU_BY_DATE);
                    break;
                default:
                    fragment = StatisticsFragment.newInstance(StatisticsFragment.STATS_TYPE_FOOD_CATEGORY);
                    break;
            }
            
            // Lưu fragment vào mảng để sử dụng lại sau này
            mFragments[position] = fragment;
            return fragment;
        }
        
        /**
         * Lấy fragment hiện tại theo vị trí
         */
        public Fragment getFragment(int position) {
            if (position >= 0 && position < mFragments.length) {
                return mFragments[position];
            }
            return null;
        }
        
        @Override
        public int getItemCount() {
            return NUM_PAGES;
        }
    }
} 