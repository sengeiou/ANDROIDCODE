package com.smartism.znzk.activity.camera;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.RelativeLayout;
import android.widget.VideoView;

import com.smartism.znzk.R;
import com.smartism.znzk.db.camera.Contact;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class VideoSurfaceActivity extends AppCompatActivity {
    VideoView videoView;
    ImageView img;
    RelativeLayout video_img;
    ImageView back_btn;
    String path;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_surface);
        videoView = (VideoView) findViewById(R.id.videoView);
        img = (ImageView) findViewById(R.id.img);
        video_img = (RelativeLayout) findViewById(R.id.video_img);
        back_btn = (ImageView) findViewById(R.id.go_back);
        Contact contact = (Contact) getIntent().getSerializableExtra("contact");
        String name = getIntent().getStringExtra("name");
        String fileAbsolutePath = ApMonitorActivity.getCameraPath(this) + contact.contactId;
        path = fileAbsolutePath + "/" + name;
        Log.e("VideoSurfaceActivity","path:"+fileAbsolutePath);
        //给SurfaceView添加CallBack监听
        /**
         * VideoView控制视频播放的功能相对较少，具体而言，它只有start和pause方法。为了提供更多的控制，
         * 可以实例化一个MediaController，并通过setMediaController方法把它设置为VideoView的控制器。
         */

        final MediaController controller = new MediaController(this);
        controller.setVisibility(View.VISIBLE);
//        videoView.setMediaController(controller);
////        Uri videoUri = Uri.parse(fileAbsolutePath + "/" + name);
//        Log.e("VideoSurfaceActivity","path:"+fileAbsolutePath+fileAbsolutePath + "/" + name);
//        videoView.setVideoPath(fileAbsolutePath + "/" + name);


        File video = new File(path);
        if(video.exists()) {
            Log.e("VideoSurfaceActivity","path:"+path);
            videoView.setVideoPath(video.getAbsolutePath());
            Log.e("VideoSurfaceActivity","getAbsolutePath:"+video.getAbsolutePath());
            // 设置videoView与mController建立关联
            videoView.setMediaController(controller);
            // 设置mController与videoView建立关联
            controller.setMediaPlayer(videoView);
            // 让VideoView获取焦点
            controller.requestFocus();
        }
            videoView.setVisibility(View.GONE);


        img.setImageResource(R.drawable.icon_record);
        videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                video_img.setVisibility(View.VISIBLE);
                videoView.setVisibility(View.GONE);
            }
        });
        img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                video_img.setVisibility(View.GONE);
                videoView.setVisibility(View.VISIBLE);
                videoView.start();
            }
        });
        back_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }



    //获取视屏的第一帧，有些手机不显示....
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
