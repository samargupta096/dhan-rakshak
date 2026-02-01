package com.dhanrakshak.data.export;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.os.Environment;
import android.util.Log;

import com.dhanrakshak.data.local.dao.AssetDao;
import com.dhanrakshak.data.local.dao.BankAccountDao;
import com.dhanrakshak.data.local.dao.SmsTransactionDao;
import com.dhanrakshak.data.local.entity.Asset;
import com.dhanrakshak.data.local.entity.BankAccount;
import com.dhanrakshak.data.local.entity.SmsTransaction;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;

/**
 * PDF Report Generator for financial reports.
 * Generates comprehensive portfolio and transaction reports.
 */
@Singleton
public class PdfReportGenerator {

    private static final String TAG = "PdfReportGenerator";

    private final Context context;
    private final AssetDao assetDao;
    private final BankAccountDao bankAccountDao;
    private final SmsTransactionDao smsTransactionDao;

    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("en", "IN"));
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
    private final SimpleDateFormat fileNameFormat = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault());

    // PDF constants
    private static final int PAGE_WIDTH = 595; // A4 width in points
    private static final int PAGE_HEIGHT = 842; // A4 height in points
    private static final int MARGIN = 40;
    private static final int LINE_HEIGHT = 20;

    @Inject
    public PdfReportGenerator(Context context, AssetDao assetDao,
            BankAccountDao bankAccountDao,
            SmsTransactionDao smsTransactionDao) {
        this.context = context.getApplicationContext();
        this.assetDao = assetDao;
        this.bankAccountDao = bankAccountDao;
        this.smsTransactionDao = smsTransactionDao;
    }

    /**
     * Generate a comprehensive portfolio report.
     */
    public Single<File> generatePortfolioReport() {
        return Single.zip(
                assetDao.getAllAssets().firstOrError(),
                bankAccountDao.getAllActiveAccounts().firstOrError(),
                (assets, accounts) -> createPortfolioReport(assets, accounts)).subscribeOn(Schedulers.io());
    }

    /**
     * Generate a transaction report for a date range.
     */
    public Single<File> generateTransactionReport(long startDate, long endDate) {
        return smsTransactionDao.getTransactionsBetweenDates(startDate, endDate)
                .firstOrError()
                .map(transactions -> createTransactionReport(transactions, startDate, endDate))
                .subscribeOn(Schedulers.io());
    }

    private File createPortfolioReport(List<Asset> assets, List<BankAccount> accounts) throws IOException {
        PdfDocument document = new PdfDocument();
        int pageNumber = 1;
        int yPosition = MARGIN;

        // Create first page
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create();
        PdfDocument.Page page = document.startPage(pageInfo);
        Canvas canvas = page.getCanvas();

        // Paints
        Paint titlePaint = new Paint();
        titlePaint.setColor(Color.parseColor("#1E88E5"));
        titlePaint.setTextSize(24);
        titlePaint.setFakeBoldText(true);

        Paint headerPaint = new Paint();
        headerPaint.setColor(Color.parseColor("#333333"));
        headerPaint.setTextSize(16);
        headerPaint.setFakeBoldText(true);

        Paint textPaint = new Paint();
        textPaint.setColor(Color.parseColor("#666666"));
        textPaint.setTextSize(12);

        Paint valuePaint = new Paint();
        valuePaint.setColor(Color.parseColor("#000000"));
        valuePaint.setTextSize(12);
        valuePaint.setFakeBoldText(true);

        Paint greenPaint = new Paint();
        greenPaint.setColor(Color.parseColor("#4CAF50"));
        greenPaint.setTextSize(12);

        Paint redPaint = new Paint();
        redPaint.setColor(Color.parseColor("#F44336"));
        redPaint.setTextSize(12);

        // Title
        canvas.drawText("Dhan-Rakshak Portfolio Report", MARGIN, yPosition + 24, titlePaint);
        yPosition += 35;
        canvas.drawText("Generated: " + dateFormat.format(new Date()), MARGIN, yPosition, textPaint);
        yPosition += 30;

        // Draw line
        Paint linePaint = new Paint();
        linePaint.setColor(Color.parseColor("#E0E0E0"));
        linePaint.setStrokeWidth(1);
        canvas.drawLine(MARGIN, yPosition, PAGE_WIDTH - MARGIN, yPosition, linePaint);
        yPosition += 20;

        // Calculate totals
        double totalAssetValue = 0;
        double totalBankBalance = 0;
        double totalProfitLoss = 0;

        for (Asset asset : assets) {
            totalAssetValue += asset.getCurrentValue();
            totalProfitLoss += asset.getProfitLoss();
        }
        for (BankAccount account : accounts) {
            totalBankBalance += account.getBalance();
        }
        double totalNetWorth = totalAssetValue + totalBankBalance;

        // Summary section
        canvas.drawText("Portfolio Summary", MARGIN, yPosition, headerPaint);
        yPosition += 25;

        canvas.drawText("Total Net Worth:", MARGIN, yPosition, textPaint);
        canvas.drawText(currencyFormat.format(totalNetWorth), MARGIN + 200, yPosition, valuePaint);
        yPosition += LINE_HEIGHT;

        canvas.drawText("Total Investments:", MARGIN, yPosition, textPaint);
        canvas.drawText(currencyFormat.format(totalAssetValue), MARGIN + 200, yPosition, valuePaint);
        yPosition += LINE_HEIGHT;

        canvas.drawText("Total Bank Balance:", MARGIN, yPosition, textPaint);
        canvas.drawText(currencyFormat.format(totalBankBalance), MARGIN + 200, yPosition, valuePaint);
        yPosition += LINE_HEIGHT;

        canvas.drawText("Total Profit/Loss:", MARGIN, yPosition, textPaint);
        Paint plPaint = totalProfitLoss >= 0 ? greenPaint : redPaint;
        String plPrefix = totalProfitLoss >= 0 ? "+" : "";
        canvas.drawText(plPrefix + currencyFormat.format(totalProfitLoss), MARGIN + 200, yPosition, plPaint);
        yPosition += 40;

        // Investment Details
        canvas.drawLine(MARGIN, yPosition, PAGE_WIDTH - MARGIN, yPosition, linePaint);
        yPosition += 20;
        canvas.drawText("Investment Holdings", MARGIN, yPosition, headerPaint);
        yPosition += 25;

        // Table header
        canvas.drawText("Asset", MARGIN, yPosition, textPaint);
        canvas.drawText("Type", MARGIN + 180, yPosition, textPaint);
        canvas.drawText("Value", MARGIN + 280, yPosition, textPaint);
        canvas.drawText("P/L", MARGIN + 380, yPosition, textPaint);
        yPosition += 5;
        canvas.drawLine(MARGIN, yPosition, PAGE_WIDTH - MARGIN, yPosition, linePaint);
        yPosition += 15;

        for (Asset asset : assets) {
            if (yPosition > PAGE_HEIGHT - MARGIN - 50) {
                // New page
                document.finishPage(page);
                pageNumber++;
                pageInfo = new PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create();
                page = document.startPage(pageInfo);
                canvas = page.getCanvas();
                yPosition = MARGIN;
            }

            String name = asset.getName().length() > 25 ? asset.getName().substring(0, 22) + "..." : asset.getName();
            canvas.drawText(name, MARGIN, yPosition, textPaint);
            canvas.drawText(asset.getAssetType(), MARGIN + 180, yPosition, textPaint);
            canvas.drawText(currencyFormat.format(asset.getCurrentValue()), MARGIN + 280, yPosition, valuePaint);

            double pl = asset.getProfitLoss();
            Paint assetPlPaint = pl >= 0 ? greenPaint : redPaint;
            String assetPlPrefix = pl >= 0 ? "+" : "";
            canvas.drawText(assetPlPrefix + currencyFormat.format(pl), MARGIN + 380, yPosition, assetPlPaint);
            yPosition += LINE_HEIGHT;
        }

        // Bank Accounts
        yPosition += 20;
        if (yPosition > PAGE_HEIGHT - MARGIN - 100) {
            document.finishPage(page);
            pageNumber++;
            pageInfo = new PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create();
            page = document.startPage(pageInfo);
            canvas = page.getCanvas();
            yPosition = MARGIN;
        }

        canvas.drawLine(MARGIN, yPosition, PAGE_WIDTH - MARGIN, yPosition, linePaint);
        yPosition += 20;
        canvas.drawText("Bank Accounts", MARGIN, yPosition, headerPaint);
        yPosition += 25;

        for (BankAccount account : accounts) {
            canvas.drawText(account.getBankName() + " (" + account.getAccountType() + ")",
                    MARGIN, yPosition, textPaint);
            canvas.drawText(currencyFormat.format(account.getBalance()),
                    MARGIN + 300, yPosition, valuePaint);
            yPosition += LINE_HEIGHT;
        }

        // Footer
        canvas.drawText("This report is auto-generated by Dhan-Rakshak",
                MARGIN, PAGE_HEIGHT - MARGIN, textPaint);

        document.finishPage(page);

        // Save file
        File outputDir = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS);
        if (outputDir == null) {
            outputDir = context.getFilesDir();
        }

        String fileName = "DhanRakshak_Portfolio_" + fileNameFormat.format(new Date()) + ".pdf";
        File file = new File(outputDir, fileName);

        try (FileOutputStream fos = new FileOutputStream(file)) {
            document.writeTo(fos);
        }
        document.close();

        Log.d(TAG, "Portfolio report saved: " + file.getAbsolutePath());
        return file;
    }

    private File createTransactionReport(List<SmsTransaction> transactions,
            long startDate, long endDate) throws IOException {
        PdfDocument document = new PdfDocument();
        int pageNumber = 1;
        int yPosition = MARGIN;

        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create();
        PdfDocument.Page page = document.startPage(pageInfo);
        Canvas canvas = page.getCanvas();

        // Paints
        Paint titlePaint = new Paint();
        titlePaint.setColor(Color.parseColor("#1E88E5"));
        titlePaint.setTextSize(24);
        titlePaint.setFakeBoldText(true);

        Paint headerPaint = new Paint();
        headerPaint.setColor(Color.parseColor("#333333"));
        headerPaint.setTextSize(14);
        headerPaint.setFakeBoldText(true);

        Paint textPaint = new Paint();
        textPaint.setColor(Color.parseColor("#666666"));
        textPaint.setTextSize(11);

        Paint creditPaint = new Paint();
        creditPaint.setColor(Color.parseColor("#4CAF50"));
        creditPaint.setTextSize(11);

        Paint debitPaint = new Paint();
        debitPaint.setColor(Color.parseColor("#F44336"));
        debitPaint.setTextSize(11);

        Paint linePaint = new Paint();
        linePaint.setColor(Color.parseColor("#E0E0E0"));
        linePaint.setStrokeWidth(1);

        // Title
        canvas.drawText("Transaction Report", MARGIN, yPosition + 24, titlePaint);
        yPosition += 35;
        canvas.drawText("Period: " + dateFormat.format(new Date(startDate)) +
                " to " + dateFormat.format(new Date(endDate)), MARGIN, yPosition, textPaint);
        yPosition += 30;

        // Summary
        double totalCredit = 0, totalDebit = 0;
        for (SmsTransaction tx : transactions) {
            if ("CREDIT".equals(tx.getTransactionType())) {
                totalCredit += tx.getAmount();
            } else {
                totalDebit += tx.getAmount();
            }
        }

        canvas.drawText("Total Income: " + currencyFormat.format(totalCredit), MARGIN, yPosition, creditPaint);
        yPosition += LINE_HEIGHT;
        canvas.drawText("Total Expenses: " + currencyFormat.format(totalDebit), MARGIN, yPosition, debitPaint);
        yPosition += LINE_HEIGHT;
        canvas.drawText("Net: " + currencyFormat.format(totalCredit - totalDebit), MARGIN, yPosition, headerPaint);
        yPosition += 30;

        // Transactions table
        canvas.drawLine(MARGIN, yPosition, PAGE_WIDTH - MARGIN, yPosition, linePaint);
        yPosition += 15;
        canvas.drawText("Date", MARGIN, yPosition, headerPaint);
        canvas.drawText("Description", MARGIN + 80, yPosition, headerPaint);
        canvas.drawText("Type", MARGIN + 300, yPosition, headerPaint);
        canvas.drawText("Amount", MARGIN + 380, yPosition, headerPaint);
        yPosition += 5;
        canvas.drawLine(MARGIN, yPosition, PAGE_WIDTH - MARGIN, yPosition, linePaint);
        yPosition += 15;

        for (SmsTransaction tx : transactions) {
            if (yPosition > PAGE_HEIGHT - MARGIN - 30) {
                document.finishPage(page);
                pageNumber++;
                pageInfo = new PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create();
                page = document.startPage(pageInfo);
                canvas = page.getCanvas();
                yPosition = MARGIN;
            }

            canvas.drawText(dateFormat.format(new Date(tx.getTransactionDate())), MARGIN, yPosition, textPaint);

            String merchant = tx.getMerchant() != null ? tx.getMerchant() : "Unknown";
            if (merchant.length() > 30)
                merchant = merchant.substring(0, 27) + "...";
            canvas.drawText(merchant, MARGIN + 80, yPosition, textPaint);

            canvas.drawText(tx.getTransactionType(), MARGIN + 300, yPosition, textPaint);

            Paint amountPaint = "CREDIT".equals(tx.getTransactionType()) ? creditPaint : debitPaint;
            String prefix = "CREDIT".equals(tx.getTransactionType()) ? "+" : "-";
            canvas.drawText(prefix + currencyFormat.format(tx.getAmount()), MARGIN + 380, yPosition, amountPaint);

            yPosition += LINE_HEIGHT;
        }

        document.finishPage(page);

        // Save file
        File outputDir = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS);
        if (outputDir == null) {
            outputDir = context.getFilesDir();
        }

        String fileName = "DhanRakshak_Transactions_" + fileNameFormat.format(new Date()) + ".pdf";
        File file = new File(outputDir, fileName);

        try (FileOutputStream fos = new FileOutputStream(file)) {
            document.writeTo(fos);
        }
        document.close();

        Log.d(TAG, "Transaction report saved: " + file.getAbsolutePath());
        return file;
    }
}
