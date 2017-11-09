package kl.law.inspector.activity;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.view.MenuItem;

import kl.law.inspector.R;
import kl.law.inspector.databinding.ActivityDocumentDetailBinding;
import kl.law.inspector.vm.DocumentViewModel;

public class DocumentDetailActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        String id = intent.getStringExtra("id");
        int progressCode = intent.getIntExtra("progressCode", 0);

        ActivityDocumentDetailBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_document_detail);
        binding.fileRecyclerView.setLayoutManager(new GridLayoutManager(this, 4));

        DocumentViewModel.DetailViewModel viewModel = new DocumentViewModel.DetailViewModel(this, binding);
        viewModel.init(id, progressCode);
        binding.setViewModel(viewModel);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("公文提交");
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}