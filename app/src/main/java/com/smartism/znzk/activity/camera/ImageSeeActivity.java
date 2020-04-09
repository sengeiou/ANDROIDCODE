package com.smartism.znzk.activity.camera;

import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;

import com.smartism.znzk.R;
import com.smartism.znzk.activity.ActivityParentActivity;
import com.smartism.znzk.db.camera.Contact;
import com.smartism.znzk.util.camera.ImageUtils;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.List;

public class ImageSeeActivity extends ActivityParentActivity {
    private ViewPager vp;
    private String device;
    private Contact mContact;
    private int startactivity = -1;
    private String mPath;
    public static final int IMAGE_WIDTH_HEIGHT = 100;
    /**
     * 装ImageView数组
     */
    private ImageView[] mImageViews;
    private int id;
    private int position = -1;
    String path;
    /**
     * 图片资源String
     */
    File[] data;
    Context context;
    private List<String> imgIdArray = new ArrayList<String>();

    @Override
    protected void onCreate(Bundle arg0) {
        super.onCreate(arg0);
        setContentView(R.layout.activity_image_see);
        mContact = (Contact) getIntent().getSerializableExtra("contact");
        startactivity = getIntent().getIntExtra("startactivity", -1);
        position = getIntent().getIntExtra("position", -1);
        vp = (ViewPager) findViewById(R.id.viewPager_image);
        id = getIntent().getIntExtra("number", 0);
        mPath = getIntent().getStringExtra("path");
        device = getIntent().getStringExtra("device");
        if (id != 0 || device != null) {
            path = Environment.getExternalStorageDirectory().getPath()
                    + "/v380";
        } else {
            path = Environment.getExternalStorageDirectory().getPath()
                    + "/screenshot";
        }

        File file = new File(path);
        FileFilter filter = new FileFilter() {

            @Override
            public boolean accept(File pathname) {
                // TODO Auto-generated method stub
                if (pathname.getName().endsWith(".jpg")) {
                    return true;
                } else {
                    return false;
                }

            }
        };
        data = file.listFiles(filter);
        if (null == data) {
            data = new File[0];

        }

        initData();
        int index = getIndex(mPath);
        if (index != -1) {
            vp.setCurrentItem(index);
        }
    }

    public int getIndex(String imgPath) {
        if (path == null || path == "") return -1;
        for (int index = 0; index < imgIdArray.size(); index++) {
            if (imgPath.equals(imgIdArray.get(index))) return index;
        }
        return -1;
    }

    private void initData() {

        for (int i = 0; i < data.length; i++) {
            if (data[i].getPath().contains(mContact.contactId))
                imgIdArray.add(data[i].getPath());
        }
        //将图片装载到数组中
        mImageViews = new ImageView[imgIdArray.size()];
        for (int i = 0; i < mImageViews.length; i++) {
            ImageView imageView = new ImageView(this);
            mImageViews[i] = imageView;

            imageView.setImageBitmap(ImageUtils.getBitmap(imgIdArray.get(i), LayoutParams.WRAP_CONTENT,
                    LayoutParams.WRAP_CONTENT));
            //设置Adapter  
            vp.setAdapter(new MyAdapter());

//          //设置ViewPager的默认项, 设置为长度的100倍，这样子开始就能往左滑动
//            vp.setCurrentItem(0 * 100);
        }


    }

    public class MyAdapter extends PagerAdapter {

        @Override
        public int getCount() {
            return mImageViews.length;
        }

        @Override
        public boolean isViewFromObject(View arg0, Object arg1) {
            return arg0 == arg1;
        }

        @Override
        public void destroyItem(View container, int position, Object object) {
            ((ViewPager) container).removeView(mImageViews[position]);

        }

        /**
         * 载入图片进去，用当前的position 除以 图片数组长度取余数是关键
         */
        @Override
        public Object instantiateItem(View container, int position) {
            ((ViewPager) container).addView(mImageViews[position]);
            mImageViews[position].setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
//                    if (startactivity == 1) {
//                        Intent monitor = new Intent();
//                        monitor.setClass(ImageSeeActivity.this, ApMonitorActivity.class);
//                        monitor.putExtra("flag", getIntent().getBooleanExtra("flag", false));
//                        monitor.putExtra("contact", mContact);
//                        monitor.putExtra("connectType", Constants.ConnectType.P2PCONNECT);
//                        ImageSeeActivity.this.startActivity(monitor);
//                    }
                    finish();
                }
            });
            return mImageViews[position];
        }

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) { //监控/拦截/屏蔽返回键
//            if (startactivity == 1) {
//                Intent monitor = new Intent();
//                monitor.setClass(this, ApMonitorActivity.class);
//                monitor.putExtra("flag", getIntent().getBooleanExtra("flag", false));
//                monitor.putExtra("contact", mContact);
//                monitor.putExtra("connectType", Constants.ConnectType.P2PCONNECT);
//                startActivity(monitor);
//            }
            finish();
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_MENU) {
            //监控/拦截菜单键
        } else if (keyCode == KeyEvent.KEYCODE_HOME) {
            //由于Home键为系统键，此处不能捕获，需要重写onAttachedToWindow()
        }
        return super.onKeyDown(keyCode, event);
    }
}
