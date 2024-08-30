package com.cghs.stresstest.util;

public class ArmFreqInfo {

	private int armFreq;
	
	private String freqShow;

	public ArmFreqInfo(int armFreq, String freqShow) {
		super();
		this.armFreq = armFreq;
		this.freqShow = freqShow;
	}

	public int getArmFreq() {
		return armFreq;
	}

	public void setArmFreq(int armFreq) {
		this.armFreq = armFreq;
	}

	public String getFreqShow() {
		return freqShow;
	}

	public void setFreqShow(String freqShow) {
		this.freqShow = freqShow;
	}
}
