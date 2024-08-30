package com.cghs.stresstest.test;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.text.InputType;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.cghs.stresstest.R;
import com.cghs.stresstest.util.CmdUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class DvfsTest extends Activity implements OnClickListener, CmdUtils.CommandResponseListener {
    private final static String LOG_TAG = "DvfsTest";

    private final static int MSG_START = 0;
    private final static int MSG_COUNTDOWN = 1;
    private final static int MSG_STARTCOUNT = 2;
    private static final int MSG_UPDATE_CONTENT = 3;

    private final String CMD_PATH = "/data/scan_dvfs.sh";
    private final static int DELAY_TIME = 5;// x1000ms
    private final int TEST_OFF = 0;
    private final int TEST_ON = 1;

    private SharedPreferences mSharedPreferences;

    private TextView mCountTV;
    private TextView mCountdownTV;
    private TextView mMaxTV;
    private TextView mWarnTV;
    private Button mStartButton;
    private Button mStopButton;
    private Button mExitBtn;
    private Button mSettingButton;

    private WakeLock mWakeLock;
    private static int mState;
    private int mCount;
    private int mCountDownTime;
    private int mMaxTimes; // max times to test
    private int mDelayTime; // delay time to start test
    private static final int DEFAULT_DELAYED_TIME = 5;
    private boolean mIsRunning;
    private StringBuilder mDetailContent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dvfs_test);
        // get the flag and count.
        mSharedPreferences = getSharedPreferences("state", 0);
        mState = mSharedPreferences.getInt("dvfs_flag", 0);
        mCount = mSharedPreferences.getInt("dvfs_count", 0);
        mMaxTimes = mSharedPreferences.getInt("dvfs_max", 0);
        mDelayTime = mSharedPreferences.getInt("dvfs_delay", DEFAULT_DELAYED_TIME);

        // init resource
        initRes();
        mWakeLock = ((PowerManager) getSystemService(POWER_SERVICE)).newWakeLock(PowerManager.FULL_WAKE_LOCK, "dvfs test");
        mWakeLock.acquire();
        if (mState == TEST_ON) {
            if (mMaxTimes != 0 && mMaxTimes <= mCount) {
                mState = TEST_OFF;
                saveSharedPreferences(mState, 0);
                saveMaxTimes(0);
                updateBtnState();
                mCountTV.setText(mCountTV.getText() + " TEST FINISH!");
                return;
            }
            mCountDownTime = DELAY_TIME;//DELAY_TIME / 1000;
            mHandler.sendEmptyMessage(MSG_STARTCOUNT);
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
                    saveDelayTimes(mDelayTime);
                    startDvfsTest();
                }
            }
        }
    }

    private void initRes() {
        mCountTV = (TextView) findViewById(R.id.count_tv);
        mCountTV.setText(getString(R.string.already_test_time) + mCount);
        mMaxTV = (TextView) findViewById(R.id.maxtime_tv);
        mMaxTV.setText(getString(R.string.max_test_time) + mMaxTimes);
        mWarnTV = (TextView) findViewById(R.id.warn_tv);
        mWarnTV.setMovementMethod(ScrollingMovementMethod.getInstance());

        mStartButton = (Button) findViewById(R.id.start_btn);
        mStartButton.setOnClickListener(this);

        mStopButton = (Button) findViewById(R.id.stop_btn);
        mStopButton.setOnClickListener(this);

        mExitBtn = (Button) findViewById(R.id.exit_btn);
        mExitBtn.setOnClickListener(this);

        mSettingButton = (Button) findViewById(R.id.setting_btn);
        mSettingButton.setOnClickListener(this);

        updateBtnState();

        mCountdownTV = (TextView) findViewById(R.id.countdown_tv);
    }

    private void doDvfsTest() {
        // save state
        saveSharedPreferences(mState, mCount + 1);
        File file = new File(CMD_PATH);
        if (null != file && file.exists()) {
            final String cmd = "sh ." + CMD_PATH;
            mIsRunning = true;
            mDetailContent = new StringBuilder(cmd + "\n");
            mWarnTV.setText(mDetailContent);
            new Thread() {
                @Override
                public void run() {
                    CmdUtils.execCmd(cmd, DvfsTest.this);
                }
            }.start();
        } else {
            mWarnTV.setText("unfound " + CMD_PATH);
            onStopClick();
        }
    }

    private void saveSharedPreferences(int flag, int count) {
        SharedPreferences.Editor edit = mSharedPreferences.edit();
        edit.putInt("dvfs_flag", flag);
        edit.putInt("dvfs_count", count);
        edit.commit();
    }

    private void saveMaxTimes(int max) {
        SharedPreferences.Editor edit = mSharedPreferences.edit();
        edit.putInt("dvfs_max", max);
        edit.commit();
    }

    private void saveDelayTimes(int time) {
        SharedPreferences.Editor edit = mSharedPreferences.edit();
        edit.putInt("dvfs_delay", time);
        edit.commit();
        Toast.makeText(this, "Set delay time:" + time + "s", Toast.LENGTH_LONG).show();
    }

    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_START:
                    Log.d(LOG_TAG, "===MSG_START mState = " + mState);
                    if (mState == TEST_ON) {
                        doDvfsTest();
                    }
                    break;
                case MSG_COUNTDOWN:
                    if (mState == TEST_OFF)
                        return;
                    if (mCountDownTime != 0) {
                        mCountdownTV.setText(getString(R.string.start_test_countdown)
                                + mCountDownTime);
                        mCountdownTV.setVisibility(View.VISIBLE);
                        mCountDownTime--;
                        sendEmptyMessageDelayed(MSG_COUNTDOWN, 1000);
                    } else {
                        mCountdownTV.setVisibility(View.INVISIBLE);
                        sendEmptyMessage(MSG_START);
                    }
                    break;
                case MSG_STARTCOUNT:
                    sendEmptyMessage(MSG_COUNTDOWN);
                    break;
                case MSG_UPDATE_CONTENT:
                    mWarnTV.setText(mDetailContent);
                    int offset = mWarnTV.getLineCount() * mWarnTV.getLineHeight()
                            - mWarnTV.getMeasuredHeight();
                    if (offset > 0) {
                        mWarnTV.scrollTo(0, offset);
                    } else {
                        mWarnTV.scrollTo(0, 0);
                    }
                    break;
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
            default:
                break;
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //       stopTest();
        mIsRunning = false;
        if (mWakeLock != null && mWakeLock.isHeld())
            mWakeLock.release();
    }

    private void onStartClick() {
        String MessageString = getString(R.string.start_confirm_dialog_msg, mDelayTime);
        new AlertDialog.Builder(DvfsTest.this)
                .setTitle(R.string.dvfs_title)
                .setMessage(MessageString)
                .setPositiveButton(R.string.dialog_ok,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog,
                                                int which) {
                                startDvfsTest();
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

    private void startDvfsTest() {
        mState = TEST_ON;
        mCount = 0;
        mCountDownTime = mDelayTime;//DELAY_TIME / 1000; // ms->s
        updateBtnState();
        mHandler.sendEmptyMessage(MSG_STARTCOUNT);
    }

    private void onStopClick() {
        mState = TEST_OFF;
        mHandler.removeMessages(MSG_START);
        mHandler.removeMessages(MSG_COUNTDOWN);
        mHandler.removeMessages(MSG_STARTCOUNT);
        mCountdownTV.setVisibility(View.INVISIBLE);
        updateBtnState();
        mStartButton.setEnabled(false);
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
                            mMaxTV.setText(getString(R.string.max_test_time) + mMaxTimes);
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

        mMaxTV.setText(getString(R.string.max_test_time)
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
                                Toast.makeText(DvfsTest.this, strMsg, Toast.LENGTH_SHORT).show();
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

    private void updateBtnState() {
        mStartButton.setEnabled(mState == TEST_OFF);
        mSettingButton.setEnabled(mState == TEST_OFF);
        mStopButton.setEnabled(mState == TEST_ON);
    }

    @Override
    public void onResponse(InputStream resIn, InputStream errIn) {
        BufferedReader bufferedReader = null;
        InputStreamReader isr = null;
        try {
            isr = new InputStreamReader(resIn);
            bufferedReader = new BufferedReader(isr);
            String line = null;
            int logCnt = 0;
            while (mIsRunning && (line = bufferedReader.readLine()) != null) {
                logCnt++;
                Log.d(LOG_TAG, "line= " + line);
                if (logCnt % 15 == 0) {
                    int start = mDetailContent.indexOf("\n");
                    mDetailContent.delete(0, start + 1);
                }
                mDetailContent.append(line + "\n");
                mHandler.sendEmptyMessage(MSG_UPDATE_CONTENT);

//                //bin文件校验失败,直接返回失败结果
//                if ((line.contains("FAIL") && isRunning)
//                    /*|| "true".equals(SystemProperties.get("persist.sys.waha"))*/) {
//                    //mHandler.removeMessages(MSG_FAIL_TIMEOUT);
//                    isRunning = false;
//                    mTestFail = true;
//                    mStatusSuffix = "失败";
//                    mCountDown = 0;
//                    mHandler.removeMessages(MSG_COUNT_DOWN);
//                    mHandler.sendEmptyMessage(MSG_UPDATE_RESULT_BIN);
//                    LogUtil.e(GPUCheckActivity.this, mStatusPrefix + "" + mStatusSuffix);
//                    return;
//                }
//
//                if (line.contains("SUCCESS")) {
//                    //mHandler.removeMessages(MSG_FAIL_TIMEOUT);
//                    break;
//                }
//                +            //执行到这说明本次测试结束
//                        +            if (isRunning) {
//                    +                mStatusSuffix = "成功";
//                    +                mCountDown = 0;
//                    +                mHandler.removeMessages(MSG_COUNT_DOWN);
//                    +                mHandler.sendEmptyMessage(MSG_UPDATE_RESULT_BIN);
//                    +                LogUtil.d(GPUCheckActivity.this, mStatusPrefix + "" + mStatusSuffix);
//                    +            }
                //mHandler.sendEmptyMessage(MSG_UPDATE_DETAIL);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            Log.d(LOG_TAG, "onResponse finish");
            if (bufferedReader != null) {
                try {
                    bufferedReader.close();
                } catch (IOException e) {
                }
            }
            if (null != isr) {
                try {
                    isr.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
