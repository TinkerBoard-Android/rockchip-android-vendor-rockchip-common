
package com.rockchip.devicetest.utils;

public class Mutex
{
	private boolean syncLock;
	
	////////////////////////////////////////////////
	//	Constructor
	////////////////////////////////////////////////

	public Mutex()
	{
		syncLock = false;
	}
	
	////////////////////////////////////////////////
	//	lock
	////////////////////////////////////////////////
	
	public synchronized void lock()
	{
		while(syncLock == true) {
			try {
				wait();
			}
			catch (Exception e) {
			};
		}
		syncLock = true;
	}
	
	/**
	 * 锁定当前线程
	 * add by xwf 2011026
	 */
	public synchronized void lockCurrent(){
		syncLock = true;
		try {
			wait();
		}
		catch (Exception e) {
		}
		syncLock = false;
	}
	public synchronized void lockCurrent(int millSec)
	{
		syncLock = true;
		try {
			wait(millSec);
		}
		catch (Exception e) {
		}
		syncLock = false;
	}

	public synchronized void unlock()
	{
		syncLock = false;
		notifyAll();
	}

}