package com.smartism.znzk.fragment;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.Group;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import com.smartism.znzk.R;
import com.smartism.znzk.activity.device.add.AddZhujiByApFailureActivity;
import com.smartism.znzk.view.alertview.AlertView;
import com.smartism.znzk.view.alertview.OnItemClickListener;

import java.net.InetAddress;


public class APFragmentSecondTip extends Fragment implements View.OnClickListener{


    public static final  String TAG = "APFragmentSecondTip";

    public interface OnAPFragmentTwoTipListener{
        void apSecondFragmentNext(String ssid,int netId);
    }

    public interface NewOnApFragmentTwoTipListener{
        void newApSecondFragmentNext();
    }

    private static final String ARG_TWO_TIP = "ARG_TWO_TIP";

    public APFragmentSecondTip() {
    }

    public static APFragmentSecondTip getInstance(String data){
       Bundle bundle = new Bundle();
       bundle.putString(ARG_TWO_TIP,data);
        APFragmentSecondTip apFragmentTwoTip = new APFragmentSecondTip();
       apFragmentTwoTip.setArguments(bundle);
       return apFragmentTwoTip  ;
    }

    private String displayData ;
    private OnAPFragmentTwoTipListener mListener ;
    private NewOnApFragmentTwoTipListener mNewListener ;
    private Context mContext ;
    private String mSSId  ;
    private int mNetId ;

    BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction().equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)){
                NetworkInfo networkInfo = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
                boolean isConnect = networkInfo.isConnected();
                mGroupSetting.setVisibility(View.VISIBLE);
                first_next_btn.setVisibility(View.GONE);
                if(isConnect){
                    WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
                    String ssid = wifiManager.getConnectionInfo().getSSID() ;
                    if(ssid.contains("AP_CONNECT_")&&ssid.length()==17){
                        mGroupSetting.setVisibility(View.GONE);
                        first_next_btn.setVisibility(View.VISIBLE);
                    }else{
                        mSSId = ssid ;
                        mNetId = wifiManager.getConnectionInfo().getNetworkId() ;
                    }
                    current_wifi_tv.setText(getString(R.string.add_zhuji_by_ap_current_wifi_enviroment,ssid));
                }else{
                    current_wifi_tv.setText(getString(R.string.add_zhuji_by_ap_current_notwifi_enviroment));
                }
            }
        }
    };
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(getArguments()!=null){
            displayData = getArguments().getString(ARG_TWO_TIP);
        }

        if(getActivity() instanceof OnAPFragmentTwoTipListener){
            mContext  = getActivity().getApplicationContext() ;
            mListener = (OnAPFragmentTwoTipListener) getActivity();
        }else{
            throw new IllegalArgumentException("宿主Activity必须实现OnAPFragmentTwoTipListener");
        }

        if(getActivity() instanceof NewOnApFragmentTwoTipListener){
            mContext  = getActivity().getApplicationContext() ;
            mNewListener = (NewOnApFragmentTwoTipListener) getActivity();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_apfragment_two_tip, container, false);
    }

    TextView current_wifi_tv ;
    Button setting_wifi,first_next_btn;
    private TextView mFirstTipTextView,mSecondTipTextView,mNotFindWifiText;
    private Group mGroupSetting;
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        current_wifi_tv = view.findViewById(R.id.current_wifi_tv);
        setting_wifi = view.findViewById(R.id.setting_wifi);
        first_next_btn = view.findViewById(R.id.first_next_btn);
        mFirstTipTextView  = view.findViewById(R.id.content_tv);
        mSecondTipTextView = view.findViewById(R.id.second_content_tv);
        mNotFindWifiText  = view.findViewById(R.id.not_find_wifi_tv);
        mGroupSetting = view.findViewById(R.id.group_setting);
        setting_wifi.setOnClickListener(this);
        first_next_btn.setOnClickListener(this);
        mNotFindWifiText.setOnClickListener(this);

        mFirstTipTextView.setText(Html.fromHtml(getResources().getString(R.string.add_zhuji_by_ap_connectwifizhuji_firsttip)));
        mSecondTipTextView.setText(Html.fromHtml(getResources().getString(R.string.add_zhuji_by_ap_connectwifizhuji_secondtip)));
        mNotFindWifiText.setText(Html.fromHtml(getResources().getString(R.string.add_zhuji_bt_ap_nofind_wifitip)));

    }

    @Override
    public void onResume() {
        super.onResume();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        mContext.registerReceiver(mReceiver,intentFilter);
    }

    @Override
    public void onPause() {
        super.onPause();
        if(mReceiver!=null){
            mContext.unregisterReceiver(mReceiver);
        }
    }

    @Override
    public void onClick(View v) {
        Intent intent = new Intent();
        switch (v.getId()){
            case R.id.setting_wifi:

                if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.HONEYCOMB){
                    intent.setClassName("com.android.settings","com.android.settings.Settings$WifiSettingsActivity");
                }else{
                    intent.setClassName("com.android.settings","com.android.settings.wifi.WifiSettings");
                }
                startActivity(intent);
                break ;
            case R.id.first_next_btn:
           //     mListener.apSecondFragmentNext(mSSId,mNetId);
                mNewListener.newApSecondFragmentNext();
                break ;
            case R.id.not_find_wifi_tv:
                intent.setClass(getContext(), AddZhujiByApFailureActivity.class);
                startActivity(intent);
                break ;
        }
    }

    //将int转化成字符串表示的IP地址
   public  String intIP2StringIP(int ip) {
        return (ip & 0xFF) + "."
           + ((ip >> 8) & 0xFF) + "."
           + ((ip >> 16) & 0xFF) + "."
           + (ip >> 24 & 0xFF);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null ;
    }
}
