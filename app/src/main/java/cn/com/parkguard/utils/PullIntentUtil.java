package cn.com.parkguard.utils;

import android.content.Context;
import android.content.Intent;

import com.alibaba.sdk.android.push.notification.CPushMessage;
import com.linked.erfli.library.utils.ToastUtil;

import cn.com.watchman.activity.EventReportActivity;

/**
 * 文件名：PullIntentUtil
 * 描    述：推送过来的信息，根据类型做跳转的工具类
 * 作    者：stt
 * 时    间：2017.7.4
 * 版    本：V1.2.1
 */

public class PullIntentUtil {
    public static void intentAvtivity(Context context, CPushMessage cPushMessage) {
        ToastUtil.show(context, cPushMessage.getContent());
        Intent intent = new Intent(context, EventReportActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }
}
