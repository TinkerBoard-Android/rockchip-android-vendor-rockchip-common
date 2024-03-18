/*******************************************************************
* Company:     Fuzhou Rockchip Electronics Co., Ltd
* Description:   
* @author:     fxw@rock-chips.com
* Create at:   2014年5月5日 下午5:51:31  
* 
* Modification History:  
* Date         Author      Version     Description  
* ------------------------------------------------------------------  
* 2014年5月5日      fxw         1.0         create
*******************************************************************/   

package com.rockchip.devicetest.testcase;

import android.content.Context;
import android.os.Handler;

import com.rockchip.devicetest.R;
import com.rockchip.devicetest.enumerate.TestResultType;
import com.rockchip.devicetest.enumerate.TestStatus;
import com.rockchip.devicetest.model.TestCaseInfo;
import com.rockchip.devicetest.model.TestResult;

public abstract class BaseTestCase implements ITestCase {

	public static final int DEFAULT_TEST_TIMEOUT = 8000;
	protected Context mContext;
	protected TestCaseInfo mTestCaseInfo;
	private IHandlerCallback mHandlerCallback;
	protected TestCaseViewListener mViewListener;
	private boolean isTesting;
	protected Handler mMainHandler = null;
	
	public BaseTestCase(Context context, Handler handler, TestCaseInfo testcase){
		mContext = context;
		mTestCaseInfo = testcase;
		mMainHandler = handler;
	}
	
	public void setHandlerCallback(IHandlerCallback handlerCallback){
		mHandlerCallback = handlerCallback;
	}
	
	public void setTestCaseViewListener(TestCaseViewListener listener){
		mViewListener = listener;
	}
	
	/**
	 * 测试初始化
	 */
	public void onTestInit() {
		isTesting = true;
		mTestCaseInfo.setStatus(TestStatus.TESTING);
		mTestCaseInfo.setResult(null);
		mTestCaseInfo.setSendResult(null);
		mTestCaseInfo.setDetail(null);
		mViewListener.onTestUIUpdate(mTestCaseInfo);
	}

	/**
	 * 正在测试
	 */
	public boolean onTesting() {
		return true;
	}

	/**
	 * 处理完成
	 */
	public boolean onTestHandled(TestResult result) {
		clearTestTimeout();
		mTestCaseInfo.setStatus(TestStatus.FINISHED);
		mViewListener.onTestUIUpdate(mTestCaseInfo);
		if(mHandlerCallback!=null){//测试处理完成
			mHandlerCallback.onMessageHandled(this, result);
		}
		isTesting = false;
		return true;
	}
	
	/**
	 * 停止测试
	 */
	public void stop(){
		clearTestTimeout();
		isTesting = false;
	}
	
	/**
	 * 设置测试超时时间
	 * @param millsec
	 */
	public void setTestTimeout(int millsec){
		mMainHandler.removeCallbacks(mTimeOutAction);
		mMainHandler.postDelayed(mTimeOutAction, millsec);
	}
	private Runnable mTimeOutAction = new Runnable(){
		public void run(){
			onTestFail(R.string.pub_time_out);
		}
	};
	public void clearTestTimeout(){
		mMainHandler.removeCallbacks(mTimeOutAction);
	}
	
	public boolean isTesting() {
		return isTesting;
	}

	/**
	 * 测试成功
	 */
	public void onTestSuccess(){
		TestResult result = new TestResult();
		result.setSuccessed(true);
		mTestCaseInfo.setResult(TestResultType.SUCCESS);
		onTestHandled(result);
	}
	public void onTestSuccess(final String detail){
		mTestCaseInfo.setDetail(detail);
		onTestSuccess();
	}
	
	/**
	 * 测试失败
	 */
	public void onTestFail(final int errResID){
		onTestFail(errResID>0?getString(errResID):"");
	}
	public void onTestFail(final String detail){
		TestResult result = new TestResult();
		result.setSuccessed(false);
		mTestCaseInfo.setResult(TestResultType.FAIL);
		mTestCaseInfo.setDetail(detail);
		onTestHandled(result);
	}
	
	/**
	 * 仅更新Detail
	 */
	public void updateDetail(String detail){
		mTestCaseInfo.setDetail(detail);
		mViewListener.onTestUIUpdate(mTestCaseInfo);
	}
	
	protected String getString(int resID){
		return mContext.getString(resID);
	}
	
	public TestCaseInfo getTestCaseInfo(){
		return mTestCaseInfo;
	}
	
	public interface TestCaseViewListener {
		
		public void onTestUIUpdate(TestCaseInfo testInfo);
		
	}
	
}
