package com.smartism.znzk.zhicheng.activities;


import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.smartism.znzk.R;
import com.smartism.znzk.util.indexlistsort.CharacterParser;
import com.smartism.znzk.zhicheng.models.ARCModel;
import com.smartism.znzk.zhicheng.tasks.GeneralHttpTask;
import com.smartism.znzk.zhicheng.tasks.GetBrandAsyncTask;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.smartism.znzk.zhicheng.activities.ZCIRRemoteList.CURRENT_IR_MAC_VALUE;
import static com.smartism.znzk.zhicheng.tasks.GeneralHttpTask.GET_BRAND_REMOTE_CONTROL;
/*
* author mz
* */
public class ZCModeListActivity extends MZBaseActivity implements GeneralHttpTask.ILoadARKeysImpl {

    private final  static String DEBUG_LOG = "ZCModeListActivity";
    String brandName ="";
    long deviceId ;
    int brandId ;
    ListView mListView ;
    BaseAdapter mAdapter;
    List<ARCModel> mModels = new ArrayList<>();
    List<ARCModel> displaynames = new ArrayList<>();
    private TextView mNoTypeTipText;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(savedInstanceState==null){
            brandName = getIntent().getStringExtra("brand_name");
            deviceId = getIntent().getLongExtra("did",0);
            brandId = getIntent().getIntExtra("brand_id",0);//brand_id
        }else{
            brandName =savedInstanceState.getString("brand_name");//brand_name
            deviceId = savedInstanceState.getLong("did");
            brandId = savedInstanceState.getInt("brand_id");
        }
        initChild();
        requestData();

    }

    void initChild(){
        mListView = findViewById(R.id.content_list);
        //用这个Adapter对象不太好
        mAdapter =new MyAdapter() ;
        setTitle(brandName);
        mListView.setAdapter(mAdapter);
        mDialog.getDialog().setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                if(mTask!=null){
                    //取消任务,采用的是线程池，可以取消，取消后不调用onPostExecute方法，源码上写的
                    mTask.cancel(true);
                }
            }
        });

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent();
                intent.putExtra("is_modelist",true);
                intent.setClass(ZCModeListActivity.this,AirConditioningActivity.class);
                intent.putExtra("did",deviceId);
                intent.putExtra("content_info",displaynames.get(position));
           //     intent.putStringArrayListExtra("kfids", (ArrayList<String>) kfids);
                intent.putParcelableArrayListExtra("kfids", (ArrayList<? extends Parcelable>) mModels);
                startActivity(intent);

            }
        });
        mNoTypeTipText = findViewById(R.id.no_type_tip_tv);
        mNoTypeTipText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mModels.size()==0){
                    //没有获取到遥控器型号
                    Log.d(DEBUG_LOG,"没有获取到遥控器型号");
                }else{
                    Intent intent = new Intent();
                    intent.putExtra("need_display_select",true);
                    intent.putExtra("is_modelist",true);
                    intent.setClass(ZCModeListActivity.this,AirConditioningActivity.class);
                    intent.putExtra("did",deviceId);
                    intent.putExtra("content_info",mModels.get(0));
                    intent.putParcelableArrayListExtra("kfids", (ArrayList<? extends Parcelable>) mModels);
                    startActivity(intent);
                }
            }
        });
    }

    GeneralHttpTask mTask ;
    void requestData(){
        mTask = new GeneralHttpTask(this,GET_BRAND_REMOTE_CONTROL);
        //请求数据
        Map<String,String> msp = new HashMap<>();
        msp.put("mac", CURRENT_IR_MAC_VALUE);
        msp.put("device_id","1");//请求空调的遥控器
        msp.put("brand_id",brandId+"");
        mTask.execute(msp);
    }


    SearchView search ;
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.search_menu,menu);
        search = (SearchView) menu.findItem(R.id.search_btn).getActionView();
        search.setQueryHint(getResources().getString(R.string.hwzf_please_input_remote_type));//hint text
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
                    ARCModel temp = displaynames.get(i);
                    if(!temp.getRcName().contains(newText)&&
                            !(temp.getFirstTwoLetters().contains(newText.toUpperCase())||temp.getFirstTwoLetters().contains(newText.toLowerCase()))){
                        displaynames.remove(i);
                        i--;
                    }
                }
                mAdapter.notifyDataSetChanged();
                return true;
            }
        });
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putString("brand_name",brandName);
        outState.putLong("did",deviceId);
        outState.putInt("brand_id",brandId);
        super.onSaveInstanceState(outState);
    }

    @Override
    public int setLayoutId() {
        return R.layout.activity_zcmode_list_layout;
    }

    @Override
    public void getRequestResult(String results) {
        if(results==null||results.equals("")){
            error(getResources().getString(R.string.hwzf_server_data_error));
        }else{
            List<ARCModel> temp  = parseJsonGetBrand(results);
            mModels.clear();
            for(int i=0;i<temp.size();i++){
                ARCModel arcModel = temp.get(i);
                arcModel.setParentId(brandId);
                arcModel.setParentName(brandName);
                mModels.add(arcModel);
                displaynames.add(arcModel);
          //      kfids.add(arcModel.getKfId());
            }
            mAdapter.notifyDataSetChanged();
        }
    }
    List<ARCModel> parseJsonGetBrand(String result){
        List<ARCModel> models =  new ArrayList<>();
        try {
            JSONArray jsonArray = new JSONArray(result);
            for(int i=0;i<jsonArray.length();i++){
                JSONObject object = jsonArray.getJSONObject(i);
                ARCModel temp = new ARCModel();
                temp.setRcName(object.getString("bn"));
                temp.setKfId(object.getString("id"));
                temp.setPinyin(CharacterParser.getInstance().getSelling(temp.getRcName()));
                //取出每一个汉字的首字母
                if(!temp.getPinyin().equalsIgnoreCase(temp.getRcName())){
                    char[] words = temp.getRcName().toCharArray();
                    if(words!=null){
                        StringBuilder sb   = new StringBuilder();
                        for(int j=0;j<words.length;j++){
                            sb.append(CharacterParser.getInstance().getSelling(String.valueOf(words[j])).substring(0,1));
                        }
                        temp.setFirstTwoLetters(sb.toString());
                    }

                }else{
                    temp.setFirstTwoLetters(temp.getPinyin());
                }
                models.add(temp);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return  models ;
    }

    class MyAdapter extends BaseAdapter{

        @Override
        public int getCount() {
            return displaynames.size();
        }

        @Override
        public Object getItem(int position) {
            return displaynames.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            TextView display_tv ;
            if(convertView==null){
                display_tv = (TextView) getLayoutInflater().inflate(android.R.layout.simple_list_item_1,parent,false);
            }else{
                display_tv = (TextView) convertView;
            }
            display_tv.setText(displaynames.get(position).getRcName());
            return display_tv;
        }
    }
}
