package com.syuria.android.reportsales.app;

/**
 * Created by HP on 15/01/2017.
 */

public class AppConfig {
    // Server user login url
    public static String BASE_URL = "http://192.168.1.9:3000/api";

    public static String URL_LOGIN = BASE_URL+"/login/mobile";

    public static String URL_DAILY_REPORT = BASE_URL+"/daily/insert";

    public static String URL_GET_DAILY_REPORT = BASE_URL+"/daily";

    public static String URL_UPDATE_DAILY_REPORT = BASE_URL+"/daily/update";

    public static String URL_UPDATE_PRODUCT_REPORT = BASE_URL+"/product/update";

    public static String URL_GET_PRODUCT = BASE_URL+"/product";

    public static String URL_GET_PRODUCT_FOCUS = BASE_URL+"/focus";

    public static String URL_PRODUCT_REPORT = BASE_URL+"/product/report";

    public static String URL_PRODUCT_FOCUS = BASE_URL+"/focus/report";

    public static String URL_HISTORY_PRODUCT_REPORT = BASE_URL+"/product";
}
