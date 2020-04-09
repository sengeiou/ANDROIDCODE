package com.smartism.znzk.zhicheng.tasks;

import android.app.Activity;
import android.os.AsyncTask;
import android.text.TextUtils;
import com.google.gson.JsonArray;
import com.smartism.znzk.util.indexlistsort.CharacterParser;
import com.smartism.znzk.util.indexlistsort.PinyinComparator;
import com.smartism.znzk.util.indexlistsort.SortModel;
import com.smartism.znzk.zhicheng.iviews.ZCBrandDisplayInterface;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.builder.PostFormBuilder;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.*;

/*
* 获取牌子，不仅仅限于空调
* */
public class GetBrandAsyncTask extends AsyncTask<Map<String,String>,Void,List<SortModel>>{

    public final static  String TEST_MAC = "afc1d38229a1vd3c";//mac值，验证作用
    public final static int AIR_TYPE = 1 ; //空调类型
    public final static String GET_BRAND_URL = "http://www.huilink.com.cn/dk2018/getbrandlist.asp?";  //请求某一种类型的所有牌子
    WeakReference<ZCBrandDisplayInterface> mBrandView  ;
    String server  = GET_BRAND_URL ;
    //常见的空调品牌
    String[] airs = new String[]{
            "格力","美的","海尔","奥克斯","志高","TCL","海信","科龙","长虹"
            ,"格兰仕","春兰","松下","新科","LG","杨子","大金"
    };
    List<String> comman_air = new ArrayList<>();
    {
        for(int i=0;i<airs.length;i++){
            comman_air.add(airs[i]);
        }
    }
   public GetBrandAsyncTask(ZCBrandDisplayInterface brandView,String url){
       mBrandView = new WeakReference<>(brandView);
       server = url ;
   }

    private  boolean isActive(){
       Activity activity = (Activity) mBrandView.get();
       if(mBrandView.get()==null&&activity.isFinishing()){
            return false;
       }

       return true ;
   }
    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        if(isActive()){
            mBrandView.get().showProgress();//显示进度条
        }
    }

    @Override
    protected void onPostExecute(List<SortModel> s) {
       if(!isActive()){
           return ;
       }
        mBrandView.get().hideProgress(); //隐藏进度条
        if(s==null||s.size()==0){
            //没有获取到数据
            mBrandView.get().errorMsg();
        }else{
            mBrandView.get().showBrands(s);
            mBrandView.get().successMsg();
        }
    }

    @Override
    protected List<SortModel> doInBackground(Map<String,String>... jsonObjects) {
       if(!isActive()){
           return null ;
       }
        List<SortModel> sortModels =null ;
        try {
            String result = null;
            List<String> keys = new ArrayList<>(jsonObjects[0].keySet());
            OkHttpClient httpClient = new OkHttpClient();
            FormBody.Builder formBody = new FormBody.Builder();
            for(int i=0;i<keys.size();i++){
                formBody.add(keys.get(i),jsonObjects[0].get(keys.get(i)));
            }
            Request request = new Request.Builder()
                    .url(server)
                    .post(formBody.build())
                    .build();
            Response response = httpClient.newCall(request).execute();
            if (response != null && response.isSuccessful()) {
                result = response.body().string();//网络请求的一部分，获取服务器的数据
                if(!TextUtils.isEmpty(result)){
                   sortModels =  parseJsonGetBrand(result);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return sortModels;
    }

    private  List<SortModel> parseJsonGetBrand(String jsonResult){
       List<SortModel> resultMode =  new ArrayList<>();
        try {
            JSONArray jsonArray = new JSONArray(jsonResult);
            if(jsonArray.length()>0){
                List<SortModel> tempModels = new ArrayList<>();
                List<SortModel> commandModels = new ArrayList<>();
                for(int i=0;i<jsonArray.length();i++){
                    SortModel temp = new SortModel();
                    JSONObject object = jsonArray.getJSONObject(i);
                    temp.setName(object.getString("bn"));
                    temp.setId(object.getInt("id"));
                    String pinyin = CharacterParser.getInstance().getSelling(temp.getName());
                    String sortString = pinyin.substring(0, 1).toUpperCase();
                    temp.setPinyin(pinyin);


                    if (sortString.matches("[A-Z]")) {
                           temp.setSortLetters(sortString.toUpperCase());
                     } else {
                           temp.setSortLetters("#");
                     }

                    //取出每一个汉字的首字母
                    if(!temp.getPinyin().equalsIgnoreCase(temp.getName())){
                        char[] words = temp.getName().toCharArray();
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

                    if(comman_air.contains(temp.getName())){

                        SortModel newMode = new SortModel();
                        newMode.setName(temp.getName());
                        newMode.setPinyin(CharacterParser.getInstance().getSelling(temp.getName()));
                        newMode.setId(temp.getId());
                        newMode.setSortLetters("#");
                        newMode.setFirstTwoLetters("");
                        commandModels.add(newMode);
                    }

                    tempModels.add(temp);
                }
                //进行排序
                Collections.sort(tempModels,new PinyinComparator());
                if(commandModels.size()>0){
                    resultMode.addAll(commandModels);
                }
                resultMode.addAll(tempModels);
            }

        } catch (JSONException e) {
            resultMode = null ;
        }


        return resultMode;
    }
}
