/*******************************************************************
* Company:     Fuzhou Rockchip Electronics Co., Ltd
* Description:   
* @author:     fxw@rock-chips.com
* Create at:   2014年9月23日 下午5:59:42  
* 
* Modification History:  
* Date         Author      Version     Description  
* ------------------------------------------------------------------  
* 2014年9月23日      fxw         1.0         create
*******************************************************************/   

package com.rockchip.devicetest.testcase.impl;

import java.util.Map;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Message;

import com.rockchip.devicetest.R;
import com.rockchip.devicetest.constants.ParamConstants;
import com.rockchip.devicetest.model.TestCaseInfo;
import com.rockchip.devicetest.model.TestResult;
import com.rockchip.devicetest.testcase.BaseTestCase;
import com.rockchip.devicetest.utils.LogUtil;
import com.rockchip.devicetest.utils.StringUtils;

public class BluetoothTest extends BaseTestCase {

	public static final String TAG = "BluetoothTest";
	public static final int MSG_OPEN_BT = 11;
	public static final int MAX_RETRY_NUM = 3;
    private static final int BT_REOPEN_INTERVAL_MS = 3 * 1000;
	// Discovery can take 12s to complete - set to 13s.
    private static final int BT_DISCOVERY_TIMEOUT_MS = 13 * 1000;
	private int mRetryOpen;
	private int mRetryDiscovery;
	private boolean hasRegisterReceiver;
	private boolean isDisabledBeforeTest;//If disabled before test, then resume disabled after testing.
	private BluetoothAdapter mBluetoothAdapter;
	private BluetoothReceiver mBluetoothReceiver;
	private BluetoothHandler mBluetoothHandler;
	private String mSpecifiedBTName;
	
	public BluetoothTest(Context context, Handler handler, TestCaseInfo testcase) {
		super(context, handler, testcase);
	}
	
	@Override
	public boolean onTesting() {
		mRetryOpen = 0;
		mRetryDiscovery = 0;
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		if(mBluetoothAdapter == null){
			onTestFail(R.string.bt_err_noexist);
			return false;
		}
		
		//Get specified bluetooth name
		if(mTestCaseInfo!=null){
			Map<String, String> attachParams = mTestCaseInfo.getAttachParams();
			mSpecifiedBTName = attachParams.get(ParamConstants.BT_NAME);
		}
		
		//1. Register broadcast
		mBluetoothReceiver = new BluetoothReceiver();
		if(!hasRegisterReceiver){
			IntentFilter intentFilter = new IntentFilter();
			intentFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
			intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
			intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
			intentFilter.addAction(BluetoothDevice.ACTION_FOUND);
			mContext.registerReceiver(mBluetoothReceiver, intentFilter);
			hasRegisterReceiver = true;
		}
		
		//2. Enable BT / StartDiscovery
		mBluetoothHandler = new BluetoothHandler();
		if (isDisabledBeforeTest = mBluetoothAdapter.isEnabled()) {
			startDiscovery();
		} else {
			updateDetail(getString(R.string.bt_start_open));
			mBluetoothHandler.sendEmptyMessage(MSG_OPEN_BT);
		}
		
		setTestTimeout(BT_DISCOVERY_TIMEOUT_MS);
		return true;
	}
	
	@Override
	public boolean onTestHandled(TestResult result) {
		if(mBluetoothHandler!=null)
			mBluetoothHandler.removeMessages(MSG_OPEN_BT);
		if(hasRegisterReceiver){
			mContext.unregisterReceiver(mBluetoothReceiver);
			hasRegisterReceiver = false;
		}
		if(mBluetoothAdapter!=null){
			mBluetoothAdapter.cancelDiscovery();
			if(!isDisabledBeforeTest&&mBluetoothAdapter.isEnabled())
				mBluetoothAdapter.disable();
		}
		return super.onTestHandled(result);
	}
	
	/**
	 * 停止测试
	 */
	public void stop() {
		if(mBluetoothHandler!=null)
			mBluetoothHandler.removeMessages(MSG_OPEN_BT);
		if(hasRegisterReceiver){
			mContext.unregisterReceiver(mBluetoothReceiver);
			hasRegisterReceiver = false;
		}
		if(mBluetoothAdapter!=null){
			mBluetoothAdapter.cancelDiscovery();
			if(!isDisabledBeforeTest&&mBluetoothAdapter.isEnabled())
				mBluetoothAdapter.disable();
		}
		super.stop();
	}
	
	public void startDiscovery(){
		if(mBluetoothHandler!=null)
			mBluetoothHandler.removeMessages(MSG_OPEN_BT);
		if(!mBluetoothAdapter.startDiscovery()){
			onTestFail(R.string.bt_err_discovery);
		}
	}

	class BluetoothHandler extends Handler {
		public void handleMessage(Message msg) {
			if(!isTesting()){
				return;
			}
			switch (msg.what) {
			case MSG_OPEN_BT:
				if (mBluetoothAdapter.isEnabled()) {
					startDiscovery();
				} else {
					if (mRetryOpen < MAX_RETRY_NUM) {
						LogUtil.i(TAG, "Try to open bluetooth. ");
						mRetryOpen++;
						mBluetoothAdapter.enable();
						sendEmptyMessageDelayed(MSG_OPEN_BT, BT_REOPEN_INTERVAL_MS);
					} else {
						onTestFail(R.string.bt_err_open);
						removeMessages(MSG_OPEN_BT);
					}
				}
				break;
			}
		}
	}

	class BluetoothReceiver extends BroadcastReceiver {
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
				int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, Integer.MIN_VALUE);
				if (state == BluetoothAdapter.STATE_ON) {
					startDiscovery();
				} else if (state == BluetoothAdapter.STATE_OFF) {
					//nothing
				}
			} else if (action.equals(BluetoothAdapter.ACTION_DISCOVERY_STARTED)) {
				updateDetail(getString(R.string.bt_start_discovery));
			} else if (action.equals(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)) {
				if(!isTesting()){
					return;
				}
				if (mRetryDiscovery < MAX_RETRY_NUM) {
					mRetryDiscovery++;
					startDiscovery();
				} else {
					onTestFail(R.string.bt_fail_discovery);
				}
			} else if (action.equals(BluetoothDevice.ACTION_FOUND)) {
				BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
				if (device != null) {
					if(StringUtils.isEmptyObj(mSpecifiedBTName)){// If don't config specify BT name, found any device will be success.
						onTestSuccess(mContext.getString(R.string.bt_found, device.getName()));
					}else if(mSpecifiedBTName.equals(device.getName())){
						onTestSuccess(mContext.getString(R.string.bt_found, device.getName()));
					}
				}
			}
		}
	}
		
}
