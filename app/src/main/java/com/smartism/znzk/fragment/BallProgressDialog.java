package com.smartism.znzk.fragment;


import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.AppCompatImageView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.smartism.znzk.R;

/*
* 以前的Diaog在Android4.x的版本显示效果不好，打算替换一下
*
* author:mz
* */
public class BallProgressDialog extends DialogFragment {

    private static final String CONTENT_FLAG = "content_flag";

    private TextView mContentTv ;
    private AppCompatImageView mBallIv;
    private Animatable mCompat ;
    private String mContent ;
    private String mOutSideContent ;

    public BallProgressDialog() {
    }

    public static BallProgressDialog getDialogInstance(String content){
        Bundle bundle = new Bundle();
        bundle.putString(CONTENT_FLAG,content);
        BallProgressDialog dialog = new BallProgressDialog();
        dialog.setArguments(bundle);
        return dialog ;
    }

    private void initView(View parent){
        mContentTv = parent.findViewById(R.id.content_tv);
        mBallIv  = parent.findViewById(R.id.ball_iv);

        //设置显示文字
        if(!TextUtils.isEmpty(mOutSideContent)){
            mContentTv.setText(mOutSideContent);
        }else{
            mContentTv.setText(mContent);
        }
        mCompat = (Animatable) mBallIv.getDrawable();
    }

    public void setContent(String content){
        mOutSideContent = content ;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments() ;
        if(bundle!=null){
            mContent = bundle.getString(CONTENT_FLAG);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_ball_progress_dialog, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView(view);
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        getDialog().setCancelable(true);
        getDialog().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
    }

    @Override
    public void onResume() {
        super.onResume();
        getDialog().getWindow().setLayout((int) (getResources().getDisplayMetrics().widthPixels*0.28f+0.5f), ViewGroup.LayoutParams.WRAP_CONTENT);
        if(mCompat!=null){
            mCompat.start();
        }
    }



    //对话框关闭时
    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        if(mCompat!=null){
            mCompat.stop();
        }
    }
}
