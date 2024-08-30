package com.cghs.stresstest.test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.DialogInterface;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Message;
import android.telephony.TelephonyManager;
import android.widget.Toast;

import com.cghs.stresstest.R;

import android.os.Environment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.CompoundButton;
//import com.android.internal.os.storage.ExternalStorageFormatter;
import com.cghs.stresstest.TestItems;
import com.cghs.stresstest.util.CmdUtils;
import com.cghs.stresstest.util.NetworkUtils;
import com.cghs.stresstest.util.StresstestUtil;

import android.util.Log;

import java.io.FileInputStream;

import android.app.Dialog;

import java.io.InputStreamReader;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;

import android.app.KeyguardManager;
import android.os.SystemProperties;
import android.os.SystemService;
import android.os.storage.VolumeInfo;
import android.os.storage.DiskInfo;
import android.os.storage.StorageVolume;
import android.os.storage.StorageManager;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Collections;


import android.content.IntentFilter;
import android.content.BroadcastReceiver;
import android.net.Uri;

import java.io.FileReader;
import java.io.FileDescriptor;

public class RecoveryTest extends StressBase {
    public static final String TAG = "RecoveryTest";

    public static final String RECOVERY_STATE_FILE = /*Environment.getExternalStorageDirectory().getPath() +*/ "/cache/recovery/Recovery_state";
    //public static final String RECOVERY_STATE_FILE_TF = "/mnt/external_sd/Recovery_state";
    public static String RECOVERY_STATE_FILE_TF = "/mnt/external_sd/Recovery_state";

    private final static int MSG_RECOVERY_WIFI_OPEN_TIMEOUT = 1;
    private final static int MSG_RECOVERY_WIFI_SCAN_TIME_OUT = 2;
    private final static int MSG_RECOVERY_BT_OPEN = 3;
    private final static int MSG_RECOVERY_MOBILE_DATA_OPEN_TIMEOUT = 4;
    private final static int MSG_RECOVERY_PING_TEST = 5;
    private final static int MSG_RECOVERY_PING_TEST_SUCCESS = 6;
    private final static int MSG_RECOVERY_PING_TEST_FAILED = 7;

    private final static int WIFI_TIME_OUT = 20000;//ms
    private final static int MOBILE_DATA_TIME_OUT = 100000;//ms

    public String usb_dir = null;
    public String sdcard_dir = null;
    public String sdcard_dir_stop = null;

    private TextView mMaxView;
    private TextView mTestTimeTv;
    private TextView mCountdownTv;
    private TextView mWarning_tf;
    private TextView mWarnTV;

    private CheckBox mEraseFlashCb;
    private CheckBox mWipeAllCb;
    private CheckBox mCheckSys;
    private CheckBox mCheckWifiBT;
    private CheckBox mCheckMobileData;
    private CountDownTimer mCountDownTimer;

    private int mStartTest = 0;
    private String RebootMode = null;

    private WakeLock mWakeLock;
    private boolean mIsEraseFlash = false;
    private boolean mIsWipeAll = false;
    private boolean mIsCheckSys = false;
    private boolean mIsCheckWifiBt = false;
    private boolean mIsCheckMobileData = false;
    private boolean mFT = false;
    private MyReceiver mReceiver;
    public static final int MSG_START_TEST = 1;
    private String UMSstate = SystemProperties.get("ro.factory.hasUMS");
    private boolean mIs3399Pro = false;
    private boolean mNpuCodeStatus = false;
    private static final int DEFAULT_DELAYED_TIME = 10;

    private WifiManager mWifiManager;
    private TelephonyManager mTelephonyManager;
    private BluetoothAdapter mBtAdapter;
    private ConnectivityManager mConnectivityManager;
    private MyBroadcastReceiver mWifiBtReceiver;
    private boolean mStartWifiTest;
    private boolean mStartBtTest;
    private boolean mStartMobileTest;
    private int mBtOpenCount;//打开bt当前尝试次数
    private int mBtScanCount;//搜索bt设备当前尝试次数
    private boolean mAlreadyFoundBtDevice;//已搜索到bt设备
    private boolean mAlreadyStartPingTest;//已经开始ping测试

    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_RECOVERY_WIFI_OPEN_TIMEOUT: {
                    mWarnTV.setText(R.string.open_wifi_timeout);
                    onStopClick();
                    break;
                }
                case MSG_RECOVERY_WIFI_SCAN_TIME_OUT: {
                    mWarnTV.setText(R.string.scan_wifi_timeout);
                    onStopClick();
                    break;
                }
                case MSG_RECOVERY_BT_OPEN: {
                    removeMessages(MSG_RECOVERY_BT_OPEN);
                    if (mBtAdapter.isEnabled()) {
                        Log.d(TAG, "bt is open and then startDiscovery!");
                        mBtAdapter.startDiscovery();
                    } else {
                        if (mBtOpenCount < 15) {
                            Log.d(TAG, "bt is close, try to open " + mBtOpenCount);
                            mBtOpenCount++;
                            mBtAdapter.enable();
                            sendEmptyMessageDelayed(MSG_RECOVERY_BT_OPEN, 1000);
                        } else {
                            mWarnTV.setText(R.string.open_bt_failed);
                            onStopClick();
                        }

                    }
                    break;
                }
                case MSG_RECOVERY_MOBILE_DATA_OPEN_TIMEOUT: {
                    mWarnTV.setText("wait " + (MOBILE_DATA_TIME_OUT / 1000) + "s but not mobile data");
                    onStopClick();
                    break;
                }
                case MSG_RECOVERY_PING_TEST: {
                    Log.d(TAG, "ping test mAlreadyStartPingTest=" + mAlreadyStartPingTest);
                    if (mAlreadyStartPingTest) {
                        return;
                    }
                    mWarnTV.setText(R.string.checking_mobile_data);
                    mAlreadyStartPingTest = true;
                    new Thread() {
                        @Override
                        public void run() {
                            mHandler.sendEmptyMessage(NetworkUtils.networkTest() ? MSG_RECOVERY_PING_TEST_SUCCESS
                                    : MSG_RECOVERY_PING_TEST_FAILED);
                        }
                    }.start();
                    break;
                }
                case MSG_RECOVERY_PING_TEST_SUCCESS: {
                    mWarnTV.setText(R.string.network_connect_success);
                    preStartTest();
                    break;
                }
                case MSG_RECOVERY_PING_TEST_FAILED: {
                    mWarnTV.setText(R.string.network_connect_failed);
                    onStopClick();
                    break;
                }
                default:
                    break;
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recovery_test);
        mIs3399Pro = "rk3399pro".equals(SystemProperties.get("ro.board.platform"));
        mWifiManager = (WifiManager) getSystemService(WIFI_SERVICE);
        mConnectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        mTelephonyManager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
        setDefaultBtnId(R.id.start_btn, R.id.stop_btn, R.id.exit_btn,
                R.id.maxtime_btn);
        mMaxView = (TextView) findViewById(R.id.maxtime_tv);
        mTestTimeTv = (TextView) findViewById(R.id.testtime_tv);
        mCountdownTv = (TextView) findViewById(R.id.countdown_tv);
        mWarning_tf = (TextView) findViewById(R.id.warning_tf);
        mWarnTV = (TextView) findViewById(R.id.warn_tv);

        mEraseFlashCb = (CheckBox) findViewById(R.id.erase_cb);
        mWipeAllCb = (CheckBox) findViewById(R.id.wipeall_cb);
        mCheckSys = (CheckBox) findViewById(R.id.check_sys);
        mCheckWifiBT = (CheckBox) findViewById(R.id.check_wifi_bt);
        mCheckMobileData = (CheckBox) findViewById(R.id.check_mobile_data);
        /*
            try{
               String encoding="GBK";
                String path = getRK_StressTest(this);
                Log.e(TAG, "------------- mount path = " + path);
                File checkFile = new File(path);
                if (checkFile.isFile() && checkFile.exists()) {
                InputStreamReader read = new InputStreamReader(
                    new FileInputStream(checkFile),encoding);
                 BufferedReader bufferedReader = new BufferedReader(read);
                   String lineTxt = null;
                    while((lineTxt = bufferedReader.readLine()) != null){
                        Log.d(TAG,lineTxt);
                        if(lineTxt.startsWith("#")){
                                        continue;
                                        }
                                        if (lineTxt.contains("recovery 0")) {
                                           Log.e(TAG,"read read read here");
                                             stopTest();
                                             finish();
                                        }
                        
                    }
                    read.close();
                }
            
            }catch  (Exception e){
            Log.e(TAG,"read error!!");
            e.printStackTrace();

            }*/
//	((KeyguardManager)getSystemService("keyguard")).newKeyguardLock("TestRecovery").disableKeyguard();
        stopTest();
        mStartBtn.setEnabled(true);
        mWakeLock = ((PowerManager) getSystemService("power")).newWakeLock(PowerManager.FULL_WAKE_LOCK, "RecoveryTest");
        mWakeLock.acquire();
        /*if(!UMSstate()){
            mEraseFlashCb.setVisibility(View.GONE);
            mWarning_tf.setVisibility(View.VISIBLE);
        }*/
        init_StoragePath(this);
        Log.d(TAG, "RECOVERY_STATE_FILE_TF:" + RECOVERY_STATE_FILE_TF);
        initData();
        updateUI();


        mEraseFlashCb.setOnCheckedChangeListener(new CheckBox.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
                // TODO Auto-generated method stub
                if (arg1)
                    mWarning_tf.setVisibility(View.VISIBLE);
                else
                    mWarning_tf.setVisibility(View.GONE);
            }
        });
        mWipeAllCb.setOnCheckedChangeListener(new CheckBox.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
                // TODO Auto-generated method stub
                if (arg1)
                    mWarning_tf.setVisibility(View.VISIBLE);
                else
                    mWarning_tf.setVisibility(View.GONE);
            }
        });

        mCheckSys.setOnCheckedChangeListener(new CheckBox.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
                // TODO Auto-generated method stub
            }
        });
        if (mStartTest == 1) {
            updateTestTimeTV();
            if (mIsCheckSys) {
                if (isRebootError()) {
                    stopTest();
                    Log.e(TAG, "check system error,stop test!!");
                    mTestTimeTv.setText(mTestTimeTv.getText() + " Test fail for error!");
                } else if (mMaxTestCount == 0 || mCurrentCount < mMaxTestCount) {
                    preStartTest();
                }
                return;
            }
            if (mMaxTestCount == 0 || mCurrentCount < mMaxTestCount) {
                if (mIsCheckWifiBt || mIsCheckMobileData) {
                    registerWifiBtReceiver();
                    return;
                }
                preStartTest();
            }
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
                    /*int time = intent.getIntExtra("time", 0);
                    if (time < DEFAULT_DELAYED_TIME) {
                        mDelayTime = DEFAULT_DELAYED_TIME;
                    } else {
                        mDelayTime = time;
                    }*/
                    mFT = true;
                    preStartTest();
                }
            }
        }
    }

    private void registerWifiBtReceiver() {
        mWifiBtReceiver = new MyBroadcastReceiver();
        IntentFilter intentFilter = new IntentFilter();
        //wifi
        intentFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        //bt
        intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        intentFilter.addAction(BluetoothDevice.ACTION_FOUND);
        //mobile data
        intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(mWifiBtReceiver, intentFilter);
        if (mIsCheckWifiBt) {
            mStartWifiTest = true;
            mWarnTV.setText(R.string.opening_wifi);
            mWifiManager.setWifiEnabled(true);
            mHandler.sendEmptyMessageDelayed(MSG_RECOVERY_WIFI_OPEN_TIMEOUT,
                    WIFI_TIME_OUT);
        } else if (mIsCheckMobileData) {
            mWarnTV.setText(R.string.open_mobile_data);
            mTelephonyManager.setDataEnabled(true);
            mStartMobileTest = true;
            if (NetworkUtils.isMobileConnected(this)) {//测试连接
                mHandler.sendEmptyMessage(MSG_RECOVERY_PING_TEST);
            } else {
                mHandler.sendEmptyMessageDelayed(MSG_RECOVERY_MOBILE_DATA_OPEN_TIMEOUT,
                        MOBILE_DATA_TIME_OUT);
            }
        }
    }

    private void unregisterReceiver() {
        if (null != mReceiver) {
            unregisterReceiver(mReceiver);
        }
        if (null != mWifiBtReceiver) {
            unregisterReceiver(mWifiBtReceiver);
        }
    }

    public void wanttoStop(Context context) {

        StorageManager mStorageManager_stop = (StorageManager) getSystemService(StorageManager.class);
        final List<VolumeInfo> volumes_stop = mStorageManager_stop.getVolumes();
        Collections.sort(volumes_stop, VolumeInfo.getDescriptionComparator());
        for (VolumeInfo vol : volumes_stop) {
            if (vol.getType() == VolumeInfo.TYPE_PUBLIC) {
                Log.d(TAG, "VolumeInfo.TYPE_PUBLIC");
                Log.d(TAG, "Volume path:" + vol.getPath());
                DiskInfo disk = vol.getDisk();
                stopTest();
                if (disk != null) {
                    if (disk.isSd()) {
                        //sdcard dir
                        StorageVolume sv = vol.buildStorageVolume(context, context.getUserId(), false);
                        sdcard_dir_stop = sv.getPath();
                        File savefile = new File(sdcard_dir_stop + "/stop.txt");
                        if (savefile.exists()) {
                            Log.d(TAG + "Check", "stop.txt isChecked");
                        }
                    }
                }
            }
        }


    }

    public void init_StoragePath(Context context) {
        StorageManager mStorageManager = (StorageManager) getSystemService(StorageManager.class);
        //flash dir
        //	flash_dir = Environment.getExternalStorageDirectory().getPath();
        final List<VolumeInfo> volumes = mStorageManager.getVolumes();
        Collections.sort(volumes, VolumeInfo.getDescriptionComparator());
        for (VolumeInfo vol : volumes) {
            if (vol.getType() == VolumeInfo.TYPE_PUBLIC) {
                Log.d(TAG, "VolumeInfo.TYPE_PUBLIC");
                Log.d(TAG, "Volume path:" + vol.getPath());
                DiskInfo disk = vol.getDisk();
                if (disk != null) {
                    if (disk.isSd()) {
                        //sdcard dir
                        StorageVolume sv = vol.buildStorageVolume(context, context.getUserId(), false);
                        sdcard_dir = sv.getPath();
                        RECOVERY_STATE_FILE_TF = new String(sdcard_dir) + "/Recovery_state";
                    } else if (disk.isUsb()) {
                        //usb dir
                        StorageVolume sv = vol.buildStorageVolume(context, context.getUserId(), false);
                        usb_dir = sv.getPath();
                        RECOVERY_STATE_FILE_TF = new String(usb_dir) + "/Recovery_state";
                    }
                }
            }
        }
    }

    private boolean UMSstate() {
        return UMSstate.equals("true");
    }

    private void initData() {
        mStartTest = getIntent().getIntExtra("enable", 0);
        mCurrentCount = getIntent().getIntExtra("cur", 0);
        mMaxTestCount = getIntent().getIntExtra("max", 0);
        if (mMaxTestCount < 0)
            mMaxTestCount = 0;
        mIsWipeAll = getIntent().getBooleanExtra("wipeall", false);
        mIsEraseFlash = getIntent().getBooleanExtra("eraseflash", false);
        mIsCheckSys = getIntent().getBooleanExtra("checksys", false);
        mIsCheckWifiBt = getIntent().getBooleanExtra("checkwifibt", false);
        mIsCheckMobileData = getIntent().getBooleanExtra("checkmobiledata", false);
        Log.d(TAG + "status", "" + mStartTest + "--"
                + mCurrentCount + "--" + mMaxTestCount
                + "--" + mIsWipeAll + "--" + mIsEraseFlash + "-" + mIsCheckSys
                + "--" + "mIsCheckWifiBt=" + mIsCheckWifiBt + ", mIsCheckMobileData=" + mIsCheckMobileData);
    }


    private void updateUI() {
        updateMaxTV();
        mEraseFlashCb.setChecked(mIsEraseFlash);
        mWipeAllCb.setChecked(mIsWipeAll);
        mCheckSys.setChecked(mIsCheckSys);
        mCheckWifiBT.setChecked(mIsCheckWifiBt);
        mCheckMobileData.setChecked(mIsCheckMobileData);
    }


    @Override
    public void updateMaxTV() {
        super.updateMaxTV();
        mMaxView.setText(getString(R.string.max_test_time) + mMaxTestCount);
    }

    public void updateTestTimeTV() {
        mTestTimeTv.setText(getString(R.string.already_test_time) + mCurrentCount);
        mTestTimeTv.setVisibility(View.VISIBLE);
    }


    @Override
    public void onStartClick() {
        mFT = true;
        preStartTest();
    }

    @Override
    public void onStopClick() {
        stopTest();
    }

    @Override
    public void onSetMaxClick() {

    }

    public void preStartTest() {
        mIsEraseFlash = mEraseFlashCb.isChecked();
        mIsWipeAll = mWipeAllCb.isChecked();
        mIsCheckSys = mCheckSys.isChecked();
        mIsCheckWifiBt = mCheckWifiBT.isChecked();
        mIsCheckMobileData = mCheckMobileData.isChecked();
        mStartTest = 1;
        incCurCount();
        writeRecoveryState(formatStateContent());
        isRunning = true;

        updateBtnState();
        mCountDownTimer = new CountDownTimer(30000, 1000) {

            @Override
            public void onTick(long millisUntilFinished) {
                mCountdownTv.setText((millisUntilFinished / 1000) + "");
                mCountdownTv.setVisibility(View.VISIBLE);
                if (mIs3399Pro) {
                    int time = (int) (millisUntilFinished / 1000);
                    if (3 == time) {
                        mNpuCodeStatus = CmdUtils.getNpuCodeStatus();
                    } else if (2 == time && !mNpuCodeStatus) {
                        Log.v(TAG, "3399pro start services: npu_powerctrl_resume");
                        SystemService.start("npu_powerctrl_resume");
                    }
                }
            }

            @Override
            public void onFinish() {
                mCountdownTv.setVisibility(View.INVISIBLE);
                if (mIsCheckSys) {
                    if (isSystemError()) {
                        stopTest();
                        mTestTimeTv.setText(mTestTimeTv.getText() + " Test fail for error!");
                    } else {
                        startTest();
                    }
                } else {
                    startTest();
                }
            }
        }.start();
    }

    private void startTest() {
        if ("true".equals(SystemProperties.get("persist.stresstest.stop"))) {
            Toast.makeText(RecoveryTest.this, "stop because persist.stresstest.stop", Toast.LENGTH_LONG).show();
            Log.e(TAG, "persist.stresstest.stop is true");
            mStopBtn.performClick();
            return;
        }
        if (mIs3399Pro && !mNpuCodeStatus) {
            mNpuCodeStatus = CmdUtils.getNpuCodeStatus();
            if (!mNpuCodeStatus) {
                Log.e(TAG, "3399pro the npu code is invalid");
                mStopBtn.performClick();
                return;
            }
        }
        Log.d(TAG, "startTest mIsWipeAll=" + mIsWipeAll + ", mIsEraseFlash=" + mIsEraseFlash);
        if (mIsWipeAll || mIsEraseFlash) {
            try {
                bootCommand(this, "--wipe_all");
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
/*		} else if (mIsEraseFlash) {
            Intent intent = new Intent(ExternalStorageFormatter.FORMAT_AND_FACTORY_RESET);
            intent.setComponent(ExternalStorageFormatter.COMPONENT_NAME);
            intent.putExtra(Intent.EXTRA_REASON, "WipeAllFlash");
            this.startService(intent);*/
        } else {
            //writeRecoveryState(formatStateContent());
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            Intent intent = new Intent(Intent.ACTION_FACTORY_RESET);
            intent.setPackage("android");
            intent.addFlags(Intent.FLAG_RECEIVER_FOREGROUND);
            intent.putExtra(Intent.EXTRA_REASON, "MasterClearConfirm");
            sendBroadcast(intent);
            Log.d(TAG, "send FACTORY_RESET  broadcast");
        }
    }

    private void stopTest() {
        Log.d(TAG, "stopTest");
        mStartWifiTest = false;
        mStartBtTest = false;
        mStartMobileTest = false;
        mIsCheckWifiBt = false;
        mIsCheckMobileData = false;
        isRunning = false;
        updateBtnState();
        mStartBtn.setEnabled(false);
        mStartTest = 0;
        mCurrentCount = 0;
        if (mCountDownTimer != null)
            mCountDownTimer.cancel();
        mCountdownTv.setVisibility(View.INVISIBLE);
        writeRecoveryState(formatStateContent());
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
            Log.e(TAG, "getBootMode fail!!!");
        }
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        try {
            String encoding = "GBK";
            File file = new File(filePath);
            if (file.isFile() && file.exists()) { //
                InputStreamReader read = new InputStreamReader(
                        new FileInputStream(file), encoding);//
                BufferedReader bufferedReader = new BufferedReader(read);
                String lineTxt = null;
                while ((lineTxt = bufferedReader.readLine()) != null) {
                    Log.d(TAG, lineTxt);
                    int p1 = lineTxt.indexOf("(");
                    int p2 = lineTxt.indexOf(")");
                    RebootMode = lineTxt.substring(p1 + 1, p2);
                    Toast.makeText(this, RebootMode, Toast.LENGTH_LONG).show();

                }
                read.close();
            } else {
                Log.e(TAG, "not find the mnt/internal_sd/boot_mode.txt");
            }
        } catch (Exception e) {
            Log.e(TAG, "read error!!");
            e.printStackTrace();
        }
    }

    private boolean isRebootError() {
        SavedRebootMode();

        if (RebootMode != null) {
            if (Integer.valueOf(RebootMode) == 7) {
                Dialog dialog = new AlertDialog.Builder(
                        this)
                        .setTitle(getString(R.string.factory_reset_excep))
                        .setMessage(getString(R.string.panic_reboot))
                        .setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                dialog.cancel();
                            }
                        }).setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                dialog.cancel();
                            }
                        }).create();
                dialog.show();
                return true;

            } else if (Integer.valueOf(RebootMode) == 8) {
                Dialog dialog = new AlertDialog.Builder(
                        this)
                        .setTitle("RecoveryTest Error")
                        .setMessage("It's reboot for watchdog,see thelast_log for details")
                        .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                dialog.cancel();
                            }
                        }).setNegativeButton("cancel", new DialogInterface.OnClickListener() {
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
                Log.d("--hjc", "-------------->>lineTxt:" + lineText);
                while ((lineText = bufferedReader.readLine()) != null) {
                    //			Log.d("--hjc","-------------->>lineTxt:"+lineText);
                    if (lineText.indexOf("Force finishing activity") != -1 || lineText.indexOf("backtrace:") != -1) {
                        Log.d("--hjc", "------lineTxt:" + lineText);
                        Dialog dialog = new AlertDialog.Builder(this)
                                .setTitle("RecoveryTest Error")
                                .setMessage("It's reboot for system,see logcat for details")
                                .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int whichButton) {
                                        dialog.cancel();
                                    }
                                }).setNegativeButton("cancel", new DialogInterface.OnClickListener() {
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
                Log.e(TAG, "process Runtime error!!");
                e.printStackTrace();
            }
        }
        return false;
    }

    private String formatStateContent() {
        StringBuilder sb = new StringBuilder();
        sb.append("enable:").append(mStartTest).append("\n");
        sb.append("currenttime:").append(mCurrentCount).append("\n");
        sb.append("maxtime:").append(mMaxTestCount).append("\n");
        sb.append("wipeall:").append(mIsWipeAll ? "1" : "0").append("\n");
        sb.append("eraseflash:").append(mIsEraseFlash ? "1" : "0").append("\n");
        sb.append("checksys:").append(mIsCheckSys ? "1" : "0").append("\n");
        sb.append("checkwifibt:").append(mIsCheckWifiBt ? "1" : "0").append("\n");
        sb.append("checkmobiledata:").append(mIsCheckMobileData ? "1" : "0").append("\n");
        return sb.toString();
    }

    private void writeRecoveryState(String content) {
        FileOutputStream fos = null;
        FileOutputStream fos1 = null;
        File file, file1;
        if (/*mIsEraseFlash ||*/ mIsWipeAll /*|| !UMSstate()*/) {
            file = new File(RECOVERY_STATE_FILE_TF);
        } else {
            file = new File(RECOVERY_STATE_FILE);
        }

        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try {
            fos = new FileOutputStream(file);
            fos.write(content.getBytes());
            fos.flush();
            FileDescriptor fd_fos = fos.getFD();
            fd_fos.sync();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException ie) {
            ie.printStackTrace();
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");
        unregisterReceiver();
        stopTest();
        mHandler.removeMessages(MSG_RECOVERY_WIFI_OPEN_TIMEOUT);
        mHandler.removeMessages(MSG_RECOVERY_WIFI_SCAN_TIME_OUT);
        mHandler.removeMessages(MSG_RECOVERY_BT_OPEN);
        mHandler.removeMessages(MSG_RECOVERY_MOBILE_DATA_OPEN_TIMEOUT);
        mWakeLock.release();
    }

    class MyBroadcastReceiver extends BroadcastReceiver {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.d(TAG, "action:" + action);
            if (mStartWifiTest) {
                if (WifiManager.WIFI_STATE_CHANGED_ACTION.equals(action)) {
                    int state = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE,
                            WifiManager.WIFI_STATE_UNKNOWN);
                    if (WifiManager.WIFI_STATE_ENABLED == state) {
                        mHandler.removeMessages(MSG_RECOVERY_WIFI_OPEN_TIMEOUT);
                        mHandler.removeMessages(MSG_RECOVERY_WIFI_SCAN_TIME_OUT);
                        mHandler.sendEmptyMessageDelayed(MSG_RECOVERY_WIFI_SCAN_TIME_OUT,
                                WIFI_TIME_OUT);
                        mWifiManager.startScan();
                    }
                } else if (WifiManager.SCAN_RESULTS_AVAILABLE_ACTION.equals(action)) {
                    mStartWifiTest = false;
                    mHandler.removeMessages(MSG_RECOVERY_WIFI_OPEN_TIMEOUT);
                    mHandler.removeMessages(MSG_RECOVERY_WIFI_SCAN_TIME_OUT);
                    List<ScanResult> resultList = mWifiManager.getScanResults();
                    if (null == resultList || resultList.isEmpty()) {
                        mWarnTV.setText(R.string.scan_wifi_list_empty);
                        onStopClick();
                    } else {
                        Log.v(TAG, "搜索到wifi数量 " + resultList.size());
                        mWarnTV.setText("wifi " + resultList.size());

                        //开始进行蓝牙测试
                        mStartBtTest = true;
                        mBtAdapter = BluetoothAdapter.getDefaultAdapter();
                        mHandler.sendEmptyMessage(MSG_RECOVERY_BT_OPEN);
                    }
                }
            } else if (mStartBtTest) {
                if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                    if (mAlreadyFoundBtDevice) {
                        mStartBtTest = false;
                        mWarnTV.setText(R.string.scan_bt_success);
                        if (mIsCheckMobileData) {
                            //进行数据网络测试
                            mTelephonyManager.setDataEnabled(true);
                            mWarnTV.setText(R.string.open_mobile_data);
                            mStartMobileTest = true;
                            if (NetworkUtils.isMobileConnected(RecoveryTest.this)) {//测试连接
                                mHandler.sendEmptyMessage(MSG_RECOVERY_PING_TEST);
                            } else {
                                mHandler.sendEmptyMessageDelayed(MSG_RECOVERY_MOBILE_DATA_OPEN_TIMEOUT,
                                        MOBILE_DATA_TIME_OUT);
                            }
                        } else {
                            preStartTest();
                        }
                    } else if (mBtScanCount < 3 && mBtAdapter.isEnabled()) {
                        Log.d(TAG, "bt is open and then startDiscovery " + mBtScanCount);
                        mBtScanCount++;
                        mWarnTV.setText(getString(R.string.scanning_bt) + " " + mBtScanCount);
                        mBtAdapter.startDiscovery();
                    } else {
                        mStartBtTest = false;
                        mWarnTV.setText(R.string.scan_bt_empty);
                        onStopClick();
                    }
                } else if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                    Log.d(TAG, "already found other bt device");
                    mWarnTV.setText(R.string.scan_bt_success);
                    mAlreadyFoundBtDevice = true;
                    mBtAdapter.cancelDiscovery();
                }
            } else if (mStartMobileTest) {
                if (ConnectivityManager.CONNECTIVITY_ACTION.equals(action)) {
                    if (NetworkUtils.isMobileConnected(RecoveryTest.this)) {
                        mHandler.sendEmptyMessage(MSG_RECOVERY_PING_TEST);
                    }
                }
            }
        }
    }

//===========================for A10 test===========================//	
    /**
     * Reboot into the recovery system with the supplied argument.
     *
     * @param arg to pass to the recovery utility.
     * @throws IOException if something goes wrong.
     */
    private static File RECOVERY_DIR = new File("/cache/recovery");
    private static File COMMAND_FILE = new File(RECOVERY_DIR, "command");

    private static void bootCommand(Context context, String arg) throws IOException {
        RECOVERY_DIR.mkdirs();  // In case we need it
        COMMAND_FILE.delete();  // In case it's not writable

        FileWriter command = new FileWriter(COMMAND_FILE);
        try {
            command.write(arg);
            command.write("\n");
        } finally {
            command.close();
        }

        // Having written the command file, go ahead and reboot
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        pm.reboot("recovery");

        throw new IOException("Reboot failed (no permissions?)");
    }

    public String getRK_StressTest(Context context) {
        String RECOVERY_STATE_FILE_TF = "/mnt/external_sd/Recovery_state";
        StorageManager mStorageManager = (StorageManager) context.getSystemService(StorageManager.class);
        final List<VolumeInfo> volumes = mStorageManager.getVolumes();
        Collections.sort(volumes, VolumeInfo.getDescriptionComparator());
        for (VolumeInfo vol : volumes) {
            if (vol.getType() == VolumeInfo.TYPE_PUBLIC) {
                DiskInfo disk = vol.getDisk();
                if (disk != null) {
                    if (disk.isSd()) {
                        //sdcard dir
                        StorageVolume sv = vol.buildStorageVolume(context, context.getUserId(), false);
                        String sdcard_dir = sv.getPath();
                        RECOVERY_STATE_FILE_TF = new String(sdcard_dir) + "/RK_StressTest.txt";
                    } else if (disk.isUsb()) {
                        //usb dir
                        StorageVolume sv = vol.buildStorageVolume(context, context.getUserId(), false);
                        String usb_dir = sv.getPath();
                        RECOVERY_STATE_FILE_TF = new String(usb_dir) + "/RK_StressTest.txt";
                    }
                }
            }
        }

        Log.d(TAG, "RECOVERY_STATE_FILE_TF:" + RECOVERY_STATE_FILE_TF);
        return RECOVERY_STATE_FILE_TF;
    }

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
                            if (lineTxt.contains("recovery 0")) {
                                stopTest();
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
