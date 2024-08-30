package com.cghs.stresstest.test;

import java.util.Timer;
import java.util.TimerTask;

import com.cghs.stresstest.util.NativeInputManager;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.PowerManager.WakeLock;

public class CameraOpenService extends Service {
	
	private final int START_CMD = 1;
	private final int STOP_CMD = 0;
	private int mMaxCount = 0;
	private int mNowCount = 0;
	private Timer mTimer = null;
	private boolean isTesting = false;
	
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		mMaxCount = intent.getIntExtra("max", 0);
		int cmd = intent.getIntExtra("cmd", 0);
		if (cmd == START_CMD) {
			TestSystemCamera();
		} else {
			stopTest();
		}
		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (mTimer != null) 
			mTimer.cancel();
	}
	
	//Test System Camera Launcher
		private void TestSystemCamera() {
			startTest();
		}
		
		private void startTest() {
			isTesting = true;
			mTimer = new Timer();
			mTimer.schedule(new TimerTask() {
				public void run() {
					if (mMaxCount != 0 && mNowCount >= mMaxCount || !isTesting) {
						return;
					} else {
						mNowCount++;
						turnCameraOn();
					}
					
				}
			}, 500L, 6000L);
		}

		private void stopTest() {
			if (mTimer != null)
				mTimer.cancel();
		}

		private void turnCameraOff() {
			NativeInputManager.sendKeyDownUpSync(4);
		}

		private void turnCameraOn() {
			Intent localIntent = new Intent();
			localIntent.setAction("android.media.action.IMAGE_CAPTURE");
			localIntent.addFlags(335544320);
			startActivity(localIntent);
			mTimer.schedule(new TimerTask() {
				public void run() {
					turnCameraOff();
				}
			}, 3000L);
		}
		
		//Test System Camera Launcher end --- ---
	
}
