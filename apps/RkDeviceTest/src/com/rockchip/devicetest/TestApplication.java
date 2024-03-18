/*******************************************************************
* Company:     Fuzhou Rockchip Electronics Co., Ltd
* Description:   
* @author:     fxw@rock-chips.com
* Create at:   2014年5月9日 上午10:28:39  
* 
* Modification History:  
* Date         Author      Version     Description  
* ------------------------------------------------------------------  
* 2014年5月9日      fxw         1.0         create
*******************************************************************/   

package com.rockchip.devicetest;

import com.rockchip.devicetest.service.TestService;

import android.app.ActivityManager;
import android.app.Application;
import android.content.ComponentName;

//proxy
public class TestApplication extends Application {

	public IndexActivity mIndexActivity;
	public TestService mTestService;
	ActivityManager mActivityManager; 
	private boolean mActivityReady;
	
	public void onCreate() {
		mActivityManager= (ActivityManager) getSystemService(ACTIVITY_SERVICE);
		super.onCreate();
	}
	
	public boolean isDispatcherReady(){
		return isOnTopApplication();
	}
	
	public void setActivityReady(boolean ready){
		this.mActivityReady = ready;
	}
	
	public boolean isOnTopApplication(){
		ComponentName cn = mActivityManager.getRunningTasks(1).get(0).topActivity;
		return mActivityReady&&mIndexActivity!=null&&cn.getPackageName().equals(getPackageName());
	}
	
	public void setShowingApp(boolean isShowing){
		if(mTestService!=null){
			mTestService.setShowingApp(isShowing);
		}
	}
	
}
