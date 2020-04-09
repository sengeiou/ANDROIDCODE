package com.smartism.znzk.fragment;


import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;

import com.smartism.znzk.R;
import com.smartism.znzk.util.Util;

public class SettingListFragment extends DialogFragment implements AdapterView.OnItemClickListener {

    public interface OnItemLickListener{
        void onItemClick(String data,int position);
    }

    private static final String DISPLAY_DATAID = "display_data_id";

    private String[]  mDisplayData ;
    private ListView mListView ;
    private OnItemLickListener mListener ;


    public SettingListFragment() {
        // Required empty public constructor
    }

    public static SettingListFragment newInstance(String[] data) {
        SettingListFragment fragment = new SettingListFragment();
        Bundle args = new Bundle();
        args.putStringArray(DISPLAY_DATAID,data);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(!(getActivity() instanceof OnItemLickListener)){
            throw new IllegalArgumentException("activity must implement OnItemClickListener");
        }else{
            mListener = (OnItemLickListener) getActivity();
        }
        if (getArguments() != null) {
            mDisplayData = getArguments().getStringArray(DISPLAY_DATAID);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_setting_list_layout, container, false) ;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mListView = view.findViewById(R.id.dialog_list_view);
        mListView.setAdapter(new ArrayAdapter(getContext(),R.layout.list_item_with_right_arrow_layout,mDisplayData));
        mListView.setOnItemClickListener(this);

        //设置对话框
        setCancelable(true);
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);//去除标题
        getDialog().getWindow().setGravity(Gravity.BOTTOM);//弹出位置
        getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));//背景设置成透明

    }

    @Override
    public void onResume() {
        super.onResume();
        //设置Dialog宽高
        getDialog().getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT
                , (int) (getResources().getDisplayMetrics().heightPixels*2/3+0.5f));
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        mListener.onItemClick(mDisplayData[position],position);
    }
}
