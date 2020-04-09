package com.smartism.znzk.adapter.camera;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.smartism.znzk.R;
import com.smartism.znzk.domain.camera.CameraInfo;

import java.util.List;

/**
 * Created by Administrator on 2016/9/19.
 */
public class BindingAdapter extends BaseAdapter {
    private List<CameraInfo> cameraInfos;
    private ListView lv_camera;
    private Context context;
    public BindingAdapter(List<CameraInfo> cameraInfos,Context context,ListView lv_camera){
        this.cameraInfos = cameraInfos;
        this.context = context;
        this.lv_camera = lv_camera;
    }

    @Override
    public int getCount() {
        if (cameraInfos==null)return 0;
        return cameraInfos.size();
    }

    @Override
    public Object getItem(int position) {
        return cameraInfos.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHandler vh = null;
        if (convertView==null){
            convertView = LayoutInflater.from(context).inflate(R.layout.activity_camera_list_item,null);
            vh = new ViewHandler();
            vh.textView = (TextView) convertView.findViewById(R.id.binding_item_name);
            vh.checkBox = (CheckBox) convertView.findViewById(R.id.binding_item_checbox);
            vh.item_camera = (LinearLayout) convertView.findViewById(R.id.item_camera);
            convertView.setTag(vh);
        }else{
            vh = (ViewHandler) convertView.getTag();
        }
        CameraInfo cameraInfo = cameraInfos.get(position);
        vh.textView.setText(cameraInfo.getZjName()!=null?cameraInfo.getZjName():cameraInfo.getN());
       // vh.textView.setText(cameraInfo.getN());
        vh.checkBox.setChecked(false);
        vh.checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked) changeItemCheckBox(position);
            }
        });
        return convertView;
    }

    public void changeItemCheckBox(int position){
        for (int indeex = 0;indeex<cameraInfos.size();indeex++){
            View view = lv_camera.getChildAt(indeex);
            ViewHandler vh = (ViewHandler) view.getTag();
            if (vh!=null) {
                vh.checkBox.setChecked(false);
            }
        }
        View view = lv_camera.getChildAt(position);
        ViewHandler vh = (ViewHandler) view.getTag();
        if (vh!=null) {
            vh.checkBox.setChecked(true);
        }

    }
    public void changeItemCheckBox(String id){
        int position = -1;
        for (int indeex = 0;indeex<cameraInfos.size();indeex++){
            if ((cameraInfos.get(indeex).getId()).equals(id)){
                position = indeex;
            }
        }
        if (position>-1) changeItemCheckBox(position);
    }
    public class ViewHandler{
        public  TextView textView;
        public CheckBox checkBox;
        public LinearLayout item_camera;
    }
}
