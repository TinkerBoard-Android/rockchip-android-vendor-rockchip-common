/*******************************************************************
* Company:     Fuzhou Rockchip Electronics Co., Ltd
* Description:   
* @author:     fxw@rock-chips.com
* Create at:   2014年5月14日 下午2:25:05  
* 
* Modification History:  
* Date         Author      Version     Description  
* ------------------------------------------------------------------  
* 2014年5月14日      fxw         1.0         create
*******************************************************************/   

package com.rockchip.devicetest;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

import com.rockchip.devicetest.aging.AgingCallback;
import com.rockchip.devicetest.aging.AgingConfig;
import com.rockchip.devicetest.aging.AgingDelegate;
import com.rockchip.devicetest.aging.CpuTest;
import com.rockchip.devicetest.aging.GpuTest;
import com.rockchip.devicetest.aging.MemoryTest;
import com.rockchip.devicetest.aging.VpuTest;
import com.rockchip.devicetest.aging.UsbhostTest;
import com.rockchip.devicetest.aging.EthernetTest;
import com.rockchip.devicetest.aging.TestProcess;
import com.rockchip.devicetest.aging.ThermalTest;
import com.rockchip.devicetest.aging.BusmonTest;
import com.rockchip.devicetest.aging.FailLists;
import com.rockchip.devicetest.constants.ResourceConstants;
import com.rockchip.devicetest.enumerate.AgingType;
import com.rockchip.devicetest.service.TestService;
import com.rockchip.devicetest.service.WatchdogService;
import com.rockchip.devicetest.utils.IniEditor;
import com.rockchip.devicetest.utils.LogUtil;
import com.rockchip.devicetest.utils.SystemInfoUtils;
import com.rockchip.devicetest.utils.TestConfigReader;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnDismissListener;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.widget.TextView;
import android.widget.Toast;

public class AgingTestActivity extends BaseActivity implements AgingCallback {

    public static final String AGINGTEST_FOREGROUND_ACTION = "com.rockchip.devicetest.state.foreground";
    public static final String AGINGTEST_BACKGROUND_ACTION = "com.rockchip.devicetest.state.background";
	private TestApplication mApp;
	private AgingDelegate mAgingDelegate;
	private IniEditor mIniConfig;
	private Handler mMainHandler;
	private int mKeyBackCount;
	private boolean hasPassedFactory;
	private Toast mBackToast;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.test_aging);
		mMainHandler = new Handler();
		Intent serviceIntent = new Intent();//启动后台服务
		serviceIntent.setClass(this, TestService.class);
		serviceIntent.putExtra(TestService.EXTRA_KEY_TESTFROM, "app");
		startService(serviceIntent);
		mApp = (TestApplication)getApplication();
		mAgingDelegate = new AgingDelegate();
		
		hasPassedFactory = hadPassFactoryTest();
		if(!hasPassedFactory){
			return;
		}
		initAgingDelegate();
		mAgingDelegate.onCreate(this);
		
		//version
		TextView softVersionText = (TextView)findViewById(R.id.tv_soft_ver2);
		softVersionText.setText(SystemInfoUtils.getAppVersionName(this));
		
	}
	
	/**
	 * 初始化测试项
	 */
	private void initAgingDelegate(){
		//read config
		InputStream in = null;
		try{
			if (Arrays.asList(getAssets().list("")).contains(ResourceConstants.AGING_CONFIG_PRODUCT_FILE))
				in = getAssets().open(ResourceConstants.AGING_CONFIG_PRODUCT_FILE);
			else
				in = getAssets().open(ResourceConstants.AGING_CONFIG_FILE);
			mIniConfig = new IniEditor();
			mIniConfig.load(in);
		}catch(Exception e){
			e.printStackTrace();
			LogUtil.show(this, "Read test config failed");
			return;
		}finally{
			if(in!=null){
				try {
					in.close();
				} catch (IOException e) {
				}
			}
		}
		//cpu
		AgingConfig agingConfig = new AgingConfig(mIniConfig, AgingConfig.AGING_CPU);
		if(agingConfig.isActivated()){
			mAgingDelegate.addAgingTest(new CpuTest(agingConfig, this));
		}

                //TestProcess
                mAgingDelegate.addAgingTest(new TestProcess(agingConfig, this));

                //FailLists
                mAgingDelegate.addAgingTest(new FailLists(agingConfig, this));

		//memory
                if(getUserConfig(AgingConfig.AGING_MEM)!=null)
                        agingConfig = new AgingConfig(getUserConfig(AgingConfig.AGING_MEM), AgingConfig.AGING_MEM);
                else
	 	        agingConfig = new AgingConfig(mIniConfig, AgingConfig.AGING_MEM);
		if(agingConfig.isActivated()){
			mAgingDelegate.addAgingTest(new MemoryTest(agingConfig, this));
		}
		//gpu
		agingConfig = new AgingConfig(mIniConfig, AgingConfig.AGING_GPU);
		if(agingConfig.isActivated()){
			mAgingDelegate.addAgingTest(new GpuTest(agingConfig, this));
		}

                //usbhost
                agingConfig = new AgingConfig(mIniConfig, AgingConfig.AGING_USBHOST);
                if(agingConfig.isActivated()){
                        mAgingDelegate.addAgingTest(new UsbhostTest(agingConfig, this));
                }

                //Lan
                agingConfig = new AgingConfig(mIniConfig, AgingConfig.AGING_ETHERNET);
                if(agingConfig.isActivated()){
                        mAgingDelegate.addAgingTest(new EthernetTest(agingConfig, this));
                }

                //Thermal
                agingConfig = new AgingConfig(mIniConfig, AgingConfig.AGING_THERMAL);
                if(agingConfig.isActivated()){
                        mAgingDelegate.addAgingTest(new ThermalTest(agingConfig, this));
                }

                agingConfig = new AgingConfig(mIniConfig, AgingConfig.AGING_BUSMON);
                if(agingConfig.isActivated()){
                        mAgingDelegate.addAgingTest(new BusmonTest(agingConfig, this));
                }
		//vpu
                if(getUserConfig(AgingConfig.AGING_VPU)!=null)
                        agingConfig = new AgingConfig(getUserConfig(AgingConfig.AGING_VPU), AgingConfig.AGING_VPU);
                else
		        agingConfig = new AgingConfig(mIniConfig, AgingConfig.AGING_VPU);
		if(agingConfig.isActivated()){
			mAgingDelegate.addAgingTest(new VpuTest(agingConfig, this));
		}
	}
	
	
	@Override
	protected void onStart() {
		super.onStart();
		mApp.setShowingApp(true);
		if(!hasPassedFactory){
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle(R.string.pub_prompt);
			builder.setMessage(R.string.aging_test_check);
			builder.setPositiveButton(getString(R.string.pub_success), new OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					finish();
				}
			});
			builder.setCancelable(false);
			builder.create().show();
			return;
		}
		
		//Disable home power
	//	Intent keyIntent = new Intent(AGINGTEST_FOREGROUND_ACTION);
	//	sendBroadcast(keyIntent);
		
		//Intent agingIntent = new Intent();//启动后台服务
		//agingIntent.setAction("com.rockchip.devicetest.action.WATCH_DOG");
		//agingIntent.putExtra(WatchdogService.COMMAND, WatchdogService.CMD_START_AGING);
		//agingIntent.setClass(this, WatchdogService.class);
		//startService(agingIntent);
		
		mMainHandler.postDelayed(new Runnable() {
			public void run() {
				mAgingDelegate.onStart();
			}
		}, 30);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
	}
	
	@Override
	protected void onPause() {
		super.onPause();
                finish();
	}
	
	protected void onStop() {
		super.onStop();
		mApp.setShowingApp(false);
		if(!hasPassedFactory){
			return;
		}
		mAgingDelegate.onStop();
		//Disable home power
	//	Intent keyIntent = new Intent(AGINGTEST_BACKGROUND_ACTION);
	//	sendBroadcast(keyIntent);
	}
	
	protected void onDestroy() {
		super.onDestroy();
		if(!hasPassedFactory){
			return;
		}
		mAgingDelegate.onDestroy();
	}
         /**
         * 获取是否已有测试配置
         */
        private IniEditor getUserConfig(String test){
               File factoryFile = ConfigFinder.findConfigFile(TestService.FILE_AGING_TEST,this);
               IniEditor mUserConfig = new IniEditor();
               if(factoryFile!=null&&factoryFile.exists()){
                        TestConfigReader configReader = new TestConfigReader();
                        mUserConfig = configReader.loadConfig(factoryFile);
                        String activated = mUserConfig.get(test, "activated");
                        if(activated!=null){
                                return mUserConfig;
                        }
                }
                        return null;
        }        
	
	/**
	 * 获取是否已通过功能测试
	 */
	private boolean hadPassFactoryTest(){
		File factoryFile = ConfigFinder.findConfigFile(TestService.FILE_AGING_TEST,this);
		IniEditor mUserConfig = new IniEditor();
		if(factoryFile!=null&&factoryFile.exists()){
			TestConfigReader configReader = new TestConfigReader();
			mUserConfig = configReader.loadConfig(factoryFile);
			String required = mUserConfig.get("FactoryTest", "required");
			if("0".equals(required)){//不需要通过工厂测试
				return true;
			}
		}
		
		File passFile = new File(Environment.getExternalStorageDirectory(), "ftest_pass.bin");
		if(passFile.exists()){
			return true;
		}
		
		int mode = Context.MODE_PRIVATE|Context.MODE_MULTI_PROCESS;
		SharedPreferences sp = getSharedPreferences(TestService.SP_CONFIG_FILE, mode);
		return sp.getBoolean(TestService.SP_KEY_FACTORY, true);
	}
	

	/**
	 * 测试失败
	 */
	public void onFailed(AgingType type) {
		mAgingDelegate.onFailed();
		//Intent agingIntent = new Intent();//启动后台服务
		//agingIntent.setAction("com.rockchip.devicetest.action.WATCH_DOG");
		//agingIntent.putExtra(WatchdogService.COMMAND, WatchdogService.CMD_STOP_AGING);
		//agingIntent.setClass(this, WatchdogService.class);
		//startService(agingIntent);
		//LogUtil.d(this, "Aging test failed. "+type.getClass());
		//Disable home power
	//	Intent keyIntent = new Intent(AGINGTEST_BACKGROUND_ACTION);
	//	sendBroadcast(keyIntent);
	}
	
	@Override
	public void onBackPressed() {
		/*
		mKeyBackCount++;
		mMainHandler.removeCallbacks(mKeyBackAction);
		mMainHandler.postDelayed(mKeyBackAction, 3000);
		final int totalCnt = 5;
		if(mKeyBackCount>=totalCnt)
			super.onBackPressed();
		else{
			if(mBackToast==null){
				mBackToast = Toast.makeText(this, getString(R.string.aging_test_back, totalCnt-mKeyBackCount), Toast.LENGTH_SHORT);
			}
			mBackToast.setText(getString(R.string.aging_test_back, totalCnt-mKeyBackCount));
			mBackToast.show();
		}
		*/
	}
	Runnable mKeyBackAction = new Runnable() {
		public void run() {
			mKeyBackCount = 0;
		}
	};
	
}
