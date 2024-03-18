/*******************************************************************
 * Company:     Fuzhou Rockchip Electronics Co., Ltd
 * Description:   
 * @author:     fxw@rock-chips.com
 * Create at:   2014年5月15日 下午5:01:01  
 * 
 * Modification History:  
 * Date         Author      Version     Description  
 * ------------------------------------------------------------------  
 * 2014年5月15日      fxw         1.0         create
 *******************************************************************/

package com.rockchip.devicetest.aging.thermal;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.regex.Pattern;

import com.rockchip.devicetest.utils.FileUtils;
import com.rockchip.devicetest.utils.StringUtils;

public class ThermalInfoReader {
	
	public static final String JACK_THERMAL1_TEMP = "/sys/bus/i2c/devices/7-0049/hwmon/hwmon3/temp1_input";
	public static final String JACK_THERMAL2_TEMP = "/sys/bus/i2c/devices/7-0048/hwmon/hwmon4/temp1_input";
	
        /**
         * Get the jack thermal sensor1 temperature
         * @return
         */
        public static int getThermal1CurrentTemp(){
                String currtemp = FileUtils.readFromFile(new File(JACK_THERMAL1_TEMP));//cpuinfo_cur_freq
                return StringUtils.parseInt(currtemp, 10);
        }

        /**
         * Get the jack thermal sensor1 temperature
         * @return
         */
        public static int getThermal2CurrentTemp(){
                String currtemp = FileUtils.readFromFile(new File(JACK_THERMAL2_TEMP));//cpuinfo_cur_freq
                return StringUtils.parseInt(currtemp, 10);
        }

}
