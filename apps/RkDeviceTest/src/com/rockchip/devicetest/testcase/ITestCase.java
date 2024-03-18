/*******************************************************************
* Company:     Fuzhou Rockchip Electronics Co., Ltd
* Description:   
* @author:     fxw@rock-chips.com
* Create at:   2014年5月4日 下午3:46:40  
* 
* Modification History:  
* Date         Author      Version     Description  
* ------------------------------------------------------------------  
* 2014年5月4日      fxw         1.0         create
*******************************************************************/   

package com.rockchip.devicetest.testcase;

import com.rockchip.devicetest.model.TestResult;

public interface ITestCase {
	
	/**
	 * 测试初始化
	 */
	public void onTestInit();

	/**
	 * 测试中
	 */
	public boolean onTesting();
	
	
	/**
	 * 测试结束
	 */
	public boolean onTestHandled(TestResult result);
	
}
