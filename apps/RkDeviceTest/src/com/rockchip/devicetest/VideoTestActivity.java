/*******************************************************************
* Company:     Fuzhou Rockchip Electronics Co., Ltd
* Description:   
* @author:     fxw@rock-chips.com
* Create at:   2014年7月3日 下午4:12:31  
* 
* Modification History:  
* Date         Author      Version     Description  
* ------------------------------------------------------------------  
* 2014年7月3日      fxw         1.0         create
*******************************************************************/   

package com.rockchip.devicetest;

import com.rockchip.devicetest.view.VideoView;

import android.app.Activity;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.os.Bundle;
import android.util.Log;

public class VideoTestActivity extends Activity {

    public static final String AGINGTEST_FOREGROUND_ACTION = "com.rockchip.devicetest.state.foreground";
    public static final String AGINGTEST_BACKGROUND_ACTION = "com.rockchip.devicetest.state.background";

	private VideoView mVideoView;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.test_video);
		mVideoView = (VideoView)findViewById(R.id.test_vv);
		mVideoView.setVideoPath("/system/usr/Aging_Test_Video.mp4");//"/data/data/com.rockchip.devicetest/files/Aging_Test_Video.mp4");
		mVideoView.setOnPreparedListener(new OnPreparedListener() {
			public void onPrepared(MediaPlayer mp) {
				Log.d("VpuTest", "VideoPlayer is onPrepared. ");
				mp.start();
			}
		});
		mVideoView.setOnCompletionListener(new OnCompletionListener() {
			public void onCompletion(MediaPlayer mp) {
				mVideoView.pause();
				mVideoView.stopPlayback();
				mVideoView.setVideoPath("/system/usr/Aging_Test_Video.mp4");
			}
		});
		mVideoView.requestFocus();
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		Intent keyIntent = new Intent(AGINGTEST_FOREGROUND_ACTION);
		sendBroadcast(keyIntent);
	}
	
	@Override
	protected void onStop() {
		super.onStop();
		Intent keyIntent = new Intent(AGINGTEST_BACKGROUND_ACTION);
		sendBroadcast(keyIntent);
	}
	
	public void onBackPressed() {
	}
	
}
