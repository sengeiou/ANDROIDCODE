package com.smartism.znzk.xiongmai.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.smartism.znzk.R;

import java.util.ArrayList;
import java.util.List;

public class PickerParentLayout extends LinearLayout implements View.OnClickListener{
    LinearLayout mTopLayout ; //顶部父布局
    LinearLayout mBottomLayout;//picker的布局
    TextView title_tv  ;
    TextView concel_tv ;
    TextView confirm_tv;
    String mTitle,mConcel,mConfirm;
    int numberCount =1; //选择器数量
    float textSize = 18 ;
    int textColor  = Color.BLACK;
    Drawable mTopBackground ;
    List<MyNumberPicker> pickers = new ArrayList<>();
    int pickerDivider  = Color.GRAY;

    OnClickListener mListener ;

    @Override
    public void onClick(View v) {
        if(mListener!=null){
            String[] temp = new String[pickers.size()];
            for(int i=0;i<pickers.size();i++){
                temp[i] = pickerValues.get(i)[pickers.get(i).getValue()];
            }
            switch (v.getId()){
                case 0:
                    //取消
                    mListener.btnOnClick(temp,v,0);
                    break;
                case 1:
                    //确定
                    mListener.btnOnClick(temp,v,1);
                    break;
            }

        }
    }

    public interface OnClickListener{
        public void btnOnClick(String[] pickerValues,View v,int position);
    }

    public PickerParentLayout(Context context){
        super(context);
        setOrientation(VERTICAL);
        initView();
    }

    public PickerParentLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        setOrientation(VERTICAL);
        iniAttr(context,attrs);
        initView();
    }


    private void iniAttr(Context context , AttributeSet attributeSet){
        //初始化属性
        TypedArray array = context.obtainStyledAttributes(attributeSet, R.styleable.PickerParentLayout);
        mTitle = array.getString(R.styleable.PickerParentLayout_pl_title);
        mConcel = array.getString(R.styleable.PickerParentLayout_pl_concel);
        mConfirm = array.getString(R.styleable.PickerParentLayout_pl_confirm);
        textSize = array.getDimension(R.styleable.PickerParentLayout_pl_textSize,18f);
        textColor=array.getColor(R.styleable.PickerParentLayout_pl_textColor,Color.BLACK);
        mTopBackground = array.getDrawable(R.styleable.PickerParentLayout_pl_background);
        pickerDivider = array.getColor(R.styleable.PickerParentLayout_pl_pickerDeviderColor,Color.GRAY);
        array.recycle();
    }

    private void initView(){
        if(TextUtils.isEmpty(mTitle)){
            mTitle = "";
        }
        if(TextUtils.isEmpty(mConcel)){
            mConcel = getResources().getString(R.string.cancel);
        }

        if(TextUtils.isEmpty(mConfirm)){
            mConfirm = getResources().getString(R.string.confirm);
        }
        int text_padding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,10,getResources().getDisplayMetrics());
        mBottomLayout = new LinearLayout(getContext());
        mTopLayout = new LinearLayout(getContext());
        LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT);
        mTopLayout.setLayoutParams(lp);
        mTopLayout.setOrientation(LinearLayout.HORIZONTAL);
        if(mTopBackground!=null){
            mTopLayout.setBackground(mTopBackground);
        }
        //取消
        concel_tv = new TextView(getContext());
        concel_tv.setPadding(text_padding,text_padding,text_padding,text_padding);
        concel_tv.setText(mConcel);
        concel_tv.setTextSize(textSize);
        concel_tv.setTextColor(textColor);
        mTopLayout.addView(concel_tv);
        concel_tv.setId(0);
        concel_tv.setOnClickListener(this);
        //标题
        title_tv = new TextView(getContext());
        title_tv.setText(mTitle);
        title_tv.setTextSize(textSize);
        title_tv.setGravity(Gravity.CENTER);
        title_tv.setTextColor(textColor);
        LinearLayout.LayoutParams title_lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        title_lp.weight = 1 ;
        title_tv.setLayoutParams(title_lp);
        mTopLayout.addView(title_tv);
        //确定
        confirm_tv = new TextView(getContext());
        confirm_tv.setTextColor(textColor);
        confirm_tv.setTextSize(textSize);
        confirm_tv.setText(mConfirm);
        confirm_tv.setPadding(text_padding,text_padding,text_padding,text_padding);
        mTopLayout.addView(confirm_tv);
        confirm_tv.setOnClickListener(this);
        confirm_tv.setId(1);
        addView(mTopLayout);

        mBottomLayout.setOrientation(HORIZONTAL);
        LayoutParams picker_lp = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        picker_lp.weight = 1 ;
        mBottomLayout.setLayoutParams(picker_lp);
        mBottomLayout.setBackground(new ColorDrawable(Color.WHITE));
        addView(mBottomLayout);

    }

    public void setTitle(String title){
        title_tv.setText(title);
        invalidate();
    }

    public void setConfirmTitle(String content){
        confirm_tv.setText(content);
        invalidate();
    }

    public void setConcelTitle(String content){
        concel_tv.setText(content);
        invalidate();
    }
    public void setTopBackground(Drawable drawable){
        mTopLayout.setBackground(drawable);
        invalidate();
    }

    List<String[]> pickerValues ;
    //这是Picker的显示值
    public void setPickerDisplayValues(ArrayList<String[]> values){
        if(values==null&&values.size()==0){
            throw new IllegalArgumentException("values must not null");
        }
        numberCount = values.size();
        pickerValues = values ;
        mBottomLayout.removeAllViews();//删除以前的View
        pickers.clear();//清空
        //创建选择器
        for(int i=0;i<numberCount;i++){
            MyNumberPicker numberPicker = new MyNumberPicker(getContext());
            numberPicker.setDeviderColor(Color.GRAY);
            LayoutParams picker = new LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT);
            picker.weight = 1 ;
            numberPicker.setLayoutParams(picker);
            numberPicker.setDeviderColor(pickerDivider);
            numberPicker.setDisplayedValues(values.get(i));
            numberPicker.setMaxValue(values.get(i).length-1);
            numberPicker.setMinValue(0);
            numberPicker.setWrapSelectorWheel(true);
            pickers.add(numberPicker);
            mBottomLayout.addView(numberPicker);
        }
        requestLayout();
    }


    public void setPickerClick(OnClickListener listener){
        mListener = listener ;
    }
}
