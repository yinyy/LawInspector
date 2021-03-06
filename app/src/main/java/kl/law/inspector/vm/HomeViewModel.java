package kl.law.inspector.vm;

import android.app.Activity;
import android.databinding.DataBindingUtil;
import android.databinding.ObservableField;
import android.databinding.ObservableInt;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.PagerAdapter;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import kl.law.inspector.BR;
import kl.law.inspector.R;
import kl.law.inspector.databinding.FragmentHomeBinding;
import kl.law.inspector.databinding.ItemNotificationBinding;
import kl.law.inspector.databinding.ItemToDoBinding;
import kl.law.inspector.databinding.ViewPagerBinding;
import kl.law.inspector.tools.ApiKit;
import kl.law.inspector.tools.NetworkAccessKit;
import kl.law.inspector.tools.RefreshRecyclerViewAdapter;
import kl.law.inspector.tools.UserData;

/**
 * Created by yinyy on 2017/9/7.
 */

public class HomeViewModel extends AbstractViewModel<Fragment, FragmentHomeBinding>{
    private ViewPagerBinding[] viewPagerBindings;
    private SwipeRefreshLayout.OnRefreshListener[] onRefreshListener;
    private Handler importanceNotificationHandler;
    private ScrollRefreshStatusModel[] scrollRefreshStatusModels;

    public HomeViewModel(Fragment owner, final FragmentHomeBinding binding) {
        super(owner, binding);

        importanceNotificationHandler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                int index = 0;
                for (index = 0; index < binding.notificationContainer.getChildCount(); index++) {
                    if (binding.notificationContainer.getChildAt(index).getVisibility() == View.VISIBLE) {
                        break;
                    }
                }

                final int nextIndex = (index + 1) % binding.notificationContainer.getChildCount();

                Animation animation1 = new AlphaAnimation(1.0f, 0.0f);
                animation1.setDuration(1000);
                binding.notificationContainer.getChildAt(index).startAnimation(animation1);

                final int finalIndex = index;
                animation1.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        binding.notificationContainer.getChildAt(finalIndex).setVisibility(View.GONE);
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });

                Animation animation2 = new AlphaAnimation(0.0f, 1.0f);
                animation2.setDuration(1000);
                binding.notificationContainer.getChildAt(nextIndex).startAnimation(animation2);
                animation2.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {
                        binding.notificationContainer.getChildAt(nextIndex).setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        //relativeLayout.getChildAt(nextIndex).setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });

                sendEmptyMessageDelayed(0, 5000);
            }
        };
    }

    public void init() {
        LayoutInflater inflater = ((Activity) context).getLayoutInflater();
        viewPagerBindings = new ViewPagerBinding[]{
                DataBindingUtil.inflate(inflater, R.layout.view_pager, null, false),
                DataBindingUtil.inflate(inflater, R.layout.view_pager, null, false),
                DataBindingUtil.inflate(inflater, R.layout.view_pager, null, false)
        };

        onRefreshListener = new SwipeRefreshLayout.OnRefreshListener[viewPagerBindings.length];
        for (int index = 0; index < viewPagerBindings.length; index++) {
            final int finalIndex = index;
            onRefreshListener[index] = new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    if(!scrollRefreshStatusModels[finalIndex].isLoading()) {
                        if (finalIndex == 0) {
                            loadLegalCase();
                        } else if (finalIndex == 1) {
                            loadDocument();
                        } else if (finalIndex == 2) {
                            loadReminder();
                        }
                    }
                }
            };
        }

        final View[] views = new View[viewPagerBindings.length];
        for (int index = 0; index < viewPagerBindings.length; index++) {
            views[index] = viewPagerBindings[index].getRoot();
        }

        binding.viewPager.setAdapter(new PagerAdapter() {
            @Override
            public int getCount() {
                return viewPagerBindings.length;
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
        });
        binding.tabLayout.setupWithViewPager(binding.viewPager);

        int[] icons = {R.drawable.ic_legal_case_green, R.drawable.ic_document_blue, R.drawable.ic_remind_yellow};
        String[] titles = {"行政执法", "公文流转", "计划任务"};
        for (int index = 0; index < viewPagerBindings.length; index++) {
            ItemToDoBinding todoBinding = DataBindingUtil.inflate(inflater, R.layout.item_to_do, null, false);
            TodoViewModel todoViewModel = new TodoViewModel();
            todoViewModel.picture.set(icons[index]);
            todoViewModel.title.set(titles[index]);
            todoViewModel.count.set(0);
            todoBinding.setViewModel(todoViewModel);

            TabLayout.Tab tab = binding.tabLayout.getTabAt(index);
            tab.setCustomView(todoBinding.getRoot());
        }
        binding.tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                int index = tab.getPosition();
                if(scrollRefreshStatusModels[index].isFirst()) {
                    if (index == 0) {
                        loadLegalCase();
                    } else if (index == 1) {
                        loadDocument();
                    } else if (index == 2) {
                        loadReminder();
                    }
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        List<LegalCaseViewModel.ItemViewModel> datasLegalCase = new LinkedList<>();
        RefreshRecyclerViewAdapter<LegalCaseViewModel.ItemViewModel> adapterLegalCase = new RefreshRecyclerViewAdapter<>(datasLegalCase, R.layout.item_legal_case, R.layout.item_footer_refresh, BR.viewModel, BR.footer);
        adapterLegalCase.getFooterViewModel().status.set(RefreshRecyclerViewAdapter.FooterViewModel.STATUS_NO_MORE_ELEMENTS);
        viewPagerBindings[0].recycleView.setAdapter(adapterLegalCase);
        viewPagerBindings[0].swipeRefreshLayout.setOnRefreshListener(onRefreshListener[0]);


        List<DocumentViewModel.ItemViewModel> datasDocument = new LinkedList<>();
        RefreshRecyclerViewAdapter<DocumentViewModel.ItemViewModel> adapterDocument = new RefreshRecyclerViewAdapter<>(datasDocument, R.layout.item_document, R.layout.item_footer_refresh, BR.viewModel, BR.footer);
        adapterDocument.getFooterViewModel().status.set(RefreshRecyclerViewAdapter.FooterViewModel.STATUS_NO_MORE_ELEMENTS);
        viewPagerBindings[1].recycleView.setAdapter(adapterDocument);
        viewPagerBindings[1].swipeRefreshLayout.setOnRefreshListener(onRefreshListener[1]);

        List<LegalCaseViewModel.ItemViewModel> datasReminder = new LinkedList<>();
        RefreshRecyclerViewAdapter<LegalCaseViewModel.ItemViewModel> adapterReminder = new RefreshRecyclerViewAdapter<>(datasReminder, R.layout.item_reminder, R.layout.item_footer_refresh, BR.viewModel, BR.footer);
        adapterReminder.getFooterViewModel().status.set(RefreshRecyclerViewAdapter.FooterViewModel.STATUS_NO_MORE_ELEMENTS);
        viewPagerBindings[2].recycleView.setAdapter(adapterReminder);
        viewPagerBindings[2].swipeRefreshLayout.setOnRefreshListener(onRefreshListener[2]);

        scrollRefreshStatusModels = new ScrollRefreshStatusModel[viewPagerBindings.length];
        for(int index = 0; index< scrollRefreshStatusModels.length; index++){
            scrollRefreshStatusModels[index] = new ScrollRefreshStatusModel();
            scrollRefreshStatusModels[index].setLoading(false);
        }

        loadNotifications();

        if(scrollRefreshStatusModels[0].isFirst()) {
            loadLegalCase();
        }
    }

    private void loadNotifications() {
        binding.notificationContainer.removeAllViews();
        importanceNotificationHandler.removeMessages(0);

        NetworkAccessKit.getData(context, ApiKit.URL_ARTICLE(ApiKit.ArticleCategory.NOTIFICATION), new NetworkAccessKit.DefaultCallback<JSONArray>() {
            @Override
            public void onSuccess(JSONArray array) {
                if (array == null) {
                    return;
                }

                try {
                    for (int i = 0; i < array.length(); i++) {
                        JSONObject jobj = array.getJSONObject(i);
                        Map<String, String> map = new HashMap<>();
                        map.put("id", jobj.getString("id"));
                        map.put("title", jobj.getString("title"));
                        map.put("description", jobj.getString("description"));

                        ItemNotificationBinding inb = DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.item_notification, null, false);
                        inb.setNotification(map);
                        inb.executePendingBindings();

                        binding.notificationContainer.addView(inb.getRoot());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                if (binding.notificationContainer.getChildCount() > 0) {
                    binding.notificationContainer.getChildAt(0).setVisibility(View.VISIBLE);
                }

                if (binding.notificationContainer.getChildCount() >= 2) {
                    importanceNotificationHandler.sendEmptyMessageDelayed(0, 5000);
                }
            }
        });
    }

    public void loadLegalCase(){
        if(scrollRefreshStatusModels[0].isLoading()){
            return ;
        }

        if(!viewPagerBindings[0].swipeRefreshLayout.isRefreshing()){
            viewPagerBindings[0].swipeRefreshLayout.setRefreshing(true);
        }

        scrollRefreshStatusModels[0].setLoading(true);

        final RefreshRecyclerViewAdapter<LegalCaseViewModel.ItemViewModel> adapter = (RefreshRecyclerViewAdapter<LegalCaseViewModel.ItemViewModel>) viewPagerBindings[0].recycleView.getAdapter();
        adapter.getFooterViewModel().status.set(RefreshRecyclerViewAdapter.FooterViewModel.STATUS_HAS_MORE_ELEMENTS);
        final List<LegalCaseViewModel.ItemViewModel> datas = adapter.getData();
        datas.clear();
        adapter.notifyDataSetChanged();

        final ItemToDoBinding todoBinding = DataBindingUtil.getBinding(binding.tabLayout.getTabAt(0).getCustomView());
        final TodoViewModel todoViewModel = todoBinding.getViewModel();

        NetworkAccessKit.getData(context, ApiKit.URL_LEGAL_CASE_TODO_LIST(UserData.getInstance().getId()), new NetworkAccessKit.DefaultCallback<JSONArray>() {
            @Override
            public void onSuccess(JSONArray jsonArray) {
                if(jsonArray!=null) {
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jsonObject = jsonArray.optJSONObject(i);
                        LegalCaseViewModel.ItemViewModel vm = new LegalCaseViewModel.ItemViewModel();
                        vm.setId(jsonObject.optString("id"));
                        vm.picture.set(jsonObject.optString("caseThumbnails"));
                        vm.title.set(jsonObject.optString("title"));

                        String description = jsonObject.optString("caseDescription");
                        vm.subtitle.set(description.length() > 20 ? description.substring(0, 20) : description);

                        vm.serial.set(jsonObject.optString("caseDocNo"));

                        String step = jsonObject.optString("step");
                        String[] values= step.split(",");

                        vm.progress.set(MessageFormat.format("{0} - {1}", values[2], values[3]));
                        vm.progressCode.set(Integer.parseInt(MessageFormat.format("{0}{1}", values[0], values[1])));

                        //TODO:剩余天数
                        //vm.remainder.set(MessageFormat.format("{0}{1}", values[0], values[1]));


                        if(vm.progressCode.get()/10==2){
                            vm.setClickAction(LegalCaseViewModel.ItemViewModel.CLICK_ACTION_FILE);
                        }else {
                            vm.setClickAction(LegalCaseViewModel.ItemViewModel.CLICK_ACTION_APPROVE);
                        }

                        datas.add(vm);
                    }
                }

                adapter.getFooterViewModel().status.set(RefreshRecyclerViewAdapter.FooterViewModel.STATUS_NO_MORE_ELEMENTS);

                scrollRefreshStatusModels[0].setLoading(false);
                viewPagerBindings[0].swipeRefreshLayout.setRefreshing(false);
            }

            @Override
            public void handleFailureAndError(String message) {
                super.handleFailureAndError(message);

                if(!TextUtils.isEmpty(message)) {
                    Toast.makeText(context, message, Toast.LENGTH_LONG).show();
                }

                adapter.getFooterViewModel().status.set(RefreshRecyclerViewAdapter.FooterViewModel.STATUS_NO_MORE_ELEMENTS);
                scrollRefreshStatusModels[0].setLoading(false);
                viewPagerBindings[0].swipeRefreshLayout.setRefreshing(false);
            }
        });
    }

    public void loadDocument(){
        if(scrollRefreshStatusModels[1].isLoading()){
            return ;
        }

        if(!viewPagerBindings[1].swipeRefreshLayout.isRefreshing()){
            viewPagerBindings[1].swipeRefreshLayout.setRefreshing(true);
        }

        scrollRefreshStatusModels[1].setLoading(true);

        final RefreshRecyclerViewAdapter<DocumentViewModel.ItemViewModel> adapter = (RefreshRecyclerViewAdapter<DocumentViewModel.ItemViewModel>) viewPagerBindings[1].recycleView.getAdapter();
        adapter.getFooterViewModel().status.set(RefreshRecyclerViewAdapter.FooterViewModel.STATUS_HAS_MORE_ELEMENTS);
        final List<DocumentViewModel.ItemViewModel> datas = adapter.getData();
        datas.clear();
        adapter.notifyDataSetChanged();

        final ItemToDoBinding todoBinding = DataBindingUtil.getBinding(binding.tabLayout.getTabAt(1).getCustomView());
        final TodoViewModel todoViewModel = todoBinding.getViewModel();

        NetworkAccessKit.getData(context, ApiKit.URL_DOCUMENT_TODO_LIST(UserData.getInstance().getId()), new NetworkAccessKit.DefaultCallback<JSONObject>() {
            @Override
            public void onSuccess(JSONObject data) {
                if(data!=null) {
                    //todoViewModel.count.set(jsonArray.length());

                    JSONArray jsonArray = data.optJSONArray("data");
                    for (int i = 0; i < jsonArray.length(); i++) {
                        try {
                            JSONObject jsonObject = jsonArray.getJSONObject(i);

                            DocumentViewModel.ItemViewModel vm = new DocumentViewModel.ItemViewModel();
                            vm.setId(jsonObject.optString("id"));
                            vm.title.set(jsonObject.optString("docTitle"));
                            vm.created.set("公文创建时间");

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
                            }

                            vm.setClickAction(DocumentViewModel.ItemViewModel.CLICK_ACTION_APPROVE);

                            datas.add(vm);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }

                adapter.getFooterViewModel().status.set(RefreshRecyclerViewAdapter.FooterViewModel.STATUS_NO_MORE_ELEMENTS);
                adapter.notifyDataSetChanged();

                scrollRefreshStatusModels[1].setLoading(false);
                viewPagerBindings[1].swipeRefreshLayout.setRefreshing(false);
            }

            @Override
            public void handleFailureAndError(String message) {
                super.handleFailureAndError(message);

                if(!TextUtils.isEmpty(message)) {
                    Toast.makeText(context, message, Toast.LENGTH_LONG).show();
                }

                adapter.getFooterViewModel().status.set(RefreshRecyclerViewAdapter.FooterViewModel.STATUS_NO_MORE_ELEMENTS);
                scrollRefreshStatusModels[1].setLoading(false);
                viewPagerBindings[1].swipeRefreshLayout.setRefreshing(false);
            }
        });
    }

    private void loadReminder(){
        if(scrollRefreshStatusModels[2].isLoading()){
            return;
        }

        if(!viewPagerBindings[2].swipeRefreshLayout.isRefreshing()){
            viewPagerBindings[2].swipeRefreshLayout.setRefreshing(true);
        }

        scrollRefreshStatusModels[2].setLoading(true);

        final RefreshRecyclerViewAdapter<LegalCaseViewModel.ItemViewModel> adapter = (RefreshRecyclerViewAdapter<LegalCaseViewModel.ItemViewModel>) viewPagerBindings[2].recycleView.getAdapter();
        adapter.getFooterViewModel().status.set(RefreshRecyclerViewAdapter.FooterViewModel.STATUS_HAS_MORE_ELEMENTS);
        final List<LegalCaseViewModel.ItemViewModel> datas = adapter.getData();
        datas.clear();
        adapter.notifyDataSetChanged();

        final ItemToDoBinding todoBinding = DataBindingUtil.getBinding(binding.tabLayout.getTabAt(2).getCustomView());
        final TodoViewModel todoViewModel = todoBinding.getViewModel();

        NetworkAccessKit.getData(context, ApiKit.URL_REMINDER_TODO_LIST(UserData.getInstance().getId()), new NetworkAccessKit.DefaultCallback<JSONObject>() {
            @Override
            public void onSuccess(JSONObject data) {
                if(data!=null) {
                    JSONArray jsonArray = data.optJSONArray("data");
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jsonObject = jsonArray.optJSONObject(i);
                        LegalCaseViewModel.ItemViewModel vm = new LegalCaseViewModel.ItemViewModel();
                        vm.setId(jsonObject.optString("id"));
                        vm.title.set(jsonObject.optString("title"));
                        vm.remainder.set(jsonObject.optString("planRemark"));

                        vm.setClickAction(LegalCaseViewModel.ItemViewModel.CLICK_ACTION_DETAIL);

                        datas.add(vm);
                    }
                }

                adapter.getFooterViewModel().status.set(RefreshRecyclerViewAdapter.FooterViewModel.STATUS_NO_MORE_ELEMENTS);
                adapter.notifyDataSetChanged();

                scrollRefreshStatusModels[2].setLoading(false);
                viewPagerBindings[2].swipeRefreshLayout.setRefreshing(false);
            }

            @Override
            public void handleFailureAndError(String message) {
                super.handleFailureAndError(message);

                if(!TextUtils.isEmpty(message)) {
                    Toast.makeText(context, message, Toast.LENGTH_LONG).show();
                }

                adapter.getFooterViewModel().status.set(RefreshRecyclerViewAdapter.FooterViewModel.STATUS_NO_MORE_ELEMENTS);
                scrollRefreshStatusModels[2].setLoading(false);
                viewPagerBindings[2].swipeRefreshLayout.setRefreshing(false);
            }
        });
    }

    public static class TodoViewModel {
        public final ObservableInt picture = new ObservableInt();
        public final ObservableField<String> title = new ObservableField<>();
        public final ObservableInt count = new ObservableInt();
    }
}
