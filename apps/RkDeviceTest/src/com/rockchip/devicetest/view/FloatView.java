package com.rockchip.devicetest.view;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.graphics.PixelFormat;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.net.Uri;
import android.net.wifi.WpsInfo;
import android.os.Environment;
import android.os.RemoteException;
import android.os.StrictMode.VmPolicy;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SlidingDrawer;
import android.view.MotionEvent;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.os.Handler;
import android.view.LayoutInflater;
import com.rockchip.devicetest.R;

import java.io.File;

import com.rockchip.devicetest.ConfigFinder;
import android.os.SystemClock;
import android.os.SystemProperties;

@SuppressLint("InlinedApi")
public class FloatView  extends View {
  private  WindowManager mWm;
	private  WindowManager.LayoutParams wmParams;
	private VideoView mVideoView;
	private boolean isTestVideoExisted;
	private Handler mMainHandler;
	private boolean isRunning;
	private String mVideoPath;
	private Context mContext;
	private LayoutInflater mInflater;
	
//	private LinearLayout lay; // 愿揽控件�?
	 private View mView = null;

	public FloatView(Context context) {
        super(context);
        
        mContext = context;
     // 设置悬浮窗体属�?
        // 1.得到WindoeManager对象�?
        mWm = (WindowManager) mContext.getSystemService("window");
        mMainHandler = new Handler();
    }


	public void createFloatView(String video,LayoutInflater inflater) {
		int w,h;
		// 2.得到WindowManager.LayoutParams对象，为后续设置相关参数做准备：
		wmParams = new WindowManager.LayoutParams();
		// 3.设置相关的窗口布�?��数，要实现悬浮窗口效果，要需要设置的参数�?
		// 3.1设置window type
		wmParams.type = LayoutParams.TYPE_PHONE;
		// 3.2设置图片格式，效果为背景透明 //wmParams.format = PixelFormat.RGBA_8888;
		//wmParams.format = 1;
		wmParams.format=PixelFormat.OPAQUE;
		wmParams.flags|=8;

		// 下面的flags属�?的效果形同�?锁定”�? 悬浮窗不可触摸，不接受任何事�?同时不影响后面的事件响应�?
		//wmParams.flags = LayoutParams.FLAG_NOT_TOUCH_MODAL | LayoutParams.FLAG_NOT_FOCUSABLE;
		//wmParams.flags |=WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED;
		wmParams.flags = wmParams.flags | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS; // 排版不受限制  
		wmParams.windowAnimations = android.R.style.Animation_Translucent;

		// 4.// 设置悬浮窗口长宽数据
		DisplayMetrics dm = new DisplayMetrics();
        dm = getResources().getDisplayMetrics(); 
        h = dm.heightPixels/4;
        w = dm.widthPixels/4;
		wmParams.width = w;//LayoutParams.WRAP_CONTENT;
		wmParams.height = h;//LayoutParams.WRAP_CONTENT;
		// 5. 调整悬浮窗口至中�?
		wmParams.gravity = Gravity.RIGHT | Gravity.BOTTOM;
		// 6. 以屏幕左上角为原点，设置x、y初始�?
		wmParams.x = 0;
		wmParams.y = 0;
		// 7.将需要加到悬浮窗口中的View加入到窗口中了：
        // 如果view没有被加入到某个父组件中，则加入WindowManager�?		
       View view = inflater.inflate(R.layout.test_aging_vpu_float, null);
       mView = view.findViewById(R.id.rl_vpu_float_content);
		mView.requestFocus();
		mView.setFocusable(true);
		mView.setFocusableInTouchMode(true);
		mView.setSelected(true);	
		mWm.addView(mView, wmParams); // 创建View
		setVideoView(video);
		
		

		}
  
	private void setVideoView(String video){
	 Log.d("FloatView","setVideoView video:"+video);	
	 mVideoPath = video;
	 	File videoFile = new File(mVideoPath);
		if(videoFile==null||!videoFile.exists()){
			isTestVideoExisted = false;
			return;
		}
	 isTestVideoExisted = true;
	 mVideoView = (VideoView)mView.findViewById(R.id.vv_vpu_float);
	                 mVideoView.setVideoPath(mVideoPath);
        //mVideoView.setMediaController(mediaController);
        mVideoView.setOnPreparedListener(new OnPreparedListener() {
                        public void onPrepared(MediaPlayer mp) {
                                Log.d("VpuTest FloatView", "VideoPlayer is onPrepared. ");
                                mp.start();
                                //mp.setLooping(true);
                        }
                });
        mVideoView.setOnCompletionListener(new OnCompletionListener() {
                        public void onCompletion(MediaPlayer mp) {
                                Log.d("VpuTest FloatView", "VideoPlayer is onCompletion. ");
                                if(true){
                                        mVideoView.pause();
                                        mVideoView.stopPlayback();
                                        mMainHandler.removeCallbacks(mRepeatAction);
					                              mMainHandler.postDelayed(mRepeatAction, 300);
                                       // SystemClock.sleep(300);
                                       // if(isTestVideoExisted){
																				//	mVideoView.setVideoPath(mVideoPath);
																			//	}
                                        //mVideoView.start();
                                }
                        }
                });
        mVideoView.requestFocus();
	}
	//循环播放
	Runnable mRepeatAction = new Runnable() {
		public void run() {
			if(isTestVideoExisted){
			    Log.d("VpuTest FloatView", "VideoPlayer mRepeatAction ");
				mVideoView.setVideoPath(mVideoPath);
			}
		}
	};
	 public void onDelete(){
	     if(null != mView){
	    	if(mView.getParent()!=null){
	    		System.out.println("onDelete and stop the service");
	    		mMainHandler.removeCallbacks(mRepeatAction);
	    		mWm.removeView(mView);
	    		mView = null;
	    	}   	
	     }
	    }
}
