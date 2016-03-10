package com.moushi.msdynamicdemo;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Created by MouShi Beijing on 15/12/29.
 */
public class MSDynamic {

    private static String baseURL = "http://101.200.130.178:7001/keywords/key/active/";
    private static String customUUID;

    public static void activate(final Context context, final String appKey) {
        activateWithUUID(context, appKey, "null");
    }

    public static void activateWithUUID(final Context context, final String appKey, final String uuid) {

        SharedPreferences userDefault = context.getSharedPreferences("msDynamic", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = userDefault.edit();
        editor.putString("appKey", appKey);
        editor.apply();

        boolean hasCustomUUID = false;
        if (!uuid.equals("null")) {
            hasCustomUUID = true;
            customUUID = uuid;
        }

        if (!userDefault.contains("deviceUUID")) {
            sendInfo(context, appKey, false, hasCustomUUID);
        } else if (userDefault.contains("isTwice")) {
            if (!userDefault.getString("isTwice", "notDone").equals("done")) {
                sendInfo(context, appKey, true, hasCustomUUID);
            }
        }
    }

    public static void isTwiceActivate(final Context context) {
        SharedPreferences userDefault = context.getSharedPreferences("msDynamic", Context.MODE_PRIVATE);

        if (!userDefault.contains("appKey")) {
            Log.e("MSDynamic", "Method MSDynamic.isTwiceActivate should be called after method MSDynamic.activate!");
        } else {
            if (!userDefault.contains("isTwice")) {
                SharedPreferences.Editor editor = userDefault.edit();
                editor.putString("isTwice", "notDone");
                editor.apply();

                String appKey = userDefault.getString("appKey", "null");
                boolean hasCustomID = userDefault.contains("hasCustomID");
                if (hasCustomID) {
                    customUUID = userDefault.getString("deviceUUID", "null");
                }
                sendInfo(context, appKey, true, hasCustomID);
            }
        }
    }

    private static void sendInfo(final Context context, final String appKey, final boolean isTwice, final boolean hasCustomID) {
        Thread sendThread = new Thread() {
            @Override
            public void run() {

                String uuid = getUUID(context);
                if (hasCustomID) {uuid = customUUID;}
                String channel = getChannel(context);
                String twice = "0";
                if (isTwice) { twice = "1";}

                try {
                    URL getURL = new URL(baseURL + appKey + "/" + channel + "/" + uuid + "/" + twice);

                    HttpURLConnection connection = (HttpURLConnection) getURL.openConnection();
                    connection.setRequestMethod("GET");
                    connection.connect();

                    InputStream resp = connection.getInputStream();

                    if (connection.getResponseCode() == 200) {
                        SharedPreferences userDefault = context.getSharedPreferences("msDynamic", Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = userDefault.edit();
                        editor.putString("deviceUUID", uuid);
                        if (isTwice) { editor.putString("isTwice", "done");}
                        editor.apply();
                    }

                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };

        sendThread.start();
    }

    private static String getChannel(Context context) {

        ApplicationInfo appInfo = context.getApplicationInfo();
        String sourceDir = appInfo.sourceDir;
        String ret = "";
        ZipFile zipFile = null;

        try {
            zipFile = new ZipFile(sourceDir);
            Enumeration<?> entries = zipFile.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = ((ZipEntry) entries.nextElement());
                String entryName = entry.getName();
                if (entryName.contains("mschannel")) {
                    ret = entryName;
                    break;
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (zipFile != null) {
                try {
                    zipFile.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        String[] split = ret.split("_");
        if (split != null && split.length >= 2) {
            return ret.substring(split[0].length() + 1);
        } else {
            return "null";
        }
    }

    private static String getUUID(Context context) {
        final TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);

        final String tmDevice, tmSerial, androidId;
        tmDevice = "" + tm.getDeviceId();
        tmSerial = "" + tm.getSimSerialNumber();
        androidId = "" + android.provider.Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);

        UUID deviceUUID = new UUID(androidId.hashCode(), ((long)tmDevice.hashCode() << 32) | tmSerial.hashCode());

        return deviceUUID.toString();
    }
}
