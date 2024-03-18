/*******************************************************************
* Company:     Fuzhou Rockchip Electronics Co., Ltd
* Description:   
* @author:     fxw@rock-chips.com
* Create at:   2014年5月14日 下午3:51:27  
* 
* Modification History:  
* Date         Author      Version     Description  
* ------------------------------------------------------------------  
* 2014年5月14日      fxw         1.0         create
*******************************************************************/   

package com.rockchip.devicetest.aging;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;

public class AgingDelegate implements IAgingTest {
	
	private List<IAgingTest> agingTestList = null;
	
	public AgingDelegate(){
		agingTestList = new ArrayList<IAgingTest>();
	}
	
	public void addAgingTest(IAgingTest test){
		agingTestList.add(test);
	}
	
	public void onCreate(Activity activity){
		for(IAgingTest test : agingTestList){
			test.onCreate(activity);
		}
	}
	
	public void onStart(){
		for(IAgingTest test : agingTestList){
			test.onStart();
		}
	}
	
	public void onStop(){
		for(IAgingTest test : agingTestList){
			test.onStop();
		}
	}
	
	public void onDestroy(){
		for(IAgingTest test : agingTestList){
			test.onDestroy();
		}
	}

	@Override
	public void onFailed() {
		for(IAgingTest test : agingTestList){
			test.onFailed();
		}
	}

}
