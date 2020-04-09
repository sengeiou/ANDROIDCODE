package com.smartism.znzk.view.cityweel;

import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.smartism.znzk.R;
import com.smartism.znzk.activity.ActivityParentActivity;
import com.smartism.znzk.application.MainApplication;
import com.smartism.znzk.domain.AreaInfo;
import com.smartism.znzk.util.Actions;
import com.smartism.znzk.util.DataCenterSharedPreferences;
import com.smartism.znzk.util.HttpRequestUtils;
import com.smartism.znzk.util.JavaThreadPool;
import com.smartism.znzk.util.LanguageUtil;
import com.smartism.znzk.util.SecurityUtil;
import com.smartism.znzk.util.WeakRefHandler;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2016/6/7 0007.
 */
public class SelectAddreActivity extends ActivityParentActivity implements AddressResult {

    protected ArrayList<AreaInfo> list; //数据源
    protected ListView listView;
    protected AddressAdapter adapter;
    protected AddressResult addressResult;


    private List<AreaInfo> areaInfos;
    private List<AreaInfo> chooseArea;

    private View contentView;
    public PopupWindow addrPopWindow;
    private LinearLayout boxBtnCancel;
    protected boolean isDataLoaded = false;

    private int adreId = 0;//1 亚洲，100000中国  -- 默认是0
    private int type = 1;
    /**
     * 选择地址
     */
    public boolean isAddrChoosed = false;

    public void setAddressResult(AddressResult addressResult) {
        this.addressResult = addressResult;
    }

    private Handler.Callback mCallback = new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            if (msg.what == 0) {
                cancelInProgress();
                adapter.notifyDataSetChanged();
            }
            if (msg.what == 1) {
                setAreaInfo();
                addrPopWindow.dismiss();
            }
            return false;
        }
    };
    private Handler handler = new WeakRefHandler(mCallback);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initProviceSelectView();
        initProviceSelectDate();
        initProviceSelectViewEnvent();
        if (Actions.VersionType.CHANNEL_WOAIJIA.equals(MainApplication.app.getAppGlobalConfig().getVersion())){
            adreId = 100000;
            type = 2;
        }else if(Actions.VersionType.CHANNEL_WANGDUODUO.equals(MainApplication.app.getAppGlobalConfig().getVersion())){
            adreId = 100000;
            type = 3 ;
        }

    }

    private void initProviceSelectDate() {
        isDataLoaded = true;
        chooseArea = new ArrayList<>();
        areaInfos = new ArrayList<>();
        list = new ArrayList<>();
        adapter = new AddressAdapter(areaInfos);
        listView.setAdapter(adapter);
    }


    private void initProviceSelectViewEnvent() {
        setAddressResult(this);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                loadNextAdress(areaInfos.get(position));
            }
        });
        boxBtnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                comeBack();
            }
        });

        addrPopWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                chooseArea.clear();
            }
        });
    }

    public void setAreaInfo() {
        addressResult.result(chooseArea, type);
    }

    TextView sAddre, cAddre, qAddre;

    private void initProviceSelectView() {
        list = new ArrayList<>();
        contentView = LayoutInflater.from(this).inflate(R.layout.address_picker, null);
        listView = (ListView) contentView.findViewById(R.id.address_picker);
        boxBtnCancel = (LinearLayout) contentView.findViewById(R.id.box_btn_cancel);
        sAddre = (TextView) contentView.findViewById(R.id.address_show_saddre);
        cAddre = (TextView) contentView.findViewById(R.id.address_show_cdaare);
        qAddre = (TextView) contentView.findViewById(R.id.address_show_qaddre);
        addrPopWindow = new PopupWindow(contentView, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT, true);
        addrPopWindow.setBackgroundDrawable(new BitmapDrawable());

    }

    public void showSelectWin(View parent) {
        sAddre.setText(chooseAdress());
        JavaThreadPool.getInstance().excute(new LoadingAddress(adreId));
        addrPopWindow.showAtLocation(parent, Gravity.BOTTOM, 0, 0);
    }

    @Override
    public void result(List<AreaInfo> areaInfos, int id) {
    }


    class AddressAdapter extends BaseAdapter {
        private List<AreaInfo> areaInfos;

        public AddressAdapter(List<AreaInfo> areaInfos) {
            this.areaInfos = areaInfos;
        }

        @Override
        public int getCount() {
            return areaInfos.size();
        }

        @Override
        public Object getItem(int position) {
            return areaInfos.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            HandleView handleView = null;
            if (convertView == null) {
                convertView = LayoutInflater.from(SelectAddreActivity.this).inflate(R.layout.item_address, null, false);
                handleView = new HandleView(convertView);
                convertView.setTag(handleView);
            } else {
                handleView = (HandleView) convertView.getTag();
            }
            handleView.setValue(areaInfos.get(position));
            return convertView;
        }

        class HandleView {
            private TextView name;

            public HandleView(View view) {
                name = (TextView) view.findViewById(R.id.item_address_name);
            }

            public void setValue(AreaInfo areaInfo) {
                name.setText(String.valueOf(areaInfo.getAreaName()));
            }
        }
    }

    public class LoadingAddress implements Runnable {
        private long pid;

        public LoadingAddress(long pid) {
            this.pid = pid;
            showInProgress("", true, true);
        }

        @Override
        public void run() {
            list.clear();
            JSONObject o = new JSONObject();
            o.put("pid", pid);
            String server = dcsp.getString(DataCenterSharedPreferences.Constant.HTTP_DATA_SERVERS, "");
            String result = HttpRequestUtils
                    .requestoOkHttpPost(server + "/jdm/s3/area/list", o, SelectAddreActivity.this);
            // -1参数为空 -2校验失败 -10服务器不存在
            if (result != null && "0".equals(result)) {
                //没有数据的时候结束该界面
                cancelInProgress();
                handler.sendEmptyMessage(1);

            } else if (result != null && result.length() > 4) {
                JSONArray resultJson = null;
                try {
                    resultJson = JSON.parseArray(result);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                //{"areaCnName":"爱尔兰","areaCnPinyin":"aierlan","areaCode":"IRL","areaEnName":"Ireland","areaLevel":2,"areaParentId":2,"areaPath":",2,19,","id":19,"valid":false}
                ArrayList<AreaInfo> tmp = new ArrayList<>(); //创建一个临时的数组，这里多次操作可能会抛出非法状态异常
                for (int i = 0; i < resultJson.size(); i++) {
                    JSONObject object = (JSONObject) resultJson.get(i);
                    AreaInfo areaInfo = new AreaInfo();
                    areaInfo.setAreaLevel(object.getIntValue("areaLevel"));
                    areaInfo.setId(Integer.parseInt(object.getString("id")));
                    areaInfo.setAreaParentId(Long.parseLong(object.getString("areaParentId")));
                    if (LanguageUtil.isZh(SelectAddreActivity.this)) {
                        areaInfo.setAreaName(object.getString("areaCnName"));
                    } else {
                        if (object.getString("areaEnName").length() == 0) {
                            areaInfo.setAreaName(object.getString("areaCnName"));
                        } else {
                            areaInfo.setAreaName(object.getString("areaEnName"));
                        }
                    }
                    tmp.add(areaInfo);
                }
                areaInfos.clear();
                areaInfos.addAll(tmp);
                handler.sendEmptyMessage(0);
            } else {
                cancelInProgress();
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(mContext, getResources().getString(R.string.net_error_requestfailed), Toast.LENGTH_SHORT).show();
                        adapter.notifyDataSetChanged();
                    }
                });
                return;
            }
        }
    }

    //显示已经选择的地址
    public String chooseAdress() {
        StringBuffer sb = new StringBuffer();
        for (AreaInfo areaInfo : chooseArea) {
            sb.append(areaInfo.getAreaName());
            sb.append(" > ");
        }
        if (chooseArea.size() > 1)
            sb.deleteCharAt(chooseArea.size() - 1);
        return sb.toString();
    }

    //返回按钮
    public void comeBack() {
        if (chooseArea.size() == 0) {
            addrPopWindow.dismiss();
        } else if (chooseArea.size() >0){
            chooseArea.remove(chooseArea.size() - 1);
            if (chooseArea.size() >0) {
                JavaThreadPool.getInstance().excute(new LoadingAddress(chooseArea.get(chooseArea.size() - 1).getId()));
            } else {
                JavaThreadPool.getInstance().excute(new LoadingAddress(adreId));
            }
            sAddre.setText(chooseAdress());
        }

    }

    //选择地址
    public void loadNextAdress(AreaInfo areaInfo) {
        chooseArea.add(areaInfo);
        JavaThreadPool.getInstance().excute(new LoadingAddress(areaInfo.getId()));
        sAddre.setText(chooseAdress());
    }
}
