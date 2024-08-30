package com.cghs.stresstest.test;

import java.util.Timer;
import java.util.TimerTask;

import com.cghs.stresstest.R;
import com.cghs.stresstest.test.service.CameraTestService;
import com.cghs.stresstest.util.IntentUtils;
import com.cghs.stresstest.util.NativeInputManager;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;

/*
 * Author 	: cghs
 * Data		: 2013-05-08
 * Function : Test Camera (not call CameraLauncher)
 */

public class CameraTest extends Activity implements OnClickListener {
    public static final String TAG = "CameraTest";
    public static final String SERVICE_NAME = "com.cghs.stresstest.test.service.CameraTestService";

    public static final int TEST_OPEN_CLOESE = 0;
    public static final int TEST_TAKE_PHOTO = 1;
    public static final int TEST_SWITCH_MODE = 2;

    public static final int FRONT_CAMERA = 0;
    public static final int BACK_CAMERA = 1;

    public static final int PICTURE_MODE = 0;
    public static final int VIDEO_MODE = 1;

    private final int MSG_START_CAMERA = 0;
    private final int MSG_UPDATE_RESULT = 1;

    private final int REQUEST_CODE_CAMERA = 0;
    private CheckBox mUseSystemCB;
    private TextView mMaxTV;
    private TextView mResultTV;
    private Button mMaxCountBtn;
    private Button mStartBtn;
    private Button mStopBtn;
    private Button mExitBtn;

    private RadioGroup mTestSelectGroup;
    private RadioGroup mCameraSelectGroup;
    private RadioGroup mModeSelectGroup;

    private boolean isTesting = false;
    private boolean isUseSystem = false; // true to call SystemCameraLauncher

    private int mSelectedTest = TEST_OPEN_CLOESE;
    private int mSelectedCamera = BACK_CAMERA;
    private int mSelectedMode = PICTURE_MODE;
    private String mSelectedTestTitle = null;

    private int mMaxCount = 0;
    private int mNowCount = 0;
    private WakeLock mWakeLock;
    private Timer mTimer = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_open_test);
        initRes();
        initDataFromIntent();

        registerReceiver(mReceiver, new IntentFilter(IntentUtils.INTENT_ACTION_STOP_AND_UPDATE_RESULT));

    }

    private void initRes() {
        mStartBtn = (Button) findViewById(R.id.start_btn);
        mStartBtn.setOnClickListener(this);
        mStopBtn = (Button) findViewById(R.id.stop_btn);
        mStopBtn.setOnClickListener(this);
        mExitBtn = (Button) findViewById(R.id.exit_btn);
        mExitBtn.setOnClickListener(this);

        mUseSystemCB = (CheckBox) findViewById(R.id.usesystem_CB);
        mUseSystemCB.setChecked(true);
        mMaxTV = (TextView) findViewById(R.id.maxtime_tv);
        mMaxTV.setText(getString(R.string.max_test_time) + mMaxCount);
        mResultTV = (TextView) findViewById(R.id.result);
        mResultTV.setText(getString(R.string.already_test_time) + mNowCount);

        mMaxCountBtn = (Button) findViewById(R.id.maxtime_btn);
        mMaxCountBtn.setOnClickListener(this);

        mTestSelectGroup = (RadioGroup) findViewById(R.id.testselect);
        mTestSelectGroup.setOnCheckedChangeListener(new TestSelectListener());

        mCameraSelectGroup = (RadioGroup) findViewById(R.id.selectcamera);
        mCameraSelectGroup.setOnCheckedChangeListener(new TestSelectListener());
        mModeSelectGroup = (RadioGroup) findViewById(R.id.selectmode);
        mModeSelectGroup.setOnCheckedChangeListener(new TestSelectListener());

    }

    private void initDataFromIntent() {
        mNowCount = getIntent().getIntExtra(IntentUtils.INTENT_EXTRA_COUNT, 0);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_CAMERA && isTesting) {
            Log.e("cghs", "mNowCount:" + mNowCount + " mMaxCount:" + mMaxCount);
            if (mMaxCount != 0 && mNowCount >= mMaxCount) {
                isTesting = false;
                mNowCount = 0;
            } else {
                mHandler.sendEmptyMessageDelayed(MSG_START_CAMERA, 10000);
            }

        }

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.start_btn:
                holdWakeLock();
                isTesting = true;
                startTest();
                break;
            case R.id.stop_btn:
                isTesting = false;
                mNowCount = 0;
                stopTest();
                releaseWakeLock();
                break;
            case R.id.exit_btn:
                finish();
                break;
            case R.id.maxtime_btn:
                onSetClick();
                break;
            default:
                break;
        }
    }

    private void testUserCamera() {
        mNowCount = mNowCount + 1;
        Intent intent = new Intent(this, CameraAutoTestActivity.class);
        startActivityForResult(intent, REQUEST_CODE_CAMERA);
    }

    private void onSetClick() {
        final EditText editText = new EditText(this);
        editText.setInputType(InputType.TYPE_CLASS_NUMBER);
        new AlertDialog.Builder(this)
                .setTitle(R.string.dialog_title)
                .setView(editText)
                .setPositiveButton(R.string.dialog_ok,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog,
                                                int which) {
                                if (!editText.getText().toString().trim()
                                        .equals("")) {
                                    mMaxCount = Integer.valueOf(editText
                                            .getText().toString());
                                    mMaxTV.setText(getString(R.string.max_test_time)
                                            + mMaxCount);
                                }
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

    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_START_CAMERA:
                    if (!isTesting)
                        return;
                    mNowCount++;
                    Intent intent = new Intent(CameraTest.this,
                            CameraAutoTestActivity.class);
                    startActivityForResult(intent, REQUEST_CODE_CAMERA);
                    break;
                case MSG_UPDATE_RESULT:
                    mResultTV.setText(getString(R.string.already_test_time)
                            + mNowCount);
                    mResultTV.setVisibility(View.VISIBLE);
                    break;
                default:
                    break;
            }
        }

        ;
    };

    // Test System Camera Launcher
    private void TestSystemCamera() {
        Intent intent = new Intent(this, CameraTestService.class);
        if (isServiceRunning()) {
            stopService(intent);
        }
        if (mMaxCount != 0)
            intent.putExtra("max", mMaxCount);
        intent.putExtra(IntentUtils.INTENT_EXTRA_CAMERA_TEST, mSelectedTest);
        intent.putExtra(IntentUtils.INTENT_EXTRA_CAMERA_TEST_TITLE,
                mSelectedTestTitle);
        startService(intent);
    }

    /*
     * private void startTest() { mTimer = new Timer(); mTimer.schedule(new
     * TimerTask() { public void run() { if (mMaxCount != 0 && mNowCount >=
     * mMaxCount || !isTesting) { return; } else { mNowCount++; turnCameraOn();
     * }
     *
     * } }, 500L, 6000L); }
     */

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

    // Test System Camera Launcher end --- ---

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mReceiver);
        releaseWakeLock();
    }

    private void holdWakeLock() {
        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        mWakeLock = powerManager.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK,
                getClass().getCanonicalName());
        mWakeLock.acquire();
    }

    private void releaseWakeLock() {
        if (mWakeLock != null && mWakeLock.isHeld()) {
            mWakeLock.release();
        }
    }

    private class TestSelectListener implements OnCheckedChangeListener {

        public void onCheckedChanged(RadioGroup group, int checkedId) {
            switch (checkedId) {
                case R.id.opentest:
                    mUseSystemCB.setVisibility(View.VISIBLE);
                    mModeSelectGroup.setVisibility(View.GONE);
                    mCameraSelectGroup.setVisibility(View.GONE);
                    mSelectedTest = TEST_OPEN_CLOESE;
                    mSelectedTestTitle = ((RadioButton) findViewById(checkedId))
                            .getText().toString();
                    break;
                case R.id.takephototest:
                    mUseSystemCB.setVisibility(View.GONE);
                    mModeSelectGroup.setVisibility(View.VISIBLE);
                    mCameraSelectGroup.setVisibility(View.VISIBLE);
                    mSelectedTest = TEST_TAKE_PHOTO;
                    mSelectedTestTitle = ((RadioButton) findViewById(checkedId))
                            .getText().toString();
                    break;
                case R.id.switchmodetest:
                    mUseSystemCB.setVisibility(View.GONE);
                    mModeSelectGroup.setVisibility(View.GONE);
                    mCameraSelectGroup.setVisibility(View.VISIBLE);
                    mSelectedTest = TEST_SWITCH_MODE;
                    mSelectedTestTitle = ((RadioButton) findViewById(checkedId))
                            .getText().toString();
                    break;

                case R.id.front_camera:
                    mSelectedCamera = FRONT_CAMERA;
                    break;
                case R.id.back_camera:
                    mSelectedCamera = BACK_CAMERA;
                    break;

                case R.id.picture_mode:
                    mSelectedMode = PICTURE_MODE;
                    break;
                case R.id.video_mode:
                    mSelectedMode = VIDEO_MODE;
                    break;

                default:
                    break;
            }
        }
    }

    private void startTest() {
        mResultTV.setVisibility(View.INVISIBLE);

        switch (mSelectedTest) {
            case TEST_OPEN_CLOESE:
                testOpenclose();
                break;
            case TEST_TAKE_PHOTO:
                testTakePhoto();
                break;
            case TEST_SWITCH_MODE:
                testSwitchMode();
                break;
            default:
                break;
        }
    }

    private void stopTest() {
        Log.e(TAG, "stopTest :" + mSelectedTest);
        switch (mSelectedTest) {
            case TEST_OPEN_CLOESE:
                stopTestOpenclose();
                break;
            case TEST_TAKE_PHOTO:
                stopTestTakePhoto();
                break;
            case TEST_SWITCH_MODE:
                stopTestSwitchMode();
                break;
            default:
                break;
        }
    }

    private void testOpenclose() {
        isUseSystem = mUseSystemCB.isChecked();
        if (isUseSystem) {
            TestSystemCamera();
        } else {
            testUserCamera();
        }
    }

    private void stopTestOpenclose() {
        if (!isUseSystem) {
            mHandler.removeMessages(MSG_START_CAMERA);
        }

        Intent intent = new Intent(this, CameraTestService.class);
        stopService(intent);

        if (mTimer != null)
            mTimer.cancel();
    }

    private void testTakePhoto() {
        Intent intent = new Intent(this, CameraTestService.class);

        if (isServiceRunning()) {
            stopService(intent);
        }

        if (mMaxCount != 0)
            intent.putExtra("max", mMaxCount);
        intent.putExtra(IntentUtils.INTENT_EXTRA_CAMERA_TEST, mSelectedTest);
        intent.putExtra(IntentUtils.INTENT_EXTRA_CAMERA_TEST_TITLE,
                mSelectedTestTitle);
        intent.putExtra(IntentUtils.INTENT_EXTRA_CAMERA_ID, mSelectedCamera);
        intent.putExtra(IntentUtils.INTENT_EXTRA_CAMERA_MODE, mSelectedMode);
        startService(intent);
    }

    private void stopTestTakePhoto() {
        Intent intent = new Intent(this, CameraTestService.class);
        stopService(intent);
    }

    private void testSwitchMode() {
        Intent intent = new Intent(this, CameraTestService.class);
        if (isServiceRunning()) {
            stopService(intent);
        }

        if (mMaxCount != 0)
            intent.putExtra("max", mMaxCount);
        intent.putExtra(IntentUtils.INTENT_EXTRA_CAMERA_TEST, mSelectedTest);
        intent.putExtra(IntentUtils.INTENT_EXTRA_CAMERA_TEST_TITLE,
                mSelectedTestTitle);
        intent.putExtra(IntentUtils.INTENT_EXTRA_CAMERA_ID, mSelectedCamera);
        intent.putExtra(IntentUtils.INTENT_EXTRA_CAMERA_MODE, mSelectedMode);

        startService(intent);
    }

    private void stopTestSwitchMode() {
        Intent intent = new Intent(this, CameraTestService.class);
        stopService(intent);
    }

    private boolean isServiceRunning() {
        ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        for (RunningServiceInfo service : manager
                .getRunningServices(Integer.MAX_VALUE)) {
            if (SERVICE_NAME.equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(
                    IntentUtils.INTENT_ACTION_STOP_AND_UPDATE_RESULT)) {
                isTesting = false;
                mNowCount = intent.getIntExtra(IntentUtils.INTENT_EXTRA_COUNT,
                        0);
                mHandler.sendEmptyMessage(MSG_UPDATE_RESULT);
            }
        }
    };

}
