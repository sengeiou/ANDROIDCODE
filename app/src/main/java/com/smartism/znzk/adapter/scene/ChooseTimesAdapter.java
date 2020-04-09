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
import com.smartism.znzk.domain.scene.TimesInfo;

import java.util.List;

/**
 * Created by Administrator on 2017/6/4.
 */

public class ChooseTimesAdapter extends BaseRecyslerAdapter {

    public ChooseTimesAdapter(List<RecyclerItemBean> list) {
        super(list);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.choose_times_item, parent, false);
        TimesHodle timesHodle = new TimesHodle(view);
        return timesHodle;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        TimesHodle timesHodle = (TimesHodle) holder;
        ((TimesHodle) holder).setValue(list.get(position));
    }

    class TimesHodle extends BaseViewHolder{
        private TextView name;
        private RadioButton choose;
        public TimesHodle(View view) {
            super(view);
            name = (TextView) view.findViewById(R.id.times_name);
            choose = (RadioButton) view.findViewById(R.id.times_choose);
        }

        public void setValue(RecyclerItemBean bean){
            TimesInfo info = (TimesInfo) bean.getT();
            name.setText(info.getName());
            choose.setChecked(info.isFlag());
        }
    }
}
