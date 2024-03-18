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
import android.os.AsyncTask;
import android.os.Bundle;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.content.Context;
import android.content.Intent;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.net.MulticastSocket;
import java.net.Inet6Address;
import java.util.Enumeration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class EthernetTest extends BaseAgingTest {

	public static final int UPDATE_DELAY = 3000;
	private Activity mActivity;
	private boolean isRunning;
        private Handler mMainHandler = new Handler();
        private static final String TESTPROCESS_ACTION="com.rockchip.devicetest.aging.TestProcess";
        private static final String FAILLISTS_ACTION="com.rockchip.devicetest.aging.FailLists";
        private static final String pass = " -> PASS";
        private static final String fail = " -> FAIL";
        private static final String functions = "Ethernet Test:  ";
        private int mTestTimes = 6;
        List<Integer> EthernetTestfailcounter = Arrays.asList(0, 0);
        List<Integer> EthernetTestfailreport = Arrays.asList(0, 0);
        List<String> EthernetIpAddress = Arrays.asList("", "");
        List<String> Ethernet_ping_ipaddress = Arrays.asList("","");
                        

	private Intent mIntenttestprocess;
        private Intent mIntentfailreport;


	public EthernetTest(AgingConfig config, AgingCallback agingCallback){
		super(config, agingCallback);
	}
	
	@Override
	public void onCreate(Activity activity) {
		mActivity = activity;
                mIntenttestprocess = new Intent(TESTPROCESS_ACTION);
                mIntentfailreport = new Intent(FAILLISTS_ACTION);
		Ethernet_ping_ipaddress.set(0, mAgingConfig.get("eth0_ping_ipaddress"));
		Ethernet_ping_ipaddress.set(1, mAgingConfig.get("eth1_ping_ipaddress"));
	}

        public void getEthernetdhcpIpAddress() {
                ConnectivityManager cm = (ConnectivityManager) mActivity.getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);

                EthernetIpAddress.set(0, "");
                EthernetIpAddress.set(1, "");
                NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
                if (activeNetwork != null && activeNetwork.isConnected()) {
                        try {
                                Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
                                while (interfaces.hasMoreElements()) {
                                        NetworkInterface networkInterface = interfaces.nextElement();
                                        if (networkInterface.getName().equalsIgnoreCase("eth0")) {
                                                Enumeration<InetAddress> inetAddressEnumeration = networkInterface.getInetAddresses();
                                                while (inetAddressEnumeration.hasMoreElements()) {
                                                        InetAddress inetAddress = inetAddressEnumeration.nextElement();
                                                        if (inetAddress instanceof Inet6Address) {
                                                                continue;

                                                        }
                                                        LogUtil.d(this, "ETH0 ip address: " + inetAddress.getHostAddress()+"\n");
                                                        EthernetIpAddress.set(0, inetAddress.getHostAddress());
                                                }
                                        }
                                        if (networkInterface.getName().equalsIgnoreCase("eth1")) {
                                                Enumeration<InetAddress> inetAddressEnumeration = networkInterface.getInetAddresses();
                                                while (inetAddressEnumeration.hasMoreElements()) {
                                                        InetAddress inetAddress = inetAddressEnumeration.nextElement();
                                                        if (inetAddress instanceof Inet6Address) {
                                                                continue;
                                                        }
                                                        LogUtil.d(this, "ETH1 ip address: " + inetAddress.getHostAddress()+"\n");
                                                        EthernetIpAddress.set(1, inetAddress.getHostAddress());
                                                }
                                        }
                                }
                        }
                        catch (IOException e) {
                                e.printStackTrace();
                        }
                }
        }

	@Override
	public void onStart() {
		isRunning = true;
                mMainHandler.postDelayed(mUpdateAction, UPDATE_DELAY);
	}

        private Runnable mUpdateAction = new Runnable(){
                public void run() {

			getEthernetdhcpIpAddress();

			try {
				for(int i=0; i<2; i++){	
					if ((EthernetTestfailcounter.get(i) >= mTestTimes)) {
						onTestProcessFail(functions, "eth"+ i + "keep fail! times: "+ EthernetTestfailcounter.get(i));
                                                if (EthernetTestfailreport.get(i).equals(0)) {
                                                        onFailListsreport(functions, " eth"+i+" :  ");
                                                        EthernetTestfailreport.set(i, EthernetTestfailcounter.get(i)+1);
						}

                                        } else if (!(EthernetIpAddress.get(i) == null || EthernetIpAddress.get(i).isEmpty())) {
        	    				Process p = Runtime.getRuntime().exec("ping -I "+ EthernetIpAddress.get(i)  +" -c 1 -w 5 "+Ethernet_ping_ipaddress.get(i));
            					int status = p.waitFor();
            					if (status == 0) {
							onTestProcessSuccess(functions, "eth"+i);
							EthernetTestfailcounter.set(i, 0);
            					} else {
                                        		onTestProcessFail(functions, " eth0 ping "+Ethernet_ping_ipaddress.get(i)+" fail counter="+ (EthernetTestfailcounter.get(i)+1) );
                                        		EthernetTestfailcounter.set(i, EthernetTestfailcounter.get(i)+1);
            					}
					} else {
							onTestProcessFail(functions, " eth"+i+ " get ipaddress fail counter="+ (EthernetTestfailcounter.get(i)+1) );
                                                        EthernetTestfailcounter.set(i, EthernetTestfailcounter.get(i)+1);
					}
				}

        		} catch (IOException e) {
                		LogUtil.d(this, "Fail: IOException\n");
        		} catch (InterruptedException e) {
                		LogUtil.d(this, "Fail: InterruptedException\n");
        		}
                        mMainHandler.postDelayed(this, UPDATE_DELAY);
                };
        };

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

	@Override
	public void onStop() {
		isRunning = false;
	}

	@Override
	public void onDestroy() {

	}
	
	@Override
	public void onFailed() {
		isRunning = false;
	}

}
