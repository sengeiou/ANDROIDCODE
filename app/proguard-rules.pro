# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in D:\android\as_bundle\sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

#雄迈摄像头SDK
#-libraryjars libs/libFunSDK.jar
-dontwarn com.lib.**
-keep class com.lib.**{*; }
-dontwarn com.video.opengl.**
-keep class com.video.opengl.**{*; }
-keep class com.smartism.znzk.xiongmai.lib.**{*;}
#声波配网添加忽略文件
#-libraryjars libs/EMTMFSDK_0101_160914.jar
-dontwarn com.lsemtmf.**
-keep class com.lsemtmf.**{*; }
-dontwarn com.larksmart.**
-keep class com.larksmart.**{*; }


 #指定代码的压缩级别
    -optimizationpasses 5

    #包明不混合大小写
    -dontusemixedcaseclassnames

    #不去忽略非公共的库类
    -dontskipnonpubliclibraryclasses

     #优化  不优化输入的类文件
    -dontoptimize

     #预校验
    -dontpreverify

     #混淆时是否记录日志
    -verbose

    # 混淆时所采用的算法
    -optimizations !code/simplification/arithmetic,!field/*,!class/merging/*

 #不混淆资源类
-keepclassmembers class **.R$* {
    public static <fields>;
}

  #保持 native 方法不被混淆
-keepclasseswithmembernames class * {
        native <methods>;
    }
# toastutils
-keep class com.hjq.toast.** {*;}


 #忽略警告
#-ignorewarnin

## 保持哪些类不被混淆(在清单文件的类需要过滤)
-keep public class * extends android.app.Fragment
-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider
-keep public class * extends android.app.backup.BackupAgentHelper
-keep public class * extends android.preference.Preference
-keep public class * extends android.support.v4.**
-keep public class com.android.vending.licensing.ILicensingService

 #保护注解
-keepattributes *Annotation*

-keep public class * extends android.view.View {
        public <init>(android.content.Context);
        public <init>(android.content.Context, android.util.AttributeSet);
        public <init>(android.content.Context, android.util.AttributeSet, int);
        public void set*(...);
    }


        #保持自定义控件类不被混淆
        -keepclasseswithmembers class * {
            public <init>(android.content.Context, android.util.AttributeSet);
        }

        #保持自定义控件类不被混淆
        -keepclassmembers class * extends android.app.Activity {
           public void *(android.view.View);
        }

        #保持 Parcelable 不被混淆
        -keep class * implements android.os.Parcelable {
          public static final android.os.Parcelable$Creator *;
        }

        #保持 Serializable 不被混淆
        -keepnames class * implements java.io.Serializable

        #保持 Serializable 不被混淆并且enum 类也不被混淆
        -keepclassmembers class * implements java.io.Serializable {
            static final long serialVersionUID;
            private static final java.io.ObjectStreamField[] serialPersistentFields;
            !static !transient <fields>;
            !private <fields>;
            !private <methods>;
            private void writeObject(java.io.ObjectOutputStream);
            private void readObject(java.io.ObjectInputStream);
            java.lang.Object writeReplace();
            java.lang.Object readResolve();
        }
-keep public class * extends android.widget.BaseAdapter {*;}

#第三方jar包
-keep class com.umeng.fb.**{ *;}
-keep public class com.idea.fifaalarmclock.app.R$*{
    public static final int *;
}
-keep public class com.umeng.fb.ui.ThreadView {
}
-dontwarn com.umeng.**
-dontwarn org.apache.commons.**
-keep public class * extends com.umeng.**
-keep class com.umeng.** {*; }

#-libraryjars libs/commons-io-2.4.jar
#-libraryjars libs/commons-lang-2.5.jar
#-libraryjars libs/dom4j-1.6.1.jar
-keep class org.dom4j.**{*;}
-dontwarn org.dom4j.**
-dontwarn org.bouncycastle.**
-dontwarn org.apache.commons.logging.impl.**
-keep class com.ObjBlockCipherParam{ *; }
-keep class org.apache.commons.logging.impl.**{*;}
#-libraryjars libs/http-legacy.jar
#-libraryjars libs/httpmime-4.1.3.jar
#-libraryjars libs/iots-android-smartlink3.7.0.jar
#-libraryjars libs/mina-core-2.0.9.jar
-keep class org.apache.mina.**{*;}
#-libraryjars libs/nineoldandroids-2.4.0.jar
#-libraryjars libs/nvsdk_1.3.jar
-keep class com.macrovideo.sdk.**{*;}
#-libraryjars libs/qr_core.jar
#-libraryjars libs/slf4j-android-1.5.8.jar
#-libraryjars libs/slidingmenu.jar
-dontwarn android.support.**
-dontwarn com.google.android.maps.**

-keep class com.slidingmenu.** { *; }
-keep interface com.slidingmenu.** { *; }

-dontwarn com.slidingmenu.lib**
-keep class com.slidingmenu.lib.** { *; }
# ActionBarSherlock混淆
-dontwarn com.actionbarsherlock.**
-keep class com.actionbarsherlock.** { *; }
-keep interface com.actionbarsherlock.** { *; }
-keep class * extends java.lang.annotation.Annotation { *; }
-keepclasseswithmembernames class * {
    native <methods>;
}

-keep class  com.tecent.stat.** {*;}
-keep public interface com.tencent.**
-keep public interface com.umeng.socialize.**
-keep public interface com.umeng.socialize.sensor.**
-keep public interface com.umeng.scrshot.*
-keep public class com.tencent.** {*;}
-keep class com.nostra13.universalimageloader.** { *; }
#-libraryjars libs/XlwDevice.jar
#-libraryjars libs/zbardecoder.jar
#-libraryjars libs/mpandroidchartlibrary-2-1-6.jar
#-libraryjars libs/p2p_core.jar
-keep class com.github.mikephil.charting.** { *; }
-keep class com.p2p.** { *; }
-keep class com.p2p.core.** {*;}
-keep class com.smartism.znzk.activity.device.** { *; }
-keep class com.smartism.znzk.yaokan.** { *; }
-dontwarn io.netty.**
-keep class io.netty.** { *; }
-keep interface io.netty.** { *; }

-dontwarn  org.apache.**
-keep class org.apache.http.entity.mime.** {*;}
-keep class org.apache.http.** {*;}
-keep class com.smartism.znzk.util.**{*;}
-keep class android.support.v4.** {*;}
-keep interface android.support.v4.app.** { *; }
-keep class android.net.http.** {*;}
-keep class com.weibo.sdk.android.** {*;}
-keep class com.sina.sso.** {*;}
-keep class java.**{*;}
-dontwarn android.support.**

-keep class android.util.FloatMath

 #避免混淆泛型 如果混淆报错建议关掉
-keepattributes Signature

-keepattributes SourceFile,LineNumberTable

#如果用用到Gson解析包的，直接添加下面这几行就能成功混淆，不然会报错。
#gson
#-libraryjars libs/gson-2.0.jar
-keep class sun.misc.Unsafe { *; }
# Application classes that will be serialized/deserialized over Gson
-keep class com.google.gson.examples.android.model.** { *; }
#smartlink wifi
-keep class com.realtek.simpleconfiglib.** {*;}
#fast json相关
#-libraryjars libs/fastjson-1.1.52.android.jar #fastjson的jar包不要混淆
-keep class com.alibaba.fastjson.** { *; }     #fastjson包下的所有类不要混淆，包括类里面的方法
#-keepattributes Signature                      #这行一定要加上，否则你的object中含有其他对象的字段的时候会抛出ClassCastException
-dontwarn com.alibaba.fastjson.**              #告诉编译器fastjson打包过程中不要提示警告

#角标相关
#https://github.com/leolin310148/ShortcutBadger/issues/46
-keep class me.leolin.shortcutbadger.impl.AdwHomeBadger { <init>(...); }
-keep class me.leolin.shortcutbadger.impl.ApexHomeBadger { <init>(...); }
-keep class me.leolin.shortcutbadger.impl.AsusHomeLauncher { <init>(...); }
-keep class me.leolin.shortcutbadger.impl.DefaultBadger { <init>(...); }
-keep class me.leolin.shortcutbadger.impl.NewHtcHomeBadger { <init>(...); }
-keep class me.leolin.shortcutbadger.impl.NovaHomeBadger { <init>(...); }
-keep class me.leolin.shortcutbadger.impl.SolidHomeBadger { <init>(...); }
-keep class me.leolin.shortcutbadger.impl.SonyHomeBadger { <init>(...); }
-keep class me.leolin.shortcutbadger.impl.XiaomiHomeBadger { <init>(...); }

#定时任务后台任务保活相关
-keep interface android.app.job.** { *; }
-keep class me.tatarka.support.** { *; }
-keep interface me.tatarka.support.** { *; }

#webview
#-keepclassmembers class cn.xx.xx.Activity$AppAndroid {
#  public *;
#}
-keepattributes *Annotation*
-keepattributes *JavascriptInterface*

## keep 使用 webview 的类
#-keepclassmembers class com.goldnet.mobile.activity.InfoDetailActivity {
#   public *;
#}

-keepclassmembernames class com.smartism.znzk.activity.WebViewActivity{
    private *;
    public *;
}
## keep 使用 webview 的类的所有的内部类
#-keepclassmembers   class com.goldnet.mobile.activity.InfoDetailActivity$*{
#    *;
#}


-keep class cn.sharesdk.**{*;}
-keep class com.sina.**{*;}
-keep class **.R$* {*;}
-keep class **.R{*;}
-keep class com.mob.**{*;}
-keep class m.framework.**{*;}
-keep class com.bytedance.**{*;}
-dontwarn cn.sharesdk.**
-dontwarn com.sina.**
-dontwarn com.mob.**
-dontwarn **.R$*



#okhttputils
-dontwarn com.zhy.http.**
-keep class com.zhy.http.**{*;}


#okhttp
-dontwarn okhttp3.**
-keep class okhttp3.**{*;}


#okio
-dontwarn okio.**
-keep class okio.**{*;}



#这里com.xiaomi.mipushdemo.DemoMessageRreceiver改成app中定义的完整类名
-keep class com.smartism.znzk.activity.BroadcastReceiver {*;}
#可以防止一个误报的 warning 导致无法成功编译，如果编译使用的 Android 版本是 23。
-dontwarn com.xiaomi.push.**


#华为混淆
#-ignorewarning
#-keepattributes *Annotation*
#-keepattributes Exceptions
#-keepattributes InnerClasses
#-keepattributes Signature
## hmscore-support: remote transport
#-keep class * extends com.huawei.hms.core.aidl.IMessageEntity { *; }
## hmscore-support: remote transport
#-keepclasseswithmembers class * implements com.huawei.hms.support.api.transport.DatagramTransport {
#<init>(...);
#}
## manifest: provider for updates
#-keep public class com.huawei.hms.update.provider.UpdateProvider { public *; protected *; }


-keepclassmembers class com.smartism.znzk.activity.ShopFragment$JavaScriptInterface {
  public *;
}

-keepclassmembers class com.smartism.znzk.activity.ShopMainActivity$JavaScriptInterface {
  public *;
}
-keepclassmembers class com.smartism.znzk.activity.WXPrePayActivity$JavaScriptInterface {
  public *;
}
-keepclassmembers class com.smartism.znzk.activity.ActivityParentWebActivity$JSObject {
  public *;
}
-keepattributes *Annotation*
-keepattributes *JavascriptInterface*


#-libraryjars libs/alipaySdk-20161129.jar

-keep class com.alipay.android.app.IAlixPay{*;}
-keep class com.alipay.android.app.IAlixPay$Stub{*;}
-keep class com.alipay.android.app.IRemoteServiceCallback{*;}
-keep class com.alipay.android.app.IRemoteServiceCallback$Stub{*;}
-keep class com.alipay.sdk.app.PayTask{ public *;}
-keep class com.alipay.sdk.app.AuthTask{ public *;}

-keepclasseswithmembernames class * {                                           # 保持 native 方法不被混淆
    native <methods>;
}

-keep class com.smartism.znzk.util.NativeUtils{*;}
-keep class  com.smartism.znzk.activity.device.DeviceInfoActivity{*;}


-dontwarn com.iflytek.**
-keepattributes Signature
-keep class com.iflytek.**{*;}

# glide 的混淆代码
-keep public class * implements com.bumptech.glide.module.GlideModule
-keep public enum com.bumptech.glide.load.resource.bitmap.ImageHeaderParser$** {
  **[] $VALUES;
  public *;
}
# banner 的混淆代码
-keep class com.youth.banner.** {
    *;
 }


#   compile 'com.google.zxing:core:3.3.0'
#     compile 'cn.bingoogolapple:bga-qrcodecore:1.1.9@aar'
#     compile 'cn.bingoogolapple:bga-zxing:1.1.9@aar'
#
#     compile 'cn.bingoogolapple:bga-photopicker:1.2.3@aar'
#     compile 'cn.bingoogolapple:bga-adapter:1.2.0@aar'
-keep class com.google.zxing.** {*;}
-dontwarn com.google.zxing.**

 -keepclassmembers class * extends com.tencent.smtt.sdk.WebChromeClient{
    		public void openFileChooser(...);
    		public void onShowFileChooser(...);
 }

 -dontoptimize
 -dontpreverify

 -dontwarn cn.jpush.**
 -keep class cn.jpush.** { *; }

-keepclasseswithmembers class android.support.v7.widget.RecyclerView$ViewHolder {
   public final View *;
}


#-ignorewarning
-keepattributes *Annotation*
-keepattributes Exceptions
-keepattributes InnerClasses
-keepattributes Signature
-keepattributes SourceFile,LineNumberTable
-keep class com.hianalytics.android.**{*;}
-keep class com.huawei.updatesdk.**{*;}
-keep class com.huawei.hms.**{*;}

#-keep class com.umeng.error.UMError { public ; }
#
#-keep class com.umeng.error.UMErrorCatch { public ; }
#
#-keep class com.umeng.error.UMErrorDataManger { public ; }
#
#-keep class com.umeng.error.BatteryUtils { public ; }