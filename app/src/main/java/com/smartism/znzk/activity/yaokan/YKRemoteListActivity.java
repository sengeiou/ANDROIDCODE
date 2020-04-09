package com.smartism.znzk.activity.yaokan;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.smartism.znzk.R;
import com.smartism.znzk.activity.ActivityParentActivity;
import com.smartism.znzk.domain.yankan.MatchRemoteControl;
import com.smartism.znzk.domain.yankan.MatchRemoteControlResult;
import com.smartism.znzk.util.Actions;

import java.util.ArrayList;
import java.util.List;


/**
 * 品牌型号列表
 */
public class YKRemoteListActivity extends ActivityParentActivity {

    private ListView lv;
    private EditText et_search;
    private String tname;
    private String bname;
    private Long did;
    private TextView tv_no_data;
    private List<MatchRemoteControl> rs = new ArrayList<MatchRemoteControl>();
    private MatchRemoteControlResult matchInfo;
    private RemoteAdapter adapter;

    //搜索功能用到
    private ArrayList<String> base = new ArrayList<String>();
    private ArrayList<String> temp = new ArrayList<String>();
    private String index;
    private String result;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_remote_list_detail);
        initView();
        initValue();
        initEvent();
        Log.d("taskId:", "devicelist" + getTaskId() + "");
    }

    private void initValue() {
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        matchInfo = (MatchRemoteControlResult) getIntent().getSerializableExtra("matchInfo");
        if (matchInfo == null) {
            et_search.setVisibility(View.GONE);
            lv.setVisibility(View.GONE);
            tv_no_data.setVisibility(View.VISIBLE);
        } else {
            tv_no_data.setVisibility(View.GONE);
            rs = matchInfo.getRs();
        }

        tname = getIntent().getStringExtra("tname");
        bname = getIntent().getStringExtra("bname");
        did = getIntent().getLongExtra("did", 0);
        adapter = new RemoteAdapter();
        lv.setAdapter(adapter);
        for (int i = 0; i < rs.size(); i++) {
            result = rs.get(i).getRmodel();
            flag:
            for (int j = 0; j < result.length(); j++) {
                if (Character.isLetter(result.charAt(j))) {
                    base.add(result);
                    temp.add(result);
                    break flag;
                }
            }
        }
        if (rs.size() > 0 && temp.size() == 0) {
            temp.add(getString(R.string.hwzf_no_type));
        }
    }

    private void initView() {
        lv = (ListView) findViewById(R.id.lv);
        et_search = (EditText) findViewById(R.id.et_search);
        et_search.clearFocus();
        tv_no_data = (TextView) findViewById(R.id.tv_no_data);
    }

    private void initEvent() {

        et_search.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                temp.clear();
            }

            @Override
            public void afterTextChanged(Editable s) {
                //Log.e("aaa",s.toString());
                Vertify(s.toString());
            }

            private void Vertify(String s) {
                for (int i = 0; i < base.size(); i++) {
                    if (base.get(i).contains(s)) {
                        temp.add(base.get(i));
                    }
                }
                if (rs.size() > 0 && temp.size() == 0) {
                    temp.add(getString(R.string.hwzf_no_type));
                }
                //			adapter.notifyDataSetChanged();
                runOnUiThread(new Runnable() {
                    public void run() {
                        lv.setAdapter(adapter);
                    }
                });
            }
        });
        lv.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                if (temp.get(position).equals(getString(R.string.hwzf_no_type))) {
                    index = temp.get(0);
                } else {
                    index = temp.get(position);
                }

                Intent intent = new Intent();
                intent.putExtra("index", index);
                intent.putExtra("tname", tname);
                intent.putExtra("bname", bname);
                intent.putExtra("did", did);
                //intent.putExtra("sort",position);
                intent.putExtra("matchInfo", matchInfo);
                intent.setClass(getApplicationContext(), YKDetailActivity.class);
                startActivity(intent);
            }
        });
        IntentFilter filter = new IntentFilter(Actions.FINISH_YK_EXIT);
        registerReceiver(receiver, filter);
    }

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            finish();
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (receiver != null) {
            unregisterReceiver(receiver);
        }
    }


    class RemoteAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return temp.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder = null;
            if (convertView == null) {
                convertView = View.inflate(getApplicationContext(), R.layout.list_yk_dev_item, null);
                holder = new ViewHolder();
                holder.tv = (TextView) convertView.findViewById(R.id.tv);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            holder.tv.setText(temp.get(position));
            convertView.findViewById(R.id.iv).setVisibility(View.GONE);
            return convertView;
        }
    }


    class ViewHolder {
        TextView tv;
    }


    public void back(View v) {
        finish();
    }

}
