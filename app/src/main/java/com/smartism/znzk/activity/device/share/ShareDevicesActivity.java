package com.smartism.znzk.activity.device.share;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.amazonaws.amplify.generated.graphql.CreateCtrUserDeviceShareMutation;
import com.amazonaws.mobile.client.AWSMobileClient;
import com.apollographql.apollo.GraphQLCall;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;
import com.smartism.znzk.R;
import com.smartism.znzk.activity.ActivityParentActivity;
import com.smartism.znzk.awsClient.AWSClients;
import com.smartism.znzk.util.Util;
import com.smartism.znzk.util.WeakRefHandler;

import java.util.UUID;

import javax.annotation.Nonnull;

import type.CreateCtrUserDeviceShareInput;

public class ShareDevicesActivity extends ActivityParentActivity {
    private String key;
    private TextView shareTitle;
    private ImageView share_qr_img;
    private String sharekey;

    private Handler.Callback mCallback = new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    cancelInProgress();
                    shareTitle.setText(getString(R.string.share_activity_title));
                    share_qr_img.setImageBitmap(Util.createImage("ctrshare"+key, Util.dip2px(getApplicationContext(), 250), Util.dip2px(getApplicationContext(), 250)));
                    break;
                case -2:
                    cancelInProgress();
                    shareTitle.setText(getString(R.string.share_activity_title_error));
                    break;
                case -1:
                    cancelInProgress();
                    shareTitle.setText(getString(R.string.operator_error));
                    break;
                default:
                    break;
            }
            return false;
        }
    };
    private Handler defHandler = new WeakRefHandler(mCallback);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share);
        shareTitle = (TextView) findViewById(R.id.share_title);
        share_qr_img = (ImageView) findViewById(R.id.share_qr);
        sharekey = getIntent().getStringExtra("sharekey");
    }

    @Override
    protected void onResume() {
        super.onResume();
        createQRImg();
    }

    public void back(View v) {
        finish();
    }

    private GraphQLCall.Callback<CreateCtrUserDeviceShareMutation.Data> mutationCallback = new GraphQLCall.Callback<CreateCtrUserDeviceShareMutation.Data>() {
        @Override
        public void onResponse(@Nonnull Response<CreateCtrUserDeviceShareMutation.Data> response) {
            Log.i(TAG,"Results GraphQL Add:" + JSON.toJSONString(response));
            if (!response.hasErrors()){
                defHandler.sendEmptyMessage(1);
            }else {
                defHandler.sendEmptyMessage(-1);
            }
        }

        @Override
        public void onFailure(@Nonnull ApolloException e) {
            Log.e("Error", e.toString());
            defHandler.sendEmptyMessage(-2);
        }
    };

    /**
     * 获取需要生成二维码的字符
     */
    public void createQRImg() {
        showInProgress(getString(R.string.share_activity_ongoing), false, false);

        key = UUID.randomUUID().toString();
        CreateCtrUserDeviceShareInput createCtrUserDevicesShareInput = CreateCtrUserDeviceShareInput.builder()
                .key(key)
                .uid(AWSMobileClient.getInstance().getUsername())
                .mac(sharekey)
                .validityTime(((int)(System.currentTimeMillis()/1000)) + 300)
                .build();

        AWSClients.getInstance().getmAWSAppSyncClient().mutate(CreateCtrUserDeviceShareMutation.builder().input(createCtrUserDevicesShareInput).build())
                .enqueue(mutationCallback);
    }
}
