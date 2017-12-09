package kl.law.inspector.vm;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.databinding.ObservableField;
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

public class LoginViewModel extends AbstractViewModel<Activity, ActivityLoginBinding> {
    public final ObservableField<String> username = new ObservableField<>();
    public final ObservableField<String> password = new ObservableField<>();
    public final ObservableField<Boolean> isRememberPassword = new ObservableField<>();
    public final ObservableField<Boolean> isAutoLogin = new ObservableField<>();

    public LoginViewModel(Activity owner, ActivityLoginBinding binding) {
        super(owner, binding);
    }

    public void tryAutoLogin() {
        SharedPreferences pref = context.getSharedPreferences("profile.data", Context.MODE_PRIVATE);
        this.username.set(pref.getString("username", ""));
        this.isRememberPassword.set(pref.getBoolean("isRememberPassword", false));

        if (isRememberPassword.get()) {
            this.password.set(pref.getString("password", ""));

            this.isAutoLogin.set(pref.getBoolean("isAutoLogin", false));
            if (isAutoLogin.get()) {
                login(username.get(), password.get(), true, true);
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

        login(username, password, isRemember, isAutoLogin);
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

    private void login(final String username, final String password, final boolean isRemember, final boolean isAutoLogin) {
        DialogKit.showLoadingDialog(context);

        NetworkAccessKit.getData(context, ApiKit.URL_LOGIN(username, password), new NetworkAccessKit.DefaultCallback<JSONObject>() {
            @Override
            public void onSuccess(JSONObject userData) {
                DialogKit.hideLoadingDialog();

                SharedPreferences pref = context.getSharedPreferences("profile.data", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = pref.edit();

                editor.putString("username", username);
                editor.putString("password", password);
                editor.putBoolean("isRememberPassword", isRemember);
                editor.putBoolean("isAutoLogin", isAutoLogin);
                editor.putString("channelId", userData.optString("baiduPushChannelId"));
                editor.commit();

                UserData user = UserData.getInstance();
                //记录用户信息
                user.setId(userData.optString("id"));
                user.setName(userData.optString("name"));
                user.setOfficeId(userData.optString("officeId"));
                user.setOfficeName(userData.optString("officeName"));
                user.setNo(userData.optString("no"));
                user.setChannelId(userData.optString("baiduPushChannelId"));
                user.setUsername(username);

                Intent intent = new Intent(context, MainActivity.class);
                context.startActivity(intent);
                ((Activity) context).finish();
            }

            @Override
            public void handleFailureAndError(String message) {
                super.handleFailureAndError(message);

                Toast.makeText(context, message, Toast.LENGTH_LONG).show();
                DialogKit.hideLoadingDialog();
            }
        });
    }

    public void test(View view){
        Intent intent = new Intent(view.getContext(), LegalCaseFileActivity.class);
        view.getContext().startActivity(intent);
    }
}