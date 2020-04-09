package com.smartism.znzk.activity.scene;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.smartism.znzk.R;
import com.smartism.znzk.adapter.recycleradapter.BaseRecyslerAdapter;
import com.smartism.znzk.adapter.recycleradapter.RecyclerItemBean;
import com.smartism.znzk.adapter.scene.ChooseTimesAdapter;
import com.smartism.znzk.domain.scene.TimesInfo;
import com.smartism.znzk.view.DividerItemDecoration;

import java.util.ArrayList;
import java.util.List;

public class ChooseTimesActivity extends AppCompatActivity implements View.OnClickListener, BaseRecyslerAdapter.RecyclerItemClickListener {
    private RecyclerView recycler;
    private ChooseTimesAdapter mAdapter;
    private List<RecyclerItemBean> list;
    private String[] times;
    private String[] values;
    private String timesCode;

    private Button sure;
    private ImageView back_btn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_times);
        timesCode = getIntent().getStringExtra("times");
        initList();
        initTimes();
        initView();
        initDate();
        initEvent();
    }

    private void initDate() {
        mAdapter = new ChooseTimesAdapter(list);
        mAdapter.setRecyclerItemClickListener(this);
        //创建默认线性LinearLayoutManager
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recycler.setLayoutManager(layoutManager);  //设置布局管理器
        recycler.setItemAnimator(new DefaultItemAnimator()); //设置Item增加、移除动画
        recycler.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL_LIST));
        recycler.setAdapter(mAdapter);
    }

    private void initEvent() {
        sure.setOnClickListener(this);
        back_btn.setOnClickListener(this);
    }

    private void initView() {
        recycler = (RecyclerView) findViewById(R.id.recycle_times);
        sure = (Button) findViewById(R.id.sure);
        back_btn = (ImageView) findViewById(R.id.back_btn);
    }

    //初始化时间列表
    public void initTimes() {
        if (list == null) {
            list = new ArrayList<>();
        }
        list.clear();
        //匹配心已经选择过的
        char[] codes = timesCode.toCharArray();
        StringBuffer sb = new StringBuffer();
        if (codes != null && codes.length > 1) {
            for (int i = 0; i < codes.length; i++) {
                if (String.valueOf(codes[i]).equals("1")) {
                    list.add(new RecyclerItemBean(new TimesInfo(times[i], values[i], true), 0));
                } else {
                    list.add(new RecyclerItemBean(new TimesInfo(times[i], values[i], false), 0));
                }
            }
            if ("1111111".equalsIgnoreCase(timesCode)){
                list.add(new RecyclerItemBean(new TimesInfo(times[codes.length], values[codes.length], true), 0));
            }
        } else {
            for (int i = 0; i < times.length; i++) {
                list.add(new RecyclerItemBean(new TimesInfo(times[i], values[i]), 0));
            }
        }
    }

    @Override
    public void onRecycleItemClick(View view, int position) {
        TimesInfo info = (TimesInfo) list.get(position).getT();
        if ("1111111".equals(info.getValue())) {
            //选中每天的时候，其余的全部是未选中状态
            ((TimesInfo) list.get(list.size() - 1).getT()).setFlag(!info.isFlag());
            for (int i = 0; i < list.size() - 1; i++) {
                ((TimesInfo) list.get(i).getT()).setFlag(info.isFlag());
            }
            mAdapter.notifyDataSetChanged();
        } else {
            //选中别的就得取消每天
            ((TimesInfo) list.get(position).getT()).setFlag(!info.isFlag());
            mAdapter.notifyItemChanged(position);
            boolean isNotChecked = false;
            for (int i = 0; i < list.size() - 1; i++) {
                if(!((TimesInfo) list.get(i).getT()).isFlag()){
                    isNotChecked = true;
                }
            }
            ((TimesInfo) list.get(list.size() - 1).getT()).setFlag(!isNotChecked);
            mAdapter.notifyItemChanged(list.size() - 1);
        }
    }

    public void initList() {
        times = new String[]{
                getString(R.string.monday),
                getString(R.string.tuesday),
                getString(R.string.wednesday),
                getString(R.string.thursday),
                getString(R.string.friday),
                getString(R.string.saturday),
                getString(R.string.sunday),
                getString(R.string.everyday)};
        values = new String[]{
                "1000000",
                "0100000",
                "0010000",
                "0001000",
                "0000100",
                "0000010",
                "0000001",
                "1111111"};
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.sure:
                String code = "";
                TimesInfo info = (TimesInfo) list.get(list.size() - 1).getT();
                if ("1111111".equals(info.getValue()) && info.isFlag()) {
                    code = "1111111";
                } else {
                    list.remove(list.size() - 1);
                    StringBuffer strbff = new StringBuffer();
                    for (RecyclerItemBean bean : list) {
                        TimesInfo timesInfo = (TimesInfo) bean.getT();
                        if (timesInfo.isFlag()) {
                            strbff.append("1");
                        } else {
                            strbff.append("0");
                        }
                    }
                    code = strbff.toString();
                }
                Intent intent = new Intent();
                intent.putExtra("times", code);
                setResult(20, intent);
                finish();
                break;
            case R.id.back_btn:
                finish();
                break;
        }
    }
}
