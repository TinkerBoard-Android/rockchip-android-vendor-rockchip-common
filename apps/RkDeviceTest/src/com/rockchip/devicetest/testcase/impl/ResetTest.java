/*******************************************************************
* Company:     Fuzhou Rockchip Electronics Co., Ltd
* Description:   
* @author:     fxw@rock-chips.com
* Create at:   2014年5月13日 上午9:06:23  
* 
* Modification History:  
* Date         Author      Version     Description  
* ------------------------------------------------------------------  
* 2014年5月13日      fxw         1.0         create
*******************************************************************/   

package com.rockchip.devicetest.testcase.impl;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Handler;
import android.view.KeyEvent;

import com.rockchip.devicetest.R;
import com.rockchip.devicetest.model.TestCaseInfo;
import com.rockchip.devicetest.testcase.BaseTestCase;

public class ResetTest extends BaseTestCase {

	public ResetTest(Context context, Handler handler, TestCaseInfo testcase) {
		super(context, handler, testcase);
	}
	
	@Override
	public boolean onTesting() {
		AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
		builder.setTitle(R.string.reset_title);
		builder.setMessage(R.string.reset_msg);
		builder.setNeutralButton(mContext.getString(R.string.pub_cancel), new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				onTestFail(0);
			}
		});
		builder.setCancelable(false);
		AlertDialog dialog = builder.create();
		dialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
			public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
				if(keyCode==KeyEvent.KEYCODE_VOLUME_UP){
					onTestSuccess();
					dialog.dismiss();
					return true;
				}
				return false;
			}
		});
		dialog.show();
		return true;
	}

}
