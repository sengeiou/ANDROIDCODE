package com.smartism.znzk.zhicheng.iviews;

import com.smartism.znzk.util.indexlistsort.SortModel;

import java.util.List;

public  interface ZCBrandDisplayInterface {
    //显示进度条
    void showProgress();
    //隐藏进度条
    void hideProgress();
    void showBrands(List<SortModel> models);
    void errorMsg();
    void successMsg();
}
