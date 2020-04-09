package com.smartism.znzk.xiongmai.activities;

import android.animation.ObjectAnimator;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.ColorDrawable;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.TextView;
import com.alibaba.fastjson.JSONObject;
import com.lib.SDKCONST;
import com.lsemtmf.genersdk.tools.commen.ToastTools;
import com.smartism.znzk.R;
import com.smartism.znzk.view.alertview.AlertView;
import com.smartism.znzk.view.alertview.OnItemClickListener;
import com.smartism.znzk.xiongmai.lib.funsdk.support.FunError;
import com.smartism.znzk.xiongmai.lib.funsdk.support.FunSupport;
import com.smartism.znzk.xiongmai.lib.funsdk.support.OnFunDeviceOptListener;
import com.smartism.znzk.xiongmai.lib.funsdk.support.config.GeneralGeneral;
import com.smartism.znzk.xiongmai.lib.funsdk.support.config.OPStorageManager;
import com.smartism.znzk.xiongmai.lib.funsdk.support.config.StorageInfo;
import com.smartism.znzk.xiongmai.lib.funsdk.support.models.FunDevice;
import com.smartism.znzk.xiongmai.lib.funsdk.support.utils.FileUtils;
import com.smartism.znzk.xiongmai.lib.funsdk.support.utils.MyUtils;
import com.smartism.znzk.xiongmai.lib.sdk.struct.H264_DVR_FILE_DATA;
import com.smartism.znzk.zhicheng.activities.AirConditioningActivity;
import com.smartism.znzk.zhicheng.activities.MZBaseActivity;
import com.smartism.znzk.zhicheng.tasks.HttpAsyncTask;

import java.util.ArrayList;
import java.util.List;

import static com.smartism.znzk.zhicheng.tasks.HttpAsyncTask.IR_REMOTE_DELETE_URL_FLAG;

/*
*
* */
public class XMDeviceSetupStorage extends MZBaseActivity implements View.OnClickListener, OnFunDeviceOptListener {

    // 存储容量
    private TextView mTextStorageCapacity = null;
    // 录像分区
    private TextView mTextStorageRecord = null;
    // 图片分区
    private TextView mTextStorageSnapshot = null;
    // 剩余容量
    private TextView mTextStorageRemain = null;
    // 停止播放
    private RadioButton mRbRecordStop=null;
    // 循环播放
    private RadioButton mRbRecordCycle=null;
    // 格式化
    private Button mBtnFormat = null;
    //显示使用空间的百分比
    private TextView use_percent_tv ;

    private FunDevice mFunDevice = null;

    private OPStorageManager mOPStorageManager;

    /**
     * 本界面需要获取到的设备配置信息
     */
    private final String[] DEV_CONFIGS = {
            // SD卡存储容量信息
            StorageInfo.CONFIG_NAME,

            // 录像满时停止录像或循环录像
            GeneralGeneral.CONFIG_NAME
    };
    private String device_sn  ;
    MyView top_view ;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //设置标题
        setTitle(getResources().getString(R.string.sd_card_set));
        if(savedInstanceState==null){
            device_sn = getIntent().getStringExtra("sn");
        }else{
            device_sn = savedInstanceState.getString("sn");
        }
        top_view = findViewById(R.id.top_view);
        use_percent_tv = findViewById(R.id.use_percent_tv);
        mTextStorageCapacity = (TextView)findViewById(R.id.textStorageCapacity);
        mTextStorageRecord = (TextView)findViewById(R.id.textStorageRecord);
        mTextStorageSnapshot = (TextView)findViewById(R.id.textStorageSnapshot);
        mTextStorageRemain = (TextView)findViewById(R.id.textStorageRemain);
        mRbRecordStop=(RadioButton)findViewById(R.id.rbRecordStop);
        mRbRecordStop.setOnClickListener(this);
        mRbRecordCycle=(RadioButton)findViewById(R.id.rbRecordCycle);
        mRbRecordCycle.setOnClickListener(this);
        mBtnFormat = (Button)findViewById(R.id.btnStorageFormat);
        mBtnFormat.setOnClickListener(this);
        use_percent_tv.setText(getString(R.string.xmdss_use_space_tip,"0%"));  //默认显示,可以不要
        FunDevice funDevice = FunSupport.getInstance().findDeviceBySn(device_sn);
        if ( null == funDevice ) {
            finish();
            return;
        }
       mFunDevice  = funDevice ;

        // 注册设备操作监听,记得解注册
        FunSupport.getInstance().registerOnFunDeviceOptListener(this);

        // 获取配置信息
        tryGetStorageConfig();

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putString("sn",device_sn);
        super.onSaveInstanceState(outState);
    }

    private void tryGetStorageConfig() {
        if ( null != mFunDevice ) {
            showProgress("");
            for ( String configName : DEV_CONFIGS ) {
                // 删除老的配置信息
                mFunDevice.invalidConfig(configName);
                // 重新搜索新的配置信息
                FunSupport.getInstance().requestDeviceConfig(
                        mFunDevice, configName);
            }
        }
    }

    @Override
    public int setLayoutId() {
        return R.layout.activity_xmdevice_setup_storage_layout;
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.rbRecordStop:
                trySetOverWrite(false);
                break;
            case R.id.rbRecordCycle:
                trySetOverWrite(true);
                 break;
            case R.id.btnStorageFormat:{
                //格式化操作
                new AlertView(getString(R.string.xmdss_sd_title),
                        getString(R.string.delete_sd_remind),
                        getString(R.string.deviceslist_server_leftmenu_delcancel),
                        new String[]{getString(R.string.confirm)}, null,
                        mContext, AlertView.Style.Alert,
                        new OnItemClickListener() {
                            @Override
                            public void onItemClick(Object o, final int position) {
                                if (position != -1) {
                                    if ( requestFormatPartition(0) ) {
                                        showProgress("");
                                    }
                                }
                            }
                        }).show();
            }
            break;
        }
    }

    //修改摄像头对tf卡录制满时的操作
    private void trySetOverWrite(boolean overWrite) {
        GeneralGeneral generalInfo = (GeneralGeneral)mFunDevice.getConfig(GeneralGeneral.CONFIG_NAME);
        if ( null != generalInfo ) {
            if ( overWrite ) {
                //录像满时，循环录像
                generalInfo.setOverWrite(GeneralGeneral.OverWriteType.OverWrite);
            } else {
                //录像满时，停止录像
                generalInfo.setOverWrite(GeneralGeneral.OverWriteType.StopRecord);
            }
            showProgress("");
            FunSupport.getInstance().requestDeviceSetConfig(mFunDevice, generalInfo);
        }
    }


    @Override
    protected void onDestroy() {
        // 注销监听
        FunSupport.getInstance().removeOnFunDeviceOptListener(this);
        super.onDestroy();
    }

    private boolean isCurrentUsefulConfig(String configName) {
        for ( int i = 0; i < DEV_CONFIGS.length; i ++ ) {
            if ( DEV_CONFIGS[i].equals(configName) ) {
                return true;
            }
        }
        return false;
    }


     //判断是否所有需要的配置都获取到了
    private boolean isAllConfigGetted() {
        for ( String configName : DEV_CONFIGS ) {
            if ( null == mFunDevice.getConfig(configName) ) {
                return false;
            }
        }
        return true;
    }

    //获取TF卡大小相关信息
    private void refreshStorageConfig() {
        StorageInfo storageInfo = (StorageInfo)mFunDevice.getConfig(StorageInfo.CONFIG_NAME);
        if ( null != storageInfo ) {
            int totalSpace = 0;
            int remainSpace = 0;
            List<StorageInfo.Partition> partitions = storageInfo.getPartitions();
            for ( StorageInfo.Partition partition : partitions ) {
                if ( partition.IsCurrent ) {
                    // 获取当前分区的大小
                    int partTotalSpace = MyUtils.getIntFromHex(partition.TotalSpace);
                    int partRemainSpace = MyUtils.getIntFromHex(partition.RemainSpace);
                    if ( partition.DirverType == SDKCONST.SDK_FileSystemDriverTypes.SDK_DRIVER_SNAPSHOT ) {
                        // 快照驱动器
                        mTextStorageSnapshot.setText(FileUtils.FormetFileSize(partTotalSpace, 2));
                    } else if ( partition.DirverType == SDKCONST.SDK_FileSystemDriverTypes.SDK_DRIVER_READ_WRITE) {
                        // 关键录像驱动器
                        mTextStorageRecord.setText(FileUtils.FormetFileSize(partTotalSpace, 2));
                    }
                    // 累加总大小
                    totalSpace += partTotalSpace;
                    remainSpace += partRemainSpace;
                }
            }

            String total  =FileUtils.FormetFileSize(totalSpace, 2) ;
            String remain = FileUtils.FormetFileSize(remainSpace, 2);
            //设置大小
            mTextStorageCapacity.setText(total);
            mTextStorageRemain.setText(remain);
            top_view.setUsePercent(1f-((float) remainSpace)/totalSpace);
            String usePercent = Math.round((1f-((float) remainSpace)/totalSpace)*100)+"%";
            use_percent_tv.setText(getString(R.string.xmdss_use_space_tip,usePercent));
        }

        GeneralGeneral generalInfo = (GeneralGeneral)mFunDevice.getConfig(GeneralGeneral.CONFIG_NAME);
        if ( null != generalInfo ) {
            if( generalInfo.getOverWrite() == GeneralGeneral.OverWriteType.OverWrite ) {
                mRbRecordCycle.setChecked(true);
            }
            else{
                mRbRecordStop.setChecked(true);
            }
        }
    }

    /**
     * 请求格式化指定的分区
     */
    private boolean requestFormatPartition(int iPartition) {
        StorageInfo storageInfo = (StorageInfo)mFunDevice.getConfig(StorageInfo.CONFIG_NAME);
        if ( null != storageInfo && iPartition < storageInfo.PartNumber ) {
            if ( null == mOPStorageManager ) {
                mOPStorageManager = new OPStorageManager();
                mOPStorageManager.setAction("Clear");
                mOPStorageManager.setSerialNo(0);
                mOPStorageManager.setType("Data");
            }

            mOPStorageManager.setPartNo(iPartition);

            return FunSupport.getInstance().requestDeviceSetConfig(mFunDevice, mOPStorageManager);
        }

        return false;
    }

    @Override
    public void onDeviceLoginSuccess(FunDevice funDevice) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onDoorBellWakeUp() {

    }


    @Override
    public void onDeviceLoginFailed(FunDevice funDevice, Integer errCode) {
        // TODO Auto-generated method stub

    }


    @Override
    public void onDeviceGetConfigSuccess(FunDevice funDevice,String configName, int nSeq) {
        if ( null != mFunDevice
                && funDevice.getId() == mFunDevice.getId()
                && isCurrentUsefulConfig(configName) ) {
            if ( isAllConfigGetted() ) {
                hideProgress();
            }

            refreshStorageConfig();
        }
    }




    @Override
    public void onDeviceGetConfigFailed(FunDevice funDevice, Integer errCode) {
         hideProgress();
        ToastTools.short_Toast(this,FunError.getErrorStr(errCode));
    }


    @Override
    public void onDeviceSetConfigSuccess(final FunDevice funDevice,
                                         final String configName) {
        if ( null != mFunDevice
                && funDevice.getId() == mFunDevice.getId() ) {

            if ( OPStorageManager.CONFIG_NAME.equals(configName)
                    && null != mOPStorageManager ) {
                // 请求格式化下一个分区
                if ( !requestFormatPartition(mOPStorageManager.getPartNo() + 1) ) {

                    // 所有分区格式化完成之后,重新获取设备磁盘信息
                    tryGetStorageConfig();
                }
            } else if ( GeneralGeneral.CONFIG_NAME.equals(configName) ) {
                // 设置录像满时，选择停止录像或循环录像成功
                hideProgress();
                refreshStorageConfig();
            }
        }
    }

    @Override
    public void onDeviceSetConfigFailed(final FunDevice funDevice,
                                        final String configName, final Integer errCode) {
        ToastTools.short_Toast(this,FunError.getErrorStr(errCode));
        hideProgress();
    }

    @Override
    public void onDeviceChangeInfoSuccess(final FunDevice funDevice) {
        // TODO Auto-generated method stub
    }

    @Override
    public void onDeviceChangeInfoFailed(final FunDevice funDevice, final Integer errCode) {
        // TODO Auto-generated method stub
    }

    @Override
    public void onDeviceOptionSuccess(final FunDevice funDevice, final String option) {
        // TODO Auto-generated method stub
    }

    @Override
    public void onDeviceOptionFailed(final FunDevice funDevice, final String option, final Integer errCode) {
        // TODO Auto-generated method stub
    }

    @Override
    public void onDeviceFileListChanged(FunDevice funDevice) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onDeviceFileListChanged(FunDevice funDevice, H264_DVR_FILE_DATA[] datas) {

    }


    @Override
    public void onDeviceFileListGetFailed(FunDevice funDevice) {
        // TODO Auto-generated method stub

    }





    //自定义显示使用空间大小百分比控件
    public static class MyView extends View {
        private int witdh, height ; //控件宽高
        private float mRadius ; //半径
        private Paint mPaint ;
        private int bgColor  ;
        private float cx,cy ; //圆心坐标
        private float totalPercent = 1f;
        public float usePercent =0f;
        private RectF mUseRectf  ;
        public MyView(Context context) {
            super(context);
            init(context);
        }

        public MyView(Context context, @Nullable AttributeSet attrs) {
            super(context, attrs);
            init(context);
        }

        void init(Context context){
            mPaint = new Paint();
            mPaint.setAntiAlias(true);
            mPaint.setDither(true);
            mPaint.setColor(context.getResources().getColor(R.color.device_main_bg));
            mPaint.setTextSize(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP,12.0f,context.getResources().getDisplayMetrics()));
            bgColor = context.getResources().getColor(R.color.zhzj_default);
            mPaint.setStyle(Paint.Style.STROKE);
            mPaint.setStrokeWidth(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,2f,context.getResources().getDisplayMetrics()));
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            mPaint.setStyle(Paint.Style.STROKE);
            canvas.drawCircle(cx,cy,mRadius,mPaint);//圆的轮廓画出来
            mPaint.setStyle(Paint.Style.FILL);
            float sweepAngle = usePercent * 360 ;
            canvas.drawArc(mUseRectf,90f-sweepAngle/2,sweepAngle,false , mPaint);
        }

        @Override
        protected void onSizeChanged(int w, int h, int oldw, int oldh) {
            super.onSizeChanged(w, h, oldw, oldh);
            witdh = w-getPaddingLeft()-getPaddingRight() ;
            height = h - getPaddingTop()-getPaddingBottom() ;

            mRadius = Math.min(witdh,height)/2;
            cx = getPaddingLeft()+witdh/2;
            cy = getPaddingTop()+height/2;
            mUseRectf = new RectF();
            mUseRectf.left = cx - mRadius ;
            mUseRectf.right = mUseRectf.left+2*mRadius;
            mUseRectf.top = cy-mRadius;
            mUseRectf.bottom = mUseRectf.top+2*mRadius;
        }

        public void setUsePercent(float value){
            usePercent = value;
            postInvalidate();
        }
    }
}
