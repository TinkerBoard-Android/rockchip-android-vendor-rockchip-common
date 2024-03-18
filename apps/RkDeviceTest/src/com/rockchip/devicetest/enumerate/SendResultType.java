/*******************************************************************
* Company:     Fuzhou Rockchip Electronics Co., Ltd
* Description:   
* @author:     fxw@rock-chips.com
* Create at:   2014年5月8日 下午4:08:59  
* 
* Modification History:  
* Date         Author      Version     Description  
* ------------------------------------------------------------------  
* 2014年5月8日      fxw         1.0         create
*******************************************************************/   

package com.rockchip.devicetest.enumerate;

import com.rockchip.devicetest.R;

public enum SendResultType {

	UNSEND(0, "UNSEND", R.string.pub_unsend),
	SUCCESS(1, "OKAY", R.string.pub_success),
	FAIL(2, "FAIL", R.string.pub_fail);
	
	private int id;
	private String result;
	private int resID;
	
	private SendResultType(int id, String result, int resID){
		this.id = id;
		this.result = result;
		this.resID = resID;
	}
	
	/**
	 * @return the id
	 */
	public int getId() {
		return id;
	}
	
	/**
	 * @return the result
	 */
	public String getResult() {
		return result;
	}
	
	public int getResID() {
		return resID;
	}
	
	public static TestResultType getTypeByID(int id){
		for(TestResultType tr : TestResultType.values()){
			if(id==tr.getId()){
				return tr;
			}
		}
		return null;
	}
	
	public static TestResultType getType(String result){
		if(result==null) return null;
		for(TestResultType tr : TestResultType.values()){
			if(result.equals(tr.getResult())){
				return tr;
			}
		}
		return null;
	}
	
}
