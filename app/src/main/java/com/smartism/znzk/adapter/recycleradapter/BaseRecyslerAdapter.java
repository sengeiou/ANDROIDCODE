package com.smartism.znzk.adapter.recycleradapter;

import android.support.v7.widget.RecyclerView;
import android.view.View;

import java.util.List;

/**
 * Created by Administrator on 2017/5/18.
 */

public abstract class BaseRecyslerAdapter extends RecyclerView.Adapter {
    private RecyclerItemClickListener recyclerItemClickListener = null;    //点击事件
    private RecyclerItemLongClickListener recyclerItemLongClickListener = null;    //长按事件
    private RecyclerHeaderListener recyclerHeaderListener = null;    //长按事件
    private RecyclerFootListener recyclerFootListener = null;    //长按事件
    public List<RecyclerItemBean> list; //数据源
    public static final int RECYCLER_HEADERVIEW = 9999;//带有头部
    public static final int RECYCLER_FOOTVIEW = 9998;//带有底部
    private int listSize = 0;
    private View HeaderView;
    private View FootView;

    public int getListSize() {
        return listSize;
    }

    public void setListSize(int listSize) {
        if (listSize<=0){
            this.listSize = list.size();
        }else {
            this.listSize = listSize;
        }
    }

    @Override
    public int getItemCount() {
        int size = list.size();
        if (listSize > 0 && size > listSize) size = listSize;
        if (HeaderView == null && FootView == null) {
            return size;
        } else if (HeaderView == null && FootView != null) {
            return size + 1;
        } else if (HeaderView != null && FootView == null) {
            return size + 1;
        } else {
            return size + 2;
        }
    }
    public BaseRecyslerAdapter(List<RecyclerItemBean> list) {
        this.list = list;
    }

    @Override
    public int getItemViewType(int position) {

        if (HeaderView != null && position == 0) {
            //第一个item应该加载Header
            return RECYCLER_HEADERVIEW;
        }
        if (FootView != null && position == getItemCount() - 1) {
            //最后一个,应该加载Footer
            return RECYCLER_FOOTVIEW;
        }

        return list.get(position).getType();
    }

    public abstract class BaseViewHolder extends RecyclerView.ViewHolder {
        private boolean ItemClickable = true;       //RecyclerView的点击事件默认可点击
        private boolean ItemLongClickable = true;   //RecyclerView的长按事件默认可点击

        public abstract void setValue(RecyclerItemBean itemBean);

        public BaseViewHolder(View view) {
            super(view);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //头部点击
                    if (HeaderView != null && getAdapterPosition() == 0) {
                        if (recyclerHeaderListener == null) return;
                        recyclerHeaderListener.onRecycleheaderClick(v);
                        return;
                    }

                    //底部点击
                    if (FootView != null && getAdapterPosition() == getItemCount() - 1) {
                        if (recyclerFootListener == null) return;
                        recyclerFootListener.onRecyclefootClick(v);
                        return;
                    }
                    if (recyclerItemClickListener != null) {
                        if (ItemClickable) {
                            if (recyclerHeaderListener != null) {
                                recyclerItemClickListener.onRecycleItemClick(v, getAdapterPosition() - 1);
                            } else {
                                recyclerItemClickListener.onRecycleItemClick(v, getAdapterPosition());
                            }
                        }
                    }
                }
            });
            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if (recyclerItemLongClickListener != null) {
                        if (ItemLongClickable) {
                            return recyclerItemLongClickListener.onRecycleItemLongClick(v, getAdapterPosition());
                        }
                    }
                    return true;
                }
            });
        }

        public void setItemClickable(boolean itemClickable) {
            ItemClickable = itemClickable;
        }

        public void setItemLongClickable(boolean itemLongClickable) {
            ItemLongClickable = itemLongClickable;
        }
    }


    public void setRecyclerItemClickListener(RecyclerItemClickListener recyclerItemClickListener) {
        this.recyclerItemClickListener = recyclerItemClickListener;
    }

    public void setRecyclerItemLongClickListener(RecyclerItemLongClickListener recyclerItemLongClickListener) {
        this.recyclerItemLongClickListener = recyclerItemLongClickListener;
    }

    /**
     * RecyclerView的点击事件
     */
    public interface RecyclerItemClickListener {
        public void onRecycleItemClick(View view, int position);
    }

    /**
     * RecyclerView的长按事件
     */
    public interface RecyclerItemLongClickListener {

        public boolean onRecycleItemLongClick(View view, int position);
    }

    /**
     * RecyclerView的点击事件
     */
    public interface RecyclerHeaderListener {
        public void onRecycleheaderClick(View view);
    }

    /**
     * FootView的点击事件
     */
    public interface RecyclerFootListener {
        public void onRecyclefootClick(View view);
    }

    public View initFootOrHead(int viewType) {
        if (HeaderView != null && viewType == RECYCLER_HEADERVIEW) {
            return HeaderView;
        }
        if (FootView != null && viewType == RECYCLER_FOOTVIEW) {
            return FootView;
        }
        return null;
    }

    public boolean initFootOrHeadForBind(int position) {
        if ((HeaderView != null && position == 0) || (FootView != null && position == getItemCount() - 1)) {
            return true;
        }
        return false;
    }

    public View getHeaderView() {
        return HeaderView;
    }

    public void setHeaderView(View headerView) {
        HeaderView = headerView;
        notifyItemInserted(0);
    }

    public View getFootView() {
        return FootView;
    }

    public void setFootView(View footView) {
        FootView = footView;
        notifyItemInserted(getItemCount() - 1);
    }

    /**
     * 头部&底部
     */

    public class HFViewHolder extends BaseViewHolder {

        public HFViewHolder(View view) {
            super(view);
        }

        @Override
        public void setValue(RecyclerItemBean itemBean) {

        }
    }

    public RecyclerHeaderListener getRecyclerHeaderListener() {
        return recyclerHeaderListener;
    }

    public void setRecyclerHeaderListener(RecyclerHeaderListener recyclerHeaderListener) {
        this.recyclerHeaderListener = recyclerHeaderListener;
    }

    public RecyclerFootListener getRecyclerFootListener() {
        return recyclerFootListener;
    }

    public void setRecyclerFootListener(RecyclerFootListener recyclerFootListener) {
        this.recyclerFootListener = recyclerFootListener;
    }
}
