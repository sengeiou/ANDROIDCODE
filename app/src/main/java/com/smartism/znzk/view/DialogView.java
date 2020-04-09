package com.smartism.znzk.view;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.smartism.znzk.R;

/**
 * Created by Administrator on 2017/2/27.
 */

public class DialogView implements View.OnClickListener {
    private Dialog dialog;
    private String title;
    private String message;
    private String cancel;
    private String other;
    private String sure;
    private TextView tv_cancel;
    private TextView tv_other;
    private TextView tv_sure;
    private DialogViewItemListener dialogViewItemListener;

    public DialogView(Context context, boolean isDismissing,
                      String title, CharSequence message, String cancel, String other, CharSequence sure, DialogViewItemListener dialogViewItemListener) {
        this.dialogViewItemListener = dialogViewItemListener;
        int mWindowWidth, mWindowHeight;
        dialog = new Dialog(context, R.style.simpleDialogStyle);
        dialog.setCanceledOnTouchOutside(isDismissing);
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_dialogview, null);
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        mWindowWidth = displayMetrics.widthPixels;
        mWindowHeight = displayMetrics.heightPixels;
        dialog.setContentView(view, new ViewGroup.MarginLayoutParams(mWindowWidth*4/5,
                ViewGroup.MarginLayoutParams.MATCH_PARENT));
//        dialog.setOnKeyListener(new DialogInterface.OnKeyListener(){
//            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
//                return true;
//            }
//        });//屏蔽掉按键 需要可以开启注释
        initView(view,title,message,cancel,other,sure);
    }

    public void setOnKeyListener(DialogInterface.OnKeyListener listener){
        if (listener != null) {
            dialog.setOnKeyListener(listener);
        }
    }

    private void initView(View view, String title, CharSequence message, String cancel, String other, CharSequence sure) {
        TextView tv_title = (TextView) view.findViewById(R.id.dialog_title);
        TextView tv_massge = (TextView) view.findViewById(R.id.dialog_message);
        tv_title.setText(title==null?"":title);
        tv_massge.setText(message==null?"":message);
        if (cancel!=null){
            TextView tv_cancel = (TextView) view.findViewById(R.id.dialog_cancel);
            tv_cancel.setVisibility(View.VISIBLE);
            tv_cancel.setText(cancel);
            tv_cancel.setOnClickListener(this);
        }
        if (other!=null){
            TextView tv_other = (TextView) view.findViewById(R.id.dialog_other);
            tv_other.setVisibility(View.VISIBLE);
            tv_other.setText(other);
            tv_other.setOnClickListener(this);
        }
        if (sure!=null){
            TextView tv_sure = (TextView) view.findViewById(R.id.dialog_sure);
            tv_sure.setVisibility(View.VISIBLE);
            tv_sure.setText(sure);
            tv_sure.setOnClickListener(this);
        }
    }

    public void show() {
        if (dialog != null) {
            dialog.show();
        }
    }
    public void dismiss() {
        if (dialog != null) {
            dialog.dismiss();
            dialog = null;
        }
    }
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.dialog_cancel:
                dialogViewItemListener.onItemListener(this,v,0);
                dismiss();
                break;
            case R.id.dialog_other:
                dialogViewItemListener.onItemListener(this,v,1);
                break;
            case R.id.dialog_sure:
                dialogViewItemListener.onItemListener(this,v,2);
                dismiss();
                break;
        }
//        dismiss();
    }


    public interface DialogViewItemListener {
        public void onItemListener(DialogView dialogView,View view,int index);
    }
}
