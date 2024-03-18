/*******************************************************************
* Company:     Fuzhou Rockchip Electronics Co., Ltd
* Description:   
* @author:     fxw@rock-chips.com
* Create at:   2014年5月8日 下午3:24:40  
* 
* Modification History:  
* Date         Author      Version     Description  
* ------------------------------------------------------------------  
* 2014年5月8日      fxw         1.0         create
*******************************************************************/   

package com.rockchip.devicetest.enumerate;

import com.rockchip.devicetest.R;
import com.rockchip.devicetest.constants.TypeConstants;


public enum Commands {

	CMD_WIFI(TypeConstants.CMD_WIFI, R.string.cmd_wifi),
	CMD_LAN(TypeConstants.CMD_LAN, R.string.cmd_lan),
	CMD_SD(TypeConstants.CMD_SD, R.string.cmd_sd),
	CMD_HDMI(TypeConstants.CMD_HDMI, R.string.cmd_hdmi),
	CMD_CVBS(TypeConstants.CMD_CVBS, R.string.cmd_cvbs),
	CMD_CHNL(TypeConstants.CMD_CHNL, R.string.cmd_chnl),
	CMD_LED(TypeConstants.CMD_LED, R.string.cmd_led),
	//CMD_CNTL(TypeConstants.CMD_CNTL, R.string.cmd_cntl),
	CMD_REST(TypeConstants.CMD_REST, R.string.cmd_rest),
	CMD_USB(TypeConstants.CMD_USB, R.string.cmd_usb),
	CMD_MIC(TypeConstants.CMD_MIC, R.string.cmd_mic),
	CMD_BT(TypeConstants.CMD_BT, R.string.cmd_bt),
	//CMD_RDSN(TypeConstants.CMD_RDSN, 0),
	CMD_CKSN(TypeConstants.CMD_CKSN, 0/*R.string.cmd_cksn*/),
	CMD_TEST(TypeConstants.CMD_TEST, 0),
	CMD_BEAT(TypeConstants.CMD_BEAT, 0);
	
	private String command;
	private int resID;
	
	private Commands(String command, int resID){
		this.command = command;
		this.resID = resID;
	}
	
	public String getCommand() {
		return command;
	}

	public int getResID() {
		return resID;
	}
	//忽略大小写
	public static Commands getType(String cmdstr){
		if(cmdstr==null) return null;
		for(Commands cmd : Commands.values()){
			if(cmdstr.equalsIgnoreCase(cmd.getCommand().trim())){
				return cmd;
			}
		}
		return null;
	}
}
