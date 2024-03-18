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

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;


import com.rockchip.devicetest.ConfigFinder;
import com.rockchip.devicetest.R;
import com.rockchip.devicetest.aging.cpu.CpuInfoReader;
import com.rockchip.devicetest.aging.cpu.LinpackLoop;
import com.rockchip.devicetest.enumerate.AgingType;
import com.rockchip.devicetest.utils.FileUtils;
import com.rockchip.devicetest.utils.LogUtil;
import com.rockchip.devicetest.utils.SystemBinUtils;
import com.rockchip.devicetest.utils.SystemBinUtils.CommandResponseListener;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.method.ScrollingMovementMethod;
import android.view.ViewGroup;
import android.widget.TextView;
import android.content.Context;
import android.text.*;
import android.text.style.ForegroundColorSpan;
import android.graphics.Color;
import android.content.Intent;
import android.content.BroadcastReceiver;
import android.content.IntentFilter;


public class FailLists extends BaseAgingTest {

	private static final String FAILLISTS_ACTION="com.rockchip.devicetest.aging.FailLists";
	public static final String LAN_TEST_BIN = "stressapptest";
	public static final int MSG_UPDATE_DETAIL = 1;
	public static final int MSG_LOOP = 2;
	private Activity mActivity;
	private ViewGroup mParent;
	private TextView mFailListsDetailText;
	private boolean isRunning;
	private FailListsHandler mFailListsHandler;
	private SpannableStringBuilder mDetailspanContent;
	private int mTestCount;
	private static final String pass = "PASS";
	private static final String fail = "FAIL";

	public FailLists(AgingConfig config, AgingCallback agingCallback){
		super(config, agingCallback);
	}
	
	@Override
	public void onCreate(Activity activity) {
		mActivity = activity;
		mParent = (ViewGroup) mActivity.findViewById(R.id.rl_faillists_content);
		mFailListsDetailText = (TextView)mActivity.findViewById(R.id.tv_faillists_detail);
		mFailListsDetailText.setMovementMethod(ScrollingMovementMethod.getInstance()); 
		mFailListsHandler = new FailListsHandler();
		mDetailspanContent= new SpannableStringBuilder();
    		IntentFilter intentFilterAdd = new IntentFilter(FAILLISTS_ACTION);
		mActivity.getApplicationContext().registerReceiver(mReceiver, intentFilterAdd);
	}

	@Override
	public void onStart() {
		isRunning = true;
		mFailListsHandler.sendEmptyMessageDelayed(MSG_LOOP, 2000);
		//startTest();
	}
	
	public void startTest(){
	}

	@Override
	public void onStop() {
		isRunning = false;
	}

	@Override
	public void onDestroy() {

	}
	
	@Override
	public void onFailed() {
		isRunning = false;
		mFailListsHandler.removeMessages(MSG_LOOP);
	}
	
	class FailListsHandler extends Handler {
		public void handleMessage(Message msg) {
			switch(msg.what){
			case MSG_UPDATE_DETAIL:
				mFailListsDetailText.setText(mDetailspanContent);
				int offset = mFailListsDetailText.getLineCount()*mFailListsDetailText.getLineHeight()-mFailListsDetailText.getMeasuredHeight();
				if(offset>0)
					mFailListsDetailText.scrollTo(0, offset);
				else
					mFailListsDetailText.scrollTo(0, 0);
				break;
			case MSG_LOOP:
				//mDetailContent = new StringBuilder();
				startTest();
				break;
			}
		}
	}
        BroadcastReceiver mReceiver = new BroadcastReceiver(){
                public void onReceive(Context context, Intent intent) {

                        String action = intent.getAction();
                        if (action.equals(FAILLISTS_ACTION)) {
				//Bundle bundle = getIntent().getExtras();

				String test_functions = intent.getStringExtra("test_functions");
				String test_result = intent.getStringExtra("test_result");
				String test_content = intent.getStringExtra("test_content");
				mDetailspanContent.append(test_functions);
                                mDetailspanContent.setSpan(new ForegroundColorSpan(Color.RED), 0, test_functions.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
				mDetailspanContent.append(test_content);
                                mDetailspanContent.append(test_result);
                                mDetailspanContent.append("\n");
				mFailListsHandler.sendEmptyMessage(MSG_UPDATE_DETAIL);

                        }
                }
        };


}
