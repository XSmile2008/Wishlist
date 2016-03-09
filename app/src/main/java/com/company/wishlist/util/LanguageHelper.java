package com.company.wishlist.util;


import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.company.wishlist.R;

import java.util.Locale;


public class LanguageHelper {

    public static Context sContext;
    public static SharedPreferences sPreferences;

    public static void initHelper(Context context, SharedPreferences preferences){
        sContext = context;
        sPreferences = preferences;
    }

    public static void setUpLnaguage(String language){
        refreshConfig(language);
    }

    public static void setUpDefaultLanguage() {
        String language = sPreferences.getString(sContext.getString(R.string.lang_key), "en");
        refreshConfig(language);
    }

    private static void refreshConfig(String language) {
        if (null == sContext || null == sPreferences){
            throw new IllegalArgumentException("Should initi LanguageHelper from Application class");
        }
        Locale locale = new Locale(language);
        Locale.setDefault(locale);
        android.content.res.Configuration config = new android.content.res.Configuration();
        config.locale = locale;
        sContext.getResources().updateConfiguration(config, sContext.getResources().getDisplayMetrics());
    }


}
