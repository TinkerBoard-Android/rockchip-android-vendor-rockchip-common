/*******************************************************************
* Company:     Fuzhou Rockchip Electronics Co., Ltd
* Description:   
* @author:     fxw@rock-chips.com
* Create at:   2014年5月12日 下午6:17:42  
* 
* Modification History:  
* Date         Author      Version     Description  
* ------------------------------------------------------------------  
* 2014年5月12日      fxw         1.0         create
*******************************************************************/   

package com.rockchip.devicetest.utils;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;

import android.content.Context;

public class FileUtils {

	/**
	 * 读取文件
	 * @param file
	 * @return 字符串
	 */
	public static String readFromFile(File file) {
        if((file != null) && file.exists()) {
            try {
                FileInputStream fin= new FileInputStream(file);
                BufferedReader reader= new BufferedReader(new InputStreamReader(fin));
                String value = reader.readLine();
                fin.close();
                return value;
            } catch(IOException e) {
            	return null;
            }
        }
        return null;
    }
	
	/**
	 * 文件中写入字符串
	 * @param file
	 * @param enabled
	 */
	public static boolean write2File(File file, String value) {
        if((file == null) || (!file.exists())) return false;
        try {
            FileOutputStream fout = new FileOutputStream(file);
            PrintWriter pWriter = new PrintWriter(fout);
            pWriter.println(value);
            pWriter.flush();
            pWriter.close();
            fout.close();
            return true;
        } catch(IOException re) {
        	return false;
        }
    }
	
	
	/**
	 * 将Asset下的文件复制到/data/data/.../files/目录下
	 * @param context
	 * @param fileName
	 */
	public static boolean copyFromAsset(Context context, String fileName, boolean recreate) {
		byte[] buf = new byte[20480];
		try {
			File fileDir = context.getFilesDir();
			if(!fileDir.exists()){
				fileDir.mkdirs();
			}
			String destFilePath = fileDir.getAbsolutePath()+File.separator+fileName;
			File destFile = new File(destFilePath);
			if(!destFile.exists() || recreate){
				destFile.createNewFile();
			}else{
				return true;
			}
			FileOutputStream os = new FileOutputStream(destFilePath);// 得到数据库文件的写入流
			InputStream is = context.getAssets().open(fileName);// 得到数据库文件的数据流
			int cnt = -1;
			while ((cnt = is.read(buf)) != -1) {
				os.write(buf, 0, cnt);
			}
			os.flush();
			is.close();
			os.close();
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		} finally {
			buf = null;
		}
	}
	
	/**
	 * 修改文件权限
	 */
	public static void chmodDataFile(Context context, String fileName){
		File fileDir = context.getFilesDir();
		String destFilePath = fileDir.getAbsolutePath()+File.separator+fileName;
		SystemBinUtils.chmod("777",destFilePath);
	}
	
	public static String getDataFileFullPath(Context context, String fileName){
		File fileDir = context.getFilesDir();
		String destFilePath = fileDir.getAbsolutePath()+File.separator+fileName;
		return destFilePath;
	}
	
	/**
	 * 读取文件内容
	 * @param file
	 * @return
	 */
	public static byte[] readFileContent(File file){
		InputStream fin = null;
		try {
			fin = new FileInputStream(file);
			byte[] readBuffer = new byte[20480];
			int readLen = 0;
			ByteArrayOutputStream contentBuf = new ByteArrayOutputStream();
			while((readLen=fin.read(readBuffer))>0){
				contentBuf.write(readBuffer, 0, readLen);
			}
			return contentBuf.toByteArray();
		} catch (Exception e) {
		} finally{
			if(fin!=null){
				try {
					fin.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return null;
	}
}
