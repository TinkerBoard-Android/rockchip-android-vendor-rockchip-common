/*******************************************************************
* Company:     Fuzhou Rockchip Electronics Co., Ltd
* Description:   
* @author:     fxw@rock-chips.com
* Create at:   2014年5月9日 下午8:47:00  
* 
* Modification History:  
* Date         Author      Version     Description  
* ------------------------------------------------------------------  
* 2014年5月9日      fxw         1.0         create
*******************************************************************/   

package com.rockchip.devicetest.model;

public class TestResult {

	private boolean successed;
	private byte[] data;

	/**
	 * @return the successed
	 */
	public boolean isSuccessed() {
		return successed;
	}

	/**
	 * @param successed the successed to set
	 */
	public void setSuccessed(boolean successed) {
		this.successed = successed;
	}

	/**
	 * @return the data
	 */
	public byte[] getData() {
		return data;
	}

	/**
	 * @param data the data to set
	 */
	public void setData(byte[] data) {
		this.data = data;
	}
	
}
