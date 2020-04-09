package com.smartism.znzk.zhicheng.activities;


import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.*;
import com.smartism.znzk.R;
import com.smartism.znzk.util.Util;
import com.smartism.znzk.util.indexlistsort.SideBar;
import com.smartism.znzk.util.indexlistsort.SortAdapter;
import com.smartism.znzk.util.indexlistsort.SortModel;
import com.smartism.znzk.widget.customview.CustomProgressDialog;
import com.smartism.znzk.zhicheng.iviews.ZCBrandDisplayInterface;
import com.smartism.znzk.zhicheng.tasks.GetBrandAsyncTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*
* 这个活动的主要目的就是展示支持的品牌列表，并跳转到具体品牌型号展示页面
*
* autho mz
* */
public class ZCBrandDisplayActivity extends AppCompatActivity implements ZCBrandDisplayInterface {
    CustomProgressDialog mDialog ; //进度条
    Toolbar mToolbar ;
    SideBar side_bar ;
    ListView brand_list ;
    TextView side_text_tv ;
    SortAdapter mSortAdapter;
    List<SortModel> mModels   = new ArrayList<>();
    List<SortModel> displaynames = new ArrayList<>();
    TextView diaplay_case_tv ;
    long deviceId;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_zcbrand_display_layout);
        Util.setStatusBarColor(this,getResources().getColor(R.color.device_main_bg));//设置状态栏颜色
        init();
        initData();
        if(savedInstanceState==null){
            deviceId = getIntent().getLongExtra("did",0);
        }else{
            deviceId = savedInstanceState.getLong("did");
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putLong("did",deviceId);
        super.onSaveInstanceState(outState);
    }

    AsyncTask mGetBrandTask ;
    void initData(){
        //请求数据
        Map<String,String> msp = new HashMap<>();
        msp.put("mac",ZCIRRemoteList.CURRENT_IR_MAC_VALUE);
        msp.put("device_id","1");//请求空调的所有品牌
        mGetBrandTask = new GetBrandAsyncTask(this,GetBrandAsyncTask.GET_BRAND_URL).execute(msp);
    }
    private  void init(){
        mDialog = new CustomProgressDialog(this);//初始化进度条
        mDialog.setCancelable(true);
        mDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                if(mGetBrandTask!=null){
                    //用户取消掉进度条后，不再将结果显示到界面,原理是不回调onPostExecute方法更新结果
                    mGetBrandTask.cancel(true);
                }
            }
        });
        mToolbar = findViewById(R.id.brand_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        side_bar = findViewById(R.id.side_bar);
        brand_list = findViewById(R.id.brand_list);
        side_text_tv = findViewById(R.id.side_text_tv);
        side_bar.setTextView(side_text_tv);
        diaplay_case_tv = findViewById(R.id.diaplay_case_tv);
        mSortAdapter = new SortAdapter(this,displaynames);
        brand_list.setAdapter(mSortAdapter);
        side_bar.setOnTouchingLetterChangedListener(new SideBar.OnTouchingLetterChangedListener() {
            @Override
            public void onTouchingLetterChanged(String s) {
                // 该字母首次出现的位置
                int position = mSortAdapter.getPositionForSection(s.charAt(0));
                if (position != -1) {
                    brand_list.setSelection(position);
                }
            }
        });
        brand_list.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                switch (scrollState){
                    case SCROLL_STATE_IDLE:
                        //滑动停止后隐藏，nice
                        diaplay_case_tv.setVisibility(View.GONE);
                        break ;
                    case SCROLL_STATE_TOUCH_SCROLL:
                        //由用户滑动就显示
                        diaplay_case_tv.setVisibility(View.VISIBLE);
                        break ;
                }
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if(displaynames.size()>0&&diaplay_case_tv.getVisibility()==View.VISIBLE){
                    diaplay_case_tv.setText(displaynames.get(firstVisibleItem).getSortLetters());
                }
            }
        });

        brand_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent();
                intent.putExtra("did",deviceId);
                intent.putExtra("brand_id",displaynames.get(position).getId());
                intent.putExtra("brand_name",displaynames.get(position).getName());
                intent.setClass(ZCBrandDisplayActivity.this,ZCModeListActivity.class);
                startActivity(intent);

            }
        });
    }

    SearchView search ;
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.search_menu,menu);
        search = (android.support.v7.widget.SearchView) menu.findItem(R.id.search_btn).getActionView();
        search.setQueryHint(getResources().getString(R.string.hwzf_please_input_air_type));//hint text
        search.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                displaynames.clear();
                displaynames.addAll(mModels);
                for(int i=0;i<displaynames.size();i++){
                    SortModel sortModel = displaynames.get(i);
                    if(!sortModel.getName().contains(newText)&&
                            !(sortModel.getFirstTwoLetters().contains(newText.toUpperCase())||sortModel.getFirstTwoLetters().contains(newText.toLowerCase()))){
                        displaynames.remove(i);
                        i--;
                    }
                }
                mSortAdapter.notifyDataSetChanged();
                return true;
            }
        });
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void showProgress() {
        if(!mDialog.isShowing()){
            mDialog.show();
        }
    }

    @Override
    public void hideProgress() {
        if(mDialog.isShowing()){
            mDialog.dismiss();
        }
    }

    @Override
    public void showBrands(List<SortModel> models) {
        mModels.clear();
        displaynames.clear();
        for(int i=0;i<models.size();i++){
            mModels.add(models.get(i));
        }
        displaynames.addAll(mModels);
        mSortAdapter.notifyDataSetChanged();
    }

    @Override
    public void errorMsg() {
        Toast toast = Toast.makeText(this,"",Toast.LENGTH_LONG);
        toast.setText(getResources().getString(R.string.net_error_requestfailed));
        toast.show();
    }

    @Override
    public void successMsg() {
        Toast toast = Toast.makeText(this,"",Toast.LENGTH_LONG);
        toast.setText("success");
        Log.d("ZCBrandDisplayActivity","成功");
        //toast.show();
    }
}
