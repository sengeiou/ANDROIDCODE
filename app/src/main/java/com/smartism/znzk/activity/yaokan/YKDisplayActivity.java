package com.smartism.znzk.activity.yaokan;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.smartism.znzk.R;
import com.smartism.znzk.activity.ActivityParentActivity;
import com.smartism.znzk.domain.yankan.Brand;
import com.smartism.znzk.domain.yankan.BrandResult;
import com.smartism.znzk.domain.yankan.MatchRemoteControlResult;
import com.smartism.znzk.util.Actions;
import com.smartism.znzk.util.JavaThreadPool;
import com.smartism.znzk.util.WeakRefHandler;
import com.smartism.znzk.util.indexlistsort.CharacterParser;
import com.smartism.znzk.util.indexlistsort.PinyinComparator;
import com.smartism.znzk.util.indexlistsort.SideBar;
import com.smartism.znzk.util.indexlistsort.SortAdapter;
import com.smartism.znzk.util.indexlistsort.SortModel;
import com.smartism.znzk.util.yaokan.YkanIRInterface;
import com.smartism.znzk.util.yaokan.YkanIRInterfaceImpl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
 * 品牌列表--(空调品牌)
 */
public class YKDisplayActivity extends ActivityParentActivity {

    private List<Brand> mList = new ArrayList<Brand>();
    private List<Brand> mListLow = new ArrayList<Brand>();
    private List<Brand> mListMain = new ArrayList<Brand>();//经常用的
    private ListView lv;
    private YkanIRInterface ykanInterface;
    private int typeId;
    private BrandResult brandResult;
    private String tname;//类型
    private String bname;//类型的品牌
    private Long did;
    //网络延迟时候 点击一个条目网络延迟没反应前 点击了另外一个条目 出现多次进入遥控器型号列表界面
    private Boolean b = true;


    private ListView sortListView;// 数据显示的listview
    private SideBar sideBar;// 字母滑动的bar
    private TextView dialog;// 显示当前选中的字母
    private SortAdapter adapter;// listviewadapter
    private CharacterParser characterParser;//汉字转换成拼音的类
    private PinyinComparator pinyinComparator;//根据拼音来排列ListView里面的数据类
    private List<SortModel> sourceDateList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_yankan_device_type);
        Log.d("taskId:", "devicedisplay" + getTaskId() + "");
        initData();
        initView();
        initEvent();
    }

    private void initData() {
        ykanInterface = new YkanIRInterfaceImpl();
        typeId = getIntent().getIntExtra("typeId", -1);
        tname = getIntent().getStringExtra("tname");
        did = getIntent().getLongExtra("did", 0);
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

    private void initView() {
        sortListView = (ListView) findViewById(R.id.lv1);
        // 实例化汉字转拼音类
        characterParser = CharacterParser.getInstance();
        pinyinComparator = new PinyinComparator();
        sideBar = (SideBar) findViewById(R.id.sidrbar);
        dialog = (TextView) findViewById(R.id.dialog);
        sideBar.setTextView(dialog);
        // 设置右侧触摸监听
        sideBar.setOnTouchingLetterChangedListener(new SideBar.OnTouchingLetterChangedListener() {
            @Override
            public void onTouchingLetterChanged(String s) {
                // 该字母首次出现的位置
                int position = adapter.getPositionForSection(s.charAt(0));
                if (position != -1) {
                    sortListView.setSelection(position);
                }
            }
        });

        JavaThreadPool.getInstance().excute(new Runnable() {
            public void run() {

                runOnUiThread(new Runnable() {
                    public void run() {
                        showInProgress(getString(R.string.ongoing), false, true);
                    }
                });

                ykanInterface.getBrandsByType(typeId, mHandler);
            }
        });
    }

    public void initEvent() {
        sortListView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {

                if (b) {
                    b = false;
                    showInProgress(getString(R.string.ongoing), false, true);

                    final int bid = sourceDateList.get(position).getBrandName();
                    bname = sourceDateList.get(position).getName();
                    new Thread(new Runnable() {
                        public void run() {
                            ykanInterface.getRemoteMatched(typeId, bid, mHandler);
                        }
                    }).start();
                } else {
                    Log.e("aaa", "net is slow@####");
                    return;
                }
            }
        });
        findViewById(R.id.back).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    /**
     * 为ListView填充数据
     *
     * @param data
     * @return
     */
    private List<SortModel> filledData(List<Brand> data) {
        List<SortModel> mSortList = new ArrayList<SortModel>();
        for (int i = 0; i < data.size(); i++) {
            if (data.get(i).getBid() < 0) {
                continue;
            }
            SortModel sortModel = new SortModel();
            sortModel.setName(data.get(i).getName());
            sortModel.setBrandName(data.get(i).getBid());

            // 汉字转换成拼音
            String pinyin = characterParser.getSelling(data.get(i).getName());
            String sortString = pinyin.substring(0, 1).toUpperCase();
            if (data.get(i).getCommon() == 1) {
//                mListMain.add(data.get(i));
                sortModel.setSortLetters("#");
            } else if (mList.get(i).getCommon() == 0) {
//                mListLow.add(data.get(i));
                if (sortString.matches("[A-Z]")) {
                    sortModel.setSortLetters(sortString.toUpperCase());
                } else {
                    sortModel.setSortLetters("#");
                }
            }
            mSortList.add(sortModel);
        }
        return mSortList;
    }


    private Handler.Callback mCallback = new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {

                case YkanIRInterfaceImpl.NET_SUCCEES_GETBRANDSBYTYPE:

                    cancelInProgress();
                    brandResult = (BrandResult) msg.obj;
                    if (brandResult != null) {
                        mList = brandResult.getRs();

                        sourceDateList = filledData(mList);
                        // 根据a-z进行排序源数据
                        Collections.sort(sourceDateList, pinyinComparator);
                        adapter = new SortAdapter(YKDisplayActivity.this, sourceDateList);
                        sortListView.setAdapter(adapter);

                    } else {
                        Toast.makeText(getApplicationContext(), R.string.net_error_ioerror, Toast.LENGTH_SHORT).show();
                    }

                    break;
                case YkanIRInterfaceImpl.NET_SUCCEES_GETREMOTEMATCHED:
                    cancelInProgress();
                    b = true;
                    // 遥控器型号
                    MatchRemoteControlResult matchInfo = (MatchRemoteControlResult) msg.obj;
//                    if (matchInfo != null) {
                    Intent intent = new Intent();
                    intent.putExtra("matchInfo", matchInfo);
                    intent.putExtra("bname", bname);
                    intent.putExtra("tname", tname);
                    intent.putExtra("did", did);
                    intent.setClass(getApplicationContext(), YKRemoteListActivity.class);
                    startActivity(intent);
//                    } else {
//                        Toast.makeText(getApplicationContext(), R.string.net_error_ioerror, Toast.LENGTH_SHORT).show();
//                    }
                    break;
                default:
                    break;
            }
            return false;
        }
    };
    private Handler mHandler = new WeakRefHandler(mCallback);
}
