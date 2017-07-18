package com.linked.erfli.library.broad;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.linked.erfli.library.base.BaseActivity;

/**
 * Created by 志强 on 2017.6.9.
 */

public class MyChatBroadcasReceiver extends BroadcastReceiver {

    private static String NET_CHANGE_ACTION = "cn.com.watchman.count";
    private static MyBroadcasNotifyInterface myBroadcasNotifyInterface = BaseActivity.notifyInterface;


    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(NET_CHANGE_ACTION)) {
            Log.i("MyChatBroadcasReceiver", "onReceive");
            myBroadcasNotifyInterface.onNetChanges(intent.getIntExtra("currentCount", 0), intent.getIntExtra("totalCount",0));
        }
    }

    // 自定义接口
    public interface MyBroadcasNotifyInterface {
        void onNetChanges(int i, int b);
    }

//    public static void register(Context context, MyBroadcasNotifyInterface fileUploadListener,Intent intent) {
//        myBroadcasNotifyInterface = fileUploadListener;
//        context.sendBroadcast(intent);
//    }
}
