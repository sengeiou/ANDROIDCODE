package com.smartism.znzk.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.smartism.znzk.R;
import com.smartism.znzk.domain.DeviceInfo;
import com.smartism.znzk.domain.ZhujiInfo;
import com.smartism.znzk.util.DataCenterSharedPreferences;

import java.util.List;

/**
 * Created by Administrator on 2017/2/16.
 */

public class DeviceExpandableAdapter extends BaseExpandableListAdapter {

    private Context context;
    private List<ZhujiInfo> zhujiLis;
    private List<List<DeviceInfo>> deviceList;
    protected DataCenterSharedPreferences dcsp = null;

    public DeviceExpandableAdapter(Context context, List<ZhujiInfo> zhujiLis, List<List<DeviceInfo>> deviceList, DataCenterSharedPreferences dcsp) {
        this.context = context;
        this.zhujiLis = zhujiLis;
        this.deviceList = deviceList;
        this.dcsp = dcsp;
    }

    @Override
    public int getGroupCount() {
        return zhujiLis.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return deviceList.get(groupPosition).size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return zhujiLis.get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return deviceList.get(childPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        ZhujiViewHodler viewHodler = null;
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_addgrup_zhuji, null, false);
            viewHodler = new ZhujiViewHodler(convertView);
            convertView.setTag(viewHodler);
        } else {
            viewHodler = (ZhujiViewHodler) convertView.getTag();
        }
        viewHodler.setValue(zhujiLis.get(groupPosition));
        if (isExpanded) {
            viewHodler.icon.setImageResource(R.drawable.arrow_right2);
        } else {
            viewHodler.icon.setImageResource(R.drawable.arrow_right1);
        }
        return convertView;
    }

    @Override
    public View getChildView(final int groupPosition, final int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        DeviceViewHodler viewHodler = null;
        convertView = LayoutInflater.from(context).inflate(R.layout.item_addgrup_device, null, false);
        viewHodler = new DeviceViewHodler(convertView);
        viewHodler.setValue(deviceList.get(groupPosition).get(childPosition));
        //选中状态 ,修改组
        viewHodler.check.setChecked(deviceList.get(groupPosition).get(childPosition).isFlag());
        viewHodler.check.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                expanOnChildViewClistener.onViewClistener(groupPosition, childPosition, isChecked);
                deviceList.get(groupPosition).get(childPosition).setFlag(isChecked);
            }
        });
        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {

        return true;
    }

    class ZhujiViewHodler {
        private TextView name;
        private ImageView icon;

        public ZhujiViewHodler(View view) {
            name = (TextView) view.findViewById(R.id.item_addgroup_zhuji_name);
            icon = (ImageView) view.findViewById(R.id.item_addgroup_zhuji_icon);
        }

        public void setValue(ZhujiInfo zhujiInfo) {
            name.setText(zhujiInfo.getName());
        }
    }

    class DeviceViewHodler {

        private TextView name;
        private ImageView icon;
        private CheckBox check;

        public DeviceViewHodler(View view) {
            name = (TextView) view.findViewById(R.id.item_addgroup_device_name);
            icon = (ImageView) view.findViewById(R.id.item_addgroup_device_logo);
            check = (CheckBox) view.findViewById(R.id.item_addgroup_device_check);
        }

        public void setValue(DeviceInfo deviceInfo) {
            boolean isNull= ((deviceInfo.getWhere()== null||"".equals(deviceInfo.getWhere())||"null".equals(deviceInfo.getWhere()))
                    &&(deviceInfo.getType()== null||"".equals(deviceInfo.getType())));
            StringBuffer sb = new StringBuffer();
            sb.append(deviceInfo.getName());
            if (isNull){
                name.setText(sb.toString());
            }else {
                sb.append(" ");
                sb.append("( ");
                sb.append(((deviceInfo.getWhere()== null||"null".equals(deviceInfo.getWhere()))? "" : deviceInfo.getWhere()+" "));
                sb.append((deviceInfo.getType()));
                sb.append(" )");
            }
            name.setText(sb.toString());
            ImageLoader.getInstance().displayImage( dcsp.getString(DataCenterSharedPreferences.Constant.HTTP_DATA_SERVERS, "")
                    + "/devicelogo/" + deviceInfo.getLogo(), icon, options, null);
            check.setChecked(deviceInfo.isFlag());
        }
    }

    private ExpanOnChildViewClistener expanOnChildViewClistener;

    public void setExpanOnChildViewClistener(ExpanOnChildViewClistener expanOnChildViewClistener) {
        this.expanOnChildViewClistener = expanOnChildViewClistener;
    }

    public interface ExpanOnChildViewClistener {
        public void onViewClistener(int groupPosition, int childPosition, boolean flag);
    }

    // 显示图片的配置
    DisplayImageOptions options = new DisplayImageOptions.Builder().showImageOnLoading(R.drawable.loading)
            .showImageOnFail(R.drawable.sorrow).cacheInMemory(true).cacheOnDisc(true)
            .bitmapConfig(Bitmap.Config.RGB_565).imageScaleType(ImageScaleType.EXACTLY_STRETCHED)// 设置图片以如何的编码方式显示
            .resetViewBeforeLoading(true)// 设置图片在下载前是否重置，复位
            // .displayer(new RoundedBitmapDisplayer(20))//是否设置为圆角，弧度为多少
            .displayer(new FadeInBitmapDisplayer(100))// 是否图片加载好后渐入的动画时间
            .build();
}
