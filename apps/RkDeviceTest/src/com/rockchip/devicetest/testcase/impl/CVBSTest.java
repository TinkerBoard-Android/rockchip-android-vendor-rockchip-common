/*******************************************************************
* Company:     Fuzhou Rockchip Electronics Co., Ltd
* Description:   
* @author:     fxw@rock-chips.com
* Create at:   2014年5月12日 下午6:14:57  
* 
* Modification History:  
* Date         Author      Version     Description  
* ------------------------------------------------------------------  
* 2014年5月12日      fxw         1.0         create
*******************************************************************/   

package com.rockchip.devicetest.testcase.impl;

import java.io.File;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Handler;

import com.rockchip.devicetest.R;
import com.rockchip.devicetest.model.TestCaseInfo;
import com.rockchip.devicetest.testcase.BaseTestCase;
import com.rockchip.devicetest.utils.FileUtils;
import com.rockchip.devicetest.utils.TimerUtil;

public class CVBSTest extends BaseTestCase {

	private static final String DISPLAY_FILE = "/sys/class/display";
    private static final String	ENABLED = "1";
    private static final String	DISABLED = "0";
    
	public CVBSTest(Context context, Handler handler, TestCaseInfo testcase) {
		super(context, handler, testcase);
	}
	
	@Override
	public boolean onTesting() {
		setEnabled(getHdmiDisplay(), false);
		TimerUtil.wait(500);//Delay
		File cvbsFile = getCvbsDisplay();
		String enablestr = FileUtils.readFromFile(cvbsFile);
		if(enablestr!=null&&!ENABLED.equals(enablestr)){
			setEnabled(cvbsFile, true);
		}
		AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
		builder.setTitle(R.string.cvbs_title);
		builder.setMessage(R.string.cvbs_msg);
		builder.setPositiveButton(mContext.getString(R.string.pub_success), new OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				onTestSuccess();
				//先关再开
				//setEnabled(getCvbsDisplay(), false);
				//setEnabled(getHdmiDisplay(), true);
			}
		});
		builder.setNegativeButton(mContext.getString(R.string.pub_fail), new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				onTestFail(0);
				//setEnabled(getCvbsDisplay(), false);
				//setEnabled(getHdmiDisplay(), true);
			}
		});
		builder.setCancelable(false);
		builder.create().show();
		return true;
	}
	
	/**
	 * 获取CVBS enable节点
	 * @return
	 */
	private File getCvbsDisplay(){
		File tvFile = new File(DISPLAY_FILE+"/display0.TV", "enable");
		//SystemBinUtils.chmod("666", tvFile.getAbsolutePath());
		return tvFile;
		/*
		File displayFile = new File(DISPLAY_FILE);
		File[] files = displayFile.listFiles();
		if(files==null) return null;
		
		for(File item : files){
			if(item.getName().endsWith("TV")){
				return new File(item, "enable");
			}
		}
		return null;*/
	}
	
	private File getHdmiDisplay(){
		File hdmiFile = new File(DISPLAY_FILE+"/display0.HDMI", "enable");
		//SystemBinUtils.chmod("666", hdmiFile.getAbsolutePath());
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
	
	public boolean setEnabled(File file, boolean enabled) {
		if(file==null) return false;
		return FileUtils.write2File(file, enabled?ENABLED:DISABLED);
    }

}
