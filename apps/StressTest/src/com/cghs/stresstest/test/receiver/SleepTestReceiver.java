package com.cghs.stresstest.test.receiver;

import android.app.KeyguardManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;
import android.util.Log;

public class SleepTestReceiver extends BroadcastReceiver {
	
	private long mAwakeTime = 20000L;
	@Override
	public void onReceive(Context context, Intent intent) {
		Log.e("cghs", "onReceive() ... ...");
		 ((PowerManager)context.getSystemService("power")).newWakeLock(268435482, "ScreenOnTimer").acquire(mAwakeTime);
		 ((KeyguardManager)context.getSystemService("keyguard")).newKeyguardLock("TestCaseSleep").disableKeyguard();
	}

}
