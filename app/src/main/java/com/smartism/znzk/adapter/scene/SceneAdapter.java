package com.smartism.znzk.adapter.scene;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.smartism.znzk.R;
import com.smartism.znzk.domain.FoundInfo;

import java.util.List;

/**
 * Created by Administrator on 2017/8/2.
 */

public class SceneAdapter extends BaseAdapter {

    public static final int SecuritySceneType_Normal = 0;
    public static final int SecuritySceneType_Time = 1;
    public static final int SecuritySceneType_Trigger = 2;
    public static final int SecuritySceneType_Home = 3;
    public static final int SecuritySceneType_Arming = 4;
    public static final int SecuritySceneType_DesArming = 5;

    private List<FoundInfo> sceneList;
    private Context context;
    LayoutInflater layoutInflater;

    public SceneAdapter(List<FoundInfo> sceneList, Context context) {
        this.sceneList = sceneList;
        this.context = context;
        layoutInflater = LayoutInflater.from(context);
    }


    @Override
    public int getCount() {
        return sceneList.size();
    }

    @Override
    public Object getItem(int position) {
        return sceneList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        DeviceInfoView viewCache = new DeviceInfoView();
        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.activity_scene_item, null);
            viewCache.icon = (ImageView) convertView.findViewById(R.id.scene_item_img);
            viewCache.name = (TextView) convertView.findViewById(R.id.scene_item_txt);
            convertView.setTag(viewCache);
        } else {
            viewCache = (DeviceInfoView) convertView.getTag();
        }
        FoundInfo sceneInfo = sceneList.get(position);
        viewCache.name.setText(sceneList.get(position).getName());
        if (sceneInfo.getType() == SecuritySceneType_Normal) {
            viewCache.icon.setImageResource(R.drawable.zhzj_sl_zdy);
        } else if (sceneInfo.getType() == SecuritySceneType_Time) {
            viewCache.icon.setImageResource(R.drawable.zhzj_sl_dingshi);
        } else if (sceneInfo.getType() == SecuritySceneType_Trigger) {
            viewCache.icon.setImageResource(R.drawable.zhzj_sl_liandong);
        } else if (sceneInfo.getType() == SecuritySceneType_Home) {//home
            viewCache.icon.setImageResource(R.drawable.zhzj_sl_zaijia);
        } else if (sceneInfo.getType() == SecuritySceneType_Arming) {//arming
            viewCache.icon.setImageResource(R.drawable.zhzj_sl_shefang);
        }
        if (sceneInfo.getType() == SecuritySceneType_DesArming) {//arming
            viewCache.icon.setImageResource(R.drawable.zhzj_sl_chefang);
        }
        return convertView;
    }

    class DeviceInfoView {
        ImageView icon;
        TextView name;
    }
}