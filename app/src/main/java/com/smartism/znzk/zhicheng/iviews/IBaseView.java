package com.smartism.znzk.zhicheng.iviews;

public interface IBaseView {

    void showProgress(String text);
    void hideProgress();
    void error(String message);
    void success(String message);
}
