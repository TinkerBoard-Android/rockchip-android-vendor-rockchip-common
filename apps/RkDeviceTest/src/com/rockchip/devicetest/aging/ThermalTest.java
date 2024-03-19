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
import com.rockchip.devicetest.aging.thermal.ThermalInfoReader;
import com.rockchip.devicetest.utils.LogUtil;

import android.app.Activity;
import android.os.Handler;
import android.widget.TextView;
import android.content.Intent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ThermalTest extends BaseAgingTest {

	public static final int UPDATE_DELAY = 3000;
	private Activity mActivity;
	private boolean isRunning;
	private long[] mCpuInfo = new long[2];
	private Handler mMainHandler = new Handler();
        private static final String TESTPROCESS_ACTION="com.rockchip.devicetest.aging.TestProcess";
	private static final String FAILLISTS_ACTION="com.rockchip.devicetest.aging.FailLists";
        private static final String pass = " -> PASS";
        private static final String fail = " -> FAIL";
        private static final String functions = "Thermal Test:  ";
	private int mTestTimes = 6;
	List<Integer> ThermalTestfailcounter = Arrays.asList(0, 0);
	List<Integer> ThermalTestfailreport = Arrays.asList(0, 0);

	private Intent mIntenttestprocess;
	private Intent mIntentfailreport;

	public ThermalTest(AgingConfig config, AgingCallback agingCallback){
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
			Integer temp1 = 10;
			Integer temp2 = 10;

			if (!(ThermalTestfailcounter.get(0) >= mTestTimes)) {
				temp1 = ThermalInfoReader.getThermal1CurrentTemp();
			
				if (temp1.equals(10)) {
					ThermalTestfailcounter.set(0, ThermalTestfailcounter.get(0)+1);
					onTestProcessFail(functions, "Thermal Sensor1 fail times: "+ ThermalTestfailcounter.get(0));
					LogUtil.d(this, "Thermal Sensor1 fail times: "+ ThermalTestfailcounter.get(0));
				} else {
					ThermalTestfailcounter.set(0, 0);
					onTestProcessSuccess(functions, "Thermal Sensor1 value: "+(temp1/1000) );
				}
			} else {
				onTestProcessFail(functions, "Thermal Sensor1 keep fail! times: "+ ThermalTestfailcounter.get(0));
                                if (ThermalTestfailreport.get(0).equals(0)) {
                                        onFailListsreport(functions, "Thermal Sensor1:  ");
					ThermalTestfailreport.set(0, ThermalTestfailcounter.get(0)+1);
                                }
			}

                        temp2 = ThermalInfoReader.getThermal2CurrentTemp();
                        if (!(ThermalTestfailcounter.get(1) >= mTestTimes)) {
                                temp2 = ThermalInfoReader.getThermal2CurrentTemp();

                                if (temp2.equals(10)) {
                                        ThermalTestfailcounter.set(1, ThermalTestfailcounter.get(1)+1);
                                        onTestProcessFail(functions, "Thermal Sensor2 fail times: "+ ThermalTestfailcounter.get(1));
					LogUtil.d(this, "Thermal Sensor2 fail times: "+ ThermalTestfailcounter.get(1));
                                } else {
					ThermalTestfailcounter.set(1, 0);
                                        onTestProcessSuccess(functions, "Thermal Sensor2 value: "+(temp2/1000) );
                                }
                        } else {
                                onTestProcessFail(functions, "Thermal Sensor2 keep fail! times: "+ ThermalTestfailcounter.get(1));
                                if (ThermalTestfailreport.get(1).equals(0)) {
                                        onFailListsreport(functions, "Thermal Sensor2:  ");
                                        ThermalTestfailreport.set(1, ThermalTestfailcounter.get(1)+1);
                                }
                        }

			mMainHandler.postDelayed(this, UPDATE_DELAY);
		};
	};

	static <T> List<T> newListWithDefault(T value, int size) {
    		List<T> list = new ArrayList<>(size);
    		for (int i = 0; i < size; i++) {
        		list.add(value);
    		}
    		return list;
	}

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
