/*******************************************************************
* Company:     Fuzhou Rockchip Electronics Co., Ltd
* Description:   
* @author:     fxw@rock-chips.com
* Create at:   2014年5月14日 下午5:42:07  
* 
* Modification History:  
* Date         Author      Version     Description  
* ------------------------------------------------------------------  
* 2014年5月14日      fxw         1.0         create
*******************************************************************/   

package com.rockchip.devicetest.aging;

import java.io.File;


import com.rockchip.devicetest.R;
import com.rockchip.devicetest.aging.gpu.cube.CubeSurfaceView;
import com.rockchip.devicetest.aging.gpu.star.StarSurfaceView;
import com.rockchip.devicetest.aging.gpu.teapot.TeapotSurfaceView;
import com.rockchip.devicetest.utils.FileUtils;

import android.app.Activity;
import android.opengl.GLSurfaceView;
import android.os.Handler;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

public class GpuTest extends BaseAgingTest {
	
	public static final int UPDATE_DELAY = 3000;
	private Activity mActivity;
	private ViewGroup mParentView;
	private TextView mGpuUsageText;
	private GLSurfaceView mGLSurfaceView1;
	private GLSurfaceView mGLSurfaceView2;
	private Handler mMainHandler = new Handler();
	private boolean isRunning;

	public GpuTest(AgingConfig config, AgingCallback agingCallback){
		super(config, agingCallback);
	}
	
	@Override
	public void onCreate(Activity activity) {
		mActivity = activity;
		mParentView = (ViewGroup)mActivity.findViewById(R.id.rl_gpu_test_content);
		mGpuUsageText = (TextView)mActivity.findViewById(R.id.tv_gpu_usage);
		
		LinearLayout surfaceContent = new LinearLayout(mActivity);
		mGLSurfaceView1 = new CubeSurfaceView(mActivity);
		mGLSurfaceView2 = new TeapotSurfaceView(mActivity);
		surfaceContent.addView(mGLSurfaceView1);
		surfaceContent.addView(mGLSurfaceView2);
		
		LinearLayout.LayoutParams lps = (LinearLayout.LayoutParams)mGLSurfaceView1.getLayoutParams();
		lps.width = 0;
		lps.weight = 0.5f;
		mGLSurfaceView1.setLayoutParams(lps);
		mGLSurfaceView2.setLayoutParams(lps);
		mParentView.addView(surfaceContent, 0);
		isRunning = true;
	}

	@Override
	public void onStart() {
		mGLSurfaceView1.onResume();
		mGLSurfaceView2.onResume();
		mMainHandler.postDelayed(mUpdateAction, 50);
	}

	@Override
	public void onStop() {
		mGLSurfaceView1.onPause();
		mGLSurfaceView2.onPause();
		isRunning = false;
	}
	
	private Runnable mUpdateAction = new Runnable(){
		public void run() {
			if(isRunning){
				updateGpuUsage();
				mMainHandler.postDelayed(this, UPDATE_DELAY);
			}
		};
	};
	
	public void updateGpuUsage(){
		String malistr = FileUtils.readFromFile(new File("/sys/devices/platform/20001000.gpu/utilisation"));//mali400
		int maliUsage = 0;
		try{
			maliUsage = Integer.parseInt(malistr);
		}catch(Exception e){
		}
		if(maliUsage>10){
			mGpuUsageText.setText((int)(maliUsage)+"%");
		}
	}

	@Override
	public void onDestroy() {
		
	}

	public void onFailed() {
		if(isRunning){
			isRunning = false;
			mMainHandler.removeCallbacks(mUpdateAction);
			mGLSurfaceView1.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
			mGLSurfaceView2.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
		}
	}

}

