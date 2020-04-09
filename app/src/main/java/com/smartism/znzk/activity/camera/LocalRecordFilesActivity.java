package com.smartism.znzk.activity.camera;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.LruCache;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.smartism.znzk.R;
import com.smartism.znzk.db.camera.Contact;
import com.smartism.znzk.global.Constants;
import com.smartism.znzk.view.alertview.AlertView;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class LocalRecordFilesActivity extends AppCompatActivity {
    List<LocalFile> fileList;
    GridView local_list;
    RecordAdapter adapter;
    ImageView back_btn;
    Contact contact;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_local_recordfiles);
        local_list = (GridView) findViewById(R.id.local_list);
        back_btn = (ImageView) findViewById(R.id.back_btn);
        contact = (Contact) getIntent().getSerializableExtra("contact");
        String fileAbsolutePath = ApMonitorActivity.getCameraPath(this) + contact.contactId;
        fileList = GetVideoFileName(fileAbsolutePath);
        if (fileList == null) {
            fileList = new ArrayList<>();
        }
        adapter = new RecordAdapter(fileList);
        local_list.setAdapter(adapter);
        back_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent monitor = new Intent();
                monitor.setClass(LocalRecordFilesActivity.this, ApMonitorActivity.class);
                monitor.putExtra("contact", contact);
                monitor.putExtra("flag", getIntent().getBooleanExtra("flag", false));
                monitor.putExtra("connectType", Constants.ConnectType.P2PCONNECT);
                startActivity(monitor);
                finish();
            }
        });
        local_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent();
                intent.putExtra("contact", contact);
                intent.putExtra("name", fileList.get(position).getName());
                intent.setClass(LocalRecordFilesActivity.this, VideoSurfaceActivity.class);
                startActivity(intent);
            }
        });

        local_list.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                new AlertView(getString(R.string.activity_scene_del),
                        getString(R.string.activity_localrecord_del), getString(R.string.cancel),
                        new String[]{getString(R.string.sure)}, null, LocalRecordFilesActivity.this,
                        AlertView.Style.Alert, new com.smartism.znzk.view.alertview.OnItemClickListener() {

                    @Override
                    public void onItemClick(Object o, int position) {
                        if (position != -1) {
                            if (deleteFile(fileList.get(position).getImg())) {
                                fileList.remove(position);
                                adapter.notifyDataSetChanged();
                            }
                        }
                    }
                }).show();

                return true;
            }
        });
    }

    /**
     * 删除单个文件
     *
     * @param fileName 要删除的文件的文件名
     * @return 单个文件删除成功返回true，否则返回false
     */
    public boolean deleteFile(String fileName) {
        File file = new File(fileName);
        // 如果文件路径所对应的文件存在，并且是一个文件，则直接删除
        if (file.exists() && file.isFile()) {
            if (file.delete()) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    class RecordAdapter extends BaseAdapter {
        private List<LocalFile> fileName;
        private LayoutInflater layoutInflater;
        private LruCache<String, BitmapDrawable> mImageCache;

        public RecordAdapter(List<LocalFile> fileName) {
            this.fileName = fileName;
            layoutInflater = LayoutInflater.from(LocalRecordFilesActivity.this);
//            int maxCache = (int) Runtime.getRuntime().maxMemory();
//            int cacheSize = maxCache / 8;
//            mImageCache = new LruCache<String, BitmapDrawable>(cacheSize) {
//                @Override
//                protected int sizeOf(String key, BitmapDrawable value) {
//                    return value.getBitmap().getByteCount();
//                }
//            };
        }

        @Override
        public int getCount() {
            return fileName.size();

        }

        @Override
        public Object getItem(int position) {
            return fileName.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                convertView = layoutInflater.inflate(R.layout.item_local_recordfile, null, false);
                holder = new ViewHolder(convertView, fileName.get(position));
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            holder.setValue(fileName.get(position));
            return convertView;
        }

        class ViewHolder {
            ImageView img;
            TextView name;

            public ViewHolder(View view, LocalFile file) {
                img = (ImageView) view.findViewById(R.id.local_record);
                name = (TextView) view.findViewById(R.id.local_name);
                img.setTag(file.getImg());
            }

            public void setValue(LocalFile file) {
//                if (file.getImg() != null || !"".equals(file.getImg())) {
//                    if (mImageCache.get(file.getImg()) != null) {
//                        img.setImageDrawable(mImageCache.get(file.getImg()));
//                    } else {
//                        ImageTask it = new ImageTask();
//                        it.execute(file.getImg());
//                    }
//                } else {
//                    img.setImageResource(R.drawable.icon_record);
//                }
                img.setImageResource(R.drawable.icon_record);
                name.setText(file.getName().replace(".mp4",""));
            }
        }

        class ImageTask extends AsyncTask<String, Void, BitmapDrawable> {

            private String imageUrl;

            @Override
            protected BitmapDrawable doInBackground(String... params) {
                imageUrl = params[0];
                Bitmap bitmap = downloadImage(imageUrl);
                BitmapDrawable db = new BitmapDrawable(local_list.getResources(),bitmap);
                // 如果本地还没缓存该图片，就缓存
                if (mImageCache.get(imageUrl) == null) {
                    mImageCache.put(imageUrl, db);
                }
                return db;
            }

            @Override
            protected void onPostExecute(BitmapDrawable result) {
                // 通过Tag找到我们需要的ImageView，如果该ImageView所在的item已被移出页面，就会直接返回null
                ImageView iv = (ImageView) local_list.findViewWithTag(imageUrl);
                if (iv != null && result != null) {
                    iv.setImageDrawable(result);
                }
            }

            /**
             * 根据url从网络上下载图片
             *
             * @return
             */
            private Bitmap downloadImage(String url) {
                Bitmap bitmap = null;
                try {
                    bitmap = createVideoThumbnail(url);
                } catch (Exception e) {
                }
                return bitmap==null?null:zoomImage(bitmap,200,80);
            }

        }
        /***
         * 图片的缩放方法
         *
         * @param bgimage
         *            ：源图片资源
         * @param newWidth
         *            ：缩放后宽度
         * @param newHeight
         *            ：缩放后高度
         * @return
         */
        public  Bitmap zoomImage(Bitmap bgimage, double newWidth,
                                       double newHeight) {
            // 获取这个图片的宽和高
            float width = bgimage.getWidth();
            float height = bgimage.getHeight();
            // 创建操作图片用的matrix对象
            Matrix matrix = new Matrix();
            // 计算宽高缩放率
            float scaleWidth = ((float) newWidth) / width;
            float scaleHeight = ((float) newHeight) / height;
            // 缩放图片动作
            matrix.postScale(scaleWidth, scaleHeight);
            Bitmap bitmap = Bitmap.createBitmap(bgimage, 0, 0, (int) width,
                    (int) height, matrix, true);
            return bitmap;
        }

    }

    class LocalFile {
        String img;
        String name;

        public LocalFile(String img, String name) {
            this.img = img;
            this.name = name;
        }

        public String getImg() {
            return img;
        }

        public void setImg(String img) {
            this.img = img;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    // 获取当前目录下所有的mp4文件
    public List<LocalFile> GetVideoFileName(String fileAbsolutePath) {
        List<LocalFile> vecFile = new ArrayList<>();
        File file = new File(fileAbsolutePath);
        File[] subFile = file.listFiles();
        if (subFile == null) return new ArrayList<>();
        for (int iFileLength = 0; iFileLength < subFile.length; iFileLength++) {
            // 判断是否为文件夹
            if (!subFile[iFileLength].isDirectory()) {
                String filename = subFile[iFileLength].getName();
                // 判断是否为MP4结尾
                if (filename.trim().toLowerCase().endsWith(".mp4")) {
                    vecFile.add(new LocalFile(fileAbsolutePath + "/" + filename, filename));
                    Log.e("LocalRecordFilesActivity", "filename" + filename);
                }
            }
        }
        return vecFile;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) { //监控/拦截/屏蔽返回键
//            Intent monitor = new Intent();
//            monitor.setClass(this, ApMonitorActivity.class);
//            monitor.putExtra("contact", contact);
//            monitor.putExtra("flag", getIntent().getBooleanExtra("flag", false));
//            monitor.putExtra("connectType", Constants.ConnectType.P2PCONNECT);
//            startActivity(monitor);
            finish();
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_MENU) {
            //监控/拦截菜单键
        } else if (keyCode == KeyEvent.KEYCODE_HOME) {
            //由于Home键为系统键，此处不能捕获，需要重写onAttachedToWindow()
        }
        return super.onKeyDown(keyCode, event);
    }

    public static Bitmap createVideoThumbnail(String filePath) {
        // MediaMetadataRetriever is available on API Level 8
        // but is hidden until API Level 10
        Class<?> clazz = null;
        Object instance = null;
        try {
            clazz = Class.forName("android.media.MediaMetadataRetriever");
            instance = clazz.newInstance();

            Method method = clazz.getMethod("setDataSource", String.class);
            method.invoke(instance, filePath);

            // The method name changes between API Level 9 and 10.
            if (Build.VERSION.SDK_INT <= 9) {
                return (Bitmap) clazz.getMethod("captureFrame").invoke(instance);
            } else {
                byte[] data = (byte[]) clazz.getMethod("getEmbeddedPicture").invoke(instance);
                if (data != null) {
                    Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
                    if (bitmap != null) return bitmap;
                }
                return (Bitmap) clazz.getMethod("getFrameAtTime").invoke(instance);
            }
        } catch (IllegalArgumentException ex) {
            // Assume this is a corrupt video file
        } catch (RuntimeException ex) {
            // Assume this is a corrupt video file.
        } catch (InstantiationException e) {
        } catch (InvocationTargetException e) {
        } catch (ClassNotFoundException e) {
        } catch (NoSuchMethodException e) {
        } catch (IllegalAccessException e) {
        } finally {
            try {
                if (instance != null) {
                    clazz.getMethod("release").invoke(instance);
                }
            } catch (Exception ignored) {
            }
        }
        return null;
    }
}
