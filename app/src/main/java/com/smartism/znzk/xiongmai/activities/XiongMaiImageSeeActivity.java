package com.smartism.znzk.xiongmai.activities;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.smartism.znzk.R;
import com.smartism.znzk.activity.ActivityParentActivity;
import com.smartism.znzk.adapter.recycleradapter.RecyclerItemBean;
import com.youth.banner.transformer.ZoomOutSlideTransformer;

import java.io.File;
import java.util.ArrayList;

public class XiongMaiImageSeeActivity extends ActivityParentActivity {

    ViewPager mViewPager ;
    ArrayList<String> paths;
    TextView mDisplayCount;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);//全屏
        setContentView(R.layout.activity_image_see);
        mViewPager = findViewById(R.id.viewPager_image);
        mDisplayCount = findViewById(R.id.displayCountText);
        initData();
    }

    private void initData(){
        Intent intent = getIntent();
        ArrayList<RecyclerItemBean> list = (ArrayList<RecyclerItemBean>) intent.getSerializableExtra("paths");
        int position = intent.getIntExtra("position",-1);
        paths = getPaths(list);
        mDisplayCount.setVisibility(View.VISIBLE);
        mDisplayCount.setText((position+1)+"/"+paths.size());
        MyViewPagerAdapter mAdapter = new MyViewPagerAdapter(paths,this);
        mViewPager.setAdapter(mAdapter);
        mViewPager.setCurrentItem(position);
        mViewPager.setOffscreenPageLimit(paths.size());//设置缓存的页面数
        mViewPager.setPageTransformer(true,new ZoomOutSlideTransformer());
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                mDisplayCount.setText((position+1)+"/"+paths.size());
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    private ArrayList<String> getPaths(ArrayList<RecyclerItemBean> list){
        ArrayList<String> tempList = new ArrayList<>();
        if(list.size()>0){
            for(RecyclerItemBean temp :list){
                tempList.add((String) temp.getT());
            }
        }
        return tempList ;
    }


    class MyViewPagerAdapter extends PagerAdapter{

        Context mContext ;
        ArrayList<String> filePaths = new ArrayList<>();

        public MyViewPagerAdapter(ArrayList<String> paths,Context context){
            filePaths = paths ;
            mContext = context ;
        }
        @Override
        public int getCount() {
            return filePaths.size();
        }

        @Override
        public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
            return view==object;
        }

        @NonNull
        @Override
        public Object instantiateItem(@NonNull ViewGroup container, int position) {
            MyImageView imageView = new MyImageView(mContext);
            imageView.setImageDrawable(filePaths.get(position));
            container.addView(imageView);
            return imageView;
        }

        @Override
        public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
            container.removeViewAt(position);
        }

        class MyImageView extends ImageView{

            String imagePath = null;
            int mWidth;
            int mHeight;

            public MyImageView(Context context) {
                super(context);
                init();
            }

            private void init() {
                DisplayMetrics metrics = getResources().getDisplayMetrics();
                mHeight = (int) (metrics.heightPixels * 0.4f);
                mWidth = metrics.widthPixels;
                ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(mWidth, mHeight);
                setLayoutParams(lp);
            }

            //通过路径设置显示图片
            public void setImageDrawable(String filePath) {
                if (!new File(filePath).exists()) {
                    //这里应该设置一个默认的显示图片
                    return;
                }
                imagePath = filePath;
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inJustDecodeBounds = true;
                BitmapFactory.decodeFile(filePath, options);
                int inSampleSize = getInSampleSize(mWidth, mHeight, options);
                options.inJustDecodeBounds = false;
                options.inSampleSize = inSampleSize;
                Bitmap bitmap = BitmapFactory.decodeFile(filePath, options);
                System.out.println(bitmap);
                setImageBitmap(bitmap);//显示图片
            }

            private int getInSampleSize(int reqWidth, int reqHeight, BitmapFactory.Options options) {
                int inSampleSize = 1;
                int bitWidth = options.outWidth;
                int bitHeight = options.outWidth;
                if (bitWidth > reqWidth || bitHeight > reqHeight) {
                    int halfWidth = bitWidth / 2;
                    int halfHeight = bitHeight / 2;
                    System.out.println("half:" + halfHeight + "-" + halfWidth);
                    System.out.println("req:" + reqHeight + "-" + reqWidth);
                    System.out.println(inSampleSize);
                    while ((halfWidth / inSampleSize) >= reqWidth &&
                            (halfHeight / inSampleSize) >= reqHeight) {

                        inSampleSize *= 2;
                    }
                }
                return inSampleSize;
            }

        }

    }

}
