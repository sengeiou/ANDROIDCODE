package com.smartism.znzk.view;


import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.PopupWindow;

import com.smartism.znzk.R;
import com.smartism.znzk.adapter.recycleradapter.BaseRecyslerAdapter;
import com.smartism.znzk.adapter.recycleradapter.RecyclerItemBean;
import com.smartism.znzk.adapter.scene.ChooseScenesAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * 显示新增智能场景的poputwindow
 *
 * @author ww
 *         2015年9月30日
 */
public class ScenePopupWindow extends PopupWindow {

    private View mView;
    private RecyclerView recycler;
    public List<RecyclerItemBean> itemBeans;
    public ChooseScenesAdapter mAdapter;

    public ScenePopupWindow(final Activity context, BaseRecyslerAdapter.RecyclerItemClickListener itemsOnClick) {
        super(context);
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mView = inflater.inflate(R.layout.main_choose_scenes, null);
        recycler = (RecyclerView) mView.findViewById(R.id.scene_recycle);
        itemBeans = new ArrayList<>();
        initRecycle(mView, itemsOnClick, context);
        DisplayMetrics metric = new DisplayMetrics();
        context.getWindowManager().getDefaultDisplay().getMetrics(metric);
        int width = metric.widthPixels;     // 屏幕宽度（像素）
        int height = metric.heightPixels*2/5;   // 屏幕高度（像素）

        //设置SelectPicPopupWindow的View
        this.setContentView(mView);
        //设置SelectPicPopupWindow弹出窗体的宽
        this.setWidth(width);
        //设置SelectPicPopupWindow弹出窗体的高
        this.setHeight(height);
        //设置SelectPicPopupWindow弹出窗体可点击
        this.setFocusable(true);

        //设置SelectPicPopupWindow弹出窗体动画效果
        this.setAnimationStyle(R.style.Right_menu_animation);
        //实例化一个ColorDrawable颜色为半透明
        ColorDrawable dw = new ColorDrawable(0000000000);
        //设置SelectPicPopupWindow弹出窗体的背景
        this.setBackgroundDrawable(dw);

        this.setOnDismissListener(new OnDismissListener() {
            @Override
            public void onDismiss() {
                WindowManager.LayoutParams params = context.getWindow().getAttributes();
                params.alpha = 1f;
                context.getWindow().setAttributes(params);
            }
        });
    }

    private void goneAllMenu() {
    }
    public void updateMenu(List<RecyclerItemBean> list) {
        updateSearchs(list);
    }

    public void initRecycle(View view, BaseRecyslerAdapter.RecyclerItemClickListener itemsOnClick, Context context) {
        if (itemBeans == null) {
            itemBeans = new ArrayList<>();
        }
        mAdapter = new ChooseScenesAdapter(itemBeans);
        mAdapter.setRecyclerItemClickListener(itemsOnClick);
        //创建默认线性LinearLayoutManager
        GridLayoutManager gridLayoutManager = new GridLayoutManager(context, 4, GridLayoutManager.VERTICAL, false);
//		GridLayoutManager gridLayoutManager = new GridLayoutManager(getActivity(),4);
        recycler.setLayoutManager(gridLayoutManager);  //设置布局管理器
        recycler.setItemAnimator(new DefaultItemAnimator()); //设置Item增加、移除动画
        recycler.setAdapter(mAdapter);
    }

    public void updateSearchs(List<RecyclerItemBean> list) {
        if (itemBeans == null) {
            itemBeans = new ArrayList<>();
        }
        itemBeans.clear();
        if (list!=null&&!list.isEmpty()){
            itemBeans.addAll(list);
        }
        if (mAdapter != null) {
            mAdapter.notifyDataSetChanged();
        }
    }

}
