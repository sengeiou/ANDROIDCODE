package com.smartism.znzk.activity.user;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.smartism.znzk.R;
import com.smartism.znzk.activity.ActivityParentActivity;
import com.smartism.znzk.util.DataCenterSharedPreferences;
import com.smartism.znzk.util.HttpRequestUtils;
import com.smartism.znzk.util.JavaThreadPool;
import com.smartism.znzk.util.LogUtil;
import com.smartism.znzk.util.WeakRefHandler;

import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class GCodeListActivity extends ActivityParentActivity implements AdapterView.OnItemClickListener {
    private ListView glistView;
    private GCodeListAdapter adapter;
    private List<GCodeBean> codeBeanList;

    private Handler.Callback mCallback = new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    cancelInProgress();
                    codeBeanList.addAll((List<GCodeBean>) msg.obj);
                    Collections.sort(codeBeanList, new EnglishComparator());
                    adapter.notifyDataSetChanged();
                    break;
            }
            return false;
        }
    };
    private Handler defHandler = new WeakRefHandler(mCallback);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gcode_list);
        initView();
        initGCodeListInfo();
    }

    public void back(View v) {
        finish();
    }

    protected void onDestroy() {
        defHandler.removeCallbacksAndMessages(null);
        defHandler = null;
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private void initView() {
        glistView = (ListView) findViewById(R.id.gcode_listview);
        glistView.setOnItemClickListener(this);
        codeBeanList = new ArrayList<>();
        adapter = new GCodeListAdapter(mContext);
        glistView.setAdapter(adapter);
    }

    private void initGCodeListInfo() {
        showInProgress(getString(R.string.loading), false, true);
        JavaThreadPool.getInstance().excute(new GCodeListInfoLoad());
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Intent backIntent = new Intent();
        backIntent.putExtra("icon", codeBeanList.get(position).getIcon());
        backIntent.putExtra("aname", codeBeanList.get(position).getAname());
        backIntent.putExtra("country", codeBeanList.get(position).getCountry());
        JSONObject object = new JSONObject();
        object.put("icon",codeBeanList.get(position).getIcon());
        object.put("aname",codeBeanList.get(position).getAname());
        object.put("country",codeBeanList.get(position).getCountry());
        dcsp.putString(DataCenterSharedPreferences.Constant.LOCALE_GCODE,object.toJSONString()).commit();
        setResult(11, backIntent);
        finish();
    }


    class GCodeListInfoLoad implements Runnable {
        @Override
        public void run() {
            String server = dcsp.getString(
                    DataCenterSharedPreferences.Constant.HTTP_DATA_SERVERS, "");
            String result = HttpRequestUtils.requestoOkHttpPost( server + "/jdm/s3/sms/gcodelist", null, GCodeListActivity.this);
            if (!StringUtils.isEmpty(result) && result.length() > 4) {
                List<GCodeBean> beanList = new ArrayList<>();
                try {
                    JSONArray array = JSON.parseArray(result);
                    if (array != null && array.size() > 0) {
                        for (int i = 0; i < array.size(); i++) {
                            JSONObject object = array.getJSONObject(i);
                            GCodeBean bean = new GCodeBean();
                            bean.setAname(object.getString("aname"));
                            bean.seteName(object.getString("ename"));
                            bean.setCountry(object.getString("country"));
                            bean.setCregExp(object.getString("cregExp"));
                            bean.setIcon(object.getString("icon"));
                            bean.setName(object.getString("name"));
                            String ename = object.getString("ename");
                            if (!StringUtils.isEmpty(ename)){
                                String sortString = ename.substring(0, 1).toUpperCase();
                                if (sortString.matches("[A-Z]")) {
                                    bean.setSortLetters(sortString.toUpperCase());
                                }else{
                                    bean.setSortLetters("a");
                                }
                            }else{
                                bean.setSortLetters("a");
                            }
                            beanList.add(bean);
                        }
                    }


                } catch (Exception e) {
                    LogUtil.e(GCodeListActivity.this,"GCodeListActivity","国家代码解析失败",e);
                }

                Message m = defHandler.obtainMessage(1);
//                m.obj = filledData(beanList);
                m.obj = beanList;
                defHandler.sendMessage(m);
            } else if ("[]".equals(result)) {
                defHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        cancelInProgress();
                        Toast.makeText(mContext, getString(R.string.net_error_nodata), Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                defHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        cancelInProgress();
                        Toast.makeText(mContext, getString(R.string.net_error_requestfailed), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }
    }

    private List<GCodeBean> filledData(List<GCodeBean> data) {
        List<GCodeBean> mSortList = new ArrayList<GCodeBean>();
        for (int i = 0; i < data.size(); i++) {

            GCodeBean bean = new GCodeBean();
            bean.seteName(data.get(i).geteName());
            bean.setAname(data.get(i).getAname());
            bean.setCountry(data.get(i).getCountry());
            bean.setCregExp(data.get(i).getCregExp());
            bean.setIcon(data.get(i).getIcon());
            bean.setName(data.get(i).getName());
            // 汉字转换成拼音
            String pinyin = bean.geteName();
            String sortString = pinyin.substring(0, 1).toUpperCase();

            if (sortString.matches("[A-Z]")) {
                bean.setSortLetters(sortString.toUpperCase());
            }
            mSortList.add(bean);
        }
        return mSortList;
    }

    public class GCodeListAdapter extends BaseAdapter {
        class GCodeInfoView {
            ImageView icon;
            TextView aname, name, ename;
        }

        Context mContext;

        public GCodeListAdapter(Context context) {
            this.mContext = context;
        }

        @Override
        public int getCount() {
            return codeBeanList.size();
        }

        @Override
        public Object getItem(int arg0) {
            return codeBeanList.get(arg0);
        }

        @Override
        public long getItemId(int arg0) {
            return 0;
        }

        @Override
        public View getView(final int position, View view, ViewGroup arg2) {
            GCodeInfoView gCodeInfoView = null;
            if (null == view) {
                gCodeInfoView = new GCodeInfoView();
                view = LayoutInflater.from(mContext).inflate(R.layout.list_gcode_item, null);
                gCodeInfoView.icon = (ImageView) view.findViewById(R.id.icon);
                gCodeInfoView.name = (TextView) view.findViewById(R.id.name);
                gCodeInfoView.aname = (TextView) view.findViewById(R.id.aname);
                gCodeInfoView.ename = (TextView) view.findViewById(R.id.ename);
                view.setTag(gCodeInfoView);
            } else {
                gCodeInfoView = (GCodeInfoView) view.getTag();
            }
            GCodeBean bean = codeBeanList.get(position);
            ImageLoader.getInstance().displayImage(bean.getIcon(), gCodeInfoView.icon);
            gCodeInfoView.name.setText(bean.getName());
            gCodeInfoView.ename.setText(bean.geteName());
            gCodeInfoView.aname.setText(bean.getAname());
            return view;
        }

    }

    private class GCodeBean {
        private String country;//国家代码例如：0086 中国  001美国或者加拿大
        private String name;//名称
        private String eName;//英文名称
        private String aname;//别称名称 +86
        private String icon; //图标
        private String cregExp;//当前国家的号码验证正则

        public String getSortLetters() {
            return sortLetters;
        }

        public void setSortLetters(String sortLetters) {
            this.sortLetters = sortLetters;
        }

        private String sortLetters;  //显示数据拼音的首字母（一定需要的属性，用来进行排序）

        public String geteName() {
            return eName;
        }

        public void seteName(String eName) {
            this.eName = eName;
        }

        public String getCountry() {
            return country;
        }

        public void setCountry(String country) {
            this.country = country;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getAname() {
            return aname;
        }

        public void setAname(String aname) {
            this.aname = aname;
        }

        public String getIcon() {
            return icon;
        }

        public void setIcon(String icon) {
            this.icon = icon;
        }

        public String getCregExp() {
            return cregExp;
        }

        public void setCregExp(String cregExp) {
            this.cregExp = cregExp;
        }

        @Override
        public String toString() {
            return "[ ename:" + eName + "]";
        }
    }

   class EnglishComparator implements Comparator<GCodeBean>{
       @Override
       public int compare(GCodeBean lhs, GCodeBean rhs) {
           return lhs.getSortLetters().compareTo(rhs.getSortLetters());
       }
   }
}
