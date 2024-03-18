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

package com.rockchip.devicetest.aging.busmon;

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

public class BusmonInfoReader {
	
	public static final String MAIN_VDD_12V_SYS_CUR = "/sys/class/hwmon/hwmon0/curr1_input";
	public static final String MAIN_VDD_12V_SYS_VOL = "/sys/class/hwmon/hwmon0/in1_input";
	public static final String MAIN_VDD_12V_SYS_LABEL = "/sys/class/hwmon/hwmon0/in1_label";
        public static final String MAIN_VDD_5V_SYS_CUR = "/sys/class/hwmon/hwmon0/curr2_input";
        public static final String MAIN_VDD_5V_SYS_VOL = "/sys/class/hwmon/hwmon0/in2_input";
        public static final String MAIN_VDD_5V_SYS_LABEL = "/sys/class/hwmon/hwmon0/in2_label";
        public static final String MAIN_VDD_3V_SYS_CUR = "/sys/class/hwmon/hwmon0/curr3_input";
        public static final String MAIN_VDD_3V_SYS_VOL = "/sys/class/hwmon/hwmon0/in3_input";
        public static final String MAIN_VDD_3V_SYS_LABEL = "/sys/class/hwmon/hwmon0/in3_label";

        public static final String MAIN_VDD_12V_CC_CUR = "/sys/class/hwmon/hwmon1/curr1_input";
        public static final String MAIN_VDD_12V_CC_VOL = "/sys/class/hwmon/hwmon1/in1_input";
        public static final String MAIN_VDD_12V_CC_LABEL = "/sys/class/hwmon/hwmon1/in1_label";
        public static final String MAIN_VDD_12V_DIS_CUR = "/sys/class/hwmon/hwmon1/curr2_input";
        public static final String MAIN_VDD_12V_DIS_VOL = "/sys/class/hwmon/hwmon1/in2_input";
        public static final String MAIN_VDD_12V_DIS_LABEL = "/sys/class/hwmon/hwmon1/in2_label";
        public static final String MAIN_AMP_PVCC_CUR = "/sys/class/hwmon/hwmon1/curr3_input";
        public static final String MAIN_AMP_PVCC_VOL = "/sys/class/hwmon/hwmon1/in3_input";
        public static final String MAIN_AMP_PVCC_LABEL = "/sys/class/hwmon/hwmon1/in3_label";

	
        /**
         * Get the VDD_12V_SYS Current
         * @return
         */
        public static int getBusmonVdd12vsysCur(){
                String current = FileUtils.readFromFile(new File(MAIN_VDD_12V_SYS_CUR));
                return StringUtils.parseInt(current, 50000);
        }

        /**
         * Get the VDD_12V_SYS voltage
         * @return
         */
        public static int getBusmonVdd12vsysVol(){
                String voltage = FileUtils.readFromFile(new File(MAIN_VDD_12V_SYS_VOL));
                return StringUtils.parseInt(voltage, 50000);
        }

        /**
         * Get the VDD_12V_SYS label
         * @return
         */
        public static String getBusmonVdd12vsysLabel(){
                return FileUtils.readFromFile(new File(MAIN_VDD_12V_SYS_LABEL));
        }

        /**
         * Get the VDD_5V_SYS Current
         * @return
         */
        public static int getBusmonVdd5vsysCur(){
                String current = FileUtils.readFromFile(new File(MAIN_VDD_5V_SYS_CUR));
                return StringUtils.parseInt(current, 50000);
        }

        /**
         * Get the VDD_5V_SYS voltage
         * @return
         */
        public static int getBusmonVdd5vsysVol(){
                String voltage = FileUtils.readFromFile(new File(MAIN_VDD_5V_SYS_VOL));
                return StringUtils.parseInt(voltage, 50000);
        }

        /**
         * Get the VDD_5V_SYS label
         * @return
         */
        public static String getBusmonVdd5vsysLabel(){
                return FileUtils.readFromFile(new File(MAIN_VDD_5V_SYS_LABEL));
        }

        /**
         * Get the VDD_3V_SYS Current
         * @return
         */
        public static int getBusmonVdd3vsysCur(){
                String current = FileUtils.readFromFile(new File(MAIN_VDD_3V_SYS_CUR));
                return StringUtils.parseInt(current, 50000);
        }

        /**
         * Get the VDD_3V_SYS voltage
         * @return
         */
        public static int getBusmonVdd3vsysVol(){
                String voltage = FileUtils.readFromFile(new File(MAIN_VDD_3V_SYS_VOL));
                return StringUtils.parseInt(voltage, 50000);
        }

        /**
         * Get the VDD_3V_SYS label
         * @return
         */
        public static String getBusmonVdd3vsysLabel(){
                return FileUtils.readFromFile(new File(MAIN_VDD_3V_SYS_LABEL));
        }

        /**
         * Get the VDD_12V_CC Current
         * @return
         */
        public static int getBusmonVdd12vccCur(){
                String current = FileUtils.readFromFile(new File(MAIN_VDD_12V_CC_CUR));
                return StringUtils.parseInt(current, 50000);
        }

        /**
         * Get the VDD_12V_CC voltage
         * @return
         */
        public static int getBusmonVdd12vccVol(){
                String voltage = FileUtils.readFromFile(new File(MAIN_VDD_12V_CC_VOL));
                return StringUtils.parseInt(voltage, 50000);
        }

        /**
         * Get the VDD_12V_CC label
         * @return
         */
        public static String getBusmonVdd12vccLabel(){
                return FileUtils.readFromFile(new File(MAIN_VDD_12V_CC_LABEL));
        }

        /**
         * Get the  VDD_12V_DIS Current
         * @return
         */
        public static int getBusmonVdd12vdisCur(){
                String current = FileUtils.readFromFile(new File(MAIN_VDD_12V_DIS_CUR));
                return StringUtils.parseInt(current, 50000);
        }

        /**
         * Get the VDD_12V_CC voltage
         * @return
         */
        public static int getBusmonVdd12vdisVol(){
                String voltage = FileUtils.readFromFile(new File(MAIN_VDD_12V_DIS_VOL));
                return StringUtils.parseInt(voltage, 50000);
        }

        /**
         * Get the VDD_12V_DIS label
         * @return
         */
        public static String getBusmonVdd12vdisLabel(){
                return FileUtils.readFromFile(new File(MAIN_VDD_12V_DIS_LABEL));
        }

        /**
         * Get the  AMP_PVCC Current
         * @return
         */
        public static int getBusmonAmppvccCur(){
                String current = FileUtils.readFromFile(new File(MAIN_AMP_PVCC_CUR));
                return StringUtils.parseInt(current, 50000);
        }

        /**
         * Get the AMP_PVCC voltage
         * @return
         */
        public static int getBusmonAmppvccVol(){
                String voltage = FileUtils.readFromFile(new File(MAIN_AMP_PVCC_VOL));
                return StringUtils.parseInt(voltage, 50000);
        }

        /**
         * Get the AMP_PVCC label
         * @return
         */
        public static String getBusmonAmppvccLabel(){
                return FileUtils.readFromFile(new File(MAIN_AMP_PVCC_LABEL));
        }

}
