package com.cghs.stresstest.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

import android.util.Log;
import android.content.Context;
import android.os.SystemProperties;
import android.content.SharedPreferences;

public class StresstestUtil {
    public static final String TAG = "StresstestUtil";
    private static File panicFile = new File("/sys/module/kernel/parameters/panic");

    public static void getBootMode(boolean enable)
            throws FileNotFoundException, IOException {
        if (enable) {
            SystemProperties.set("ctl.start", "getbootmode");
            String mount_rt_add;
            while (true) {
                mount_rt_add = SystemProperties.get("init.svc.getbootmode", "");
                Log.d("-----------------------------------** StressTest--getbootmode>> **", "mount_rt  " + mount_rt_add);
                if (mount_rt_add != null && mount_rt_add.equals("stopped")) {
                    break;
                }
                try {
                    Thread.sleep(1000);
                } catch (Exception ex) {
                    Log.e("-----------------------------------** StressTest--getbootmode>> **", "Exception: " + ex.getMessage());
                }
            }
        }
    }

    public static boolean readState(String fileName, HashMap<String, String> map) {
        String value;
        File file = new File(fileName);
        if (file == null || !file.exists()) {
            return false;
        }
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(file));
            String tempString = null;
            while ((tempString = reader.readLine()) != null) {
                String[] temp = tempString.split(":");
                if (temp.length < 2) {
                    Log.e(TAG, "recovery test state file phase err.");
                    return false;
                }
                map.put(temp[0], temp[1]);
            }
            reader.close();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e1) {
                }
            }
        }
        return false;
    }

    public static void writeState(String content, String path) {
        writeState(content, path, false);
    }

    public static void writeState(String content, String path, boolean append) {
        FileOutputStream fos = null;
        File file = new File(path);

        if (file != null && !file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try {
            fos = new FileOutputStream(file);
            fos.write(content.getBytes());
            fos.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException ie) {
            ie.printStackTrace();
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
