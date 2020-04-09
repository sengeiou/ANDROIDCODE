package com.smartism.znzk.activity;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.http.SslError;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.webkit.JavascriptInterface;
import android.webkit.SslErrorHandler;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSONObject;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.hjq.toast.ToastUtils;
import com.just.agentweb.AbsAgentWebSettings;
import com.just.agentweb.AgentWeb;
import com.just.agentweb.IAgentWebSettings;
import com.just.agentweb.WebChromeClient;
import com.just.agentweb.WebViewClient;
import com.smartism.znzk.R;
import com.smartism.znzk.application.MainApplication;
import com.smartism.znzk.awsClient.AWSClients;
import com.smartism.znzk.util.Actions;
import com.smartism.znzk.util.ToastUtil;
import com.smartism.znzk.util.webviewimage.ImageUtil;
import com.smartism.znzk.util.webviewimage.PermissionUtil;
import com.smartism.znzk.view.alertview.AlertView;
import com.smartism.znzk.view.alertview.OnItemClickListener;
import com.smartism.znzk.webview.JsObjectInterface;

import java.util.ArrayList;
import java.util.List;


@SuppressLint("SetJavaScriptEnabled")
public class CommonWebViewActivity extends ActivityParentWebActivity implements View.OnClickListener{
	/***mProgressBar 进度条参数**/
    public ProgressBar mProgressBar;
	public boolean isAnimStart = false;
	public int currentProgress;
	/**end***/

	private AgentWeb mAgentWeb;
	private LinearLayout mAgentWebLayout;
	private TextView tv_title,iv_close;
	private String url;
	private static final int REQUEST_CODE_PICK_IMAGE = 0;
	private static final int REQUEST_CODE_IMAGE_CAPTURE = 1;
	private static final int P_CODE_PERMISSIONS = 101;
	private Intent mSourceIntent;
    private boolean resumed = false;
	private boolean needreload = false;

	private boolean needClearHistory = false; //是否需要清除历史记录
    private boolean firstCreateClearHistory = true; //首次创建 清空历史记录，有进入会重定向
    private boolean isTriggerWeiXinPay = false;//是否跳转了微信支付

	@Override
	protected void refreshPage() {
		mAgentWeb.getUrlLoader().reload();
	}

	@Override
	protected void paySuccessToPage(String payParam) {
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("method","paySuccess");
		mAgentWeb.getJsAccessEntrace().quickCallJs("postMessageHandler('"+jsonObject.toJSONString()+"')"); //支付成功
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_webview_common);
        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);
		initView();
	}

	private void initView() {
		tv_title = (TextView) findViewById(R.id.tv_title);
		iv_close = (TextView) findViewById(R.id.iv_close);
		iv_close.setOnClickListener(this);
		needreload = getIntent().getBooleanExtra("need_reload",false);
		tv_title.setText(getIntent().getStringExtra("title"));
		url = getIntent().getStringExtra("url");

		mAgentWebLayout = (LinearLayout) findViewById(R.id.web_view_layout);

		mAgentWeb = AgentWeb.with(this)
				.setAgentWebParent(mAgentWebLayout, new LinearLayout.LayoutParams(-1, -1))
				.useDefaultIndicator()
				.setAgentWebWebSettings(getSettings())//设置 IAgentWebSettings。
				.interceptUnkownUrl()
				.setWebChromeClient(mWebChromeClient)
				.setWebViewClient(mWebViewClient)
				.createAgentWeb()
				.ready()
				.go(url);

		mAgentWeb.getJsInterfaceHolder().addJavaObject(JS_OBJECT_NAME,new JsObjectInterface() {
			/*
			 * JS调用android的方法  显示toast
			 * @JavascriptInterface仍然必不可少
			 *
			 * */
			@JavascriptInterface
			public void  showToast(String content){
				cancelInProgress();
				ToastUtil.longMessage(content);
			}
			/*
			 * JS调用android的方法  显示toast和finish当前页
			 * @JavascriptInterface仍然必不可少
			 *
			 * */
			@JavascriptInterface
			public void  showToastAndFinish(String content){
				showToast(content);
				finish();
			}
			/*
			 * JS调用android的方法  显示toast和finish当前页
			 * @JavascriptInterface仍然必不可少
			 *
			 * */
			@JavascriptInterface
			public void  showAlertAndCloseSelfAndRefreshParent(String content){
				showToast(content);
				setResult(RESULT_NEEDREFRESH);
				finish();
			}
			/*
			 * JS调用android的方法  finish当前页
			 * @JavascriptInterface仍然必不可少
			 *
			 * */
			@JavascriptInterface
			public void  closeMyself(){
				finish();
			}
			/*
			 * JS调用android的方法  finish当前页 提示并刷新父窗体
			 * @JavascriptInterface仍然必不可少
			 *
			 * */
			@JavascriptInterface
			public void  closeSelfAndRefreshParent(){
				Activity parent = getParent();
				setResult(RESULT_NEEDREFRESH);
				finish();
			}
			/*
			 * JS调用android的方法  点击超链接打开新的activity
			 * @JavascriptInterface仍然必不可少
			 *
			 * */
			@JavascriptInterface
			public void  openNewViewWithGet(String url){
				Intent intent = new Intent();
				intent.setClass(getApplicationContext(), CommonWebViewActivity.class);
				intent.putExtra("url",url);
				intent.putExtra("show_save", false);
				startActivityForResult(intent,RESULT_REQUEST);
			}
			/*
			 * JS调用android的方法  点击超链接打开新的activity
			 * @JavascriptInterface仍然必不可少
			 *
			 * */
			@JavascriptInterface
			public void  openNewAddViewWithGet(String url){
				Intent intent = new Intent();
				intent.setClass(getApplicationContext(), CommonWebViewActivity.class);
				intent.putExtra("url",url);
				intent.putExtra("show_save", true);
				startActivityForResult(intent,RESULT_REQUEST);
			}

			/*
			 * JS调用android的方法  支持的原生支付
			 * @JavascriptInterface仍然必不可少
			 *
			 * */
			@JavascriptInterface
			public String supportPayType(){
//				return "['wxpay','alipay','paypal']";
				return "['wxpay','alipay']";
			}

			@JavascriptInterface
			public void pay(String result, String type) {
				Log.e("shopMain", "startpay:" + result);
				showInProgress();
				if (type.equals("wxpay")) {
					weixinPay(result);
				} else if (type.equals("alipay")) {
					alipay(result);
				} else if (type.equals("paypal")){
					payPal(result);
				}
			}

			@JavascriptInterface
			public void refreshAndClearHistory() {
				mAgentWeb.getUrlLoader().reload();
				needClearHistory = true;
			}

			@JavascriptInterface
			public void toUrlAndClearHistory(String url) {
				mAgentWeb.getUrlLoader().reload();
				needClearHistory = true;
			}

			@JavascriptInterface
			public void postMessage(String params) {
				JSONObject jsonObject = JSONObject.parseObject(params);
				switch (jsonObject.getString("method")) {
					case "supportPayType":
						JSONObject jsonPar = new JSONObject();
						jsonPar.put("pay",jsonObject.getString("params"));
						if ("wxpay".equals(jsonObject.getString("params"))){
							jsonPar.put("support",true);
						}else{
							jsonPar.put("support",false);
						}
						jsonObject.put("params",jsonPar);
						runOnUiThread(() -> {
							mAgentWeb.getWebCreator().getWebView().loadUrl("javascript:postMessageHandler('"+jsonObject.toJSONString()+"')");
						});
						break;
					case "pay":
						JSONObject payJson = jsonObject.getJSONObject("params");
						if ("wxpay".equals(payJson.getString("type"))){
							weixinPay(payJson.getString("payPar"));
						}else{
							ToastUtil.longMessage(getString(R.string.not_support));
						}
						break;
					case "showToast":
						showToast(jsonObject.getString("params"));
						break;
					case "showToastAndFinish":
						showToastAndFinish(jsonObject.getString("params"));
						break;
					case "showAlertAndCloseSelfAndRefreshParent":
						showAlertAndCloseSelfAndRefreshParent(jsonObject.getString("params"));
						break;
					case "closeMyself":
						finish();
						break;
					case "closeSelfAndRefreshParent":
						closeSelfAndRefreshParent();
						break;
					case "openNewViewWithGet":
						jsonPar = jsonObject.getJSONObject("params");
						if (jsonPar.getBoolean("showAdd")){
							openNewAddViewWithGet(jsonPar.getString("url"));
						}else{
							openNewViewWithGet(jsonPar.getString("url"));
						}
						if (jsonPar.getBoolean("closeP")){
							finish();
						}
						break;
					case "saveToS3":
						jsonPar = jsonObject.getJSONObject("params");
						showInProgress();
						AWSClients.getInstance().saveStringToS3(jsonPar.getString("parent"),jsonPar.getString("content"),jsonPar.getString("parent"),new TransferListener() {

							@Override
							public void onStateChanged(int id, TransferState state) {
								if (TransferState.COMPLETED == state) {
									// Handle a completed upload.
									runOnUiThread(()->{
										cancelInProgress();
										ToastUtils.show("Submit success");
										finish();
									});
								}
							}

							@Override
							public void onProgressChanged(int id, long bytesCurrent, long bytesTotal) {
								float percentDonef = ((float) bytesCurrent / (float) bytesTotal) * 100;
								int percentDone = (int)percentDonef;

								Log.d(TAG, "ID:" + id + " bytesCurrent: " + bytesCurrent
										+ " bytesTotal: " + bytesTotal + " " + percentDone + "%");
							}

							@Override
							public void onError(int id, Exception ex) {
								// Handle errors
								cancelInProgress();
								ToastUtils.show("Submit failed");
							}

						});
						break;
				}
			}
		});
	}

	private WebViewClient mWebViewClient = new WebViewClient() {
		@Override
		public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
			return super.shouldOverrideUrlLoading(view, request);
		}

		@Override
		public void onPageStarted(WebView view, String url, Bitmap favicon) {
			super.onPageStarted(view, url, favicon);
			mProgressBar.setVisibility(View.VISIBLE);
			mProgressBar.setAlpha(1.0f);
		}

		@Override
		public void onPageFinished(WebView view, String url) {
			super.onPageFinished(view, url);
			tv_title.setText(view.getTitle());
			if (firstCreateClearHistory || needClearHistory){
				view.clearHistory();
				firstCreateClearHistory = false;
				needClearHistory = false;
			}
		}

		@Override
		public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
			new AlertView(getString(R.string.tips), getString(R.string.notification_error_ssl_cert_invalid), getString(R.string.cancel),
					new String[]{getString(R.string.sure)}, null, CommonWebViewActivity.this, AlertView.Style.Alert, new OnItemClickListener() {

				@Override
				public void onItemClick(Object o, int position) {
					if (position != -1) {
						handler.proceed();
					}else {
						handler.cancel();
					}
				}
			}).show();
		}
	};
	private WebChromeClient mWebChromeClient = new WebChromeClient() {
		@Override
		public void onReceivedTitle(WebView view, String title) {
			super.onReceivedTitle(view, title);
			if (tv_title != null) {
				tv_title.setText(title);
			}
		}

		//		@Override
//		public void onProgressChanged(WebView view, int newProgress) {
//			super.onProgressChanged(view, newProgress);
//			currentProgress = mProgressBar.getProgress();
//			if (newProgress >= 100 && !isAnimStart) {
//				// 防止调用多次动画
//				isAnimStart = true;
//				mProgressBar.setProgress(newProgress);
//				// 开启属性动画让进度条平滑消失
//				startDismissAnimation(mProgressBar.getProgress());
//			} else {
//				// 开启属性动画让进度条平滑递增
//				startProgressAnimation(newProgress);
//			}
//		}
	};

	/**
	 * @return IAgentWebSettings
	 */
	public IAgentWebSettings getSettings() {
		return new AbsAgentWebSettings() {
			private AgentWeb mAgentWeb;

			@Override
			protected void bindAgentWebSupport(AgentWeb agentWeb) {
				this.mAgentWeb = agentWeb;
			}

			@Override
			public WebSettings getWebSettings() {
				WebSettings settings = super.getWebSettings();
				settings.setUseWideViewPort(true);
				settings.setLoadWithOverviewMode(true);
				return settings;
			}
		};
	}

    @Override
    protected void onResume() {
        super.onResume();
		mAgentWeb.getWebLifeCycle().onResume();
        if (resumed && needreload){
			mAgentWeb.getUrlLoader().reload();
        }
        resumed = true;
        if (isTriggerWeiXinPay){ //为什么要这样做。通过startActivityForResult启动微信支付没有看到activityResult回调。
            isTriggerWeiXinPay = false;
            if (Actions.VersionType.CHANNEL_AIERFUDE.equals(MainApplication.app.getAppGlobalConfig().getVersion())) {
                mAgentWeb.getUrlLoader().loadUrl("http://appshop.efud110.com/order/list");
                mAgentWeb.getWebCreator().getWebView().clearHistory();
            }
        }
    }

	@Override
	protected void onPause() {
		mAgentWeb.getWebLifeCycle().onPause();
		super.onPause();

	}

	@Override
	protected void onDestroy() {
		mAgentWeb.getWebLifeCycle().onDestroy();
		super.onDestroy();
	}

    /**
     * progressBar递增动画
     */
    private void startProgressAnimation(int newProgress) {
        ObjectAnimator animator = ObjectAnimator.ofInt(mProgressBar, "progress", currentProgress, newProgress);
        animator.setDuration(300);
        animator.setInterpolator(new DecelerateInterpolator());
        animator.start();
    }

    /**
     * progressBar消失动画
     */
    private void startDismissAnimation(final int progress) {
        ObjectAnimator anim = ObjectAnimator.ofFloat(mProgressBar, "alpha", 1.0f, 0.0f);
        anim.setDuration(1500);  // 动画时长
        anim.setInterpolator(new DecelerateInterpolator());     // 减速
        // 关键, 添加动画进度监听器
        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                float fraction = valueAnimator.getAnimatedFraction();      // 0.0f ~ 1.0f
                int offset = 100 - progress;
                mProgressBar.setProgress((int) (progress + offset * fraction));
            }
        });

        anim.addListener(new AnimatorListenerAdapter() {

            @Override
            public void onAnimationEnd(Animator animation) {
                // 动画结束
                mProgressBar.setProgress(0);
                mProgressBar.setVisibility(View.GONE);
                isAnimStart = false;
            }
        });
        anim.start();
    }

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (mAgentWeb.handleKeyEvent(keyCode, event)) {
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}
	public void back(View v){
		if(mAgentWeb.getWebCreator().getWebView() != null && mAgentWeb.getWebCreator().getWebView().canGoBack())
		{
			mAgentWeb.back();//返回上一页面
		}
		else
		{
			finish();
		}
	}

	public void close(View v){
    	finish();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.save:
				showInProgress(getString(R.string.submiting),false,true);
				mAgentWeb.getJsAccessEntrace().quickCallJs("submit");//提交保存
				break;
			case R.id.iv_close:
				close(null);
				break;
			default:
				break;
		}
	}

//	@Override
//	public void openFileChooserCallBack(ValueCallback<Uri> uploadMsg, String acceptType) {
//		mUploadMsg = uploadMsg;
//		showOptions();
//	}
//
//	@Override
//	public boolean openFileChooserCallBackAndroid5(WebView webView, ValueCallback<Uri[]> filePathCallback, WebChromeClient.FileChooserParams fileChooserParams) {
//		mUploadMsgForAndroid5 = filePathCallback;
//		showOptions();
//
//		return true;
//	}
	public void showOptions() {
		AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
		alertDialog.setOnCancelListener(new CommonWebViewActivity.DialogOnCancelListener());

		alertDialog.setTitle(getString(R.string.activity_devices_list_choose_operation));
		// gallery, camera.
		String[] options = {getString(R.string.album), getString(R.string.camera)};

		alertDialog.setItems(options, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						if (which == 0) {
							if (PermissionUtil.isOverMarshmallow()) {
								if (!PermissionUtil.isPermissionValid(CommonWebViewActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
									Toast.makeText(CommonWebViewActivity.this,
											"请去\"设置\"中开启本应用的图片媒体访问权限",
											Toast.LENGTH_SHORT).show();

									restoreUploadMsg();
									requestPermissionsAndroidM();
									return;
								}

							}

							try {
								mSourceIntent = ImageUtil.choosePicture();
								startActivityForResult(mSourceIntent, REQUEST_CODE_PICK_IMAGE);
							} catch (Exception e) {
								e.printStackTrace();
								Toast.makeText(CommonWebViewActivity.this,
										"请去\"设置\"中开启本应用的图片媒体访问权限",
										Toast.LENGTH_SHORT).show();
								restoreUploadMsg();
							}

						} else {
							if (PermissionUtil.isOverMarshmallow()) {
								if (!PermissionUtil.isPermissionValid(CommonWebViewActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
									Toast.makeText(CommonWebViewActivity.this,
											"请去\"设置\"中开启本应用的图片媒体访问权限",
											Toast.LENGTH_SHORT).show();

									restoreUploadMsg();
									requestPermissionsAndroidM();
									return;
								}

								if (!PermissionUtil.isPermissionValid(CommonWebViewActivity.this, Manifest.permission.CAMERA)) {
									Toast.makeText(CommonWebViewActivity.this,
											"请去\"设置\"中开启本应用的相机权限",
											Toast.LENGTH_SHORT).show();

									restoreUploadMsg();
									requestPermissionsAndroidM();
									return;
								}
							}

							try {
								mSourceIntent = ImageUtil.takeBigPicture();
								startActivityForResult(mSourceIntent, REQUEST_CODE_IMAGE_CAPTURE);

							} catch (Exception e) {
								e.printStackTrace();
								Toast.makeText(CommonWebViewActivity.this,
										"请去\"设置\"中开启本应用的相机和图片媒体访问权限",
										Toast.LENGTH_SHORT).show();

								restoreUploadMsg();
							}
						}
					}
				}
		);

		alertDialog.show();
	}
	private class DialogOnCancelListener implements DialogInterface.OnCancelListener {
		@Override
		public void onCancel(DialogInterface dialogInterface) {
			restoreUploadMsg();
		}
	}

	private void restoreUploadMsg() {
//		if (mUploadMsg != null) {
//			mUploadMsg.onReceiveValue(null);
//			mUploadMsg = null;
//
//		} else if (mUploadMsgForAndroid5 != null) {
//			mUploadMsgForAndroid5.onReceiveValue(null);
//			mUploadMsgForAndroid5 = null;
//		}
	}
	private void requestPermissionsAndroidM() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			List<String> needPermissionList = new ArrayList<>();
			needPermissionList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
			needPermissionList.add(Manifest.permission.READ_EXTERNAL_STORAGE);
			needPermissionList.add(Manifest.permission.CAMERA);
			PermissionUtil.requestPermissions(CommonWebViewActivity.this, P_CODE_PERMISSIONS, needPermissionList);
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
//		if (resultCode != Activity.RESULT_OK) {
//			if (mUploadMsg != null) {
//				mUploadMsg.onReceiveValue(null);
//			}
//
//			if (mUploadMsgForAndroid5 != null) {         // for android 5.0+
//				mUploadMsgForAndroid5.onReceiveValue(null);
//			}
//			return;
//		}
//		switch (requestCode) {
//			case REQUEST_CODE_IMAGE_CAPTURE:
//			case REQUEST_CODE_PICK_IMAGE:
//				try {
//					if (mUploadMsg != null) {
//
//						String sourcePath = ImageUtil.retrievePath(this, mSourceIntent, data);
//
//						if (TextUtils.isEmpty(sourcePath) || !new File(sourcePath).exists()) {
//							Log.e(TAG, "sourcePath empty or not exists.");
//							break;
//						}
//						Uri uri = Uri.fromFile(new File(sourcePath));
//						mUploadMsg.onReceiveValue(uri);
//						mUploadMsg = null;
//
//					} else if (mUploadMsgForAndroid5!=null) {
//						String sourcePath = ImageUtil.retrievePath(this, mSourceIntent, data);
//
//						if (TextUtils.isEmpty(sourcePath) || !new File(sourcePath).exists()) {
//							Log.e(TAG, "sourcePath empty or not exists.");
//							break;
//						}
//						Uri uri = Uri.fromFile(new File(sourcePath));
//						mUploadMsgForAndroid5.onReceiveValue(new Uri[]{uri});
//						mUploadMsgForAndroid5 = null;
//					}
//				} catch (Exception e) {
//                    Log.e(TAG, "image upload error.",e);
//				}
//				break;
//		}

	}
}
