package com.cghs.stresstest.test.receiver;

import com.cghs.stresstest.test.DvfsTest;
import com.cghs.stresstest.test.RebootTest;
import android.app.KeyguardManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

public class RebootReceiver extends BroadcastReceiver {
	private SharedPreferences mSharedPreferences;
	@Override
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();
		if (action.equals(Intent.ACTION_BOOT_COMPLETED)) {
			mSharedPreferences = context.getSharedPreferences("state", 0);
			int rebootFlag = mSharedPreferences.getInt("reboot_flag", 0);
			int rebootCount= mSharedPreferences.getInt("reboot_count", 0);
			int maxtime= mSharedPreferences.getInt("reboot_max", 0);
                        Log.d("StressTest","=========onReceive===rebootFlag:"+rebootFlag);
			if (rebootFlag == 0) {
				//not
				//dvfs
				int dvfsFlag = mSharedPreferences.getInt("dvfs_flag", 0);
				if (dvfsFlag == 1) {
					Intent pintent = new Intent(context, DvfsTest.class);
					pintent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					context.startActivity(pintent);
				}
			} else {
				Intent pintent = new Intent(context, RebootTest.class);
				pintent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				context.startActivity(pintent);
			}

		}
	}

}
