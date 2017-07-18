package cn.com.watchman.broad;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.linked.erfli.library.broad.MyChatBroadcasReceiver;

/**
 * Created by 志强 on 2017.6.9.
 */

public class WatchChatBroadcasReceiver extends BroadcastReceiver {

    private static String NET_CHANGE_ACTION = "cn.com.watchman.count";
    MyChatBroadcasReceiver.MyBroadcasNotifyInterface myBroadcasNotifyInterface;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(NET_CHANGE_ACTION)) {
            Log.i("MyChatBroadcasReceiver", "onReceive");
            myBroadcasNotifyInterface = (MyChatBroadcasReceiver.MyBroadcasNotifyInterface) context.getApplicationContext();
            myBroadcasNotifyInterface.onNetChanges(intent.getIntExtra("currentCount", 0), intent.getIntExtra("totalCount", 0));
        }
    }


}
