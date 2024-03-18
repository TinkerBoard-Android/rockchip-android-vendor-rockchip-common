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
import com.rockchip.devicetest.aging.cpu.CpuInfoReader;
import com.rockchip.devicetest.aging.cpu.LinpackLoop;

import android.app.Activity;
import android.os.Handler;
import android.widget.TextView;

public class CpuTest extends BaseAgingTest {

	public static final int UPDATE_DELAY = 3000;
	private Activity mActivity;
	//private TextView mCpuModelText;
	private TextView mCpuCoreNumText;
	private TextView mCpuFreqText;
	private TextView mCpuCurrFreqText;
	private TextView mCpuUsageText;
	private boolean isRunning;
	private long[] mCpuInfo = new long[2];
	private Handler mMainHandler = new Handler();
	
	public CpuTest(AgingConfig config, AgingCallback agingCallback){
		super(config, agingCallback);
	}

	@Override
	public void onCreate(Activity activity) {
		mActivity = activity;
		//mCpuModelText = (TextView)mActivity.findViewById(R.id.tv_cpu_model);
		mCpuCoreNumText = (TextView)mActivity.findViewById(R.id.tv_cpu_corenum);
		mCpuFreqText = (TextView)mActivity.findViewById(R.id.tv_cpu_freq);
		mCpuCurrFreqText = (TextView)mActivity.findViewById(R.id.tv_cpu_currfreq);
		mCpuUsageText = (TextView)mActivity.findViewById(R.id.tv_cpu_usage);
		isRunning = true;
	}

	@Override
	public void onStart() {
		int coreNum = CpuInfoReader.getCpuCores();
		String freqRange = CpuInfoReader.getCpuMinFreq()/1000+"~"+CpuInfoReader.getCpuMaxFreq()/1000+" MHz";
		//mCpuModelText.setText(CpuInfoReader.getCpuModel());//CPU型号
		mCpuCoreNumText.setText(coreNum<=0?"Unknow":coreNum+"");//CPU核心数
		mCpuFreqText.setText(freqRange);//CPU频率范围
		mCpuCurrFreqText.setText(CpuInfoReader.getCpuCurrentFreq()/1000+" MHz");//CPU当前频率
		mMainHandler.postDelayed(mUpdateAction, 50);//CPU使用率
		for(int i=0; i<0; i++){
			new Thread(){
				public void run() {
					while(isRunning){
						LinpackLoop.main();
					}
				};
			}.start();
		}
	}
	
	private Runnable mUpdateAction = new Runnable(){
		public void run() {
			updateCpuUsage();
			mMainHandler.postDelayed(this, UPDATE_DELAY);
			mCpuCurrFreqText.setText(CpuInfoReader.getCpuCurrentFreq()/1000+" MHz");//CPU当前频率
		};
	};
	
	public void updateCpuUsage(){
		long[] cpuInfo = CpuInfoReader.getCpuTime();
		if(cpuInfo[0]==0||cpuInfo[1]==0){
			return;
		}
		if(mCpuInfo[0]==0||mCpuInfo[1]==0){
			mCpuInfo = cpuInfo;
			return;
		}
		long totalTime = cpuInfo[0]-mCpuInfo[0];
		long iddleTime = cpuInfo[1]-mCpuInfo[1];
		int percent = (int)((totalTime-iddleTime)*1.00f/totalTime*100);
		if(percent==0) percent = 1;
		mCpuUsageText.setText(percent+"%");
		mCpuInfo = cpuInfo;
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
