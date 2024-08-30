package com.cghs.stresstest.test;

import com.cghs.stresstest.R;

import android.app.Activity;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.MediaController;
import android.widget.Toast;
import android.widget.VideoView;

public class VideoPlayActivity extends Activity{
	private VideoView mVideoView;
	private String mPath = null;
	private int mTime; //hours
	private int mAutoTestFlag = 0;
	private WakeLock mWakeLock;
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);  
		mWakeLock = ((PowerManager)getSystemService("power")).newWakeLock(PowerManager.FULL_WAKE_LOCK, "videotest");
		mWakeLock.acquire();
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,   
	                WindowManager.LayoutParams.FLAG_FULLSCREEN);  
		setContentView(R.layout.activity_video_play);
		
		init();
		//LogFileHelper.writeLogWithoutClose("["+SystemUtil.getSystemTime()+"]"+" :VIDEO TEST START.\n");
		playVideo();
		
	};
	
	private void init() {
		mPath = getIntent().getStringExtra("path");
		mAutoTestFlag = getIntent().getIntExtra("auto", 0);
		if (mAutoTestFlag != 0) {
			mTime = getIntent().getIntExtra("time", 0);
			mHandler.sendEmptyMessageDelayed(1, 2*60*60*1000);
		}
		
		mVideoView = (VideoView) this.findViewById(R.id.video_view);
		Button stopBtn = (Button) findViewById(R.id.stop_btn);
		stopBtn.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				stopVideo();
			}
		});
		
	}
	
	private void playVideo() {
		if (mPath == null) {
			Toast.makeText(this, R.string.error_video, Toast.LENGTH_LONG).show();
			return;
		}
//		LogFileHelper.writeLogWithoutClose("["+SystemUtil.getSystemTime()+"]"+" :PLAY VIDEO ="+mPath+".\n");
        MediaController mc = new MediaController(this);
        mVideoView.setMediaController(mc);
        //videoView.setVideoURI(Uri.parse(""));
        mVideoView.setVideoPath(mPath);
        mVideoView.setOnCompletionListener(new OnCompletionListener() {
			public void onCompletion(MediaPlayer mp) {
				mVideoView.start();
			}
		});
        mVideoView.requestFocus();
        mVideoView.start();
	}
	
	private void stopVideo() {
		mVideoView.stopPlayback();
		mWakeLock.release();
//		LogFileHelper.writeLogWithoutClose("["+SystemUtil.getSystemTime()+"]"+" :VIDEO TEST FINISH.\n");
		finish();
	}
	
	private Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 1:
				if(mAutoTestFlag == 1)
					stopVideo();
				break;

			default:
				break;
			}
		};
	};
	
}
