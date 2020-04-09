package com.smartism.znzk.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alipay.sdk.app.PayTask;
import com.paypal.android.sdk.payments.PayPalItem;
import com.paypal.android.sdk.payments.PaymentConfirmation;
import com.smartism.znzk.R;
import com.smartism.znzk.global.Constants;
import com.smartism.znzk.util.Actions;
import com.smartism.znzk.util.CollectionsUtils;
import com.smartism.znzk.util.PayPalHelper;
import com.smartism.znzk.util.ToastUtil;
import com.smartism.znzk.util.WeakRefHandler;
import com.tencent.mm.sdk.modelpay.PayReq;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.WXAPIFactory;

import java.math.BigDecimal;
import java.util.Map;

public abstract class ActivityParentWebActivity extends ActivityParentActivity{
//	private static final String CLIENT_ID = "Ae3JbTeBUWf606psohPgPe2SOELqlumjBlriVwaN6cE5iIRau2sCqm7otgOLCggTobpLC0vtCUjPspNU"; //PayPal生成的ClientID，分为正式环境和测试环境、注意区分,这里是测试环境id
	private static final String CLIENT_ID = "AZMcO0EKBD8Ev_Pb5wUjuc-xXsVsmIsJLZKyJaPvHC8071hwxF5lmV1oepzcaslqo_rQwSkR8byBd-zp"; //PayPal生成的ClientID，分为正式环境和测试环境、注意区分,这里是测试环境id
    public final String JS_OBJECT_NAME = "android"; //js调用原生方法的object名称
    public static final int RESULT_REQUEST=445566;
    public static final int RESULT_NEEDREFRESH=445567;
    public static final int SDK_PAY_FLAG = 1;
    private IWXAPI api;

	private Handler.Callback mCallback = new Handler.Callback() {
		@Override
		public boolean handleMessage(Message msg) {
			switch (msg.what){
				case SDK_PAY_FLAG:
					ActivityParentWebActivity.PayResult payResult = new ActivityParentWebActivity.PayResult((Map<String, String>) msg.obj);
					/**
					 对于支付结果，请商户依赖服务端的异步通知结果。同步通知结果，仅作为支付结束的通知。
					 */
					String resultInfo = payResult.getResult();// 同步返回需要验证的信息
					String resultStatus = payResult.getResultStatus();
					// 判断resultStatus 为9000则代表支付成功
					if (TextUtils.equals(resultStatus, "9000")) {
						// 该笔订单是否真实支付成功，需要依赖服务端的异步通知。
						Toast.makeText(ActivityParentWebActivity.this, getString(R.string.oay_result_ok), Toast.LENGTH_LONG).show();
					} else {
						// 该笔订单真实的支付结果，需要依赖服务端的异步通知。
						Toast.makeText(ActivityParentWebActivity.this, getString(R.string.oay_result_fail), Toast.LENGTH_LONG).show();
					}
					cancelInProgress();
					refreshPage();
					break;
			}
			return false;
		}
	};
	private Handler mHandler = new WeakRefHandler(mCallback);

    private BroadcastReceiver defaultReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context arg0, Intent intent) {
			cancelInProgress();
			if (Actions.WXPAY_ONRESP.equals(intent.getAction())) {
				refreshPage();//收到微信支付回调，刷新当前页
            }else if(Actions.WXPAY_SUCCESS.equals(intent.getAction())){
            	paySuccessToPage("{\"type\":\"wxpay\"}");
			}
        }
    };

    /**
     * 刷新当前页
     */
    protected abstract void refreshPage();

	/**
	 * 支付成功调用页面
	 * @param payParam
	 */
	protected abstract void paySuccessToPage(String payParam);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        api = WXAPIFactory.createWXAPI(this, Constants.APP_ID, false);
        api.registerApp(Constants.APP_ID);
        initRegisterReceiver();

		//PayPal支付初始化,必要的
		PayPalHelper.getInstrance().initPayPal(this,CLIENT_ID);
    }

    /**
     * 注册广播
     */
    private void initRegisterReceiver() {
        IntentFilter receiverFilter = new IntentFilter();
        receiverFilter.addAction(Actions.WXPAY_ONRESP);
        receiverFilter.addAction(Actions.WXPAY_SUCCESS);
        this.registerReceiver(defaultReceiver, receiverFilter);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RESULT_REQUEST && resultCode == RESULT_NEEDREFRESH){
			cancelInProgress();
            refreshPage();
        }

		//PayPal支付回调，必要的，否则无法回调
		PayPalHelper.getInstrance().onActivityResult(requestCode,resultCode,data);
    }

	protected void alipay(final String json) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				PayTask task = new PayTask(ActivityParentWebActivity.this);
				Map<String, String> result = task.payV2(json, true);
				Log.e("AliPay:", json + ";" + result.toString());
				Message msg = new Message();
				msg.what = SDK_PAY_FLAG;
				msg.obj = result;
				mHandler.sendMessage(msg);
			}
		}).start();
	}

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (defaultReceiver != null) {
            unregisterReceiver(defaultReceiver);
        }

		//PayPal停止支付
		PayPalHelper.getInstrance().destroy();
    }

	//PayPal支付
	protected void payPal(String params) {
		JSONArray pArray = JSON.parseArray(params);
		if (CollectionsUtils.isEmpty(pArray)){
			ToastUtil.shortMessage(getString(R.string.oay_result_fail_errpar));
			return;
		}
		PayPalItem[] tmp = new PayPalItem[pArray.size()];
		for (int i = 0;i < pArray.size();i++){
			JSONObject par = pArray.getJSONObject(i);
			tmp[i] = new PayPalItem(par.getString("name"),1,new BigDecimal(par.getDoubleValue("amount")),PayPalHelper.CURRENCY,par.getString("orderID"));
		}
		//创建支付商品,参数说明:商品名、商品数量、商品价格、商品使用的货币码、订单号(可以服务器生成，这里采用当前时间)
		PayPalHelper.getInstrance().doPay(tmp, new PayPalHelper.PayPalResult() {
			@Override
			public void paySuccess(PaymentConfirmation payResult) {
//				Log.i(TAG,payResult.toJSONObject().toString());
				cancelInProgress();
				//{"client":{"environment":"sandbox","paypal_sdk_version":"2.16.0","platform":"Android","product_name":"PayPal-Android-SDK"},"response":{"create_time":"2019-09-29T03:13:04Z","id":"PAYID-LWICCQI8RP612075E827194K","intent":"sale","state":"approved"},"response_type":"payment"}
				ToastUtil.shortMessage(getString(R.string.oay_result_ok));
				JSONObject payParam = new JSONObject();
				payParam.put("payType","paypal");
				payParam.put("payOrderID",payResult.getProofOfPayment().getTransactionId());
				paySuccessToPage(payParam.toJSONString());
			}

			@Override
			public void payFailed(int failedCode) {
				cancelInProgress();
				if (failedCode == PayPalHelper.RESULT_CANCEL){
					ToastUtil.shortMessage(getString(R.string.oay_result_cancel));
				}else if(failedCode == PayPalHelper.RESULT_EXTRAS_INVALID){
					ToastUtil.shortMessage(getString(R.string.oay_result_fail_errpar));
				}else{
					ToastUtil.shortMessage(getString(R.string.oay_result_fail));
				}
			}
		});
	}

	protected void weixinPay(String parmas) {
		if (parmas != null && parmas.length() > 0) {
			Log.e("get server pay params:", parmas);
			JSONObject json = JSON.parseObject(parmas);
			if (null != json && !json.containsKey("retcode")) {
				PayReq req = new PayReq();
				req.appId = json.getString("appid");
				req.partnerId = json.getString("partnerid");
				req.prepayId = json.getString("prepayid");
//                    procduct_Id = req.prepayId;
				req.nonceStr = json.getString("noncestr");
				req.timeStamp = json.getString("timestamp");
				req.packageValue = json.getString("package");
				req.sign = json.getString("sign");
				req.extData = "app data"; // optional
//					Toast.makeText(this, "正常调起支付", Toast.LENGTH_SHORT).show();
				// 在支付之前，如果应用没有注册到微信，应该先调用IWXMsg.registerApp将应用注册到微信
				api.sendReq(req);
//                    web_view.setVisibility(View.GONE);
			} else {
				Log.d("PAY_GET", "返回错误" + json.getString("retmsg"));
				Toast.makeText(this, "返回错误" + json.getString("retmsg"), Toast.LENGTH_SHORT).show();
			}
		} else {
			Log.d("PAY_GET", "服务器请求错误");
			Toast.makeText(this, "服务器请求错误", Toast.LENGTH_SHORT).show();
		}
	}

	public class PayResult {
		private String resultStatus;
		private String result;
		private String memo;

		public PayResult(Map<String, String> rawResult) {
			if (rawResult == null) {
				return;
			}

			for (String key : rawResult.keySet()) {
				if (TextUtils.equals(key, "resultStatus")) {
					resultStatus = rawResult.get(key);
				} else if (TextUtils.equals(key, "result")) {
					result = rawResult.get(key);
				} else if (TextUtils.equals(key, "memo")) {
					memo = rawResult.get(key);
				}
			}
		}

		@Override
		public String toString() {
			return "resultStatus={" + resultStatus + "};memo={" + memo
					+ "};result={" + result + "}";
		}

		/**
		 * @return the resultStatus
		 */
		public String getResultStatus() {
			return resultStatus;
		}

		/**
		 * @return the memo
		 */
		public String getMemo() {
			return memo;
		}

		/**
		 * @return the result
		 */
		public String getResult() {
			return result;
		}
	}
}
