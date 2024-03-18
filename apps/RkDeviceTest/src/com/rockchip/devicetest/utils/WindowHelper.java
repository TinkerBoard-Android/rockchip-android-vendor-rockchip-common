/*******************************************************************
* Company:     Fuzhou Rockchip Electronics Co., Ltd
* Description:   
* @author:     fxw@rock-chips.com
* Create at:   2014��4��15�� ����4:48:16  
* 
* Modification History:  
* Date         Author      Version     Description  
* ------------------------------------------------------------------  
* 2014��4��15��      fxw         1.0         create
*******************************************************************/   

package com.rockchip.devicetest.utils;

import android.app.Activity;
import android.os.Build;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.Window;
import android.view.WindowManager;

public class WindowHelper {
	
	private static int mScreenWidth = -1;
	private static int mScreenHeight = -1;

	/**
	 * 设置全屏
	 * @param window
	 */
    public static void setFullScreen(Window window){
    	if(Build.VERSION.SDK_INT<=14) return;
    	int flag = getViewStaticProperty("SYSTEM_UI_FLAG_FULLSCREEN")
    			|getViewStaticProperty("SYSTEM_UI_FLAG_HIDE_NAVIGATION")
    			|getViewStaticProperty("SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN")
    			|getViewStaticProperty("SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION")
    			|getViewStaticProperty("SYSTEM_UI_FLAG_IMMERSIVE_STICKY");
    	ReflectionUtils.invokeMethod(window.getDecorView(), "setSystemUiVisibility",new Class[]{int.class}, flag);	
    }
    private static int getViewStaticProperty(String prop){
    	Object ret = ReflectionUtils.getStaticFieldValue("android.view.View", prop);
    	return ret==null?0:(Integer)ret;
    }
    
    /**
     * 获取Window宽高
     */
	public static Display getWindowDisplay(Window window){
		return window.getWindowManager().getDefaultDisplay();
	}
	public static Display getWindowDisplay(Activity activity){
		return activity.getWindowManager().getDefaultDisplay();
	}
	
	/**
	 * 获取屏幕宽度
	 * @return
	 */
	public static int getWinWidth(Activity activity){
		return getWidth(activity.getWindowManager());
	}
	public static int getWinWidth(Window win){
		return getWidth(win.getWindowManager());
	}
	private static int getWidth(WindowManager wm){
		if(mScreenWidth==-1){//init
			DisplayMetrics displayMetrics = new DisplayMetrics();	        
			wm.getDefaultDisplay().getMetrics(displayMetrics);
			mScreenWidth = displayMetrics.widthPixels;
			mScreenHeight = displayMetrics.heightPixels;
		}
		return mScreenWidth;
	}
	
	/**
	 * 获取屏幕高度
	 * @return
	 */
	public static int getWinHeight(Activity activity){
		return getHeight(activity.getWindowManager());
	}
	public static int getWinHeight(Window win){
		return getHeight(win.getWindowManager());
	}
	private static int getHeight(WindowManager wm){
		if(mScreenHeight==-1){//init
			DisplayMetrics displayMetrics = new DisplayMetrics();	        
			wm.getDefaultDisplay().getMetrics(displayMetrics);
			mScreenWidth = displayMetrics.widthPixels;
			mScreenHeight = displayMetrics.heightPixels;
		}
		return mScreenHeight;
	}
	
}
