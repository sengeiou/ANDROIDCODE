package com.smartism.znzk.activity.weather;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageLoadingListener;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.smartism.znzk.R;
import com.smartism.znzk.activity.ActivityParentActivity;
import com.smartism.znzk.activity.device.HeaterSignalDiagnosisActivity;
import com.smartism.znzk.activity.device.ZhujiListFragment;
import com.smartism.znzk.util.Actions;
import com.smartism.znzk.util.DataCenterSharedPreferences;
import com.smartism.znzk.util.TemperatureUtil;
import com.smartism.znzk.util.ToastUtil;
import com.smartism.znzk.util.Util;

import java.util.Locale;

public class WeatherInfoActivity extends ActivityParentActivity {
    private TextView textWeatherMain,textWeatherTemp,textWeatherPm25,textWeatherAirQuality,textWeatherHumidity,textWeatherUVIndex,textWeatherWind;
    private ImageView iconWeather,refresh;

    // 显示图片的配置
    DisplayImageOptions options = new DisplayImageOptions.Builder().showImageOnLoading(R.drawable.loading)
            .showImageOnFail(R.drawable.sorrow).cacheInMemory(true).cacheOnDisc(true)
            .bitmapConfig(Bitmap.Config.RGB_565).imageScaleType(ImageScaleType.EXACTLY_STRETCHED)// 设置图片以如何的编码方式显示
            .resetViewBeforeLoading(true)// 设置图片在下载前是否重置，复位
            // .displayer(new RoundedBitmapDisplayer(20))//是否设置为圆角，弧度为多少
            .displayer(new FadeInBitmapDisplayer(100))// 是否图片加载好后渐入的动画时间
            .build();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather_info);
        initView();
        initData();
        initRegisterReceiver();
    }

    public void initView(){
        refresh = (ImageView) findViewById(R.id.refresh);
        iconWeather = (ImageView) findViewById(R.id.icon_weather);
        textWeatherMain = (TextView) findViewById(R.id.text_weather);
        textWeatherTemp = (TextView) findViewById(R.id.outdoor_temp);
        textWeatherPm25 = (TextView) findViewById(R.id.outdoor_pm25);
        textWeatherAirQuality = (TextView) findViewById(R.id.outdoor_quality);
        textWeatherHumidity = (TextView) findViewById(R.id.outdoor_humidity);
        textWeatherUVIndex = (TextView) findViewById(R.id.outdoor_uvindex);
        textWeatherWind = (TextView) findViewById(R.id.outdoor_wind);
    }

    private void initData(){
        try {
            JSONObject weatherInfo = JSON.parseObject(mContext.getDcsp().getString(DataCenterSharedPreferences.Constant.WEATHER_INFO,"{}"));
            JSONObject weather = weatherInfo.getJSONArray("weather").getJSONObject(0);
            textWeatherMain.setText(weather.getString("main"));
            ImageLoader.getInstance().displayImage(String.format("https://openweathermap.org/img/wn/%s@2x.png", weather.getString("icon")), iconWeather, options, new MImageLoadingBar());
            JSONObject main = weatherInfo.getJSONObject("main");
            String tempUnit = mContext.getDcsp().getString(DataCenterSharedPreferences.Constant.SHOW_TEMPERATURE_UNIT, "ssd");
            if (tempUnit.equals("ssd")) {
                textWeatherTemp.setText(String.format(Locale.ENGLISH, "%.0f℃", main.getFloatValue("temp")));
            } else {
                textWeatherTemp.setText(String.format(Locale.ENGLISH, "%.0f℉", TemperatureUtil.ssdToFsd(main.getFloatValue("temp"))));
            }
            if (weatherInfo.containsKey("uvindex")){
                textWeatherUVIndex.setText(String.format("%.0f %s",weatherInfo.getFloatValue("uvindex"),uvIndexToName(weatherInfo.getFloatValue("uvindex"))));
            }
            if (weatherInfo.containsKey("pm25")){
                textWeatherPm25.setText(String.valueOf(weatherInfo.getFloatValue("pm25")));
            }
            if (weatherInfo.containsKey("pm10")){
                textWeatherAirQuality.setText(Util.pm10ToName(weatherInfo.getFloatValue("pm10")));
            }
            textWeatherHumidity.setText(main.getIntValue("humidity")+"%");
            JSONObject wind = weatherInfo.getJSONObject("wind");
            textWeatherWind.setText(degreesToName(wind.getFloatValue("deg")) + " " + String.format(Locale.ENGLISH,"%.0f",wind.getFloatValue("speed")*3.6)+"km/h");//1m/s = 3.6KM/H
        }catch (Exception ex){

        }
    }

    public void back(View v){
        finish();
    }

    public void refresh(View v){
        mContext.sendBroadcast(new Intent(Actions.WEATHER_GET));
        startShowLoading(refresh);
    }

    private String degreesToName(float degrees){
        //参考：http://www.mcu3.com/page95?article_id=294
        if (358.76 <= degrees || degrees <= 11.25){
            return "N";
        }else if(11.26<=degrees && degrees <= 33.75){
            return "NNE";
        }else if(33.76<=degrees && degrees <= 56.25){
            return "NE";
        }else if(56.26<=degrees && degrees <= 78.75){
            return "ENE";
        }else if(78.76<=degrees && degrees <= 101.25){
            return "E";
        }else if(101.26<=degrees && degrees <= 123.75){
            return "ESE";
        }else if(123.76<=degrees && degrees <= 145.25){
            return "SE";
        }else if(145.26<=degrees && degrees <= 168.75){
            return "SSE";
        }else if(168.76<=degrees && degrees <= 191.25){
            return "S";
        }else if(191.26<=degrees && degrees <= 213.75){
            return "SSW";
        }else if(213.76<=degrees && degrees <= 236.25){
            return "SW";
        }else if(236.26<=degrees && degrees <= 258.75){
            return "WSW";
        }else if(258.76<=degrees && degrees <= 281.25){
            return "W";
        }else if(281.26<=degrees && degrees <= 303.75){
            return "WNW";
        }else if(303.76<=degrees && degrees <= 306.25){
            return "NW";
        }else if(306.26<=degrees && degrees <= 348.75){
            return "NNW";
        }
        return "";
    }

    private String uvIndexToName(float uvIndex){
        if (0 <= uvIndex && uvIndex < 3){
            return "Low";
        }else if(3 <= uvIndex && uvIndex < 6){
            return "Moderate";
        }else if(6 <= uvIndex && uvIndex < 8){
            return "High";
        }else if(8 <= uvIndex && uvIndex < 11){
            return "Very high";
        }else{
            return "Extreme";
        }
    }

    private android.content.BroadcastReceiver receiver = new android.content.BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (Actions.WEATHER_GET_RESULT.equals(intent.getAction())) {
                stopShowLoading(refresh);
                initData();
            }
        }
    };

    /**
     * 注册广播
     */
    private void initRegisterReceiver() {
        IntentFilter receiverFilter = new IntentFilter();
        receiverFilter.addAction(Actions.WEATHER_GET_RESULT);
        mContext.registerReceiver(receiver, receiverFilter);
    }

    public class MImageLoadingBar implements ImageLoadingListener {

        @Override
        public void onLoadingCancelled(String arg0, View arg1) {
            if (arg1 != null)
                arg1.clearAnimation();
        }

        @Override
        public void onLoadingComplete(String arg0, View arg1, Bitmap arg2) {
            arg1.clearAnimation();
        }

        @Override
        public void onLoadingFailed(String arg0, View arg1, FailReason arg2) {
            arg1.clearAnimation();
        }

        @Override
        public void onLoadingStarted(String arg0, View arg1) {
            arg1.startAnimation(mContext.imgloading_animation);
        }
    }

    public void stopShowLoading(ImageView imageView) {
        imageView.clearAnimation();
    }

    public void startShowLoading(ImageView imageView) {
        Animation imgloading_animation = AnimationUtils.loadAnimation(WeatherInfoActivity.this,
                R.anim.loading_revolve);
        imgloading_animation.setInterpolator(new LinearInterpolator());
        imageView.startAnimation(imgloading_animation);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (receiver != null) {
            unregisterReceiver(receiver);
            receiver = null;
        }
    }
}
