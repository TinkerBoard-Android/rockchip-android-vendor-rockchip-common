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

public class MemoryTest extends BaseAgingTest implements CommandResponseListener {

	public static final String MEM_TEST_BIN = "stressapptest";
	public static final int MSG_UPDATE_DETAIL = 1;
	public static final int MSG_LOOP = 2;
	private Activity mActivity;
	private ViewGroup mParent;
	private TextView mMemDetailText;
	private TextView mMemCountText;
	private boolean isRunning;
	private MemoryHandler mMemoryHandler;
	private StringBuilder mDetailContent;
	private int mTestCount;

	public MemoryTest(AgingConfig config, AgingCallback agingCallback){
		super(config, agingCallback);
	}
	
	@Override
	public void onCreate(Activity activity) {
		mActivity = activity;
		mParent = (ViewGroup) mActivity.findViewById(R.id.rl_mem_content);
		mMemDetailText = (TextView)mActivity.findViewById(R.id.tv_mem_detail);
		mMemCountText = (TextView)mActivity.findViewById(R.id.tv_mem_count);
		mMemDetailText.setMovementMethod(ScrollingMovementMethod.getInstance()); 
		mMemoryHandler = new MemoryHandler();
		mDetailContent = new StringBuilder();
		boolean copyResult = FileUtils.copyFromAsset(mActivity, MEM_TEST_BIN, true);
		if(copyResult){
			FileUtils.chmodDataFile(mActivity, MEM_TEST_BIN);
		}else{
			mMemDetailText.setText(R.string.mem_err_bin);
		}
	}

	@Override
	public void onStart() {
		isRunning = true;
		mMemoryHandler.sendEmptyMessageDelayed(MSG_LOOP, 2000);
		//startTest();
	}
	
	public void startTest(){
		if(!isRunning){
			return;
		}
		mTestCount++;
		mMemCountText.setText(mActivity.getString(R.string.mem_test_count, mTestCount));
		String binPath = FileUtils.getDataFileFullPath(mActivity, MEM_TEST_BIN);
		final String cmd = String.format(".%s -M %d -s %s -m %d", binPath, getMemorySize(), getMemoryTime(), getThreadNum());
		LogUtil.d(this, "Execute Memory test cmd: "+cmd);
		new Thread(){
			public void run() {
				SystemBinUtils.execCommand(cmd, MemoryTest.this);
			}
		}.start();
	}
	
	@Override
	public void onStop() {
		isRunning = false;
	}

	@Override
	public void onDestroy() {

	}
	
	/**
	 * 获取测试内存大小
	 */
	public int getMemorySize(){
		String memstr = mAgingConfig.get("mem_size");
		return Integer.parseInt(memstr);
	}
	
	/**
	 * 获取测试内存时间
	 */
	public int getMemoryTime(){
		String memstr = mAgingConfig.get("mem_time");
		return Integer.parseInt(memstr);
	}
	
	/**
	 * 是否循环测试
	 */
	public boolean isLoopTest(){
		String memstr = mAgingConfig.get("mem_loop");
		return "1".equals(memstr);
	}
	
	/**
	 * 获取线程数
	 */
	public int getThreadNum(){
		String threadstr = mAgingConfig.get("threads");
		return Integer.parseInt(threadstr);
	}

	@Override
	public void onResponse(InputStream resIn, InputStream errIn) {
		BufferedReader bufferedReader = null;
		try{
			InputStreamReader isr = new InputStreamReader(resIn);
			bufferedReader = new BufferedReader(isr);
			String line = null;
			int logCnt = 0;
			while(isRunning&&(line=bufferedReader.readLine())!=null){
				logCnt++;
				if(logCnt<=2) continue;
				//System.out.println(line);
				if(logCnt%15==0){
					int start = mDetailContent.indexOf("\n");
					mDetailContent.delete(0, start+1);
				}
				mDetailContent.append(line+"\n");
				mMemoryHandler.sendEmptyMessage(MSG_UPDATE_DETAIL);
				
				if(line.contains("Error")||(line.contains("error")&&!line.contains("errors"))||line.contains("FAIL")){
					isRunning = false;
					mAgingCallback.onFailed(AgingType.MEM);
					return;
				}
				
				if(line.contains("PASS")){
					break;
				}
			}
			if(isLoopTest()&&isRunning){
				mMemoryHandler.sendEmptyMessageDelayed(MSG_LOOP, 5000);
			}
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			if(bufferedReader!=null){
				try{
					bufferedReader.close();
				}catch(IOException e){
				}
			}
		}
	}
	
	@Override
	public void onFailed() {
		isRunning = false;
		mMemoryHandler.removeMessages(MSG_LOOP);
	}
	
	class MemoryHandler extends Handler {
		public void handleMessage(Message msg) {
			switch(msg.what){
			case MSG_UPDATE_DETAIL:
				mMemDetailText.setText(mDetailContent.toString());
				int offset = mMemDetailText.getLineCount()*mMemDetailText.getLineHeight()-mMemDetailText.getMeasuredHeight();
				if(offset>0)
					mMemDetailText.scrollTo(0, offset);
				else
					mMemDetailText.scrollTo(0, 0);
				break;
			case MSG_LOOP:
				mDetailContent = new StringBuilder();
				startTest();
				break;
			}
		}
	}

}
