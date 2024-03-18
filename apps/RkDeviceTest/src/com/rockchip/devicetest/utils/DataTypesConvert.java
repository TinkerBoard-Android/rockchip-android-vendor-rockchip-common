/*******************************************************************
* Company:     Fuzhou Rockchip Electronics Co., Ltd
* Filename:    DataTypesConvert.java  
* Description:   
* @author:     fxw@rock-chips.com
* Create at:   2011-11-17 下午03:33:56  
* 
* Modification History:  
* Date         Author      Version     Description  
* ------------------------------------------------------------------  
* 2011-11-17      xwf         1.0         create
*******************************************************************/   


package com.rockchip.devicetest.utils;

public class DataTypesConvert {

	/**
	 * 将byte数组转换为int(默认小端模式)
	 * @param data
	 * @param start 含
	 * @param end 含
	 * @return
	 */
	public static int changeByteToInt(byte[] data, int start, int end) {
		int result = 0;
		/*
		for (int i = start; i <= end; i++) {
			int tmp = data[i];
			if (tmp < 0) {
				result = result + (256 + tmp)
						* (int) Math.pow(256, (end - i));
			} else {
				result = result + tmp
						* (int) Math.pow(256, (end - i));
			}
		}*/
		
		for (int i = end; i >= start; i--) {
			int tmp = data[i];
			if (tmp < 0) {
				result = result + (256 + tmp)
						* (int) Math.pow(256, (i - start));
			} else {
				result = result + tmp
						* (int) Math.pow(256, (i - start));
			}
		}
		return result;
	}

	/**
	 * sortType 1 表示从高位到低位 2 表示从低位到高位
	 * 
	 * @param bArray
	 * @return
	 */
	public static long changeByteToLong(byte[] data, int startNum, int endNum,
			int sortType) {
		long TNumber = 0;
		if (sortType == 1)
			for (int i = startNum; i <= endNum; i++) {
				int tmp = data[i];
				if (tmp < 0) {
					TNumber = TNumber + (256 + tmp)
							* (long) Math.pow(256, (endNum - i));
				} else {
					TNumber = TNumber + tmp
							* (long) Math.pow(256, (endNum - i));
				}
			}
		else
			for (int i = endNum; i >= startNum; i--) {
				int tmp = data[i];
				if (tmp < 0) {
					TNumber = TNumber + (256 + tmp)
							* (long) Math.pow(256, (i - startNum));
				} else {
					TNumber = TNumber + tmp
							* (long) Math.pow(256, (i - startNum));
				}
			}
		return TNumber;
	}
	
	/**
	 * 从消息体中获取DATA字段
	 * @return
	 */
	public static byte[] fetchData(byte[] msg, int start, int end){
		if(start>end) return null;
		byte[] tData = new byte[end-start+1];
		for(int i=start,j=0; i<=end; i++,j++){
			tData[j] = msg[i];
		}
		return tData;
	}
	
	/**
	 * 填充DATA字段
	 * @return
	 */
	public static void fillData(byte[] src, byte[] dst, int start, int end){
		if(start>end) return;
		
		int cpyLen = end-start+1;
		if(cpyLen<=src.length)
			System.arraycopy(src, 0, dst, start, cpyLen);
	}
	
}
