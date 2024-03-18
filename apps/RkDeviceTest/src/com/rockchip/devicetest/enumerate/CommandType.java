/*******************************************************************
* Company:     Fuzhou Rockchip Electronics Co., Ltd
* Description:   
* @author:     fxw@rock-chips.com
* Create at:   2014年5月9日 下午3:49:56  
* 
* Modification History:  
* Date         Author      Version     Description  
* ------------------------------------------------------------------  
* 2014年5月9日      fxw         1.0         create
*******************************************************************/   

package com.rockchip.devicetest.enumerate;

import com.rockchip.devicetest.constants.TypeConstants;

public enum CommandType {

	CMD(TypeConstants.TYPE_CMD),
	ACK(TypeConstants.TYPE_ACK),
	DATA(TypeConstants.TYPE_DATA),
	RDY(TypeConstants.TYPE_RDY),
	SYNC(TypeConstants.TYPE_SYNC);
	
	private int cmdType;
	
	private CommandType(int type){
		this.cmdType = type;
	}
	
	public static CommandType getType(int cmdtype){
		for(CommandType cmd : CommandType.values()){
			if(cmdtype==cmd.getCmdType()){
				return cmd;
			}
		}
		return null;
	}
	
	/**
	 * @return the cmdType
	 */
	public int getCmdType() {
		return cmdType;
	}
}
