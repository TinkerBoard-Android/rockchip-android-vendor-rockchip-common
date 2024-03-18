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


import com.rockchip.devicetest.R;
import com.rockchip.devicetest.aging.busmon.BusmonInfoReader;
import com.rockchip.devicetest.utils.LogUtil;

import android.app.Activity;
import android.os.Handler;
import android.widget.TextView;
import android.content.Intent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BusmonTest extends BaseAgingTest {

	public static final int UPDATE_DELAY = 3000;
	private Activity mActivity;
	private boolean isRunning;
	private Handler mMainHandler = new Handler();
        private static final String TESTPROCESS_ACTION="com.rockchip.devicetest.aging.TestProcess";
	private static final String FAILLISTS_ACTION="com.rockchip.devicetest.aging.FailLists";
        private static final String pass = " -> PASS";
        private static final String fail = " -> FAIL";
        private static final String functions = "BusMonitor Test: ";
	private int mTestTimes = 6;
	List<Integer> BusmonTestfailcounter = Arrays.asList(0, 0);
	List<Integer> BusmonTestfailreport = Arrays.asList(0, 0);
	private Intent mIntenttestprocess;
	private Intent mIntentfailreport;

	public BusmonTest(AgingConfig config, AgingCallback agingCallback){
		super(config, agingCallback);
	}

	@Override
	public void onCreate(Activity activity) {
		mActivity = activity;
                mIntenttestprocess = new Intent(TESTPROCESS_ACTION);
                mIntentfailreport = new Intent(FAILLISTS_ACTION);
		isRunning = true;
	}

	@Override
	public void onStart() {
		mMainHandler.postDelayed(mUpdateAction, UPDATE_DELAY);
	}
	
	private Runnable mUpdateAction = new Runnable(){
		public void run() {
			Integer current1 = 50000;
			Integer current2  = 50000;

			if (!(BusmonTestfailcounter.get(0) >= mTestTimes)) {
				current1 = BusmonInfoReader.getBusmonVdd12vsysCur();
			
				if (current1.equals(50000)) {
					BusmonTestfailcounter.set(0, BusmonTestfailcounter.get(0)+1);
					onTestProcessFail(functions, "Bus Monitor1 fail times: "+ BusmonTestfailcounter.get(0));
				} else {
					onTestProcessSuccess(functions, "Bus Monitor1 : ");
				}
			} else {
				onTestProcessFail(functions, "Bus Monitor1 keep fail! times: "+ BusmonTestfailcounter.get(0));
                                if (BusmonTestfailreport.get(0).equals(0)) {
                                        onFailListsreport(functions, "Bus Monitor1:  ");
                                        BusmonTestfailreport.set(0, BusmonTestfailreport.get(0)+1);
                                }
			}

                        if (!(BusmonTestfailcounter.get(1) >= mTestTimes)) {
                                current2 = BusmonInfoReader.getBusmonVdd12vccCur();

                                if (current2.equals(50000)) {
                                        BusmonTestfailcounter.set(1, BusmonTestfailcounter.get(1)+1);
                                        onTestProcessFail(functions, "Bus Monitor2 fail times: "+ BusmonTestfailcounter.get(1));
                                } else {
                                        onTestProcessSuccess(functions, "Bus Monitor2 : ");
                                }
                        } else {
                                onTestProcessFail(functions, "Bus Monitor2 keep fail! times: "+ BusmonTestfailcounter.get(1));
                                if (BusmonTestfailreport.get(1).equals(0)) {
                                        onFailListsreport(functions, "Bus Monitor2:  ");
                                        BusmonTestfailreport.set(1, BusmonTestfailreport.get(1)+1);
                                }
                        }

			mMainHandler.postDelayed(this, UPDATE_DELAY);
		};
	};

        private void onTestProcessFail(String test_functions, String test_content) {
                mIntenttestprocess.putExtra("test_functions", test_functions);
                mIntenttestprocess.putExtra("test_result", fail);
                mIntenttestprocess.putExtra("test_content", test_content);
                mActivity.getApplicationContext().sendBroadcast(mIntenttestprocess);

        }

        private void onTestProcessSuccess(String test_functions, String test_content) {
                mIntenttestprocess.putExtra("test_functions", test_functions);
                mIntenttestprocess.putExtra("test_result", pass);
                mIntenttestprocess.putExtra("test_content", test_content);
                mActivity.getApplicationContext().sendBroadcast(mIntenttestprocess);

        }

        private void onFailListsreport(String test_functions, String test_content) {
                mIntentfailreport.putExtra("test_functions", test_functions);
                mIntentfailreport.putExtra("test_result", fail);
                mIntentfailreport.putExtra("test_content", test_content);
                mActivity.getApplicationContext().sendBroadcast(mIntentfailreport);

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
	}

}
