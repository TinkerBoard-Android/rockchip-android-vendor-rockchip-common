package com.rockchip.devicetest.enumerate;

import com.rockchip.devicetest.R;

public enum TestStatus {

	WAITING(1, R.string.test_status_wait),
	TESTING(2, R.string.test_status_doing),
	FINISHED(3, R.string.test_status_finish);
	
	private int state;
	private int resID;
	
	private TestStatus(int state, int resID){
		this.state = state;
		this.resID = resID;
	}

	public int getState() {
		return state;
	}

	public int getResID() {
		return resID;
	}
	
}
