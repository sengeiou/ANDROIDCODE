package com.smartism.znzk.activity.device;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.p2p.core.P2PHandler;
import com.smartism.znzk.R;
import com.smartism.znzk.activity.ActivityParentMonitorActivity;
import com.smartism.znzk.camera.P2PConnect;
import com.smartism.znzk.communication.protocol.SyncMessage;
import com.smartism.znzk.communication.protocol.SyncMessageContainer;
import com.smartism.znzk.db.DatabaseOperator;
import com.smartism.znzk.db.camera.Contact;
import com.smartism.znzk.domain.DeviceInfo;
import com.smartism.znzk.domain.DeviceKeys;
import com.smartism.znzk.global.Constants;
import com.smartism.znzk.util.DataCenterSharedPreferences;
import com.smartism.znzk.util.HttpRequestUtils;
import com.smartism.znzk.util.JavaThreadPool;
import com.smartism.znzk.util.SecurityUtil;
import com.smartism.znzk.util.WeakRefHandler;
import com.smartism.znzk.util.camera.T;
import com.smartism.znzk.view.MyGridView;

import org.apache.commons.lang.StringUtils;

import java.io.Serializable;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

public class EditKeyActivity extends ActivityParentMonitorActivity {
    private List<CommandKey> keys;
    private EditKeyItemAdapter editKeyItemAdapter;
    private GridView keysgGridView; //正常的指令面板
    DeviceInfo deviceInfo;

    private Handler.Callback mCallback = new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            if (msg.what == 11) { // 键值获取成功
                defHandler.removeMessages(6);
            }
            return false;
        }
    };
    private Handler defHandler = new WeakRefHandler(mCallback);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_key);
        deviceInfo = (DeviceInfo) getIntent().getSerializableExtra("device");
        List<DeviceKeys> deviceKeyses = DatabaseOperator.getInstance(EditKeyActivity.this).findDeviceKeysByDeviceId(deviceInfo.getId());
        if (deviceKeyses!=null&&!deviceKeyses.isEmpty()) {
            keys = initKeys(deviceKeyses);
        }
        editKeyItemAdapter = new EditKeyItemAdapter(this);
        keysgGridView = (GridView) findViewById(R.id.command_key);
        keysgGridView.setAdapter(editKeyItemAdapter);
    }
    public List<CommandKey> initKeys(List<DeviceKeys> deviceKeyses){
        List<CommandKey> commandKeys = new ArrayList<>();
        for (DeviceKeys deviceKeys: deviceKeyses){
            CommandKey commandKey = new CommandKey();
            commandKey.setId(deviceKeys.getDeviceId());
            commandKey.setName(deviceKeys.getKeyName());
            commandKey.setIoc(deviceKeys.getKeyIco());
            commandKey.setSort(deviceKeys.getKeySort());
            commandKey.setWhere(deviceKeys.getKeyWhere());
            commandKeys.add(commandKey);
        }
        return commandKeys;
    }
    public void edit(View view){
        saveModify();
    }
    public void back(View v) {
        Intent intent = new Intent();
        intent.setClass(EditKeyActivity.this,DeviceInfoActivity.class);
        intent.putExtra("device", deviceInfo);
        intent.putExtra("contact", getIntent().getSerializableExtra("camera"));
        intent.putExtra("group" ,(DeviceInfo)getIntent().getSerializableExtra("group"));
        intent.putExtra("camera" , (Contact)getIntent().getSerializableExtra("camera"));
        intent.putExtra("connectType", Constants.ConnectType.P2PCONNECT);
        startActivity(intent);
        finish();
    }


    class EditKeyItemAdapter extends BaseAdapter {
        /**
         * 视图内部类
         *
         * @author Administrator
         *
         */
        class DeviceInfoView {
            Spinner edit_keybg;
            EditText edit_keyname;

        }

        LayoutInflater layoutInflater;

        public EditKeyItemAdapter(Context context) {
            layoutInflater = LayoutInflater.from(context);
        }

        @Override
        public int getCount() {
            return keys.size();
        }

        @Override
        public Object getItem(int position) {
            return keys.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(final int position, View view, ViewGroup parent) {
            DeviceInfoView viewCache = new DeviceInfoView();
            if (view == null) {
                view = layoutInflater.inflate(R.layout.activity_history_edit_key_item, null);
                viewCache.edit_keybg = (Spinner) view.findViewById(R.id.edit_keybg);
                viewCache.edit_keyname = (EditText) view.findViewById(R.id.edit_keyname);
                view.setTag(viewCache);
            } else {
                viewCache = (DeviceInfoView) view.getTag();
            }

            viewCache.edit_keyname.setText(keys.get(position).getName());
            final List<String> resId = new ArrayList<String>();
            resId.add("checkbutton_circular");
            resId.add("checkbutton_rectangle");
            resId.add("checkbutton_square");
            resId.add("checkbutton_triangle");
            for (int i = 0; i < resId.size(); i++) {
                if (keys.get(position).getIoc() != null && keys.get(position).getIoc().equals(resId.get(i))) {
                    String temp = resId.get(0);
                    resId.set(0, resId.get(i));
                    resId.set(i, temp);

                }
            }
            MyAdapter mAdapter = new MyAdapter(resId);
            final EditText et = viewCache.edit_keyname;
            viewCache.edit_keybg.setAdapter(mAdapter);
            viewCache.edit_keyname.addTextChangedListener(new TextWatcher() {
                private CharSequence temp;
                private int editStart;
                private int editEnd;
                private int maxLen = 8;

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                }

                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    temp = s;
                }

                @Override
                public void afterTextChanged(Editable s) {
                    editStart = et.getSelectionStart();
                    editEnd = et.getSelectionEnd();
                    Log.i("gongbiao1", "" + editStart);
                    if (calculateLength(s.toString()) > maxLen) {
                        s.delete(editStart - 1, editEnd);
                        int tempSelection = editStart;
                        et.setText(s);
                        et.setSelection(tempSelection);
                    }

                    keys.get(position).setName(s.toString());
                }

                private int calculateLength(String etstring) {
                    char[] ch = etstring.toCharArray();

                    int varlength = 0;
                    for (int i = 0; i < ch.length; i++) {
                        if ((ch[i] >= 0x2E80 && ch[i] <= 0xFE4F) || (ch[i] >= 0xA13F && ch[i] <= 0xAA40)
                                || ch[i] >= 0x80) { // 中文字符范围0x4e00 0x9fbb
                            varlength = varlength + 2;
                        } else {
                            varlength++;
                        }
                    }
                    // 这里也可以使用getBytes,更准确嘛
                    // varlength =
                    // etstring.getBytes(CharSet.forName(GBK)).lenght;//
                    // 编码根据自己的需求，注意u8中文占3个字节...
                    return varlength;
                }
            });

            viewCache.edit_keybg.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                    keys.get(position).setIoc(resId.get(pos));
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });

            return view;
        }

    }

    class MyAdapter extends BaseAdapter {
        private List<String> resId;

        public MyAdapter(List<String> resId) {
            this.resId = resId;
        }

        @Override
        public int getCount() {
            return resId.size();
        }

        @Override
        public Object getItem(int position) {
            return resId.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            SpinnerImage spImg = new SpinnerImage();

            if (convertView == null) {
                convertView = View.inflate(EditKeyActivity.this, R.layout.spinner_image_item, null);
                spImg.iv_spinner = (ImageView) convertView.findViewById(R.id.iv_spinner);
                convertView.setTag(spImg);
            } else {
                spImg = (SpinnerImage) convertView.getTag();
            }
            spImg.iv_spinner.setImageResource(
                    getResources().getIdentifier(resId.get(position), "drawable", getBaseContext().getPackageName()));

            return convertView;

        }

        class SpinnerImage {
            ImageView iv_spinner;

        }

    }

    private void saveModify() {
        final long did = deviceInfo.getId();
        JavaThreadPool.getInstance().excute(new Runnable() {

            @Override
            public void run() {
                String server = dcsp.getString(DataCenterSharedPreferences.Constant.HTTP_DATA_SERVERS, "");
                JSONObject pJsonObject = new JSONObject();
                pJsonObject.put("did", did);
                JSONArray p = new JSONArray();
                for (CommandKey key : keys) {
                    JSONObject o = new JSONObject();
                    o.put("id", key.getId());
                    o.put("n", key.getName());
                    o.put("i", key.getIoc());
                    o.put("w", key.getWhere());
                    p.add(o);

                }
                pJsonObject.put("keys", p);
//                final String result = HttpRequestUtils
//                        .requestHttpServer(
//                                 server + "/jdm/service/dkeyupdate?v="
//                                        + URLEncoder.encode(
//                                        SecurityUtil.crypt(pJsonObject.toJSONString(), DataCenterSharedPreferences.Constant.KEY_HTTP)),
//                                EditKeyActivity.this, defHandler);

                final String result = HttpRequestUtils
                        .requestoOkHttpPost( server + "/jdm/s3/d/dkeyupdate",pJsonObject,EditKeyActivity.this);
                if ("0".equals(result)) {
                    defHandler.post(new Runnable() {

                        @Override
                        public void run() {
                            cancelInProgress();
                            Toast.makeText(EditKeyActivity.this,
                                    getString(R.string.activity_editscene_modify_success), Toast.LENGTH_LONG).show();
                            defHandler.sendEmptyMessage(11);
                        }
                    });
                }
            }
        });
    }

    class CommandKey implements Serializable {
        private long id;
        private int sort;
        private String name;
        private String ioc;
        private int where;

        public long getId() {
            return id;
        }

        public void setId(long id) {
            this.id = id;
        }

        public int getSort() {
            return sort;
        }

        public void setSort(int sort) {
            this.sort = sort;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getIoc() {
            return ioc;
        }

        public void setIoc(String ioc) {
            this.ioc = ioc;
        }

        public int getWhere() {
            return where;
        }

        public void setWhere(int where) {
            this.where = where;
        }

        @Override
        public String toString() {
            return "CommandKey [id=" + id + ", sort=" + sort + ", name=" + name + ", ioc=" + ioc + ", where=" + where
                    + "]";
        }

    }
}
