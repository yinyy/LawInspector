package kl.law.inspector.vm;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.databinding.ObservableField;
import android.databinding.ObservableInt;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import kl.law.inspector.BR;
import kl.law.inspector.R;
import kl.law.inspector.activity.DocumentDetailActivity;
import kl.law.inspector.databinding.ActivityDocumentCreateBinding;
import kl.law.inspector.databinding.ActivityDocumentDetailBinding;
import kl.law.inspector.databinding.FragmentDocumentBinding;
import kl.law.inspector.databinding.MemberListItemBinding;
import kl.law.inspector.databinding.ViewPagerBinding;
import kl.law.inspector.tools.ApiKit;
import kl.law.inspector.tools.FileKit;
import kl.law.inspector.tools.NetworkAccessKit;
import kl.law.inspector.tools.RefreshRecyclerViewAdapter;
import kl.law.inspector.tools.SimpleRecycleViewAdapter;
import kl.law.inspector.tools.UserData;

/**
 * Created by yinyy on 2017/10/4.
 */

public class DocumentViewModel extends AbstractViewModel<FragmentDocumentBinding> {
    private ViewPagerBinding[] viewPagerBindings;
    private MyScrollListener[] scrollListener;
    private MyRefreshListener[] refreshListeners;

    public DocumentViewModel(Context context, FragmentDocumentBinding binding) {
        super(context, binding);
    }

    public void init() {
        LayoutInflater layoutInflater = ((Activity) context).getLayoutInflater();
        viewPagerBindings = new ViewPagerBinding[]{DataBindingUtil.inflate(layoutInflater, R.layout.view_pager, null, false),
                DataBindingUtil.inflate(layoutInflater, R.layout.view_pager, null, false),
                DataBindingUtil.inflate(layoutInflater, R.layout.view_pager, null, false)};
        scrollListener = new MyScrollListener[]{
                new MyScrollListener(0),
                new MyScrollListener(1),
                new MyScrollListener(2)
        };
        refreshListeners = new MyRefreshListener[]{
                new MyRefreshListener(0),
                new MyRefreshListener(1),
                new MyRefreshListener(2)
        };

        DividerItemDecoration decoration = new DividerItemDecoration(context, DividerItemDecoration.VERTICAL);
        decoration.setDrawable(ContextCompat.getDrawable(context, R.drawable.ic_recycle_view_default_decoration));
        for (ViewPagerBinding b : viewPagerBindings) {
            b.recycleView.addItemDecoration(decoration);
        }

        final View[] views = new View[]{viewPagerBindings[0].getRoot(), viewPagerBindings[1].getRoot(), viewPagerBindings[2].getRoot()};
        ((FragmentDocumentBinding) binding).viewPager.setAdapter(new PagerAdapter() {
            @Override
            public int getCount() {
                return views.length;
            }

            @Override
            public boolean isViewFromObject(View view, Object object) {
                return view == object;
            }

            @Override
            public void destroyItem(ViewGroup container, int position, Object object) {
                container.removeView(views[position]);
            }

            @Override
            public Object instantiateItem(ViewGroup container, int position) {
                container.addView(views[position]);
                return views[position];
            }

            @Override
            public CharSequence getPageTitle(int position) {
                switch (position) {
                    case 0:
                        return "审批中";
                    case 1:
                        return "传阅中";
                    case 2:
                        return "已完成";
                    default:
                        return "其它";
                }
            }
        });
        ((FragmentDocumentBinding) binding).tabLayout.setupWithViewPager(((FragmentDocumentBinding) binding).viewPager);

        for(int index=0;index<views.length;index++){
            List<ItemViewModel> datas = new ArrayList<>();

            RefreshRecyclerViewAdapter<ItemViewModel> adapter = new RefreshRecyclerViewAdapter<>(datas, R.layout.item_document, R.layout.item_footer_refresh, BR.viewModel, BR.footer);
            viewPagerBindings[index].recycleView.setAdapter(adapter);
            viewPagerBindings[index].swipeRefreshLayout.setOnRefreshListener(refreshListeners[index]);
        }
    }

    public void onResume(){
        for(int index=0;index<viewPagerBindings.length;index++){
            viewPagerBindings[index].recycleView.clearOnScrollListeners();

            scrollListener[index].currentPage = 0;
            scrollListener[index].hasMoreElemets = true;

            RefreshRecyclerViewAdapter adapter=(RefreshRecyclerViewAdapter)viewPagerBindings[index].recycleView.getAdapter();
            List<ItemViewModel> datas = adapter.getData();
            datas.clear();

            RefreshRecyclerViewAdapter.FooterViewModel footer = adapter.getFooterViewModel();
            footer.status.set(RefreshRecyclerViewAdapter.FooterViewModel.STATUS_HAS_MORE_ELEMENTS);

            viewPagerBindings[index].recycleView.getAdapter().notifyDataSetChanged();

            viewPagerBindings[index].recycleView.addOnScrollListener(scrollListener[index]);
        }
    }

    private void loadDocuments(final int index){
        String url = ApiKit.URL_DOCUMENT_LIST(scrollListener[index].nextPage(), index+1, UserData.getInstance().getId());

        NetworkAccessKit.getData(context, url, new NetworkAccessKit.DefaultCallback<JSONObject>() {

            @Override
            public void success(JSONObject data) {
                scrollListener[index].clearLoading();

                RefreshRecyclerViewAdapter<ItemViewModel> adapter = (RefreshRecyclerViewAdapter<ItemViewModel>) viewPagerBindings[index].recycleView.getAdapter();
                List<ItemViewModel> datas = adapter.getData();

                int pagecount = data.optInt("pagecount", 0);
                if (pagecount > scrollListener[index].getCurrentPage()) {
                    adapter.getFooterViewModel().status.set(RefreshRecyclerViewAdapter.FooterViewModel.STATUS_HAS_MORE_ELEMENTS);
                } else {
                    adapter.getFooterViewModel().status.set(RefreshRecyclerViewAdapter.FooterViewModel.STATUS_NO_MORE_ELEMENTS);
                    scrollListener[index].setHasMoreElemets(false);
                }

                JSONArray jsonArray = data.optJSONArray("data");
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = jsonArray.optJSONObject(i);
                    ItemViewModel vm = new ItemViewModel();
                    vm.setId(jsonObject.optString("id"));
                    vm.title.set(jsonObject.optString("title"));
                    vm.created.set(jsonObject.optString("created"));
                    vm.progressCode.set(jsonObject.optInt("progressCode"));
                    datas.add(vm);
                }

                adapter.notifyDataSetChanged();
                scrollListener[index].clearLoading();
            }

            @Override
            public void failure(String remark) {
                scrollListener[index].clearLoading();
                Toast.makeText(context, remark, Toast.LENGTH_LONG).show();
            }

            @Override
            public void error(String message) {
                scrollListener[index].clearLoading();
                Toast.makeText(context, message, Toast.LENGTH_LONG).show();
            }
        });
    }

    private class MyScrollListener extends RecyclerView.OnScrollListener {
        private boolean isLoading = false;
        private int index;
        private int currentPage;
        private boolean hasMoreElemets = true;

        public MyScrollListener(int index){
            this.index = index;
            this.currentPage = 0;
        }

        public void clearLoading(){
            this.isLoading = false;
        }

        public int getCurrentPage(){
            return currentPage;
        }

        public int nextPage(){
            currentPage++;

            return currentPage;
        }

        public void setHasMoreElemets(boolean hasMoreElemets){
            this.hasMoreElemets = hasMoreElemets;
        }

        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);

            int visibleCount = recyclerView.getChildCount();
            int childCount = recyclerView.getAdapter().getItemCount();
            int firstVisibleIndex = ((LinearLayoutManager)recyclerView.getLayoutManager()).findFirstVisibleItemPosition();

            if(hasMoreElemets && !isLoading && (firstVisibleIndex+visibleCount>=childCount)){
                isLoading=true;

                loadDocuments(index);
            }
        }
    }

    private class MyRefreshListener implements SwipeRefreshLayout.OnRefreshListener{
        private int index;

        public MyRefreshListener(int index) {
            this.index = index;
        }

        @Override
        public void onRefresh() {
            viewPagerBindings[index].swipeRefreshLayout.setRefreshing(false);

            scrollListener[index].currentPage = 0;
            scrollListener[index].hasMoreElemets = true;

            RefreshRecyclerViewAdapter adapter=(RefreshRecyclerViewAdapter)viewPagerBindings[index].recycleView.getAdapter();
            List<ItemViewModel> datas = adapter.getData();
            datas.clear();

            RefreshRecyclerViewAdapter.FooterViewModel footer = adapter.getFooterViewModel();
            footer.status.set(RefreshRecyclerViewAdapter.FooterViewModel.STATUS_HAS_MORE_ELEMENTS);

            viewPagerBindings[index].recycleView.getAdapter().notifyDataSetChanged();
        }
    }

    public static class ItemViewModel {
        public final ObservableField<String> title = new ObservableField<>();
        public final ObservableField<String> created = new ObservableField<>();
        public final ObservableInt progressCode = new ObservableInt();

        private String id;
        public void setId(String id){
            this.id = id;
        }

        public void onItemClicked(View view){
            Intent intent = new Intent(view.getContext(), DocumentDetailActivity.class);
            intent.putExtra("id", id);
            intent.putExtra("progressCode", progressCode.get());
            view.getContext().startActivity(intent);
        }
    }

    public static class CreateViewModel extends AbstractViewModel<ActivityDocumentCreateBinding>{
        public final ObservableField<String> title = new ObservableField<>();

        public final ObservableField<SimpleRecycleViewAdapter> fileAdapter = new ObservableField<>();

        public CreateViewModel(Context context, ActivityDocumentCreateBinding binding) {
            super(context, binding);
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
            Toast.makeText(context,"保存公文。", Toast.LENGTH_LONG).show();
            ((Activity)context).finish();
        }
    }

    public static class DetailViewModel extends AbstractViewModel<ActivityDocumentDetailBinding> {
        public final ObservableField<String> title = new ObservableField<>();
        public final ObservableInt progressCode = new ObservableInt();
        public final ObservableField<String> approverOpinion = new ObservableField<>();

        public final ObservableField<SimpleRecycleViewAdapter> fileAdapter = new ObservableField<>();
        public final ObservableField<SimpleRecycleViewAdapter> approverAdapter = new ObservableField<>();
        public final ObservableField<SimpleRecycleViewAdapter> readerAdapter = new ObservableField<>();

        public DetailViewModel(Context context, ActivityDocumentDetailBinding binding) {
            super(context, binding);
        }

        public void init(String id) {
            progressCode.set(4);
            title.set("xxx事件");

            List<AttachmentViewModel> datas = new ArrayList<>();
            for(int i=0;i<10;i++) {
                String attachment = "http://img1.gtimg.com/cq/pics/hv1/140/248/1766/114897530.jpg";
                AttachmentViewModel flivm = new AttachmentViewModel(context);
                if(FileKit.isBitmap(attachment) || FileKit.isVideo(attachment)){
                    flivm.imageUrl.set(attachment);
                }else{
                    flivm.imageRes.set(FileKit.getFileIcon(attachment));
                }
                flivm.type.set(AttachmentViewModel.TYPE_ITEM);
                flivm.id.set(i+"");
                flivm.showDelete.set(false);
                datas.add(flivm);
            }

            SimpleRecycleViewAdapter<AttachmentViewModel> adapter1 = new SimpleRecycleViewAdapter<>(datas, R.layout.item_attachment, BR.viewModel);
            fileAdapter.set(adapter1);

            List<MemberViewModel> ps = new ArrayList<>();
            for(int i=1;i<=10;i++) {
                MemberViewModel mvm = new MemberViewModel();
                mvm.setId(i+"");
                mvm.name.set("审核" + i);
                mvm.selected.set(false);
                ps.add(mvm);
            }

            SimpleRecycleViewAdapter<MemberViewModel> adapter2 = new SimpleRecycleViewAdapter<>(ps, R.layout.member_list_item, BR.viewModel);
            approverAdapter.set(adapter2);

            ps = new ArrayList<>();
            for(int i=1;i<=10;i++) {
                MemberViewModel mvm = new MemberViewModel();
                mvm.setId(i+"");
                mvm.name.set("阅读" + i);
                mvm.selected.set(false);
                ps.add(mvm);
            }

            SimpleRecycleViewAdapter<MemberViewModel> adapter3 = new SimpleRecycleViewAdapter<>(ps, R.layout.member_list_item, BR.viewModel);
            readerAdapter.set(adapter3);
        }

        public void inittttt(String id, int progressCode) {
            this.progressCode.set(progressCode);
            title.set("xxx事件");
            approverOpinion.set("这里是审批人的意见。将要发送给张三、李四、王五、赵六传阅。");

            List<AttachmentViewModel> datas = new ArrayList<>();
            for(int i=0;i<10;i++) {
                String attachment = "http://img1.gtimg.com/cq/pics/hv1/140/248/1766/114897530.jpg";
                AttachmentViewModel flivm = new AttachmentViewModel(context);
                if(FileKit.isBitmap(attachment) || FileKit.isVideo(attachment)){
                    flivm.imageUrl.set(attachment);
                }else{
                    flivm.imageRes.set(FileKit.getFileIcon(attachment));
                }
                flivm.type.set(AttachmentViewModel.TYPE_ITEM);
                flivm.id.set(i+"");
                flivm.showDelete.set(false);
                datas.add(flivm);
            }

            SimpleRecycleViewAdapter<AttachmentViewModel> adapter1 = new SimpleRecycleViewAdapter<>(datas, R.layout.item_attachment, BR.viewModel);
            fileAdapter.set(adapter1);

            List<MemberViewModel> ps = new ArrayList<>();
            for(int i=1;i<=10;i++) {
                MemberViewModel mvm = new MemberViewModel();
                mvm.setId(i+"");
                mvm.name.set("审核" + i);
                mvm.selected.set(false);
                ps.add(mvm);
            }

            SimpleRecycleViewAdapter<MemberViewModel> adapter2 = new SimpleRecycleViewAdapter<>(ps, R.layout.member_list_item, BR.viewModel);
            approverAdapter.set(adapter2);

            ps = new ArrayList<>();
            for(int i=1;i<=10;i++) {
                MemberViewModel mvm = new MemberViewModel();
                mvm.setId(i+"");
                mvm.name.set("阅读" + i);
                mvm.selected.set(false);
                ps.add(mvm);
            }

            SimpleRecycleViewAdapter<MemberViewModel> adapter3 = new SimpleRecycleViewAdapter<>(ps, R.layout.member_list_item, BR.viewModel);
            readerAdapter.set(adapter3);
        }

        public void onSendClicked(View view){
            Toast.makeText(view.getContext(), "发送", Toast.LENGTH_LONG).show();
        }

        public void onFinishClicked(View view){
            Toast.makeText(view.getContext(), "完成", Toast.LENGTH_LONG).show();
        }

        public void onReadClicked(View view){
            Toast.makeText(view.getContext(), "已阅读", Toast.LENGTH_LONG).show();
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

                ActivityDocumentDetailBinding parentBinding = DataBindingUtil.getBinding((View) (view.getParent().getParent().getParent().getParent()));
                SimpleRecycleViewAdapter<MemberViewModel> adapter = (SimpleRecycleViewAdapter<MemberViewModel>) parentBinding.approverRecyclerView.getAdapter();
                List<MemberViewModel> datas = adapter.getData();
                for (MemberViewModel data : datas) {
                    if (data != current) {
                        data.selected.set(false);
                    }
                }

                current.selected.set(!current.selected.get());

                adapter.notifyDataSetChanged();
            }else if(parentRecyclerView.getId()==R.id.readerRecyclerView){
                MemberListItemBinding itemBinding = DataBindingUtil.getBinding(view);
                MemberViewModel current = itemBinding.getViewModel();
                current.selected.set(!current.selected.get());

                ActivityDocumentDetailBinding parentBinding = DataBindingUtil.getBinding((View) (view.getParent().getParent().getParent().getParent()));
                SimpleRecycleViewAdapter<List<MemberViewModel>> adapter = (SimpleRecycleViewAdapter<List<MemberViewModel>>) parentBinding.readerRecyclerView.getAdapter();
                adapter.notifyDataSetChanged();
            }
        }
    }
}
