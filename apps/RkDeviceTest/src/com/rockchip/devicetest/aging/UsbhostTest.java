/*******************************************************************
* Company:     Fuzhou Rockchip Electronics Co., Ltd
* Description:   
* @author:     fxw@rock-chips.com
* Create at:   2014年5月14日 下午5:42:07  
* 
* Modification History:  
* Date         Author      Version     Description  
* ------------------------------------------------------------------  
* 2014年5月14日      fxw         1.0         create
*******************************************************************/   

package com.rockchip.devicetest.aging;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

import com.rockchip.devicetest.ConfigFinder;
import com.rockchip.devicetest.R;
import com.rockchip.devicetest.aging.cpu.CpuInfoReader;
import com.rockchip.devicetest.aging.cpu.LinpackLoop;
import com.rockchip.devicetest.enumerate.AgingType;
import com.rockchip.devicetest.utils.FileUtils;
import com.rockchip.devicetest.utils.LogUtil;
import com.rockchip.devicetest.utils.SystemBinUtils;
import com.rockchip.devicetest.utils.SystemBinUtils.CommandResponseListener;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.method.ScrollingMovementMethod;
import android.view.ViewGroup;
import android.widget.TextView;
import android.content.Context;
import android.content.Intent;

public class UsbhostTest extends BaseAgingTest {

	public static final int UPDATE_DELAY = 3000;
	private static final String TEST_STRING = "ASUS UsbHostTest File";
	private static final String TESTPROCESS_ACTION="com.rockchip.devicetest.aging.TestProcess";
	private static final String FAILLISTS_ACTION="com.rockchip.devicetest.aging.FailLists";
	public static final String USBHOST_TEST_BIN = "stressapptest";
	private Activity mActivity;
	private boolean isRunning;
	private Handler mMainHandler = new Handler();
	private StringBuilder mDetailContent;
	private int mTestCount;
	private int mTestTimes = 6;
        private static final String pass = " -> PASS";
        private static final String fail = " -> FAIL";
        private static final String functions = "UsbHost Test:  ";
        List<Integer> usbTestfailreport;
	List<String> usbPathList;
        List<Integer> usbTestresult;
        List<Integer> usbTestfailcounter;
        List<String> usbTestcontent;
        private int usize;

	private String mTestcontent = "";
        private Intent mIntenttestprocess;
        private Intent mIntentfailreport;

	public UsbhostTest(AgingConfig config, AgingCallback agingCallback){
		super(config, agingCallback);
	}
	
	@Override
	public void onCreate(Activity activity) {
		mActivity = activity;
		mDetailContent = new StringBuilder();
                mIntenttestprocess = new Intent(TESTPROCESS_ACTION);
                mIntentfailreport = new Intent(FAILLISTS_ACTION);
	}

	@Override
	public void onStart() {
		isRunning = true;
		mMainHandler.postDelayed(mUpdateAction, UPDATE_DELAY);
		usbPathList = ConfigFinder.getAliveUsbPath(mActivity.getApplicationContext());
		usize = usbPathList.size();
                usbTestresult = newListWithDefault(1, usize);
                usbTestfailcounter = newListWithDefault(0, usize);
                usbTestcontent = newListWithDefault("", usize);
		usbTestfailreport = newListWithDefault(0, usize);
	}

        private static <T> List<T> newListWithDefault(T value, int size) {
               List<T> list = new ArrayList<>(size);
               for (int i = 0; i < size; i++) {
                       list.add(value);
               }
               return list;
        }

        private Runnable mUpdateAction = new Runnable(){
                public void run() {
                		for(int i=0; i<usize; i++){
					boolean testRes = false;
					if (!(usbTestfailcounter.get(i) >= mTestTimes)) {
		                        	testRes = testUsbDevice(usbPathList.get(i), usize==1?0:i+1);
                	                        if(testRes == false && !usbTestfailcounter.equals(mTestTimes)){
                                        	        usbTestcontent.set(i, mTestcontent + "fail counter="+ (usbTestfailcounter.get(i)+1));
                                                	onTestProcessFail(functions, usbPathList.get(i)+ " " + usbTestcontent.get(i));
							usbTestfailcounter.set(i, usbTestfailcounter.get(i)+1);
	
        	                                } else {
                        	                        mDetailContent.append(usbPathList.get(i)+ "Test Pass" +"\n");
                                	                onTestProcessSuccess(functions, usbPathList.get(i));
							usbTestfailcounter.set(i, 0);
                                       	 	}

					} else {
		                                onTestProcessFail(functions, usbPathList.get(i) + "keep fail! times: "+ usbTestfailcounter.get(i));
                		                if (usbTestfailreport.get(i).equals(0)) {
                                		        onFailListsreport(functions, usbPathList.get(i) + ":  ");
                                        		usbTestfailreport.set(i, usbTestfailcounter.get(i)+1);
                                		}
					}
					mTestcontent = "";
                		}
                        mMainHandler.postDelayed(this, UPDATE_DELAY);
                };
        };



	@Override
	public void onStop() {
		isRunning = false;
	}

	@Override
	public void onDestroy() {

	}

        private void onTestProcessFail(String test_functions, String test_content) {
                mIntenttestprocess.putExtra("test_functions", test_functions);
                mIntenttestprocess.putExtra("test_result", fail);
                mIntenttestprocess.putExtra("test_content", test_content);
                mActivity.getApplicationContext().sendBroadcast(mIntenttestprocess);

        }

        private void onTestProcessSuccess(String test_functions, String test_content) {
                mIntenttestprocess.putExtra("test_functions", test_functions);
                mIntenttestprocess.putExtra("test_result", pass);
                mIntenttestprocess.putExtra("test_content", test_content);
                mActivity.getApplicationContext().sendBroadcast(mIntenttestprocess);

        }

        private void onFailListsreport(String test_functions, String test_content) {
                mIntentfailreport.putExtra("test_functions", test_functions);
                mIntentfailreport.putExtra("test_result", fail);
                mIntentfailreport.putExtra("test_content", test_content);
                mActivity.getApplicationContext().sendBroadcast(mIntentfailreport);

        }

	/**
	 * 获取测试内存大小
	 */
	public int getUsbhostSize(){
		String usbhoststr = mAgingConfig.get("mem_size");
		return Integer.parseInt(usbhoststr);
	}
	
	/**
	 * 获取测试内存时间
	 */
	public int getUsbhostTime(){
		String usbhoststr = mAgingConfig.get("mem_time");
		return Integer.parseInt(usbhoststr);
	}
	
	/**
	 * 是否循环测试
	 */
	public boolean isLoopTest(){
		String usbhoststr = mAgingConfig.get("mem_loop");
		return "1".equals(usbhoststr);
	}
	
	/**
	 * 获取线程数
	 */
	public int getThreadNum(){
		String threadstr = mAgingConfig.get("threads");
		return Integer.parseInt(threadstr);
	}

	@Override
	public void onFailed() {
		isRunning = false;
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
	    mTestcontent = R.string.pub_exception+" "+usbIndex;
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
		mTestcontent = R.string.sd_err_mkdir+" "+usbIndex;
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
		    mTestcontent = R.string.sd_err_mkfile+" "+usbIndex;
                return false;
            } else {
                boolean writeResult = doWriteFile(f.getAbsoluteFile().toString());
                        if(!writeResult){
				mTestcontent = R.string.sd_err_write+" "+usbIndex;
                                return false;
                        }
                        String readResult = doReadFile(f.getAbsoluteFile().toString());
                        if(readResult==null){
				mTestcontent = R.string.sd_err_read+" "+usbIndex;
                                return false;
                        }
                        if(!readResult.equals(TEST_STRING)) {
				mTestcontent = R.string.sd_err_match+" "+usbIndex;
                                return false;
                        }
                        return true;
            }
        } catch (IOException ex) {
		mTestcontent = R.string.pub_exception+" "+usbIndex;
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
