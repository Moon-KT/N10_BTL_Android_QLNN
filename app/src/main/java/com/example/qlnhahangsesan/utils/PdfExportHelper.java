package com.example.qlnhahangsesan.utils;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.core.content.FileProvider;

import com.example.qlnhahangsesan.R;
import com.example.qlnhahangsesan.model.StatisticItem;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class PdfExportHelper {

    private static final String TAG = "PdfExportHelper";
    
    /**
     * Xuất danh sách thống kê sang file PDF
     * @param context Context ứng dụng
     * @param statisticsList Danh sách dữ liệu thống kê
     * @param title Tiêu đề báo cáo
     * @param isMonetary Có phải dữ liệu tiền tệ không
     * @return Đường dẫn đến file PDF đã tạo, null nếu thất bại
     */
    public static Uri exportToPdf(Context context, List<StatisticItem> statisticsList, String title, boolean isMonetary) {
        if (statisticsList == null || statisticsList.isEmpty()) {
            return null;
        }
        
        // Tạo tên file
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String fileName = "Sesan_Statistics_" + timeStamp + ".pdf";
        
        // Đường dẫn file
        File pdfFile;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            File directory = new File(context.getExternalFilesDir(null), "SesenReports");
            if (!directory.exists()) {
                directory.mkdirs();
            }
            pdfFile = new File(directory, fileName);
        } else {
            File directory = new File(Environment.getExternalStorageDirectory(), "SesenReports");
            if (!directory.exists()) {
                directory.mkdirs();
            }
            pdfFile = new File(directory, fileName);
        }

        try {
            Document document = new Document(PageSize.A4);
            PdfWriter.getInstance(document, new FileOutputStream(pdfFile));
            document.open();

            // Thêm tiêu đề báo cáo
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, BaseColor.DARK_GRAY);
            Paragraph titlePara = new Paragraph(title, titleFont);
            titlePara.setAlignment(Element.ALIGN_CENTER);
            document.add(titlePara);
            
            // Thêm ngày xuất báo cáo
            Font dateFont = FontFactory.getFont(FontFactory.HELVETICA, 12, BaseColor.GRAY);
            String currentDate = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()).format(new Date());
            Paragraph datePara = new Paragraph("Ngày xuất báo cáo: " + currentDate, dateFont);
            datePara.setAlignment(Element.ALIGN_CENTER);
            datePara.setSpacingAfter(20);
            document.add(datePara);
            
            // Tạo bảng dữ liệu
            PdfPTable table = new PdfPTable(2);
            table.setWidthPercentage(100);
            table.setSpacingBefore(10f);
            table.setSpacingAfter(10f);
            
            // Thiết lập độ rộng các cột
            float[] columnWidths = {2f, 1f};
            table.setWidths(columnWidths);
            
            // Tạo header cho bảng
            Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, BaseColor.WHITE);
            PdfPCell cell1 = new PdfPCell(new Phrase("Tên", headerFont));
            cell1.setBackgroundColor(BaseColor.DARK_GRAY);
            cell1.setPadding(5);
            cell1.setHorizontalAlignment(Element.ALIGN_CENTER);
            
            PdfPCell cell2 = new PdfPCell(new Phrase("Giá trị", headerFont));
            cell2.setBackgroundColor(BaseColor.DARK_GRAY);
            cell2.setPadding(5);
            cell2.setHorizontalAlignment(Element.ALIGN_CENTER);
            
            table.addCell(cell1);
            table.addCell(cell2);
            
            // Thêm dữ liệu vào bảng
            Font dataFont = FontFactory.getFont(FontFactory.HELVETICA, 12, BaseColor.BLACK);
            Font oddRowFont = FontFactory.getFont(FontFactory.HELVETICA, 12, BaseColor.BLACK);
            Font evenRowFont = FontFactory.getFont(FontFactory.HELVETICA, 12, BaseColor.BLACK);
            
            boolean isOddRow = true;
            
            for (StatisticItem item : statisticsList) {
                Font currentFont = isOddRow ? oddRowFont : evenRowFont;
                BaseColor rowColor = isOddRow ? BaseColor.LIGHT_GRAY : BaseColor.WHITE;
                
                PdfPCell nameCell = new PdfPCell(new Phrase(item.getName(), currentFont));
                nameCell.setBackgroundColor(rowColor);
                nameCell.setPadding(5);
                
                PdfPCell valueCell;
                if (isMonetary) {
                    String formattedValue = String.format(Locale.getDefault(), "%,.0f VNĐ", item.getValue());
                    valueCell = new PdfPCell(new Phrase(formattedValue, currentFont));
                } else {
                    String formattedValue = String.format(Locale.getDefault(), "%,d", item.getIntValue());
                    valueCell = new PdfPCell(new Phrase(formattedValue, currentFont));
                }
                
                valueCell.setBackgroundColor(rowColor);
                valueCell.setPadding(5);
                valueCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                
                table.addCell(nameCell);
                table.addCell(valueCell);
                
                isOddRow = !isOddRow;
            }
            
            document.add(table);
            
            // Thêm thông tin cuối trang
            Font footerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, BaseColor.GRAY);
            Paragraph footerPara = new Paragraph("Báo cáo này được tạo tự động từ ứng dụng quản lý nhà hàng Sesan", footerFont);
            footerPara.setAlignment(Element.ALIGN_CENTER);
            footerPara.setSpacingBefore(20);
            document.add(footerPara);
            
            document.close();
            
            // Trả về Uri cho file PDF
            return FileProvider.getUriForFile(context, context.getPackageName() + ".provider", pdfFile);
            
        } catch (DocumentException | IOException e) {
            Log.e(TAG, "Error creating PDF", e);
            return null;
        }
    }
    
    /**
     * Mở file PDF
     * @param context Context ứng dụng
     * @param pdfUri Uri của file PDF
     */
    public static void openPdf(Context context, Uri pdfUri) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(pdfUri, "application/pdf");
        intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        
        try {
            context.startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(context, "Không có ứng dụng nào để mở file PDF", Toast.LENGTH_SHORT).show();
        }
    }
    
    /**
     * Chia sẻ file PDF
     * @param context Context ứng dụng
     * @param pdfUri Uri của file PDF
     * @param title Tiêu đề báo cáo để chia sẻ
     */
    public static void sharePdf(Context context, Uri pdfUri, String title) {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("application/pdf");
        shareIntent.putExtra(Intent.EXTRA_STREAM, pdfUri);
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, title);
        shareIntent.putExtra(Intent.EXTRA_TEXT, "Báo cáo thống kê nhà hàng Sesan: " + title);
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        
        context.startActivity(Intent.createChooser(shareIntent, context.getString(R.string.share_via)));
    }
} 