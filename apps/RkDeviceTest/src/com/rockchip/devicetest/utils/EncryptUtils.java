/*******************************************************************
* Company:     Fuzhou Rockchip Electronics Co., Ltd
* Description:   
* @author:     fxw@rock-chips.com
* Create at:   2014年5月21日 下午4:46:33  
* 
* Modification History:  
* Date         Author      Version     Description  
* ------------------------------------------------------------------  
* 2014年5月21日      fxw         1.0         create
*******************************************************************/   

package com.rockchip.devicetest.utils;

public class EncryptUtils {

	public static void encrypt(byte[] buf, int len){
		byte S[] = new byte[256];
		byte K[] = new byte[256];
		byte temp;
		int i,j,t,x;
		byte key[]={124,78,3,4,85,5,9,7,45,44,123,56,23,13,23,17};
		
		j = 0;
		for(i=0; i<256; i++){
			S[i] = (byte)i;
			j&=0x0f;
			K[i] = key[j];
			j++;
		}
		
		j = 0;
		for(i=0; i<256; i++){
			j = ((j + S[i] + K[i]) % 256)&0x0FF;
			temp = S[i];
			S[i] = S[j];
			S[j] = temp;
		}
		
		i = j = 0;
		for(x=0; x<len; x++){
			i = ((i+1) % 256)&0x0FF;
			j = ((j + S[i]) % 256)&0x0FF;
			temp = S[i];
			S[i] = S[j];
			S[j] = temp;
			t = ((S[i] + (S[j] % 256)) % 256)&0x0FF;
			buf[x] = (byte)(buf[x] ^ S[t]);
		}
	}
	
	public static void decrypt(byte[] buf, int len){
		encrypt(buf, len);
	}
	
}
