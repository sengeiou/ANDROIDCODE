package com.smartism.znzk.activity.view;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.smartism.znzk.R;
import com.smartism.znzk.activity.FragmentParentActivity;
import com.smartism.znzk.activity.camera.ApMonitorActivity;
import com.smartism.znzk.activity.user.LoginActivity;
import com.smartism.znzk.activity.view.LockPatternView.Cell;
import com.smartism.znzk.activity.view.LockPatternView.DisplayMode;
import com.smartism.znzk.communication.service.CoreService;
import com.smartism.znzk.global.Constants;
import com.smartism.znzk.util.DataCenterSharedPreferences;
import com.smartism.znzk.util.DataCenterSharedPreferences.Constant;
import com.smartism.znzk.util.HttpRequestUtils;
import com.smartism.znzk.util.JavaThreadPool;
import com.smartism.znzk.util.Util;
import com.smartism.znzk.util.WeakRefHandler;

import java.util.List;

public class LockActivity extends Activity implements LockPatternView.OnPatternListener {

    private List<Cell> lockPattern;
    private LockPatternView lockPatternView;
    protected DataCenterSharedPreferences dcsp = null;

    private int lockPattearnErrorCount;
    private long dateOfLockPattearn;
    private Runnable removeErrorLockPattern;
    private final long removeDelayTime = 300l;
    private TextView errorCountAndLeftTime;

    private String patternString;
    private TextView tv_cancle;
    private boolean isCamera;

    private Handler.Callback mCallback = new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            return false;
        }
    };
    private Handler mHandler = new WeakRefHandler(mCallback);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lock);

        dcsp = DataCenterSharedPreferences.getInstance(getApplicationContext(),
                Constant.CONFIG);
        patternString = dcsp.getString(Constant.CODE_GENSTURE, "");
        lockPattern = LockPatternView.stringToPattern(patternString);
        lockPatternView = (LockPatternView) findViewById(R.id.lock_pattern);
        lockPatternView.setOnPatternListener(this);
        isCamera = getIntent().getBooleanExtra("isCamera", false);
        errorCountAndLeftTime = (TextView) findViewById(R.id.error_count_left_time);
        tv_cancle = (TextView) findViewById(R.id.tv_cancle);
        tv_cancle.setVisibility(getIntent().getBooleanExtra("isCancle", false) == true ? View.VISIBLE : View.GONE);
        removeErrorLockPattern = new Runnable() {

            @Override
            public void run() {
                lockPatternView.clearPattern();
            }
        };
        lockPattearnErrorCount = dcsp.getInt(Constant.COUNT_ERRORLOCKPATTERN, 0);
        dateOfLockPattearn = dcsp.getLong(Constant.DATE_OF_LOCKPATTERNENTERERROR, 0);
        if (lockPattearnErrorCount > 0 && lockPattearnErrorCount < 5) {

            String pre = getResources().getString(R.string.gensture_wrong_pre_tip);
            String tail = getResources().getString(R.string.gensture_wrong_tail_tip);


            errorCountAndLeftTime.setText(pre + " " + (5 - lockPattearnErrorCount) + " " + tail);
        } else if (lockPattearnErrorCount >= 5 && (System.currentTimeMillis() - dateOfLockPattearn) < 60 * 1000) {
            lockPatternView.disableInput();
            timer();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        lockPattearnErrorCount = dcsp.getInt(Constant.COUNT_ERRORLOCKPATTERN, 0);
        dateOfLockPattearn = dcsp.getLong(Constant.DATE_OF_LOCKPATTERNENTERERROR, 0);
        if ((System.currentTimeMillis() - dateOfLockPattearn) >= 6 * 60 * 60 * 1000) { //6小时一个轮回
            updateCountAndDate(0, 0);
        }
    }

    private void timer() {
        long cur = System.currentTimeMillis();

        String tail = getResources().getString(R.string.gensture_resttime);

        errorCountAndLeftTime.setText((60 - (cur - dateOfLockPattearn) / 1000) + " " + tail);
        mHandler.postDelayed(new Runnable() {

            @Override
            public void run() {
                long cur = System.currentTimeMillis();
                if ((cur - dateOfLockPattearn) <= 60 * 1000) {
                    lockPatternView.disableInput();
                    timer();
                } else {
                    lockPatternView.enableInput();
                    errorCountAndLeftTime.setText("");
                    updateCountAndDate(lockPattearnErrorCount - 1, System.currentTimeMillis());
                }
            }
        }, 1000);
    }

    public void cancle(View v) {
        if (isCamera) {
            Intent intent = getIntent();
            intent.putExtra("isShot",false);
            intent.setClass(this, ApMonitorActivity.class);
            startActivity(intent);
        }
        finish();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
//		getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public void onPatternStart() {
    }

    @Override
    public void onPatternCleared() {
    }

    @Override
    public void onPatternCellAdded(List<Cell> pattern) {
    }

    private void updateCountAndDate(int count, long date) {
        lockPattearnErrorCount = count;
        dateOfLockPattearn = date;
        dcsp.putInt(Constant.COUNT_ERRORLOCKPATTERN, lockPattearnErrorCount)
                .putLong(Constant.DATE_OF_LOCKPATTERNENTERERROR, dateOfLockPattearn).commit();
        if (count >= 5) {
            lockPatternView.disableInput();
            timer();
        } else if (count > 0 && count < 5) {

            String pre = getResources().getString(R.string.gensture_wrong_pre_tip);
            String tail = getResources().getString(R.string.gensture_wrong_tail_tip);


            errorCountAndLeftTime.setText(pre + " " + (5 - lockPattearnErrorCount) + " " + tail);
        }
    }

    @Override
    public void onPatternDetected(List<Cell> pattern) {
        if (pattern.equals(lockPattern)) {
            updateCountAndDate(0, 0);
            dcsp.putBoolean(Constant.IS_LOOKS, false).commit();
            if (isCamera) {
                Intent intent = getIntent();
                intent.setClass(this, ApMonitorActivity.class);
                intent.putExtra("isShot", true);
                startActivity(intent);
            }
            if (getIntent().getBooleanExtra("isCancle", false) == true)//此处非摄像头页面跳转逻辑
                setResult(RESULT_OK, getIntent());
            finish();
        } else {
            lockPatternView.setDisplayMode(DisplayMode.Wrong);
            mHandler.removeCallbacks(removeErrorLockPattern);
            mHandler.postDelayed(removeErrorLockPattern, removeDelayTime);
            updateCountAndDate(lockPattearnErrorCount + 1, System.currentTimeMillis());
            Toast.makeText(this, R.string.lockpattern_error, Toast.LENGTH_SHORT).show();
        }
    }

    //忘记手势密码后从新回到登陆界面
    public void resetPassWord(View v) {
        updateCountAndDate(0, 0);
        dcsp.putBoolean(Constant.IS_LOOKS, false).commit();
        dcsp.getEditor().remove(Constant.CODE_GENSTURE).commit();

        JavaThreadPool.getInstance().excute(new ExitLogin());

        //preferences.edit().clear().commit();
        // 跳转到登录页面，并且需要清空activity栈
        Intent refreshContans = new Intent();
        refreshContans.setAction(Constants.Action.ACTIVITY_FINISH);
        sendBroadcast(refreshContans);
        Intent loginIntent = new Intent();
        loginIntent.setClass(this, CoreService.class);
        stopService(loginIntent);
        loginIntent.addFlags(
                Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        loginIntent.setClass(this, LoginActivity.class);

        //MainApplication.LockS = false;
        startActivity(loginIntent);
    }

    private class ExitLogin implements Runnable {
        @Override
        public void run() {
            String server = dcsp.getString(DataCenterSharedPreferences.Constant.HTTP_DATA_SERVERS, "");
            String result = HttpRequestUtils
                    .requestoOkHttpPost(server + "/jdm/s3/u/logout", null,dcsp);
            Util.clearLoginInfo(getApplicationContext(), dcsp); //清空用户登录信息
        }
    }
}
