package kl.law.inspector.activity;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.widget.Toast;

import com.baidu.android.pushservice.PushConstants;
import com.baidu.android.pushservice.PushManager;

import kl.law.inspector.R;
import kl.law.inspector.databinding.ActivityMainBinding;
import kl.law.inspector.tools.PushKit;
import kl.law.inspector.vm.MainActivityViewModel;

public class MainActivity extends AppCompatActivity{

    private boolean isQuit=false;
    private Handler handler = new Handler();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        PushManager.startWork(getApplicationContext(), PushConstants.LOGIN_TYPE_API_KEY, PushKit.getMetaValue(this, "api_key"));

        ActivityMainBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        MainActivityViewModel viewModel = new MainActivityViewModel(this, binding);
        viewModel.init();

        binding.setViewModel(viewModel);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode==KeyEvent.KEYCODE_BACK){
            if(isQuit){
                finish();
            }else{
                isQuit = true;

                Toast.makeText(this, "再按一次返回键退出程序。", Toast.LENGTH_SHORT).show();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        isQuit = false;
                    }
                }, 1000);
            }
        }

        return false;
    }
}
