package com.cghs.stresstest.test;

import com.cghs.stresstest.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.os.SystemService;
import android.telephony.TelephonyManager;
import android.text.InputType;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.os.PowerManager.WakeLock;
//import android.os.storage.IMountService;
import android.os.ServiceManager;
import android.os.Environment;

import com.cghs.stresstest.util.CmdUtils;
import com.cghs.stresstest.util.NetworkUtils;
import com.cghs.stresstest.util.StresstestUtil;

import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;

import android.app.Dialog;

import java.io.InputStreamReader;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;

import android.app.KeyguardManager;

import android.os.SystemProperties;
import android.os.storage.VolumeInfo;
import android.os.storage.DiskInfo;
import android.os.storage.StorageVolume;
import android.os.storage.StorageManager;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Collections;
/*
 * Author	: huangjc
 * Date  	: 2013-05-06
 * Function	: Reboot Test
 */

public class RebootTest extends Activity implements OnClickListener {
    private final static String LOG_TAG = "RebootTest";

    private final static int MSG_REBOOT = 0;
    private final static int MSG_REBOOT_COUNTDOWN = 1;
    private final static int MSG_REBOOT_STARTCOUNT = 2;
    private final static int MSG_REBOOT_WIFI_OPEN_TIMEOUT = 3;
    private final static int MSG_REBOOT_WIFI_SCAN_TIME_OUT = 4;
    private final static int MSG_REBOOT_BT_OPEN = 5;
    private final static int MSG_REBOOT_MOBILE_DATA_OPEN_TIMEOUT = 6;
    private final static int MSG_REBOOT_PING_TEST = 7;
    private final static int MSG_REBOOT_PING_TEST_SUCCESS = 8;
    private final static int MSG_REBOOT_PING_TEST_FAILED = 9;

    private final static String SDCARD_PATH = "/mnt/external_sd";

    private final static int DELAY_TIME = 5;// x1000ms
    private final static int WIFI_TIME_OUT = 20000;//ms
    private final static int MOBILE_DATA_TIME_OUT = 100000;//ms
    private final int REBOOT_OFF = 0;
    private final int REBOOT_ON = 1;

    private SharedPreferences mSharedPreferences;

    private TextView mCountTV;
    private TextView mCountdownTV;
    private TextView mMaxTV;
    private TextView mWarnTV;
    private Button mStartButton;
    private Button mStopButton;
    private Button mExitBtn;
    private Button mSettingButton;
    private Button mSettingDelayButton;
    private Button mClearButton;
    private CheckBox mSdcardCheckCB;
    private CheckBox mAutoCheckSys;
    private CheckBox mAutoCheckWifiBT;
    private CheckBox mAutoCheckMobileData;

    private WakeLock mWakeLock;
    private static int mState;
    private int mCount;
    private int mCountDownTime;
    private int mMaxTimes; // max times to reboot
    private int mDelayTime; // delay time to reboot
    private boolean mIsCheckSD = false;
    private boolean mIsCheckSys = false;
    private boolean mIsCheckWifiBt = false;
    private boolean mIsCheckMobileData = false;
    private boolean mFT = false;
    private String mSdState = null;
    private String RebootMode = null;
    public String sdcard_dir_stop = null;
    private static StorageManager mStorageManager = null;
    public static String sdcard_dir = "";
    private boolean mIs3399Pro = false;
    private boolean mNpuCodeStatus = false;
    private static final int DEFAULT_DELAYED_TIME = 10;

    private WifiManager mWifiManager;
    private TelephonyManager mTelephonyManager;
    private BluetoothAdapter mBtAdapter;
    private ConnectivityManager mConnectivityManager;
    private MyBroadcastReceiver mReceiver;
    private boolean mStartWifiTest;
    private boolean mStartBtTest;
    private boolean mStartMobileTest;
    private int mBtOpenCount;//打开bt当前尝试次数
    private int mBtScanCount;//搜索bt设备当前尝试次数
    private boolean mAlreadyFoundBtDevice;//已搜索到bt设备
    private boolean mAlreadyStartPingTest;//已经开始ping测试

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (mStorageManager == null) {
            mStorageManager = (StorageManager) getSystemService(Context.STORAGE_SERVICE);
        }
        setContentView(R.layout.activity_reboot_test);
        // get the reboot flag and count.
        mSharedPreferences = getSharedPreferences("state", 0);
        mState = mSharedPreferences.getInt("reboot_flag", 0);
        mCount = mSharedPreferences.getInt("reboot_count", 0);
        mMaxTimes = mSharedPreferences.getInt("reboot_max", 0);
        mDelayTime = mSharedPreferences.getInt("reboot_delay", DEFAULT_DELAYED_TIME);
        mIsCheckSD = mSharedPreferences.getBoolean("check_sd", false);
        mIsCheckWifiBt = mSharedPreferences.getBoolean("check_wifi_bt", false);
        mIsCheckMobileData = mSharedPreferences.getBoolean("check_mobile_data", false);
        mWifiManager = (WifiManager) getSystemService(WIFI_SERVICE);
        mConnectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        mTelephonyManager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
        mIs3399Pro = "rk3399pro".equals(SystemProperties.get("ro.board.platform"));

        // init resource
        initRes();
        mWakeLock = ((PowerManager) getSystemService("power")).newWakeLock(PowerManager.FULL_WAKE_LOCK, "reboot test");
        mWakeLock.acquire();
        if (mState == REBOOT_ON) {
            if (mIsCheckSys) {
                Log.d(LOG_TAG, "======mIsCheckSys is true");
            }
            if (mIsCheckSD) {
                if (!isMountSD()) {
                    TextView tv = (TextView) findViewById(R.id.sdcard_check_tv);
                    tv.setText(getString(R.string.check_sd_result) + false);
                    tv.setVisibility(View.VISIBLE);
                    onStopClick();
                    return;
                }
            }


            if (mMaxTimes != 0 && mMaxTimes <= mCount) {
                mState = REBOOT_OFF;
                saveSharedPreferences(mState, 0);
                saveMaxTimes(0);
                updateBtnState();
                mCountTV.setText(mCountTV.getText() + " TEST FINISH!");
                return;
            } else if (mIsCheckSys) {
                if (isRebootError()) {
                    mState = REBOOT_OFF;
                    saveSharedPreferences(mState, 0);
                    saveMaxTimes(0);
                    updateBtnState();
                    mCountTV.setText(mCountTV.getText() + " Test fail for error!");
                    return;
                }
            }
            if (mIsCheckWifiBt || mIsCheckMobileData) {
                registerReceiver();
                return;
            }
            mCountDownTime = mDelayTime;//DELAY_TIME / 1000;
            mHandler.sendEmptyMessage(MSG_REBOOT_STARTCOUNT);
        }
        /*wanttoStop(this);*/

        dealComingIntent();
    }

    private void dealComingIntent() {
        Intent intent = getIntent();
        if (null != intent) {
            Bundle data = intent.getBundleExtra("data");
            if (null != data) {
                if (data.getBoolean("start", false)) {
                    int time = data.getInt("time", 0);
                    if (time < DEFAULT_DELAYED_TIME) {
                        mDelayTime = DEFAULT_DELAYED_TIME;
                    } else {
                        mDelayTime = time;
                    }
                    mFT = true;
                    saveDelayTimes(mDelayTime);
                    startRebootTest();
                }
            }
        }
    }

    private void registerReceiver() {
        mReceiver = new MyBroadcastReceiver();
        IntentFilter intentFilter = new IntentFilter();
        //wifi
        intentFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        //bt
        intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        intentFilter.addAction(BluetoothDevice.ACTION_FOUND);
        //mobile data
        intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(mReceiver, intentFilter);
        if (mIsCheckWifiBt) {
            mStartWifiTest = true;
            mWarnTV.setText(R.string.opening_wifi);
            mWifiManager.setWifiEnabled(true);
            mHandler.sendEmptyMessageDelayed(MSG_REBOOT_WIFI_OPEN_TIMEOUT,
                    WIFI_TIME_OUT);
        } else if (mIsCheckMobileData) {
            mTelephonyManager.setDataEnabled(true);
            mWarnTV.setText(R.string.open_mobile_data);
            mStartMobileTest = true;
            if (NetworkUtils.isMobileConnected(this)) {//测试连接
                mHandler.sendEmptyMessage(MSG_REBOOT_PING_TEST);
            } else {
                mHandler.sendEmptyMessageDelayed(MSG_REBOOT_MOBILE_DATA_OPEN_TIMEOUT,
                        MOBILE_DATA_TIME_OUT);
            }
        }
    }

    private void unregisterReceiver() {
        if (null != mReceiver) {
            unregisterReceiver(mReceiver);
        }
    }

/*	public void wanttoStop(Context context){

	StorageManager mStorageManager_stop = (StorageManager) getSystemService(StorageManager.class);
		final List<VolumeInfo> volumes_stop = mStorageManager_stop.getVolumes();
		Collections.sort(volumes_stop, VolumeInfo.getDescriptionComparator());
		for (VolumeInfo vol : volumes_stop) {
            if (vol.getType() == VolumeInfo.TYPE_PUBLIC) {
                Log.d(LOG_TAG, "VolumeInfo.TYPE_PUBLIC");
                Log.d(LOG_TAG, "Volume path:"+vol.getPath());
                DiskInfo disk = vol.getDisk();
                if(disk != null) {
                	if(disk.isSd()) {
                		//sdcard dir
                		StorageVolume sv = vol.buildStorageVolume(context, context.getUserId(), false);
                		sdcard_dir_stop = sv.getPath();
                        File savefile = new File(sdcard_dir_stop+"/stop.txt");
				if (savefile.exists()) {
					 Log.d(LOG_TAG+"Check", "stop.txt isChecked");
					onStopClick();
					
				}
                	}
                }
            }
        }



	}*/

    private void initRes() {
        mCountTV = (TextView) findViewById(R.id.count_tv);
        mCountTV.setText(getString(R.string.reboot_time) + mCount);
        mMaxTV = (TextView) findViewById(R.id.maxtime_tv);
        if (mMaxTimes == 0) {
            mMaxTV.setText(getString(R.string.reboot_maxtime)
                    + getString(R.string.not_setting));
        } else {
            mMaxTV.setText(getString(R.string.reboot_maxtime) + mMaxTimes);
        }
        mWarnTV = (TextView) findViewById(R.id.warn_tv);

        mStartButton = (Button) findViewById(R.id.start_btn);
        mStartButton.setOnClickListener(this);

        mStopButton = (Button) findViewById(R.id.stop_btn);
        mStopButton.setOnClickListener(this);

        mExitBtn = (Button) findViewById(R.id.exit_btn);
        mExitBtn.setOnClickListener(this);

        mSettingButton = (Button) findViewById(R.id.setting_btn);
        mSettingButton.setOnClickListener(this);

        mSettingDelayButton = (Button) findViewById(R.id.setting_delay_btn);
        mSettingDelayButton.setOnClickListener(this);

        mClearButton = (Button) findViewById(R.id.clear_btn);
        mClearButton.setOnClickListener(this);

        mSdcardCheckCB = (CheckBox) findViewById(R.id.sdcard_check_cb);
        mSdcardCheckCB.setChecked(mIsCheckSD);
        mSdcardCheckCB.setOnCheckedChangeListener(new OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {

                    if (!isMountSD()) {
                        Toast.makeText(RebootTest.this, "Please insert sdcard!", Toast.LENGTH_LONG).show();
                        buttonView.setChecked(false);
                    }
                }
            }
        });

        mAutoCheckSys = (CheckBox) findViewById(R.id.is_check_sys);
        mAutoCheckSys.setChecked(mIsCheckSys);
        mAutoCheckSys.setOnCheckedChangeListener(new OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // TODO Auto-generated method stub
            }
        });

        mAutoCheckWifiBT = (CheckBox) findViewById(R.id.is_check_wifi_bt);
        mAutoCheckWifiBT.setChecked(mIsCheckWifiBt);

        mAutoCheckMobileData = (CheckBox) findViewById(R.id.is_check_mobile_data);
        mAutoCheckMobileData.setChecked(mIsCheckMobileData);

        updateBtnState();

        mCountdownTV = (TextView) findViewById(R.id.countdown_tv);
    }

    private void reboot() {
        // save state
        saveSharedPreferences(mState, mCount + 1);

        // 重启
        /*
         * String str = "重启"; try { str = runCmd("reboot", "/system/bin"); }
         * catch (IOException e) { e.printStackTrace(); }
         */
        /*
         * Intent reboot = new Intent(Intent.ACTION_REBOOT);
         * reboot.putExtra("nowait", 1); reboot.putExtra("interval", 1);
         * reboot.putExtra("window", 0); sendBroadcast(reboot);
         */
        if (mState == REBOOT_ON) {
            String strProduct = SystemProperties.get("ro.target.product", "");
            if ("tablet".equals(strProduct)) {
                PowerManager pManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
                pManager.reboot(null);
            } else {
                SystemProperties.set("sys.powerctl", "reboot");
                System.out.println("execute cmd--> reboot\n" + "重启");
            }
        }

    }

    private void saveSharedPreferences(int flag, int count) {
        SharedPreferences.Editor edit = mSharedPreferences.edit();
        edit.putInt("reboot_flag", flag);
        edit.putInt("reboot_count", count);
        edit.putBoolean("check_sd", mIsCheckSD);
        edit.putBoolean("check_sys", mIsCheckSys);
        edit.putBoolean("check_wifi_bt", mIsCheckWifiBt);
        edit.putBoolean("check_mobile_data", mIsCheckMobileData);
        edit.commit();
    }

    private void saveMaxTimes(int max) {
        SharedPreferences.Editor edit = mSharedPreferences.edit();
        edit.putInt("reboot_max", max);
        edit.commit();
    }

    private void saveDelayTimes(int time) {
        SharedPreferences.Editor edit = mSharedPreferences.edit();
        edit.putInt("reboot_delay", time);
        edit.commit();
        Toast.makeText(this, "Set delay time:" + time + "s", Toast.LENGTH_LONG).show();
    }

    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_REBOOT:
                    Log.d(LOG_TAG, "===MSG_REBOOT mState = " + mState);
                    if (mState == 1) {
                        if ("true".equals(SystemProperties.get("persist.stresstest.stop"))) {
                            Toast.makeText(RebootTest.this, "stop because persist.stresstest.stop", Toast.LENGTH_LONG).show();
                            Log.e(LOG_TAG, "persist.stresstest.stop is true");
                            mStopButton.performClick();
                            return;
                        }
                        if (mIs3399Pro && !mNpuCodeStatus) {
                            mNpuCodeStatus = CmdUtils.getNpuCodeStatus();
                            if (!mNpuCodeStatus) {
                                Log.e(LOG_TAG, "3399pro the npu code is invalid");
                                mStopButton.performClick();
                                return;
                            }
                        }
                        Toast.makeText(RebootTest.this, "Start reboot now!!", Toast.LENGTH_LONG).show();
                        reboot();
                    }
                    break;

                case MSG_REBOOT_COUNTDOWN:
                    if (mState == 0)
                        return;
                    //                 Log.d("hjc","==MSG_REBOOT_COUNTDOWN===beforce mCountDownTime:"+mCountDownTime);
                    if (mCountDownTime != 0) {
                        mCountdownTV.setText(getString(R.string.reboot_countdown)
                                + mCountDownTime);
                        mCountdownTV.setVisibility(View.VISIBLE);
                        mCountDownTime--;
                        //               Log.d("hjc","==MSG_REBOOT_COUNTDOWN===mCountDownTime====="+mCountDownTime);
                        sendEmptyMessageDelayed(MSG_REBOOT_COUNTDOWN, 1000);
                        if (mIs3399Pro) {
                            if (3 == mCountDownTime) {
                                mNpuCodeStatus = CmdUtils.getNpuCodeStatus();
                            } else if (2 == mCountDownTime && !mNpuCodeStatus) {
                                Log.v(LOG_TAG, "3399pro start services: npu_powerctrl_resume");
                                SystemService.start("npu_powerctrl_resume");
                            }
                        }
                    } else {
                        if (mIsCheckSys) {
                            if (isSystemError()) {
                                mState = REBOOT_OFF;
                                saveSharedPreferences(mState, 0);
                                saveMaxTimes(0);
                                updateBtnState();
                                mCountTV.setText(mCountTV.getText() + " Test fail for error!");

                            } else {
                                mCountdownTV.setText(getString(R.string.reboot_countdown)
                                        + mCountDownTime);
                                mCountdownTV.setVisibility(View.VISIBLE);
                                sendEmptyMessage(MSG_REBOOT);
                                //             Log.d("hjc","===CheckSys====send MSG_REBOOT==now");
                            }
                        } else {
                            mCountdownTV.setText(getString(R.string.reboot_countdown)
                                    + mCountDownTime);
                            mCountdownTV.setVisibility(View.VISIBLE);
                            //           Log.d("hjc","===UnCheckSys====send MSG_REBOOT==now====mCountDownTime:"+mCountDownTime);
                            sendEmptyMessage(MSG_REBOOT);
                        }

                    }

                    break;
                case MSG_REBOOT_STARTCOUNT:
                    //		mWakeLock = ((PowerManager)getSystemService("power")).newWakeLock(PowerManager.FULL_WAKE_LOCK, "reboot test");
                    //		mWakeLock.acquire();
                    sendEmptyMessage(MSG_REBOOT_COUNTDOWN);
                    break;
                case MSG_REBOOT_WIFI_OPEN_TIMEOUT: {
                    mWarnTV.setText(R.string.open_wifi_timeout);
                    onStopClick();
                    break;
                }
                case MSG_REBOOT_WIFI_SCAN_TIME_OUT: {
                    mWarnTV.setText(R.string.scan_wifi_timeout);
                    onStopClick();
                    break;
                }
                case MSG_REBOOT_BT_OPEN: {
                    removeMessages(MSG_REBOOT_BT_OPEN);
                    if (mBtAdapter.isEnabled()) {
                        Log.d(LOG_TAG, "bt is open and then startDiscovery!");
                        mBtAdapter.startDiscovery();
                    } else {
                        if (mBtOpenCount < 15) {
                            Log.d(LOG_TAG, "bt is close, try to open " + mBtOpenCount);
                            mBtOpenCount++;
                            mBtAdapter.enable();
                            sendEmptyMessageDelayed(MSG_REBOOT_BT_OPEN, 1000);
                        } else {
                            mWarnTV.setText(R.string.open_bt_failed);
                            onStopClick();
                        }

                    }
                    break;
                }
                case MSG_REBOOT_MOBILE_DATA_OPEN_TIMEOUT: {
                    mWarnTV.setText("wait " + (MOBILE_DATA_TIME_OUT / 1000) + "s but not mobile data");
                    onStopClick();
                    break;
                }
                case MSG_REBOOT_PING_TEST: {
                    Log.d(LOG_TAG, "ping test mAlreadyStartPingTest=" + mAlreadyStartPingTest);
                    if (mAlreadyStartPingTest) {
                        return;
                    }
                    mWarnTV.setText(R.string.checking_mobile_data);
                    mAlreadyStartPingTest = true;
                    new Thread() {
                        @Override
                        public void run() {
                            mHandler.sendEmptyMessage(NetworkUtils.networkTest() ? MSG_REBOOT_PING_TEST_SUCCESS
                                    : MSG_REBOOT_PING_TEST_FAILED);
                        }
                    }.start();
                    break;
                }
                case MSG_REBOOT_PING_TEST_SUCCESS: {
                    mWarnTV.setText(R.string.network_connect_success);
                    mCountDownTime = mDelayTime;
                    mHandler.sendEmptyMessage(MSG_REBOOT_STARTCOUNT);
                    break;
                }
                case MSG_REBOOT_PING_TEST_FAILED: {
                    mWarnTV.setText(R.string.network_connect_failed);
                    onStopClick();
                    break;
                }
                default:
                    break;
            }
        }

        ;
    };

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.start_btn:
                onStartClick();
                break;
            case R.id.stop_btn:
                onStopClick();
                break;
            case R.id.exit_btn:
                finish();
                break;
            case R.id.setting_btn:
                onSettingClick();
                break;
            case R.id.setting_delay_btn:
                onDelayTimeClick();
                break;
            case R.id.clear_btn:
                onClearSetting();
                break;
            default:
                break;
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //       stopTest();
        unregisterReceiver();
        mHandler.removeMessages(MSG_REBOOT_WIFI_OPEN_TIMEOUT);
        mHandler.removeMessages(MSG_REBOOT_WIFI_SCAN_TIME_OUT);
        mHandler.removeMessages(MSG_REBOOT_BT_OPEN);
        mHandler.removeMessages(MSG_REBOOT_MOBILE_DATA_OPEN_TIMEOUT);
        if (mWakeLock != null && mWakeLock.isHeld())
            mWakeLock.release();
    }

    private void onStartClick() {
        mFT = true;
        String MessageString = getString(R.string.reboot_dialog_msg, mDelayTime);
        new AlertDialog.Builder(RebootTest.this)
                .setTitle(R.string.reboot_dialog_title)
                .setMessage(MessageString)
                .setPositiveButton(R.string.dialog_ok,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog,
                                                int which) {
                                startRebootTest();
                            }
                        })
                .setNegativeButton(R.string.dialog_cancel,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog,
                                                int which) {
                                dialog.cancel();
                            }
                        }).show();
    }

    private void startRebootTest() {
        mState = REBOOT_ON;
        mIsCheckSD = mSdcardCheckCB.isChecked();
        mIsCheckSys = mAutoCheckSys.isChecked();
        mIsCheckWifiBt = mAutoCheckWifiBT.isChecked();
        mIsCheckMobileData = mAutoCheckMobileData.isChecked();
        mCountDownTime = mDelayTime;//DELAY_TIME / 1000; // ms->s
        updateBtnState();
        mHandler.sendEmptyMessage(MSG_REBOOT_STARTCOUNT);
    }

    private void onStopClick() {
        mStartWifiTest = false;
        mStartBtTest = false;
        mStartMobileTest = false;
        mState = REBOOT_OFF;
        mHandler.removeMessages(MSG_REBOOT);
        mHandler.removeMessages(MSG_REBOOT_COUNTDOWN);
        mHandler.removeMessages(MSG_REBOOT_STARTCOUNT);
        mCountdownTV.setVisibility(View.INVISIBLE);
        updateBtnState();
        mStartButton.setEnabled(false);
        mIsCheckSD = false;
        mIsCheckSys = false;
        mIsCheckWifiBt = false;
        mIsCheckMobileData = false;
        saveSharedPreferences(mState, 0);
        if (mWakeLock != null && mWakeLock.isHeld()) {
            mWakeLock.release();
        }
    }

    private void onSettingClick() {
        final EditText editText = new EditText(this);
        editText.setInputType(InputType.TYPE_CLASS_NUMBER);
        new AlertDialog.Builder(this)
                .setTitle(R.string.btn_setting)
                .setView(editText)
                .setPositiveButton(R.string.dialog_ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (!editText.getText().toString().trim().equals("")) {
                            mMaxTimes = Integer.valueOf(editText.getText().toString());
                            saveMaxTimes(mMaxTimes);
                            mMaxTV.setText(getString(R.string.reboot_maxtime) + mMaxTimes);
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

    private void onClearSetting() {
        mMaxTimes = 0;
        mDelayTime = DEFAULT_DELAYED_TIME;
        saveMaxTimes(mMaxTimes);
        saveDelayTimes(mDelayTime);

        mMaxTV.setText(getString(R.string.reboot_maxtime)
                + getString(R.string.not_setting));

    }

    private void onDelayTimeClick() {
        final EditText editText = new EditText(this);
        editText.setInputType(InputType.TYPE_CLASS_NUMBER);
        new AlertDialog.Builder(this)
                .setTitle(R.string.btn_setting_delay)
                .setView(editText)
                .setPositiveButton(R.string.dialog_ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (!editText.getText().toString().trim().equals("")) {
                            int time = Integer.valueOf(editText.getText().toString());
                            if (time < DEFAULT_DELAYED_TIME) {
                                String strMsg = getString(R.string.delayed_at_least_msg, DEFAULT_DELAYED_TIME);
                                Toast.makeText(RebootTest.this, strMsg, Toast.LENGTH_SHORT).show();
                            } else {
                                mDelayTime = time;
                                saveDelayTimes(mDelayTime);
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

    private void SavedRebootMode() {
        Process process = null;
        String filePath = "mnt/internal_sd/boot_mode.txt";
        File file1 = new File(filePath);
        if (file1.isFile() && file1.exists()) {
            file1.delete();
        }
        try {
            StresstestUtil.getBootMode(true);
        } catch (Exception e) {
            Log.e(LOG_TAG, "getBootMode fail!!!");
        }
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        try {
            String encoding = "GBK";
            File file = new File(filePath);
            if (file.isFile() && file.exists()) { //判断文件是否存在
                InputStreamReader read = new InputStreamReader(
                        new FileInputStream(file), encoding);//考虑到编码格式
                BufferedReader bufferedReader = new BufferedReader(read);
                String lineTxt = null;
                while ((lineTxt = bufferedReader.readLine()) != null) {
                    Log.d(LOG_TAG, lineTxt);
                    int p1 = lineTxt.indexOf("(");
                    int p2 = lineTxt.indexOf(")");
                    RebootMode = lineTxt.substring(p1 + 1, p2);
                    Toast.makeText(this, RebootMode, Toast.LENGTH_LONG).show();

                }
                read.close();
            } else {
                Log.e(LOG_TAG, "not find the mnt/internal_sd/boot_mode.txt");
            }
        } catch (Exception e) {
            Log.e(LOG_TAG, "read error!!");
            e.printStackTrace();
        }
    }

    private boolean isRebootError() {
        SavedRebootMode();

        if (RebootMode != null) {
            if (Integer.valueOf(RebootMode) == 7) {
                Dialog dialog = new AlertDialog.Builder(
                        this)
                        .setTitle("REBOOT TEST FAIL")
                        .setMessage("It's reboot fail by panic,please analysis last_log")
                        .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                dialog.cancel();
                            }
                        }).setNegativeButton("Cancle", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                dialog.cancel();
                            }
                        }).create();
                dialog.show();
                return true;

            } else if (Integer.valueOf(RebootMode) == 8) {
                Dialog dialog = new AlertDialog.Builder(
                        this)
                        .setTitle("REBOOT TEST FAIL")
                        .setMessage("It's reboot fail by watchdog,please analysis last_log")
                        .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                dialog.cancel();
                            }
                        }).setNegativeButton("Cancle", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                dialog.cancel();
                            }
                        }).create();
                dialog.show();
                return true;
            }
            return false;
        }
        return false;
    }

    private boolean isSystemError() {

        InputStreamReader reader = null;
        BufferedReader bufferedReader = null;
        Process process = null;
        String lineText = null;
        if (!mFT) {
            try {
                process = Runtime.getRuntime().exec("logcat -d");
                reader = new InputStreamReader(process.getInputStream());
                bufferedReader = new BufferedReader(reader);

                while ((lineText = bufferedReader.readLine()) != null) {
                    //	Log.d("--hjc","-------------->>lineTxt:"+lineText);
                    if (lineText.indexOf("Force finishing activity") != -1 || lineText.indexOf("backtrace:") != -1) {
                        Log.d("reboot test", "------lineTxt:" + lineText);
                        Dialog dialog = new AlertDialog.Builder(
                                this)
                                .setTitle("REBOOT TEST FAIL")
                                .setMessage("System has some error,please analysis logcat")
                                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int whichButton) {
                                        dialog.cancel();
                                    }
                                }).setNegativeButton("Cancle", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int whichButton) {
                                        dialog.cancel();
                                    }
                                }).create();
                        dialog.show();
                        reader.close();
                        bufferedReader.close();
                        return true;
                    }
                }
                reader.close();
                bufferedReader.close();
                return false;
            } catch (Exception e) {
                Log.e(LOG_TAG, "process Runtime error!!");
                e.printStackTrace();
            }
        }
        return false;

    }

    private void updateBtnState() {
        mStartButton.setEnabled(mState == REBOOT_OFF);
        mClearButton.setEnabled(mState == REBOOT_OFF);
        mSettingButton.setEnabled(mState == REBOOT_OFF);
        mSettingDelayButton.setEnabled(mState == REBOOT_OFF);
        mStopButton.setEnabled(mState == REBOOT_ON);
    }

    /*
        public static String getSdCardState() {
            try {
                IMountService mMntSvc = null;
                if (mMntSvc == null) {
                    mMntSvc = IMountService.Stub.asInterface(ServiceManager
                                                             .getService("mount"));
                }
                return mMntSvc.getVolumeState(SDCARD_PATH);
            } catch (Exception rex) {
                return Environment.MEDIA_REMOVED;
            }

        }
        */
    public static boolean isMountSD() {
        final List<VolumeInfo> volumes = mStorageManager.getVolumes();
        Collections.sort(volumes, VolumeInfo.getDescriptionComparator());

        for (VolumeInfo vol : volumes) {
            if (vol.getType() == VolumeInfo.TYPE_PUBLIC) {
                DiskInfo disk = vol.getDisk();
                if (disk != null) {
                    if (disk.isSd()) {
                        // sdcard dir
                        int status = vol.getState();
                        if (status == VolumeInfo.STATE_MOUNTED) {
                            sdcard_dir = vol.path;
                            return true;
                        }
                        return false;
                    }
                }
            }
        }
        return false;
    }

    class MyBroadcastReceiver extends BroadcastReceiver {

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.d(LOG_TAG, "action:" + action + " mStartWifiTest=" + mStartWifiTest + ", mStartBtTest=" + mStartBtTest);
            if (mStartWifiTest) {
                if (WifiManager.WIFI_STATE_CHANGED_ACTION.equals(action)) {
                    int state = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE,
                            WifiManager.WIFI_STATE_UNKNOWN);
                    if (WifiManager.WIFI_STATE_ENABLED == state) {
                        mHandler.removeMessages(MSG_REBOOT_WIFI_OPEN_TIMEOUT);
                        mHandler.removeMessages(MSG_REBOOT_WIFI_SCAN_TIME_OUT);
                        mHandler.sendEmptyMessageDelayed(MSG_REBOOT_WIFI_SCAN_TIME_OUT,
                                WIFI_TIME_OUT);
                        mWifiManager.startScan();
                    }
                } else if (WifiManager.SCAN_RESULTS_AVAILABLE_ACTION.equals(action)) {
                    mStartWifiTest = false;
                    mHandler.removeMessages(MSG_REBOOT_WIFI_OPEN_TIMEOUT);
                    mHandler.removeMessages(MSG_REBOOT_WIFI_SCAN_TIME_OUT);
                    List<ScanResult> resultList = mWifiManager.getScanResults();
                    if (null == resultList || resultList.isEmpty()) {
                        mWarnTV.setText(R.string.scan_wifi_list_empty);
                        onStopClick();
                    } else {
                        Log.v(LOG_TAG, "搜索到wifi数量 " + resultList.size());
                        mWarnTV.setText("wifi " + resultList.size());

                        //开始进行蓝牙测试
                        mStartBtTest = true;
                        mBtAdapter = BluetoothAdapter.getDefaultAdapter();
                        mHandler.sendEmptyMessage(MSG_REBOOT_BT_OPEN);
                    }
                }
            } else if (mStartBtTest) {
                if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                    if (mAlreadyFoundBtDevice) {
                        mStartBtTest = false;
                        if (mIsCheckMobileData) {
                            mTelephonyManager.setDataEnabled(true);
                            mWarnTV.setText(R.string.open_mobile_data);
                            //进行数据网络测试
                            mStartMobileTest = true;
                            if (NetworkUtils.isMobileConnected(RebootTest.this)) {//测试连接
                                mHandler.sendEmptyMessage(MSG_REBOOT_PING_TEST);
                            } else {
                                mHandler.sendEmptyMessageDelayed(MSG_REBOOT_MOBILE_DATA_OPEN_TIMEOUT,
                                        MOBILE_DATA_TIME_OUT);
                            }
                        } else {
                            mCountDownTime = mDelayTime;
                            mHandler.sendEmptyMessage(MSG_REBOOT_STARTCOUNT);
                        }
                    } else if (mBtScanCount < 3 && mBtAdapter.isEnabled()) {
                        Log.d(LOG_TAG, "bt is open and then startDiscovery " + mBtScanCount);
                        mBtScanCount++;
                        mWarnTV.setText(getString(R.string.scanning_bt) + " " + mBtScanCount);
                        mBtAdapter.startDiscovery();
                    } else {
                        mStartBtTest = false;
                        mWarnTV.setText(R.string.scan_bt_empty);
                        onStopClick();
                    }
                } else if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                    Log.d(LOG_TAG, "already found other bt device");
                    mWarnTV.setText(R.string.scan_bt_success);
                    mAlreadyFoundBtDevice = true;
                    mBtAdapter.cancelDiscovery();
                }
            } else if (mStartMobileTest) {
                if (ConnectivityManager.CONNECTIVITY_ACTION.equals(action)) {
                    if (NetworkUtils.isMobileConnected(RebootTest.this)) {
                        mHandler.sendEmptyMessage(MSG_REBOOT_PING_TEST);
                    }
                }
            }
        }
    }

}
