package com.smartism.znzk.adapter;

import java.util.ArrayList;
import java.util.List;

import com.smartism.znzk.R;
import com.smartism.znzk.domain.DeviceUserInfo;
import com.smartism.znzk.view.SwipListView;

import android.content.Context;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class DeviceListAdapter extends BaseAdapter{
	private Context context;
	private SwipListView swipLv;
	private List<DeviceUserInfo> deviceInfos = new ArrayList<DeviceUserInfo>();
    public DeviceListAdapter(Context context,List<DeviceUserInfo> deviceInfos,SwipListView lv_device) {
		this.context = context;
		this.deviceInfos = deviceInfos;
		this.swipLv = swipLv;
	}

	@Override  
    public int getCount() {  
        return deviceInfos.size();  
    }  

    @Override  
    public Object getItem(int position) {  
        return deviceInfos.get(position);  
    }  

    @Override  
    public long getItemId(int position) {  
        return position;  
    }  

    @Override  
    public View getView(int position, View convertView, ViewGroup parent) {
    	ViewHolder holder = null;
        if(null == convertView) {  
        	holder = new ViewHolder();
            convertView = View.inflate(context, R.layout.device_detail_list_item, null);
            holder.tv_name = (TextView) convertView.findViewById(R.id.tv_name);
            holder.tv_status = (TextView) convertView.findViewById(R.id.tv_status);
            holder.tv_delete = (TextView) convertView.findViewById(R.id.tv_delete);
            convertView.setTag(holder);
        } else{
        	holder = (ViewHolder) convertView.getTag();
        }
        DeviceUserInfo userInfo = deviceInfos.get(position);
        holder.tv_name.setText(userInfo.getName());
        
       if(!userInfo.getOnline()){
    	   holder.tv_status.setText("离线");
        }else{
        	 holder.tv_status.setText("在线");
        }
       
          
      final int pos = position;  
        holder.tv_delete.setOnClickListener(new OnClickListener() {  
            @Override  
            public void onClick(View v) {  
            	deviceInfos.remove(pos);  
                notifyDataSetChanged();  
                swipLv.turnToNormal();
                
            }  
        });
          
        return convertView;  
    }
    class ViewHolder{
    	TextView tv_name,tv_status,tv_delete;
    }
}
