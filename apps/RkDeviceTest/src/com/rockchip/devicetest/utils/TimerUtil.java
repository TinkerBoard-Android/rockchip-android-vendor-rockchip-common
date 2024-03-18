package com.rockchip.devicetest.utils;

public final class TimerUtil
{
	public final static void wait(int waitTime)
	{
		try {
			Thread.sleep(waitTime);
		}
		catch (Exception e) {}
	}

	public final static void waitRandom(int time)
	{
		int waitTime = (int)(Math.random() * (double)time);		
		try {
			Thread.sleep(waitTime);
		}
		catch (Exception e) {}
	}
}

