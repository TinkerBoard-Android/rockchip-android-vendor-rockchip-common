package com.cghs.stresstest.test;

import java.io.IOException;

import com.cghs.stresstest.R;

import android.app.Activity;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class CameraAutoTestActivity extends Activity implements SurfaceHolder.Callback{
	private final String LOG_TAG = "CameraAutoTestActivity";

	private final int MSG_FINISH = 0;
	
	private Camera mCameraDevice;
	private View nocamera;
	private boolean hasCamera = false;
	private SurfaceView mSurfaceView;
	private Button mSwitchBut;
	private SurfaceHolder mSurfaceHolder = null;
	private int mNumberOfCameras = 0;
	private int mCurrentCameraId = 0;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.camera_auto_test);
		initRes();
		mHandler.sendEmptyMessageDelayed(MSG_FINISH, 5000);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		SurfaceHolder holder = mSurfaceView.getHolder();
        holder.addCallback(this);
        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
	}
	
	private void initRes() {
		nocamera = findViewById(R.id.nocamera);
		mSurfaceView = (SurfaceView)findViewById(R.id.camera_preview);
		mSwitchBut = (Button)findViewById(R.id.camera_switch_btn);
		mSwitchBut.setOnClickListener(mOnClickListener);
		mNumberOfCameras = Camera.getNumberOfCameras();
		if(mNumberOfCameras <= 1)
			mSwitchBut.setVisibility(View.GONE);
	}
	
	private OnClickListener mOnClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.camera_switch_btn:
				mCameraDevice.stopPreview();
				mCameraDevice.release();
				mCameraDevice = null;
				mCurrentCameraId = (mCurrentCameraId + 1) % mNumberOfCameras;
				mCameraDevice = Camera.open(mCurrentCameraId);
				mSurfaceHolder = null;
				mSurfaceView.setVisibility(View.GONE);
				mSurfaceView.setVisibility(View.VISIBLE);
				break;

			default:
				break;
			}
		}
	};

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		 Log.d(LOG_TAG, "---->>>>>>>>>> surfaceChanged()");
			if (holder.getSurface() == null) {
	            Log.d(LOG_TAG, "---- surfaceChanged(),  holder.getSurface() == null");
	            return;
	        }
			mSurfaceHolder = holder;
			try{
				hasCamera = true;
				nocamera.setVisibility(View.GONE);
				mCameraDevice = Camera.open(mCurrentCameraId);
			}catch(Exception e){
				hasCamera = false;
				nocamera.setVisibility(View.VISIBLE);
				mSwitchBut.setVisibility(View.GONE);
				Log.e(LOG_TAG, " ____________- camera error");
				return;
			}
			try{
				mCameraDevice.setPreviewDisplay(mSurfaceHolder);
			}catch(IOException e){
				e.printStackTrace();
			}
			mCameraDevice.startPreview();
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		Log.e(LOG_TAG, "surfaceDestroyed() ... ...");
		if(mCameraDevice != null){
			mCameraDevice.stopPreview();
	    	mCameraDevice.release();
		}
		mSurfaceHolder = null;
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
        if(mCameraDevice != null)
        	mCameraDevice.release();
	}
	
	private Handler mHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case MSG_FINISH:
				setResult(0);
				finish();
				break;

			default:
				break;
			}
		};
	};
	
}
