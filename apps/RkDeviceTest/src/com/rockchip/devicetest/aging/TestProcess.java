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
import android.os.SystemClock;

public class TestProcess extends BaseAgingTest {

	private static final String TEST_STRING = "ASUS UsbHostTest File";
	private static final String TESTPROCESS_ACTION="com.rockchip.devicetest.aging.TestProcess";
	public static final String LAN_TEST_BIN = "stressapptest";
	public static final int MSG_UPDATE_DETAIL = 1;
	public static final int MSG_LOOP = 2;
	private Activity mActivity;
	private ViewGroup mParent;
	private TextView mTestProcessDetailText;
	private TextView mTestProcessCountText;
	private boolean isRunning;
	private TestProcessHandler mTestProcessHandler;
	private StringBuilder mDetailContent;
	private SpannableStringBuilder mDetailspanContent;
	private int mTestCount;
	private long mstartTime;
	private static final String pass = "PASS";
	private static final String fail = "FAIL";
	private static final String functions = "LAN Test: ";

	public TestProcess(AgingConfig config, AgingCallback agingCallback){
		super(config, agingCallback);
	}
	
	@Override
	public void onCreate(Activity activity) {

		mActivity = activity;
		mParent = (ViewGroup) mActivity.findViewById(R.id.rl_testprocess_content);
		mTestProcessDetailText = (TextView)mActivity.findViewById(R.id.tv_testprocess_detail);
		mTestProcessCountText = (TextView)mActivity.findViewById(R.id.tv_testprocess_count);
		mTestProcessDetailText.setMovementMethod(ScrollingMovementMethod.getInstance()); 
		mTestProcessHandler = new TestProcessHandler();
		mDetailContent = new StringBuilder();
		mDetailspanContent= new SpannableStringBuilder();
    		IntentFilter intentFilterAdd = new IntentFilter(TESTPROCESS_ACTION);
		mActivity.getApplicationContext().registerReceiver(mReceiver, intentFilterAdd);
		mstartTime = SystemClock.elapsedRealtime(); 
	}

	@Override
	public void onStart() {
		isRunning = true;
		mTestProcessHandler.sendEmptyMessageDelayed(MSG_LOOP, 2000);
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
		mTestProcessHandler.removeMessages(MSG_LOOP);
	}
	
	class TestProcessHandler extends Handler {
		public void handleMessage(Message msg) {
			long testtime = SystemClock.elapsedRealtime() - mstartTime;
			switch(msg.what){
			case MSG_UPDATE_DETAIL:

				mTestProcessDetailText.setText(mDetailspanContent);
				int offset = mTestProcessDetailText.getLineCount()*mTestProcessDetailText.getLineHeight()-mTestProcessDetailText.getMeasuredHeight();
				if(offset>0)
					mTestProcessDetailText.scrollTo(0, offset);
				else
					mTestProcessDetailText.scrollTo(0, 0);
				mTestProcessCountText.setText(Long.toString(testtime/1000)+" seconds");

				break;
			case MSG_LOOP:
				mDetailContent = new StringBuilder();
				startTest();
				break;
			}
		}
	}
        BroadcastReceiver mReceiver = new BroadcastReceiver(){
                public void onReceive(Context context, Intent intent) {

                        String action = intent.getAction();
                        if (action.equals(TESTPROCESS_ACTION)) {
				//Bundle bundle = getIntent().getExtras();

				String test_functions = intent.getStringExtra("test_functions");
				String test_result = intent.getStringExtra("test_result");
				String test_content = intent.getStringExtra("test_content");
				mDetailspanContent.append(test_functions);
				mDetailspanContent.append(test_content);
                                //mDetailspanContent.setSpan(new ForegroundColorSpan(Color.RED), 0, test_functions.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
                                mDetailspanContent.append(test_result);
                                mDetailspanContent.append("\n");
				mTestProcessHandler.sendEmptyMessage(MSG_UPDATE_DETAIL);

                        }
                }
        };


}
