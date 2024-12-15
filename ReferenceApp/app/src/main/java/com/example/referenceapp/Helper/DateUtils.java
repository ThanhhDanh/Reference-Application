package com.example.referenceapp.Helper;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DateUtils {
    public static String formatDate(String isoDate) {
        try {
            // Chuyển chuỗi ISO 8601 thành Date
            SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault());
            isoFormat.setTimeZone(java.util.TimeZone.getTimeZone("UTC"));
            Date date = isoFormat.parse(isoDate);

            // Định dạng lại Date thành chuỗi theo định dạng mong muốn
            SimpleDateFormat desiredFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault());
            return desiredFormat.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
            return isoDate; // Trả lại chuỗi gốc nếu lỗi
        }
    }
}
