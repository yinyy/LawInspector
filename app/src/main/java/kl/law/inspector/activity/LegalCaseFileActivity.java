package kl.law.inspector.activity;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import java.io.File;
import java.util.List;

import kl.law.inspector.R;
import kl.law.inspector.databinding.ActivityLegalCaseFileBinding;
import kl.law.inspector.tools.CameraKit;
import kl.law.inspector.tools.FileKit;
import kl.law.inspector.tools.SimpleRecycleViewAdapter;
import kl.law.inspector.vm.AttachmentViewModel;
import kl.law.inspector.vm.LegalCaseViewModel;

public class LegalCaseFileActivity extends AppCompatActivity {
    private ActivityLegalCaseFileBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        String id = intent.getStringExtra("id");

        binding = DataBindingUtil.setContentView(this, R.layout.activity_legal_case_file);
        LegalCaseViewModel.FileViewModel viewModel = new LegalCaseViewModel.FileViewModel(this, binding);
        viewModel.init(id);
        binding.setViewModel(viewModel);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("案件资料");
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        finish();
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            File file = null;

            if ((requestCode & 0x00ff) == AttachmentViewModel.REQUEST_CAPTURE_PICTURE) {
                Bundle extras = data.getExtras();
                Bitmap bm = (Bitmap) extras.get("data");
                file = CameraKit.saveBitmap(this, bm);
            } else if ((requestCode & 0x00ff) == AttachmentViewModel.REQUEST_CAPTURE_VIDEO ||
                    (requestCode & 0x00ff) == AttachmentViewModel.REQUEST_PICK ||
                    (requestCode & 0x00ff) == AttachmentViewModel.REQUEST_OPEN_FILE) {
                Uri uri = data.getData();
                file = FileKit.getFile(this, uri);
            }

            if (file != null) {
                String filename = file.getAbsolutePath();

                SimpleRecycleViewAdapter<AttachmentViewModel> adapter = null;
                if ((requestCode & 0xff00) == 0x0100) {
                    adapter = (SimpleRecycleViewAdapter<AttachmentViewModel>) binding.documentRecyclerView.getAdapter();
                } else if ((requestCode & 0xff00) == 0x0200) {
                    adapter = (SimpleRecycleViewAdapter<AttachmentViewModel>) binding.pictureRecyclerView.getAdapter();
                } else if ((requestCode & 0xff00) == 0x0400) {
                    adapter = (SimpleRecycleViewAdapter<AttachmentViewModel>) binding.videoRecyclerView.getAdapter();
                }

                if (adapter != null) {
                    List<AttachmentViewModel> datas = adapter.getData();

                    AttachmentViewModel flivm = new AttachmentViewModel(this);

                    if (FileKit.isBitmap(filename) || FileKit.isVideo(filename)) {
                        flivm.imageUrl.set(filename);
                    } else {
                        flivm.imageRes.set(FileKit.getFileIcon(filename));
                    }

                    flivm.localFile.set(file);
                    flivm.type.set(AttachmentViewModel.TYPE_ITEM);
                    flivm.showDelete.set(true);
                    datas.add(datas.size()-1, flivm);

                    adapter.notifyDataSetChanged();
                }
            }
        }
    }
}