package com.smartism.znzk.adapter.scene;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.TextView;

import com.smartism.znzk.R;
import com.smartism.znzk.adapter.recycleradapter.BaseRecyslerAdapter;
import com.smartism.znzk.adapter.recycleradapter.RecyclerItemBean;
import com.smartism.znzk.domain.Tips;

import java.util.List;

/**
 * 设备操作指令适配器
 * Created by Administrator on 2017/6/6.
 */

public class ChooseTipsAdapter extends BaseRecyslerAdapter {

    public ChooseTipsAdapter(List<RecyclerItemBean> list) {
        super(list);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.choose_times_item, parent, false);
        TipsHodle deviceHodle = new TipsHodle(view);
        return deviceHodle;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        TipsHodle deviceHodle = (TipsHodle) holder;
        ((TipsHodle) holder).setValue(list.get(position));
    }

    class TipsHodle extends BaseViewHolder{
        private TextView name;
        private RadioButton choose;
        public TipsHodle(View view) {
            super(view);
            name = (TextView) view.findViewById(R.id.times_name);
            choose = (RadioButton) view.findViewById(R.id.times_choose);
        }

        public void setValue(RecyclerItemBean bean){
            Tips tip = (Tips) bean.getT();
            name.setText(tip.getE());
            choose.setChecked(tip.isFlag());
        }
    }


}
