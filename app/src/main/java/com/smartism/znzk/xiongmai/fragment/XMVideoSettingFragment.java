package com.smartism.znzk.xiongmai.fragment;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;


import com.smartism.znzk.R;

import static com.smartism.znzk.xiongmai.fragment.XMVideoSettingFragment.VideoSettingInterface.RIGHT_LEFT_REVERSE;
import static com.smartism.znzk.xiongmai.fragment.XMVideoSettingFragment.VideoSettingInterface.TOP_BOTTOM_REVERSE;

public class XMVideoSettingFragment extends Fragment implements View.OnClickListener{




   public  interface VideoSettingInterface{
       final static int TOP_BOTTOM_REVERSE = 0X34,RIGHT_LEFT_REVERSE=0X33;
        //图像倒置功能实现
        boolean changeImageReverse(int flag,boolean isOpen);
    }

    VideoSettingInterface mInterface ;

    ImageView image_reverse_img_top,image_reverse_img_right;
    public XMVideoSettingFragment() {
    }

    public static XMVideoSettingFragment getInstance(Bundle bundle){
        //传入一些设置的状态
        XMVideoSettingFragment fragment = new XMVideoSettingFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(getActivity() instanceof VideoSettingInterface){
            mInterface = (VideoSettingInterface) getActivity();
        }else{
            throw new IllegalArgumentException("活动必须实现VideoSettingInterface接口");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.xm_fragment_video_control,container,false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        image_reverse_img_right = view.findViewById(R.id.image_reverse_img_right);
        image_reverse_img_top = view.findViewById(R.id.image_reverse_img_top);

        image_reverse_img_top.setOnClickListener(this);
        image_reverse_img_right.setOnClickListener(this);


        //View初始状态设置
        if(getArguments()!=null){
            Bundle bundle = getArguments() ;
            //初始化View状态
            if(bundle.getBoolean("isFlipTop")){
                //如果已经上下倒置了
                isRecordImageTop = true ;
                image_reverse_img_top.setImageResource(R.drawable.zhzj_switch_on);
            }

            if(bundle.getBoolean("isFlipRight")){
                //如果已经左右倒置
                isRecordImageRight = true ;
                image_reverse_img_right.setImageResource(R.drawable.zhzj_switch_on);
            }

        }
    }

   boolean isRecordImageTop= false;
    boolean isRecordImageRight = false;
    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.image_reverse_img_top:
                //图像上下倒置,说明已经不正常显示,恢复
                if(isRecordImageTop){
                    image_reverse_img_top.setImageResource(R.drawable.zhzj_switch_off);
                    mInterface.changeImageReverse(TOP_BOTTOM_REVERSE,true);//关闭上下倒置
                    isRecordImageTop = false;
                }else{
                    //图像上下倒置
                    image_reverse_img_top.setImageResource(R.drawable.zhzj_switch_on);
                    mInterface.changeImageReverse(TOP_BOTTOM_REVERSE,false);//打开
                    isRecordImageTop = true ;
                }
                break;
            case R.id.image_reverse_img_right:
                //图像左右倒置
                if(isRecordImageRight){
                    image_reverse_img_right.setImageResource(R.drawable.zhzj_switch_off);
                    mInterface.changeImageReverse(RIGHT_LEFT_REVERSE,true);//关闭
                    isRecordImageRight = false;
                }else{
                    image_reverse_img_right.setImageResource(R.drawable.zhzj_switch_on);
                    mInterface.changeImageReverse(RIGHT_LEFT_REVERSE,false);//打开
                    isRecordImageRight = true ;
                }
                break;
        }
    }
}
