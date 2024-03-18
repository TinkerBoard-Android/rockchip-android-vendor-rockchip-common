/*******************************************************************
* Company:     Fuzhou Rockchip Electronics Co., Ltd
* Description:   
* @author:     fxw@rock-chips.com
* Create at:   2014年5月14日 下午3:46:44  
* 
* Modification History:  
* Date         Author      Version     Description  
* ------------------------------------------------------------------  
* 2014年5月14日      fxw         1.0         create
*******************************************************************/   

package com.rockchip.devicetest.aging;

import android.app.Activity;

public interface IAgingTest {

	public void onCreate(Activity activity);
	public void onStart();
	public void onStop();
	public void onDestroy();
	public void onFailed();
	
}
