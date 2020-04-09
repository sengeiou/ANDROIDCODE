package com.smartism.znzk.awsClient;

import android.content.Context;
import android.util.Log;
import android.view.View;

import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobile.config.AWSConfiguration;
import com.amazonaws.mobileconnectors.appsync.AWSAppSyncClient;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.amazonaws.services.s3.AmazonS3Client;
import com.hjq.toast.ToastUtils;
import com.smartism.znzk.application.MainApplication;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

/**
 * AWS 客户端 单例
 * 2020年03月04日 王建
 */
public class AWSClients {
    private static final String TAG = MainApplication.TAG;
    private static volatile AWSClients _instance;
    private Context mContext;

    //aws 操作组件
    private AWSAppSyncClient mAWSAppSyncClient;

    public static AWSClients getInstance() {
        if (_instance == null) {
            synchronized (AWSClients.class) {
                if (_instance == null) {
                    _instance = new AWSClients();
                }
            }
        }
        return _instance;
    }

    public void init(Context context){
        try {
            mContext = context;
            mAWSAppSyncClient = AWSAppSyncClient.builder()
                    .context(context.getApplicationContext())
                    .awsConfiguration(new AWSConfiguration(context.getApplicationContext()))
                    .cognitoUserPoolsAuthProvider(() -> {
                        try {
                            return AWSMobileClient.getInstance().getTokens().getIdToken().getTokenString();
                        } catch (Exception e) {
                            Log.e(TAG, e.getLocalizedMessage());
                            return e.getLocalizedMessage();
                        }
                    })
                    // If you are using complex objects (S3) then uncomment
                    //.s3ObjectManager(new S3ObjectManagerImplementation(new AmazonS3Client(AWSMobileClient.getInstance())))
                    .build();
        }catch (Exception ex){
            Log.e(TAG,"init AppSyncClient Error",ex);
        }
    }

    /**
     * 保存信息到S3
     * @param parent
     * @param content
     * @return
     */
    public void saveStringToS3(String parent,String content,String namePrefix,TransferListener listener) {
        TransferUtility transferUtility =
                TransferUtility.builder()
                        .context(mContext.getApplicationContext())
                        .awsConfiguration(AWSMobileClient.getInstance().getConfiguration())
                        .s3Client(new AmazonS3Client(AWSMobileClient.getInstance()))
                        .build();

        String fileName = namePrefix + "_" +System.currentTimeMillis()+".txt";
        File file = new File(mContext.getApplicationContext().getFilesDir(), fileName);
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(file));
            writer.append(content);
            writer.close();
        }
        catch(Exception e) {
            Log.e(TAG, e.getMessage());
        }

        TransferObserver uploadObserver =
                transferUtility.upload(
                         parent + "/"+fileName,
                        new File(mContext.getApplicationContext().getFilesDir(),fileName));

        // Attach a listener to the observer to get state update and progress notifications
        uploadObserver.setTransferListener(listener);

        // If you prefer to poll for the data, instead of attaching a
        // listener, check for the state and progress in the observer.
        if (TransferState.COMPLETED == uploadObserver.getState()) {
            // Handle a completed upload.
        }

        Log.d(TAG, "Bytes Transferred: " + uploadObserver.getBytesTransferred());
        Log.d(TAG, "Bytes Total: " + uploadObserver.getBytesTotal());
    }

    public AWSAppSyncClient getmAWSAppSyncClient() {
        return mAWSAppSyncClient;
    }
}
