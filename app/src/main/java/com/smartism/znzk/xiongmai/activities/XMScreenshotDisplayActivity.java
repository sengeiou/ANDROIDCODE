package com.smartism.znzk.xiongmai.activities;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Vibrator;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.smartism.znzk.R;
import com.smartism.znzk.adapter.recycleradapter.BaseRecyslerAdapter;
import com.smartism.znzk.adapter.recycleradapter.RecyclerItemBean;
import com.smartism.znzk.camera.adapter.ImageListAdapter;
import com.smartism.znzk.widget.NormalDialog;
import com.smartism.znzk.zhicheng.activities.MZBaseActivity;

import java.io.File;
import java.io.FilenameFilter;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

//图片加载缓存策略没有考虑，如果图片过多，可能会导致卡顿
public class XMScreenshotDisplayActivity extends MZBaseActivity implements BaseRecyslerAdapter.RecyclerItemLongClickListener,BaseRecyslerAdapter.RecyclerItemClickListener {

    private  final int PERMMISION_REQUEST_CODE = 0X99;//申请储存权限请求码
    private final   int WRITE_PERMISSION_REQUEST_CODE = 0x89;

    private String[] needPermission = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};
    private RecyclerView mRecyclerView ;
    private ImageListAdapter mAdapter ;
    private List<RecyclerItemBean> pictrues =  new ArrayList<>();;
    private String mDeviceSn = "" ;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(savedInstanceState==null){
            mDeviceSn = getIntent().getStringExtra("sn");
        }else{
            mDeviceSn = savedInstanceState.getString("sn");
        }
        setTitle(getString(R.string.capture));
        initView();
        checkPermission();

    }

    private void checkPermission(){
        if(ContextCompat.checkSelfPermission(this,Manifest.permission.WRITE_EXTERNAL_STORAGE)==PackageManager.PERMISSION_DENIED){
            ActivityCompat.requestPermissions(this,needPermission,PERMMISION_REQUEST_CODE);
        }else{
            initImageList();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode==PERMMISION_REQUEST_CODE){
            if(permissions[0].equals(needPermission[0])){
                if(grantResults[0]==PackageManager.PERMISSION_GRANTED){
                    initImageList();
                }else{
                    if(!ActivityCompat.shouldShowRequestPermissionRationale(this,needPermission[0])){
                        new AlertDialog.Builder(this)
                                .setTitle(getString(R.string.permission_setting_app))
                                .setMessage( getString(R.string.permission_storage))
                                .setCancelable(true)
                                .setPositiveButton(getString(R.string.ready_guide_msg13), new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        Intent  intent = new Intent();
                                        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                        intent.setData(Uri.fromParts("package",getPackageName(),null));
                                        startActivityForResult(intent,WRITE_PERMISSION_REQUEST_CODE);
                                    }
                                }).setNegativeButton(getString(R.string.cancel_panel),null)
                                .show();
                    }else{
                        new AlertDialog.Builder(this)
                                .setTitle(getString(R.string.permission_request))
                                .setMessage( getString(R.string.permission_storage))
                                .setCancelable(true)
                                .setPositiveButton(getString(R.string.ready_guide_msg13),null)
                                .show();
                    }

                }

            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==WRITE_PERMISSION_REQUEST_CODE){
            if(ContextCompat.checkSelfPermission(this,Manifest.permission.WRITE_EXTERNAL_STORAGE)==PackageManager.PERMISSION_GRANTED){
                initImageList();
            }

        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putString("sn",mDeviceSn);
        super.onSaveInstanceState(outState);
    }

    private void initView(){
        mRecyclerView=findViewById(R.id.horizon_listview);//列表
        mAdapter = new ImageListAdapter(pictrues);
        mRecyclerView.setAdapter(mAdapter);
        mAdapter.setRecyclerItemClickListener(this);
        mAdapter.setRecyclerItemLongClickListener(this);

        //创建默认线性LinearLayoutManager
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this,3);
        //设置布局管理器
        mRecyclerView.setLayoutManager(gridLayoutManager);
        //设置adapter
        //设置Item增加、移除动画
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
    }

    @Override
    public int setLayoutId() {
        return R.layout.activity_xmscreenshot_display;
    }

    private void initImageList(){
        ArrayList<File> lastTimeFile = new ArrayList<>();
        if(pictrues==null){
            pictrues = new ArrayList<>();
        }
        pictrues.clear();//清除里面的内容
        //获取截图目录下的文件
        List<String> tempList= getImageFiles();
        if(tempList.size()>0){
            //按最后修改的时间进行排序,前提是用户没对图片进行修改
            Collections.sort(tempList, new Comparator<String>() {
                @Override
                public int compare(String o1, String o2) {
                    File file1 = new File(o1);
                    File file2 = new File(o2);
                    return file1.lastModified()>file2.lastModified()?-1:
                            file1.lastModified()==file2.lastModified()?0:1;
                }
            });
            for(String path :tempList){
                pictrues.add(new RecyclerItemBean(path,1));
            }
        }

        if(mAdapter!=null){
            mAdapter.notifyDataSetChanged();
        }
    }

    //获取截图目录下的文件名
    private List<String> getImageFiles(){
        List<String> list = new ArrayList<>();
        File picFile = new File(Environment.getExternalStorageDirectory().toString()+File.separator+getPackageName()
                +File.separator+"xiongmaitempimg"+File.separator+mDeviceSn+File.separator+"local_picture");
        if(!picFile.exists()){
            return  list;
        }
        //获取该目录下的文件名，注意仅仅是文件名
        String[] temp = picFile.list(new FilenameFilter() {
            @Override
            public boolean accept(File file, String s) {
                //s表示当前目录下的文件名
                if(s.endsWith(".jpg")){
                    return true;
                }
                return false ;
            }
        });
        if(temp!=null){
            //加上父目录路径
            for(int i=0;i<temp.length;i++){
                temp[i]=picFile.toString()+File.separator+temp[i];
                list.add(temp[i]);
            }
        }

        return list;
    }

    @Override
    public void onRecycleItemClick(View view, int position) {
        Intent intent = new Intent();
        intent.setClass(this, XiongMaiImageSeeActivity.class);
        intent.putExtra("paths", (Serializable) pictrues);
        intent.putExtra("position",position);
        startActivity(intent);
    }

    @Override
    public boolean onRecycleItemLongClick(View view, final int position) {
        //长按事件
        final String path = (String) pictrues.get(position).getT();
        NormalDialog dialog = new NormalDialog(this, getResources().getString(R.string.delete), getResources().getString(R.string.confirm_delete),
                getResources().getString(R.string.delete),
                getResources().getString(R.string.cancel));
        dialog.setOnButtonOkListener(new NormalDialog.OnButtonOkListener() {

            @Override
            public void onClick() {
                File file = new File(path);
                if (file.exists()) {
                    file.delete();
                    pictrues.remove(position);
                    mAdapter.notifyItemRemoved(position);
                }
            }
        });
        Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        vibrator.vibrate(500);
        dialog.showDialog();
        return true;
    }
}
