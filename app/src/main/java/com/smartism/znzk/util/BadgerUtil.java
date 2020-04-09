package com.smartism.znzk.util;

import android.content.Context;
import android.util.Log;

import com.smartism.znzk.db.DatabaseOperator;

import me.leolin.shortcutbadger.ShortcutBadger;

/**
 * Created by 王建 on 16/11/3.
 * 角标工具类
 */

public class BadgerUtil {
    /**
     * 更新角标显示并返回总数
     * @param context
     * @return
     */
    public static int updateBadger(Context context) {
        int totalNr = DatabaseOperator.getInstance(context.getApplicationContext()).queryDeviceInfoAllNotReadCount();
        boolean success = ShortcutBadger.applyCount(context, totalNr);
        Log.e("BADGER", "更新角标结果为:" + success);
        return totalNr;
    }
}
