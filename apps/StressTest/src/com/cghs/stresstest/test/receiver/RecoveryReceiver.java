package com.cghs.stresstest.test.receiver;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import com.cghs.stresstest.test.RecoveryTest;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.app.KeyguardManager;
import android.content.Intent;
import android.util.Log;
import android.os.storage.VolumeInfo;
import android.os.storage.DiskInfo;
import android.os.storage.StorageVolume;
import android.os.storage.StorageManager;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Collections;


public class RecoveryReceiver extends BroadcastReceiver {
    public static final String TAG = "RecoveryReceiver";

    private int mIsStart = -1;
    private int mCurCount = -1;
    private int mMaxCount = -1;
    private boolean mIsWipeAll = false;
    private boolean mIsEraseFlash = false;
    private boolean mIsCheckWifiBt = false;
    private boolean mIsCheckMobileData = false;
    private static boolean mIs_mounted = false;
    private static boolean mIs_booted = false;

    @Override
    public void onReceive(Context context, Intent intent) {
        try {
            Thread.sleep(1500);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        //if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {


        boolean result = readState(RecoveryTest.RECOVERY_STATE_FILE);
        if (!result) {
            result = readState(getRECOVERY_STATE_FILE_TF(context));
            if (!result)
                return;
        }

        if (mIsStart == 0) {
            return;
        }
        Intent startIntent = new Intent(context, RecoveryTest.class);
        startIntent.putExtra("enable", mIsStart);
        startIntent.putExtra("cur", mCurCount);
        startIntent.putExtra("max", mMaxCount);
        startIntent.putExtra("wipeall", mIsWipeAll);
        startIntent.putExtra("eraseflash", mIsEraseFlash);
        startIntent.putExtra("checkwifibt", mIsCheckWifiBt);
        startIntent.putExtra("checkmobiledata", mIsCheckMobileData);
        startIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(startIntent);
    }


    public boolean readState(String fileName) {
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
                if (temp[0].equals("enable")) {
                    mIsStart = Integer.valueOf(temp[1]);
                } else if (temp[0].equals("currenttime")) {
                    mCurCount = Integer.valueOf(temp[1]);
                } else if (temp[0].equals("maxtime")) {
                    mMaxCount = Integer.valueOf(temp[1]);
                    if (mMaxCount < 0)
                        mMaxCount = 0;
                } else if (temp[0].equals("wipeall")) {
                    mIsWipeAll = (Integer.valueOf(temp[1]) == 1) ? true : false;
                } else if (temp[0].equals("eraseflash")) {
                    mIsEraseFlash = (Integer.valueOf(temp[1]) == 1) ? true : false;
                } else if (temp[0].equals("checkwifibt")) {
                    mIsCheckWifiBt = (Integer.valueOf(temp[1]) == 1) ? true : false;
                } else if (temp[0].equals("checkmobiledata")) {
                    mIsCheckMobileData = (Integer.valueOf(temp[1]) == 1) ? true : false;
                }
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

    public String getRECOVERY_STATE_FILE_TF(Context context) {
        String RECOVERY_STATE_FILE_TF = "/mnt/external_sd/Recovery_state";
        StorageManager mStorageManager = (StorageManager) context.getSystemService(StorageManager.class);
        final List<VolumeInfo> volumes = mStorageManager.getVolumes();
        Collections.sort(volumes, VolumeInfo.getDescriptionComparator());
        for (VolumeInfo vol : volumes) {
            if (vol.getType() == VolumeInfo.TYPE_PUBLIC) {
                DiskInfo disk = vol.getDisk();
                if (disk != null) {
                    if (disk.isSd()) {
                        //sdcard dir
                        StorageVolume sv = vol.buildStorageVolume(context, context.getUserId(), false);
                        String sdcard_dir = sv.getPath();
                        RECOVERY_STATE_FILE_TF = new String(sdcard_dir) + "/Recovery_state";
                    } else if (disk.isUsb()) {
                        //usb dir
                        StorageVolume sv = vol.buildStorageVolume(context, context.getUserId(), false);
                        String usb_dir = sv.getPath();
                        RECOVERY_STATE_FILE_TF = new String(usb_dir) + "/Recovery_state";
                    }
                }
            }
        }

        Log.d(TAG, "RECOVERY_STATE_FILE_TF:" + RECOVERY_STATE_FILE_TF);
        return RECOVERY_STATE_FILE_TF;
    }
}
