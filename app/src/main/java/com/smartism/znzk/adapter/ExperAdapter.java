package com.smartism.znzk.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.smartism.znzk.R;
import com.smartism.znzk.domain.ZhujiInfo;

import java.util.List;

/**
 * 主机体验列表
 * Created by Administrator on 2017/6/26.
 */

public class ExperAdapter extends BaseAdapter {

    private List<ZhujiInfo> zhujiInfos;
    private Context context;

    public ExperAdapter(List<ZhujiInfo> zhujiInfos, Context context) {
        this.zhujiInfos = zhujiInfos;
        this.context = context;
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
        HandlerView hv = null;
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_experzhuji_list, null);
            hv = new HandlerView(convertView);
            final View finalConvertView = convertView;
            convertView.setTag(hv);
        } else {
            hv = (HandlerView) convertView.getTag();
        }
        hv.setValue(zhujiInfos.get(position), context);
        return convertView;
    }
}

class HandlerView {
    TextView name, where, count;
    RelativeLayout layout;

    public HandlerView(View view) {
        name = (TextView) view.findViewById(R.id.exper_name);
        where = (TextView) view.findViewById(R.id.exper_country);
        count = (TextView) view.findViewById(R.id.exper_usercount);
        layout = (RelativeLayout) view.findViewById(R.id.exper_layout);
    }

    public void setValue(ZhujiInfo zhuji, Context context) {
        name.setText(context.getString(R.string.experience_zhuji_master)+zhuji.getMasterid());
        where.setText(zhuji.getCountry());
        count.setText(String.valueOf(zhuji.getUsercount()));
    }
}
