package com.smartism.znzk.camera.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.smartism.znzk.R;
import com.smartism.znzk.adapter.recycleradapter.BaseRecyslerAdapter;
import com.smartism.znzk.adapter.recycleradapter.RecyclerItemBean;
import com.smartism.znzk.util.camera.ImageUtils;

import java.util.List;

/**
 * Created by Administrator on 2017/5/18.
 */

public class ImageListAdapter extends BaseRecyslerAdapter {
    public static final int IMAGE_WIDTH = 100;
    public static final int IMAGE_HEIGHT = 500;

    public ImageListAdapter(List<RecyclerItemBean> list) {
        super(list);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = null;
        if (viewType==0){
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.horizontal_list_item, parent, false);
        }else {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.image_list_item, parent, false);
        }
        ImageViewHoder imageViewHoder = new ImageViewHoder(view);
        return imageViewHoder;
    }


    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ImageViewHoder imageViewHoder = (ImageViewHoder) holder;
        String path = (String) list.get(position).getT();
        imageViewHoder.mImage.setImageBitmap(ImageUtils.getBitmap(path,IMAGE_WIDTH,IMAGE_HEIGHT));
    }



    class ImageViewHoder extends BaseViewHolder{
        private ImageView mImage;

        @Override
        public void setValue(RecyclerItemBean itemBean) {

        }

        public ImageViewHoder(View view) {
            super(view);
            mImage = (ImageView) view.findViewById(R.id.img_list_item);
        }
    }
}
