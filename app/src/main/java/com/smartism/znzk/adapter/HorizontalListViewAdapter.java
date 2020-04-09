package com.smartism.znzk.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.smartism.znzk.R;
import com.smartism.znzk.util.camera.ImageUtils;

import java.util.List;

public class HorizontalListViewAdapter extends BaseAdapter{
    private Context mContext;
    private LayoutInflater mInflater;
    Bitmap iconBitmap;
    private int selectIndex = -1;
    public static final int IMAGE_WIDTH = 100;
	public static final int IMAGE_HEIGHT = 500;
	private int number=0;
    List<String> picture;
    StartActivity sActivity = null;

    public void setStartActivity(StartActivity startActivity) {
        this.sActivity = startActivity;
    }

    public HorizontalListViewAdapter(Context context, List<String> picture){
        this.mContext = context;
        mInflater=(LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);//LayoutInflater.from(mContext);
       this.picture = picture;

    }
    public HorizontalListViewAdapter(Context context,List<String> picture,int number){
        this.mContext = context;
        mInflater=(LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);//LayoutInflater.from(mContext);
       this.picture = picture;
		this.number = number;
    }
    @Override
    public int getCount() {
        return picture.size();
    }
    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        ViewHolder holder;
        if(convertView==null){
            holder = new ViewHolder();
            convertView = mInflater.inflate(R.layout.horizontal_list_item, null);
            holder.mImage=(ImageView)convertView.findViewById(R.id.img_list_item);
            convertView.setTag(holder);
        }else{
            holder=(ViewHolder)convertView.getTag();
        }
        if(position == selectIndex){
            convertView.setSelected(true);
        }else{
            convertView.setSelected(false);
        }
        String path = picture.get(position);
        holder.mImage.setImageBitmap(ImageUtils.getBitmap(path,IMAGE_WIDTH,
        		IMAGE_HEIGHT));

        holder.mImage.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
                sActivity.startImageSeeActivity(number,picture.get(position));

//				if(number==1){
//					VPlayMonitor.getInstance().finish();
//				}else{
//					ApMonitorActivity.getInstance().finish();
//				}

			}
		});

        holder.mImage.setOnLongClickListener(new OnLongClickListener() {

			@Override
			public boolean onLongClick(View v) {
				// TODO Auto-generated method stub
                sActivity.delectFile(number,picture.get(position));
				return true;
			}
		});
        /*holder.mImage.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				switch (event.getAction()) {
				case MotionEvent.ACTION_DOWN:
					Intent in = new Intent();
					in.setClass(mContext, ImageSeeActivity.class);
					mContext.startActivity(in);
					ApMonitorActivity.getInstance().finish();
					return true;

				case MotionEvent.ACTION_CANCEL:
					return true;
				case MotionEvent.ACTION_MOVE:
					return true;
				}

				return false;
			}
		});*/
        return convertView;
    }
  public interface StartActivity{
      public void startImageSeeActivity(int number,String path);
      public void delectFile(int number,String path);
  }
    private static class ViewHolder {
        private ImageView mImage;
    }

    public void setSelectIndex(int i){
        selectIndex = i;
    }
}