/*******************************************************************
* Company:     Fuzhou Rockchip Electronics Co., Ltd
* Filename:    NetworkDetecting.java  
* Description:   
* @author:     fxw@rock-chips.com
* Create at:   2011-9-9 下午06:02:42  
* 
* Modification History:  
* Date         Author      Version     Description  
* ------------------------------------------------------------------  
* 2011-9-9      xwf         1.0         create
*******************************************************************/   


package com.rockchip.wfd;

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.IntentFilter;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.State;
import android.net.wifi.WifiManager;
import android.provider.Settings;

/**
 * 网络检测
 */
public class NetworkDetecting {
	
	public static final int REQUEST_CONNECT_WIFI = 10;
	
	private WifiManager mWifiManager;
	private Context mContext;
	private boolean isConnected;
	private AlertDialog mAlertDialog;
	private OnClickListener mOnClickListener;
	
	
	public NetworkDetecting(Context context){
		mContext = context;
		mWifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
	}
	
	/**
	 * 检测当前网络状态
	 * 已连接true 未连接false
	 */
	public boolean detect(){
		isConnected = isConnect();
		if(isConnected){
			if(mAlertDialog!=null){
				mAlertDialog.dismiss();
			}
			return isConnected;
		}
		showAlertDialog();
		return isConnected;
	}
	
	/**
	 * 检测WIFI是否已打开
	 * @return
	 */
	public boolean detectWifiEnable(){
		boolean isEnabled = mWifiManager.isWifiEnabled();
		if(!isEnabled){
			AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
			builder.setTitle(mContext.getString(R.string.dlg_prompt));
			builder.setMessage(R.string.dlg_msg_wlan);
			builder.setPositiveButton(mContext.getString(R.string.dlg_ok), new OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					if(!mWifiManager.isWifiEnabled()){
						mWifiManager.setWifiEnabled(true);
					}
					dialog.dismiss(); 
				}
			});
			builder.setNegativeButton(mContext.getString(R.string.dlg_cancel), new OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					if(mOnClickListener!=null){
						mOnClickListener.onClick(dialog, which);
					}
					dialog.dismiss();
				}
			});
			builder.create().show();
		}
		return isEnabled;
	}
	
	public boolean isWifiEnabled(){
		return mWifiManager.isWifiEnabled();
	}
	
	/**
	 * 判断当前网络是否已连接或正在连接
	 * @return
	 */
	public boolean isConnect(){
		Context context = mContext.getApplicationContext();
	    ConnectivityManager connectivity = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
	    if (connectivity == null) {    
	      return false;
	    } else {
	        NetworkInfo[] info = connectivity.getAllNetworkInfo();
	        if (info != null) {        
	            for (int i = 0; i<info.length; i++) {
	                if (info[i]!=null&&(info[i].getState() == NetworkInfo.State.CONNECTED)) {              
	                    return true; 
	                }        
	            }     
	        } 
	    }
	    return false;
	}
	
	/**
	 * 获取激活的连接
	 */
	public NetworkInfo getActiveNetworkInfo(){
		Context context = mContext.getApplicationContext();
		ConnectivityManager connectivity = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		return connectivity.getActiveNetworkInfo();
	}
	
	/**
	 * 弹出窗口提示进行网络设置
	 */
	private void showAlertDialog(){
		if(mAlertDialog == null){
			AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
			builder.setTitle(mContext.getString(R.string.dlg_prompt));
			builder.setMessage(R.string.dlg_msg_wlan);
			builder.setPositiveButton(mContext.getString(R.string.dlg_ok), new OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					IntentFilter filter = new IntentFilter();
					filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
					mContext.registerReceiver(mWifiReceiver, filter);
					if(!mWifiManager.isWifiEnabled()){
						mWifiManager.setWifiEnabled(true);
					}
					try{
						Intent intent = new Intent();
						intent.setClassName("com.rk.setting", "com.rk.setting.wifi.WifiSetting");
						intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
						mContext.startActivity(intent);
					}catch(ActivityNotFoundException anfe){
						//anfe.printStackTrace();
						Intent intent = new Intent(Settings.ACTION_WIFI_SETTINGS);
						intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
						mContext.startActivity(intent);
					}
					dialog.dismiss(); 
				}
			});
			builder.setNegativeButton(mContext.getString(R.string.dlg_cancel), new OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					if(mOnClickListener!=null){
						mOnClickListener.onClick(dialog, which);
					}
					dialog.dismiss();
				}
			});
			mAlertDialog = builder.create();
		}
		mAlertDialog.show();
	}
	
	/**
	 * 是否已连接
	 * @return
	 */
	public boolean isConnected(){
		return isConnected;
	}
	
	public void setNegativeListener(OnClickListener onClickListener){
		mOnClickListener = onClickListener;
	}
	
	BroadcastReceiver mWifiReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
        	String action = intent.getAction();
        	if (WifiManager.NETWORK_STATE_CHANGED_ACTION.equals(action)) {
            	ConnectivityManager connectivity = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            	State state = connectivity.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState(); 
            	if(State.CONNECTED==state){
            		Intent mainIntent = new Intent(mContext, mContext.getClass());
            		mainIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            		mContext.startActivity(mainIntent);
            		mContext.unregisterReceiver(this);
            	} 
            }
        }
    };
}
