package com.rockchip.devicetest;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import android.util.Log;

import com.rockchip.devicetest.utils.LogUtil;
import com.rockchip.devicetest.utils.StorageUtils;

import android.content.Context;
import android.os.Environment;
import android.os.storage.StorageManager;

public class ConfigFinder {
	
       private Context mContext;
	/**
	 * 查找配置文件
	 * @return
	 */
	public static File findConfigFile(String file, Context mContext){
		if(file==null) return null;
	StorageManager mStorageManager = (StorageManager)mContext.getSystemService(Context.STORAGE_SERVICE);	
		//0.Absolute

                                Log.d("hjc","findConfigFile:"+file);
		if(file.startsWith("/")||file.startsWith("\\")){
			return new File(file);
		}
		
		File existedFile = null;
		
		//1.External SDCard
		if(StorageUtils.getSDcardDir(mStorageManager)!=null){
                       File cardfile = new File(StorageUtils.getSDcardDir(mStorageManager));
                       existedFile = new File(cardfile,file);
                       if(existedFile.exists())
			return existedFile;
		}
		
		//2.USB
		List<String> usbList = getAliveUsbPath(mContext);
		for(String usb : usbList){
			existedFile = new File(usb, file);
                                Log.d("hjc","usb file:"+existedFile);
			if(existedFile.exists()){
				return existedFile;
			}
		}
		
		//3.Internal SDCard
		File sdDir = Environment.getExternalStorageDirectory();
		existedFile = new File(sdDir, file);
		if(existedFile.exists()){
			return existedFile;
		}
		//Not Found
		return null;
	}
	private static String getSubUsbPath(String usbPath){
		Process process;
	    String temp;
	    try {
	        process = Runtime.getRuntime().exec("/system/bin/ls "+usbPath);
	        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
	        while ((temp = reader.readLine()) != null) {
	            if (true/*temp.startsWith("udisk") && !temp.equals("udisk")*/) {
	            	usbPath += "/"+temp;
	                reader.close();
	                process.destroy();
	                return usbPath;
	            }
	        }
	        return usbPath;
	    } catch (IOException e) {
	        e.printStackTrace();
	        return usbPath;
	    }
	}
	
	/**
	 * 是否存在此配置文件
	 * @param file
	 * @return
	 */
	public static boolean hasConfigFile(String file,Context context){
		File searchFile = findConfigFile(file,context);
		boolean isExisted = searchFile!=null&&searchFile.exists();
		return isExisted;
	}
	
	/**
	 * 获取已经挂载的U盘
	 * @return
	 */
	public static List<String> getAliveUsbPath(Context mContext){
                StorageManager mStorageManager = (StorageManager)mContext.getSystemService(Context.STORAGE_SERVICE);
		List<String> usbList = new ArrayList<String>();
	/*	if(Environment.MEDIA_MOUNTED.equals(Environment.getHostStorage_Extern_0_State())){
			String udisk0 = Environment.getHostStorage_Extern_0_Directory().getAbsolutePath();
			usbList.add(udisk0);
		}
		if(Environment.MEDIA_MOUNTED.equals(Environment.getHostStorage_Extern_1_State())){
			String udisk1 = Environment.getHostStorage_Extern_1_Directory().getAbsolutePath();
			usbList.add(udisk1);
		}
		if(Environment.MEDIA_MOUNTED.equals(Environment.getHostStorage_Extern_2_State())){
			String udisk2 = Environment.getHostStorage_Extern_2_Directory().getAbsolutePath();
			usbList.add(udisk2);
		}
		if(Environment.MEDIA_MOUNTED.equals(Environment.getHostStorage_Extern_3_State())){
			String udisk3 = Environment.getHostStorage_Extern_3_Directory().getAbsolutePath();
			usbList.add(udisk3);
		}
		if(Environment.MEDIA_MOUNTED.equals(Environment.getHostStorage_Extern_4_State())){
			String udisk4 = Environment.getHostStorage_Extern_4_Directory().getAbsolutePath();
			usbList.add(udisk4);
		}
		if(Environment.MEDIA_MOUNTED.equals(Environment.getHostStorage_Extern_5_State())){
			String udisk5 = Environment.getHostStorage_Extern_5_Directory().getAbsolutePath();
			usbList.add(udisk5);
		}
        */
                usbList = StorageUtils.getUsbPaths(mStorageManager);
                Log.d("hjc","======getAliveUsbPath:"+usbList);
		return usbList;
	}

}
