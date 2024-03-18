package com.rockchip.devicetest.utils;

import java.io.DataOutputStream;
import java.io.InputStream;


public class SystemBinUtils {
	
	public static final String TAG = "SystemBin";
	
    public static boolean execCommand(String cmd){
    	Process process = null;
        DataOutputStream os = null;
        try{
            process = Runtime.getRuntime().exec(cmd);
            os = new DataOutputStream(process.getOutputStream());
            os.writeBytes("\n");
            os.flush();
            os.writeBytes("exit\n");
            os.flush();
            process.waitFor();
        } catch (Exception e) {
        	e.printStackTrace();
            return false;
        } finally {
            try {
                if (os != null)   {
                    os.close();
                }
                process.destroy();
            } catch (Exception e) {
            	e.printStackTrace();
            }
        }
        return true;
    }
    
    public static boolean execCommand(String cmd, CommandResponseListener listener){
    	Process process = null;
        DataOutputStream os = null;
        try{
            process = Runtime.getRuntime().exec(cmd);
            os = new DataOutputStream(process.getOutputStream());
            os.writeBytes("\n");
            os.flush();
            os.writeBytes("exit\n");
            os.flush();
            if(listener!=null){
            	InputStream resIn = process.getInputStream();
            	InputStream errIn = process.getErrorStream();
            	listener.onResponse(resIn, errIn);
            }
            process.waitFor();
        } catch (Exception e) {
        	e.printStackTrace();
            return false;
        } finally {
            try {
                if (os != null)   {
                    os.close();
                }
                process.destroy();
            } catch (Exception e) {
            	e.printStackTrace();
            }
        }
        return true;
    }
    
    public static void chmod(String paramString){
    	String str = "chmod 700 " + paramString;
    	execCommand(str);
    }
    
    public static void chmod(String mode, String paramString){
    	String str = "chmod "+ mode +" " + paramString;
    	execCommand(str);
    }
    
    public static void deleteFile(String paramString){
    	String str = "rm -r " + paramString;
    	execCommand(str);
    }
    
    public interface CommandResponseListener{
    	public void onResponse(InputStream resIn, InputStream errIn);
    	
    }

}
