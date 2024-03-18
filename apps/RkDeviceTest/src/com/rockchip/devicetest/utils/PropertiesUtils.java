/*******************************************************************
* Company:     Fuzhou Rockchip Electronics Co., Ltd
* Description:   
* @author:     fxw@rock-chips.com
* Create at:   2014年5月5日 下午6:15:24  
* 
* Modification History:  
* Date         Author      Version     Description  
* ------------------------------------------------------------------  
* 2014年5月5日      fxw         1.0         create
*******************************************************************/   

package com.rockchip.devicetest.utils;

import java.io.InputStream;
import java.util.Properties;

import android.content.Context;

public class PropertiesUtils {
	
	public static Properties getProperties(Context context, String fileName){             
		Properties props = new Properties();             
		try {            
			InputStream in = context.getAssets().open(fileName);//PropertiesUtill.class.getResourceAsStream("/assets/  setting.properties "));
			props.load(in);
			in.close();
		} catch (Exception e) { 
		}
		return props;
	}
}
