package com.smartism.znzk.activity.view;

import java.util.ArrayList;
import java.util.List;

import com.smartism.znzk.R;
import com.smartism.znzk.activity.user.GenstureSettingActivity;
import com.smartism.znzk.activity.view.LockPatternView.Cell;
import com.smartism.znzk.activity.view.LockPatternView.DisplayMode;
import com.smartism.znzk.util.DataCenterSharedPreferences;
import com.smartism.znzk.util.DataCenterSharedPreferences.Constant;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class LockSetupActivity extends Activity implements LockPatternView.OnPatternListener, OnClickListener {

    //private static final String TAG = "LockSetupActivity";
    private LockPatternView lockPatternView;
    private TextView tv_setting_gensure;
    private Button leftButton;
    private Button rightButton;
    private static final int STEP_1 = 1; // 开始
    private static final int STEP_2 = 2; // 第一次设置手势完成
    private static final int STEP_3 = 3; // 按下继续按钮
    private static final int STEP_4 = 4; // 第二次设置手势完成
    // private static final int SETP_5 = 4; // 按确认按钮
    private int step;
    private List<Cell> choosePattern;
    private boolean confirm = false;

    DataCenterSharedPreferences dcsp = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lock_setup);

        dcsp = DataCenterSharedPreferences.getInstance(getApplicationContext(),
                Constant.CONFIG);
        lockPatternView = (LockPatternView) findViewById(R.id.lock_pattern);
        lockPatternView.setOnPatternListener(this);
        tv_setting_gensure = (TextView) findViewById(R.id.tv_setting_gensure);
        leftButton = (Button) findViewById(R.id.left_btn);
        rightButton = (Button) findViewById(R.id.right_btn);
        step = STEP_1;
        updateView();
    }

    private void updateView() {
        switch (step) {
            case STEP_1:

                leftButton.setText(R.string.cancel_panel);
                rightButton.setText("");
                rightButton.setVisibility(View.INVISIBLE);
                rightButton.setEnabled(false);
                choosePattern = null;
                confirm = false;
                lockPatternView.clearPattern();
                lockPatternView.enableInput();
                break;
            case STEP_2:
                leftButton.setText(R.string.try_again_panel);
                rightButton.setText(R.string.goon);
                rightButton.setVisibility(View.VISIBLE);
                rightButton.setEnabled(true);
                lockPatternView.disableInput();

                break;
            case STEP_3:
                leftButton.setText(R.string.cancel_panel);
                rightButton.setText("");
                rightButton.setVisibility(View.INVISIBLE);
                rightButton.setEnabled(false);
                lockPatternView.clearPattern();
                lockPatternView.enableInput();
                break;
            case STEP_4:
                leftButton.setText(R.string.cancel_panel);

                if (confirm) {
                    rightButton.setText(R.string.confirm);
                    rightButton.setEnabled(true);
                    lockPatternView.disableInput();
                    rightButton.setVisibility(View.VISIBLE);
                } else {
                    rightButton.setText(getString(R.string.error));
                    lockPatternView.setDisplayMode(DisplayMode.Wrong);
                    lockPatternView.enableInput();
                    rightButton.setEnabled(false);
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.left_btn:
                if (step == STEP_1 || step == STEP_3 || step == STEP_4) {
                    finish();
                } else if (step == STEP_2) {
                    step = STEP_1;
                    updateView();
                }
                break;
            case R.id.right_btn:
                if (step == STEP_2) {
                    step = STEP_3;
                    tv_setting_gensure.setText(R.string.confirm);
                    updateView();
                } else if (step == STEP_4) {
                    dcsp.putString(Constant.CODE_GENSTURE, LockPatternView.patternToString(choosePattern))
                            .putBoolean(Constant.IS_APP_GENSTURE, true).commit();
                    //Log.e("aaa", dcsp.getString(Constant.CODE_GENSTURE, "没有密码"));
                    //Log.e("aaa", LockPatternView.patternToString(choosePattern));
                    //			SharedPreferences preferences = getSharedPreferences(TAG_MAIN, MODE_PRIVATE);
                    //			preferences.edit().putString(LOCK_KEY, LockPatternView.patternToString(choosePattern)).commit();
                    // 将手势密码写进到sharePrefence中
                    Toast.makeText(getApplicationContext(), R.string.gensture_save, Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(LockSetupActivity.this, GenstureSettingActivity.class));
                    finish();
                }
                break;
            default:
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
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

    @Override
    public void onPatternDetected(List<Cell> pattern) {
        if (pattern.size() < LockPatternView.MIN_LOCK_PATTERN_SIZE) {
            Toast.makeText(this, R.string.lockpattern_recording_incorrect_too_short, Toast.LENGTH_LONG).show();
            lockPatternView.setDisplayMode(DisplayMode.Wrong);
            return;
        }
        if (choosePattern == null) {
            choosePattern = new ArrayList<Cell>(pattern);
            // Log.d(TAG, "choosePattern = " +
            // Arrays.toString(choosePattern.toArray()));
            step = STEP_2;
            updateView();
            return;
        }
        //Log.e(TAG, "choosePattern = " + Arrays.toString(choosePattern.toArray()));
        //Log.e(TAG, "pattern = " + Arrays.toString(pattern.toArray()));
        if (choosePattern.equals(pattern)) {
            //Log.e(TAG, "pattern = " + Arrays.toString(pattern.toArray()));
            confirm = true;
        } else {
            confirm = false;
        }
        step = STEP_4;
        updateView();
    }
}
