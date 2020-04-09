package com.smartism.znzk.adapter.recycleradapter;


import java.io.Serializable;

/**
 * 用于封装不同的数据
 * Created by Administrator on 2017/5/10.
 */

public class RecyclerItemBean<T> implements Serializable {
    private T t; //数据
    private int type; //设定RecyclerView的Type 用于多布局显示
    private boolean flag;//是否选中
    public RecyclerItemBean(T t, int type) {
        this.t = t;
        this.type = type;
    }

    public boolean isFlag() {
        return flag;
    }

    public void setFlag(boolean flag) {
        this.flag = flag;
    }

    public RecyclerItemBean(T t, int type, boolean flag) {
        this.t = t;
        this.type = type;
        this.flag = flag;
    }

    public T getT() {
        return t;
    }

    public void setT(T t) {
        this.t = t;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }
}
