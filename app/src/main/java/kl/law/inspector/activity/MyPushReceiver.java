package kl.law.inspector.activity;

import android.content.Context;
import android.util.Log;

import com.baidu.android.pushservice.PushMessageReceiver;

import java.util.List;

/**
 * Created by yinyy on 2017/10/14.
 */

public class MyPushReceiver extends PushMessageReceiver {
    @Override
    public void onBind(Context context, int errorCode, String appId, String userId, String channelId, String requestId) {
        //绑定成功后，需要向服务器上报本地及其的channelId，以便服务器可以进行推送
        Log.d("TEST", errorCode+" == " + appId + " == " + userId+ " == " + channelId+ " == " + requestId);
    }

    @Override
    public void onUnbind(Context context, int errorCode, String appId) {

    }

    @Override
    public void onSetTags(Context context, int errorCode, List sucessTags, List failTags, String requestId) {

    }

    @Override
    public void onDelTags(Context context, int errorCode, List sucessTags, List failTags, String requestId) {

    }

    @Override
    public void onListTags(Context context, int errorCode, List tags, String requestId) {

    }

    @Override
    public void onMessage(Context context, String message, String customContentString) {

    }

    @Override
    public void onNotificationClicked(Context context, String title, String description, String customContentString){

    }

    @Override
    public void onNotificationArrived(Context context, String title, String description, String customContentString) {

    }
}
