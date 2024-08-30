package com.cghs.stresstest.log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Set;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.Environment;
import android.os.IBinder;
import android.os.SystemProperties;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.util.Log;
import java.util.Calendar;
import android.net.Uri;
import android.database.Cursor;
import android.provider.MediaStore;
import android.content.ContentValues;
import com.cghs.stresstest.R;

public class LogService extends Service {

	private static final String TAG = "LogService";
	private static final boolean DEBUG = true;
	private LogThread logThread;
	private static final String BASE_DIR = /*Environment
			.getExternalStorageDirectory().getPath() +*/ "/mnt/internal_sd/testlog";
	private static final String FILE_PATH[] = { "/ADBLog", "/KMSGLog",
			"/RADIOLog", "/BUGREPORTLog" };
	private static final String FILE_SIGN[] = { "/adblogcat_", "/kmsg_",
			"/radio_", "/bugreport_" };
	private static final String LOG_SIGN[] = { "logcat", "dmesg", "radio",
			"bugreport" };
	private static final String LOG_CMD[] = { "logcat -d -v time", "dmesg",
			"logcat -b radio -d -v time", "bugreport -d -v time" };
	private static final String CLEAN_CMD[] = { "logcat -c", "dmesg -c",
			"logcat -b radio -c", "bugreport -c" };
	private static final String FILE_TYPE = ".log";

	private ArrayList<Handler> mHandlers = new ArrayList<Handler>();
	private ArrayList<Looper> mLoopers = new ArrayList<Looper>();
	private HashMap<Integer, ArrayList<Runnable>> mRuns = new HashMap<Integer, ArrayList<Runnable>>();
	private int mSleepTime;
	private static final int DELAY_TIME = 5 * 1000;
	private static final int SLEEP_TIME = 60 * 1000;
	private static final int MAX_LENGTH = 16 * 1024 * 1024;

	private String mLogSwitch;
	private String mLogDay;
 // private String mLogLevel;
	private String mReboot;
	private String[] mCols = new String[] { MediaStore.Files.FileColumns._ID, };

	private IBinder binder = new LogBinder();

	public class LogBinder extends Binder {
		LogService getService() {
			return LogService.this;
		}
	}

	@Override
	public IBinder onBind(Intent intent) {
		if (DEBUG) {
			Log.i(TAG, "----------onBind()----------");
		}
		return binder;
	}

	private class LogConstants {
		final static String LOG_OPEN = "1";
		final static String LOG_CLOSE = "0";

		final static String LOG_LEVEL = "log_level";
		final static String LOG_SWITCH = "log_switch";
		final static String LOG_DAY = "log_day";
	}

	@Override
	public void onCreate() {
		super.onCreate();
		if (DEBUG) {
			Log.i(TAG, "----------onCreate()----------");
		}
			for (String logSign : LOG_SIGN) {
				HandlerThread thread = new HandlerThread(logSign);
				thread.start();
				Looper looper = thread.getLooper();
				mLoopers.add(looper);
				Handler hd = new Handler(looper);
				mHandlers.add(hd);
			}
		
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if (DEBUG) {
			Log.i(TAG, "----------onStartCommand()----------");
		}
		int returnValue = super.onStartCommand(intent, flags, startId);
		if (intent != null) {
			mLogSwitch = intent.getStringExtra("logSwitch");
			mLogDay = intent.getStringExtra("logDay");
			// mLogLevel = intent.getStringExtra("mLogLevel");
			mReboot = intent.getStringExtra("reboot");
		}
		// if the app is crash, the mLogSwitch and mLogDay may be null, casue the Integer.parseInt(mLogDay)
		// error.
		mLogSwitch = SystemProperties.get("persist.sys.rk.logswitch", "0");
		mLogDay = SystemProperties.get("persist.sys.rk.logday", "1");
		
		if (DEBUG) {
			Log.i(TAG, "mLogSwitch = " + mLogSwitch);
			Log.i(TAG, "mLogDay = " + mLogDay);
			Log.i(TAG, "reboot = " + mReboot);
		}

		if (LogConstants.LOG_CLOSE.equals(mLogSwitch)) {
			if (logThread != null)
				logThread.cancel();
			stopService(intent);
		} else {
			if (logThread != null) {
				logThread.cancel();
			}
			logThread = new LogThread();
			logThread.setReboot(mReboot);
			logThread.start();
		}

		return returnValue;
	}

	public void onDestroy() {
		super.onDestroy();
		if (DEBUG) {
			Log.i(TAG, "----------onDestroy----------");

		}
		if (logThread != null) {
			logThread.cancel();
		}

		for (int i = 0; i < mLoopers.size(); i++) {
			Looper looper = mLoopers.get(i);
			looper.quit();
			looper = null;
		}
		mLoopers.clear();

		for (int i = 0; i < mHandlers.size(); i++) {
			Handler handler = mHandlers.get(i);
			handler = null;
		}
		mHandlers.clear();

		mRuns.clear();
	}

	class LogThread extends Thread {
		private boolean mCanceled = false;
		private String reboot;

		public String getReboot() {
			return reboot;
		}

		public void setReboot(String reboot) {
			this.reboot = reboot;
		}

		public LogThread() {
			super();
		}

		public void run() {
			while (!mCanceled) {
				try {
					if (DEBUG) {
						Log.i(TAG, "into LogThread run");
					}
					for (String logSign : LOG_SIGN) {
						int position = getPosition(logSign);
						if (DEBUG) {
							Log.i(TAG, "position = " + position);
						}
						if (position == -1) {
							cancel();
						}
						final String tmpReboot = reboot;
						final String tmpLogSign = logSign;
						Runnable mRun = new Runnable() {
							public void run() {
								if (DEBUG) {
									Log.i(TAG, tmpLogSign + " log start");
								}
								long start = SystemClock.uptimeMillis();
								getLogCat(tmpReboot, tmpLogSign);
								long end = SystemClock.uptimeMillis();
								if (DEBUG) {
									Log.i(TAG, tmpLogSign
											+ " log end spent =  "
											+ (end - start));
								}

							}

						};
						if (mRuns.get(position) == null) {
							ArrayList<Runnable> tmpRunList = new ArrayList<Runnable>();
							tmpRunList.add(mRun);
							mRuns.put(position, tmpRunList);
						} else {
							mRuns.get(position).add(mRun);
						}
						mHandlers.get(position).postDelayed(mRun,
								DELAY_TIME * 0);
					}

					Thread.sleep(SLEEP_TIME);

					mSleepTime++;
					if (mSleepTime == 5) {
						Set<Integer> set = mRuns.keySet();
						for (Integer tmpSet : set) {
							ArrayList<Runnable> RunList = mRuns.get(tmpSet);
							for (int j = 0; j < RunList.size(); j++) {
								mHandlers.get(tmpSet).removeCallbacks(
										RunList.get(j));
							}
						}
						mSleepTime = 0;

						mRuns.clear();
						Thread.sleep(SLEEP_TIME);
					}
					if (reboot != null) {
						reboot = null;
					}
				} catch (Exception e) {
					e.printStackTrace();
					cancel();
				} finally {

				}
			}
		}

		public void cancel() {
			if (DEBUG) {
				Log.i(TAG, "----------cancel()----------");
			}
			mCanceled = true;
			logThread = null;
		}
	}

	@Override
	public boolean onUnbind(Intent intent) {
		return super.onUnbind(intent);
	}

	// public void getLogCat(String reboot) {
	// for (String logSign : LOG_SIGN) {
	// getLogCat(reboot, logSign);
	// }
	// }

	private void getLogCat(String reboot, String logSign) {
		final String tmpReboot = reboot;
		final String tmpLogSign = logSign;
		if (DEBUG) {
			Log.i(TAG, "into getLogCat tmpReboot =  " + tmpReboot
					+ "  tmpLogSign = " + tmpLogSign);
		}
		// new Thread() {
		// public void run() {
		// if (DEBUG) {
		// Log.i(TAG, "ThreadName = "
		// + Thread.currentThread().getName());
		// }
		doLogSavedAction(tmpReboot, tmpLogSign);
		//doMediaScanAction(tmpLogSign);
		// }
		// }.start();
	}

	private void doLogSavedAction(String reboot, String logSign) {

		InputStreamReader reader = null;
		BufferedReader bufferedReader = null;
		BufferedWriter bufferedWriter = null;
		File path = null;
		String fullName = null;
		Process process = null;
		try {
			String logCmd = getLogCmd(logSign);
			process = Runtime.getRuntime().exec(logCmd);
			String filePath = getFilePath(logSign);
			path = new File(filePath);
			if (!path.exists()) {
				boolean boo = path.mkdirs();
				if (DEBUG) {
					Log.i(TAG, "mkdirs " + path.getPath() + "is  " + boo);
				}
				path.setWritable(true);
			}

			fullName = getFullName(logSign);

			File file = new File(fullName);
			if (!file.exists()) {
				file.createNewFile();
				deleteOldFile(path);
			}

			if (file.exists() && file.getName().contains("bugreport")
					&& file.length() > MAX_LENGTH) {
				if (DEBUG) {
					Log.i(TAG,
							"bugreport log file is large, delete and cerate it");
				}
				file.delete();
				file.createNewFile();
			}

			reader = new InputStreamReader(process.getInputStream());
			bufferedReader = new BufferedReader(reader, 1024);
			bufferedWriter = new BufferedWriter(new FileWriter(fullName, true));

			if ("reboot".equals(reboot)) {
				bufferedWriter.write("\n\n");
				bufferedWriter.write("----------system reboot----------");
				bufferedWriter.write("\n");
			}

			String line = "";
			while ((line = bufferedReader.readLine()) != null) {
				if (!line.startsWith("---")) {
					bufferedWriter.write(line);
					bufferedWriter.write("\n");
					bufferedWriter.flush();
				}
			}

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (bufferedWriter != null) {
					bufferedWriter.flush();
					bufferedWriter.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}

			try {
				if (bufferedReader != null)
					bufferedReader.close();
			} catch (IOException e) {
				e.printStackTrace();
			}

			try {
				if (reader != null)
					reader.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		cleanLog(logSign);
	}

	private void deleteOldFile(File path) {
		File[] subFile = path.listFiles();
		if (subFile.length > Integer.parseInt(mLogDay)) {
			long[] subFileName = new long[subFile.length];
			for (int i = 0; i < subFile.length; i++) {
				int index = subFile[i].getName().lastIndexOf(".");
				String s = subFile[i].getName().substring(index - 8, index);
				subFileName[i] = Long.parseLong(s);
			}
			Arrays.sort(subFileName);
			if (DEBUG) {
				Log.i(TAG, "delete old file path = " + path.getPath() + "  "
						+ subFileName[0]);
			}
			for (int i = 0; i < subFile.length; i++) {
				if (subFile[i].getName().contains(
						String.valueOf(subFileName[0]))) {
					boolean delFile = subFile[i].delete();
					if (DEBUG) {
						Log.i(TAG, "delete old file " + delFile);
					}
					break;
				}

			}
		}
	}

	private void cleanLog(String logSign) {
		try {
			String logCleanCmd = getLogCleanCmd(logSign);
			Runtime.getRuntime().exec(logCleanCmd);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.e(TAG, e.getMessage());
		}

	}

	private String getLogCleanCmd(String logSign) {
		String logCleanCmd = null;
		int position = getPosition(logSign);
		if (-1 != position) {
			logCleanCmd = CLEAN_CMD[position];
		}
		return logCleanCmd;

	}

	private String getLogCmd(String logSign) {
		String logCmd = null;
		int position = getPosition(logSign);
		if (-1 != position) {
			logCmd = LOG_CMD[position];
		}
		return logCmd;
	}

	private int getPosition(String logSign) {
		int position = -1;
		for (int i = 0; i < LOG_SIGN.length; i++) {
			if (LOG_SIGN[i].equals(logSign)) {
				position = i;
				break;
			}
		}
		return position;
	}

	private String getFilePath(String logSign) {
		String filePath = null;
		int position = getPosition(logSign);
		if (-1 != position) {
			filePath = BASE_DIR + FILE_PATH[position];
		}
		return filePath;
	}

	private String getFullName(String logSign) {
		String fullName = null;
		long time = System.currentTimeMillis();
		final Calendar mCalendar = Calendar.getInstance();
		mCalendar.setTimeInMillis(time);
		int mYear = mCalendar.get(Calendar.YEAR);
		int mMonth = mCalendar.get(Calendar.MONTH);
		int mDay = mCalendar.get(Calendar.DAY_OF_MONTH);
		String year = String.valueOf(mYear);
		String month;
		if ((mMonth + 1) < 10) {
			month = "0" + String.valueOf(mMonth + 1);
		} else {
			month = String.valueOf(mMonth + 1);
		}
		String day;
		if (mDay < 10) {
			day = "0" + String.valueOf(mDay);
		} else {
			day = String.valueOf(mDay);
		}
		String fileName = year + month + day;
		int position = getPosition(logSign);
		if (-1 != position) {
			fullName = BASE_DIR + FILE_PATH[position] + FILE_SIGN[position]
					+ fileName + FILE_TYPE;
		}
		return fullName;
	}

	private void doMediaScanAction(String logSign) {
		String fullName = getFullName(logSign);
		String filePath = getFilePath(logSign);
		File tpFile = new File(fullName);
		if (!tpFile.exists()) {
			sendMediaScan(filePath);
		} else {
			Cursor cursor = null;
			long did = 0;
			try {
				cursor = getContentResolver().query(
						Uri.parse("content://media/external/file"),
						mCols,
						MediaStore.Files.FileColumns.DATA + " = '" + fullName
								+ "'", null, null);
				if (cursor != null && cursor.getCount() > 0) {
					cursor.moveToFirst();
					did = cursor.getLong(0);
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				try {
					if (cursor != null) {
						cursor.close();
					}
				} catch (Exception e) {
					e.printStackTrace();
				}

			}

			if (did == 0) {
				sendMediaScan(filePath);
			} else {
				ContentValues values = new ContentValues();
				values.put(MediaStore.Files.FileColumns.DATE_MODIFIED,
						System.currentTimeMillis() / 1000);
				values.put(MediaStore.Files.FileColumns.SIZE, tpFile.length());
				int rs = getContentResolver().update(
						Uri.parse("content://media/external/file/" + did),
						values, null, null);
				System.out.println("yy rs1=" + rs);
			}

		}
	}

	private void sendMediaScan(String path) {
		Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
				Uri.fromFile(new File(path).getParentFile()));
		sendBroadcast(intent);
	}

}
