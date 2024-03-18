/*******************************************************************
* Company:     Fuzhou Rockchip Electronics Co., Ltd
* Description:   
* @author:     fxw@rock-chips.com
* Create at:   2014年5月14日 上午11:12:27  
* 
* Modification History:  
* Date         Author      Version     Description  
* ------------------------------------------------------------------  
* 2014年5月14日      fxw         1.0         create
*******************************************************************/   

package com.rockchip.devicetest.testcase.impl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.List;

import android.content.Context;
import android.os.Handler;

import com.rockchip.devicetest.ConfigFinder;
import com.rockchip.devicetest.R;
import com.rockchip.devicetest.model.TestCaseInfo;
import com.rockchip.devicetest.testcase.BaseTestCase;
import com.rockchip.devicetest.utils.StorageUtils;

public class UsbTest extends BaseTestCase {

    private static final String TEST_STRING = "Rockchip UsbHostTest File";
    private StringBuilder mDetailInfo;
    private Context mContext;

	public UsbTest(Context context, Handler handler, TestCaseInfo testcase) {
		super(context, handler, testcase);
		mDetailInfo = new StringBuilder();
                mContext = context;
	}
	
	@Override
	public void onTestInit() {
		super.onTestInit();
	}
	
	public boolean onTesting() {
		List<String> usbPathList = ConfigFinder.getAliveUsbPath(mContext);
		if(usbPathList.isEmpty()){
			onTestFail(R.string.usb_err_unmount);
        	return false;
		}
		boolean result = true;
		int usize = usbPathList.size();
		mDetailInfo.delete(0, mDetailInfo.length());
		for(int i=0; i<usize; i++){
			boolean testRes = testUsbDevice(usbPathList.get(i), usize==1?0:i+1);
			if(testRes==false){
				result = false;
			}
		}
		if(result){
			onTestSuccess(mDetailInfo.toString());
		}else{
			onTestFail(mDetailInfo.toString());
		}
		return result;
	}
	

    
    //测试失败
    public void onTestFail(int errResID, int usbIndex) {
    	if(usbIndex==0){//只有一个U盘
    		mDetailInfo.append(getString(errResID));
    	}else{
    		mDetailInfo.append(getString(R.string.cmd_usb)+usbIndex+": "+getString(errResID)+". ");
    	}
    }
    //测试成功
    public void onTestSuccess(int usbIndex) {
    	if(usbIndex==0){//只有一个U盘
    		mDetailInfo.append(getString(R.string.pub_success));
    	}else{
    		mDetailInfo.append(getString(R.string.cmd_usb)+usbIndex+": "+getString(R.string.pub_success)+". ");
    	}
    }

    /**
     * 测试USB
     * @return
     */
    public boolean testUsbDevice(String usbPath, int usbIndex) {
        Process process;
        String temp;
        Runtime runtime = Runtime.getRuntime();
        try {
            process = runtime.exec("/system/bin/ls "+usbPath);
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            while ((temp = reader.readLine()) != null) {
                if (temp.startsWith("udisk") && !temp.equals("udisk")) {
                	usbPath += "/"+temp;
                    process.destroy();
                    reader.close();
                    return testReadAndWrite(usbPath, usbIndex);
                }
            }
            return testReadAndWrite(usbPath, usbIndex);
        } catch (IOException e) {
            e.printStackTrace();
            onTestFail(R.string.pub_exception, usbIndex);
            return false;
        }
    }

    public boolean testReadAndWrite(String usbPath, int usbIndex) {
        return dotestReadAndWrite(usbPath, usbIndex);
    }

    private boolean dotestReadAndWrite(String usbPath, int usbIndex) {
        String directoryName = usbPath + "/rktest";

        File directory = new File(directoryName);
        if (!directory.isDirectory()) { // Create Test Dir
            if (!directory.mkdirs()) {
            	onTestFail(R.string.sd_err_mkdir, usbIndex);
                return false;
            }
        }
        File f = new File(directoryName, "UsbHostTest.txt");
        try {
            // Remove stale file if any
            if (f.exists()) {
                f.delete();
            }
            if (!f.createNewFile()) { // Create Test File
            	onTestFail(R.string.sd_err_mkfile, usbIndex);
                return false;
            } else {
            	boolean writeResult = doWriteFile(f.getAbsoluteFile().toString());
    			if(!writeResult){
    				onTestFail(R.string.sd_err_write, usbIndex);
    				return false;
    			}
    			String readResult = doReadFile(f.getAbsoluteFile().toString());
    			if(readResult==null){
    				onTestFail(R.string.sd_err_read, usbIndex);
    				return false;
    			}
    			if(!readResult.equals(TEST_STRING)) {
    				onTestFail(R.string.sd_err_match, usbIndex);
    				return false;
    			}
    			onTestSuccess(usbIndex);
    			return true;
            }
        } catch (IOException ex) {
        	onTestFail(R.string.pub_exception, usbIndex);
            return false;
        } finally{
			if (f.exists()) {
				f.delete();
			}
			if (directory.exists()) {
				directory.delete();
			}
		}
    }

    /**
	 * 写入测试数据
	 * @param filename
	 * @return
	 */
	public boolean doWriteFile(String filename) {
		try {
			OutputStreamWriter osw = new OutputStreamWriter(new FileOutputStream(filename));
			osw.write(TEST_STRING, 0, TEST_STRING.length());
			osw.flush();
			osw.close();
			return true;
		} catch (IOException e) {
			return false;
		}
	}

	/**
	 * 读取测试数据
	 * @param filename
	 * @return
	 */
	public String doReadFile(String filename) {
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(
					new FileInputStream(filename)));
			String data = null;
			StringBuilder temp = new StringBuilder();
			while ((data = br.readLine()) != null) {
				temp.append(data);
			}
			br.close();
			return temp.toString();
		} catch (Exception e) {
			return null;
		}
	}

}
