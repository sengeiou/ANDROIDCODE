package com.smartism.znzk.util;

import android.app.Activity;
import android.content.Intent;

import com.paypal.android.sdk.payments.PayPalConfiguration;
import com.paypal.android.sdk.payments.PayPalItem;
import com.paypal.android.sdk.payments.PayPalPayment;
import com.paypal.android.sdk.payments.PayPalPaymentDetails;
import com.paypal.android.sdk.payments.PayPalService;
import com.paypal.android.sdk.payments.PaymentActivity;
import com.paypal.android.sdk.payments.PaymentConfirmation;

import java.math.BigDecimal;

/*
* author : mz
*
* 主要思想：通过启动PayPal支付页面，然后将结果通过onActivityResult返回
*
* 大体流程：
*   1、创建PayPalConfiguration对象(配置你的ClientId和环境)，通过Intent传至PayPalService
*   2、创建订单对象
*   3、启动PaymentActivity（需要传订单对象以及配置对象）
*   4、在onActivityResult方法中接收结果
* */
public class PayPalHelper {

    //支付失败码
    public static final int RESULT_CANCEL = 1 ; //取消支付
    public static final int RESULT_EXTRAS_INVALID = 2 ; //提交的PayPalPayment或者PayPalConfiguration对象无效
    public static final int RESULT_UNKONW_ERROR = 3 ; //其它错误

    public static final PayPalHelper sHelper = new PayPalHelper();

    public  static final String CURRENCY = "USD"; //货币码,这里使用美刀

    private static final String ENVIRONMENT_SENDBOX = PayPalConfiguration.ENVIRONMENT_SANDBOX ; //测试PayPal支付用的，这里的环境必须和ClientId相对应
    private static final String ENVIRONMENT_PRODUCT = PayPalConfiguration.ENVIRONMENT_PRODUCTION ; //上线环境，真枪实弹
    private static final int REQUEST_PAYPAL_CODE = 0x475 ; //请求码


    private Activity activity ; //需要启用PayPal支付的页面
    private PayPalResult result ; //支付结果回调
    private PayPalConfiguration config ; //配置对象，包括ClientId和支付环境

    public static PayPalHelper getInstrance(){
        return sHelper ;
    }

    /*
    *  context :启动服务
    *  clientId : PayPal开发者网站创建的appId
    *
    *  每一个支付页面都需要调用该方法启动服务，并且在支付结束停止服务
    * */
    public PayPalHelper initPayPal(Activity context, String clientId){
        activity = context ;
        config = new PayPalConfiguration()
                .environment(ENVIRONMENT_SENDBOX)
                .clientId(clientId);
        Intent intent  = new Intent(context, PayPalService.class);
        intent.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION,config);
        context.startService(intent);
        return this ;
    }


    /*
        支付,含运费和税费,PayPalResult ：支付结果回调
        items：封装商品，一个PayPalItem对象代表一件商品
        shipping :运费
        tax :税费
        result :结果回调
     */
    public PayPalHelper doPay(PayPalItem[] items , BigDecimal shipping, BigDecimal tax, PayPalResult result){
        checkArray(items);
        checkNull(shipping);
        checkNull(tax);
        checkNull(result);
        this.result = result ;
        //商品总价格，不包含运费和税费，保留2位小数，四舍五入，好像不太好
        BigDecimal goodsTotalPrice = PayPalItem.getItemTotal(items).setScale(2, BigDecimal.ROUND_HALF_UP);
        BigDecimal totalPrice  = new BigDecimal(0);//加运费+加商品+加税费
        //用于描述PayPayPayment，运费、商品总价、税费
        PayPalPaymentDetails payPalPaymentDetails = new PayPalPaymentDetails(shipping,goodsTotalPrice,tax);
        totalPrice = totalPrice.add(goodsTotalPrice);
        totalPrice = totalPrice.add(tax);
        totalPrice = totalPrice.add(shipping);

        //第三个参数是订单描述，通常为商品名,可以自定义
        PayPalPayment payPalPayment = new PayPalPayment(totalPrice,CURRENCY,items[0].getName(), PayPalPayment.PAYMENT_INTENT_SALE);
        payPalPayment.items(items).paymentDetails(payPalPaymentDetails);
        Intent intent = new Intent(activity, PaymentActivity.class);
        intent.putExtra(PaymentActivity.EXTRA_PAYMENT,payPalPayment);
        intent.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION,config);
        activity.startActivityForResult(intent,REQUEST_PAYPAL_CODE);
        return this ;
    }

    //支付，不含运费和税费
    public PayPalHelper doPay(PayPalItem[] items, PayPalResult result){
        doPay(items,new BigDecimal(0),new BigDecimal(0),result);
        return this ;
    }

    //支付结果回调，必须在Activity的onActivityResult方法调用
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        if(requestCode==REQUEST_PAYPAL_CODE&&result!=null){
            if(resultCode== Activity.RESULT_OK){
                //成功,处于安全考虑，建议将PayPal返回的支付结果id传至服务器进行验证，在来确认订单的有效性
                PaymentConfirmation paymentResult =
                        data.getParcelableExtra(PaymentActivity.EXTRA_RESULT_CONFIRMATION);
                result.paySuccess(paymentResult);
            }else if(resultCode== Activity.RESULT_CANCELED){
                result.payFailed(RESULT_CANCEL);
            }else if(resultCode== PaymentActivity.RESULT_EXTRAS_INVALID){
                result.payFailed(RESULT_EXTRAS_INVALID);
            }else{
                result.payFailed(RESULT_UNKONW_ERROR);
            }
        }
    }

    private void checkArray(Object[] object){
        checkNull(object);
        if(object.length==0){
            throw new IllegalArgumentException("array's length is zero");
        }
    }

    private void checkNull(Object object){
        if(object==null){
            throw new NullPointerException("object is null");
        }
    }

    //停止服务,和initPayPal配对使用
    public void destroy(){
        if(activity!=null){
            activity.stopService(new Intent(activity, PayPalService.class));
        }
        activity = null ;
        result = null ;
    }


    public interface PayPalResult{

        //支付成功回调
        void paySuccess(PaymentConfirmation payResult);

        //支付失败回调，具体的失败码需要我们自己分析
        void payFailed(int failedCode);
    }
}
