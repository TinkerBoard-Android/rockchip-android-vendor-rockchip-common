package com.rockchip.devicetest.testcase;

import java.util.List;

import com.rockchip.devicetest.adapter.TestCaseArrayAdapter;
import com.rockchip.devicetest.adapter.TestCaseArrayAdapter.ListHoder;
import com.rockchip.devicetest.enumerate.Commands;
import com.rockchip.devicetest.model.TestCaseInfo;
import com.rockchip.devicetest.testcase.BaseTestCase.TestCaseViewListener;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ListView;

public class TestCaseListView extends ListView implements TestCaseViewListener {

	private List<TestCaseInfo> mTestCaseList;
	private TestCaseArrayAdapter mTestArrayAdapter;
	
	public TestCaseListView(Context context) {
		super(context);
	}
	
	public TestCaseListView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	public TestCaseListView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}
	
	public void setDataSource(List<TestCaseInfo> testcaseList){
		if(mTestCaseList!=testcaseList){
			mTestCaseList.clear();
			mTestCaseList.addAll(testcaseList);
		}
		mTestArrayAdapter.notifyDataSetChanged();
	}
	
	public void setTestCaseAdapter(TestCaseArrayAdapter adapter){
		mTestArrayAdapter = adapter;
		mTestCaseList = adapter.getTestCaseList();
		setAdapter(adapter);
	}
	
	/**
	 * 测试项UI更新
	 */
	public void onTestUIUpdate(TestCaseInfo testInfo){
		ListHoder holder = getViewHolder(testInfo);
		if(holder!=null){
			holder.update(getContext(), testInfo);
		}
	}
	
	//获取View Item Holder
	private ListHoder getViewHolder(TestCaseInfo testInfo){
		Commands cmds = testInfo.getCmd();
		for(int i=0; i<mTestCaseList.size(); i++){
			TestCaseInfo testcase = mTestCaseList.get(i);
			if(testcase.getCmd()==cmds){
				for(int j=0; j< getChildCount(); j++){
					View view = getChildAt(j);
					if(view==null){
						return null;
					}
					int position = getPositionForView(view)-1/*header*/;
					if(position==i){
						ListHoder holder = (ListHoder)view.getTag();
						return holder;
					}
				}
			}
		}
		return null;
	}
	
	public interface ListViewLoadListener {
		public void onListViewLoadCompleted();
	}

}
