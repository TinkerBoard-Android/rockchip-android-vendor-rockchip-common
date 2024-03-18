package com.rockchip.devicetest.testcase;



import java.io.File; 

import android.content.Context;
import android.provider.Settings;

import com.rockchip.devicetest.utils.FileUtils;

public class UsbSettings {

	public enum UsbMode {
		HOST("1"),
		SLAVE("2");
		
		public String value;
		private UsbMode(String value){
			this.value = value;
		}
		
		public static UsbMode getMode(String mode){
			for(UsbMode um : UsbMode.values()){
				if(um.value.equals(mode)){
					return um;
				}
			}
			return null;
		}
	}

    private static final String SYS_USB_FILE = "/sys/bus/platform/drivers/usb20_otg/force_usb_mode";

    /**
     * 获得当前Usb mode
     */
    public static UsbMode getCurrentUsbMode(){
    	File file = new File(SYS_USB_FILE);
    	String mode = FileUtils.readFromFile(file);
    	return UsbMode.getMode(mode);
    }
    
    /**
     * 设置成Host mode
     */
    public static boolean setUsbHostMode(){
    	UsbMode mode = getCurrentUsbMode();
    	if(UsbMode.HOST != mode){
    		return setUsbMode(UsbMode.HOST);
    	}
    	return true;
    }
    
    /**
     * 设置成Slave mode
     */
    public static boolean setUsbSlaveMode(){
    	UsbMode mode = getCurrentUsbMode();
    	if(UsbMode.SLAVE != mode){
    		return setUsbMode(UsbMode.SLAVE);
    	}
    	return true;
    }
    
    /**
     * 设置Usb mode
     */
    public static boolean setUsbMode(UsbMode mode){
    	File file = new File(SYS_USB_FILE);
    	return FileUtils.write2File(file, mode.value);
    }
    
    /**
     * 开启ADB
     * @param context
     */
    public static void enableADB(Context context){
    	Settings.Global.putInt(context.getContentResolver(),Settings.Global.ADB_ENABLED, 1);
    }
    
    /**
     * 关闭ADB
     * @param context
     */
    public static void disableADB(Context context){
    	Settings.Global.putInt(context.getContentResolver(),Settings.Global.ADB_ENABLED, 0);
    }

}
