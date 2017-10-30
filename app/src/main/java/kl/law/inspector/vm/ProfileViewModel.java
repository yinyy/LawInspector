package kl.law.inspector.vm;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.databinding.ObservableField;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.Toast;

import java.text.MessageFormat;

import kl.law.inspector.activity.LoginActivity;
import kl.law.inspector.databinding.FragmentProfileBinding;
import kl.law.inspector.tools.UserData;

/**
 * Created by yinyy on 2017/10/9.
 */

public class ProfileViewModel extends AbstractViewModel<FragmentProfileBinding> {
    public final ObservableField<String> name = new ObservableField<>();
    public final ObservableField<String> department = new ObservableField<>();
    public final ObservableField<String> department2 = new ObservableField<>();
    public final ObservableField<String> username = new ObservableField<>();
    public final ObservableField<String> serial = new ObservableField<>();


    public ProfileViewModel(Context context, FragmentProfileBinding binding) {
        super(context, binding);
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
        Toast.makeText(view.getContext(), "检查更新", Toast.LENGTH_LONG).show();
    }
}