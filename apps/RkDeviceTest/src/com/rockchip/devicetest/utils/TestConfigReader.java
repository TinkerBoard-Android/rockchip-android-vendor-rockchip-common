/*******************************************************************
* Company:     Fuzhou Rockchip Electronics Co., Ltd
* Description:   
* @author:     fxw@rock-chips.com
* Create at:   2014年5月21日 下午5:08:51  
* 
* Modification History:  
* Date         Author      Version     Description  
* ------------------------------------------------------------------  
* 2014年5月21日      fxw         1.0         create
*******************************************************************/   

package com.rockchip.devicetest.utils;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;

public class TestConfigReader {
	
	public static final int MIN_FILE_SIZE = 12;

	public IniEditor loadConfig(String file){
		return loadConfig(new File(file));
	}
	public IniEditor loadConfig(File file){
		IniEditor configLoader = new IniEditor();
		byte[] fdata = FileUtils.readFileContent(file);
		if(fdata==null||fdata.length<MIN_FILE_SIZE){
			LogUtil.e(this, "Read config error. ");
			return configLoader;
		}
		//check header
		byte[] head = DataTypesConvert.fetchData(fdata, 0, 3);
		if(!"SIGN".equals(new String(head))){
			LogUtil.e(this, "Config header error. ");
			return configLoader;
		}
		
		//content length
		int flen = DataTypesConvert.changeByteToInt(fdata, 4, 7);
		if(flen+MIN_FILE_SIZE!=fdata.length){
			LogUtil.e(this, "Config file length error. ");
			return configLoader;
		}
		
		//content
		byte[] cdata = DataTypesConvert.fetchData(fdata, 8, 8+flen-1);
		if(flen==0||cdata==null||cdata.length==0){
			LogUtil.e(this, "Config file length is zero. ");
			return configLoader;
		}
		EncryptUtils.decrypt(cdata, flen);//decrypt
		//LogUtil.d(this, new String(cdata));
		try {
			configLoader.load(new ByteArrayInputStream(cdata));
		} catch (IOException e) {
			LogUtil.e(this, "Parse ini file error. ");
		}
		return configLoader;
	}
	
	
}
