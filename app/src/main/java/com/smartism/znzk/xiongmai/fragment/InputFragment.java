package com.smartism.znzk.xiongmai.fragment;


import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;


import com.smartism.znzk.R;


/*
* 密码输入对话框
* */
public class InputFragment extends DialogFragment implements View.OnClickListener{

    //数据key
    private static final String IPF_TITLE_FLAG = "input_password_fragment_title";
    private static final String IPF_INPUT_HINT = "input_password_fragment_hint";

    public interface OnInputContentListener{
        //输入内容，是否点击确认按钮
        void onInputContent(String content,boolean confirm);
    }

    public static InputFragment getInstance(String title, String hint){
        Bundle bundle = new Bundle();
        bundle.putString(IPF_TITLE_FLAG,title);
        bundle.putString(IPF_INPUT_HINT,hint);
        InputFragment inputFragment = new InputFragment();
        inputFragment.setArguments(bundle);
        return inputFragment;
    }


    private String mTitle ;
    private String mHintText ;

    private OnInputContentListener mListener ;

    //确认取消按钮
    private Button mCancelBtn,mConfirmBtn ;
    //标题
    private TextView mTitleTv  ;
    //密码输入框
    private EditText mInputPwdEdit ;

    public InputFragment() {

    }


    private void initView(View parent){
        mTitleTv =  parent.findViewById(R.id.input_title_tv);
        mCancelBtn = parent.findViewById(R.id.cancel_btn);
        mConfirmBtn = parent.findViewById(R.id.confirm_btn);
        mInputPwdEdit = parent.findViewById(R.id.new_password_edit);

        if(!TextUtils.isEmpty(mHintText)){
            mInputPwdEdit.setHint(mHintText);
        }

        if(!TextUtils.isEmpty(mTitle)){
            mTitleTv.setText(mTitle);
        }

        //事件
        mCancelBtn.setOnClickListener(this);
        mConfirmBtn.setOnClickListener(this);

    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(getActivity() instanceof OnInputContentListener){
            mListener = (OnInputContentListener) getActivity();
            if(getArguments()!=null){
                Bundle bundle = getArguments() ;
                mTitle = bundle.getString(IPF_TITLE_FLAG);
                mHintText = bundle.getString(IPF_INPUT_HINT);
            }
        }else{
            throw new IllegalStateException("宿主Activity必须实现OnInputContentListener");
        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_input_password_layout, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initView(view);//初始化View
        //设置Dialog
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);//去除标题
        getDialog().setCancelable(true);
        getDialog().getWindow().setGravity(Gravity.CENTER);
        getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT)); //透明
    }

    @Override
    public void onResume() {
        super.onResume();
        //设置宽高
        getDialog().getWindow().setLayout((int) (getResources().getDisplayMetrics().widthPixels*0.85f+0.5f),
                ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.cancel_btn:
                mListener.onInputContent(mInputPwdEdit.getText().toString(),false);
                dismiss();
                break ;
            case R.id.confirm_btn:
                mListener.onInputContent(mInputPwdEdit.getText().toString(),true);
                dismiss();
                break ;
        }
    }
}
