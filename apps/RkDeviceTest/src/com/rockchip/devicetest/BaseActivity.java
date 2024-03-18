/*******************************************************************
* Company:     Fuzhou Rockchip Electronics Co., Ltd
* Description:   
* @author:     fxw@rock-chips.com
* Create at:   2014��4��15�� ����4:42:08  
* 
* Modification History:  
* Date         Author      Version     Description  
* ------------------------------------------------------------------  
* 2014��4��15��      fxw         1.0         create
*******************************************************************/   

package com.rockchip.devicetest;



import com.rockchip.devicetest.utils.WindowHelper;

import android.app.Activity;
import android.os.Bundle;

public class BaseActivity extends Activity {
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		WindowHelper.setFullScreen(getWindow());
		//getWindow().setBackgroundDrawableResource(R.drawable.main_bg);
	}
}
