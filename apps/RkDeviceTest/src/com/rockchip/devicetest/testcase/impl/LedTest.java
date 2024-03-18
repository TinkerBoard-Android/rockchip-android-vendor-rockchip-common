/*******************************************************************
* Company:     Fuzhou Rockchip Electronics Co., Ltd
* Description:   
* @author:     fxw@rock-chips.com
* Create at:   2014年5月13日 下午5:51:34  
* 
* Modification History:  
* Date         Author      Version     Description  
* ------------------------------------------------------------------  
* 2014年5月13日      fxw         1.0         create
*******************************************************************/   

package com.rockchip.devicetest.testcase.impl;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnDismissListener;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.rockchip.devicetest.R;
import com.rockchip.devicetest.model.TestCaseInfo;
import com.rockchip.devicetest.testcase.BaseTestCase;
import com.rockchip.devicetest.testcase.LEDSettings;
import com.rockchip.devicetest.testcase.LEDSettings.LEDMode;

public class LedTest extends BaseTestCase {

	private static final int MSG_ON_OFF = 1;
	private LEDMode mLEDMode;
	private boolean mStartOnOff;
	private LEDHandler mLEDHandler;
	
	public LedTest(Context context, Handler handler, TestCaseInfo testcase) {
		super(context, handler, testcase);
		mLEDHandler = new LEDHandler(handler.getLooper());
	}
	
	public boolean onTesting() {
		AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
		builder.setTitle(R.string.led_title);
		builder.setMessage(R.string.led_test_msg);
		/*
		builder.setSingleChoiceItems(R.array.test_led, 0, new DialogInterface.OnClickListener(){
			@Override
			public void onClick(DialogInterface dialog, int which) {
				if(which==0){
					mStartOnOff = false;
					LEDSettings.onLed();
					mLEDMode = LEDMode.ON;
				}else if(which==1){
					mStartOnOff = false;
					LEDSettings.offLed();
					mLEDMode = LEDMode.OFF;
				}else if(which==2){
					mStartOnOff = true;
					mLEDHandler.sendEmptyMessageDelayed(MSG_ON_OFF, 100);
				}
			}
		});*/
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
		builder.setOnDismissListener(new OnDismissListener() {
			public void onDismiss(DialogInterface dialog) {
				mStartOnOff = false;
			}
		});
		builder.setCancelable(false);
		AlertDialog dialog = builder.create();
		dialog.show();
		mStartOnOff = true;
		mLEDHandler.sendEmptyMessageDelayed(MSG_ON_OFF, 50);
		return super.onTesting();
	}
	
	class LEDHandler extends Handler {
		
		public LEDHandler(Looper looper) {
			super(looper);
		}
		
		public void handleMessage(Message msg) {
			switch(msg.what){
			case MSG_ON_OFF:
				if(mStartOnOff){
					if(mLEDMode==LEDMode.OFF){
						mLEDMode = LEDMode.ON;
						LEDSettings.onLed(mContext);
					}else{
						mLEDMode = LEDMode.OFF;
						LEDSettings.offLed(mContext);
					}
					mLEDHandler.sendEmptyMessageDelayed(MSG_ON_OFF, 600);
				}
				break;
			}
			
		}
	}
	

}
