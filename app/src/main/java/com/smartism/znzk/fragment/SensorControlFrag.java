package com.smartism.znzk.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.p2p.core.P2PHandler;
import com.smartism.znzk.R;
import com.smartism.znzk.activity.camera.MainControlActivity;
import com.smartism.znzk.application.MainApplication;
import com.smartism.znzk.db.camera.Contact;
import com.smartism.znzk.global.Constants;

import java.util.ArrayList;

/**
 * 传感器控制页面，暂未启用
 *
 * @author 王建
 */
public class SensorControlFrag extends BaseFragment implements View.OnClickListener {
    private Context mContext;
    private Contact contact;
    private boolean isRegFilter = false;
    boolean isOpenReverse = true;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mContext = getActivity();
        contact = (Contact) getArguments().getSerializable("contact");
        View view = inflater.inflate(R.layout.fragment_sensor_control, container, false);
        regFilter();
        P2PHandler.getInstance().getDefenceArea(contact.contactId, contact.getContactPassword(), MainApplication.GWELL_LOCALAREAIP);
        return view;
    }

    public void regFilter() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(Constants.P2P.ACK_RET_SET_DEFENCE_AREA);
        filter.addAction(Constants.P2P.ACK_RET_GET_DEFENCE_AREA);
        filter.addAction(Constants.P2P.ACK_RET_CLEAR_DEFENCE_AREA);
        filter.addAction(Constants.P2P.RET_SET_DEFENCE_AREA);//学习探头返回
        filter.addAction(Constants.P2P.RET_GET_DEFENCE_AREA);//获取探头列表返回
        mContext.registerReceiver(mReceiver, filter);
        isRegFilter = true;
    }

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context arg0, Intent intent) {
            if (intent.getAction().equals(Constants.P2P.RET_SET_DEFENCE_AREA)) {//学习探头返回
//                if (result == ACK_PWD_ERROR) {
//                    Toast.makeText(SensorActivity.this, R.string.password_wrong, Toast.LENGTH_SHORT).show();
//                } else if (result == ACK_NET_ERROR) {
//                    Toast.makeText(SensorActivity.this, R.string.net_error, Toast.LENGTH_SHORT).show();
//                } else if (result == ACK_INSUFFICIENT_PERMISSIONS) {
//                    Toast.makeText(SensorActivity.this, R.string.permission_denied, Toast.LENGTH_SHORT).show();
//                } else if (result == ACK_SUCCESS) {
//                    Log.d(TAG, "SENSOR_ACK_SUCCESS");
//                }
            } else if (intent.getAction().equals(Constants.P2P.RET_GET_DEFENCE_AREA)) { //获取探头列表返回
                ArrayList<int[]> data = (ArrayList<int[]>) intent.getSerializableExtra("data");
                if (data != null) {
                    for (int i = 0; i < data.size(); i++) {
                        int[] group = data.get(i);
                        for (int j : group) {
//                            items.add(j);
                        }
                    }
                }
//                adapter.notifyDataSetChanged();
            }
        }
    };


    public void addSensor(View view) {
//        P2PHandler.getInstance().setDefenceAreaState(contact.contactId, contact.getContactPassword(), position / 8, position % 8, 0);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (isRegFilter) {
            mContext.unregisterReceiver(mReceiver);
            isRegFilter = false;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Intent it = new Intent();
        it.setAction(Constants.Action.CONTROL_BACK);
        mContext.sendBroadcast(it);
    }

    @Override
    public void onClick(View v) {

    }
}
