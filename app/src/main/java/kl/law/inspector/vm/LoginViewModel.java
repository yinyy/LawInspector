package kl.law.inspector.vm;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.databinding.ObservableField;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import org.json.JSONObject;

import kl.law.inspector.activity.LegalCaseFileActivity;
import kl.law.inspector.activity.MainActivity;
import kl.law.inspector.databinding.ActivityLoginBinding;
import kl.law.inspector.tools.ApiKit;
import kl.law.inspector.tools.DialogKit;
import kl.law.inspector.tools.NetworkAccessKit;
import kl.law.inspector.tools.UserData;

/**
 * Created by yinyy on 2017/8/17.
 */

public class LoginViewModel extends AbstractViewModel<ActivityLoginBinding> {
    public final ObservableField<String> username = new ObservableField<>();
    public final ObservableField<String> password = new ObservableField<>();
    public final ObservableField<Boolean> isRememberPassword = new ObservableField<>();
    public final ObservableField<Boolean> isAutoLogin = new ObservableField<>();

    public LoginViewModel(Context context, ActivityLoginBinding binding) {
        super(context, binding);
    }

    public void tryAutoLogin() {
        SharedPreferences pref = context.getSharedPreferences("profile.data", Context.MODE_PRIVATE);
        this.username.set(pref.getString("username", ""));
        this.isRememberPassword.set(pref.getBoolean("isRememberPassword", false));

        if (isRememberPassword.get()) {
            this.password.set(pref.getString("password", ""));

            this.isAutoLogin.set(pref.getBoolean("isAutoLogin", false));
            if (isAutoLogin.get()) {
                login(context, username.get(), password.get(), true, true);
            }
        }
    }

    public void onLogin(View view) {
        String username = binding.userameEdit.getText().toString().trim();
        String password = binding.passwordEdit.getText().toString().trim();

        if ("".equals(username)) {
            Toast.makeText(view.getContext(), "请输入用户名。", Toast.LENGTH_LONG).show();
            return;
        }

        if ("".equals(password)) {
            Toast.makeText(view.getContext(), "请输入密码。", Toast.LENGTH_LONG).show();
            return;
        }

        boolean isRemember = binding.rememberCheckbox.isChecked();
        boolean isAutoLogin = binding.autoCheckbox.isChecked();

        login(view.getContext(), username, password, isRemember, isAutoLogin);
    }

    public void onReset(View view) {
        binding.userameEdit.setText("");
        binding.passwordEdit.setText("");
    }

    public void onRememberClicked(View view) {
        if (!binding.rememberCheckbox.isChecked()) {
            binding.autoCheckbox.setChecked(false);
        }
    }

    public void onAutoLoginClicked(View view) {
        if (binding.autoCheckbox.isChecked()) {
            binding.rememberCheckbox.setChecked(true);
        }
    }

    private void login(final Context context, final String username, final String password, final boolean isRemember, final boolean isAutoLogin) {
        DialogKit.showLoadingDialog(context);

        NetworkAccessKit.getData(context, ApiKit.URL_LOGIN(username, password), new NetworkAccessKit.Callback<JSONObject>() {
            @Override
            public void success(JSONObject userData) {
                DialogKit.hideLoadingDialog();

                SharedPreferences pref = context.getSharedPreferences("profile.data", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = pref.edit();

                editor.putString("username", username);
                editor.putString("password", password);
                editor.putBoolean("isRememberPassword", isRemember);
                editor.putBoolean("isAutoLogin", isAutoLogin);
                editor.commit();

                UserData user = UserData.getInstance();
                try {
                    //记录用户信息
                    user.setId(userData.getString("id"));
                    user.setName(userData.getString("name"));
                    user.setOfficeId(userData.getString("officeId"));
                    user.setOfficeName(userData.getString("officeName"));
                    user.setNo(userData.getString("no"));
                    user.setUsername(username);
                }catch (Exception e){
                    e.printStackTrace();
                }

                Intent intent = new Intent(context, MainActivity.class);
                context.startActivity(intent);
                ((Activity) context).finish();
            }

            @Override
            public void failure(String remark) {
                DialogKit.hideLoadingDialog();

                Log.d("TEST", remark);
                Toast.makeText(context, "发生异常：" + remark, Toast.LENGTH_LONG).show();
            }

            @Override
            public void error(String message) {
                DialogKit.hideLoadingDialog();

                Log.d("TEST", "发生错误：" + message);
                Toast.makeText(context,"发生错误：" + message, Toast.LENGTH_LONG).show();
            }
        });
    }

    public void test(View view){
        Intent intent = new Intent(view.getContext(), LegalCaseFileActivity.class);
        view.getContext().startActivity(intent);
    }
}