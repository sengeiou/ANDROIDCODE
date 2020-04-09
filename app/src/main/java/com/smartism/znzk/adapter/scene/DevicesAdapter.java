package com.smartism.znzk.adapter.scene;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.smartism.znzk.R;
import com.smartism.znzk.adapter.recycleradapter.BaseRecyslerAdapter;
import com.smartism.znzk.adapter.recycleradapter.RecyclerItemBean;
import com.smartism.znzk.domain.DeviceInfo;
import com.smartism.znzk.domain.Tips;
import com.smartism.znzk.domain.scene.DeviceTipsInfo;

import java.util.List;

/**
 * Created by Administrator on 2017/6/6.
 */

public class DevicesAdapter extends BaseRecyslerAdapter {
    private int flag = 1;

    public DevicesAdapter(List<RecyclerItemBean> list, int flag) {
        super(list);
        this.flag = flag;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = initFootOrHead(viewType);
        if (view == null) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.scene_devices_item, parent, false);
            TextHodle deviceHodle = new TextHodle(view, parent.getContext());
            return deviceHodle;
        }
        return new HFViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (!initFootOrHeadForBind(position)) {
            ((TextHodle) holder).setValue(list.get(position));
        }
    }

    class TextHodle extends BaseViewHolder {
        private TextView name, keys;
        public View view;
        public Context context;

        public TextHodle(View view, Context context) {
            super(view);
            this.context = context;
            name = (TextView) view.findViewById(R.id.times_name);
            keys = (TextView) view.findViewById(R.id.times_keys);
        }

        public void setValue(RecyclerItemBean bean) {
            DeviceTipsInfo dtInfo = (DeviceTipsInfo) bean.getT();
            DeviceInfo info = dtInfo.getDeviceInfo();
            List<Tips> tips = dtInfo.getTips();
            //设备名称和地址和类型
            boolean isNull = ((info.getWhere() == null || "".equals(info.getWhere()) || "null".equals(info.getWhere()))
                    && (info.getType() == null || "".equals(info.getType())));
            StringBuffer sb = new StringBuffer();
            sb.append(info.getName());
            if (isNull) {
                name.setText(sb.toString());
            } else {
                sb.append(" ");
                sb.append("( ");
                sb.append(((info.getWhere() == null || "null".equals(info.getWhere())) ? "" : info.getWhere() + " "));
                sb.append((info.getType()));
                sb.append(" )");
                name.setText(sb.toString());
            }
            if (flag == 1) {
                //显示设备指令
                StringBuffer stringBuffer = new StringBuffer();
                if (tips != null && !tips.isEmpty()) {
                    for (Tips tip : tips) {
                        Log.e("resultStr", info.getName() + "<-------->" + tip.toString());
                        stringBuffer.append(tip.getE());
                        stringBuffer.append(",");
                    }
                    if (stringBuffer.length() > 1)
                        stringBuffer.deleteCharAt(stringBuffer.length() - 1);
                    keys.setText(stringBuffer.toString());
                } else {
                    if (info.getTip() != null) {
                        if (info.getTip().equals("0")) {
                            keys.setText(context.getString(R.string.activity_scene_item_home));
                        } else if (info.getTip().equals("1")) {
                            keys.setText(context.getString(R.string.devices_list_menu_dialog_jsbts));
                        } else if (info.getTip().equals("2")) {
                            if (DeviceInfo.CaMenu.ybq.value().equals(info.getCa())) {
                                keys.setText(context.getString(R.string.ybq_chart_name));
                            } else {
                                keys.setText(context.getString(R.string.activity_scene_item_inhome));
                            }
                        } else {
                            keys.setText(context.getString(R.string.activity_scene_item_outside));
                        }
                    }
                }
            } else {
                if (info.getTipName() != null) {
                    keys.setText(info.getTipName());
                }
            }

        }
    }
}
