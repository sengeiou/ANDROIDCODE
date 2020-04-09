package com.smartism.znzk.util;

import android.content.Context;

import java.util.Locale;

public class LanguageUtil {
    /**
     * 判断当前语言 是否是中文
     * @param context
     * @return
     */
    public static boolean isZh(Context context) {
        Locale locale = context.getResources().getConfiguration().locale;
        String language = locale.getLanguage();
        if (language.contains("zh")) {
            return true;
        } else {
            return false;
        }
    }
}
