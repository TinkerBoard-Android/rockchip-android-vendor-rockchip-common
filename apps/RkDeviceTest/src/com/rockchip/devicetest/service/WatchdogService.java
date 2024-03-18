/*******************************************************************
* Company:     Fuzhou Rockchip Electronics Co., Ltd
* Description:   
* @author:     fxw@rock-chips.com
* Create at:   2014年5月21日 下午3:45:52  
* 
* Modification History:  
* Date         Author      Version     Description  
* ------------------------------------------------------------------  
* 2014年5月21日      fxw         1.0         create
*******************************************************************/   

package com.rockchip.devicetest.service;

import com.rockchip.devicetest.testcase.LEDSettings;
import com.rockchip.devicetest.testcase.LEDSettings.LEDMode;

import android.app.ActivityManager;
import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.content.Context;

/**
 *	看门狗监听服务,独立进程
 */
public class WatchdogService extends Service {
	
	public static final String COMMAND = "command";
	public static final int CMD_START_AGING = 1;
	public static final int CMD_STOP_AGING = 2;
	private Handler mMainHandler = null;
	private boolean isRunningAgingTest;
	private LEDMode mLEDMode;
	private ActivityManager mActivityManager;
        private Context mContext;

	@Override
	public void onCreate() {
		super.onCreate();
                mContext = this;
		mMainHandler = new Handler();
		mActivityManager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		int command = intent.getIntExtra(COMMAND, 0);
		if(command == CMD_START_AGING){
			startAgingTest();
		}else if(command == CMD_STOP_AGING){
			stopAgingTest();
		}
		return super.onStartCommand(intent, flags, startId);
	}
	
	/**
	 * 启动老化测试
	 */
	public void startAgingTest(){
		if(isRunningAgingTest){
			return;
		}
		isRunningAgingTest = true;
		mMainHandler.postDelayed(mLedCtrlAction, 3000);
	}
	
	Runnable mLedCtrlAction = new Runnable() {
		public void run() {
			if(isRunningAgingTest){
				ComponentName cn = mActivityManager.getRunningTasks(1).get(0).topActivity;
				boolean isHDMINotificiation = cn.getClassName().endsWith("HDMINotificiationActivity");
				if(!cn.getPackageName().equals(getPackageName())&&!isHDMINotificiation){
					mMainHandler.postDelayed(mDelayStopAction, 6000);
				}else{
					mMainHandler.removeCallbacks(mDelayStopAction);
				}
				if(mLEDMode==LEDMode.OFF){
					LEDSettings.onLed(mContext);
					mLEDMode = LEDMode.ON;
				}else{
					LEDSettings.offLed(mContext);
					mLEDMode = LEDMode.OFF;
				}
				//mMainHandler.removeCallbacks(mLedCtrlAction);
				mMainHandler.postDelayed(mLedCtrlAction, 1500);
			}
		}
	};
	
	private Runnable mDelayStopAction = new Runnable(){
		public void run() {
			ComponentName cn = mActivityManager.getRunningTasks(1).get(0).topActivity;
			boolean isHDMINotificiation = cn.getClassName().endsWith("HDMINotificiationActivity");
			if(cn.getPackageName().equals(getPackageName())||isHDMINotificiation){
				//
			}else{
				isRunningAgingTest = false;
				mMainHandler.removeCallbacks(mLedCtrlAction);
				LEDSettings.onLed(mContext);
			}
		};
	};
	
	/**
	 * 停止老化测试
	 */
	public void stopAgingTest(){
		isRunningAgingTest = false;
		mMainHandler.removeCallbacks(mLedCtrlAction);
		LEDSettings.offLed(this);
	}
	
	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}
	
}
