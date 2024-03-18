/*******************************************************************
* Company:     Fuzhou Rockchip Electronics Co., Ltd
* Description:   
* @author:     fxw@rock-chips.com
* Create at:   2014年5月16日 上午11:44:58  
* 
* Modification History:  
* Date         Author      Version     Description  
* ------------------------------------------------------------------  
* 2014年5月16日      fxw         1.0         create
*******************************************************************/   

package com.rockchip.devicetest.aging;

import com.rockchip.devicetest.utils.IniEditor;
import com.rockchip.devicetest.utils.IniEditor.Section;

public class AgingConfig {

	public static final String AGING_CPU = "cpu";
	public static final String AGING_MEM = "memory";
	public static final String AGING_GPU = "gpu";
	public static final String AGING_VPU = "vpu";
	public static final String AGING_USBHOST = "usbhost";
	public static final String AGING_ETHERNET = "ethernet";
	public static final String AGING_THERMAL = "thermal";
	public static final String AGING_BUSMON = "busmon";
	public static final String ACTIVATED = "1";
	private Section mSection;

	public AgingConfig(Section sec){
		mSection = sec;
	}
	
	public AgingConfig(IniEditor iniconfig, String section){
		mSection = iniconfig.getSection(section);
	}
	
	public boolean isActivated(){
		String cpuActivate = get("activated");
		return (ACTIVATED.equals(cpuActivate));
	}
	
	public boolean hasOption(String name) {
		return mSection.hasOption(name);
	}
	
	public String get(String option) {
		return mSection.get(option);
	}
	
}
