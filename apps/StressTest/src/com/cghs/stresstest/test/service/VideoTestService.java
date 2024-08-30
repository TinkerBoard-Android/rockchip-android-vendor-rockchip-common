package com.cghs.stresstest.test.service;

import java.util.Timer;
import java.util.TimerTask;

import com.cghs.stresstest.test.VideoTest;
import com.cghs.stresstest.util.IntentUtils;
import com.cghs.stresstest.util.NativeInputManager;

import com.cghs.stresstest.R;
import android.R.integer;
import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.Notification;
import android.app.PendingIntent;
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

public class VideoTestService extends Service {
	public static final String TAG = "VideoTestService";

	private static final int VIEW_WIDTH = 250;
	private static final int VIEW_HEIGHT = 130;
	
	private int mTestMode = 0;
	private int mMaxTestCount = 0;
	private int mCurrentCount = 0;
	private String mTestTitle = null;

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
	private Timer mTimertouch = null;
	PendingIntent pintent;
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
		/*Notification notification = new Notification(R.drawable.ic_launcher,
                "video test service is running",
                System.currentTimeMillis());
        pintent=PendingIntent.getService(this, 0, intent, 0);
        notification.setLatestEventInfo(this, "Video Test Service",
                "video test service is running！", pintent);
            
        //让该service前台运行，避免手机休眠时系统自动杀掉该服务
        //如果 id 为 0 ，那么状态栏的 notification 将不会显示。
        startForeground(startId, notification);*/
		mTestMode = intent
				.getIntExtra(IntentUtils.INTENT_EXTRA_VIDEO_TEST, -1);
		mMaxTestCount = intent.getIntExtra(IntentUtils.INTENT_EXTRA_MAX, 0);
		mTestTitle = intent
				.getStringExtra(IntentUtils.INTENT_EXTRA_VIDEO_TEST_TITLE);
		mTestTitle = mTestTitle == null ? getString(R.string.video_title)
				: mTestTitle;

		createFloatingView(LayoutInflater.from(this));

		startTest();

		return super.onStartCommand(intent, flags, startId);
	}

	private void createFloatingView(LayoutInflater inflater) {
		mWm = (WindowManager) getApplicationContext()
				.getSystemService("window");

		View view = inflater.inflate(R.layout.video_view, null);
		mView = view.findViewById(R.id.root1_view);
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
			int mWidth = Integer.valueOf(getResources().getString(R.string.video_width));
			int mHeight = Integer.valueOf(getResources().getString(R.string.video_high));
			//int mWidth = VIEW_WIDTH;
			//int mHeight = VIEW_HEIGHT;
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
		mMaxView = (TextView) mView.findViewById(R.id.max_time);
		mMaxView.setText(getString(R.string.max_time) + mMaxTestCount);
		mTestView = (TextView) mView.findViewById(R.id.test_count);
		mTestView
				.setText(getString(R.string.already_test_time) + mCurrentCount);
		mStopBtn = (Button) mView.findViewById(R.id.stop);
		mStopBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				stopTest();
				stopSelf();
				//mWm.removeView(mView);
				Intent intent_stop = new Intent(IntentUtils.INTENT_ACTION_STOP_AND_UPDATE_RESULT);
				intent_stop.putExtra(IntentUtils.INTENT_EXTRA_TIME, mCurrentCount);
				sendBroadcast(intent_stop);
				Intent intent = new Intent(VideoTestService.this,
						VideoTest.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				intent.putExtra("time", mCurrentCount);
				startActivity(intent);

			}
		});
	}

	private void refreshView() {
		//String timedisplay = String.valueOf(mCurrentCount - 1);
		mTestView
				.setText(getString(R.string.already_test_time) + mCurrentCount +"H");
		mView.setBackgroundColor(mColor[mCurrentCount%mColor.length]);
		mView.invalidate();
		
		if(mView!=null && mView.getParent()!=null){
			Log.d(TAG,"--------------->invalidate-------already_test_time:"+mCurrentCount);
			mWm.updateViewLayout(mView, mWmParams);
		}
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
		Log.e(TAG, "Video test startTest . testnow" );
		switch (mTestMode) {
		case VideoTest.TEST_SYSTEM_VIDEO:
			startTestVideo();
			break;
		default:
			Log.e(TAG, "Video test mode err!!!");
			break;
		}
	}

	private void stopTest() {
		if (mTimer != null) {
			mTimer.cancel();
			mTimer = null;
		}
		if (mTimertouch != null) {
			mTimertouch.cancel();
			mTimertouch = null;
		}
	}

	private void startTestVideo() {
		Intent intent = new Intent();
//		intent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
		intent.setClassName("android.rk.RockVideoPlayer", "android.rk.RockVideoPlayer.RockVideoPlayer");
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(intent);
		
		if (mTimertouch == null)
			mTimertouch = new Timer();
		mTimertouch.schedule(new TimerTask() {

			@Override
			public void run() {
				touchVideoStart();
			}
		}, 2000);
		
		if (mTimer == null)
			mTimer = new Timer();
		mTimer.schedule(new TimerTask() {

			@Override
			public void run() {
				if ((mMaxTestCount > 0 && mMaxTestCount > mCurrentCount)
						|| mMaxTestCount == 0) {
					if(isTesting()){
					mCurrentCount++;
					Log.d(TAG,"-------->REFRESH_VIEW-------------");
					//mTestView.postInvalidate();
					Message message = new Message();
					mHandler.removeMessages(REFRESH_VIEW);
					message.what = REFRESH_VIEW;
					mHandler.sendMessage(message);
					}else{
						stopTest();
						Intent intent_stop = new Intent(IntentUtils.INTENT_ACTION_STOP_AND_UPDATE_RESULT);
						intent_stop.putExtra(IntentUtils.INTENT_EXTRA_TIME, mCurrentCount);
						sendBroadcast(intent_stop);
					}
				} else {
					stopTest();
					stopSelf();
					Intent intent_stop = new Intent(IntentUtils.INTENT_ACTION_STOP_AND_UPDATE_RESULT);
					intent_stop.putExtra(IntentUtils.INTENT_EXTRA_TIME, mCurrentCount);
					sendBroadcast(intent_stop);
					Intent intent = new Intent(VideoTestService.this,
							VideoTest.class);
					intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					intent.putExtra("time", mCurrentCount);
					startActivity(intent);
				}
			}
		}, 3600000, 3600000);
	}



	// Test take photo
	private void touchVideoStart() {
		float[] postions = new float[2];
		 postions[0] = 323f; //s10 JB
		 postions[1] = 327f; // s10 JB
		//postions[0] = Float.parseFloat(getString(R.string.touch_x)); // lite2
		//postions[1] = Float.parseFloat(getString(R.string.touch_y)); // lite2
		NativeInputManager.sendTouchEventSync(postions[0], postions[1]);
	}
	// Test take photo end --- ---


	@Override
	public void onDestroy() {
		Log.e(TAG, "onDestroy");
		super.onDestroy();
		stopTest();
		Intent intent = new Intent(IntentUtils.INTENT_ACTION_STOP_AND_UPDATE_RESULT);
		intent.putExtra(IntentUtils.INTENT_EXTRA_TIME, mCurrentCount);
		sendBroadcast(intent);
		mWm.removeView(mView);
		//stopForeground(true);
	}

	private static final int REFRESH_VIEW = 0;
	private Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case REFRESH_VIEW:
				Log.d(TAG,"-------->REFRESH_VIEWmsg-------------");
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
		if (name.equals("android.rk.RockVideoPlayer.VideoPlayActivity")){
		    return true;
		}else
			return false;
	}
}
