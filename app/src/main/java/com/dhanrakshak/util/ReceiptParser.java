package com.dhanrakshak.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ReceiptParser {

    public static class ReceiptData {
        public double amount = 0.0;
        public String date = null;
        public String merchant = null;
    }

    public static ReceiptData parse(String ocrText) {
        ReceiptData data = new ReceiptData();
        String[] lines = ocrText.split("\n");

        // Simple Heuristic Parsing
        // 1. Merchant: Usually the first non-empty line
        for (String line : lines) {
            String trimmed = line.trim();
            if (!trimmed.isEmpty() && trimmed.length() > 3) {
                data.merchant = trimmed;
                break;
            }
        }

        // 2. Amount: Look for "Total", "Amount", "Balance" followed by currency/numbers
        // Or just the largest number found at the bottom
        Pattern amountPattern = Pattern.compile("(?i)(total|amount|balance|due|payable).*?(\\d+[.,]\\d{2})");
        Pattern anyNumberPattern = Pattern.compile("(\\d+[.,]\\d{2})");

        // Try to find explicit total line first
        for (String line : lines) {
            Matcher m = amountPattern.matcher(line);
            if (m.find()) {
                try {
                    String amountStr = m.group(2).replace(",", "");
                    data.amount = Double.parseDouble(amountStr);
                } catch (Exception ignored) {
                }
            }
        }

        // If not found, look for largest number (often the total)
        if (data.amount == 0) {
            double maxVal = 0;
            for (String line : lines) {
                Matcher m = anyNumberPattern.matcher(line);
                while (m.find()) {
                    try {
                        String valStr = m.group(1).replace(",", "");
                        // Filter out small numbers like dates 20.24 etc. if possible
                        double val = Double.parseDouble(valStr);
                        if (val > maxVal)
                            maxVal = val;
                    } catch (Exception ignored) {
                    }
                }
            }
            data.amount = maxVal;
        }

        // 3. Date: Look for date patterns DD/MM/YYYY or YYYY-MM-DD
        Pattern datePattern = Pattern.compile("\\b(\\d{1,2}[/-]\\d{1,2}[/-]\\d{2,4})\\b");
        for (String line : lines) {
            Matcher m = datePattern.matcher(line);
            if (m.find()) {
                data.date = m.group(1);
                break;
            }
        }

        return data;
    }
}
