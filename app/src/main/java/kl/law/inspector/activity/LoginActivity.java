package kl.law.inspector.activity;


import android.Manifest;
import android.content.pm.PackageManager;
import android.databinding.DataBindingUtil;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import kl.law.inspector.R;
import kl.law.inspector.databinding.ActivityLoginBinding;
import kl.law.inspector.vm.LoginViewModel;

public class LoginActivity extends AppCompatActivity {
    private ActivityLoginBinding binding;
    private LoginViewModel viewModel;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String[] permissions = new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.CAMERA};
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if(isPermissionAllGranted(permissions)){
                binding = DataBindingUtil.setContentView(this, R.layout.activity_login);

                viewModel = new LoginViewModel(this, binding);
                binding.setViewModel(viewModel);

                viewModel.tryAutoLogin();
            }else{
                ActivityCompat.requestPermissions(this, permissions, 1000);
            }
        } else {
            binding = DataBindingUtil.setContentView(this, R.layout.activity_login);

            viewModel = new LoginViewModel(this, binding);
            binding.setViewModel(viewModel);

            viewModel.tryAutoLogin();
        }
    }

    public boolean isPermissionAllGranted(String[] permissions) {
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }

        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        boolean isAllGranted = true;

        for(int res : grantResults){
            if(res!=PackageManager.PERMISSION_GRANTED){
                isAllGranted = false;
                break;
            }
        }

        if(isAllGranted){
            binding = DataBindingUtil.setContentView(this, R.layout.activity_login);

            viewModel = new LoginViewModel(this, binding);
            binding.setViewModel(viewModel);

            viewModel.tryAutoLogin();
        }else{
            Toast.makeText(this, "没有获取相应的权限。", Toast.LENGTH_LONG).show();
            finish();
        }
    }
}
