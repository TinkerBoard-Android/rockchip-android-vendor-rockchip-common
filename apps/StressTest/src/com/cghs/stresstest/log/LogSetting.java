package com.cghs.stresstest.log;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.util.Log;
import android.os.SystemProperties;
import com.cghs.stresstest.R;
import android.os.Handler;
import android.os.Message;

public class LogSetting extends PreferenceActivity implements
		Preference.OnPreferenceChangeListener {

	private final static String LOG_LEVEL = "log_level";
	private final static String LOG_SWITCH = "log_switch";
	private final static String LOG_DAY = "log_day";
	private final static String TAG = "LogSetting";
	private final static boolean DEBUG = false;
	private ListPreference mLogSwitch;
	private ListPreference mLogDay;
	private final static int START_SERVER = 1;
	private final static int STOP_SERVER = 0;
	private final static int MSG_DELAT_TIME = 1000;

	private Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {

			if (DEBUG) {
				Log.i(TAG, "msg.what = " + msg.what);
			}
			switch (msg.what) {
			case START_SERVER:
				Intent startSv = (Intent) msg.obj;
				startService(startSv);
				break;
			case STOP_SERVER:
				Intent stopSv = (Intent) msg.obj;
				stopService(stopSv);
				break;
			default:
				break;
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.i(TAG, "into onCreate");
		addPreferencesFromResource(R.xml.log_settings);

		mLogSwitch = (ListPreference) findPreference(LOG_SWITCH);
		mLogSwitch.setOnPreferenceChangeListener(this);
		if (DEBUG) {
			String logSwitch = mLogSwitch.getValue();
			Log.i(TAG, "into onCreate logSwitch = " + logSwitch);
		}
		mLogDay = (ListPreference) findPreference(LOG_DAY);
		mLogDay.setOnPreferenceChangeListener(this);
		if (DEBUG) {
			String logDay = mLogDay.getValue();
			Log.i(TAG, "into onCreate mLogDay = " + logDay);
		}

	}

	@Override
	public void onResume() {
		super.onResume();
		if (DEBUG) {
			Log.i(TAG, "into onResume");
		}

	}

	public boolean onPreferenceChange(Preference preference, Object newValue) {
		// TODO Auto-generated method stub
		final String key = preference.getKey();
		String logSwitch = mLogSwitch.getValue();
		String logDay = mLogDay.getValue();
		if (DEBUG) {
			Log.i(TAG, "oldLogSwitch = " + logSwitch);
			Log.i(TAG, "oldLogDay = " + logDay);
			Log.i(TAG, "newValue = " + (String) newValue);
		}
		Intent service = new Intent(this, LogService.class);
		service.putExtra("logSwitch", logSwitch);
		service.putExtra("logDay", logDay);
		if (LOG_SWITCH.equals(key)) {
			if (logSwitch.equals(newValue)) {
				if (DEBUG) {
					Log.i(TAG, "the logSwitch value isn't change");
				}

			} else {
				logSwitch = (String) newValue;
				mLogSwitch.setValue(logSwitch);
				SystemProperties.set("persist.sys.rk.logswitch", logSwitch);
				if (DEBUG) {
					Log.i(TAG,
							"hw.logswitch = "
									+ SystemProperties.get(
											"persist.sys.rk.logswitch", "0"));
					Log.i(TAG, "mLogSwitch = " + mLogSwitch.getValue());
				}
				service.putExtra("logSwitch", logSwitch);
				if ("1".equals(logSwitch)) {
					Message msg = mHandler.obtainMessage(START_SERVER);
					msg.obj = service;
					mHandler.sendMessage(msg);
				} else {
					Message msg = mHandler.obtainMessage(STOP_SERVER);
					msg.obj = service;
					mHandler.sendMessage(msg);
				}
			}

		} else if (LOG_DAY.equals(key)) {
			if (logDay.equals(newValue)) {
				if (DEBUG) {
					Log.i(TAG, "the logDay value isn't change");
				}

			} else {
				logDay = (String) newValue;
				mLogDay.setValue(logDay);
				SystemProperties.set("persist.sys.rk.logday", logDay);
				service.putExtra("logDay", logDay);
				if ("1".equals(logSwitch)) {
					Message msg = mHandler.obtainMessage(STOP_SERVER);
					msg.obj = service;
					mHandler.sendMessage(msg);

					Message msg2 = mHandler.obtainMessage(START_SERVER);
					msg2.obj = service;
					mHandler.sendMessageDelayed(msg2, MSG_DELAT_TIME);
				}
			}

		}
		return true;
	}

	public void onPause() {
		super.onPause();
		if (DEBUG) {
			Log.i(TAG, "into onPause");
		}
	}

	public void onStop() {
		super.onStop();
		if (DEBUG) {
			Log.i(TAG, "into onStop");
		}
	}

	public void onDestroy() {
		super.onDestroy();
		if (DEBUG) {
			Log.i(TAG, "into onDestroy");
		}
	}

}
