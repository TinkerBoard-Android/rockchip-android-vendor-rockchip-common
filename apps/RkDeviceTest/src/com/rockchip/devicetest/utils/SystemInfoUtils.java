/*******************************************************************
* Company:     Fuzhou Rockchip Electronics Co., Ltd
* Description:   
* @author:     fxw@rock-chips.com
* Create at:   2014年5月8日 下午5:06:15  
* 
* Modification History:  
* Date         Author      Version     Description  
* ------------------------------------------------------------------  
* 2014年5月8日      fxw         1.0         create
*******************************************************************/   

package com.rockchip.devicetest.utils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Environment;
import android.os.StatFs;
import android.text.format.Formatter;
import android.util.Log;

public class SystemInfoUtils {

    private static final String FILENAME_PROC_VERSION = "/proc/version";
    private static final String FILENAME_MEMINFO = "/proc/meminfo";
    private static final String FILENAME_MAC = "/sys/class/net/eth0/address";
	
    
    /**
     * Reads a line from the specified file.
     * @param filename the file to read from
     * @return the first line, if any.
     * @throws IOException if the file couldn't be read
     */
    private static String readLine(String filename) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(filename), 256);
        try {
            return reader.readLine();
        } finally {
            reader.close();
        }
    }
   
   /**
     * 获取Mac地址
     */
    public static String getMac(Context context){
       String mac ="";
       try {  
          mac =readLine(FILENAME_MAC);
       }catch(Exception e){
       }
       return mac;
    }
 
    /**
     * 获取flash容量
     * @param context
     * @return
     */
    public static String getFormattedFlashSpace(Context context){
    	String[] partitions = {"/dev", "/system", "/cache", "/metadata", "/data", "/mnt/internal_sd"};
    	long flashSize = 0;
    	for(String part : partitions){
    		try{
	    		StatFs stat = new StatFs(part);
				long blockSize = stat.getBlockSize();
				long totalBlocks = stat.getBlockCount();
				flashSize += blockSize*totalBlocks;
    		}catch(Exception e){
    		}
    	}
    	flashSize = (long)(Math.ceil(flashSize/1024.00/1024.00/1024.00)*1024*1024*1024);
        String szie=Formatter.formatFileSize(context, flashSize);
            if((flashSize/1024/1024/1024)>4 && (flashSize/1024/1024/1024)<8)
                      szie="8GB";
                if((flashSize/1024/1024/1024)>8 && (flashSize/1024/1024/1024)<16)
                      szie="16GB";
                   if((flashSize/1024/1024/1024)>16 && (flashSize/1024/1024/1024)<32)
                      szie="32GB";
    	return szie;
    }
    
    /**
     * 获取内存容量
     */
    public static String getFormattedRamSpace(Context context){
    	try {
			String line = readLine(FILENAME_MEMINFO);
	        int begin = line.indexOf(':');
	        int end = line.indexOf('k');
	        line = line.substring(begin + 1, end).trim();
	        int total = Integer.parseInt(line);
                String space = Formatter.formatFileSize(context, total*1024);
                if(total> 2200000)
                      space="4GB";
                if(total> 1200000 && total<2200000)
                      space="2GB";
                if(total> 530000 && total<1200000)
                      space="1GB";
                if(total> 270000 && total<530000)
                      space="512MB";
	        return space;
		} catch (Exception e) {
		}
    	return null;
    }
    
    /**
     * 获取App版本信息
     */
    public static String getAppVersionName(Context context){
    	try {
			PackageInfo pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), PackageManager.GET_META_DATA);
			return pInfo.versionName;
    	} catch (NameNotFoundException e) {
    		return null;
		}  
    }
    
    /**
     * 获取内核版本
     * @return
     */
	public static String getFormattedKernelVersion() {
        try {
            return formatKernelVersion(readLine(FILENAME_PROC_VERSION));

        } catch (IOException e) {
            LogUtil.e(SystemInfoUtils.class,  "IO Exception when getting kernel version for Device Info screen");
            return "Unavailable";
        }
    }

    public static String formatKernelVersion(String rawKernelVersion) {
        // Example (see tests for more):
        // Linux version 3.0.31-g6fb96c9 (android-build@xxx.xxx.xxx.xxx.com) \
        //     (gcc version 4.6.x-xxx 20120106 (prerelease) (GCC) ) #1 SMP PREEMPT \
        //     Thu Jun 28 11:02:39 PDT 2012

        final String PROC_VERSION_REGEX =
            "Linux version (\\S+) " + /* group 1: "3.0.31-g6fb96c9" */
            "\\((\\S+?)\\) " +        /* group 2: "x@y.com" (kernel builder) */
            "(?:\\(gcc.+? \\)) " +    /* ignore: GCC version information */
            "(#\\d+) " +              /* group 3: "#1" */
            "(?:.*?)?" +              /* ignore: optional SMP, PREEMPT, and any CONFIG_FLAGS */
            "((Sun|Mon|Tue|Wed|Thu|Fri|Sat).+)"; /* group 4: "Thu Jun 28 11:02:39 PDT 2012" */

        Matcher m = Pattern.compile(PROC_VERSION_REGEX).matcher(rawKernelVersion);
        if (!m.matches()) {
        	LogUtil.e(SystemInfoUtils.class, "Regex did not match on /proc/version: " + rawKernelVersion);
            return "Unavailable";
        } else if (m.groupCount() < 4) {
        	LogUtil.e(SystemInfoUtils.class, "Regex match on /proc/version only returned " + m.groupCount()
                    + " groups");
            return "Unavailable";
        }
        return m.group(1) + "  " +                 // 3.0.31-g6fb96c9
            m.group(2) + " " + m.group(3) + "  " + // x@y.com #1
            m.group(4);                            // Thu Jun 28 11:02:39 PDT 2012
    }
}
