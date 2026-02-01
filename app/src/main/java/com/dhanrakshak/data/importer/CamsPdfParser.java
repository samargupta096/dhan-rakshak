package com.dhanrakshak.data.importer;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.dhanrakshak.data.local.dao.AssetDao;
import com.dhanrakshak.data.local.entity.Asset;
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader;
import com.tom_roush.pdfbox.pdmodel.PDDocument;
import com.tom_roush.pdfbox.text.PDFTextStripper;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;

/**
 * Parser for CAMS (Computer Age Management Services) Consolidated Account
 * Statement PDFs.
 * This allows importing all mutual fund holdings from a single PDF downloaded
 * from:
 * -
 * https://www.camsonline.com/Investors/Statements/Consolidated-Account-Statement
 * - https://mfs.kfintech.com/investor/ (KFintech CAS)
 * 
 * Supports holdings across all brokers: Groww, INDmoney, Zerodha, Angel One,
 * Motilal Oswal, etc.
 */
@Singleton
public class CamsPdfParser {

    private static final String TAG = "CamsPdfParser";

    private final Context context;
    private final AssetDao assetDao;

    // Regex patterns for extracting MF data from CAMS PDF
    private static final Pattern SCHEME_PATTERN = Pattern.compile(
            "([A-Z][A-Za-z0-9\\s\\-&]+(?:Fund|Plan|Growth|Direct|Regular)[^\\n]*)");

    private static final Pattern FOLIO_PATTERN = Pattern.compile(
            "Folio\\s*No[.:]?\\s*([A-Z0-9/\\-]+)", Pattern.CASE_INSENSITIVE);

    private static final Pattern UNITS_PATTERN = Pattern.compile(
            "Closing\\s*Unit\\s*Balance[:\\s]+([0-9,]+\\.?[0-9]*)");

    private static final Pattern NAV_PATTERN = Pattern.compile(
            "NAV[:\\s]+(?:Rs\\.?|₹)?\\s*([0-9,]+\\.?[0-9]*)");

    private static final Pattern VALUE_PATTERN = Pattern.compile(
            "(?:Market|Current)\\s*Value[:\\s]+(?:Rs\\.?|₹)?\\s*([0-9,]+\\.?[0-9]*)");

    private static final Pattern SCHEME_CODE_PATTERN = Pattern.compile(
            "(?:AMFI|Scheme)\\s*Code[:\\s]+([0-9]+)");

    @Inject
    public CamsPdfParser(Context context, AssetDao assetDao) {
        this.context = context.getApplicationContext();
        this.assetDao = assetDao;
        // Initialize PDFBox for Android
        PDFBoxResourceLoader.init(context);
    }

    /**
     * Parse CAMS/KFintech Consolidated Account Statement PDF.
     * 
     * @param pdfUri URI of the PDF file
     * @return List of parsed mutual fund holdings
     */
    public Single<List<ParsedMutualFund>> parseCamsPdf(Uri pdfUri) {
        return Single.fromCallable(() -> {
            List<ParsedMutualFund> holdings = new ArrayList<>();

            try (InputStream inputStream = context.getContentResolver().openInputStream(pdfUri);
                    PDDocument document = PDDocument.load(inputStream)) {

                PDFTextStripper stripper = new PDFTextStripper();
                String text = stripper.getText(document);

                Log.d(TAG, "Extracted PDF text, length: " + text.length());

                // Split by each fund section
                String[] sections = text.split("(?=Folio No)");

                for (String section : sections) {
                    ParsedMutualFund mf = parseSection(section);
                    if (mf != null && mf.units > 0) {
                        holdings.add(mf);
                    }
                }

            } catch (Exception e) {
                Log.e(TAG, "Error parsing PDF", e);
                throw new RuntimeException("Failed to parse CAMS PDF: " + e.getMessage());
            }

            return holdings;
        }).subscribeOn(Schedulers.io());
    }

    /**
     * Parse a single fund section from the PDF text.
     */
    private ParsedMutualFund parseSection(String section) {
        try {
            ParsedMutualFund mf = new ParsedMutualFund();

            // Extract folio number
            Matcher folioMatcher = FOLIO_PATTERN.matcher(section);
            if (folioMatcher.find()) {
                mf.folioNumber = folioMatcher.group(1).trim();
            }

            // Extract scheme name
            Matcher schemeMatcher = SCHEME_PATTERN.matcher(section);
            if (schemeMatcher.find()) {
                mf.schemeName = schemeMatcher.group(1).trim();
            }

            // Extract scheme code
            Matcher codeMatcher = SCHEME_CODE_PATTERN.matcher(section);
            if (codeMatcher.find()) {
                mf.schemeCode = Long.parseLong(codeMatcher.group(1).trim());
            }

            // Extract units
            Matcher unitsMatcher = UNITS_PATTERN.matcher(section);
            if (unitsMatcher.find()) {
                mf.units = parseNumber(unitsMatcher.group(1));
            }

            // Extract NAV
            Matcher navMatcher = NAV_PATTERN.matcher(section);
            if (navMatcher.find()) {
                mf.nav = parseNumber(navMatcher.group(1));
            }

            // Extract current value
            Matcher valueMatcher = VALUE_PATTERN.matcher(section);
            if (valueMatcher.find()) {
                mf.currentValue = parseNumber(valueMatcher.group(1));
            }

            // Calculate average NAV if we have value and units
            if (mf.units > 0 && mf.currentValue > 0) {
                mf.avgNav = mf.currentValue / mf.units;
            }

            return mf.schemeName != null ? mf : null;

        } catch (Exception e) {
            Log.e(TAG, "Error parsing section", e);
            return null;
        }
    }

    /**
     * Import parsed mutual funds into database.
     */
    public Completable importHoldings(List<ParsedMutualFund> holdings) {
        return Completable.fromAction(() -> {
            for (ParsedMutualFund mf : holdings) {
                Asset asset = new Asset(
                        "MUTUAL_FUND",
                        mf.schemeName,
                        String.valueOf(mf.schemeCode),
                        mf.units,
                        mf.avgNav > 0 ? mf.avgNav : mf.nav);
                asset.setCurrentPrice(mf.nav);
                assetDao.insert(asset).blockingAwait();
            }
        }).subscribeOn(Schedulers.io());
    }

    private double parseNumber(String str) {
        try {
            return Double.parseDouble(str.replace(",", "").trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    /**
     * Data class for parsed mutual fund from CAMS PDF.
     */
    public static class ParsedMutualFund {
        public String folioNumber;
        public String schemeName;
        public long schemeCode;
        public double units;
        public double nav;
        public double avgNav;
        public double currentValue;

        @Override
        public String toString() {
            return schemeName + " - " + units + " units @ ₹" + nav;
        }
    }
}
