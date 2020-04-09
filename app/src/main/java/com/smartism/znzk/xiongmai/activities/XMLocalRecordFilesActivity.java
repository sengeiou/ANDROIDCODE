package com.smartism.znzk.xiongmai.activities;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Vibrator;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.smartism.znzk.R;
import com.smartism.znzk.activity.ActivityParentActivity;
import com.smartism.znzk.application.MainApplication;
import com.smartism.znzk.widget.NormalDialog;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*
* 本地视频文件展示活动 -雄迈
* */
public class XMLocalRecordFilesActivity extends ActivityParentActivity implements AdapterView.OnItemLongClickListener,AdapterView.OnItemClickListener{

    final String TAG = getClass().getSimpleName();
    RelativeLayout titleLayout ; //标题栏
    LinearLayout mItemLayout ; //分割线布局
    GridView mGridView ; //视频列表布局
    List<String> mMediaPaths = new ArrayList<>();//本地视频路径
    Map<String,Object> mPicPathsMap = new HashMap<>();
    String Local_MEDIA_PATH ;
    String mDeviceSN  ;
    int mWidthImage; //列表项的宽度
    BaseAdapter mBaseAdapter ;
    ImageView mBackButton ;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_local_recordfiles);
        if(savedInstanceState==null){
            //初始化路径
            mDeviceSN = getIntent().getStringExtra("sn");
        }else{
            mDeviceSN = savedInstanceState.getString("sn");
        }
        init();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putString("sn",mDeviceSN);
        super.onSaveInstanceState(outState);

    }

    private void init(){
        //初始化控件
        titleLayout = findViewById(R.id.layout_title);
        mItemLayout = findViewById(R.id.include_layout);
        mGridView = findViewById(R.id.local_list);
        mBackButton = findViewById(R.id.back_btn);
        mBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //退出
                finish();
            }
        });
       //初始化文件数据
        initialFileData();

        //初始化列表项宽度
        mWidthImage = getResources().getDisplayMetrics().widthPixels/3;//一行显示三个

        //接下来是显示
        mBaseAdapter = new MyAdapter(mWidthImage,this);
        mGridView.setOnItemLongClickListener(this);
        mGridView.setOnItemClickListener(this);
        mGridView.setAdapter(mBaseAdapter);//显示列表

    }


    private void initialFileData(){
        //像这些自字符串最好用一个字符串变量来代替，避免出错
        Local_MEDIA_PATH  =Environment.getExternalStorageDirectory()+ File.separator+getPackageName()+File.separator+"xiongmaitempimg"
                +File.separator+mDeviceSN+File.separator+"local_media";//存放当前设备的本地录像的路径
        //初始化缩略图路径
        File file = new File(Local_MEDIA_PATH);
        Log.v(TAG,"文件路径:"+file.toString());
        if(file.exists()){
            //视频文件路径
            String[] temp = file.list(new FilenameFilter() {
                @Override
                public boolean accept(File file, String s) {
                    if(s.endsWith(".mp4")){
                        return true;
                    }
                    return false ;
                }
            });
            /*
             * 咱得这样处理，如果有缩率图，则显示缩率图，没有咱显示一家人看未来的图片
             * */
            if(temp!=null){
                for(int i =0;i<temp.length;i++){
                    String mediaPath  = file.toString()+File.separator+temp[i] ;
                    mMediaPaths.add(mediaPath);//注意通过list方法获取出来的文件名是不包含父路径的，因此需要添加上前缀
                    String picPath = file.toString()+File.separator+temp[i].split(".mp4")[0]+".jpg";
                    if(new File(picPath).exists()) {
                        //说明该视频存在缩率图片，那么视频封面就显示缩率图。
                        mPicPathsMap.put(mediaPath,picPath);
                    }else{
                        mPicPathsMap.put(mediaPath, R.drawable.header_icon);//一家人看视频照片
                    }
                }
            }
            Log.v(TAG,mMediaPaths.toString());
            Log.v(TAG,mPicPathsMap.toString());//打印输出一下
        }
    }
    //点击列表项调用手机自带的播放器进行播放
    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        Uri mediaUri = null ;
        if(Build.VERSION.SDK_INT<Build.VERSION_CODES.N){
            //本版小于Android7.0时
            mediaUri = Uri.fromFile(new File(mMediaPaths.get(i)));
        }else{
            //版本大于等于Android7.0时
            mediaUri = FileProvider.getUriForFile(this, MainApplication.app.getPackageName()+".FileProvider"
                    ,new File(mMediaPaths.get(i)));
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);//这里需要进行临时授权,因为FileProvider必须以临时授权的方式
        }
        Log.v(TAG,mediaUri.toString());
        intent.setDataAndType(mediaUri,"video/*");
        if(intent.resolveActivity(getPackageManager())!=null){
            startActivity(intent);
        }else{
            Log.v(TAG,"手机好像不存在视频播放软件");
        }
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> adapterView, View view, final int i, long l) {
        //实现删除
        NormalDialog dialog = new NormalDialog(this, getResources().getString(R.string.delete), getResources().getString(R.string.confirm_delete),
                getResources().getString(R.string.delete),
                getResources().getString(R.string.cancel));
        dialog.setOnButtonOkListener(new NormalDialog.OnButtonOkListener() {

            @Override
            public void onClick() {
                File mediaFile = new File(mMediaPaths.get(i));//视频文件
                if (mediaFile.exists()) {
                    mediaFile.delete();
                }
                //缩率图文件
                Object obj = mPicPathsMap.get(mMediaPaths.get(i));
                if(obj instanceof  String){
                    File picFile = new File((String) obj);
                    if(picFile.exists()){
                        picFile.delete();
                    }
                }
                mPicPathsMap.remove(mMediaPaths.get(i));
                mMediaPaths.remove(i);
                mBaseAdapter.notifyDataSetChanged();
            }
        });
        Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        vibrator.vibrate(500);
        dialog.showDialog();
        return true;
    }


    class MyAdapter extends BaseAdapter{

        int mWidthImage ; //每一个列表项宽度
        Context mContext ;
        public MyAdapter(int imageWidth,Context context){
            this.mContext = context;
            mWidthImage = imageWidth ;
        }
        @Override
        public int getCount() {
            return mMediaPaths.size();
        }

        @Override
        public Object getItem(int i) {
            return mMediaPaths.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            ViewHolder viewHolder = null ;
            if(view==null){
                viewHolder = new ViewHolder();
                FrameLayout frameLayout = new FrameLayout(mContext);
                AdapterView.LayoutParams frameLp = new AdapterView.LayoutParams(AdapterView.LayoutParams.WRAP_CONTENT, AdapterView.LayoutParams.WRAP_CONTENT);
                frameLayout.setLayoutParams(frameLp);
                //设置显示的缩率图
                ImageView imageView = new ImageView(mContext);
                ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(mWidthImage,mWidthImage);
                imageView.setPadding(8,0,0,0);
                imageView.setLayoutParams(lp);
                frameLayout.addView(imageView);
                //设置播放按钮
                ImageButton imageButton = new ImageButton(mContext);
                imageButton.setClickable(false);//设置为不可以点击
                imageButton.setImageResource(R.drawable.device_online);
                FrameLayout.LayoutParams imageButtonLp = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                imageButtonLp.gravity = Gravity.CENTER;
                imageButton.setBackgroundDrawable(new ColorDrawable(0x00000000));
                imageButton.setLayoutParams(imageButtonLp);
                imageButton.setFocusable(false);
                //imageButton.setClickable(false);
                frameLayout.addView(imageButton);

                viewHolder.imageButton = imageButton ;
                viewHolder.imageView = imageView ;

                frameLayout.setTag(viewHolder);//保存ViewHolder
                view = frameLayout;

            }else{
               viewHolder = (ViewHolder) view.getTag();
            }
            Object obj =  mPicPathsMap.get(mMediaPaths.get(i));
            if(obj instanceof  String){
                //有缩率图显示缩率图，没有显示一家人看照片图片
                viewHolder.imageView.setImageBitmap(fromFilePath((String) obj));//设置图片
            }else{
                viewHolder.imageView.setImageBitmap(fromFilePath((Integer)obj));
            }

            return view;
        }

        //从文件当中加载图片
        private Bitmap fromFilePath(String path){
            Bitmap bitmap =  null;
            File file = new File(path);
            if(!file.exists()){
                return null;
            }
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true ; //只加载图片宽高等信息，不加载实际的资源
            BitmapFactory.decodeFile(path,options);
           options.inSampleSize = getInSampleSize(options);
            //再一次加载图片
            options.inJustDecodeBounds = false ;
            bitmap = BitmapFactory.decodeFile(path,options);
            return bitmap;
        }

        //从资源文件当中加载图片
        private Bitmap fromFilePath(int resId){
            Bitmap bitmap =  null;
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true ; //只加载图片宽高等信息，不加载实际的资源
            BitmapFactory.decodeResource(getResources(),resId,options);
            options.inSampleSize = getInSampleSize(options);
            //再一次加载图片
            options.inJustDecodeBounds = false ;
            bitmap = BitmapFactory.decodeResource(getResources(),resId,options);

            return bitmap;
        }

        private int getInSampleSize(BitmapFactory.Options options){
            int inSampleSize = 1;  //进行采样率计算
            int reqWidth = mWidthImage;
            int reqHeight = mWidthImage ; //要求的宽高
            int picWidth = options.outWidth;
            int picHeight = options.outHeight;

            if(picHeight>reqHeight||picWidth>reqWidth){
                int halfWidth = picWidth/2;
                int halfHeight = picHeight/2;
                while((halfHeight/inSampleSize)>=reqHeight
                        &&(halfWidth/inSampleSize)>=reqWidth){
                    //表明这个采样率可以
                    inSampleSize *=2 ;
                }
            }
            return inSampleSize;
        }


        //提高查找效率
        class ViewHolder{
            ImageView imageView ;
            ImageButton imageButton ;
        }
    }
}
