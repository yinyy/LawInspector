package kl.law.inspector.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

import com.baidu.android.pushservice.PushMessageReceiver;

import org.json.JSONObject;

import java.util.List;

import kl.law.inspector.tools.ApiKit;
import kl.law.inspector.tools.NetworkAccessKit;
import kl.law.inspector.tools.UserData;

/**
 * Created by yinyy on 2017/10/14.
 */

public class MyPushReceiver extends PushMessageReceiver {
    @Override
    public void onBind(final Context context, int errorCode, String appId, String userId, String channelId, String requestId) {
        //绑定成功后，需要向服务器上报本地机器的channelId，以便服务器可以进行推送
        Log.d("TEST", errorCode+" == " + appId + " == " + userId+ " == " + channelId+ " == " + requestId);

        //先查询本地保存的channelId
        SharedPreferences pref = context.getSharedPreferences("profile.data", Context.MODE_PRIVATE);
        String currentChannelId = pref.getString("channelId", "");
        if(!currentChannelId.equals(channelId)){
            UserData.getInstance().setChannelId(channelId);
            NetworkAccessKit.getData(context, ApiKit.URL_BIND_CHANNEL_ID(UserData.getInstance().getId(), UserData.getInstance().getChannelId()), new NetworkAccessKit.DefaultCallback<JSONObject>() {
                @Override
                public void onSuccess(JSONObject data) {
                    Log.d("TEST", "成功更新百度推送ChannelId。");
                }

                @Override
                public void onFailure(int code, String remark) {
                    Log.d("TEST", "更新百度推送ChannelId发生错误。");
                    Toast.makeText(context, "更新百度推送ChannelId发生错误。", Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    @Override
    public void onUnbind(final Context context, int errorCode, String appId) {
        NetworkAccessKit.getData(context, ApiKit.URL_UNBIND_CHANNEL_ID(UserData.getInstance().getId()), new NetworkAccessKit.DefaultCallback<JSONObject>() {
            @Override
            public void onSuccess(JSONObject data) {
                Log.d("TEST", "成功解除百度推送ChannelId。");
            }

            @Override
            public void onFailure(int code, String remark) {
                Log.d("TEST", "更新百度推送ChannelId发生错误。");
                Toast.makeText(context, "更新百度推送ChannelId发生错误。", Toast.LENGTH_LONG).show();
            }
        });
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
        Toast.makeText(context, "点击了消息", Toast.LENGTH_LONG).show();

        Intent intent= new Intent(context.getApplicationContext(),MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        context.startActivity(intent);
    }

    @Override
    public void onNotificationArrived(Context context, String title, String description, String customContentString) {
        Toast.makeText(context, "有新消息到达", Toast.LENGTH_LONG).show();
    }
}
