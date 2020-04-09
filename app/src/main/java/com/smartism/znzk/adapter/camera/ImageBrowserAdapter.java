package com.smartism.znzk.adapter.camera;

import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.smartism.znzk.R;
import com.smartism.znzk.activity.camera.ImageSeeActivity;
import com.smartism.znzk.db.camera.Contact;
import com.smartism.znzk.util.camera.ImageUtils;
import com.smartism.znzk.util.camera.Utils;
import com.smartism.znzk.widget.NormalDialog;

import java.io.File;
import java.io.FileFilter;
import java.util.List;


public class ImageBrowserAdapter extends BaseAdapter {
	File[] data;
	Context context;
	private String device;
	public static final int IMAGE_WIDTH = 100;
	public static final int IMAGE_HEIGHT = 500;
	private Contact contact;
	List<String> pictrues;
	public ImageBrowserAdapter(Context context, String device , Contact contact) {
		this.context = context;
		this.contact = contact;
		this.device = device;
		pictrues = Utils.getScreenShotImagePath(contact.contactId, 1);
		String path = null;
		if(device!=null&&device.equals("v380")){
			path = Environment.getExternalStorageDirectory().getPath()
					+ "/v380";
		}else{
			path = Environment.getExternalStorageDirectory().getPath()
				+ "/screenshot";
		}
		
		File file = new File(path);
		FileFilter filter = new FileFilter() {

			@Override
			public boolean accept(File pathname) {
				// TODO Auto-generated method stub
				if (pictrues!=null){
					for (String path: pictrues){
						Log.e("wwwwwwwsssssssssssss",path+"----"+pathname);
						if (pathname.equals(path)) return true;
					}
					return false;
				}else {
					if (pathname.getName().endsWith(".jpg")) {
						return true;
					} else {
						return false;
					}
				}


			}
		};
		data = file.listFiles(filter);
		if (null == data) {
			data = new File[0];
		}
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return data.length;
	}

	@Override
	public Object getItem(int arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long getItemId(int arg0) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public View getView(final int arg0, View arg1, ViewGroup arg2) {
		// TODO Auto-generated method stub
		RelativeLayout view = (RelativeLayout) arg1;
		if (null == view) {
			view = (RelativeLayout) LayoutInflater.from(context).inflate(
					R.layout.list_imgbrowser_item, null);

		}
		String path = data[arg0].getPath();
		ImageView img = (ImageView) view.findViewById(R.id.img);
		img.setImageBitmap(ImageUtils.getBitmap(path, IMAGE_WIDTH,
				IMAGE_HEIGHT));
		// TextView text = (TextView) view.findViewById(R.id.text);
		// text.setText(fileName);

		view.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View view) {
				// TODO Auto-generated method stub
				// ((ImageBrowser)context).createGalleryDialog(arg0);
				Intent intent = new Intent();
				intent.setClass(context, ImageSeeActivity.class);
				intent.putExtra("contact", contact);
				intent.putExtra("position", arg0);
				intent.putExtra("device", device);
				context.startActivity(intent);
			}

		});

		view.setOnLongClickListener(new OnLongClickListener() {

			@Override
			public boolean onLongClick(View view) {
				Log.e("adpter", "setOnLongClickListener");
				NormalDialog dialog = new NormalDialog(context, context
						.getResources().getString(R.string.delete), context
						.getResources().getString(R.string.confirm_delete),
						context.getResources().getString(R.string.delete),
						context.getResources().getString(R.string.cancel));
				dialog.setOnButtonOkListener(new NormalDialog.OnButtonOkListener() {

					@Override
					public void onClick() {
						// TODO Auto-generated method stub
						File f = data[arg0];
						try {
							f.delete();
							updateData();
						} catch (Exception e) {
							Log.e("my",
									"delete file error->ImageBrowserAdapter.java");
						}
					}
				});
				dialog.showDialog();
				Log.e("adpter", "showDialog");
				return true;
			}

		});
		Log.e("my", Runtime.getRuntime().totalMemory() + "");
		return view;
	}

	public void updateData() {
		String path = null;
		if(device!=null&&device.equals("v380")){
			path = Environment.getExternalStorageDirectory().getPath()
					+ "/v380";
		}else{
			path = Environment.getExternalStorageDirectory().getPath()
				+ "/screenshot";
		}
		File file = new File(path);
		FileFilter filter = new FileFilter() {

			@Override
			public boolean accept(File pathname) {
				// TODO Auto-generated method stub
				if (pathname.getName().endsWith(".jpg")) {
					return true;
				} else {
					return false;
				}

			}
		};
		File[] files = file.listFiles(filter);
		data = files;
		notifyDataSetChanged();
		// ((ImageBrowser)context).updateGalleryData(files);
	}
}
