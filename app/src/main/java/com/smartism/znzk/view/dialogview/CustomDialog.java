package com.smartism.znzk.view.dialogview;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.smartism.znzk.R;


/**
 * Created by jiaweili on 16/11/29.
 */
public class CustomDialog extends ProgressDialog {


    ProgressBar pdLoad;
    TextView tvLoadDialog;

    private String message = null;

    public CustomDialog(Context context) {
        super(context);

    }

    public CustomDialog(Context context, int theme) {
        super(context, theme);

    }
  public CustomDialog(Context context, int theme, String message) {
        super(context, theme);
        this.message = message;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        init(getContext());
    }


    private void init(Context context) {
        pdLoad = (ProgressBar) findViewById(R.id.pd_load);
        tvLoadDialog = (TextView) findViewById(R.id.tv_load_dialog);
        //设置不可取消，点击其他区域不能取消，实际中可以抽出去封装供外包设置
        setCancelable(false);
        setCanceledOnTouchOutside(false);

        setContentView(R.layout.dialog_wait);
        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.width = WindowManager.LayoutParams.WRAP_CONTENT;
        params.height = WindowManager.LayoutParams.WRAP_CONTENT;
        getWindow().setAttributes(params);

    }

    @Override
    public void show() {
        super.show();
    }


}
