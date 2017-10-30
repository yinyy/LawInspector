package kl.law.inspector.vm;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.databinding.ObservableBoolean;
import android.databinding.ObservableField;
import android.databinding.ObservableInt;
import android.databinding.ObservableLong;
import android.provider.MediaStore;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Toast;

import java.io.File;
import java.util.List;

import kl.law.inspector.R;
import kl.law.inspector.tools.SimpleRecycleViewAdapter;

/**
 * Created by yinyy on 2017/10/13.
 */

public class AttachmentViewModel {
    public static final int REQUEST_OPEN_FILE = 0x0001;
    public static final int REQUEST_CAPTURE_PICTURE = 0x0002;
    public static final int REQUEST_CAPTURE_VIDEO = 0x0004;
    public static final int REQUEST_PICK = 0x0008;

    public static final int TYPE_ADD_BUTTON = 0x00;
    public static final int TYPE_ITEM = 0x01;

    public final ObservableField<String> id = new ObservableField<>();
    public final ObservableField<String> title = new ObservableField<>();
    public final ObservableInt type = new ObservableInt();
    public final ObservableField<String> imageUrl = new ObservableField<>();
    public final ObservableInt imageRes = new ObservableInt();
    public final ObservableBoolean showDelete = new ObservableBoolean();
    public final ObservableField<File> localFile = new ObservableField<>();
    public final ObservableField<String> remoteUrl = new ObservableField<>();
    public final ObservableLong progress = new ObservableLong();

    private Context context;

    public AttachmentViewModel(Context context){
        this.context = context;

        progress.set(-1);
    }

    public void onAddClicked(final View view) {
        int recyclerViewId = ((View)view.getParent().getParent()).getId();
        if(recyclerViewId==R.id.documentRecyclerView){
            recyclerViewId=0x0100;
        }else if(recyclerViewId==R.id.pictureRecyclerView){
            recyclerViewId=0x0200;
        }else if(recyclerViewId==R.id.videoRecyclerView){
            recyclerViewId = 0x0400;
        }else {
            recyclerViewId = 0x0800;
        }

        final int finalRecyclerViewId = recyclerViewId;
        new AlertDialog.Builder(view.getContext()).setItems(R.array.attachmentSource, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == 0) {
                    //File previewImageFile = CameraKit.createPreviewImageFile(view.getContext());
                    //Uri uri = CameraKit.createPreviewImageUri(view.getContext(), previewImageFile);

//                intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION
//                        | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
//                intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    ((Activity) context).startActivityForResult(intent, AttachmentViewModel.REQUEST_CAPTURE_PICTURE | finalRecyclerViewId);
                } else if (which == 1) {
                    Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
                    ((Activity) context).startActivityForResult(intent, AttachmentViewModel.REQUEST_CAPTURE_VIDEO | finalRecyclerViewId);
                }else if (which==2){
                    Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    ((Activity) context).startActivityForResult(intent, AttachmentViewModel.REQUEST_PICK | finalRecyclerViewId);
                }else if(which==3){
                    Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                    intent.setType("*/*");//设置类型，我这里是任意类型，任意后缀的可以这样写。
                    intent.addCategory(Intent.CATEGORY_OPENABLE);

                    try {
                        ((Activity) context).startActivityForResult(intent, AttachmentViewModel.REQUEST_OPEN_FILE | finalRecyclerViewId);
                    } catch (ActivityNotFoundException e) {
                        new AlertDialog.Builder(context).setTitle("提示").setMessage("本机没有安装文件管理器。").setPositiveButton("确定", null).show();
                    }
                }
            }
        }).setTitle("来源").show();
    }

    public void onPreviewClicked(View view){
        Toast.makeText(context, "预览选中的文件。", Toast.LENGTH_SHORT).show();
    }

    public void onRemoveClicked(final View view){
        new AlertDialog.Builder(context).setTitle("确定删除选中的文件吗？").setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                RecyclerView rv = (RecyclerView) view.getParent().getParent();
                SimpleRecycleViewAdapter<AttachmentViewModel> adapter = (SimpleRecycleViewAdapter<AttachmentViewModel>) rv.getAdapter();
                List<AttachmentViewModel> datas = adapter.getData();

                View layout = (View) view.getParent();
                int index = rv.getChildLayoutPosition(layout);

                datas.remove(index);

                adapter.notifyDataSetChanged();
            }
        }).setNegativeButton("取消", null).show();
    }
}
