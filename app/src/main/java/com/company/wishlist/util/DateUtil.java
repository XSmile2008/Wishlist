package com.company.wishlist.util;

import com.codetroopers.betterpickers.calendardatepicker.MonthAdapter;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class DateUtil {

    public static String getFormattedDate(long timeInMillis) {
        return SimpleDateFormat.getDateInstance().format(new Date(timeInMillis));
    }

    public static long parse(String date) {
        return 0;
    }

    public static long subtractDaysFromDate(long date, int countSubtractedDays) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date(date));
        cal.add(Calendar.DATE, -countSubtractedDays);
        return cal.getTimeInMillis();
    }

    public static boolean isToday(long date){
        Calendar daeForCheck = Calendar.getInstance();
        daeForCheck.setTime(new Date(date));
        return isSameDay(daeForCheck, Calendar.getInstance());
    }

    public static long getDateWithoutTime(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);

        return cal.getTimeInMillis();
    }

    public static MonthAdapter.CalendarDay getToday(){
        MonthAdapter.CalendarDay today = new MonthAdapter.CalendarDay(System.currentTimeMillis());
        return today;
    }

    private static boolean isSameDay(Calendar cal1, Calendar cal2) {
        if (cal1 == null || cal2 == null) {
            throw new IllegalArgumentException("The dates must not be null");
        }
        return (cal1.get(Calendar.ERA) == cal2.get(Calendar.ERA) &&
                cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR));
    }

}
