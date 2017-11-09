package kl.law.inspector.activity;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Window;
import android.view.WindowManager;

import kl.law.inspector.R;
import kl.law.inspector.databinding.ActivityImageBrowserBinding;
import kl.law.inspector.vm.ImageBrowserViewModel;

public class ImageBrowserActivity extends AppCompatActivity {
    private ImageBrowserViewModel viewModel;
    private ActivityImageBrowserBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,  WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getSupportActionBar().hide();

        Intent intent = getIntent();

        binding = DataBindingUtil.inflate(LayoutInflater.from(this), R.layout.activity_image_browser, null, false);
        viewModel = new ImageBrowserViewModel(this, binding);
        viewModel.init(intent.getStringArrayListExtra("files"), intent.getIntExtra("position", 0));
        binding.setViewModel(viewModel);

        setContentView(binding.getRoot());
    }
}
