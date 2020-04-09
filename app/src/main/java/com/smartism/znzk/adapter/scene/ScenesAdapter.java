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

public class ScenesAdapter extends BaseRecyslerAdapter {
    // 0自定义  1 定时场景  2 联动  3 在家  4 设防  5 撤防
    public static final int SecuritySceneType_Normal = 0;
    public static final int SecuritySceneType_Time = 1;
    public static final int SecuritySceneType_Trigger = 2;
    public static final int SecuritySceneType_Home = 3;
    public static final int SecuritySceneType_Arming = 4;
    public static final int SecuritySceneType_DesArming = 5;

    public ScenesAdapter(List<RecyclerItemBean> list) {
        super(list);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.select_scenes_item, parent, false);
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

            if (itemBean.isFlag()){
                if (sceneInfo.getType() == SecuritySceneType_Normal) {
                    icon.setImageResource(R.drawable.zhzj_cj_zidingyi_hover);
                } else if (sceneInfo.getType() == SecuritySceneType_Time) {
                    icon.setImageResource(R.drawable.zhzj_cj_dingshii_hover);
                } else if (sceneInfo.getType() == SecuritySceneType_Trigger) {
                    icon.setImageResource(R.drawable.zhzj_cj_liandongi_hover);
                } else if (sceneInfo.getType() == SecuritySceneType_Home) {//home
                    icon.setImageResource(R.drawable.zhzj_sy_athome_hover);
                } else if (sceneInfo.getType() == SecuritySceneType_Arming) {//arming
                    icon.setImageResource(R.drawable.zhzj_sy_shefang_hover);
                }if (sceneInfo.getType() == SecuritySceneType_DesArming) {//arming
                    icon.setImageResource(R.drawable.zhzj_sy_chefang_hover);
                }
            }else {
                if (sceneInfo.getType() == SecuritySceneType_Normal) {
                    icon.setImageResource(R.drawable.zhzj_cj_zidingyi);
                } else if (sceneInfo.getType() == SecuritySceneType_Time) {
                    icon.setImageResource(R.drawable.zhzj_cj_dingshi);
                } else if (sceneInfo.getType() == SecuritySceneType_Trigger) {
                    icon.setImageResource(R.drawable.zhzj_cj_liandong);
                } else if (sceneInfo.getType() == SecuritySceneType_Home) {//home
                    icon.setImageResource(R.drawable.zhzj_sy_athome);
                } else if (sceneInfo.getType() == SecuritySceneType_Arming) {//arming
                    icon.setImageResource(R.drawable.zhzj_sy_shefang);
                }else if (sceneInfo.getType() == SecuritySceneType_DesArming) {//arming
                    icon.setImageResource(R.drawable.zhzj_sy_chefang);
                }else if (sceneInfo.getType()==100){
                    icon.setImageResource(R.drawable.selector_jinji_iv);
                }
            }

            name.setText(sceneInfo!=null?sceneInfo.getName():"");
            choose.setVisibility(View.GONE);
        }

        public SceneHodle(View view) {
            super(view);
            icon = (ImageView) view.findViewById(R.id.scene_icon);
            choose = (ImageView) view.findViewById(R.id.scene_choose);
            name = (TextView) view.findViewById(R.id.scene_name);
        }

    }
}
