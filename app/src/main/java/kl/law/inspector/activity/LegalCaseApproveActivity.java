package kl.law.inspector.activity;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import kl.law.inspector.BR;
import kl.law.inspector.R;
import kl.law.inspector.databinding.ActivityLegalCaseApproveBinding;
import kl.law.inspector.vm.LegalCaseViewModel;

public class LegalCaseApproveActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        String id = intent.getStringExtra("id");
        int progressCode = intent.getIntExtra("progressCode", 0);
        int stage = progressCode / 10;
        int step = progressCode % 10;

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        if (stage == 1) {
            getSupportActionBar().setTitle("案件审批");
        } else if (stage == 2) {
            getSupportActionBar().setTitle("案件调查");
        } else if (stage == 3) {
            getSupportActionBar().setTitle("行政处罚审批");
        } else if (stage == 4) {
            getSupportActionBar().setTitle("结案登记审批");
        }

        ActivityLegalCaseApproveBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_legal_case_approve);
        LegalCaseViewModel.ApproveViewModel viewModel = new LegalCaseViewModel.ApproveViewModel(this, binding);
        viewModel.init(id, stage, step);

        binding.setStage(stage);
        binding.setStep(step);
        binding.setVariable(BR.viewModel, viewModel);
        binding.executePendingBindings();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        finish();
        return super.onOptionsItemSelected(item);
    }
}
