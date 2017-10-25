package kl.law.inspector.activity;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;

import kl.law.inspector.R;
import kl.law.inspector.databinding.ActivityLegalCaseDetailBinding;
import kl.law.inspector.vm.LegalCaseViewModel;

public class LegalCaseDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        String id = intent.getStringExtra("id");
        int statusCode = intent.getIntExtra("statusCode", 0);

        ActivityLegalCaseDetailBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_legal_case_detail);
        LegalCaseViewModel.DetailViewModel viewModel = new LegalCaseViewModel.DetailViewModel(this, binding);
        viewModel.init(id);

        binding.setViewModel(viewModel);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("案件详情");
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId()==android.R.id.home) {
            finish();
        }
        
        return super.onOptionsItemSelected(item);
    }
}
