package com.smartism.znzk.xiongmai.lib.funsdk.support;



import com.smartism.znzk.xiongmai.lib.funsdk.support.models.FunDevRecordFile;

import java.util.List;

public interface OnFunDeviceRecordListener extends OnFunListener {

    void onRequestRecordListSuccess(List<FunDevRecordFile> files);

    void onRequestRecordListFailed(final Integer errCode);

}
