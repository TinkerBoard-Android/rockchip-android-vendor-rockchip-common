package com.cghs.stresstest.test.receiver;

import java.io.File;

import com.cghs.stresstest.StressTestActivity;
import com.cghs.stresstest.test.WifiOpenTest;
import com.cghs.stresstest.test.RebootTest;
import com.cghs.stresstest.test.SleepTest;
import com.cghs.stresstest.test.BluetoothOpenTest;
import com.cghs.stresstest.test.FlyModeOpenTest;
import com.cghs.stresstest.test.RecoveryTest;
import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.FileReader;
import java.io.IOException;
import android.app.Dialog;
import java.io.InputStreamReader;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Collections;
import android.os.storage.VolumeInfo;
import android.os.storage.DiskInfo;
import android.os.storage.StorageVolume;
import android.os.storage.StorageManager;

public class MountReceiver extends BroadcastReceiver {

	private String TAG = "VRMountReceiver";
	private Context mContext;
	private SharedPreferences mSharedPreferences;
	private int mIsStart = -1;
	private int mCurCount = -1;
	private int mMaxCount = -1;
	private boolean mIsWipeAll = false;
	private boolean mIsEraseFlash = false;


	public MountReceiver() {
		// this.mContext = getPac
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO: This method is called when the BropaadcastReceiver is receiving
		// an Intent broadcast.
		// throw new UnsupportedOperationException("Not yet implemented");
		String action = intent.getAction();
		String devicePath = null;
		Log.e(TAG, "---------------- action = " + action);
		if(intent.getData() != null){
			Uri uri = intent.getData();
			if(uri != null)
				devicePath = uri.getPath();
		}
		if (action.equals(Intent.ACTION_MEDIA_MOUNTED)) {
			//Environment
			Uri pathUri = intent.getData();
			//Environment
			if (pathUri != null) {
				String path = pathUri.getPath();
				Log.e(TAG, "------------- mount path = " + path);
				File dirFile = new File(path);
				File checkFile = new File(dirFile, "RK_StressTest.txt");
				if (checkFile.exists()) {
						// 启动Activity页面
				/*		Intent startIntent = new Intent(context,
								StressTestActivity.class);
						startIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK
								| Intent.FLAG_ACTIVITY_NEW_TASK);
						context.startActivity(startIntent);
				*/
					//启动reboot test or wifi test
						mSharedPreferences = context.getSharedPreferences("state", 0);
						int rebootFlag = mSharedPreferences.getInt("reboot_flag", 0);
						int rebootCount= mSharedPreferences.getInt("reboot_count", 0);
						int maxtime= mSharedPreferences.getInt("reboot_max", 0);
						
			                        Log.d(TAG,"=========onReceive===rebootFlag:"+rebootFlag);
						if (rebootFlag == 0&&(isDoTest(checkFile)==1)) {
							Log.d(TAG,"=========start vr reboot now======");
							saveSharedPreferences(1,0);
							Intent rebootintent = new Intent(context, RebootTest.class);
							rebootintent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
							context.startActivity(rebootintent);
							//not 
						} else if(isDoTest(checkFile)==0){
						   	Log.d(TAG,"=========stop vr reboot test now======");
							saveSharedPreferences(0,0);
						}else if(isDoTest(checkFile)==2){
							Log.d(TAG,"=========start vr wifi test now======");
							Intent wifiintent = new Intent(context, WifiOpenTest.class);
							wifiintent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
							wifiintent.putExtra("auto",1);
							context.startActivity(wifiintent);
							
						}else if(isDoTest(checkFile)==3){
							Log.d(TAG,"=========start vr sleep test now======");
							Intent sleepintent = new Intent(context, SleepTest.class);
							sleepintent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
							sleepintent.putExtra("auto",1);
							context.startActivity(sleepintent);
						}else if(isDoTest(checkFile)==4){
							Log.d(TAG,"=========start vr BluetoothOpen Test now======");
							Intent bluetoothintent = new Intent(context, BluetoothOpenTest.class);
							bluetoothintent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
							bluetoothintent.putExtra("auto",1);
							context.startActivity(bluetoothintent);	
							}else if(isDoTest(checkFile)==5){
							Log.d(TAG,"=========start vr FlyModeOpen Test now======");
							Intent flyintent = new Intent(context, FlyModeOpenTest.class);
							flyintent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
							flyintent.putExtra("auto",1);
							context.startActivity(flyintent);
							}else if(isDoTest(checkFile)==6){
								boolean result = readState(RecoveryTest.RECOVERY_STATE_FILE);
								if (!result ) {
									result = readState(getRECOVERY_STATE_FILE_TF(context));
								}		
								if (mIsStart == 1) {
									return ;
								}
							Log.d(TAG,"=========start vr RecoveryTest Test now======");
							Intent recoveryintent = new Intent(context, RecoveryTest.class);
							recoveryintent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
							recoveryintent.putExtra("enable",1);
							context.startActivity(recoveryintent);
						}
			} else {
				Log.e(TAG, "---------------- pathUri is null ");
			}
		
		}
	}
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
            	} else if (temp[0].equals("maxtime")){
            		mMaxCount = Integer.valueOf(temp[1]);
                        if(mMaxCount < 0)
                           mMaxCount = 0;
            	} else if (temp[0].equals("wipeall")) {
            		mIsWipeAll = (Integer.valueOf(temp[1]) == 1) ? true : false;
            	} else if (temp[0].equals("eraseflash")) {
            		mIsEraseFlash = (Integer.valueOf(temp[1]) == 1) ? true : false;
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
   public String getRECOVERY_STATE_FILE_TF(Context context){
        String RECOVERY_STATE_FILE_TF="/mnt/external_sd/Recovery_state";
        StorageManager mStorageManager = (StorageManager) context.getSystemService(StorageManager.class);
                final List<VolumeInfo> volumes = mStorageManager.getVolumes();
                Collections.sort(volumes, VolumeInfo.getDescriptionComparator());
                for (VolumeInfo vol : volumes) {
            if (vol.getType() == VolumeInfo.TYPE_PUBLIC) {
                DiskInfo disk = vol.getDisk();
                if(disk != null) {
                        if(disk.isSd()) {
                                //sdcard dir
                                StorageVolume sv = vol.buildStorageVolume(context, context.getUserId(), false);
                               String sdcard_dir = sv.getPath();
                                RECOVERY_STATE_FILE_TF=new String(sdcard_dir)+"/Recovery_state";
                        }else if(disk.isUsb()){
                                //usb dir
                        StorageVolume sv = vol.buildStorageVolume(context, context.getUserId(), false);
                              String usb_dir = sv.getPath();
                                RECOVERY_STATE_FILE_TF=new String(usb_dir)+"/Recovery_state";
                        }
                }
            }
        }

        Log.d(TAG, "RECOVERY_STATE_FILE_TF:" +RECOVERY_STATE_FILE_TF);
        return RECOVERY_STATE_FILE_TF;
    }
    
	public void saveSharedPreferences(int flag, int count) {
		if(mSharedPreferences!=null){
		SharedPreferences.Editor edit = mSharedPreferences.edit();
		edit.putInt("reboot_flag", flag);
		edit.putInt("reboot_max", count);
		edit.commit();
	}
	}
	private int isDoTest(File file) {
		
			try {
	            String encoding="GBK";
	            if(file.isFile() && file.exists()){ //判断文件是否存在
	                InputStreamReader read = new InputStreamReader(
	                new FileInputStream(file),encoding);//考虑到编码格式
	                BufferedReader bufferedReader = new BufferedReader(read);
	                String lineTxt = null;
	                while((lineTxt = bufferedReader.readLine()) != null){
	                	Log.d(TAG,lineTxt);
	                	if(lineTxt.startsWith("#")){
										continue;
										}
										if (lineTxt.contains("reboot 1")) {
										    return 1;
										}
										if (lineTxt.contains("reboot 0")) {
										    return 0;
										}
										if (lineTxt.contains("wifi 1")) {
										    return 2;
										}
										if (lineTxt.contains("sleep 1")) {
										    return 3;
										}
										if (lineTxt.contains("bluetooth 1")) {
										    return 4;
										}
										if (lineTxt.contains("flymode 1")) {
										    return 5;
										}
										if (lineTxt.contains("recovery 1")) {
										    return 6;
										}
	                	
	                }
	                read.close();
	    }else{
	        Log.e(TAG,"not find RK_StressTest.txt");
	    }
	    } catch (Exception e) {
	       Log.e(TAG,"read error!!");
	        e.printStackTrace();
	    }
	    return -1;
	}
}
