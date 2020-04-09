package com.smartism.znzk.activity.device.add;

import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSONObject;
import com.smartism.znzk.R;
import com.smartism.znzk.activity.ActivityParentActivity;
import com.smartism.znzk.activity.device.DeviceMainActivity;
import com.smartism.znzk.activity.device.ZhujiListFragment;
import com.smartism.znzk.communication.protocol.SyncMessage;
import com.smartism.znzk.communication.protocol.SyncMessage.CodeMenu;
import com.smartism.znzk.communication.protocol.SyncMessage.CommandMenu;
import com.smartism.znzk.communication.protocol.SyncMessageContainer;
import com.smartism.znzk.db.DatabaseOperator;
import com.smartism.znzk.domain.CategoryInfo;
import com.smartism.znzk.domain.ZhujiInfo;
import com.smartism.znzk.util.Actions;
import com.smartism.znzk.util.DataCenterSharedPreferences.Constant;
import com.smartism.znzk.util.WeakRefHandler;
import com.smartism.znzk.view.MyGridView;
import com.smartism.znzk.view.alertview.AlertView;
import com.smartism.znzk.view.alertview.AlertView.Style;
import com.smartism.znzk.view.alertview.OnDismissListener;
import com.smartism.znzk.view.alertview.OnItemClickListener;
import com.smartism.znzk.view.pickerview.OptionsPickerView;
import com.smartism.znzk.view.pickerview.OptionsPickerView.OnOptionsSelectListener;

import org.apache.commons.lang.StringUtils;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

public class AddDeviceOfDIYActivity extends ActivityParentActivity implements OnClickListener {
    private int backCount = 0;
    private int STUDY_TIMEOUT = 10;//超时时间为10秒
    private boolean inStudy = false;
    private AlertView autoPeiTips;
    private Button startStudy, exitStudy, exitFinishStudy;
    private RelativeLayout study_layout;
    //private GridView commandGridView;
    private MyGridView myGridView;
    private KeyItemAdapter keyItemAdapter;
    private CategoryInfo cInfo;
    private ZhujiInfo zhuji;
    private List<CommandKey> keys;
    private TextView regiter_title;
    //播放声音
    private SoundPool soundPool = null;
    //声音的资源id
    private int sourceid;
    //遥控器学码方式 默认为RF
    private int function = 0;
    private OptionsPickerView<FunctionBean> funOptions;
    private TextView t_fun;
    private ArrayList<FunctionBean> fs;
    private RelativeLayout fun_layout;
    private BroadcastReceiver defaultReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context arg0, Intent intent) {
            if (Actions.STUDY_ACTIONS.equals(intent.getAction())) { //学习返回
                int command = intent.getIntExtra("command", 0);
                int code = intent.getIntExtra("code", 0);
                int c = intent.getIntExtra("c", 0);
                int s = intent.getIntExtra("s", 0);
                Log.e("loglog", command + "-" + code + "-" + c + "-" + s);
                if (command == CommandMenu.rp_pStudy.value() && code == CodeMenu.rp_pStudy_into.value()) {
                    inStudy = true;
                    //成功进入学习模式
                    cancelInProgress();
                    //commandGridView.setVisibility(View.VISIBLE);
                    myGridView.setVisibility(View.VISIBLE);
                    startStudy.setVisibility(View.GONE);
                    study_layout.setVisibility(View.VISIBLE);
                    defaultHandler.removeMessages(1);
                    defaultHandler.sendMessageDelayed(defaultHandler.obtainMessage(1), STUDY_TIMEOUT * 1000);
                } else if (command == CommandMenu.rp_pStudy.value() && code == CodeMenu.rp_pStudy_command.value()) {
                    defaultHandler.removeMessages(1);
                    defaultHandler.sendMessageDelayed(defaultHandler.obtainMessage(1), STUDY_TIMEOUT * 1000);
                    //成功完成一个按键的学习
                    if (s == 0) {
                        if (c >= 1 && keys.get(c - 1) != null) {
                            CommandKey key = keys.get(c - 1);
                            key.setName(getString(R.string.activity_add_device_diy_sfail));
                            key.setIoc("1");
                            keys.remove(c - 1);
                            keys.add(c - 1, key);
                        }
                    } else if (s == 1) {
                        if (c >= 1 && keys.get(c - 1) != null) {
                            CommandKey key = keys.get(c - 1);
                            key.setName(getString(R.string.activity_add_device_diy_success));
                            key.setIoc("1");
                            keys.remove(c - 1);
                            keys.add(c - 1, key);
                        }
                    }
                    if (s == 2) {
                        defaultHandler.removeMessages(1);
                        autoPeiTips.show();
                    }
                    keyItemAdapter.notifyDataSetChanged();
                    boolean needShowFinish = true;
                    for (CommandKey key: keys) {
                        if ("0".equals(key.getIoc())){
                            needShowFinish = false;
                            break;
                        }
                    }
                    if (needShowFinish){
                        showInProgress(getString(R.string.operationing));
                        exitFinishStudy.setClickable(false);
                    }
                } else if (command == CommandMenu.rp_pStudyE.value() && code == CodeMenu.rp_pStudyE.value()) {
                    defaultHandler.removeMessages(1);
                    finish();
                } else if (command == CommandMenu.rp_pStudyE.value() && code == CodeMenu.rp_pStudyE_finish.value()) {
                    regiter_title.setText(getString(R.string.activity_add_device_diy_title_finish));
                    defaultHandler.removeMessages(1);
                    soundPool.play(sourceid, 1, 1, 0, 0, 1);
                    SyncMessageContainer.getInstance()
                            .produceSendMessage(new SyncMessage(SyncMessage.CommandMenu.rq_refresh));
                    Toast.makeText(AddDeviceOfDIYActivity.this, getString(R.string.activity_add_device_diy_success), Toast.LENGTH_LONG).show();
//                    finish();
                    Intent in = new Intent();
                    in.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    in.setClass(AddDeviceOfDIYActivity.this, DeviceMainActivity.class);
                    startActivity(in);
                }

            }
        }
    };


    private Handler.Callback mCallback = new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            cancelInProgress();
            switch (msg.what) {
                case 1:
                    cancelInProgress();
                    new AlertView(getString(R.string.tips), getString(R.string.timeout), null,
                            new String[]{getString(R.string.sure)}, null, AddDeviceOfDIYActivity.this, Style.Alert, new OnItemClickListener() {

                        @Override
                        public void onItemClick(Object o, int position) {
                            if (inStudy && backCount < 3) {
                                backCount++;
                                showInProgress(getString(R.string.activity_add_device_diy_submit_exit), false, false);
                                defaultHandler.sendEmptyMessageDelayed(1, 5000);
                                //发送退出学习指令 不完成学习
                                SyncMessage message = new SyncMessage();
                                message.setCommand(CommandMenu.rq_pStudyE.value());
                                message.setCode(CodeMenu.rq_pStudyE.value());
                                message.setDeviceid(zhuji.getId());
                                SyncMessageContainer.getInstance().produceSendMessage(message);
                            } else {
                                finish();
                            }
                        }
                    }).show();
                    break;
            }
            return false;
        }
    };
    private Handler defaultHandler = new WeakRefHandler(mCallback);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_device_diy);
        initView();
        initData();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK
                && event.getAction() == KeyEvent.ACTION_DOWN && inStudy && backCount <= 3) {
            backCount++;
            showInProgress(getString(R.string.activity_add_device_diy_submit_exit), false, false);
            defaultHandler.sendEmptyMessageDelayed(1, 5000);
            //发送退出学习指令 不完成学习
            SyncMessage message = new SyncMessage();
            message.setCommand(CommandMenu.rq_pStudyE.value());
            message.setCode(CodeMenu.rq_pStudyE.value());
            message.setDeviceid(zhuji.getId());
            SyncMessageContainer.getInstance().produceSendMessage(message);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void initView() {
        // 注册广播
        IntentFilter receiverFilter = new IntentFilter();
        receiverFilter.addAction(Actions.STUDY_ACTIONS);
        registerReceiver(defaultReceiver, receiverFilter);
        startStudy = (Button) findViewById(R.id.add_study_btn);
        startStudy.setOnClickListener(this);
        exitStudy = (Button) findViewById(R.id.add_study_exit_btn);
        exitStudy.setOnClickListener(this);
        regiter_title = (TextView) findViewById(R.id.regiter_title);
        exitFinishStudy = (Button) findViewById(R.id.add_study_exit_finish_btn);
        exitFinishStudy.setOnClickListener(this);
        study_layout = (RelativeLayout) findViewById(R.id.study_layout);
        //commandGridView = (GridView) findViewById(R.id.study_command_key);
        myGridView = (MyGridView) findViewById(R.id.study_command_key);
        autoPeiTips = new AlertView(getString(R.string.tips), getString(R.string.activity_add_device_diy_have),
                null, new String[]{getString(R.string.sure)}, null, AddDeviceOfDIYActivity.this, Style.Alert, null);
        autoPeiTips.setOnDismissListener(new OnDismissListener() {

            @Override
            public void onDismiss(Object o) {
                if (inStudy && backCount < 3) {
                    backCount++;
                    showInProgress(getString(R.string.activity_add_device_diy_submit_exit), false, false);
                    defaultHandler.sendEmptyMessageDelayed(1, 5000);
                    //发送退出学习指令 不完成学习
                    SyncMessage message = new SyncMessage();
                    message.setCommand(CommandMenu.rq_pStudyE.value());
                    message.setCode(CodeMenu.rq_pStudyE.value());
                    message.setDeviceid(zhuji.getId());
                    SyncMessageContainer.getInstance().produceSendMessage(message);
                } else {
                    finish();
                }
            }
        });
        t_fun = (TextView) findViewById(R.id.diy_type);
        fun_layout = (RelativeLayout) findViewById(R.id.diy_layout);
        fun_layout.setOnClickListener(this);
        funOptions = new OptionsPickerView<FunctionBean>(this);
        fs = new ArrayList<FunctionBean>();
        FunctionBean bean = new FunctionBean();
        bean.setValue(0);
        bean.setName("RF");
        fs.add(bean);
        bean = new FunctionBean();
        bean.setValue(1);
        bean.setName("IR");
        fs.add(bean);
        funOptions.setTitle(getString(R.string.activity_add_device_diy_function));
        funOptions.setPicker(fs);
        funOptions.setCyclic(false);
        funOptions.setOnoptionsSelectListener(new OnOptionsSelectListener() {
            @Override
            public void onOptionsSelect(int options1, int option2, int options3) {
                function = fs.get(options1).getValue();
                t_fun.setText(fs.get(options1).getName());
            }
        });
    }

    private void initData() {
        //载入音频流，返回在池中的id
        createSoundPool();
        sourceid = soundPool.load(this, R.raw.pdsuccess, 0);
//        zhuji = DatabaseOperator.getInstance(getApplicationContext()).queryDeviceZhuJiInfo(dcsp.getString(Constant.APP_MASTERID, ""));
        //替换
        zhuji = DatabaseOperator.getInstance(getApplicationContext()).queryDeviceZhuJiInfo(ZhujiListFragment.getMasterId());
        keys = new ArrayList<CommandKey>();
        cInfo = (CategoryInfo) getIntent().getSerializableExtra("category");
        int k = 1;
        if (cInfo.getCkey() != null && cInfo.getCkey().startsWith("ykq_")) {
            k = Integer.parseInt(cInfo.getCkey().substring(cInfo.getCkey().indexOf("_") + 1));
        }
        for (int i = 0; i < k; i++) {
            CommandKey key = new CommandKey();
            key.setSort(i + 1);
            key.setName(getString(R.string.activity_add_device_diy_pt));
            key.setIoc("0");
            keys.add(key);
        }
        keyItemAdapter = new KeyItemAdapter(AddDeviceOfDIYActivity.this);
        //commandGridView.setAdapter(keyItemAdapter);
        myGridView.setAdapter(keyItemAdapter);
    }

    protected void createSoundPool() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            createNewSoundPool();
        } else {
            createOldSoundPool();
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    protected void createNewSoundPool() {
        //指定声音池的最大音频流数目为10，声音品质为5
        AudioAttributes.Builder b = new AudioAttributes.Builder();
        b.setUsage(AudioAttributes.USAGE_GAME);
        b.setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION);
        SoundPool.Builder sBuilder = new SoundPool.Builder();
        sBuilder.setMaxStreams(10);
        sBuilder.setAudioAttributes(b.build());
        soundPool = sBuilder.build();
    }

    @SuppressWarnings("deprecation")
    protected void createOldSoundPool() {
        //指定声音池的最大音频流数目为10，声音品质为5
        soundPool = new SoundPool(5, AudioManager.STREAM_MUSIC, 0);
    }

    public void back(View v) {
        if (inStudy && backCount < 3) {
            backCount++;
            showInProgress(getString(R.string.activity_add_device_diy_submit_exit));
            defaultHandler.sendEmptyMessageDelayed(1, 5000);
            //发送退出学习指令 不完成学习
            SyncMessage message = new SyncMessage();
            message.setCommand(CommandMenu.rq_pStudyE.value());
            message.setCode(CodeMenu.rq_pStudyE.value());
            message.setDeviceid(zhuji.getId());
            SyncMessageContainer.getInstance().produceSendMessage(message);
        } else {
            finish();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.add_study_btn:
                if (zhuji == null) {
                    Toast.makeText(AddDeviceOfDIYActivity.this, getString(R.string.zjnothave), Toast.LENGTH_SHORT).show();
                    return;
                }
                if (function == -1) {
                    Toast.makeText(AddDeviceOfDIYActivity.this, getString(R.string.activity_add_device_diy_function_hit), Toast.LENGTH_SHORT).show();
                    return;
                }
                showInProgress(getString(R.string.activity_add_device_diy_submit_into), false, false);
                defaultHandler.sendEmptyMessageDelayed(1, 5000);
                //发送进入学习指令
                SyncMessage message = new SyncMessage();
                message.setCommand(CommandMenu.rq_pStudy.value());
                message.setCode(CodeMenu.rq_pStudy_into.value());
                message.setDeviceid(zhuji.getId());
                JSONObject o = new JSONObject();
                o.put("c", cInfo.getId());
                o.put("t", function);
                o.put("p", "1527");
                Log.e("loglog", cInfo.getId() + "--" + function + "--" + "1527");
                try {
                    message.setSyncBytes(o.toJSONString().getBytes("UTF-8"));
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                SyncMessageContainer.getInstance()
                        .produceSendMessage(message);
                break;
            case R.id.add_study_exit_btn:
                if (zhuji == null) {
                    Toast.makeText(AddDeviceOfDIYActivity.this, getString(R.string.zjnothave), Toast.LENGTH_SHORT).show();
                    return;
                }
                new AlertView(getString(R.string.tips), getString(R.string.activity_add_device_diy_buttonexit_tip),
                        getString(R.string.cancel), new String[]{getString(R.string.sure)}, null, AddDeviceOfDIYActivity.this, Style.Alert, new OnItemClickListener() {

                    @Override
                    public void onItemClick(Object o, int position) {
                        if (position != -1) {
                            if (inStudy && backCount < 3) {
                                backCount++;
                                showInProgress(getString(R.string.activity_add_device_diy_submit_exit), false, false);
                                defaultHandler.sendEmptyMessageDelayed(1, 5000);
                                //发送退出学习指令
                                SyncMessage message = new SyncMessage();
                                message.setCommand(CommandMenu.rq_pStudyE.value());
                                message.setCode(CodeMenu.rq_pStudyE.value());
                                message.setDeviceid(zhuji.getId());
                                SyncMessageContainer.getInstance()
                                        .produceSendMessage(message);
                            } else {
                                finish();
                            }
                        }
                    }
                }).show();
                break;
            case R.id.add_study_exit_finish_btn:
                if (zhuji == null) {
                    Toast.makeText(AddDeviceOfDIYActivity.this, getString(R.string.zjnothave), Toast.LENGTH_SHORT).show();
                    return;
                }
                new AlertView(getString(R.string.tips), getString(R.string.
                        activity_add_device_diy_buttonfinish_tip),
                        getString(R.string.cancel), new String[]{getString(R.string.sure)}, null, AddDeviceOfDIYActivity.this, Style.Alert, new OnItemClickListener() {

                    @Override
                    public void onItemClick(Object o, int position) {
                        if (position != -1) {
                            if (inStudy && backCount < 3) {
                                backCount++;
                                showInProgress(getString(R.string.activity_add_device_diy_submit_exit), false, false);
                                defaultHandler.sendEmptyMessageDelayed(1, 5000);
                                //发送退出学习指令
                                SyncMessage message = new SyncMessage();
                                message.setCommand(CommandMenu.rq_pStudyE.value());
                                message.setCode(CodeMenu.rq_pStudyE_finish.value());
                                message.setDeviceid(zhuji.getId());
                                SyncMessageContainer.getInstance()
                                        .produceSendMessage(message);
                            } else {
                                finish();
                            }
                        }
                    }
                }).show();
                break;
            case R.id.diy_layout:
                funOptions.show();
                break;
            default:
                break;
        }
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(defaultReceiver);
        defaultHandler.removeCallbacksAndMessages(null);
        defaultHandler = null;
        super.onDestroy();

    }

    /**
     * 播放配对成功提示音
     */
    private void startMusic() {
        try {
        } catch (Exception e) {
            Log.e("AlertMessage", "播放音乐失败", e);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    class KeyItemAdapter extends BaseAdapter {
        /**
         * 视图内部类
         *
         * @author Administrator
         */
        class DeviceInfoView {
            ImageView keybg;
            TextView keyname;
        }

        LayoutInflater layoutInflater;

        public KeyItemAdapter(Context context) {
            layoutInflater = LayoutInflater.from(context);
        }

        @Override
        public int getCount() {
            // TODO Auto-generated method stub
            return keys.size();
        }

        @Override
        public Object getItem(int position) {
            // TODO Auto-generated method stub
            return keys.get(position);
        }

        @Override
        public long getItemId(int position) {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public View getView(final int position, View view, ViewGroup parent) {
            DeviceInfoView viewCache = new DeviceInfoView();
            if (view == null) {
                view = layoutInflater.inflate(
                        R.layout.activity_add_device_diy_key_item, null);
                viewCache.keybg = (ImageView) view.findViewById(R.id.dinfo_keybg);
                viewCache.keyname = (TextView) view.findViewById(R.id.dinfo_keyname);
                view.setTag(viewCache);
            } else {
                viewCache = (DeviceInfoView) view.getTag();
            }
            viewCache.keyname.setText(keys.get(position).getName());
            if (!StringUtils.isEmpty(keys.get(position).getIoc()) && "1".equals(keys.get(position).getIoc())) {
                viewCache.keybg.setBackgroundResource(R.drawable.bg_adddevice_diy_pressed);
                viewCache.keyname.setText("");
            } else {
                viewCache.keybg.setBackgroundResource(R.drawable.bg_adddevice_diy_normal);
            }
            return view;
        }

    }

    class CommandKey {
        private int sort;
        private String name;
        private String ioc;
        private int where;

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

    }

    class FunctionBean {
        private int value;
        private String name;

        public int getValue() {
            return value;
        }

        public void setValue(int value) {
            this.value = value;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        //这个用来显示在PickerView上面的字符串,PickerView会通过反射获取getPickerViewText方法显示出来。
        public String getPickerViewText() {
            //这里还可以判断文字超长截断再提供显示
            return name;
        }
    }
}
