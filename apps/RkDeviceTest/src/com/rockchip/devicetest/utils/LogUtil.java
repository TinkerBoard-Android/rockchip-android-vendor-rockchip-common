/*******************************************************************
* Company:     Fuzhou Rockchip Electronics Co., Ltd
* Filename:    LogUtil.java  
* Description:   
* @author:     fxw@rock-chips.com
* Create at:   2011-11-11 上午10:35:08  
* 
* Modification History:  
* Date         Author      Version     Description  
* ------------------------------------------------------------------  
* 2011-11-11      xwf         1.0         create
*******************************************************************/
package com.rockchip.devicetest.utils;

import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

public class LogUtil{
	
	private static final String TAG = "Rockchip";
	public static boolean ENABLED = true;
	
	public static void show(Context context, String msg){
		Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
	}
	
	public static void show(Handler handler, final Context context, final String msg){
		handler.post(new Runnable(){
			public void run() {
				Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
			}
		});
	}
	
	public static void d(String msg){
		if(ENABLED){
			Log.d(TAG+"[DEBUG]", msg);
		}
	}

	public static void d(Object obj, String msg){
		if(ENABLED){
			Log.d(obj.getClass().getSimpleName(), msg);
		}
	}
	
	public static void i(Object obj, String msg){
		if(ENABLED){
			Log.i(obj.getClass().getSimpleName(), msg);
		}
	}
	
	public static void v(Object obj, String msg){
		if(ENABLED){
			Log.v(obj.getClass().getSimpleName(), msg);
		}
	}
	
	public static void e(Object obj, String msg){
		Log.e(obj.getClass().getSimpleName(), msg);
	}
	public static void e(Object obj, String msg, Exception ex){
		Log.e(obj.getClass().getSimpleName(), msg, ex);
	}
	public static void e(String msg, Exception ex){
		Log.e(TAG+"[ERROR]", msg, ex);
	}
	
	
	public static void w(Object obj, String msg){
		if(ENABLED){
			Log.w(obj.getClass().getSimpleName(), msg);
		}
	}
}
