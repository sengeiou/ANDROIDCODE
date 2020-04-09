package com.smartism.znzk.camera.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.smartism.znzk.R;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class RecordAdapter extends BaseAdapter {
	Context context;
	public  List<String> list=new ArrayList<String>();
	public static Date startTime;
	public RecordAdapter(){
		
	}
	public RecordAdapter(Context context, List<String> list){
		this.context = context;
		this.list = list;
	}
	class ViewHolder{
		public TextView record_name;

		public TextView getRecord_name() {
			return record_name;
		}

		public void setRecord_name(TextView record_name) {
			this.record_name = record_name;
		}
	}
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		Log.e("getCount", "length="+list.size());
		return list.size();
	}

	@Override
	public Object getItem(int arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long getItemId(int arg0) {
		// TODO Auto-generated method stub
		return 0;
	}
	@Override
	public View getView(int arg0, View arg1, ViewGroup arg2) {
		// TODO Auto-generated method stub
		View view = arg1;
		final ViewHolder holder;
		if(null==view){
			view = LayoutInflater.from(context).inflate(R.layout.list_record_item,null);
			holder=new ViewHolder();
			holder.setRecord_name((TextView)view.findViewById(R.id.rName));
			view.setTag(holder);
		}else{
			holder=(ViewHolder)view.getTag();
		}
		holder.getRecord_name().setText(list.get(arg0).substring(6,list.get(arg0).length()));
		return view;
	}
	public String getLastItem(){
		if(list.size()>0){
			String lastTime=list.get(list.size()-1).substring(6,22);
			lastTime=lastTime.replace("_"," ");	
			Log.e("lastTime", lastTime);
			return lastTime;
		}else{
			return "";
		}
	}
	public static void setStartTime(Date startTime){
		RecordAdapter.startTime=startTime;
	}
	public  void setList(List<String> list){
		this.list = list;
		notifyDataSetChanged();
	}
    public void upLoadData(List<String> loadData){
        Log.e("listsize","old_list_size"+list.size());
        Log.e("loaddate", "loaddata_size"+loadData.size());
    	if(loadData.size()<=0){
    		return;
    	}
    	List<String>  removeList=new ArrayList<String>();
    	List<String>  addList=new ArrayList<String>();
    	for(String str:loadData){
    		for(String s:list){
    			if(str.equals(s)){
    				removeList.add(str);
    			}
    		Log.e("adddate",s+"--");
    		}
    		addList.add(str);
    		Log.e("adddate",str);
    	}
    	Log.e("removelist", "removelist"+removeList.size());
    	addList.removeAll(removeList);
    	Log.e("removelist", "removelist"+addList.size());
    	list.addAll(addList);
    	Log.e("listsize", "list_size--"+list.size());
    	for(String st:list){
    		Log.e("datas", "data"+st);
    	}
    	this.notifyDataSetChanged();
    }
    public List<String> getList(){
    	return this.list;
    }
    public void loadData(){
    	this.notifyDataSetChanged();
    }
    
}
