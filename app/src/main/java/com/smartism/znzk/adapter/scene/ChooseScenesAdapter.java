package com.smartism.znzk.adapter.scene;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.smartism.znzk.R;
import com.smartism.znzk.adapter.recycleradapter.BaseRecyslerAdapter;
import com.smartism.znzk.adapter.recycleradapter.RecyclerItemBean;
import com.smartism.znzk.domain.SceneInfo;

import java.util.List;

/**
 * Created by Administrator on 2017/6/13.
 */

public class ChooseScenesAdapter extends BaseRecyslerAdapter {
    // 0自定义  1 定时场景  2 联动  3 在家  4 设防  5 撤防
    public static final int SecuritySceneType_Normal = 0;
    public static final int SecuritySceneType_Time = 1;
    public static final int SecuritySceneType_Trigger = 2;
    public static final int SecuritySceneType_Home = 3;
    public static final int SecuritySceneType_Arming = 4;
    public static final int SecuritySceneType_DesArming = 5;

    public ChooseScenesAdapter(List<RecyclerItemBean> list) {
        super(list);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.choose_scenes_item, parent, false);
        SceneHodle sceneHodle = new SceneHodle(view);
        return sceneHodle;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        SceneHodle sceneHodle = (SceneHodle) holder;
        ((SceneHodle) holder).setValue(list.get(position));
    }

    class SceneHodle extends BaseViewHolder {
        private ImageView icon, choose;
        private TextView name;

        @Override
        public void setValue(RecyclerItemBean itemBean) {
            SceneInfo sceneInfo = (SceneInfo) itemBean.getT();
            icon.setImageResource(R.drawable.zhzj_sl_zaijia);
            // 0自定义  1 定时场景  2 联动  3 在家  4 设防  5 撤防
            if (sceneInfo.getType() == SecuritySceneType_Normal) {
                icon.setImageResource(R.drawable.zhzj_sl_zdy);
            } else if (sceneInfo.getType() == SecuritySceneType_Time) {
                icon.setImageResource(R.drawable.zhzj_sl_dingshi);
            } else if (sceneInfo.getType() == SecuritySceneType_Trigger) {
                icon.setImageResource(R.drawable.zhzj_sl_liandong);
            } else if (sceneInfo.getType() == SecuritySceneType_Home) {//home
                icon.setImageResource(R.drawable.zhzj_sl_zaijia);
            } else if (sceneInfo.getType() == SecuritySceneType_Arming) {//arming
                icon.setImageResource(R.drawable.zhzj_sl_shefang);
            }if (sceneInfo.getType() == SecuritySceneType_DesArming) {//arming
                icon.setImageResource(R.drawable.zhzj_sl_chefang);
            }
            if (sceneInfo.getType()>2){
                    choose.setImageResource(R.drawable.zhzj_cj_xuanzhong);
            }else {
                if (itemBean.isFlag()) {
                    choose.setImageResource(R.drawable.zhzj_cj_xuanzhong);
                } else {
                    choose.setImageResource(R.drawable.zhzj_cj_moren);
                }
            }

//            name.setText(sceneInfo.getName());
            name.setText(sceneInfo==null?"":sceneInfo.getName());
        }

        public SceneHodle(View view) {
            super(view);
            icon = (ImageView) view.findViewById(R.id.scene_icon);
            choose = (ImageView) view.findViewById(R.id.scene_choose);
            name = (TextView) view.findViewById(R.id.scene_name);
        }

    }
}
