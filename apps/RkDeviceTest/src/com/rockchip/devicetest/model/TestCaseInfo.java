/*******************************************************************
* Company:     Fuzhou Rockchip Electronics Co., Ltd
* Description:   
* @author:     fxw@rock-chips.com
* Create at:   2014年5月4日 下午3:59:23  
* 
* Modification History:  
* Date         Author      Version     Description  
* ------------------------------------------------------------------  
* 2014年5月4日      fxw         1.0         create
*******************************************************************/   

package com.rockchip.devicetest.model;

import java.util.Map;

import com.rockchip.devicetest.enumerate.Commands;
import com.rockchip.devicetest.enumerate.SendResultType;
import com.rockchip.devicetest.enumerate.TestResultType;
import com.rockchip.devicetest.enumerate.TestStatus;

public class TestCaseInfo {
	
	private TestStatus status;
	private TestResultType result;
	private SendResultType sendResult;
	private String detail;
	private int testKeycode;
	private String testKeychar;
	
	private Commands cmd;//命令字
	private int param1;//参数1
	private int param2;//参数2
	private Map<String, String> attachParams;
	
	public void reset(){
		status = null;
		result = null;
		sendResult = null;
		detail = null;
		param1 = 0;
		param2 = 0;
		attachParams = null;
	}
	
	/**
	 * @return the cmd
	 */
	public Commands getCmd() {
		return cmd;
	}
	/**
	 * @param cmd the cmd to set
	 */
	public void setCmd(Commands cmd) {
		this.cmd = cmd;
	}
	public void setCmd(String str) {
		this.cmd = Commands.getType(str);
	}
	
	/**
	 * @return the param1
	 */
	public int getParam1() {
		return param1;
	}
	/**
	 * @param param1 the param1 to set
	 */
	public void setParam1(int param1) {
		this.param1 = param1;
	}
	/**
	 * @return the param2
	 */
	public int getParam2() {
		return param2;
	}
	/**
	 * @param param2 the param2 to set
	 */
	public void setParam2(int param2) {
		this.param2 = param2;
	}
	/**
	 * @return the attachParams
	 */
	public Map<String, String> getAttachParams() {
		return attachParams;
	}
	/**
	 * @param attachParams the attachParams to set
	 */
	public void setAttachParams(Map<String, String> attachParams) {
		this.attachParams = attachParams;
	}
	
	public TestResultType getResult() {
		return result;
	}
	
	public int getResultResID() {
		if(result==null)
			return 0;
		return result.getResID();
	}

	public void setResult(TestResultType result) {
		this.result = result;
	}
	
	public TestStatus getStatus() {
		return status;
	}

	public void setStatus(TestStatus status) {
		this.status = status;
	}

	public SendResultType getSendResult() {
		return sendResult;
	}

	public void setSendResult(SendResultType sendResult) {
		this.sendResult = sendResult;
	}

	/**
	 * @return the detail
	 */
	public String getDetail() {
		return detail;
	}
	/**
	 * @param detail the detail to set
	 */
	public void setDetail(String detail) {
		this.detail = detail;
	}

	public int getTestKeycode() {
		return testKeycode;
	}

	public void setTestKeycode(int testKeycode) {
		this.testKeycode = testKeycode;
	}

	/**
	 * @return the testKeychar
	 */
	public String getTestKeychar() {
		return testKeychar;
	}

	/**
	 * @param testKeychar the testKeychar to set
	 */
	public void setTestKeychar(String testKeychar) {
		this.testKeychar = testKeychar;
	}
	
}
