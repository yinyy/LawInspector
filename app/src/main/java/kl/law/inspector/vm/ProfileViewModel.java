package kl.law.inspector.vm;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.databinding.DataBindingUtil;
import android.databinding.ObservableField;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import org.json.JSONObject;

import java.text.MessageFormat;

import kl.law.inspector.R;
import kl.law.inspector.activity.LoginActivity;
import kl.law.inspector.databinding.ActivityDownloadBinding;
import kl.law.inspector.databinding.FragmentProfileBinding;
import kl.law.inspector.tools.ApiKit;
import kl.law.inspector.tools.NetworkAccessKit;
import kl.law.inspector.tools.UserData;

/**
 * Created by yinyy on 2017/10/9.
 *
 * http://blog.csdn.net/yanxiaosa/article/details/52612058
 */

public class ProfileViewModel extends AbstractViewModel<Fragment, FragmentProfileBinding> {
    public final ObservableField<String> name = new ObservableField<>();
    public final ObservableField<String> department = new ObservableField<>();
    public final ObservableField<String> department2 = new ObservableField<>();
    public final ObservableField<String> username = new ObservableField<>();
    public final ObservableField<String> serial = new ObservableField<>();

    public ProfileViewModel(Fragment owner, FragmentProfileBinding binding) {
        super(owner, binding);
    }

    public void init(){
        name.set(UserData.getInstance().getName());
        department.set(UserData.getInstance().getOfficeName());
        department2.set(MessageFormat.format("所在部门：{0}", UserData.getInstance().getOfficeName()));
        username.set(MessageFormat.format("登录名：{0}", UserData.getInstance().getUsername()));
        serial.set(MessageFormat.format("编号：{0}", UserData.getInstance().getNo()));
    }

    public void onQuitClicked(final View view){
        new AlertDialog.Builder(view.getContext()).setTitle("提示").setMessage("确认退出当前用户吗？").setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                SharedPreferences pref = view.getContext().getSharedPreferences("profile.data", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = pref.edit();

                editor.putBoolean("isRememberPassword", false);
                editor.commit();

                Intent intent = new Intent(view.getContext(), LoginActivity.class);
                view.getContext().startActivity(intent);

                ((Activity)view.getContext()).finish();
            }
        }).setNegativeButton("取消", null).show();
    }

    public void onCheckUpdateClicked(View view){
        int currentVersionCode = 0;
        try{
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            currentVersionCode = packageInfo.versionCode;
        }catch (Exception e){
            e.printStackTrace();
        }

        final int finalCurrentVersionCode = currentVersionCode;
        NetworkAccessKit.getData(context, ApiKit.URL_CHECK_UPDATE(currentVersionCode), new NetworkAccessKit.DefaultCallback<JSONObject>() {
            @Override
            public void onSuccess(final JSONObject data) {
                final int newVersionCode = data.optInt("versionCode");
                if(newVersionCode> finalCurrentVersionCode){
                    final String versionName = data.optString("versionName");
                    new AlertDialog.Builder(context).setTitle("提示")
                            .setMessage(MessageFormat.format("发现新版本（{0}）。是否现在下载？", versionName))
                            .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    ActivityDownloadBinding binding = DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.activity_download, null, false);
                                    DownloadViewModel downloadViewModel = new DownloadViewModel(owner, binding);
                                    downloadViewModel.init(data.optString("url"));
                                    binding.setViewModel(downloadViewModel);

                                    downloadViewModel.start();

                                    //request.allowScanningByMediaScanner();//允许被扫描
                                    //request.setVisibleInDownloadsUi(true);//通知栏一直显示
                                    //request.setTitle("文件下载");
                                    //request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);//下载完成也会持续显示
//                                    final long downloadId = downloadManager.enqueue(request);//得到下载文件的唯一id
//
//                                    IntentFilter intentFilter = new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE);
//                                    BroadcastReceiver receiver = new BroadcastReceiver() {
//                                        @Override
//                                        public void onReceive(Context context, Intent intent) {
//                                            if(DownloadManager.ACTION_DOWNLOAD_COMPLETE.equals(intent.getAction())){
//                                                long intentDownloadId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
//                                                if(intentDownloadId==downloadId){
//                                                    new AlertDialog.Builder(context)
//                                                            .setTitle("提示")
//                                                            .setMessage("下载完成，是否现在安装？")
//                                                            .setPositiveButton("确定", null)
//                                                            .setNegativeButton("取消", null).show();
//                                                }
//                                            }
//                                        }
//                                    };
//                                    context.registerReceiver(receiver, intentFilter);
                                }
                            })
                            .setNegativeButton("取消", null)
                            .show();
                }else{
                    Toast.makeText(context,"没有发现新版本。", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void handleFailureAndError() {
                Toast.makeText(context, "检查版本信息时发生错误，请稍后重试。", Toast.LENGTH_LONG).show();
            }
        });
    }

    public void onNotificationClicked(View view){
        NetworkAccessKit.getData(context, ApiKit.URL_PUSH_MESSAGE(UserData.getInstance().getId(), "test"), new NetworkAccessKit.DefaultCallback<JSONObject>() {
            @Override
            public void onSuccess(JSONObject data) {
                Toast.makeText(context, "消息推送成功。", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onFailure(int code, String remark) {
                Toast.makeText(context, "消息推送失败。", Toast.LENGTH_LONG).show();
            }
        });
    }
}