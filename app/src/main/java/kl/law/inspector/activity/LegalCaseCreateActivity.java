package kl.law.inspector.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.databinding.DataBindingUtil;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;

import kl.law.inspector.R;
import kl.law.inspector.databinding.ActivityLegalCaseCreateBinding;
import kl.law.inspector.vm.LegalCaseViewModel;

public class LegalCaseCreateActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivityLegalCaseCreateBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_legal_case_create);
        LegalCaseViewModel.CreateViewModel viewModel = new LegalCaseViewModel.CreateViewModel(this, binding);
        viewModel.init();
        binding.setViewModel(viewModel);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("案件录入");
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                new AlertDialog.Builder(this).setTitle("提示").setMessage("是否放弃本次案例的录入？").setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                }).setNegativeButton("取消", null).show();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
