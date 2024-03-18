/*******************************************************************
* Company:     Fuzhou Rockchip Electronics Co., Ltd
* Description:   
* @author:     fxw@rock-chips.com
* Create at:   2014年5月8日 下午5:13:10  
* 
* Modification History:  
* Date         Author      Version     Description  
* ------------------------------------------------------------------  
* 2014年5月8日      fxw         1.0         create
*******************************************************************/   

package com.rockchip.devicetest.adapter;

import java.util.List;

import com.rockchip.devicetest.R;
import com.rockchip.devicetest.model.VersionInfo;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class VersionAdapter extends ArrayAdapter<VersionInfo> {

	private LayoutInflater mLayoutInflater;
	private List<VersionInfo> mVersionList;
	
	public VersionAdapter(Context context,List<VersionInfo> versionList) {
		super(context, 0, versionList);
		mVersionList = versionList;
		mLayoutInflater = LayoutInflater.from(context);
	}

	/**
	 * @return the mVersionList
	 */
	public List<VersionInfo> getVersionList() {
		return mVersionList;
	}
	
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ListHoder listHoder;
		if(convertView==null){
			convertView = mLayoutInflater.inflate(R.layout.main_version_listitem, null);
			listHoder = new ListHoder();
			listHoder.txtVerName = (TextView)convertView.findViewById(R.id.tv_ver_title);
			listHoder.txtVerValue = (TextView)convertView.findViewById(R.id.tv_ver_detail);
			convertView.setTag(listHoder);
		}else{
			listHoder = (ListHoder)convertView.getTag();
		}
		VersionInfo item = getItem(position);
		listHoder.txtVerName.setText(item.getVerName());
		listHoder.txtVerValue.setText(item.getVerValue());
		return convertView;
	}
	
	public static class ListHoder{
		public TextView txtVerName;
		public TextView txtVerValue;
	}
}
