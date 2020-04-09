package com.smartism.znzk.widget;



import com.smartism.znzk.R;

import android.app.Dialog;
import android.content.Context;
import android.text.InputFilter;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;



/**
 * Created by dxs on 2015/9/10.
 */
public class ImputDialog extends Dialog {
    private EditText etImput;
    private TextView txTitle,btnSure,btnCancel;
    private ImputDialog dialog;

    public ImputDialog(Context context) {
        super(context,R.style.CustomnewDialog);
        initUI(context);
    }

    public ImputDialog(Context context, int theme) {
        super(context, theme);
        initUI(context);
    }

    protected ImputDialog(Context context, boolean cancelable, OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
        initUI(context);
    }

    private void initUI(Context context) {
        dialog=this;
        View view=LayoutInflater.from(context).inflate(R.layout.dialog_input,null);
        setContentView(view);
        etImput= (EditText) view.findViewById(R.id.input1);
        txTitle= (TextView) view.findViewById(R.id.title_text);
        btnCancel= (TextView) view.findViewById(R.id.button2_text);
        btnSure= (TextView) view.findViewById(R.id.button1_text);
        etImput.setInputType(InputType.TYPE_CLASS_TEXT);
        btnSure.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                inputClickListner.onYesClick(dialog,v, etImput.getText().toString().trim());
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                inputClickListner.onNoClick(v);
                inputDialogDismiss();
            }
        });
        

    }

    public void inputDialogShow(){
        this.show();
    }

    public void setEditextHint(String etHint){
        etImput.setHint(etHint);
    }

    public void setInputTitle(String title){
        txTitle.setText(title);
    }

    public void inputDialogDismiss(){
        this.dismiss();
    }

    public void setYes(String Yes){
        btnSure.setText(Yes);
    }

    public void setNo(String No){
        btnCancel.setText(No);
    }

    public void setEdtextText(String etText){
        etImput.setText(etText);
        Log.e("dxsprepoin", "长度-->" + etText.length());
        etImput.setSelection(etText.length());
    }

    public void setEdtextType(int inputType){
        etImput.setInputType(inputType);
    }

    public void setEMS(int EMS){
        etImput.setMaxEms(EMS);

    }
    public void setMaxWidth(int pix){
        etImput.setMaxWidth(pix);
    }

    public void setMaxCharater(int numbers){
        etImput.setFilters(new InputFilter[]{new InputFilter.LengthFilter(numbers)});
    }


    public void SetText(String title,String hint,String etText,String Yes,String No){
        setInputTitle(title);
        if(etText.length()<=0){
            setEditextHint(hint);
        }else{
            setEdtextText(etText);
        }
        setYes(Yes);
        setNo(No);
    }


    private MyInputClickListner inputClickListner;
    public void setOnMyinputClickListner(MyInputClickListner inputClickListner){
        this.inputClickListner=inputClickListner;
    }
    public interface MyInputClickListner{
        void onYesClick(Dialog dialog, View v, String input);
        void onNoClick(View v);
    }
    public void setInputType(){
    	
    }

}
