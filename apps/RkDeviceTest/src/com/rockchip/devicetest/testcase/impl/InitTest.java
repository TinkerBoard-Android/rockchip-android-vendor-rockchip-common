/*******************************************************************
* Company:     Fuzhou Rockchip Electronics Co., Ltd
* Description:   
* @author:     fxw@rock-chips.com
* Create at:   2014年5月9日 上午9:22:29  
* 
* Modification History:  
* Date         Author      Version     Description  
* ------------------------------------------------------------------  
* 2014年5月9日      fxw         1.0         create
*******************************************************************/   

package com.rockchip.devicetest.testcase.impl;

import android.content.Context;
import android.os.Handler;

import com.rockchip.devicetest.model.TestCaseInfo;
import com.rockchip.devicetest.testcase.BaseTestCase;

public class InitTest extends BaseTestCase {

	public InitTest(Context context, Handler handler, TestCaseInfo testcase) {
		super(context, handler, testcase);
	}
	
	/* (non-Javadoc)
	 * @see com.rockchip.devicetest.testcase.BaseTestCase#onTestInit()
	 */
	@Override
	public void onTestInit() {
		super.onTestInit();
	}

}
