/*******************************************************************
* Company:     Fuzhou Rockchip Electronics Co., Ltd
* Description:   
* @author:     fxw@rock-chips.com
* Create at:   2014年5月13日 上午9:04:27  
* 
* Modification History:  
* Date         Author      Version     Description  
* ------------------------------------------------------------------  
* 2014年5月13日      fxw         1.0         create
*******************************************************************/   

package com.rockchip.devicetest.testcase.impl;

import java.io.IOException;
import java.io.File;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.res.AssetFileDescriptor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Handler;

import com.rockchip.devicetest.R;
import com.rockchip.devicetest.model.TestCaseInfo;
import com.rockchip.devicetest.model.TestResult;
import com.rockchip.devicetest.utils.FileUtils;
import com.rockchip.devicetest.testcase.BaseTestCase;
import android.util.Log;
import android.net.Uri;

public class AudioChannelTest extends BaseTestCase {
	
	public static final String AUDIO_FILE = "test_music.mp3";
	private MediaPlayer mPlayer;
	private AudioManager mAudioManager;
	private boolean leftEnable = true;//左声道
	private boolean rightEnable = false;//右声道
	private int mOldVolume;
	private boolean mSpeakerOn;
	
	public AudioChannelTest(Context context, Handler handler, TestCaseInfo testcase) {
		super(context, handler, testcase);
		mAudioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
	}
	
	public boolean onTesting() {
		//View view = mLayoutInflater.inflate(R.layout.test_key, null);
		AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
		builder.setTitle(R.string.audio_title);
		//builder.setView(view);
		builder.setSingleChoiceItems(R.array.test_audio, 0, new DialogInterface.OnClickListener(){
			@Override
			public void onClick(DialogInterface dialog, int which) {
				if(which==0){
					leftEnable = true;
					rightEnable = false;
				}else if(which==1){
					leftEnable = false;
					rightEnable = true;
				}else if(which==2){
					leftEnable = true;
					rightEnable = true;
				}
				if(mPlayer!=null)
					mPlayer.setVolume(leftEnable ? 1 : 0, rightEnable ? 1 : 0);
			}
		});
		builder.setPositiveButton(mContext.getString(R.string.pub_success), new OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				onTestSuccess();
			}
		});
		builder.setNegativeButton(mContext.getString(R.string.pub_fail), new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				onTestFail(0);
			}
		});
		builder.setCancelable(false);
		AlertDialog dialog = builder.create();
		dialog.show();
		startPlay();
		return super.onTesting();
	}
	
	public boolean onTestHandled(TestResult result) {
		stopPlay();
		mAudioManager.setStreamSolo(AudioManager.STREAM_MUSIC, false);
		mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, mOldVolume, 0);
		if(!mSpeakerOn){
			this.mAudioManager.setSpeakerphoneOn(false);
		}
		return super.onTestHandled(result);
	}
	
	/**
	 * 开始播放
	 */
	private void startPlay(){
		stopPlay();
		initMusic();
		this.mAudioManager.setStreamSolo(AudioManager.STREAM_MUSIC, true);
		int i = this.mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
		mOldVolume = i;
		int j = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
		mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, j, 0);
		this.mSpeakerOn = this.mAudioManager.isSpeakerphoneOn();
		if (!this.mSpeakerOn) {
			this.mAudioManager.setSpeakerphoneOn(true);
		}
		mPlayer.setVolume(leftEnable ? 1 : 0, rightEnable ? 1 : 0);
                Log.d("hjc","startPlay:"+("data/media/0/"+AUDIO_FILE));
		this.mPlayer.start();
                
	}
	
	//停止播放
	private void stopPlay(){
		if(mPlayer == null) {
			return;
		}
		mPlayer.stop();
		this.mPlayer.release();
		this.mPlayer = null;
	}
	
	private void initMusic(){
		mPlayer = new MediaPlayer();
		try {
                        boolean copyResult = FileUtils.copyFromAsset(mContext, AUDIO_FILE, true);
                        FileUtils.chmodDataFile(mContext, AUDIO_FILE);
			//AssetFileDescriptor fd = mContext.getAssets().openFd(AUDIO_FILE);
			//mPlayer.setDataSource(fd.getFileDescriptor(), fd.getStartOffset(),
			//		fd.getDeclaredLength());
                     //case rockit not ready for fd.
                        Uri uri =Uri.fromFile(new File(mContext.getFilesDir().
                                                       getAbsolutePath()+File.separator+AUDIO_FILE));
                        mPlayer.setDataSource(mContext,uri,null);
			mPlayer.prepare();
			mPlayer.setLooping(true);
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
