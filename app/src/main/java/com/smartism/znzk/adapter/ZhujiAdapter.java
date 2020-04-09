package com.smartism.znzk.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;

import com.smartism.znzk.R;
import com.smartism.znzk.domain.ZhujiInfo;

import java.util.List;

/**
 * Created by Administrator on 2016/12/15.
 */

public class ZhujiAdapter extends BaseAdapter {
    private List<ZhujiInfo> zhujiInfos;
    private Context context;
    private ZhujiInfo zhuji;

    public ZhujiInfo getZhuji() {
        return zhuji;
    }

    public void setZhuji(ZhujiInfo zhuji) {
        this.zhuji = zhuji;
    }

    public ZhujiAdapter(List<ZhujiInfo> zhujiInfos, Context context, ZhujiInfo zhuji) {
        this.zhujiInfos = zhujiInfos;
        this.context = context;
        this.zhuji = zhuji;
    }

    @Override
    public int getCount() {
        return zhujiInfos.size();
    }

    @Override
    public Object getItem(int position) {
        return zhujiInfos.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        HandlerViews hv = null;
        if (convertView == null){
            convertView = LayoutInflater.from(context).inflate(R.layout.rightmenu_zhuji_list_item,null);
            hv = new HandlerViews(convertView);
            final View finalConvertView = convertView;
            convertView.setTag(hv);
        }else{
            hv = (HandlerViews) convertView.getTag();
        }
        hv.setValue(zhujiInfos.get(position),zhuji);
        return convertView;
    }
}
class HandlerViews{
    TextView name;
    RadioButton select;
    LinearLayout layout;
    public HandlerViews(View view) {
        name = (TextView) view.findViewById(R.id.rightmenu_zhuji_name);
        select = (RadioButton) view.findViewById(R.id.rightmenu_zhuji_chack);
        layout = (LinearLayout) view.findViewById(R.id.ll_rightmenu_zhuji);
        select.setClickable(false);
    }
    public void setValue(ZhujiInfo zhuji,ZhujiInfo zhujicurr){
        name.setText(zhuji.getName());
        if (zhujicurr == null) {
            select.setChecked(false);
        }else{
            select.setChecked(zhuji.getId()==zhujicurr.getId());
        }

    }
}