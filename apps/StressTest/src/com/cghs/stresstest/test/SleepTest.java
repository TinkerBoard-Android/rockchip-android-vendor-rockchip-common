package com.cghs.stresstest.test;

import com.cghs.stresstest.R;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.KeyguardManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.provider.Settings;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;


import android.net.Uri;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;

public class SleepTest extends Activity implements OnClickListener {
    private final String LOG_TAG = "SleepTestActivity";
    public final static String TAG = "SleepTestActivity";

    private final String ACTION_SLEEP = "com.rockchip.sleep.ACTION_TEST_CASE_SLEEP";
    private final long MIN_SLEEP_TIME = 30000L;

    private long mAwakeTime = 5000L;
    private long mSleepTime = 40000L;
    private int mTestCount = 0;
    private int mLimitCount = 0;
    private boolean mIsRunning = false;

    private Button mStartBtn;
    private Button mStopBtn;
    private Button mExitBtn;
    private TextView mWakeTV;
    private Button mWakeBtn;
    private TextView mIntervalTV;
    private Button mIntervalBtn;
    private TextView mMaxTV;
    private Button mMaxBtn;

    private int mAutoTestFlag = 0;
    private MyReceiver mReceiver;
    private AlarmManager mAlarmManager;
    private PendingIntent mSleepOperation;
    private boolean mIsRepeatAlarm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sleep_test);
        initRes();

        registerReceiver(SleepTestReceiver, new IntentFilter(ACTION_SLEEP));

        mAlarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);

        mAutoTestFlag = getIntent().getIntExtra("auto", 0);
        if (mAutoTestFlag != 0) {
            startTest(this);
        }
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_MEDIA_MOUNTED);
        filter.addDataScheme("file");
        mReceiver = new MyReceiver();
        registerReceiver(mReceiver, filter);

        dealComingIntent();
    }

    private void dealComingIntent() {
        Intent intent = getIntent();
        if (null != intent) {
            Bundle data = intent.getBundleExtra("data");
            if (null != data) {
                if (data.getBoolean("start", false)) {
                    int time = data.getInt("time", 0);
                    if (time < 40) {
                        mSleepTime = 40000;
                    } else {
                        mSleepTime = time * 1000;
                    }
                    startTest(this);
                }
            }
        }
    }

    private void initRes() {
        mStartBtn = (Button) findViewById(R.id.start_btn);
        mStartBtn.setOnClickListener(this);
        mStopBtn = (Button) findViewById(R.id.stop_btn);
        mStopBtn.setOnClickListener(this);
        mExitBtn = (Button) findViewById(R.id.exit_btn);
        mExitBtn.setOnClickListener(this);

        mWakeBtn = (Button) findViewById(R.id.waketime_btn);
        mWakeBtn.setOnClickListener(this);
        mIntervalBtn = (Button) findViewById(R.id.intervaltime_btn);
        mIntervalBtn.setOnClickListener(this);
        mMaxBtn = (Button) findViewById(R.id.max_count_btn);
        mMaxBtn.setOnClickListener(this);

        mWakeTV = (TextView) findViewById(R.id.waketime_tv);
        mIntervalTV = (TextView) findViewById(R.id.intervaltime_tv);
        mMaxTV = (TextView) findViewById(R.id.max_count_tv);
        updateView();

    }


    private void updateView() {
        mWakeTV.setText(getString(R.string.wake_string) + mAwakeTime / 1000);
        mIntervalTV.setText(getString(R.string.interval_string) + mSleepTime / 1000);
        mMaxTV.setText(getString(R.string.maxcount_string) + mLimitCount + "  "
                + getString(R.string.nowcount_string) + mTestCount);
    }

    private void startTest(Context context) {
        stopAlarm(context);
        mIsRunning = true;
        if (mStartBtn != null && mStopBtn != null) {
            mStartBtn.setEnabled(!mIsRunning);
            mStopBtn.setEnabled(mIsRunning);
        }
        try {
            Settings.System.putInt(context.getContentResolver(),
                    "screen_off_timeout", 15000);
            mIsRepeatAlarm = true;
            setAlarm(context, mSleepTime);
            return;
        } catch (NumberFormatException localNumberFormatException) {
            while (true)
                Log.e(LOG_TAG, "could not persist screen timeout setting");
        }
    }

    private void stopTest(Context context) {
        Log.d(LOG_TAG, "stopTest ...");
        mIsRepeatAlarm = false;
        stopAlarm(context);
        mIsRunning = false;
        if (mStartBtn != null && mStopBtn != null) {
            mStartBtn.setEnabled(!mIsRunning);
            mStopBtn.setEnabled(mIsRunning);
        }
    }


    private void setAlarm(Context paramContext, long paramLong) {
        Log.d(LOG_TAG, "setAlarm " + paramLong);
        if ("true".equals(SystemProperties.get("persist.stresstest.stop"))) {
            mIsRunning = false;
            stopTest(SleepTest.this);
            Toast.makeText(SleepTest.this, "stop because persist.stresstest.stop", Toast.LENGTH_LONG).show();
            Log.e(LOG_TAG, "persist.stresstest.stop is true");
            return;
        }
        if (null == mSleepOperation) {
            mSleepOperation = PendingIntent.getBroadcast(
                    paramContext, 0,
                    new Intent(ACTION_SLEEP),
                    PendingIntent.FLAG_UPDATE_CURRENT//use 0?
            );
        }
        /*mAlarmManager.set(AlarmManager.RTC_WAKEUP, paramLong + System.currentTimeMillis(),
                localPendingIntent);
        if (repeat)
            localAlarmManager.setRepeating(0,
                    paramLong + System.currentTimeMillis(), paramLong,
                    localPendingIntent);*/
        mAlarmManager.setExact(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                SystemClock.elapsedRealtime() + paramLong, mSleepOperation);
    }

    private void stopAlarm(Context paramContext) {
        /*PendingIntent localPendingIntent = PendingIntent.getBroadcast(
                paramContext, 0, new Intent(
                        ACTION_SLEEP), 0);
        ((AlarmManager) paramContext.getSystemService("alarm"))
                .cancel(localPendingIntent);*/
        if (null != mAlarmManager && null != mSleepOperation) {
            mAlarmManager.cancel(mSleepOperation);
        }
    }

    private void showToast(String ss) {
        Toast.makeText(this, ss, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onPause() {
        super.onPause();
//		((KeyguardManager)getSystemService("keyguard")).newKeyguardLock("TestCaseSleep").reenableKeyguard();
    }

    protected void onDestroy() {
        super.onDestroy();
        stopTest(this);
        Log.e(LOG_TAG, "unregisterReceiver(SleepTestReceiver)");
        unregisterReceiver(SleepTestReceiver);
        if (null != mReceiver) {
            unregisterReceiver(mReceiver);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.start_btn:
                if (mSleepTime < MIN_SLEEP_TIME) {
                    showToast("interval time need at least " + (MIN_SLEEP_TIME / 1000) + "s");
                } else if (mSleepTime < mAwakeTime) {
                    showToast("need interval time > wake up time ");
                } else {
                    startTest(this);
                }
                break;
            case R.id.stop_btn:
                stopTest(this);
                break;
            case R.id.exit_btn:
                finish();
                break;
            case R.id.waketime_btn:
                onSetClick(R.id.waketime_btn);
                break;
            case R.id.intervaltime_btn:
                onSetClick(R.id.intervaltime_btn);
                break;
            case R.id.max_count_btn:
                onSetClick(R.id.max_count_btn);
                break;
            default:
                break;
        }
    }

    ;

    private void onSetClick(final int id) {
        final EditText editText = new EditText(this);
        editText.setInputType(InputType.TYPE_CLASS_NUMBER);
        new AlertDialog.Builder(this)
                .setTitle(R.string.dialog_title)
                .setView(editText)
                .setPositiveButton(R.string.dialog_ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (!editText.getText().toString().trim().equals("")) {
                            if (id == R.id.waketime_btn) {
                                mAwakeTime = Integer.valueOf(editText.getText().toString()) * 1000L;
                                updateView();
                            } else if (id == R.id.intervaltime_btn) {
                                mSleepTime = Integer.valueOf(editText.getText().toString()) * 1000L;
                                updateView();
                            } else if (id == R.id.max_count_btn) {
                                mLimitCount = Integer.valueOf(editText.getText().toString());
                                updateView();
                            }
                        }
                    }
                })
                .setNegativeButton(R.string.dialog_cancel, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }

                }).show();
    }


    private BroadcastReceiver SleepTestReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.e(LOG_TAG, "SleepTestReceiver onReceive...");
            mTestCount = mTestCount + 1;
            updateView();
            if (mLimitCount != 0 && mTestCount >= mLimitCount) {
                mIsRunning = false;
                stopTest(context);
            } else {
                ((PowerManager) context.getSystemService("power")).newWakeLock(
                        PowerManager.ACQUIRE_CAUSES_WAKEUP
                                | PowerManager.FULL_WAKE_LOCK, "ScreenOnTimer")
                        .acquire(mAwakeTime);
                if (mIsRepeatAlarm) {
                    setAlarm(context, mSleepTime);
                }
//				 ((KeyguardManager)context.getSystemService("keyguard")).newKeyguardLock("TestCaseSleep").disableKeyguard();
            }
        }

    };

    class MyReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            String devicePath = null;
            if (intent.getData() != null) {
                Uri uri = intent.getData();
                if (uri != null)
                    devicePath = uri.getPath();
            }
            if (Intent.ACTION_MEDIA_MOUNTED.equals(action)) {
                File dirFile = new File(devicePath);
                File checkFile = new File(dirFile, "RK_StressTest.txt");
                try {
                    String encoding = "GBK";
                    if (checkFile.isFile() && checkFile.exists()) { //判断文件是否存在
                        InputStreamReader read = new InputStreamReader(
                                new FileInputStream(checkFile), encoding);//考虑到编码格式
                        BufferedReader bufferedReader = new BufferedReader(read);
                        String lineTxt = null;
                        while ((lineTxt = bufferedReader.readLine()) != null) {
                            Log.d(TAG, lineTxt);
                            if (lineTxt.startsWith("#")) {
                                continue;
                            }
                            if (lineTxt.contains("sleep 0")) {
                                stopTest(context);
                            }

                        }
                        read.close();
                    } else {
                        Log.e(TAG, "not find RK_StressTest.txt");
                    }
                } catch (Exception e) {
                    Log.e(TAG, "read error!!");
                    e.printStackTrace();
                }


            }
        }
    }


}

