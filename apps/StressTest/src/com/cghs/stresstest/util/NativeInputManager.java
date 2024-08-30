package com.cghs.stresstest.util;

import android.content.Context;
import android.hardware.input.InputManager;
import android.os.SystemClock;
import android.util.Log;
import android.view.InputDevice;
import android.view.KeyEvent;
import android.view.MotionEvent;

public class NativeInputManager {

	public static void injectKeyEvent(KeyEvent ev, boolean sync) {
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

        InputManager.getInstance().injectInputEvent(newEvent, 
                sync ? (InputManager.INJECT_INPUT_EVENT_MODE_WAIT_FOR_FINISH)
                        : (InputManager.INJECT_INPUT_EVENT_MODE_WAIT_FOR_RESULT));
    }

	public static void injectPointerEvent(MotionEvent ev) {
        MotionEvent newEvent = MotionEvent.obtain(ev);
        
        if ((newEvent.getSource() & InputDevice.SOURCE_CLASS_POINTER) == 0) {
            newEvent.setSource(InputDevice.SOURCE_TOUCHSCREEN);
        }
        
        InputManager.getInstance().injectInputEvent(newEvent, InputManager.INJECT_INPUT_EVENT_MODE_ASYNC);
    }

	 public static void sendKeyDownUpSync(final int keyCode) {
	        new Thread(new Runnable() {
	            @Override
	            public void run() {
	                KeyEvent keDown = new KeyEvent(KeyEvent.ACTION_DOWN, keyCode);
	                injectKeyEvent( keDown, true);

	                KeyEvent keUp = new KeyEvent(KeyEvent.ACTION_UP, keyCode);
	                injectKeyEvent( keUp, true);
	            }
	        }).start(); 
	    }

	
	 public static void sendTouchEventSync(final float x, final float y) {
	        new Thread(new Runnable() {
	        
	            @Override
	            public void run() {
	                //ACTION_DOWN
	                long downTime ;
	                long eventTime ;
	                try {
	                downTime = android.os.SystemClock.uptimeMillis();
	                eventTime = android.os.SystemClock.uptimeMillis();
	                MotionEvent me = MotionEvent.obtain(downTime, eventTime + 100, MotionEvent.ACTION_DOWN,
	                        x, y, 0);
	                injectPointerEvent(me);
	                } catch (SecurityException e) {
	                    Log.d("","MotionEvent.ACTION_DOWN");
	                }
	                
	                //ACTION_UP
	                try {
	                downTime = android.os.SystemClock.uptimeMillis();
	                eventTime = android.os.SystemClock.uptimeMillis();

	                MotionEvent me_up = MotionEvent.obtain(downTime, eventTime + 100, MotionEvent.ACTION_UP,
	                        x, y, 0);
	                injectPointerEvent(me_up);
	                } catch (SecurityException e) {
	                	Log.d("","MotionEvent.ACTION_UP");
	                }
	            }
	        }).start(); 
	    }
}
