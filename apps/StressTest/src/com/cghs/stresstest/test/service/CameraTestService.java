package com.cghs.stresstest.test.service;

import java.util.Timer;
import java.util.TimerTask;

import com.cghs.stresstest.test.CameraAutoTestActivity;
import com.cghs.stresstest.test.CameraTest;
import com.cghs.stresstest.util.IntentUtils;
import com.cghs.stresstest.util.NativeInputManager;

import com.cghs.stresstest.R;
import android.R.integer;
import android.app.ActivityManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.provider.MediaStore;
import android.text.StaticLayout;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

public class CameraTestService extends Service {
	public static final String TAG = "CameraTestService";

	private static final int VIEW_WIDTH = 250;
	private static final int VIEW_HEIGHT = 130;
	private long mVideoLimit = 2 * 60 * 1000;// 2mins
	float[] modepositions = {1010.0f, 491.0f};
	float[] modeVideo = {891.0f, 479.0f};
	float[] modePicture = {1009.0f, 480.0f};
	

	private int mTestMode = 0;
	private int mMaxTestCount = 0;
	private int mCurrentCount = 0;
	private boolean isTesting = false;
	private String mTestTitle = null;
	private int mCameraID = -1;
	private int mCameraMode = -1;

	private int mCurrentX = 0;
	private int mCurrentY = 0;
	private int mStartX = 0;
	private int mStartY = 0;

	private WindowManager mWm = null;
	private WindowManager.LayoutParams mWmParams = null;

	private View mView = null;
	private TextView mTitleView = null;
	private TextView mMaxView = null;
	private TextView mTestView = null;
	private Button mStopBtn = null;

	private Timer mTimer = null;
	private int[]mColor = {Color.RED,Color.WHITE,Color.YELLOW};

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if (intent == null) {
			Log.e(TAG, "onStartCommand(): intent = null");
			return -1;
		}
		mTestMode = intent
				.getIntExtra(IntentUtils.INTENT_EXTRA_CAMERA_TEST, -1);
		mMaxTestCount = intent.getIntExtra(IntentUtils.INTENT_EXTRA_MAX, 0);
		mTestTitle = intent
				.getStringExtra(IntentUtils.INTENT_EXTRA_CAMERA_TEST_TITLE);
		mTestTitle = mTestTitle == null ? getString(R.string.camera_title)
				: mTestTitle;

		mCameraID = intent.getIntExtra(IntentUtils.INTENT_EXTRA_CAMERA_ID, -1);
		mCameraMode = intent.getIntExtra(IntentUtils.INTENT_EXTRA_CAMERA_MODE,
				-1);

		createFloatingView(LayoutInflater.from(this));

		startTest();

		return super.onStartCommand(intent, flags, startId);
	}

	private void createFloatingView(LayoutInflater inflater) {
		mWm = (WindowManager) getApplicationContext()
				.getSystemService("window");

		View view = inflater.inflate(R.layout.camera_view, null);
		mView = view.findViewById(R.id.root_view);
		mView.setOnTouchListener(mTouchListener);
		initWmParams();
		mView.setBackgroundColor(Color.RED);
		mWm.addView(mView, mWmParams);
		setupViews();
	}

	private void initWmParams() {
		if (mWmParams == null) {
			mWmParams = new WindowManager.LayoutParams();
			mWmParams.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT
					| WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY;// 2002|WindowManager.LayoutParams.TYPE_SYSTEM_ALERT
			// ;
			//mWmParams.format = PixelFormat.TRANSLUCENT;
			mWmParams.flags |= 8;
			//mWmParams.flags |= WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED;
			mWmParams.gravity = Gravity.LEFT | Gravity.TOP;

			//int mWidth = VIEW_WIDTH;
			//int mHeight = VIEW_HEIGHT;
			int mWidth = Integer.valueOf(getResources().getString(R.string.camera_width));
			int mHeight = Integer.valueOf(getResources().getString(R.string.camera_high));
			// DisplayMetrics d = getResources().getDisplayMetrics();
			// wmParams.x = (d.widthPixels - mWidth) / 2;
			// wmParams.y = (d.heightPixels - mHeight) / 2;
			mWmParams.x = 0;
			mWmParams.y = 0;

			mWmParams.width = mWidth;
			mWmParams.height = mHeight;
		}
	}

	private void setupViews() {
		mTitleView = (TextView) mView.findViewById(R.id.title);
		mTitleView.setText(mTestTitle);
		mMaxView = (TextView) mView.findViewById(R.id.max_count);
		mMaxView.setText(getString(R.string.max_test_time) + mMaxTestCount);
		mTestView = (TextView) mView.findViewById(R.id.test_count);
		mTestView
				.setText(getString(R.string.already_test_time) + mCurrentCount);
		mStopBtn = (Button) mView.findViewById(R.id.stop);
		mStopBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				stopTest();
				stopSelf();
//				mWm.removeView(mView);
/*				Intent intent = new Intent(CameraTestService.this,
						CameraTest.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				intent.putExtra("count", mCurrentCount);
				startActivity(intent);*/
				Intent intent = new Intent(IntentUtils.INTENT_ACTION_STOP_AND_UPDATE_RESULT);
				intent.putExtra(IntentUtils.INTENT_EXTRA_COUNT, mCurrentCount);
				sendBroadcast(intent);
				String name = getRunningActivity();
				if (name.equals("com.android.camera.CameraActivity") || 
						name.equals("com.android.camera.VideoCamera"))
					NativeInputManager.sendKeyDownUpSync(4);
			}
		});
	}

	private void refreshView() {
		mTestView
				.setText(getString(R.string.already_test_time) + mCurrentCount);
		mView.setBackgroundColor(mColor[mCurrentCount%mColor.length]);
		mView.invalidate();
		
		if(mView!=null && mView.getParent()!=null){
			Log.d(TAG,"--------------->invalidate-------already_test_time:"+mCurrentCount);
			mWm.updateViewLayout(mView, mWmParams);
		}
		//mTestView.postInvalidate();
	}

	private boolean isDoubleClick = false;
	OnTouchListener mTouchListener = new OnTouchListener() {
		@Override
		public boolean onTouch(View v, MotionEvent event) {
			switch (event.getAction() & MotionEvent.ACTION_MASK) {
			case MotionEvent.ACTION_DOWN:
				isDoubleClick = false;
				break;
			case MotionEvent.ACTION_POINTER_DOWN:
				isDoubleClick = true;
				break;
			case MotionEvent.ACTION_MOVE:
				break;
			case MotionEvent.ACTION_POINTER_UP:
				break;
			case MotionEvent.ACTION_UP:
				break;
			}
			if (!isDoubleClick) {
				tarckFlinger(event);
				return true;
			}
			return true;
		}
	};

	private boolean tarckFlinger(MotionEvent event) {
		/*
		 * wmParams.width+=50; wmParams.height+=50;
		 * mVideoView.setVideoMeasure(wmParams.width, wmParams.height);
		 * wm.updateViewLayout(mRootView, wmParams);
		 */
		mCurrentX = (int) event.getRawX();
		mCurrentY = (int) event.getRawY();
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			mStartX = (int) event.getX();
			mStartY = (int) event.getY();
			break;
		case MotionEvent.ACTION_MOVE:
			updateWindowParams();
			break;
		case MotionEvent.ACTION_UP:
			mStartX = 0;
			mStartY = 0;
			break;
		}
		return true;
	}

	private void updateWindowParams() {
		mWmParams.x = mCurrentX - mStartX;
		mWmParams.y = mCurrentY - mStartY;
		mWm.updateViewLayout(mView, mWmParams);
	}

	private void startTest() {
		Log.e(TAG, "Camera test startTest . testmode:" + mTestMode);
		switch (mTestMode) {
		case CameraTest.TEST_OPEN_CLOESE:
			startTestOpenClose();
			break;
		case CameraTest.TEST_TAKE_PHOTO:
			startTestTakePhoto();
			break;
		case CameraTest.TEST_SWITCH_MODE:
			startTestSwitchMode();
			break;
		default:
			Log.e(TAG, "Camera test mode err: testmode:" + mTestMode);
			break;
		}
	}

	private void stopTest() {
		if (mTimer != null) {
			mTimer.cancel();
			mTimer = null;
		}
	}

	private void startTestOpenClose() {
		isTesting = true;
		mTimer = new Timer();
		mTimer.schedule(new TimerTask() {
			public void run() {
				if (mMaxTestCount != 0 && mCurrentCount >= mMaxTestCount
						|| !isTesting) {
					stopSelf();
					return;
				} else {
					mCurrentCount++;
					mHandler.removeMessages(REFRESH_VIEW1);
					Message message = new Message();
					message.what = REFRESH_VIEW1;
					mHandler.sendMessage(message);
					turnCameraOn();
					}
			}
		}, 500L, 12000L);
	}

	private void startTestTakePhoto() {
		Intent intent = new Intent();
		if (mCameraMode == CameraTest.PICTURE_MODE) {
			intent.setAction(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA);
//			intent.setClassName("com.android.gallery3d", "com.android.camera.CameraActivity");
		} else if (mCameraMode == CameraTest.VIDEO_MODE) {
//			intent.setAction(MediaStore.ACTION_VIDEO_CAPTURE);
			intent.setAction(MediaStore.INTENT_ACTION_VIDEO_CAMERA);	
			
		}

		if (mCameraID == CameraTest.FRONT_CAMERA) {
			intent.putExtra("android.intent.extras.CAMERA_FACING",
					android.hardware.Camera.CameraInfo.CAMERA_FACING_FRONT);
		} else if (mCameraID == CameraTest.BACK_CAMERA) {
			intent.putExtra("android.intent.extras.CAMERA_FACING",
					android.hardware.Camera.CameraInfo.CAMERA_FACING_BACK);
		}

		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
				| Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(intent);

		if (mTimer == null) {
			mTimer = new Timer();
		}

		if (mCameraMode == CameraTest.VIDEO_MODE) {
			mTimer.schedule(new TimerTask() {
				@Override
				public void run() {
					if ((mMaxTestCount > 0 && mMaxTestCount > mCurrentCount)
							|| mMaxTestCount == 0) {
						if(isTesting()){
						mCurrentCount++;
						mHandler.removeMessages(REFRESH_VIEW1);
						Message message = new Message();
						message.what = REFRESH_VIEW1;
						mHandler.sendMessage(message);
						touchActionButton();
						stopVideoDelay();
						}else{
							stopTest();
						}
					} else {
						stopTest();
						NativeInputManager.sendKeyDownUpSync(4);
						stopSelf();
					}
				}
			}, 5000, mVideoLimit + 7 * 1000);// repeat
		} else {
			mTimer.schedule(new TimerTask() {
				@Override
				public void run() {
					if ((mMaxTestCount > 0 && mMaxTestCount > mCurrentCount)
							|| mMaxTestCount == 0) {
						if(isTesting()){
						mCurrentCount++;
						mHandler.removeMessages(REFRESH_VIEW1);
						Message message = new Message();
						message.what = REFRESH_VIEW1;
						mHandler.sendMessage(message);
						touchActionButton();
						}else{
							stopTest();
						}
					} else {
						stopTest();
						NativeInputManager.sendKeyDownUpSync(4);
						stopSelf();
					}
				}
			}, 5000L, 10000L);// repeat
		}

	}

	private void startTestSwitchMode() {
		Intent intent = new Intent();
//		intent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
		intent.setClassName("com.android.camera2", "com.android.camera.CameraLauncher");
		if (mCameraID == CameraTest.FRONT_CAMERA) {
			intent.putExtra("android.intent.extras.CAMERA_FACING",
					android.hardware.Camera.CameraInfo.CAMERA_FACING_FRONT);
		} else {
			intent.putExtra("android.intent.extras.CAMERA_FACING",
					android.hardware.Camera.CameraInfo.CAMERA_FACING_BACK);
		}
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
				| Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(intent);

		if (mTimer == null)
			mTimer = new Timer();
		mTimer.schedule(new TimerTask() {

			@Override
			public void run() {
				if ((mMaxTestCount > 0 && mMaxTestCount > mCurrentCount)
						|| mMaxTestCount == 0) {
					if(isTesting()){
					mCurrentCount++;
					mHandler.removeMessages(REFRESH_VIEW1);
					Message message = new Message();
					message.what = REFRESH_VIEW1;
					mHandler.sendMessage(message);
					switchMode();
					}else{
						stopTest();
					}
				} else {
					stopTest();
					NativeInputManager.sendKeyDownUpSync(4);
					stopSelf();
				}
			}
		}, 3000L, 5000L);
	}

	// Test Open/Close Camera

	private void turnCameraOff() {
		NativeInputManager.sendKeyDownUpSync(4);
	}

	private void turnCameraOn() {
		Intent localIntent = new Intent();
		localIntent.setAction("android.media.action.IMAGE_CAPTURE");
		localIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
				| Intent.FLAG_ACTIVITY_CLEAR_TOP);
		this.startActivity(localIntent);
		
		mTimer.schedule(new TimerTask() {
			public void run() {
				turnCameraOff();
			}
		}, 10000L);
	}

	// Test Open/Close Camera end --- ---

	// Test take photo
	private void touchActionButton() {
		float[] postions = new float[2];
		// postions[0] = 1792f; //s10 JB
		// postions[1] = 560f; // s10 JB
		postions[0] = Float.parseFloat(getString(R.string.touch_x)); // lite2
		postions[1] = Float.parseFloat(getString(R.string.touch_y)); // lite2
		NativeInputManager.sendTouchEventSync(postions[0], postions[1]);
	}

	private void stopVideoDelay() {
		mTimer.schedule(new TimerTask() {

			@Override
			public void run() {
				touchActionButton();
			}
		}, mVideoLimit);// no repeat
	}

	// Test take photo end --- ---

	private void switchMode() {
		//just for A10
		modepositions[0] = Float.parseFloat(getString(R.string.modepositions_x));
		modepositions[1] = Float.parseFloat(getString(R.string.modepositions_y));
		
		modeVideo[0] = Float.parseFloat(getString(R.string.modeVideo_x));
		modeVideo[1] = Float.parseFloat(getString(R.string.modeVideo_y));
		
		modePicture[0] = Float.parseFloat(getString(R.string.modePicture_x));
		modePicture[1] = Float.parseFloat(getString(R.string.modePicture_y));
		try {
			NativeInputManager.sendTouchEventSync(modepositions[0], modepositions[1]);
			if (mCurrentCount%2 == 1) {
				Thread.sleep(1500);
				NativeInputManager.sendTouchEventSync(modeVideo[0], modeVideo[1]);
			} else {
				Thread.sleep(1500);
				NativeInputManager.sendTouchEventSync(modePicture[0], modePicture[1]);
			}
			
		} catch (Exception e) {

		}
	}

	@Override
	public void onDestroy() {
		Log.e(TAG, "onDestroy");
		super.onDestroy();
		if (mTimer != null) {
			mTimer.cancel();
		}
		Intent intent = new Intent(IntentUtils.INTENT_ACTION_STOP_AND_UPDATE_RESULT);
		intent.putExtra(IntentUtils.INTENT_EXTRA_COUNT, mCurrentCount);
		sendBroadcast(intent);
		mWm.removeView(mView);

	}

	private static final int REFRESH_VIEW1 = 0;
	private Handler mHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case REFRESH_VIEW1:
				Log.d(TAG,"-------->REFRESH_VIEW-------------");
				refreshView();
				break;

			default:
				break;
			}
		};
	};
	
	private String getRunningActivity() {
		ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
		String name = manager.getRunningTasks(2).get(0).topActivity.getClassName();
		Log.e(TAG, "RUNNING: "+name);
		return name;
	}

	private Boolean isTesting(){
		String name = getRunningActivity();
		if (name.equals("com.android.camera.CameraActivity") || 
				name.equals("com.android.camera.VideoCamera") || name.equals("com.android.camera.CameraLauncher")){
		    return true;
		}else
			return false;
	}
}
