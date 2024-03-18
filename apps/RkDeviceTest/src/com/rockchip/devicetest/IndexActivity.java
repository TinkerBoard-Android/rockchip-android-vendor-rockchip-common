package com.rockchip.devicetest;

import java.io.File;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import com.rockchip.devicetest.adapter.TestCaseArrayAdapter;
import com.rockchip.devicetest.constants.ParamConstants;
import com.rockchip.devicetest.enumerate.Commands;
import com.rockchip.devicetest.enumerate.TestResultType;
import com.rockchip.devicetest.enumerate.TestStatus;
import com.rockchip.devicetest.model.TestCaseInfo;
import com.rockchip.devicetest.model.TestResult;
import com.rockchip.devicetest.service.TestService;
import com.rockchip.devicetest.testcase.BaseTestCase;
import com.rockchip.devicetest.testcase.IHandlerCallback;
import com.rockchip.devicetest.testcase.TestCaseListView;
import com.rockchip.devicetest.testcase.TestCaseListView.ListViewLoadListener;
import com.rockchip.devicetest.utils.IniEditor;
import com.rockchip.devicetest.utils.IniEditor.Section;
import com.rockchip.devicetest.utils.LogUtil;
import com.rockchip.devicetest.utils.PropertiesUtils;
import com.rockchip.devicetest.utils.StringUtils;
import com.rockchip.devicetest.utils.TestConfigReader;
import com.rockchip.devicetest.utils.SystemInfoUtils;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.StatFs;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;
import android.os.storage.StorageEventListener;
import android.text.format.Formatter;
import android.util.Log;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

public class IndexActivity extends BaseActivity implements ListViewLoadListener {

	private TestApplication mApp;
	private LayoutInflater mLayoutInflater;
	private TestCaseListView mTestListView;
	private TextView mResultText;
	private List<TestCaseInfo> mTestCaseList;
	private List<BaseTestCase> mTestHandlerList;
	private Map<String, String> mTestHandlerConfig;
	private IniEditor mUserConfig;
	private Handler mMainHandler = new Handler();
	private boolean isRunningTask;
	private int mSeletedTestIndex;
	private StorageManager mStorageManager = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_index);
		mStorageManager = (StorageManager) getSystemService(Context.STORAGE_SERVICE);
		mStorageManager.registerListener(mStorageListener);
		mTestCaseList = new ArrayList<TestCaseInfo>();
		mTestHandlerList = new ArrayList<BaseTestCase>();
		//init handler config
		initHandlerConfig();
		initUserConfig();
		updateVersionInfo();
		mLayoutInflater = LayoutInflater.from(this);
		mTestListView = (TestCaseListView)findViewById(R.id.main_list_view);
		mTestListView.setOnItemClickListener(mOnItemClickListener);
		
		//Test case info
		View headerView = mLayoutInflater.inflate(R.layout.main_listheader, null);
		mTestListView.addHeaderView(headerView, null, false);
		TestCaseArrayAdapter arrayAdapter = new TestCaseArrayAdapter(this, mTestCaseList);
		arrayAdapter.setOnListViewLoadListener(this);
		mTestListView.setTestCaseAdapter(arrayAdapter);
		initTestCase(getIntent());
		
		//Result
		mResultText = (TextView)findViewById(R.id.tv_main_result);
		
		//version
		TextView softVersionText = (TextView)findViewById(R.id.tv_soft_ver);
		softVersionText.setText(SystemInfoUtils.getAppVersionName(this));
				
		//Activity is created, and be ready
		mApp = (TestApplication)getApplication();
		mApp.mIndexActivity = this;
		isRunningTask = false;
	}
	
	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		if(intent.getBooleanExtra("keytest", false)){//按键测试, Home键测试会启动activity
			return;
		}
		if(intent.hasExtra(TestService.EXTRA_KEY_TESTDATA)){
			mApp.setActivityReady(false);
			initTestCase(intent);
		}
	}
	
	protected void onStart() {
		super.onStart();
		mApp.setShowingApp(true);
	}
	
	protected void onResume() {
		super.onResume();
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		mMainHandler.removeCallbacksAndMessages(null);
		mApp.setActivityReady(false);
	}
	
	@Override
	protected void onStop() {
		super.onStop();
		mApp.setShowingApp(false);
	}
	
	protected void onDestroy() {
		super.onDestroy();
		if (mStorageManager != null) {
			mStorageManager.unregisterListener(mStorageListener);
		}
	}
	
	public void onListViewLoadCompleted(){
		if(!isRunningTask){
			isRunningTask = true;
			mApp.setActivityReady(true);
			startTest();
		}
	}
	
	
	/**
	 * 启动测试
	 */
	public void startTest(){
		mTestHandlerList.clear();
		mSeletedTestIndex = 0;
		mResultText.setVisibility(View.GONE);
		doFunctionTest(mSeletedTestIndex);
	}
	//执行测试
	@SuppressWarnings({"unchecked", "rawtypes"})
	private void doFunctionTest(int testIndex){
		if(testIndex>=0&&testIndex<mTestCaseList.size()){
			TestCaseInfo testInfo = mTestCaseList.get(testIndex);
			Commands cmd = testInfo.getCmd();
			try{
				String testclass = mTestHandlerConfig.get(cmd.getCommand().trim());
				if(StringUtils.isEmptyObj(testclass)){
					LogUtil.e(this, cmd.getCommand()+" 's Handler isn't exist. ");
					return;
				}
				Class cls = Class.forName(testclass);
				Constructor constructor = cls.getConstructor(Context.class, Handler.class, TestCaseInfo.class);
				
				BaseTestCase tescase = (BaseTestCase)constructor.newInstance(this, mMainHandler, testInfo);
				tescase.setTestCaseViewListener(mTestListView);
				tescase.setHandlerCallback(mHandlerCallback);
				testInfo.setAttachParams(getAttachedParams(cmd.getCommand()));
				tescase.onTestInit();
				tescase.onTesting();
				mTestHandlerList.add(tescase);
			}catch(Exception ex){
				LogUtil.e(this, "Test faild. ", ex);
			}
		}else{
			LogUtil.e(this, "Do test out of testlist.");
		}
	}
	
	/**
	 * 测试完成
	 */
	IHandlerCallback mHandlerCallback = new IHandlerCallback(){
		public void onMessageHandled(BaseTestCase testcase, TestResult result) {
			if(mSeletedTestIndex>=mTestCaseList.size()-1){//测试结束
				boolean ret = true;
				for(TestCaseInfo testInfo : mTestCaseList){
					if(testInfo.getResult()==TestResultType.FAIL){
						ret = false;
						break;
					}
				}
				if(ret){//测试成功
					mResultText.setVisibility(View.VISIBLE);
					try{//此文件用于烧写、写号工具判断是否已经过功能测试
						File passFile = new File(Environment.getExternalStorageDirectory(), "ftest_pass.bin");
						passFile.createNewFile();
					}catch(Exception e){
						LogUtil.e(this, "Failed to create ftest_pass.bin");
					}
				}
				saveFactoryTest(ret);
				LogUtil.d(this, "Test Finished. Result: "+ret);
			}else{
				//if(result.isSuccessed()){
					mSeletedTestIndex++;
					doFunctionTest(mSeletedTestIndex);
				//}
			}
		}
	};
	
	/**
	 * 保存功能测试结果
	 * @param result
	 */
	public void saveFactoryTest(boolean result){
		int mode = Context.MODE_PRIVATE|Context.MODE_MULTI_PROCESS;
		SharedPreferences sp = getSharedPreferences(TestService.SP_CONFIG_FILE, mode);
		sp.edit().putBoolean(TestService.SP_KEY_FACTORY, result).commit();
	}
	
	/**
	 * 初始化用户配置信息
	 */
	private void initUserConfig(){
		File factoryFile = ConfigFinder.findConfigFile(TestService.FILE_FACTORY_TEST,this);
		mUserConfig = new IniEditor();
		if(factoryFile==null||!factoryFile.exists()){
			return;
		}
		TestConfigReader configReader = new TestConfigReader();
		mUserConfig = configReader.loadConfig(factoryFile);
	}
	
	/**
	 * 初始化命令消息和处理者配置
	 */
	private void initHandlerConfig(){
		Properties props = PropertiesUtils.getProperties(this, "testconfig.properties");
		mTestHandlerConfig = new HashMap<String, String>();
		for(Entry<Object, Object> entry : props.entrySet()){
			mTestHandlerConfig.put((String)entry.getKey(), (String)entry.getValue());
		}
	}
	
	/**
	 * 初始化测试项
	 */
	private void initTestCase(Intent indexIntent){
		Intent serviceIntent = new Intent();
		serviceIntent.setClass(this, TestService.class);
		serviceIntent.putExtra(TestService.EXTRA_KEY_TESTFROM, "app");
		startService(serviceIntent);
		ArrayList<String> cmdList = null;
		if(indexIntent!=null){
			cmdList = indexIntent.getStringArrayListExtra(TestService.EXTRA_KEY_TESTDATA);
		}
		mTestCaseList.clear();
		//远程PC端控制入口进入
		if(cmdList!=null&&cmdList.size()>0){
			for(String cmd : cmdList){
				TestCaseInfo testcase = new TestCaseInfo();
				testcase.setCmd(cmd);
				testcase.setStatus(TestStatus.WAITING);
				mTestCaseList.add(testcase);
			}
		}
		//本地端入口进入
		else{// all testcases default
			List<String> mTestItemList = mUserConfig.sectionNames();
			//int keyCodeStart = KeyEvent.KEYCODE_1;
			for(String item : mTestItemList){
				Map<String, String> attachedParams = getAttachedParams(item);
				if(!ParamConstants.ENABLED.equals(attachedParams.get(ParamConstants.ACTIVATED))){
					continue;
				}
				TestCaseInfo testcase = new TestCaseInfo();
				testcase.setCmd(Commands.getType(item));
				testcase.setStatus(TestStatus.WAITING);
				testcase.setAttachParams(attachedParams);
				String testKey = attachedParams.get(ParamConstants.TEST_KEY);
				if(testKey!=null&&testKey.length()==1){
					KeyCharacterMap keyCharacterMap = KeyCharacterMap.load(KeyCharacterMap.VIRTUAL_KEYBOARD);
					KeyEvent[] keyEvents = keyCharacterMap.getEvents(testKey.toCharArray());
					if(keyEvents!=null&&keyEvents.length>=1){
						testcase.setTestKeycode(keyEvents[0].getKeyCode());
						testcase.setTestKeychar(testKey);
					}else{
						testcase.setTestKeychar("OK");
					}
				}else{
					testcase.setTestKeychar("OK");
				}
				
				//testcase.setTestKeycode(keyCodeStart);
				mTestCaseList.add(testcase);
				/*
				keyCodeStart++;
				if(keyCodeStart>KeyEvent.KEYCODE_9){
					keyCodeStart = KeyEvent.KEYCODE_0;//KEYCODE 不够用
				}*/
			}
		}
		mTestListView.setDataSource(mTestCaseList);
	}
	
	/**
	 * 构建版本信息
	 */
	private void updateVersionInfo(){
		TextView snText = (TextView)findViewById(R.id.tv_sn);
		TextView macText = (TextView)findViewById(R.id.tv_mac);
		TextView modelText = (TextView)findViewById(R.id.tv_model);
		TextView versionText = (TextView)findViewById(R.id.tv_version);
		TextView ramText = (TextView)findViewById(R.id.tv_ram);
		TextView flashText = (TextView)findViewById(R.id.tv_flash);
		modelText.setText(Build.PRODUCT);
		versionText.setText(Build.DISPLAY);
                snText.setText(Build.SERIAL);
                macText.setText(SystemInfoUtils.getMac(this));
		ramText.setText(SystemInfoUtils.getFormattedRamSpace(this));
		//flash
		flashText.setText(SystemInfoUtils.getFormattedFlashSpace(this));
		/*
		String flashPath = Environment.getExternalStorageDirectory().getAbsolutePath();
		String status = mStorageManager.getVolumeState(flashPath);
		if(Environment.MEDIA_MOUNTED.equals(status)||Environment.MEDIA_MOUNTED_READ_ONLY.equals(status)){
			
		}*/
		/*
		mVersionInfoList = new ArrayList<VersionInfo>();
		VersionInfo verInfo = new VersionInfo();
		verInfo.setVerName(getString(R.string.main_version_model));
		verInfo.setVerValue(Build.PRODUCT);
		mVersionInfoList.add(verInfo);
		
		verInfo = new VersionInfo();
		verInfo.setVerName(getString(R.string.main_version_android));
		verInfo.setVerValue(Build.DISPLAY);
		mVersionInfoList.add(verInfo);
		
		verInfo = new VersionInfo();
		verInfo.setVerName(getString(R.string.main_version_kernel));
		verInfo.setVerValue(VersionInfoUtils.getFormattedKernelVersion());
		mVersionInfoList.add(verInfo);*/
	}
	StorageEventListener mStorageListener = new StorageEventListener() {
		public void onStorageStateChanged(String path, String oldState,
				String newState) {
			if (Environment.MEDIA_MOUNTED.equals(newState)){
				if(Environment.getExternalStorageDirectory().getAbsolutePath().equals(path)){
					updateVersionInfo();
				}
			}
		}
	};

	
	/**
	 * 获取每个命令的附加参数
	 * @param cmd
	 * @return
	 */
	public Map<String, String> getAttachedParams(String cmd){
		if(mUserConfig==null){
			return new HashMap<String, String>();
		}
		Section section = mUserConfig.getSection(cmd.trim());
		if(section==null){
			return new HashMap<String, String>();
		}
		return section.options();
	}
	
	/**
	 * 若当前测试用例列表中已存在此case, 则重置信息并返回
	 * 若不存在, 则新建一个TestCaseInfo
	 * @param command
	 * @return
	 */
	public TestCaseInfo createOrGetTestCaseInfo(Commands command){
		for(TestCaseInfo info : mTestCaseList){
			if(info.getCmd()==command){
				info.reset();
				return info;
			}
		}
		return new TestCaseInfo();
	}
	
	//重测处理
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if(isTestting()){
			Log.d("IndexActivity", "It is testing. ");
			return super.onKeyDown(keyCode, event);
		}
		
		if(keyCode>KeyEvent.KEYCODE_0&&keyCode<=KeyEvent.KEYCODE_9){
			for(BaseTestCase testcase : mTestHandlerList){
				TestCaseInfo testInfo = testcase.getTestCaseInfo();
				if(testInfo.getTestKeycode()==keyCode){
					if(testInfo.getResult()==TestResultType.FAIL){
						testcase.onTestInit();
						testcase.onTesting();
						return true;
					}
				}
			}
		}
		return super.onKeyDown(keyCode, event);
	}
	
	AdapterView.OnItemClickListener mOnItemClickListener = new AdapterView.OnItemClickListener(){
		public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
			if(isTestting()){
				Log.d("IndexActivity", "It is testing. ");
				return;
			}
			
			position = position-1;//Header
			if(position>=0&&position<mTestCaseList.size()){
				TestCaseInfo testInfo = mTestCaseList.get(position);
				BaseTestCase currentTestcase = null;
				for(BaseTestCase testcase : mTestHandlerList){
					TestCaseInfo testInfo2 = testcase.getTestCaseInfo();
					if(testInfo2.getCmd()==testInfo.getCmd()){
						currentTestcase = testcase;
						break;
					}
				}
				if(currentTestcase==null) return;
				if(!currentTestcase.isTesting()&&testInfo.getResult()==TestResultType.FAIL){
					currentTestcase.onTestInit();
					currentTestcase.onTesting();
				}
			}
		}
	};
	
	//是否有其他测试项正在测试
	public boolean isTestting(){
		for(BaseTestCase testcase : mTestHandlerList){
			if(testcase.isTesting()){
				return true;
			}
		}
		return false;
	}
	
}




