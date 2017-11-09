package kl.law.inspector.activity;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.view.MenuItem;

import kl.law.inspector.R;
import kl.law.inspector.databinding.ActivityDocumentApproveBinding;
import kl.law.inspector.vm.DocumentViewModel;

public class DocumentApproveActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        String id = intent.getStringExtra("id");
        int progressCode = intent.getIntExtra("progressCode", 0);

        ActivityDocumentApproveBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_document_approve);
        binding.fileRecyclerView.setLayoutManager(new GridLayoutManager(this, 4));
        binding.approverRecyclerView.setLayoutManager(new GridLayoutManager(this, 5));
        binding.readerRecyclerView.setLayoutManager(new GridLayoutManager(this, 5));

        DocumentViewModel.ApproveViewModel viewModel = new DocumentViewModel.ApproveViewModel(this, binding);
        viewModel.init(id);
        binding.setViewModel(viewModel);
        binding.setProgressCode(progressCode+1);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("公文流转");
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
