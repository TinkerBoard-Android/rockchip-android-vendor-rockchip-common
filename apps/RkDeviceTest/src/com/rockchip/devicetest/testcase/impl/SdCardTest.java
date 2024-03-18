/*******************************************************************
* Company:     Fuzhou Rockchip Electronics Co., Ltd
* Description:   
* @author:     fxw@rock-chips.com
* Create at:   2014年5月5日 下午5:52:00  
* 
* Modification History:  
* Date         Author      Version     Description  
* ------------------------------------------------------------------  
* 2014年5月5日      fxw         1.0         create
*******************************************************************/   

package com.rockchip.devicetest.testcase.impl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import android.content.Context;

import com.rockchip.devicetest.R;
import com.rockchip.devicetest.model.TestCaseInfo;
import com.rockchip.devicetest.model.TestResult;
import com.rockchip.devicetest.testcase.BaseTestCase;
import com.rockchip.devicetest.utils.StorageUtils;

import android.os.Environment;
import android.os.Handler;
import android.os.Message;

import android.os.storage.StorageVolume;
//import android.os.storage.IMountService;
import android.os.storage.StorageManager;
import android.os.storage.StorageEventListener;
import android.util.Log;

public class SdCardTest extends BaseTestCase {

	public static final int INDEX_SDCARD = 1;
	
	private static final String TEST_STRING = "Rockchip UsbHostTest File";
	private StorageManager mStorageManager = null;
	private String sdcard_path;
	
	public SdCardTest(Context context, Handler handler, TestCaseInfo testcase) {
		super(context, handler, testcase);
		mStorageManager = (StorageManager) mContext.getSystemService(Context.STORAGE_SERVICE);
	}
	
	public void onTestInit() {
		super.onTestInit();
                sdcard_path = StorageUtils.getSDcardDir(mStorageManager);
	/*	StorageVolume[] storageVolumes = mStorageManager.getVolumeList();
		if(storageVolumes.length>1)
			sdcard_path = storageVolumes[INDEX_SDCARD].getPath();
		else
			sdcard_path = Environment.getSecondVolumeStorageDirectory().getAbsolutePath();
         */
	}

	public boolean onTesting() {
		return testSdcard();
	}
	
	@Override
	public boolean onTestHandled(TestResult result) {
		return super.onTestHandled(result);
	}
	
	/**
	 * 测试SDCARD
	 * @return
	 */
	public boolean testSdcard() {
		try {
                        Log.d("hjc","testSdcard sdcard_path:"+sdcard_path);
                        if(sdcard_path==null){
                           onTestFail(R.string.sd_err_unmount);
                                return false;
                        }
			String externalVolumeState = mStorageManager
                                                     .getVolumeState(sdcard_path.replace("/mnt/media_rw/","/storage/"));
			if (!externalVolumeState.equals(Environment.MEDIA_MOUNTED)) {
				onTestFail(R.string.sd_err_unmount);
				return false;
			}
			
			return testReadAndWrite(sdcard_path);
		} catch (Exception rex) {
			onTestFail(R.string.pub_exception);
			return false;
		}
	}

	public boolean testReadAndWrite(String directoryName) {
		return dotestReadAndWrite(directoryName);
	}

	private boolean dotestReadAndWrite(String directoryName) {
		File directory = new File(directoryName + "/rktest");
		if (!directory.isDirectory()) {
			if (!directory.mkdirs()) {
				onTestFail(R.string.sd_err_mkdir);
				return false;
			}
		}
		File f = new File(directoryName, "storagetest.txt");
		try {
			if (f.exists()) {
				f.delete();
			}
			if (!f.createNewFile()) {
				onTestFail(R.string.sd_err_mkfile);
				return false;
			} 
			boolean writeResult = doWriteFile(f.getAbsoluteFile().toString());
			if(!writeResult){
				onTestFail(R.string.sd_err_write);
				return false;
			}
			String readResult = doReadFile(f.getAbsoluteFile().toString());
			if(readResult==null){
				onTestFail(R.string.sd_err_read);
				return false;
			}
			if(!readResult.equals(TEST_STRING)) {
				onTestFail(R.string.sd_err_match);
				return false;
			}
			onTestSuccess();
			return true;
		} catch (IOException ex) {
			onTestFail(R.string.pub_exception);
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
