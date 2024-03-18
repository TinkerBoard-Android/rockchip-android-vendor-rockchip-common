/*******************************************************************
* Company:     Fuzhou Rockchip Electronics Co., Ltd
* Description:   
* @author:     fxw@rock-chips.com
* Create at:   2014年5月13日 上午9:27:11  
* 
* Modification History:  
* Date         Author      Version     Description  
* ------------------------------------------------------------------  
* 2014年5月13日      fxw         1.0         create
*******************************************************************/   

package com.rockchip.devicetest.testcase.impl;

import java.util.HashMap;

import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.KeyguardManager;
import android.app.KeyguardManager.KeyguardLock;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.graphics.Color;
import android.hardware.input.InputManager;
import android.os.Handler;
import android.os.SystemClock;
import android.view.InputDevice;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnKeyListener;
import android.view.WindowManager;

import com.rockchip.devicetest.IndexActivity;
import com.rockchip.devicetest.R;
import com.rockchip.devicetest.model.TestCaseInfo;
import com.rockchip.devicetest.model.TestResult;
import com.rockchip.devicetest.testcase.BaseTestCase;

public class KeyTest extends BaseTestCase {
	
	private HashMap<Integer, Integer> mButtonKeyMaps = new HashMap<Integer, Integer>();
	private LayoutInflater mLayoutInflater;
	private View mView;
	private WindowManager mWindowManager;
	private KeyguardLock kl;
	private AlertDialog mDialog;
	
	public KeyTest(Context context, Handler handler, TestCaseInfo testcase) {
		super(context, handler, testcase);
		mLayoutInflater = LayoutInflater.from(context);
	}
	
	@Override
	public void onTestInit() {
		super.onTestInit();

		mWindowManager = (WindowManager)mContext.getSystemService(Context.WINDOW_SERVICE);
        KeyguardManager km = (KeyguardManager)mContext.getSystemService(Context.KEYGUARD_SERVICE);
        kl = km.newKeyguardLock("keyLock");
		
		int[] resIds = {R.id.btn_volumeup, R.id.btn_volumedown, R.id.btn_back, R.id.btn_menu, R.id.btn_home,
				R.id.btn_dpadup, R.id.btn_dpaddown, R.id.btn_dpadleft, R.id.btn_dpadright, R.id.btn_dpadcenter};
		int[] keycodes = {KeyEvent.KEYCODE_VOLUME_UP, KeyEvent.KEYCODE_VOLUME_DOWN, KeyEvent.KEYCODE_BACK, KeyEvent.KEYCODE_MENU, KeyEvent.KEYCODE_HOME, 
				KeyEvent.KEYCODE_DPAD_UP, KeyEvent.KEYCODE_DPAD_DOWN, KeyEvent.KEYCODE_DPAD_LEFT, KeyEvent.KEYCODE_DPAD_RIGHT, KeyEvent.KEYCODE_DPAD_CENTER};

		for (int i = 0; i < resIds.length; i++) {
			Integer key = Integer.valueOf(keycodes[i]);
			Integer value = Integer.valueOf(resIds[i]);
			mButtonKeyMaps.put(key, value);
		}
	}
	
	@Override
	public boolean onTesting() {
		if(mDialog!=null){
			mDialog.show();
			return true;
		}
        kl.disableKeyguard(); 
		mView = mLayoutInflater.inflate(R.layout.test_key, null);
		AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
		builder.setTitle(R.string.key_title);
		builder.setView(mView);
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
		mDialog = builder.create();
		mDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_KEYGUARD_DIALOG);
		mDialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL);
		mDialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
			public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
				int actionCode = event.getAction();
				System.out.println("================="+keyCode);
				
				int btnID = 0;
				Integer btnIDInteger = mButtonKeyMaps.get(keyCode);
				if(btnIDInteger==null){
					return false;
				}
				btnID = btnIDInteger.intValue();
				switch (actionCode) {
				case 0:
					setButtonBackgroundDown(btnID);
					break;
				case 1:
					setButtonBackgroundUp(btnID);
					break;
				default:
					break;
				}
				if(keyCode>=19&&keyCode<=23){
					return false;
				}
				if(event.getAction()==KeyEvent.ACTION_UP&&keyCode==KeyEvent.KEYCODE_HOME){
					Intent intent = new Intent();
					intent.putExtra("keytest", true);
					intent.setClass(mContext, IndexActivity.class);
					mContext.startActivity(intent);
					Runnable backAction = new Runnable() {
						public void run() {
							ActivityManager am = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
							ComponentName cn = am.getRunningTasks(1).get(0).topActivity;
							if(!cn.getPackageName().equals(mContext.getPackageName())){//处理多个launcher，按HOME弹出LAUNCHER选择框
								long now = SystemClock.uptimeMillis(); 
								final KeyEvent keyDown = new KeyEvent(now, now, KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_BACK, 0);
								final KeyEvent keyUp = new KeyEvent(now, now, KeyEvent.ACTION_UP, KeyEvent.KEYCODE_BACK, 0);
								boolean result = injectKeyEvent(keyDown, false);
								boolean result2 = injectKeyEvent(keyUp, false);
							}
						}
					};
					mMainHandler.postDelayed(backAction, 1000);
				}
				return true;
			}
		});
		mDialog.show();
		return true;
	}
	@Override
	public boolean onTestHandled(TestResult result) {
		kl.reenableKeyguard();
		return super.onTestHandled(result);
	}
	
	private void setButtonBackgroundDown(int resId) {
		mView.findViewById(resId).setBackgroundColor(Color.BLUE);
	}
	
	private void setButtonBackgroundUp(int resId) {
		mView.findViewById(resId).setBackgroundColor(Color.GREEN);
	}
	
	public boolean injectKeyEvent(KeyEvent ev, boolean sync) {
		   long downTime = ev.getDownTime();
		   long eventTime = ev.getEventTime();
		   int action = ev.getAction();
		   int code = ev.getKeyCode();
		   int repeatCount = ev.getRepeatCount();
		   int metaState = ev.getMetaState();
		   int deviceId = ev.getDeviceId();
		   int scancode = ev.getScanCode();
		   int source = ev.getSource();
		   int flags = ev.getFlags();	   
		   if (source == InputDevice.SOURCE_UNKNOWN) {
			   source = InputDevice.SOURCE_KEYBOARD;
		   }	
		   if (eventTime == 0) eventTime = SystemClock.uptimeMillis();
		   if (downTime == 0) downTime = eventTime;
		   KeyEvent newEvent = new KeyEvent(downTime, eventTime, action, code, repeatCount, metaState,
				   deviceId, scancode, flags | KeyEvent.FLAG_FROM_SYSTEM, source);
		   InputManager mInputManager = (InputManager)mContext.getSystemService(Context.INPUT_SERVICE);
		   final boolean result = mInputManager.injectInputEvent(newEvent, sync ? 2:1);	  
		   return result;
	   }

}
