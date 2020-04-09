/**
 * 通用js,包含mui的基础引用
 * app 引用 每一个页面都需要引用这个js
 */

document.write('<link rel="stylesheet" type="text/css" href="css/mui.min.css" \/>');
document.write('<link rel="stylesheet" type="text/css" href="css/mui.loading.css" \/>');
document.write('<link rel="stylesheet" type="text/css" href="css/app.css" \/>');
document.write('<script src="js/jquery-1.10.2.min.js"><\/script>');
document.write('<script src="js/mui.min.js"><\/script>');
document.write('<script src="js/mui.loading.js"><\/script>');

initMobileObject();
var mobileObject;
function jdmAlert(content){
    if (isMobile() && (typeof (mobileObject.postMessage) != "undefined")){
        mobileObject.postMessage(JSON.stringify({
            method:'showToast',
            params: content
        }));
    }else{
        mui.toast(content);
    }
}

function jdmShowProgress(content){
    mui.showLoading(content,"div"); //加载文字和类型，plus环境中类型为div时强制以div方式显示
}

function jdmCancelProgress(callback){
    mui.hideLoading(callback);//隐藏后的回调函数
}

function jdmAlertAndClose(content){
    if (isMobile() && (typeof (mobileObject.postMessage) != "undefined")){
        mobileObject.postMessage(JSON.stringify({
            method:'showToastAndFinish',
            params: content
        }));
    }else{
        jdmAlert(content);
        window.close();
    }
}

function jdmAlertAndCloseAndRefreshParent(content){
    if (isMobile() && (typeof (mobileObject.postMessage) != "undefined")){
        mobileObject.postMessage(JSON.stringify({
            method:'showAlertAndCloseSelfAndRefreshParent',
            params: content
        }));
    }else{
        jdmAlert(content);
        window.close();
    }
}

function jdmOpenNewViewWithGet(url){
    if (isMobile() && (typeof (mobileObject.postMessage) != "undefined")){
        mobileObject.postMessage(JSON.stringify({
            method:'openNewViewWithGet',
            params: {
                url: url,
                closeP: false,
                showAdd: false
            }
        }));
    }else{
        if (mui.os.android || mui.os.ios){
            window.location.href = "jdmnew://" + url;
        }else{
            window.location.href = url;
        }
    }
}

function initMobileObject() {
    if (isMobile()){
        if (isMobileAndroid()){
            mobileObject = android;
        }else{
            mobileObject = window.webkit.messageHandlers.smartism;
        }
    }
}
function isMobile(){
    return typeof(android) != "undefined" || (typeof(window.webkit) != "undefined" && typeof(window.webkit.messageHandlers) != "undefined" && typeof(window.webkit.messageHandlers.smartism) != "undefined");
}
function isMobileAndroid(){
    return typeof(android) != "undefined";
}

function saveToS3(p,c){
    if (isMobile() && (typeof (mobileObject.postMessage) != "undefined")){
        mobileObject.postMessage(JSON.stringify({
            method:'saveToS3',
            params:{
                parent: p,
                content: c
            }
        }));
    }
}

/***通用工具方法***/

/**
 * 替换指定位置的字符
 */
function jdmReplaceStr(sourceStr, index, str) {
    var a = sourceStr;
    a=a.split('');  //将a字符串转换成数组
    a.splice(index,str.length,str); //将1这个位置的字符，替换成'xxxxx'. 用的是原生js的splice方法。
    return a.join('');  //将数组转换成字符串。  完成。
}