package kl.law.inspector.vm;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.databinding.ObservableField;
import android.databinding.ObservableInt;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import kl.law.inspector.BR;
import kl.law.inspector.R;
import kl.law.inspector.activity.DocumentApproveActivity;
import kl.law.inspector.activity.DocumentDetailActivity;
import kl.law.inspector.databinding.ActivityDocumentApproveBinding;
import kl.law.inspector.databinding.ActivityDocumentCreateBinding;
import kl.law.inspector.databinding.ActivityDocumentDetailBinding;
import kl.law.inspector.databinding.FragmentDocumentBinding;
import kl.law.inspector.databinding.MemberListItemBinding;
import kl.law.inspector.tools.ApiKit;
import kl.law.inspector.tools.FileKit;
import kl.law.inspector.tools.NetworkAccessKit;
import kl.law.inspector.tools.RefreshRecyclerViewAdapter;
import kl.law.inspector.tools.SimpleRecycleViewAdapter;
import kl.law.inspector.tools.UserData;

/**
 * Created by yinyy on 2017/10/4.
 */

public class DocumentViewModel extends AbstractViewModel<Fragment, FragmentDocumentBinding> {
    public static final int REQUEST_CODE_CREATE = 0x01;
    public static final int REQUEST_CODE_APPROVE = 0x02;

    //private ViewPagerBinding[] viewPagerBindings;
    //private ScrollRefreshStatusModel[] scrollRefreshStatusModels;

    private ScrollRefreshStatusModel scrollRefreshStatusModel;

    public DocumentViewModel(Fragment owner, FragmentDocumentBinding binding) {
        super(owner, binding);
    }

    public void init() {
        scrollRefreshStatusModel = new ScrollRefreshStatusModel();

        DividerItemDecoration decoration = new DividerItemDecoration(context, DividerItemDecoration.VERTICAL);
        decoration.setDrawable(ContextCompat.getDrawable(context, R.drawable.ic_recycle_view_default_decoration));
        binding.recycleView.addItemDecoration(decoration);

        List<ItemViewModel> datas = new ArrayList<>();
        RefreshRecyclerViewAdapter<ItemViewModel> adapter = new RefreshRecyclerViewAdapter<>(datas, R.layout.item_document, R.layout.item_footer_refresh, BR.viewModel, BR.footer);
        binding.recycleView.setAdapter(adapter);

        binding.swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (!scrollRefreshStatusModel.isLoading()) {
                    refreshDocument();
                }
            }
        });
        binding.recycleView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                int visibleCount = recyclerView.getChildCount();
                int childCount = recyclerView.getAdapter().getItemCount();
                int firstVisibleIndex = ((LinearLayoutManager) recyclerView.getLayoutManager()).findFirstVisibleItemPosition();

                if (scrollRefreshStatusModel.hasMoreElements() && !scrollRefreshStatusModel.isLoading() && (firstVisibleIndex + visibleCount >= childCount)) {
                    loadDocument();
                }
            }
        });

        loadDocument();
    }

    public void onResume(){
    }

    public void refreshDocument(){
        RefreshRecyclerViewAdapter adapter = (RefreshRecyclerViewAdapter) binding.recycleView.getAdapter();
        adapter.getFooterViewModel().status.set(RefreshRecyclerViewAdapter.FooterViewModel.STATUS_HAS_MORE_ELEMENTS);

        scrollRefreshStatusModel.setPage(0);
        scrollRefreshStatusModel.setHasMoreElements(true);
        scrollRefreshStatusModel.setLoading(true);
        adapter.getData().clear();
        adapter.notifyDataSetChanged();
        scrollRefreshStatusModel.setLoading(false);

        loadDocument();
    }

    private void loadDocument(){
        if(scrollRefreshStatusModel.isLoading()){
            return ;
        }

        if(!binding.swipeRefreshLayout.isRefreshing()){
            binding.swipeRefreshLayout.setRefreshing(true);
        }

        scrollRefreshStatusModel.setLoading(true);

        final RefreshRecyclerViewAdapter<ItemViewModel> adapter = (RefreshRecyclerViewAdapter<ItemViewModel>) binding.recycleView.getAdapter();
        final List<ItemViewModel> datas = adapter.getData();

        NetworkAccessKit.getData(context, ApiKit.URL_DOCUMENT_LIST(scrollRefreshStatusModel.nextPage(), UserData.getInstance().getId()), new NetworkAccessKit.DefaultCallback<JSONObject>() {

            @Override
            public void onSuccess(JSONObject data) {

                int pagecount = data.optInt("pagecount", 0);
                if (pagecount > scrollRefreshStatusModel.getPage()) {
                    adapter.getFooterViewModel().status.set(RefreshRecyclerViewAdapter.FooterViewModel.STATUS_HAS_MORE_ELEMENTS);
                } else {
                    adapter.getFooterViewModel().status.set(RefreshRecyclerViewAdapter.FooterViewModel.STATUS_NO_MORE_ELEMENTS);
                    scrollRefreshStatusModel.setHasMoreElements(false);
                }

                JSONArray jsonArray = data.optJSONArray("data");
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = jsonArray.optJSONObject(i);
                    ItemViewModel vm = new ItemViewModel();
                    vm.setId(jsonObject.optString("id"));
                    vm.title.set(jsonObject.optString("docTitle"));
                    vm.created.set(jsonObject.optString("created"));

                    int progressCode = Integer.parseInt(jsonObject.optString("dRStage"));
                    vm.progressCode.set(progressCode);
                    if(progressCode==0){
                        vm.progress.set("办公室审批");
                    }else if(progressCode==1){
                        vm.progress.set("领导批示");
                    }else if(progressCode==2){
                        vm.progress.set("公文转送");
                    }else if(progressCode==3){
                        vm.progress.set("公文传阅");
                    }else if(progressCode==4){
                        vm.progress.set("公文归档");
                        vm.setClickAction(ItemViewModel.CLICK_ACTION_DETAIL);
                    }

                    datas.add(vm);
                }

                adapter.notifyDataSetChanged();

                scrollRefreshStatusModel.setLoading(false);
                binding.swipeRefreshLayout.setRefreshing(false);
            }

            @Override
            public void handleFailureAndError(String message) {
                super.handleFailureAndError(message);

                if(!TextUtils.isEmpty(message)) {
                    Toast.makeText(context, message, Toast.LENGTH_LONG).show();
                }

                adapter.getFooterViewModel().status.set(RefreshRecyclerViewAdapter.FooterViewModel.STATUS_NO_MORE_ELEMENTS);
                scrollRefreshStatusModel.setHasMoreElements(false);
                scrollRefreshStatusModel.setLoading(false);
                binding.swipeRefreshLayout.setRefreshing(false);
            }
        });
    }

    public static class ItemViewModel {
        public static final int CLICK_ACTION_APPROVE = 0x01;
        public static final int CLICK_ACTION_DETAIL = 0x02;

        public final ObservableField<String> title = new ObservableField<>();
        public final ObservableField<String> created = new ObservableField<>();
        public final ObservableInt progressCode = new ObservableInt();
        public final ObservableField<String> progress = new ObservableField<>();

        private int clickAction;
        public void setClickAction(int clickAction){
            this.clickAction = clickAction;
        }

        private String id;
        public void setId(String id){
            this.id = id;
        }
        public String getId(){return id;}

        public void onItemClicked(View view){
            if(clickAction==CLICK_ACTION_APPROVE) {
                Intent intent = new Intent(view.getContext(), DocumentApproveActivity.class);
                intent.putExtra("id", id);
                intent.putExtra("progressCode", progressCode.get());
                ((Activity)view.getContext()).startActivityForResult(intent, REQUEST_CODE_APPROVE);
            }else if(clickAction==CLICK_ACTION_DETAIL) {
                Intent intent = new Intent(view.getContext(), DocumentDetailActivity.class);
                intent.putExtra("id", id);
                view.getContext().startActivity(intent);
            }
        }
    }

    public static class CreateViewModel extends AbstractViewModel<Activity, ActivityDocumentCreateBinding>{
        public final ObservableField<String> title = new ObservableField<>();
        public final ObservableField<String> opinion = new ObservableField<>();

        public final ObservableField<SimpleRecycleViewAdapter> fileAdapter = new ObservableField<>();

        public CreateViewModel(Activity owner, ActivityDocumentCreateBinding binding) {
            super(owner, binding);
        }

        public void init(){
            GridLayoutManager layoutManager = new GridLayoutManager(context, 4);
            binding.recyclerView.setLayoutManager(layoutManager);

            List<AttachmentViewModel> datas = new ArrayList<>();
            AttachmentViewModel flivm = new AttachmentViewModel(context);
            flivm.type.set(0);
            flivm.showDelete.set(false);
            datas.add(flivm);

            SimpleRecycleViewAdapter<AttachmentViewModel> adapter = new SimpleRecycleViewAdapter<>(datas,R.layout.item_attachment, BR.viewModel);
            fileAdapter.set(adapter);
        }

        public void onSubmit(View view){
            //获取待上传的文件列表
            List<Map<String, Object>> fileList = new LinkedList<>();

            SimpleRecycleViewAdapter<AttachmentViewModel> adapter = (SimpleRecycleViewAdapter<AttachmentViewModel>) binding.recyclerView.getAdapter();
            List<AttachmentViewModel> datas = adapter.getData();
            for(AttachmentViewModel attachmentViewModel : datas){
                if(attachmentViewModel.type.get()==AttachmentViewModel.TYPE_ITEM){
                    if(attachmentViewModel.localFile.get()!=null){
                        Map<String, Object> m = new HashMap<>();
                        m.put("viewModel", attachmentViewModel);

                        fileList.add(m);
                    }
                }
            }

            if(fileList.size()==0) {
                Toast.makeText(context, "请选择上传的文件。", Toast.LENGTH_LONG).show();
                return;
            }

            view.setEnabled(false);
            postFiles(fileList);
        }

        private void postFiles(final List<Map<String, Object>> fileList){
            if(fileList.size()>0) {
                Map<String, Object> map = fileList.remove(0);
                final AttachmentViewModel viewModel = (AttachmentViewModel) map.get("viewModel");
                File file = viewModel.localFile.get();

                NetworkAccessKit.uploadFile(file, new NetworkAccessKit.DefaultCallback<JSONObject>() {
                    @Override
                    public void onSuccess(JSONObject data) {
                        String remoteUrl = data.optString("path");
                        Log.d("TEST", remoteUrl);

                        viewModel.remoteUrl.set(remoteUrl);
                        viewModel.localFile.set(null);

                        postFiles(fileList);
                    }

                    @Override
                    public void onFailure(int code, String remark) {
                        super.onFailure(code, remark);

                        if(code==CODE_INVALID_FILE_TYPE) {
                            Toast.makeText(context, "不受支持的文件格式。", Toast.LENGTH_LONG).show();
                        }else{
                            Toast.makeText(context, remark, Toast.LENGTH_LONG).show();
                        }

                        postFiles(fileList);
                    }
                }, new NetworkAccessKit.ProgressCallback() {
                    @Override
                    public void onProgress(long bytesWritter, long totalSize) {
                        Log.d("TEST", "写入：" + bytesWritter + ", 总计：" + totalSize);

                        long percent = bytesWritter * 100 / totalSize;

                        viewModel.progress.set(percent);
                    }
                });
            }else{
                saveDocument();
            }
        }

        private void saveDocument() {
            try {
                JSONObject jsonData = new JSONObject();

                SimpleRecycleViewAdapter<AttachmentViewModel> adapter = (SimpleRecycleViewAdapter<AttachmentViewModel>) binding.recyclerView.getAdapter();
                List<AttachmentViewModel> datas = adapter.getData();
                StringBuilder files = new StringBuilder();
                for (AttachmentViewModel data : datas) {
                    if (data.type.get() == AttachmentViewModel.TYPE_ITEM) {
                        if(files.length()>0){
                            files.append(";");
                        }
                        files.append(data.remoteUrl.get());
                    }
                }

                JSONObject documentObject = new JSONObject();
                documentObject.put("files", files.toString());
                documentObject.put("title", title.get());
                documentObject.put("opinion", opinion.get());

                JSONObject userObject = new JSONObject();
                userObject.put("userId", UserData.getInstance().getId());
                userObject.put("officeId", UserData.getInstance().getOfficeId());

                jsonData.put("document", documentObject);
                jsonData.put("user", userObject);

                NetworkAccessKit.postData(context, ApiKit.URL_DOCUMENT_CREATE, jsonData, new NetworkAccessKit.DefaultCallback() {
                    @Override
                    public void onSuccess(Object data) {
                        Toast.makeText(context, "公文创建成功。", Toast.LENGTH_LONG).show();

                        owner.setResult(Activity.RESULT_OK);
                        owner.finish();
                    }

                    @Override
                    public void onFailure(int code, String remark) {
                        Log.d("TEST", remark);
                    }
                });
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    public static class DetailViewModel extends AbstractViewModel<Activity, ActivityDocumentDetailBinding> {
        public final ObservableField<String> title = new ObservableField<>();
        private String id;

        public final ObservableField<SimpleRecycleViewAdapter> fileAdapter = new ObservableField<>();

        public DetailViewModel(Activity owner, ActivityDocumentDetailBinding binding) {
            super(owner, binding);
        }

        public void init(String id, int progressCode) {
            this.id = id;

            NetworkAccessKit.getData(context, ApiKit.URL_DOCUMENT_DETAIL(id), new NetworkAccessKit.DefaultCallback<JSONObject>() {
                @Override
                public void onSuccess(JSONObject data) {
                    title.set(data.optString("docTitle"));

                    List<AttachmentViewModel> datas = new ArrayList<>();
                    String fileStr = data.optString("files");
                    String[] files = fileStr.split(";");
                    for (int i = 0; i < files.length; i++) {
                        String attachment = files[i].trim();
                        if(TextUtils.isEmpty(attachment)){
                            continue;
                        }

                        AttachmentViewModel flivm = new AttachmentViewModel(context);
                        if (FileKit.isBitmap(attachment) || FileKit.isVideo(attachment)) {
                            flivm.imageUrl.set(attachment);
                        } else {
                            flivm.imageRes.set(FileKit.getFileIcon(attachment));
                        }
                        flivm.remoteUrl.set(attachment);
                        flivm.type.set(AttachmentViewModel.TYPE_ITEM);
                        flivm.showDelete.set(false);
                        datas.add(flivm);
                    }

                    SimpleRecycleViewAdapter<AttachmentViewModel> adapter1 = new SimpleRecycleViewAdapter<>(datas, R.layout.item_attachment, BR.viewModel);
                    fileAdapter.set(adapter1);
                }
            });
        }
    }

    public static class ApproveViewModel extends AbstractViewModel<Activity, ActivityDocumentApproveBinding> {
        public final ObservableField<String> title = new ObservableField<>();
        public final ObservableField<String> opinion = new ObservableField<>();
        public final ObservableField<String> opinion1 = new ObservableField<>();
        public final ObservableField<String> opinion2 = new ObservableField<>();

        public final ObservableField<SimpleRecycleViewAdapter> fileAdapter = new ObservableField<>();
        public final ObservableField<SimpleRecycleViewAdapter> approverAdapter = new ObservableField<>();
        public final ObservableField<SimpleRecycleViewAdapter> readerAdapter = new ObservableField<>();

        private String id;

        public ApproveViewModel(Activity owner, ActivityDocumentApproveBinding binding) {
            super(owner, binding);
        }

        public void init(String id) {
            this.id = id;

            NetworkAccessKit.getData(context, ApiKit.URL_DOCUMENT_DETAIL(id), new NetworkAccessKit.DefaultCallback<JSONObject>() {
                @Override
                public void onSuccess(JSONObject data) {
                    title.set(data.optString("docTitle"));
                    opinion.set(data.optString("leaderOptions"));
                    opinion1.set(data.optString("opinion1"));
                    opinion2.set(data.optString("opinion2"));

                    List<AttachmentViewModel> datas = new ArrayList<>();
                    String fileStr = data.optString("files");
                    String[] files = fileStr.split(";");
                    for (int i = 0; i < files.length; i++) {
                        String attachment = files[i].trim();
                        if(TextUtils.isEmpty(attachment)){
                            continue;
                        }

                        AttachmentViewModel flivm = new AttachmentViewModel(context);
                        if (FileKit.isBitmap(attachment) || FileKit.isVideo(attachment)) {
                            flivm.imageUrl.set(attachment);
                        } else {
                            flivm.imageRes.set(FileKit.getFileIcon(attachment));
                        }
                        flivm.remoteUrl.set(attachment);
                        flivm.type.set(AttachmentViewModel.TYPE_ITEM);
                        flivm.showDelete.set(false);
                        datas.add(flivm);
                    }

                    SimpleRecycleViewAdapter<AttachmentViewModel> adapter1 = new SimpleRecycleViewAdapter<>(datas, R.layout.item_attachment, BR.viewModel);
                    fileAdapter.set(adapter1);

                    final int progressCode = Integer.parseInt(data.optString("dRStage"));
                    if(progressCode==0 || progressCode==2){
                        NetworkAccessKit.getData(context, ApiKit.URL_USER_LIST, new NetworkAccessKit.DefaultCallback<JSONArray>() {
                            @Override
                            public void onSuccess(JSONArray data) {
                                List<MemberViewModel> members = new ArrayList<>();
                                for(int i=0;i<data.length();i++) {
                                    JSONObject jsonObject = data.optJSONObject(i);

                                    MemberViewModel mvm = new MemberViewModel();
                                    mvm.setId(jsonObject.optString("id"));
                                    mvm.name.set(MessageFormat.format("{0}", jsonObject.optString("name"), jsonObject.optString("no")));
                                    mvm.selected.set(false);
                                    members.add(mvm);
                                }

                                if(progressCode==0){
                                    SimpleRecycleViewAdapter<MemberViewModel> adapter = new SimpleRecycleViewAdapter<>(members, R.layout.member_list_item, BR.viewModel);
                                    approverAdapter.set(adapter);
                                }else if(progressCode==2){
                                    SimpleRecycleViewAdapter<MemberViewModel> adapter = new SimpleRecycleViewAdapter<>(members, R.layout.member_list_item, BR.viewModel);
                                    readerAdapter.set(adapter);
                                }
                            }
                        });
                    }
                }
            });
        }

        public void onSendToLeaderClicked(View view) {
            String approvalIds = "";
            SimpleRecycleViewAdapter<MemberViewModel> adapter = (SimpleRecycleViewAdapter<MemberViewModel>) binding.approverRecyclerView.getAdapter();
            List<MemberViewModel> datas = adapter.getData();
            for (MemberViewModel vm : datas) {
                if (vm.selected.get()) {
                    if("".equals(approvalIds)){
                        approvalIds = vm.id;
                    }else{
                        approvalIds = approvalIds + ";" + vm.id;
                    }
                }
            }

            if ("".equals(approvalIds)) {
                Toast.makeText(context, "请选择审批人。", Toast.LENGTH_LONG).show();
                return;
            }

            view.setEnabled(false);

            try {
                JSONObject jsonData = new JSONObject();
                JSONObject userObject = new JSONObject();
                userObject.put("userId", UserData.getInstance().getId());
                userObject.put("officeId", UserData.getInstance().getOfficeId());
                jsonData.put("user", userObject);

                JSONObject documentObject = new JSONObject();
                documentObject.put("id", id);
                documentObject.put("progressCode", 1);
                documentObject.put("approvalId", approvalIds);
                documentObject.put("opinion", opinion2.get());
                jsonData.put("document", documentObject);

                NetworkAccessKit.postData(context, ApiKit.URL_DOCUMENT_APPROVE, jsonData, new NetworkAccessKit.DefaultCallback() {
                    @Override
                    public void onSuccess(Object data) {
                        Toast.makeText(context, "公文已发送。", Toast.LENGTH_LONG).show();

                        owner.setResult(Activity.RESULT_OK);
                        owner.finish();
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public void onLeaderFinishClicked(View view) {
            if(TextUtils.isEmpty(opinion.get().trim())){
                Toast.makeText(context, "请输入批示意见。", Toast.LENGTH_LONG).show();
                return ;
            }

            view.setEnabled(false);

            try {
                JSONObject jsonData = new JSONObject();
                JSONObject userObject = new JSONObject();
                userObject.put("userId", UserData.getInstance().getId());
                userObject.put("officeId", UserData.getInstance().getOfficeId());
                jsonData.put("user", userObject);

                JSONObject documentObject = new JSONObject();
                documentObject.put("id", id);
                documentObject.put("progressCode", 2);
                documentObject.put("opinion", opinion.get());
                jsonData.put("document", documentObject);

                NetworkAccessKit.postData(context, ApiKit.URL_DOCUMENT_APPROVE, jsonData, new NetworkAccessKit.DefaultCallback() {
                    @Override
                    public void onSuccess(Object data) {
                        Toast.makeText(context, "公文已批示。", Toast.LENGTH_LONG).show();

                        owner.setResult(Activity.RESULT_OK);
                        owner.finish();
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public void onSendToAllClicked(final View view) {
            final List<String> readers = new LinkedList<>();
            SimpleRecycleViewAdapter<MemberViewModel> adapter = (SimpleRecycleViewAdapter<MemberViewModel>) binding.readerRecyclerView.getAdapter();
            List<MemberViewModel> datas = adapter.getData();
            for (MemberViewModel vm : datas) {
                if (vm.selected.get()) {
                    readers.add(vm.id);
                }
            }

            if(readers.size()==0){
                new AlertDialog.Builder(context).setTitle("提示").setMessage("还未选择传阅人。是否继续？").setNegativeButton("取消", null).setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        view.setEnabled(false);

                        try {
                            JSONObject jsonData = new JSONObject();
                            JSONObject userObject = new JSONObject();
                            userObject.put("userId", UserData.getInstance().getId());
                            userObject.put("officeId", UserData.getInstance().getOfficeId());
                            jsonData.put("user", userObject);

                            JSONObject documentObject = new JSONObject();
                            documentObject.put("id", id);
                            documentObject.put("progressCode", 3);
                            documentObject.put("readerIds", new JSONArray(readers));
                            jsonData.put("document", documentObject);

                            NetworkAccessKit.postData(context, ApiKit.URL_DOCUMENT_APPROVE, jsonData, new NetworkAccessKit.DefaultCallback() {
                                @Override
                                public void onSuccess(Object data) {
                                    Toast.makeText(context, "公文归档。", Toast.LENGTH_LONG).show();

                                    owner.setResult(Activity.RESULT_OK);
                                    owner.finish();
                                }
                            });
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }).show();
            }else{
                view.setEnabled(false);

                try {
                    JSONObject jsonData = new JSONObject();
                    JSONObject userObject = new JSONObject();
                    userObject.put("userId", UserData.getInstance().getId());
                    userObject.put("officeId", UserData.getInstance().getOfficeId());
                    jsonData.put("user", userObject);

                    JSONObject documentObject = new JSONObject();
                    documentObject.put("id", id);
                    documentObject.put("progressCode", 3);
                    documentObject.put("readerIds", new JSONArray(readers));
                    jsonData.put("document", documentObject);

                    NetworkAccessKit.postData(context, ApiKit.URL_DOCUMENT_APPROVE, jsonData, new NetworkAccessKit.DefaultCallback() {
                        @Override
                        public void onSuccess(Object data) {
                            Toast.makeText(context, "公文已开始传阅。", Toast.LENGTH_LONG).show();

                            owner.setResult(Activity.RESULT_OK);
                            owner.finish();
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        public void onReadClicked(View view) {
            view.setEnabled(false);

            try {
                JSONObject jsonData = new JSONObject();
                JSONObject userObject = new JSONObject();
                userObject.put("userId", UserData.getInstance().getId());
                userObject.put("officeId", UserData.getInstance().getOfficeId());
                jsonData.put("user", userObject);

                JSONObject documentObject = new JSONObject();
                documentObject.put("id", id);
                documentObject.put("progressCode", 4);
                jsonData.put("document", documentObject);

                NetworkAccessKit.postData(context, ApiKit.URL_DOCUMENT_APPROVE, jsonData, new NetworkAccessKit.DefaultCallback() {
                    @Override
                    public void onSuccess(Object data) {
                        Toast.makeText(context, "已阅读。", Toast.LENGTH_LONG).show();

                        owner.setResult(Activity.RESULT_OK);
                        owner.finish();
                    }

                    @Override
                    public void onFailure(int code, String remark) {
                        Log.d("TEST", remark);
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static class MemberViewModel {
        public final ObservableField<String> name = new ObservableField<>();
        public final ObservableField<String> imageUrl = new ObservableField<>();
        public final ObservableField<Boolean> selected = new ObservableField<>();

        private String id;
        public void setId(String id){
            this.id = id;
        }

        public void onItemClicked(View view){
            View parentRecyclerView = (View) view.getParent();
            if(parentRecyclerView.getId()==R.id.approverRecyclerView) {
                MemberListItemBinding itemBinding = DataBindingUtil.getBinding(view);
                MemberViewModel current = itemBinding.getViewModel();

//                ActivityDocumentApproveBinding parentBinding = DataBindingUtil.getBinding((View) (view.getParent().getParent().getParent().getParent()));
//                SimpleRecycleViewAdapter<MemberViewModel> adapter = (SimpleRecycleViewAdapter<MemberViewModel>) parentBinding.approverRecyclerView.getAdapter();
//                List<MemberViewModel> datas = adapter.getData();
//                for (MemberViewModel data : datas) {
//                    if (data != current) {
//                        data.selected.set(false);
//                    }
//                }

                current.selected.set(!current.selected.get());

//                adapter.notifyDataSetChanged();
            }else if(parentRecyclerView.getId()==R.id.readerRecyclerView) {
                MemberListItemBinding itemBinding = DataBindingUtil.getBinding(view);
                MemberViewModel current = itemBinding.getViewModel();
                current.selected.set(!current.selected.get());

                ActivityDocumentApproveBinding parentBinding = DataBindingUtil.getBinding((View) (view.getParent().getParent().getParent().getParent()));
                SimpleRecycleViewAdapter<MemberViewModel> adapter = (SimpleRecycleViewAdapter<MemberViewModel>) parentBinding.readerRecyclerView.getAdapter();
                adapter.notifyDataSetChanged();

                int count = 0;
                List<MemberViewModel> datas = adapter.getData();
                for (MemberViewModel mvm : datas) {
                    if (mvm.selected.get()) {
                        count++;
                    }
                }

                if (count > 0) {
                    parentBinding.sendDocButton.setText("传阅公文");
                } else {
                    parentBinding.sendDocButton.setText("公文归档");
                }
            }
        }
    }
}
