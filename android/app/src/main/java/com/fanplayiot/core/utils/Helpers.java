package com.fanplayiot.core.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;


import com.demoproject.BuildConfig;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.NetworkInterface;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class Helpers {
    public static final long WEEK_IN_TIMESTAMP = 604800000L;

    public static Integer calculateAverage(List<Integer> datas) {
        Integer sum = 0;
        if (!datas.isEmpty()) {
            for (Integer data : datas) {
                sum += data;
            }
            return sum / datas.size();
        }
        return sum;
    }

    public static boolean isInternetAvailable(@NonNull Context context) {
        ConnectivityManager connMgr = (ConnectivityManager) context.
                getSystemService(Context.CONNECTIVITY_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            NetworkCapabilities capabilities = connMgr.getNetworkCapabilities(connMgr.getActiveNetwork());
            return capabilities != null && capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);
        } else {
            return isConnected_below_M(connMgr);
        }
    }

    @SuppressWarnings("deprecation")
    private static boolean isConnected_below_M(ConnectivityManager connMgr) {
        NetworkInfo activeInfo = connMgr.getActiveNetworkInfo();
        return activeInfo != null && activeInfo.isConnected();
    }
    public static File getLocalBitmapUri(@NonNull Context context, @NonNull Bitmap bitmap, @NonNull String prefix) {
        File file = null;
        FileOutputStream outputStream = null;
        try {
            file = new File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), prefix + System.currentTimeMillis() + ".png");
            outputStream = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 90, outputStream);

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    // error
                }
            }
        }
        return file;
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private static Uri getImageUriForFile(@NonNull Context context, @NonNull String prefix) {
        Uri imageUri = null;
        try {
            File folder = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
            if (folder != null) {
                folder.mkdirs();
            }

            File file = new File(folder, prefix + System.currentTimeMillis() + ".png");
            if (file.exists()) file.delete();
            else file.createNewFile();
            imageUri = FileProvider.getUriForFile(
                    context,
                    BuildConfig.APPLICATION_ID + ".provider",
                    file);
        } catch (Exception e) {
            // error
        }
        return imageUri;
    }

    /**
     * Returns MAC address of the given interface name.
     *
     * @return mac address or empty string
     */
    @Nullable
    public static String getMACAddress() {
        try {
            List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface intf : interfaces) {

                if (!intf.getName().equalsIgnoreCase("wlan0")) continue;

                byte[] mac = intf.getHardwareAddress();
                if (mac == null) return null;
                StringBuilder buf = new StringBuilder();
                for (byte aMac : mac) buf.append(String.format("%02X:", aMac));
                if (buf.length() > 0) buf.deleteCharAt(buf.length() - 1);
                return buf.toString();
            }
        } catch (Exception ignored) {
        } // for now eat exceptions
        return null;
    }

    public static String getPhoneDetails() {
        return "VERSION.RELEASE : " + Build.VERSION.RELEASE
                + ", MANUFACTURER : " + Build.MANUFACTURER
                + ", MODEL : " + Build.MODEL;
    }

    public static boolean checkBloodSugar(int value) {
        return (value >= 50 & value <= 300);
    }

    public static boolean checkBloodPressureSystolic(int value) {
        return (value >= 90 & value <= 140);
    }

    public static boolean checkBloodPressureDiastolic(int value) {
        return (value >= 60 & value <= 90);
    }

    public static boolean checkHeartRate(int value) {
        return (value >= 50 & value <= 220);
    }

    public static boolean checkHourInDay(int value) {
        return (value >= 0 & value <= 24);
    }

    public static boolean checkMinuteInHour(int value) {
        return (value >= 0 & value <= 60);
    }

    public static boolean checkSteps(int value) {
        return (value >= 0 & value <= 999999);
    }

    public static boolean checkCalories(int value) {
        return (value >= 0 & value <= 9999);
    }

    public static boolean checkDistance(int value) {
        return (value >= 0 & value <= 99);
    }

    /**
     * Method returns long value of current date without hours, minutes, seconds, millis etc
     *
     * @return long today's date
     */
    public static Date getTodayStartOfDay() {
        long now = System.currentTimeMillis();
        long today = now - now % 86400000;
        return new Date(today);
    }

    /**
     * Calculate no of months between two dates
     *
     * @param start start date
     * @param end   end date
     * @return int no .of months
     */
    public static int monthsBetween(@NonNull Date start, @NonNull Date end) {
        Calendar cal = Calendar.getInstance();
        Date temp = end;
        if (start.before(end)) {
            cal.setTime(start);
        } else {
            cal.setTime(end);
            temp = start;
        }
        int c = 0;
        while (cal.getTime().before(temp)) {
            cal.add(Calendar.MONTH, 1);
            c++;
        }
        return c - 1;
    }

    public static String[] getXAxisBarValueWeek() {
        return new String[]{"SUN", "MON", "TUE", "WED", "THU", "FRI", "SAT"};
    }

    public static String[] getXAxisBarValueMonth() {
        return new String[]{"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
    }

    public static String getMonthString(int month) {
        if (month >= 0 && month < 12)
            return getXAxisBarValueMonth()[month - 1];
        else
            return getXAxisBarValueMonth()[0];
    }
}
