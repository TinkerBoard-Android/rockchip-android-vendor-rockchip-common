package com.rockchip.devicetest.testcase;

import java.io.File; 
import com.rockchip.devicetest.service.TestService;
import com.rockchip.devicetest.utils.IniEditor;
import com.rockchip.devicetest.utils.LogUtil;
import com.rockchip.devicetest.utils.SystemInfoUtils;
import com.rockchip.devicetest.utils.TestConfigReader;
import com.rockchip.devicetest.ConfigFinder;
import com.rockchip.devicetest.utils.FileUtils;
import android.content.Context;
import android.os.SystemProperties;

public class LEDSettings {

	public enum LEDMode {
		ON("3"),
		OFF("0");
		
		public String value;
		private LEDMode(String value){
			this.value = value;
		}
		
		public static LEDMode getMode(String mode){
			for(LEDMode um : LEDMode.values()){
				if(um.value.equals(mode)){
					return um;
				}
			}
			return null;
		}
	}

    //private static final String SYS_LED_FILE = "/sys/class/leds/power-green/brightness";

    /**
     * 获得当前灯节点路径
     */
    public static String getSysLedFile(Context mContext){
    	String SYS_LED_FILE = "/sys/class/leds/power-green/brightness";;
    	File factoryFile = ConfigFinder.findConfigFile(TestService.FILE_AGING_TEST,mContext);
        IniEditor mUserConfig = new IniEditor();
        if(factoryFile!=null&&factoryFile.exists()){
                  TestConfigReader configReader = new TestConfigReader();
                  mUserConfig = configReader.loadConfig(factoryFile);
                  String led_file = mUserConfig.get("LED", "led_file");
                  if(led_file!=null)
                         SYS_LED_FILE = led_file;
        }
        String led_path = SystemProperties.get("persist.sys.leds_path","unknow");
        if(!"unknow".equals(led_path)){
                  SYS_LED_FILE = led_path;
        }
    	System.out.println("============SYS_LED_FILE=="+SYS_LED_FILE);
    	
    	return SYS_LED_FILE;
    }
    
    /**
     * 开灯
     */
    public static boolean onLed(Context mContext){
		return setLedMode(LEDMode.ON,mContext);
    }
    
    /**
     * 关灯
     */
    public static boolean offLed(Context mContext){
    	return setLedMode(LEDMode.OFF,mContext);
    }
    
    /**
     * 修改LED状态
     */
    public static boolean setLedMode(LEDMode mode,Context mContext){
    	File file = new File(getSysLedFile(mContext));
    	return FileUtils.write2File(file, mode.value);
    }
    

}
