package com.smartism.znzk.activity.user;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.smartism.znzk.R;
import com.smartism.znzk.activity.FragmentParentActivity;
import com.smartism.znzk.application.MainApplication;
import com.smartism.znzk.fragment.EmailRegisterFragment;
import com.smartism.znzk.fragment.PhoneRegisterFragment;

import java.util.ArrayList;
import java.util.List;


public class RegisterActivity extends FragmentParentActivity {
    private TextView title;
    private int flag = 1;  //是请求验证码的 “t” ：1、是注册请求，2、登录请求 3、找回密码请求
    private TabLayout tabLayout;
    private ArrayList<String> titles;
    private List<Fragment> fragmentList;
    private PageAdapter pageAdapter;
    private ViewPager viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        //启动activity时不自动弹出软键盘
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        initView();
    }

    private void initView() {
        flag = getIntent().getIntExtra("flag", 1);
        title = (TextView) findViewById(R.id.regiter_title);
        if (flag == 1) {
            title.setText(getResources().getString(R.string.register_title_button));
        } else {
            title.setText(getResources().getString(R.string.resetpassword_zhaohuititle));
        }
        tabLayout = (TabLayout) findViewById(R.id.tablayout);
        viewPager = (ViewPager) findViewById(R.id.viewpager);
        initTab();
        pageAdapter = new PageAdapter(getSupportFragmentManager());
        viewPager.setAdapter(pageAdapter);
        tabLayout.setupWithViewPager(viewPager);
    }


    private void initTab() {
        fragmentList = new ArrayList<>();
        titles = new ArrayList<>();
        if (MainApplication.app.getAppGlobalConfig().isPhone()) {
            titles.add(getString(R.string.register_phone_regis));
            fragmentList.add(new PhoneRegisterFragment());
        } else {
            tabLayout.setVisibility(View.GONE);
        }
        fragmentList.add(new EmailRegisterFragment());
        titles.add(getString(R.string.register_email_regis));
        tabLayout.setTabMode(TabLayout.MODE_FIXED);
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);
    }


    private class PageAdapter extends FragmentPagerAdapter {
        public PageAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public int getCount() {
            return fragmentList.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return titles.get(position);
        }

        @Override
        public Fragment getItem(int position) {
            return fragmentList.get(position);
        }
    }

    public void back(View v) {
        finish();
    }

}
