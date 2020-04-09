package com.smartism.znzk.adapter.scene;

import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.TextView;

import com.smartism.znzk.R;
import com.smartism.znzk.adapter.recycleradapter.BaseRecyslerAdapter;
import com.smartism.znzk.adapter.recycleradapter.RecyclerItemBean;
import com.smartism.znzk.domain.DeviceInfo;
import com.smartism.znzk.domain.scene.DeviceTipsInfo;

import java.util.List;

/**
 * Created by Administrator on 2017/6/4.
 */

public class ChooseDeviceAdapter extends BaseRecyslerAdapter {

    public ChooseDeviceAdapter(List<RecyclerItemBean> list) {
        super(list);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.choose_times_item, parent, false);
        DeviceHodle deviceHodle = new DeviceHodle(view);
        return deviceHodle;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        DeviceHodle deviceHodle = (DeviceHodle) holder;
        ((DeviceHodle) holder).setValue(list.get(position));
    }

    class DeviceHodle extends BaseViewHolder {
        private TextView name;
        private RadioButton choose;

        public DeviceHodle(View view) {
            super(view);
            name = (TextView) view.findViewById(R.id.times_name);
            choose = (RadioButton) view.findViewById(R.id.times_choose);
        }

        public void setValue(RecyclerItemBean bean) {
            DeviceTipsInfo dtInfo = (DeviceTipsInfo) bean.getT();
            DeviceInfo info = dtInfo.getDeviceInfo();
            String tyepe = "";
            if (!TextUtils.isEmpty(info.getType())) {
                tyepe = "(" + info.getType() + ")";
            }
            name.setText(info.getName() + tyepe);
            choose.setChecked(info.isFlag());
        }
    }
}
