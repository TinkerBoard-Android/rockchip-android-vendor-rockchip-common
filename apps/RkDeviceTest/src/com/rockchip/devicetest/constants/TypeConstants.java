/*******************************************************************
* Company:     Fuzhou Rockchip Electronics Co., Ltd
* Description:   
* @author:     fxw@rock-chips.com
* Create at:   2014年5月4日 下午5:04:20  
* 
* Modification History:  
* Date         Author      Version     Description  
* ------------------------------------------------------------------  
* 2014年5月4日      fxw         1.0         create
*******************************************************************/   

package com.rockchip.devicetest.constants;

public class TypeConstants {
	
	//Command
	public static final String CMD_WIFI = "WIFI";//Wifi
	public static final String CMD_LAN = "LAN ";//Lan
	public static final String CMD_SD = "SD  ";//Sdcard
	public static final String CMD_USB = "USB ";//USB
	public static final String CMD_HDMI = "HDMI";//HDMI
	public static final String CMD_CVBS = "CVBS";//CVBS
	public static final String CMD_CHNL = "CHNL";//左右声道
	public static final String CMD_LED = "LED ";//LED
	public static final String CMD_CNTL = "CNTL";//遥控器
	public static final String CMD_REST = "REST";//Reset
	public static final String CMD_RDSN = "RDSN";//验号
	public static final String CMD_TEST = "TEST";//功能测试
	public static final String CMD_BEAT = "BEAT";//心跳包
	public static final String CMD_CKSN = "CKSN";//验号?
	public static final String CMD_MIC = "MIC";//麦克风
	public static final String CMD_BT = "BT";//蓝牙
	
	//CommandType
	public static final int TYPE_CMD = 0;
	public static final int TYPE_ACK = 1;
	public static final int TYPE_RDY = 2;;
	public static final int TYPE_DATA = 3;;
	public static final int TYPE_SYNC = 4;
	
	//Socket
	public static final int DEFAULT_SERVER_PORT = 3066;
	public static final int SERVER_TIME_OUT = 1800000;//1800s
	public static final int CONNECT_TIME_OUT = 10000;//10s
	public static final int READ_TIME_OUT = 4000;//4s
	
	//Packet接收缓冲区大小
	public static final int RECV_MESSAGE_BUFSIZE = 256;
	public static final int MIN_MESSAGE_SIZE = 9;
	public static final int MAX_MESSAGE_SIZE = 1024;

}
