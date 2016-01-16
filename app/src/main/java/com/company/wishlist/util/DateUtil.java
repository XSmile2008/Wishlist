package com.company.wishlist.util;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by v.odahovskiy on 16.01.2016.
 */
public class DateUtil {

    public static String getFormattedDate(long timeInMillis){
        return new SimpleDateFormat("yyyy-MM-dd").format(new Date(timeInMillis));
    }

}
