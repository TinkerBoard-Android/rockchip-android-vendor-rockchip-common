/*******************************************************************
* Company:     Fuzhou Rockchip Electronics Co., Ltd
* Description:   
* @author:     fxw@rock-chips.com
* Create at:   2014年5月12日 下午5:41:32  
* 
* Modification History:  
* Date         Author      Version     Description  
* ------------------------------------------------------------------  
* 2014年5月12日      fxw         1.0         create
*******************************************************************/   

package com.rockchip.devicetest.testcase.impl;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.os.Handler;
import android.widget.VideoView;

import com.rockchip.devicetest.ConfigFinder;
import com.rockchip.devicetest.R;
import com.rockchip.devicetest.constants.ResourceConstants;
import com.rockchip.devicetest.enumerate.AgingType;
import com.rockchip.devicetest.model.TestCaseInfo;
import com.rockchip.devicetest.testcase.BaseTestCase;
import com.rockchip.devicetest.utils.FileUtils;
import com.rockchip.devicetest.utils.IniEditor;
import com.rockchip.devicetest.utils.LogUtil;
import com.rockchip.devicetest.utils.SystemBinUtils;

public class HDMITest extends BaseTestCase {

    private static final String DISPLAY_FILE = "/sys/class/display";
    private static final String	ENABLED = "1";
    private static final String	DISABLED = "0";
    private VideoView mVideoView;
    
	public HDMITest(Context context, Handler handler, TestCaseInfo testcase) {
		super(context, handler, testcase);
	}
	
	@Override
	public boolean onTesting() {
		File testVideo = getTestVideoFile();
		if(testVideo==null||!testVideo.exists()){
			onTestFail(R.string.vpu_err_video);
			return false;
		}
		File hdmiFile = getHdmiDisplay();
		String enablestr = FileUtils.readFromFile(hdmiFile);
		if(enablestr!=null&&!ENABLED.equals(enablestr)){
			setEnabled(hdmiFile, true);
		}
		mVideoView = new VideoView(mContext);
		int winHeight = mContext.getResources().getDisplayMetrics().heightPixels;
		mVideoView.setMinimumHeight((int)(winHeight*0.7));
		mVideoView.setMinimumWidth((int)(winHeight*0.7*1.5));
		AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
		builder.setTitle(R.string.hdmi_title);
		builder.setMessage(R.string.hdmi_msg);
		builder.setView(mVideoView);
		builder.setPositiveButton(mContext.getString(R.string.pub_success), new OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				mVideoView.stopPlayback();
				onTestSuccess();
			}
		});
		builder.setNegativeButton(mContext.getString(R.string.pub_fail), new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				mVideoView.stopPlayback();
				onTestFail(0);
			}
		});
		builder.setCancelable(false);
		builder.create().show();
		final String videoPath = testVideo.getAbsolutePath();
		mVideoView.setVideoPath(testVideo.getAbsolutePath());
		mVideoView.setOnPreparedListener(new OnPreparedListener() {
			public void onPrepared(MediaPlayer mp) {
				mp.start();
			}
		});
		mVideoView.setOnCompletionListener(new OnCompletionListener() {
			public void onCompletion(MediaPlayer mp) {
				if(isTesting()){
					mVideoView.setVideoPath(videoPath);
				}
			}
		});
		mVideoView.setOnErrorListener(new OnErrorListener() {
			public boolean onError(MediaPlayer mp, int what, int extra) {
				onTestFail(R.string.vpu_err_play);
				return true;
			}
		});
		return true;
	}
	
	/**
	 * 获取HDMI enable节点
	 * @return
	 */
	private File getHdmiDisplay(){
		File hdmiFile = new File(DISPLAY_FILE+"/display0.HDMI", "enable");
		SystemBinUtils.chmod("666", hdmiFile.getAbsolutePath());
		return hdmiFile;
		/*
		File displayFile = new File(DISPLAY_FILE);
		File[] files = displayFile.listFiles();
		if(files==null) return null;
		
		for(File item : files){
			if(item.getName().endsWith("HDMI")){
				return new File(item, "enable");
			}
		}
		return null;*/
	}
	
	/**
	 * 从agingconfig中获取测试片源文件路径
	 * @param file
	 * @param enabled
	 * @return
	 */
	public File getTestVideoFile(){
		InputStream in = null;
		try{
			in = mContext.getAssets().open(ResourceConstants.AGING_CONFIG_FILE);
			IniEditor agingConfig = new IniEditor();
			agingConfig.load(in);
			String mediaFile = agingConfig.get(AgingType.VPU.getType(), "testvideo");
			return ConfigFinder.findConfigFile(mediaFile,mContext);
		}catch(Exception e){
			e.printStackTrace();
			LogUtil.e(mContext, "Read aging test config failed");
			return null;
		}finally{
			if(in!=null){
				try {
					in.close();
				} catch (IOException e) {
				}
			}
		}
	}
	
	
	public boolean setEnabled(File file, boolean enabled) {
		return FileUtils.write2File(file, enabled?ENABLED:DISABLED);
    }

}
