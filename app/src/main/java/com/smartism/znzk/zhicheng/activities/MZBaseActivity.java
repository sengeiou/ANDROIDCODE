package com.smartism.znzk.zhicheng.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.NavUtils;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.smartism.znzk.R;
import com.smartism.znzk.activity.FragmentParentActivity;
import com.smartism.znzk.fragment.BallProgressDialog;
import com.smartism.znzk.util.Util;
import com.smartism.znzk.widget.customview.CustomProgressDialog;
import com.smartism.znzk.zhicheng.iviews.IBaseView;

public abstract class MZBaseActivity extends FragmentParentActivity implements IBaseView {

    protected BallProgressDialog mDialog ;
    FrameLayout child_content_mz ;
    Toolbar mToolbar;
    TextView title ;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mz_base_layout);
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M){
            Util.setStatusBarColor(this,getResources().getColor(R.color.device_main_bg));
        }
        if(savedInstanceState==null){
            mDialog = BallProgressDialog.getDialogInstance(getResources().getString(R.string.loading));
        }
        init();
        setChildLayout();
    }


    public abstract  int setLayoutId();

  /*  //初始化子View工作
    public void initial(){

    }*/

    void setChildLayout(){
        View view  = getLayoutInflater().inflate(setLayoutId(),child_content_mz,false);
        child_content_mz.addView(view);
    }

    public void setTitle(String title){
        mToolbar.setTitle(title);
      //  this.title.setText(title);
    }

    void init(){
        child_content_mz = findViewById(R.id.child_content_mz);
        mToolbar = findViewById(R.id.toolbar_mz);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("");
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = NavUtils.getParentActivityIntent(MZBaseActivity.this);
                if(intent!=null){
                    NavUtils.navigateUpTo(MZBaseActivity.this,intent);
                }
                finish();

            }
        });
        title = findViewById(R.id.title_tv_mz);
    }


    @Override
    public void showProgress(String text) {
        if(mDialog!=null&&(mDialog.getDialog()==null||!mDialog.getDialog().isShowing())){
            mDialog.setContent(text);
            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            fragmentTransaction.setCustomAnimations(R.anim.fade_in_center,R.anim.fade_out_center,R.anim.fade_in_center,R.anim.fade_out_center);
            mDialog.show(fragmentTransaction,"dialog");
        }
    }

    @Override
    public void hideProgress() {
        if(mDialog!=null&&mDialog.getDialog()!=null&&mDialog.getDialog().isShowing()){
            mDialog.dismiss();
        }
    }

    //让子类去实现
    @Override
    public void error(String message) {
        if(message==null||message.equals("")){
            return ;
        }
        Toast toast = Toast.makeText(this,"",Toast.LENGTH_LONG);
        toast.setText(message);
        toast.show();
    }

    //具体功能让子类去实现
    @Override
    public void success(String message) {
        if(message==null||message.equals("")){
            return ;
        }
        Toast toast = Toast.makeText(this,"",Toast.LENGTH_LONG);
        toast.setText(message);
        toast.show();
    }

    public static boolean isActive(Activity activity) {
        if(activity==null&&activity.isFinishing()){
            return false;
        }
        return true ;
    }
}
