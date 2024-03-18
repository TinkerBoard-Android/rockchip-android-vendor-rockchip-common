/*******************************************************************
* Company:     Fuzhou Rockchip Electronics Co., Ltd
* Description:   
* @author:     fxw@rock-chips.com
* Create at:   2014年5月8日 下午2:58:56  
* 
* Modification History:  
* Date         Author      Version     Description  
* ------------------------------------------------------------------  
* 2014年5月8日      fxw         1.0         create
*******************************************************************/   

package com.rockchip.devicetest.adapter;

import java.util.List;

import com.rockchip.devicetest.R;
import com.rockchip.devicetest.enumerate.Commands;
import com.rockchip.devicetest.enumerate.SendResultType;
import com.rockchip.devicetest.enumerate.TestResultType;
import com.rockchip.devicetest.enumerate.TestStatus;
import com.rockchip.devicetest.model.TestCaseInfo;
import com.rockchip.devicetest.testcase.TestCaseListView.ListViewLoadListener;

import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class TestCaseArrayAdapter extends ArrayAdapter<TestCaseInfo> {
	
	private Context mContext;
	private LayoutInflater mLayoutInflater;
	private List<TestCaseInfo> mTestCaseList;
	private ListViewLoadListener mListViewListener;
	private Handler mMainHandler;
	private boolean mCheckingLoad;
	
	public TestCaseArrayAdapter(Context context, List<TestCaseInfo> testList) {
		super(context, 0, testList);
		mContext = context;
		mTestCaseList = testList;
		mLayoutInflater = LayoutInflater.from(context);
		mMainHandler = new Handler();
	}
	
	public List<TestCaseInfo> getTestCaseList() {
		return mTestCaseList;
	}
	
	@Override
	public void notifyDataSetChanged() {
		super.notifyDataSetChanged();
		if(mListViewListener==null){
			return;
		}
		mCheckingLoad = true;
	}
	
	public void setOnListViewLoadListener(ListViewLoadListener listener){
		mListViewListener = listener;
	}
	
	Runnable mCheckAction = new Runnable(){
		public void run() {
			if(mCheckingLoad){
				mCheckingLoad = false;
				mMainHandler.removeCallbacks(mCheckAction);
				if(mListViewListener!=null){
					mListViewListener.onListViewLoadCompleted();
				}
			}
		};
	};
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ListHoder listHoder;
		if(convertView==null){
			convertView = mLayoutInflater.inflate(R.layout.main_listitem, null);
			listHoder = new ListHoder();
			listHoder.txtTitle = (TextView)convertView.findViewById(R.id.tv_test_title);
			listHoder.txtStatus = (TextView)convertView.findViewById(R.id.tv_test_status);
			listHoder.txtResult = (TextView)convertView.findViewById(R.id.tv_test_result);
			listHoder.txtSend = (TextView)convertView.findViewById(R.id.tv_test_send);
			listHoder.txtDetail = (TextView)convertView.findViewById(R.id.tv_test_detail);
			convertView.setTag(listHoder);
		}else{
			listHoder = (ListHoder)convertView.getTag();
		}
		TestCaseInfo item = getItem(position);
		listHoder.update(mContext, item);
		if(mCheckingLoad){
			mMainHandler.removeCallbacks(mCheckAction);
			mMainHandler.postDelayed(mCheckAction, 500);
		}
		return convertView;
	}
	
	public static class ListHoder{
		public TextView txtTitle;
		public TextView txtStatus;
		public TextView txtResult;
		public TextView txtSend;
		public TextView txtDetail;
		
		public void update(Context context, TestCaseInfo testInfo){
			//column1 title
			Commands cmd = testInfo.getCmd();
			if(cmd!=null){
				int resID = testInfo.getCmd().getResID();
				if(resID<=0){
					txtTitle.setText(testInfo.getCmd().getCommand());
				}else{
					txtTitle.setText(testInfo.getCmd().getResID());
				}
			}
			//column2 status
			TestStatus tStatus = testInfo.getStatus();
			if(tStatus!=null){
				txtStatus.setText(tStatus.getResID());
				if(tStatus==TestStatus.WAITING){
					txtStatus.setTextColor(Color.WHITE);
				}else{
					txtStatus.setTextColor(Color.GREEN);
				}
			}else{
				txtStatus.setText("");
			}
			//column3 result
			TestResultType result = testInfo.getResult();
			if(result!=null){
				if(result == TestResultType.SUCCESS){
					txtResult.setTextColor(Color.GREEN);
					txtResult.setText(result.getResID());
				}else{
					txtResult.setTextColor(Color.YELLOW);
					String failStr = context.getString(result.getResID());
					failStr += "("+context.getString(R.string.pub_retest, testInfo.getTestKeychar())+")";
					txtResult.setText(failStr);
				}
			}else{
				txtResult.setText("");
			}
			//column4 Send result
			SendResultType sendResult = testInfo.getSendResult();
			if(sendResult!=null){
				txtSend.setText(sendResult.getResID());
				if(sendResult == SendResultType.SUCCESS){
					txtSend.setTextColor(Color.GREEN);
				}else{
					txtSend.setTextColor(Color.YELLOW);
				}
			}else{
				txtSend.setText("");
			}
			//column5 detail
			txtDetail.setText(testInfo.getDetail());
		}
		
	}

}
