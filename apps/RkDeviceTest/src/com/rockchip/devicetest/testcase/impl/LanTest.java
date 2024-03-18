/*******************************************************************
* Company:     Fuzhou Rockchip Electronics Co., Ltd
* Description:   
* @author:     fxw@rock-chips.com
* Create at:   2014年5月12日 下午2:08:44  
* 
* Modification History:  
* Date         Author      Version     Description  
* ------------------------------------------------------------------  
* 2014年5月12日      fxw         1.0         create
*******************************************************************/   

package com.rockchip.devicetest.testcase.impl;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
//import android.net.ethernet.EthernetManager;
import android.net.EthernetManager;
import android.net.ConnectivityManager;
import android.net.IpConfiguration.IpAssignment;
import android.net.IpConfiguration.ProxySettings;
//import android.net.EthernetDataTracker;
import android.os.Handler;
import android.os.SystemProperties;

import com.rockchip.devicetest.R;
import com.rockchip.devicetest.model.TestCaseInfo;
import com.rockchip.devicetest.model.TestResult;
import com.rockchip.devicetest.testcase.BaseTestCase;
import com.rockchip.devicetest.utils.StringUtils;
import android.net.LinkProperties;
import android.provider.Settings.System;
import android.util.Log;
import android.net.NetworkInfo;
import android.net.Network;
import android.net.LinkAddress;

public class LanTest extends BaseTestCase {

	private EthernetManager mEthernetManager;
	private boolean hasRegister;
        private Context mContext;
        private ConnectivityManager mConnectivityManager;	

	public LanTest(Context context, Handler handler, TestCaseInfo testcase) {
		super(context, handler, testcase);
                mContext = context;
                mConnectivityManager = (ConnectivityManager) mContext.getSystemService(
                Context.CONNECTIVITY_SERVICE);
                mEthernetManager = mContext.getSystemService(EthernetManager.class);
	}
        /**
     * Return whether Ethernet port is available.
     */
    //public boolean isEthernetAvailable() {
    //    return mConnectivityManager.isNetworkSupported(ConnectivityManager.TYPE_ETHERNET)
    //            && mEthernetManager.getAvailableInterfaces().length > 0;
   // }

    private Network getFirstEthernet() {
        final Network[] networks = mConnectivityManager.getAllNetworks();
        for (final Network network : networks) {
            NetworkInfo networkInfo = mConnectivityManager.getNetworkInfo(network);
            if (networkInfo != null && networkInfo.getType() == ConnectivityManager.TYPE_ETHERNET) {
                return network;
            }
        }
        return null;
    }

    public String getEthernetIpAddress() {
        final Network network = getFirstEthernet();
        if (network == null) {
            return null;
        }
        final StringBuilder sb = new StringBuilder();
        boolean gotAddress = false;
        final LinkProperties linkProperties = mConnectivityManager.getLinkProperties(network);
        for (LinkAddress linkAddress : linkProperties.getLinkAddresses()) {
            if (gotAddress) {
                sb.append("\n");
            }
            sb.append(linkAddress.getAddress().getHostAddress());
            gotAddress = true;
        }
        if (gotAddress) {
            return sb.toString();
        } else {
            return null;
        }
    }	
        private final EthernetManager.Listener mEthernetListener = new EthernetManager.Listener() {
              @Override
              public void onAvailabilityChanged(String iface, boolean isAvailable) {
              }
        };
	@Override
	public boolean onTesting() {
		hasRegister = true;
		setTestTimeout(DEFAULT_TEST_TIMEOUT);
		NetworkInfo networkInfo = mConnectivityManager.getActiveNetworkInfo();
                if (networkInfo == null) {
                      onTestFail(R.string.lan_err_disconnect);
                } else {
                     if(networkInfo.getType() == ConnectivityManager.TYPE_ETHERNET){
                         onTestSuccess(getEthernetIpAddress());
                     }
                }
	/*	if(isEthernetAvailable()){//以太网已开启
			boolean mConnect = (mEthManager.getEthernetConnectState() == EthernetManager.ETHER_STATE_CONNECTED);
                       Log.d("hjc","======mConnect:"+mConnect+"  =====getEthernetConnectState:"+mEthManager.getEthernetConnectState());
			if(!mConnect){//waiting
				//onTestFail(R.string.lan_err_disconnect);
				//return;
                                mEthManager.reconnect("eth0");
			}else{
				testEthernet();
			}
		}else{
			mEthManager.reconnect("eth0");
                     /*
			boolean enabledRes = mEthManager.setEthernetEnabled(true);
			if(!enabledRes){
				onTestFail(R.string.lan_err_enable);
			}
                     */
	//	}
		return true;
	}
	
	public boolean onTestHandled(TestResult result) {
		if(hasRegister){
			hasRegister = false;
//			mContext.unregisterReceiver(mEthernetReceiver);
		}
		return super.onTestHandled(result);
	}
	
	public void stop() {
		if(hasRegister){
			hasRegister = false;
//			mContext.unregisterReceiver(mEthernetReceiver);
		}
		super.stop();
	}
	
	/**
	 * 测试以太网
	 */
/*	public void testEthernet() {
		ContentResolver contentResolver = mContext.getContentResolver();
		//int useStaticIp = System.getInt(contentResolver, System.ETHERNET_USE_STATIC_IP, 0);
		boolean useStaticIp = (mEthManager.getConfiguration().ipAssignment == IpAssignment.STATIC) ? true : false;;
		String ipaddress = null;
		if (useStaticIp) {
			ipaddress = mEthManager.getConfiguration().getStaticIpConfiguration().ipAddress.getAddress().getHostAddress();
			ipaddress += "(static)";
		}else{
			//ipaddress = getEthInfoFromDhcp();
			ipaddress = mEthManager.getIpAddress();
		}
                 Log.d("hjc","=====testEthernet ipaddress:"+ipaddress);
		if(StringUtils.isEmptyObj(ipaddress)){
			onTestFail(R.string.lan_err_ip);
		}else{
			String ipdetail = mContext.getString(R.string.lan_ip_address, ipaddress);
			onTestSuccess(ipdetail);
		}
	}
	
	public String getEthInfoFromDhcp() {
		String tempIpInfo;
		String mEthIpAddress;
		//String iface = mEthManager.getEthernetIfaceName();
		String iface = "eth0";

		tempIpInfo = SystemProperties.get("dhcp." + iface + ".ipaddress");
                
		if ((tempIpInfo != null) && (!tempIpInfo.equals(""))) {
			mEthIpAddress = tempIpInfo;
		} else {
			mEthIpAddress = "";
		}
		return mEthIpAddress;
	}
	
	private final BroadcastReceiver mEthernetReceiver = new BroadcastReceiver() {
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (action.equals(EthernetManager.ETHERNET_STATE_CHANGED_ACTION)) {
				int ethernetState = intent.getIntExtra(EthernetManager.EXTRA_ETHERNET_STATE, -1);
				if(ethernetState == EthernetManager.ETHER_STATE_CONNECTED){
					testEthernet();
				}
			}
		}
	};
*/

}
