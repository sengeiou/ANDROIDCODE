package com.smartism.znzk.xiongmai.devices.tour.presenter;

import android.content.Context;
import android.os.Message;
import android.support.annotation.Nullable;

import com.lib.MsgContent;
import com.lib.SDKCONST;
import com.smartism.znzk.R;
import com.smartism.znzk.xiongmai.devices.tour.listener.TourContract;
import com.smartism.znzk.xiongmai.devices.tour.listener.TourDataSource;
import com.smartism.znzk.xiongmai.devices.tour.model.TourRepository;
import com.smartism.znzk.xiongmai.devices.tour.model.bean.OPTourControlBean;
import com.smartism.znzk.xiongmai.devices.tour.model.bean.PTZTourBean;
import com.smartism.znzk.xiongmai.devices.tour.model.bean.TourBean;
import com.smartism.znzk.xiongmai.devices.tour.model.bean.TourState;
import com.smartism.znzk.xiongmai.lib.funsdk.support.models.FunDevice;
import com.smartism.znzk.xiongmai.lib.sdk.bean.DetectTrackBean;
import com.smartism.znzk.xiongmai.lib.sdk.bean.OPPTZControlBean;
import com.smartism.znzk.xiongmai.lib.sdk.bean.TimimgPtzTourBean;

import java.util.List;

/**
 * Created by ccy on 2017-09-28.
 *
 *  2019年09月27日  拷贝自 FunSDKDemo_Android_Old2018
 *  整个 tour包都是拷过来的,此类拷贝自：demo中的  com.example.funsdkdemo.devices.tour.presenter 在基础上有修改，增加DetectTrackBean的克隆传输，设置成功失败都调用变更函数
 * @author 王建
 */

public class TourPresenter implements TourContract.ITourPresenter{
    private static final int WATCH_PRESET = 100;//守望预置点
    private Context context;
    private TourContract.ITourView tourView; // 持有V
    private TourDataSource dataSource;      //持有M
    private TourState mCurrentTourState = TourState.IDLE; //当前巡航状态
    private TimimgPtzTourBean timimgPtzTourBean;
    private DetectTrackBean detectTrackBean;
    public TourPresenter(Context context, TourContract.ITourView tourView, FunDevice funDevice) {
        this.context = context;
        this.tourView = tourView;
        dataSource = new TourRepository(funDevice,context);
    }

    @Override
    public void getTour() {
        tourView.showLoading(true, context.getString(R.string.loading));
        dataSource.getTour(new SimpleTourCallback<List<PTZTourBean>>() {
            @Override
            public void onSuccess(@Nullable List<PTZTourBean> ptzTourBeen) {
                if (tourView.isActive()) {
                    //因相同ID可以重复添加，有可能误操作导致巡航线上的点超过了3个，那么认为出了错误，清除之
                    if (ptzTourBeen != null && ptzTourBeen.get(0).Tour.size() > 3) {
                        clearTour(ptzTourBeen.get(0).Id);
                    } //巡航线上已有的巡航点不是从小到大排序的，也认为出了错误
                    else if(ptzTourBeen != null && !isSorted(ptzTourBeen.get(0).Tour)){
                        clearTour(ptzTourBeen.get(0).Id);
                    } else {
                        tourView.dismissLoading();
                        tourView.onLoadTours((ptzTourBeen == null) ? null : ptzTourBeen.get(0));
                    }
                }
            }
        });
    }

    /**
     * 从小到大排序？
     * @param data
     * @return
     */
    private boolean isSorted(List<TourBean> data){
        int lastId = 0 ;
        for (int i = 0; i < data.size(); i++) {
            if(i == 0){
                lastId = data.get(0).Id;
                continue;
            }
            if(lastId > data.get(i).Id){
                return false;
            }
            lastId = data.get(i).Id;
        }
        return true;
    }


    @Override
    public void addTour(final int presetId, final int tourId, final int presetIndex) {

        //因巡航线允许重复添加相同id的巡航点，为避免发生这种情况，加载框不允许取消
        tourView.showLoading(false, "");

        //1、新建预置点
        dataSource.controlPreset(OPPTZControlBean.SET_PRESET, 0, presetId, new SimpleTourCallback() {
            @Override
            public void onSuccess(@Nullable Object o) {
                //2、将新建的预置点添加到巡航线上
                dataSource.controlTour(OPTourControlBean.ADD_TOUR, presetId, tourId, presetIndex, new SimpleTourCallback() {
                    @Override
                    public void onSuccess(@Nullable Object o) {
                        if (tourView.isActive()) {
                            tourView.dismissLoading();
                            tourView.onTourAdded(presetId);
                        }
                    }
                });
            }
        });
    }



    @Override
    public void resetTour(final int presetId, final int tourId) {
        tourView.showLoading(true, "");

        //重新设置预置点即可
        dataSource.controlPreset(OPPTZControlBean.SET_PRESET, 0, presetId, new SimpleTourCallback() {
            @Override
            public void onSuccess(@Nullable Object o) {
                if (tourView.isActive()) {
                    tourView.dismissLoading();
                    tourView.onTourReseted(presetId);
                }
            }
        });
    }

    @Override
    public void deleteTour(final int presetId, final int tourId) {

        tourView.showLoading(true, "");

        //1、删除对应预置点（该步可省略）
        dataSource.controlPreset(OPPTZControlBean.REMOVE_PRESET, 0, presetId, new SimpleTourCallback() {
            @Override
            public void onSuccess(@Nullable Object o) {
                //2、移除巡航线上对应的点
                dataSource.controlTour(OPTourControlBean.DELETE_TOUR, presetId, tourId, new SimpleTourCallback() {
                    @Override
                    public void onSuccess(@Nullable Object o) {
                        if (tourView.isActive()) {
                            tourView.dismissLoading();
                            tourView.onTourDeleted(presetId);
                        }
                    }
                });
            }
        });
    }

    @Override
    public void startTour(final int tourId) {
        tourView.showLoading(true, "");
        dataSource.controlTour(OPTourControlBean.START_TOUR, 0, tourId, new SimpleTourCallback() {
            @Override
            public void onSuccess(@Nullable Object o) {
                if (tourView.isActive()) {
                    tourView.dismissLoading();
                    tourView.onTourStarted();
                }
            }
        });
        dataSource.registerTourEnd(new SimpleTourCallback() {
            @Override
            public void onSuccess(@Nullable Object o) {
                if (tourView.isActive()) {
                    tourView.onTourStoped();
                }
            }
        });
    }

    @Override
    public void stopTour() {
        tourView.showLoading(true, "");
        dataSource.controlTour(OPTourControlBean.STOP_TOUR, 0, 0, new SimpleTourCallback() {
            @Override
            public void onSuccess(@Nullable Object o) {
                if (tourView.isActive()) {
                    tourView.dismissLoading();
                    tourView.onTourStoped();
                }
            }
        });
    }

    @Override
    public void start360Tour() {
        tourView.showLoading(true, "");
        //转到99号预置点（不用预设）即开启360巡航
        dataSource.controlPreset(OPPTZControlBean.TURN_PRESET, 0, 99, new SimpleTourCallback() {
            @Override
            public void onSuccess(@Nullable Object o) {
                if (tourView.isActive()) {
                    tourView.dismissLoading();
                    tourView.onTour360Started();
                }
            }
        });
    }

    @Override
    public void stop360Tour() {
        //TODO:停止360巡航
    }

    @Override
    public void clearTour(int tourId) {
        tourView.showLoading(true, "");
        dataSource.controlTour(OPTourControlBean.CLEAR_TOUR, 0, tourId, new SimpleTourCallback() {
            @Override
            public void onSuccess(@Nullable Object o) {
                if (tourView.isActive()) {
                    tourView.dismissLoading();
                    tourView.onTourCleared();
                }
            }
        });
    }

    @Override
    public void turnPreset(int presetId) {
        tourView.showLoading(true, "");
        dataSource.controlPreset(OPPTZControlBean.TURN_PRESET, 0, presetId, new SimpleTourCallback() {
            @Override
            public void onSuccess(@Nullable Object o) {
                if (tourView.isActive()) {
                    tourView.dismissLoading();
                }
            }
        });
    }

    @Override
    public void removeAllCallback() {
        dataSource.removeAllCallback();
    }

    @Override
    public void setTourState(TourState state) {
        mCurrentTourState = state;
    }

    @Override
    public TourState getTourState() {
        return mCurrentTourState;
    }

    @Override
    public void getTimimgPtzTour() {
        dataSource.getTimimgPtzTour(new TourDataSource.TourCallback() {
            @Override
            public void onSuccess(@Nullable Object result) {
                timimgPtzTourBean = (TimimgPtzTourBean) result;
                if (tourView != null && timimgPtzTourBean != null) {
                    tourView.onTmimgPtzTourResult(timimgPtzTourBean.isEnable(),timimgPtzTourBean.getTimeInterval());
                }
            }

            @Override
            public void onError(Message msg, MsgContent ex, String extraStr) {
                if (tourView != null) {
                    tourView.onFailed(msg,ex,extraStr);
                }
            }
        });
    }

    @Override
    public void setTimingPtzTour(boolean isEnable, int timeInterval) {
        if (timimgPtzTourBean != null) {
            timimgPtzTourBean.setEnable(isEnable);
            timimgPtzTourBean.setTimeInterval(timeInterval);
            dataSource.setTimingPtzTour(timimgPtzTourBean, new TourDataSource.TourCallback() {
                @Override
                public void onSuccess(@Nullable Object o) {
                    if (tourView != null) {
                        tourView.onSaveTimimgPtzTourResult(true);
                    }
                }

                @Override
                public void onError(Message msg, MsgContent ex, String extraStr) {
                    if (tourView != null) {
                        tourView.onFailed(msg,ex,extraStr);
                    }
                }
            });
        }
    }

    @Override
    public void getDetectTrack() {
        tourView.showLoading(true, "");
        dataSource.getDetectTrack(new TourDataSource.TourCallback() {
            @Override
            public void onSuccess(@Nullable Object object) {
                detectTrackBean = (DetectTrackBean) object;
                if (tourView != null && detectTrackBean != null) {
                    tourView.onUpdateDetectTrack(detectTrackBean.getEnable() == SDKCONST.Switch.Open,
                            detectTrackBean.getSensitivity());
                }
            }

            @Override
            public void onError(Message msg, MsgContent ex, String extraStr) {
                if (tourView != null) {
                    tourView.onFailed(msg,ex,extraStr);
                }
            }
        });
    }

    @Override
    public void setDetectTrackSwitch(int iSwitch) {
        if (detectTrackBean == null) {
            return;
        }

        tourView.showLoading(true, "");

        DetectTrackBean tmpDetectTrackBean = detectTrackBean.clone();
        tmpDetectTrackBean.setEnable(iSwitch);
        dataSource.setDetectTrack(tmpDetectTrackBean,new TourDataSource.TourCallback() {
            @Override
            public void onSuccess(@Nullable Object object) {
                detectTrackBean.setEnable(iSwitch);
                if (tourView != null ) {
                    tourView.onSaveDetectTrack(true);
                    tourView.onUpdateDetectTrack(detectTrackBean.getEnable() == SDKCONST.Switch.Open,
                            detectTrackBean.getSensitivity());
                }
            }

            @Override
            public void onError(Message msg, MsgContent ex, String extraStr) {
                if (tourView != null) {
                    tourView.onFailed(msg,ex,extraStr);
                    tourView.onUpdateDetectTrack(detectTrackBean.getEnable() == SDKCONST.Switch.Open,
                            detectTrackBean.getSensitivity());
                }
            }
        });
    }

    @Override
    public void setSensitivity(int sensitivity) {
        if (detectTrackBean == null) {
            return;
        }

        tourView.showLoading(true, "");

        DetectTrackBean tmpDetectTrackBean = detectTrackBean.clone();
        tmpDetectTrackBean.setSensitivity(sensitivity);
        dataSource.setDetectTrack(tmpDetectTrackBean,new TourDataSource.TourCallback() {
            @Override
            public void onSuccess(@Nullable Object object) {
                detectTrackBean.setSensitivity(sensitivity);
                if (tourView != null ) {
                    tourView.onSaveDetectTrack(true);
                    tourView.onUpdateDetectTrack(detectTrackBean.getEnable() == SDKCONST.Switch.Open,
                            detectTrackBean.getSensitivity());
                }
            }

            @Override
            public void onError(Message msg, MsgContent ex, String extraStr) {
                if (tourView != null) {
                    tourView.onFailed(msg,ex,extraStr);
                    tourView.onUpdateDetectTrack(detectTrackBean.getEnable() == SDKCONST.Switch.Open,
                            detectTrackBean.getSensitivity());
                }
            }
        });
    }

    //简单的统一错误处理
    private void handleCommonError(Message msg, MsgContent ex, String extraStr) {
        if (tourView.isActive()) {
            tourView.dismissLoading();
            tourView.onFailed(msg, ex, extraStr);
        }
    }


    private abstract class SimpleTourCallback<T> implements TourDataSource.TourCallback<T> {
        @Override
        public void onError(Message msg, MsgContent ex, String extraStr) {
            handleCommonError(msg, ex, extraStr);
        }
    }

}
