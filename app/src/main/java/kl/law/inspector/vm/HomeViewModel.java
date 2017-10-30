package kl.law.inspector.vm;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.databinding.ObservableField;
import android.databinding.ObservableInt;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;

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
import kl.law.inspector.tools.ApiKit;
import kl.law.inspector.tools.NetworkAccessKit;
import kl.law.inspector.tools.RefreshRecyclerViewAdapter;
import kl.law.inspector.tools.SimpleRecycleViewAdapter;
import kl.law.inspector.tools.UserData;

/**
 * Created by yinyy on 2017/9/7.
 */

public class HomeViewModel extends AbstractViewModel<FragmentHomeBinding>{

    public HomeViewModel(Context context, FragmentHomeBinding binding) {
        super(context, binding);
    }

    public void init(){
        DividerItemDecoration decoration = new DividerItemDecoration(context, DividerItemDecoration.VERTICAL);
        decoration.setDrawable(ContextCompat.getDrawable(context, R.drawable.ic_recycle_view_default_decoration));
        binding.legalCaseTodoListRecyclerView.addItemDecoration(decoration);
        binding.documentTodoListRecyclerView.addItemDecoration(decoration);
        binding.reminderTodoListRecyclerView.addItemDecoration(decoration);

        fetchNotifications();
        initTodo();

        initTodoList();

        fetchLegalCaseTodoList();
        fetchDocumentTodoList();
        fetchReminderTodoList();
    }

    public void onResume(){
        fetchLegalCaseTodoList();
        fetchDocumentTodoList();
        fetchReminderTodoList();
    }
    private void fetchNotifications() {
        NetworkAccessKit.getData(context, ApiKit.URL_ARTICLE(ApiKit.ArticleCategory.NOTIFICATION), new NetworkAccessKit.DefaultCallback<JSONArray>() {
            @Override
            public void success(JSONArray array) {
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
                    final Handler handler = new Handler() {
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

                    handler.sendEmptyMessageDelayed(0, 5000);
                }
            }
        });
    }

    private void initTodo(){
        GridLayoutManager layoutManager = new GridLayoutManager(context, 3);
        binding.todoRecycclerView.setLayoutManager(layoutManager);

        List<TodoViewModel> datas = new LinkedList<>();
        TodoViewModel data = new TodoViewModel(this);
        data.picture.set(R.drawable.ic_legal_case_green);
        data.title.set("行政执法");
        data.count.set(0);
        datas.add(data);

        data = new TodoViewModel(this);
        data.picture.set(R.drawable.ic_document_blue);
        data.title.set("公文流转");
        data.count.set(0);
        datas.add(data);

        data = new TodoViewModel(this);
        data.picture.set(R.drawable.ic_remind_yellow);
        data.title.set("待办提醒");
        data.count.set(0);
        datas.add(data);

        SimpleRecycleViewAdapter<TodoViewModel> adapter = new SimpleRecycleViewAdapter<>(datas, R.layout.item_to_do, BR.viewModel);
        binding.todoRecycclerView.setAdapter(adapter);
    }

    private void initTodoList(){
        List<LegalCaseViewModel.ItemViewModel> datasLegalCase = new LinkedList<>();
        RefreshRecyclerViewAdapter<LegalCaseViewModel.ItemViewModel> adapterLegalCase = new RefreshRecyclerViewAdapter<>(datasLegalCase, R.layout.item_legal_case, R.layout.item_footer_refresh, BR.viewModel, BR.footer);
        adapterLegalCase.getFooterViewModel().status.set(RefreshRecyclerViewAdapter.FooterViewModel.STATUS_NO_MORE_ELEMENTS);
        binding.legalCaseTodoListRecyclerView.setAdapter(adapterLegalCase);

        List<DocumentViewModel.ItemViewModel> datasDocument = new LinkedList<>();
        RefreshRecyclerViewAdapter<DocumentViewModel.ItemViewModel> adapterDocument = new RefreshRecyclerViewAdapter<>(datasDocument, R.layout.item_document, R.layout.item_footer_refresh, BR.viewModel, BR.footer);
        adapterDocument.getFooterViewModel().status.set(RefreshRecyclerViewAdapter.FooterViewModel.STATUS_NO_MORE_ELEMENTS);
        binding.documentTodoListRecyclerView.setAdapter(adapterDocument);

        List<DocumentViewModel.ItemViewModel> datasReminder = new LinkedList<>();
        RefreshRecyclerViewAdapter<DocumentViewModel.ItemViewModel> adapterReminder = new RefreshRecyclerViewAdapter<>(datasReminder, R.layout.item_document, R.layout.item_footer_refresh, BR.viewModel, BR.footer);
        adapterReminder.getFooterViewModel().status.set(RefreshRecyclerViewAdapter.FooterViewModel.STATUS_NO_MORE_ELEMENTS);
        binding.reminderTodoListRecyclerView.setAdapter(adapterReminder);
    }

    private void fetchLegalCaseTodoList(){
        final SimpleRecycleViewAdapter<TodoViewModel> todoAdapter = (SimpleRecycleViewAdapter<TodoViewModel>) binding.todoRecycclerView.getAdapter();
        final List<TodoViewModel> todoDatas = todoAdapter.getData();

        final RefreshRecyclerViewAdapter<LegalCaseViewModel.ItemViewModel> adapter = (RefreshRecyclerViewAdapter<LegalCaseViewModel.ItemViewModel>) binding.legalCaseTodoListRecyclerView.getAdapter();
        adapter.getFooterViewModel().status.set(RefreshRecyclerViewAdapter.FooterViewModel.STATUS_HAS_MORE_ELEMENTS);
        final List<LegalCaseViewModel.ItemViewModel> datas = adapter.getData();
        datas.clear();

        NetworkAccessKit.getData(context, ApiKit.URL_LEGAL_CASE_TODO_LIST(UserData.getInstance().getId()), new NetworkAccessKit.DefaultCallback<JSONArray>() {
            @Override
            public void success(JSONArray jsonArray) {
                if(jsonArray!=null) {
                    todoDatas.get(0).count.set(jsonArray.length());
                    todoAdapter.notifyDataSetChanged();

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
                        vm.remainder.set(MessageFormat.format("{0}{1}", values[0], values[1]));


                        if(vm.progressCode.get()/10==2){
                            vm.setClickAction(LegalCaseViewModel.ItemViewModel.CLICK_ACTION_FILE);
                        }else {
                            vm.setClickAction(LegalCaseViewModel.ItemViewModel.CLICK_ACTION_APPROVE);
                        }

                        datas.add(vm);
                    }
                }

                adapter.getFooterViewModel().status.set(RefreshRecyclerViewAdapter.FooterViewModel.STATUS_NO_MORE_ELEMENTS);
                adapter.notifyDataSetChanged();
            }
        });
    }

    private void fetchDocumentTodoList(){
        final SimpleRecycleViewAdapter<TodoViewModel> todoAdapter = (SimpleRecycleViewAdapter<TodoViewModel>) binding.todoRecycclerView.getAdapter();
        final List<TodoViewModel> todoDatas = todoAdapter.getData();

        final RefreshRecyclerViewAdapter<DocumentViewModel.ItemViewModel> adapter = (RefreshRecyclerViewAdapter<DocumentViewModel.ItemViewModel>) binding.documentTodoListRecyclerView.getAdapter();
        adapter.getFooterViewModel().status.set(RefreshRecyclerViewAdapter.FooterViewModel.STATUS_HAS_MORE_ELEMENTS);
        final List<DocumentViewModel.ItemViewModel> datas = adapter.getData();
        datas.clear();

        NetworkAccessKit.getData(context, ApiKit.URL_DOCUMENT_TODO_LIST(UserData.getInstance().getId()), new NetworkAccessKit.DefaultCallback<JSONArray>() {
            @Override
            public void success(JSONArray jsonArray) {
                //TODO:后期打开
//                if(jsonArray!=null) {
//                    todoDatas.get(1).count.set(jsonArray.length());
//                    todoAdapter.notifyDataSetChanged();
//
//                    for (int i = 0; i < jsonArray.length(); i++) {
//                        try {
//                            JSONObject jsonObject = jsonArray.getJSONObject(i);
//
//                            DocumentViewModel.ItemViewModel vm = new DocumentViewModel.ItemViewModel();
//                            vm.setId(i + "");
//                            vm.title.set("关于某某事情的决定" + i);
//                            vm.created.set("2017/10/05");
//                            vm.progressCode.set(i);
//
//                            datas.add(vm);
//                        } catch (Exception e) {
//                            e.printStackTrace();
//                        }
//                    }
//                }
//
//                adapter.getFooterViewModel().status.set(RefreshRecyclerViewAdapter.FooterViewModel.STATUS_NO_MORE_ELEMENTS);
//                adapter.notifyDataSetChanged();
            }
        });
    }

    private void fetchReminderTodoList(){
        final SimpleRecycleViewAdapter<TodoViewModel> todoAdapter = (SimpleRecycleViewAdapter<TodoViewModel>) binding.todoRecycclerView.getAdapter();
        final List<TodoViewModel> todoDatas = todoAdapter.getData();

        final RefreshRecyclerViewAdapter<DocumentViewModel.ItemViewModel> adapter = (RefreshRecyclerViewAdapter<DocumentViewModel.ItemViewModel>) binding.reminderTodoListRecyclerView.getAdapter();
        adapter.getFooterViewModel().status.set(RefreshRecyclerViewAdapter.FooterViewModel.STATUS_HAS_MORE_ELEMENTS);
        final List<DocumentViewModel.ItemViewModel> datas = adapter.getData();
        datas.clear();

        NetworkAccessKit.getData(context, ApiKit.URL_REMINDER_TODO_LIST(UserData.getInstance().getId()), new NetworkAccessKit.DefaultCallback<JSONArray>() {
            @Override
            public void success(JSONArray jsonArray) {
                //TODO:后期打开
//                if(jsonArray!=null) {
//                    todoDatas.get(2).count.set(jsonArray.length());
//                    todoAdapter.notifyDataSetChanged();
//
//                    for (int i = 0; i < jsonArray.length(); i++) {
//                        try {
//                            JSONObject jsonObject = jsonArray.getJSONObject(i);
//
//                            DocumentViewModel.ItemViewModel vm = new DocumentViewModel.ItemViewModel();
//                            vm.setId(i + "");
//                            vm.title.set("关于某某事情的决定" + i);
//                            vm.created.set("2017/10/05");
//                            vm.progressCode.set(i);
//
//                            datas.add(vm);
//                        } catch (Exception e) {
//                            e.printStackTrace();
//                        }
//                    }
//                }
//
//                adapter.getFooterViewModel().status.set(RefreshRecyclerViewAdapter.FooterViewModel.STATUS_NO_MORE_ELEMENTS);
//                adapter.notifyDataSetChanged();
            }
        });
    }

    public static class TodoViewModel {
        public final ObservableInt picture = new ObservableInt();
        public final ObservableField<String> title = new ObservableField<>();
        public final ObservableInt count = new ObservableInt();

        private HomeViewModel parentViewModel;

        public TodoViewModel(HomeViewModel parentViewModel){
            this.parentViewModel = parentViewModel;
        }

        public void onItemClicked(View view){
            RecyclerView recyclerView = (RecyclerView) view.getParent().getParent();
            int index = recyclerView.indexOfChild((View) view.getParent());
            switch (index){
                case 0:
                    parentViewModel.binding.legalCaseTodoListRecyclerView.setVisibility(View.VISIBLE);
                    parentViewModel.binding.documentTodoListRecyclerView.setVisibility(View.GONE);
                    parentViewModel.binding.reminderTodoListRecyclerView.setVisibility(View.GONE);
                    break;
                case 1:
                    parentViewModel.binding.legalCaseTodoListRecyclerView.setVisibility(View.GONE);
                    parentViewModel.binding.documentTodoListRecyclerView.setVisibility(View.VISIBLE);
                    parentViewModel.binding.reminderTodoListRecyclerView.setVisibility(View.GONE);
                    break;
                case 2:
                    parentViewModel.binding.legalCaseTodoListRecyclerView.setVisibility(View.GONE);
                    parentViewModel.binding.documentTodoListRecyclerView.setVisibility(View.GONE);
                    parentViewModel.binding.reminderTodoListRecyclerView.setVisibility(View.VISIBLE);
                    break;
            }
        }
    }
}
