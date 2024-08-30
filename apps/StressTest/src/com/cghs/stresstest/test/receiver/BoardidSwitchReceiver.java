package com.cghs.stresstest.test.receiver;

import java.util.HashMap;

import com.cghs.stresstest.test.BoardidSwitchTest;
import com.cghs.stresstest.util.StresstestUtil;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BoardidSwitchReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
			HashMap<String, String>map = new HashMap<String, String>();
			boolean result = StresstestUtil.readState(BoardidSwitchTest.BOARD_ID_SWITCH_PATH, map);
			if (!result) return ;
			
			String value = map.get("enable");
			if (value != null && value.equals("1")) {
				Intent intent2 = new Intent(context, BoardidSwitchTest.class);
				intent2.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				context.startActivity(intent2);
			}
		}
	}

}
