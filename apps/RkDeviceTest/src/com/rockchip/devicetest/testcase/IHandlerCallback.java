package com.rockchip.devicetest.testcase;

import com.rockchip.devicetest.model.TestResult;

public interface IHandlerCallback {

	public void onMessageHandled(BaseTestCase testcase, TestResult result);
	
}
