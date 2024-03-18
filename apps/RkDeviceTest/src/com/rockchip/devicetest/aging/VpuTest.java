/*******************************************************************
* Company:     Fuzhou Rockchip Electronics Co., Ltd
* Description:   
* @author:     fxw@rock-chips.com
* Create at:   2014年5月14日 下午3:43:56  
* 
* Modification History:  
* Date         Author      Version     Description  
* ------------------------------------------------------------------  
* 2014年5月14日      fxw         1.0         create
*******************************************************************/   

package com.rockchip.devicetest.aging;

import java.io.File;

import com.rockchip.devicetest.ConfigFinder;
import com.rockchip.devicetest.R;
import com.rockchip.devicetest.enumerate.AgingType;
import com.rockchip.devicetest.utils.LogUtil;
import com.rockchip.devicetest.view.VideoView;
import com.rockchip.devicetest.view.FloatView;

import android.app.Activity;
import android.content.Context;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.TextView;	
import android.view.LayoutInflater;

public class VpuTest extends BaseAgingTest {
	
	public static final int DETECT_VIDEO_TIME = 60000;//60s
	private Activity mActivity;
	private VideoView mVideoView;
	private TextView mResultTextView;
	private boolean isTestVideoExisted;
	private boolean isRunning;
	private Handler mMainHandler;
	private String mVideoPath;
	private int mDuration;
	private long mLastStartTime;
	private int mTimeOutStartCnt;
	private FloatView mFloatView;
	private String mVideoPathFloat;


	public VpuTest(AgingConfig config, AgingCallback agingCallback){
		super(config, agingCallback);
		mMainHandler = new Handler();
	}
	
	public void onCreate(Activity activity) {
		mActivity = activity;
		mVideoView = (VideoView)mActivity.findViewById(R.id.vv_vpu);
		mResultTextView = (TextView)mActivity.findViewById(R.id.tv_vpu_result);
		File videoFile = getTestVideoFile();
		if(videoFile==null||!videoFile.exists()){
			isTestVideoExisted = false;
			mResultTextView.setText(R.string.vpu_err_video);
			mResultTextView.setVisibility(View.VISIBLE);
			return;
		}
		isTestVideoExisted = true;
		mTimeOutStartCnt = 0;
		mLastStartTime = System.currentTimeMillis();
		mVideoPath = videoFile.getAbsolutePath();
		//final MediaController mediaController = new MediaController(mActivity);
		mVideoView.setVideoPath(mVideoPath);
        //mVideoView.setMediaController(mediaController);
        mVideoView.setOnPreparedListener(new OnPreparedListener() {
			public void onPrepared(MediaPlayer mp) {
				Log.d("VpuTest", "VideoPlayer is onPrepared. ");
				mp.start();
				mLastStartTime = System.currentTimeMillis();
				//mp.setLooping(true);
			}
		});
        mVideoView.setOnCompletionListener(new OnCompletionListener() {
			public void onCompletion(MediaPlayer mp) {
				Log.d("VpuTest", "VideoPlayer is onCompletion. ");
				if(isRunning){
					mVideoView.pause();
					mVideoView.stopPlayback();
					mMainHandler.removeCallbacks(mRepeatAction);
					mMainHandler.postDelayed(mRepeatAction, 300);
					//mVideoView.start();
				}
			}
		});
        mVideoView.setOnErrorListener(new OnErrorListener() {
			public boolean onError(MediaPlayer mp, int what, int extra) {
				if(what==-1010){
					mVideoView.setVideoPath(mVideoPath);
					LogUtil.e(VpuTest.this, "Rock On prepared error. ");
				}else{
					mResultTextView.setText(R.string.vpu_err_play);
					mResultTextView.setVisibility(View.VISIBLE);
					mAgingCallback.onFailed(AgingType.VPU);
				}
				return true;
			}
		});
        mVideoView.requestFocus();
        //float video view test
        if(isFloatVideoActivitied()){
             File floatVideoFile = getFloatVideoFile();
		         if(floatVideoFile==null||!floatVideoFile.exists()){
                 Log.d("VpuTest", "floatVideoFile is null. ");
		         }else {
		             String mFloatVideoPath = floatVideoFile.getAbsolutePath();
		             mFloatView = new FloatView(mActivity.getApplicationContext());
		             mFloatView.createFloatView(mFloatVideoPath,LayoutInflater.from(mActivity));
		         }
		    }
	}
	
	//循环播放
	Runnable mRepeatAction = new Runnable() {
		public void run() {
			if(isTestVideoExisted){
				mVideoView.setVideoPath(mVideoPath);
			}
		}
	};
	
	//检测播放状态
	Runnable mDetectVideoAction = new Runnable() {
		public void run() {
			if(isRunning){
				if((System.currentTimeMillis()-mLastStartTime)>DETECT_VIDEO_TIME){
					mTimeOutStartCnt++;
					if(mTimeOutStartCnt<=1){//first time, try to repeat again
						mMainHandler.removeCallbacks(mRepeatAction);
						mMainHandler.postDelayed(mRepeatAction, 300);
						Log.d("VpuTest", "Detect video isn't playing. try again. ");
					}else{//Error
						Log.d("VpuTest", "Detect video occour error. stop test.");
						mAgingCallback.onFailed(AgingType.VPU);
						return;
					}
				}else{
					mTimeOutStartCnt = 0;
				}
				mMainHandler.postDelayed(mDetectVideoAction, DETECT_VIDEO_TIME);//60s
			}
		}
	};
	
	/**
	 * 获取片源时长
	 */
	public int getVideoDuration(){
		if(mDuration<=0){
			mDuration = mVideoView.getDuration();
		}
		return mDuration;
	}

	public void onStart() {
		if(isTestVideoExisted){
			mVideoView.start();
			isRunning = true;
		}
	}
	
	/**
	 * 获取测试片源路径
	 * @return
	 */
	public File getTestVideoFile(){
            if(getSysTestVideoFile()!=null){
               if(getSysTestVideoFile().exists())
                   return getSysTestVideoFile();
            }
		String mediaFile = mAgingConfig.get("testvideo");
	    return ConfigFinder.findConfigFile(mediaFile,mActivity.getApplicationContext());
	}
        public File getSysTestVideoFile(){
                String mediaFile = mAgingConfig.get("testvideo_sys");
            return ConfigFinder.findConfigFile(mediaFile,mActivity.getApplicationContext());
        }
	
/**
	 * 获取float视频测试是否开启
	 * @return
	 */
	public Boolean isFloatVideoActivitied(){
		String Activitied = mAgingConfig.get("activated_float");
	    return ("1".equals(Activitied));
	}
	/**
	 * 获取float测试片源路径
	 * @return
	 */
	public File getFloatVideoFile(){
            if(getSysFloatVideoFile()!=null){
               if(getSysFloatVideoFile().exists())
                   return getSysFloatVideoFile();
            }
		String mediaFile = mAgingConfig.get("floatvideo");
	    return ConfigFinder.findConfigFile(mediaFile,mActivity.getApplicationContext());
	}
        
        public File getSysFloatVideoFile(){
                String mediaFile = mAgingConfig.get("floatvideo_sys");
            return ConfigFinder.findConfigFile(mediaFile,mActivity.getApplicationContext());
        }

	

	public void onStop() {
		mMainHandler.removeCallbacks(mDetectVideoAction);
		mMainHandler.removeCallbacks(mRepeatAction);
		if(isTestVideoExisted){
			mVideoView.pause();
		}
		isRunning = false;
		
	}

	public void onDestroy() {
		if(isTestVideoExisted){
			mVideoView.stopPlayback();
		}
		if(mFloatView!=null){
	      mFloatView.onDelete();
	  }
	}

	@Override
	public void onFailed() {
		isRunning = false;
		mVideoView.stopPlayback();
	}
	
}

class MyVideoView extends VideoView{

	public MyVideoView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
	}
	
}
