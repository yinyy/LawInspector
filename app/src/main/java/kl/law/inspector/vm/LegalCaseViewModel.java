package kl.law.inspector.vm;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.databinding.ObservableBoolean;
import android.databinding.ObservableField;
import android.databinding.ObservableInt;
import android.provider.MediaStore;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.SimpleAdapter;
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
import kl.law.inspector.activity.LegalCaseApproveActivity;
import kl.law.inspector.activity.LegalCaseDetailActivity;
import kl.law.inspector.activity.LegalCaseFileActivity;
import kl.law.inspector.activity.LegalCaseProgressActivity;
import kl.law.inspector.databinding.ActivityLegalCaseApproveBinding;
import kl.law.inspector.databinding.ActivityLegalCaseCreateBinding;
import kl.law.inspector.databinding.ActivityLegalCaseDetailBinding;
import kl.law.inspector.databinding.ActivityLegalCaseFileBinding;
import kl.law.inspector.databinding.ActivityLegalCaseProgressBinding;
import kl.law.inspector.databinding.FragmentLegalCaseBinding;
import kl.law.inspector.databinding.ViewPagerBinding;
import kl.law.inspector.tools.ApiKit;
import kl.law.inspector.tools.DialogKit;
import kl.law.inspector.tools.FileKit;
import kl.law.inspector.tools.NetworkAccessKit;
import kl.law.inspector.tools.RefreshRecyclerViewAdapter;
import kl.law.inspector.tools.SimpleRecycleViewAdapter;
import kl.law.inspector.tools.UserData;

/**
 * Created by yinyy on 2017/9/26.
 * http://www.jianshu.com/p/3bf125b4917d
 */

public class LegalCaseViewModel extends AbstractViewModel<Fragment, FragmentLegalCaseBinding>{
    public static final int REQUEST_CODE_CREATE = 0x01;
    public static final int REQUEST_CODE_STATISTICS = 0x02;
    public static final int REQUEST_CODE_FILE = 0x04;
    public static final int REQUEST_CODE_DETAIL = 0x08;
    public static final int REQUEST_CODE_APPROVE = 0x10;

    private ViewPagerBinding[] viewPagerBindings;
    private ScrollRefreshStatusModel[] scrollRefreshStatusModels;

    public LegalCaseViewModel(Fragment owner, FragmentLegalCaseBinding binding) {
        super(owner, binding);
    }

    public void init() {
        LayoutInflater layoutInflater = ((Activity) context).getLayoutInflater();
        viewPagerBindings = new ViewPagerBinding[]{
                DataBindingUtil.inflate(layoutInflater, R.layout.view_pager, null, false),
                DataBindingUtil.inflate(layoutInflater, R.layout.view_pager, null, false),
                DataBindingUtil.inflate(layoutInflater, R.layout.view_pager, null, false),
                DataBindingUtil.inflate(layoutInflater, R.layout.view_pager, null, false),
                DataBindingUtil.inflate(layoutInflater, R.layout.view_pager, null, false)};

        scrollRefreshStatusModels = new ScrollRefreshStatusModel[viewPagerBindings.length];
        for (int index = 0; index < scrollRefreshStatusModels.length; index++) {
            scrollRefreshStatusModels[index] = new ScrollRefreshStatusModel();
        }

        DividerItemDecoration decoration = new DividerItemDecoration(context, DividerItemDecoration.VERTICAL);
        decoration.setDrawable(ContextCompat.getDrawable(context, R.drawable.ic_recycle_view_default_decoration));
        for (ViewPagerBinding b : viewPagerBindings) {
            b.recycleView.addItemDecoration(decoration);
        }

        final View[] views = new View[viewPagerBindings.length];
        for(int index=0;index<viewPagerBindings.length;index++){
            views[index] = viewPagerBindings[index].getRoot();
        }

        binding.viewPager.setAdapter(new PagerAdapter() {
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
                        return "立案";
                    case 1:
                        return "调查";
                    case 2:
                        return "处罚";
                    case 3:
                        return "结案";
                    case 4:
                        return "已完成";
                    default:
                        return "其它";
                }
            }
        });
        binding.tabLayout.setupWithViewPager(binding.viewPager);

        for (int index = 0; index < views.length; index++) {
            List<LegalCaseViewModel.ItemViewModel> datas = new ArrayList<>();
            RefreshRecyclerViewAdapter<ItemViewModel> adapter = new RefreshRecyclerViewAdapter<>(datas, R.layout.item_legal_case, R.layout.item_footer_refresh, BR.viewModel, BR.footer);
            viewPagerBindings[index].recycleView.setAdapter(adapter);

            final int finalIndex = index;
            viewPagerBindings[index].swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    if(!scrollRefreshStatusModels[finalIndex].isLoading()){
                        refreshLegalCase(finalIndex);
                    }
                }
            });
            viewPagerBindings[index].recycleView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);

                    int visibleCount = recyclerView.getChildCount();
                    int childCount = recyclerView.getAdapter().getItemCount();
                    int firstVisibleIndex = ((LinearLayoutManager) recyclerView.getLayoutManager()).findFirstVisibleItemPosition();

                    if (scrollRefreshStatusModels[finalIndex].hasMoreElements() && !scrollRefreshStatusModels[finalIndex].isLoading() && (firstVisibleIndex + visibleCount >= childCount)) {
                        loadLegalCase(finalIndex);
                    }
                }
            });
        }

        binding.tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                int index = tab.getPosition();
                if (scrollRefreshStatusModels[index].isFirst()) {
                    loadLegalCase(tab.getPosition());
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        if(scrollRefreshStatusModels[0].isFirst()) {
            loadLegalCase(0);
        }
    }

    public void onResume(){
    }

    public void refreshLegalCase(int index){
        RefreshRecyclerViewAdapter adapter = (RefreshRecyclerViewAdapter) viewPagerBindings[index].recycleView.getAdapter();
        adapter.getFooterViewModel().status.set(RefreshRecyclerViewAdapter.FooterViewModel.STATUS_HAS_MORE_ELEMENTS);

        scrollRefreshStatusModels[index].setPage(0);
        scrollRefreshStatusModels[index].setHasMoreElements(true);
        scrollRefreshStatusModels[index].setLoading(true);
        adapter.getData().clear();
        adapter.notifyDataSetChanged();
        scrollRefreshStatusModels[index].setLoading(false);

        loadLegalCase(index);
    }

    private void loadLegalCase(final int index) {
        if(scrollRefreshStatusModels[index].isLoading()){
            return ;
        }

        if(!viewPagerBindings[index].swipeRefreshLayout.isRefreshing()){
            viewPagerBindings[index].swipeRefreshLayout.setRefreshing(true);
        }

        scrollRefreshStatusModels[index].setLoading(true);

        NetworkAccessKit.getData(context, ApiKit.URL_LEGAL_CASE_LIST(scrollRefreshStatusModels[index].nextPage(), index + 1, UserData.getInstance().getId()), new NetworkAccessKit.DefaultCallback<JSONObject>() {
            @Override
            public void onSuccess(JSONObject data) {
                RefreshRecyclerViewAdapter<ItemViewModel> adapter = (RefreshRecyclerViewAdapter<ItemViewModel>) viewPagerBindings[index].recycleView.getAdapter();
                List<ItemViewModel> datas = adapter.getData();

                int pagecount = data.optInt("pagecount", 0);
                if (pagecount > scrollRefreshStatusModels[index].getPage()) {
                    adapter.getFooterViewModel().status.set(RefreshRecyclerViewAdapter.FooterViewModel.STATUS_HAS_MORE_ELEMENTS);
                } else {
                    adapter.getFooterViewModel().status.set(RefreshRecyclerViewAdapter.FooterViewModel.STATUS_NO_MORE_ELEMENTS);
                    scrollRefreshStatusModels[index].setHasMoreElements(false);
                }

                JSONArray jsonArray = data.optJSONArray("data");
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = jsonArray.optJSONObject(i);
                    LegalCaseViewModel.ItemViewModel vm = new LegalCaseViewModel.ItemViewModel();
                    vm.setId(jsonObject.optString("id"));
                    vm.picture.set(jsonObject.optString("caseThumbnails"));
                    vm.title.set(jsonObject.optString("title"));
                    String description = jsonObject.optString("caseDescription");
                    vm.subtitle.set(description.length() > 20 ? description.substring(0, 20) : description);
                    vm.serial.set(jsonObject.optString("caseDocNo"));

                    vm.remainder.set("测试：" + jsonObject.optString("step"));

                    String step = jsonObject.optString("step");
                    String[] values = step.split(",");

                    vm.progress.set(MessageFormat.format("{0} - {1}", values[2], values[3]));
                    //vm.progressCode.set(Integer.parseInt(MessageFormat.format("{0}{1}", values[0], values[1])));

                    datas.add(vm);
                }

                adapter.notifyDataSetChanged();

                scrollRefreshStatusModels[index].setLoading(false);
                viewPagerBindings[index].swipeRefreshLayout.setRefreshing(false);
            }

            @Override
            public void handleFailureAndError() {
                scrollRefreshStatusModels[index].setHasMoreElements(false);
                scrollRefreshStatusModels[index].setLoading(false);
                viewPagerBindings[index].swipeRefreshLayout.setRefreshing(false);

                Toast.makeText(context, "加载数据时发生错误，请稍后重试。", Toast.LENGTH_LONG).show();
            }
        });
    }

    public static class CreateViewModel extends AbstractViewModel<Activity, ActivityLegalCaseCreateBinding>{
        public final ObservableField<String> title = new ObservableField<>();
        public final ObservableField<String> party = new ObservableField<>();
        public final ObservableField<String> address = new ObservableField<>();
        public final ObservableField<String> corporation = new ObservableField<>();
        public final ObservableField<String> phone = new ObservableField<>();
        public final ObservableField<String> description = new ObservableField<>();
        public final ObservableField<String> currentOperator = new ObservableField<>();
        public final ObservableBoolean authorized = new ObservableBoolean();

        public final ObservableField<BaseAdapter> sourceAdapter = new ObservableField<>();
        public final ObservableField<BaseAdapter> behaviorAdapter = new ObservableField<>();
        public final ObservableField<BaseAdapter> legalProvisionAdapter = new ObservableField<>();

        public final ObservableField<BaseAdapter> memberAdapter = new ObservableField<>();

        public CreateViewModel(Activity owner, ActivityLegalCaseCreateBinding binding) {
            super(owner, binding);
        }

        public void init(){
            currentOperator.set(UserData.getInstance().getName());

            NetworkAccessKit.getData(context, ApiKit.URL_ARTICLE(ApiKit.ArticleCategory.LEGAL_CASE_SOURCE), new NetworkAccessKit.DefaultCallback<JSONArray>(){

                @Override
                public void onSuccess(JSONArray data) {
                    List<String> items = new LinkedList<String>();
                    items.add("请选择");

                    for (int i = 0; i < data.length(); i++) {
                        try {
                            JSONObject jobj = data.getJSONObject(i);
                            items.add(jobj.getString("title"));
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    }

                    sourceAdapter.set(new ArrayAdapter<String>(context, R.layout.simple_spinner_item_1, items));
                }
            });

            NetworkAccessKit.getData(context, ApiKit.URL_ARTICLE(ApiKit.ArticleCategory.ILLEGAL_BEHAVIOR), new NetworkAccessKit.DefaultCallback<JSONArray>(){

                @Override
                public void onSuccess(JSONArray data) {
                    List<String> items = new LinkedList<String>();
                    items.add("请选择");

                    for (int i = 0; i < data.length(); i++) {
                        try {
                            JSONObject jobj = data.getJSONObject(i);
                            items.add(jobj.getString("title"));
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    }

                    behaviorAdapter.set(new ArrayAdapter<String>(context, R.layout.simple_spinner_item_1, items));
                }
            });

            List<String> items = new LinkedList<String>();
            items.add("请选择");
            legalProvisionAdapter.set(new ArrayAdapter<String>(context, R.layout.simple_spinner_item_1, items));

            binding.behaviourSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    String behavior = (String)parent.getAdapter().getItem(position);
                    final ArrayAdapter<String> adapter = (ArrayAdapter<String>) binding.legalProvisionSpinner.getAdapter();
                    adapter.clear();
                    adapter.add("请选择");

                    NetworkAccessKit.getData(context, ApiKit.URL_PROVISION_BY_BEHAVIOR(behavior), new NetworkAccessKit.DefaultCallback<JSONArray>() {

                        @Override
                        public void onSuccess(JSONArray data) {

                            for (int i = 0; i < data.length(); i++) {
                                try {
                                    JSONObject jobj = data.getJSONObject(i);
                                    adapter.add(jobj.getString("title"));
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }

                            adapter.notifyDataSetChanged();
                        }
                    });
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });

            NetworkAccessKit.getData(context, ApiKit.URL_USER(UserData.getInstance().getOfficeId()), new NetworkAccessKit.DefaultCallback<JSONArray>(){

                @Override
                public void onSuccess(JSONArray data) {
                    List<Map<String, String>> items = new LinkedList<>();

                    for (int i = 0; i < data.length(); i++) {
                        try {
                            JSONObject jobj = data.getJSONObject(i);
                            Map<String, String> item = new HashMap<String, String>();

                            if(UserData.getInstance().getNo().equals(jobj.getString("no"))) {
                                continue;
                            }

                            item.put("id", jobj.getString("id"));
                            item.put("name", jobj.getString("name"));
                            item.put("title", jobj.getString("name")+"["+jobj.getString("no")+"]");
                            items.add(item);
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    }

                    memberAdapter.set(new SimpleAdapter(context, items, R.layout.simple_spinner_item_1, new String[]{"title"}, new int[]{R.id.text1}));
                }
            });
        }

        public void onSubmit(View view){
            if(TextUtils.isEmpty(title.get().trim())){
                DialogKit.showMessage(context, "请填写标题。");
                return;
            }

            if(TextUtils.isEmpty(party.get().trim())){
                DialogKit.showMessage(context, "请填写当事人。");
                return;
            }

            if(TextUtils.isEmpty(address.get().trim())){
                DialogKit.showMessage(context, "请填写地址。");
                return;
            }

            if(TextUtils.isEmpty(corporation.get().trim())){
                DialogKit.showMessage(context, "请填写法人。");
                return;
            }

            if(TextUtils.isEmpty(phone.get().trim())){
                DialogKit.showMessage(context, "请填写联系电话。");
                return;
            }

            if("请选择".equals(binding.sourceSpinner.getSelectedItem().toString())){
                DialogKit.showMessage(context, "请选择案件来源。");
                return;
            }

            if("请选择".equals(binding.legalProvisionSpinner.getSelectedItem().toString())){
                DialogKit.showMessage(context, "请选择法律条文。");
                return;
            }

            if(TextUtils.isEmpty(description.get().trim())){
                DialogKit.showMessage(context, "请填写详细案情描述。");
                return;
            }

            view.setEnabled(false);

            String source = binding.sourceSpinner.getSelectedItem().toString();
            String behaviour = binding.behaviourSpinner.getSelectedItem().toString();
            String legalProvision = binding.legalProvisionSpinner.getSelectedItem().toString();
            Map<String, String> member = (Map<String, String>) binding.memberSpinner.getSelectedItem();

            try {
                JSONObject jsonData =new JSONObject();

                JSONObject legalCaseObject = new JSONObject();
                legalCaseObject.put("title", title.get());
                legalCaseObject.put("caseParties", party.get());
                legalCaseObject.put("address", address.get());
                legalCaseObject.put("caseLegalAgent", corporation.get());
                legalCaseObject.put("phoneNumber", phone.get());
                legalCaseObject.put("kind", source);
                legalCaseObject.put("behavior", behaviour);
                legalCaseObject.put("provision",legalProvision);
                legalCaseObject.put("caseDescription", description.get());
                legalCaseObject.put("assigneeIds", MessageFormat.format("{0};{1}", UserData.getInstance().getId(), member.get("id")));
                legalCaseObject.put("illegalConstructionFlag", authorized.get());
                jsonData.put("legalCase", legalCaseObject);

                JSONObject userObject = new JSONObject();
                userObject.put("id", UserData.getInstance().getId());
                userObject.put("officeId", UserData.getInstance().getOfficeId());
                jsonData.put("user", userObject);

                NetworkAccessKit.postData(context, ApiKit.URL_LEGAL_CASE_CREATE, jsonData, new NetworkAccessKit.DefaultCallback<JSONObject>() {

                    @Override
                    public void onSuccess(JSONObject data) {
                        //Log.d("TEST", "Create Legal Case, ID is " + data.optString("id") + ".");
                        Toast.makeText(context, "案件信息保存成功。", Toast.LENGTH_LONG).show();

                        owner.setResult(Activity.RESULT_OK);
                        ((Activity)context).finish();
                    }
                });
            }catch (Exception e){
                e.printStackTrace();
                Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }
    }

    public static class DetailViewModel extends AbstractViewModel<Activity, ActivityLegalCaseDetailBinding>{
        public final ObservableField<String> title = new ObservableField<>();
        public final ObservableField<String> party = new ObservableField<>();
        public final ObservableField<String> address = new ObservableField<>();
        public final ObservableField<String> corporation = new ObservableField<>();
        public final ObservableField<String> phone = new ObservableField<>();
        public final ObservableField<String> description = new ObservableField<>();
        public final ObservableField<String> members = new ObservableField<>();
        public final ObservableField<String> serial = new ObservableField<>();

        private String id;
        private int stage;//工作流的大状态

        public DetailViewModel(Activity owner, ActivityLegalCaseDetailBinding binding) {
            super(owner, binding);
        }

        public void init(String id) {
            this.id = id;

            NetworkAccessKit.getData(context, ApiKit.URL_LEGAL_CASE_DETAIL(id), new NetworkAccessKit.DefaultCallback<JSONObject>() {
                @Override
                public void onSuccess(JSONObject data) {
                    title.set(data.optString("title"));
                    party.set(data.optString("caseParties"));
                    address.set(data.optString("address"));
                    corporation.set(data.optString("caseLegalAgent"));
                    phone.set(data.optString("phoneNumber"));
                    description.set(data.optString("caseDescription"));
                    members.set(data.optString("assigneeNames"));
                    serial.set(data.optString("caseDocNo"));

                    stage = data.optInt("caseStage", 0);
                }
            });
        }

        public void onProgressClicked(View view) {
            Intent intent = new Intent(view.getContext(), LegalCaseProgressActivity.class);
            intent.putExtra("id", id);
            //intent.putExtra("stage", stage);
            view.getContext().startActivity(intent);
        }

        public void onFileClicked(View view) {
            Intent intent = new Intent(view.getContext(), LegalCaseFileActivity.class);
            intent.putExtra("id", id);
            view.getContext().startActivity(intent);
        }
    }

    public static class ItemViewModel {
        public static final int CLICK_ACTION_DETAIL = 0x00;
        public static final int CLICK_ACTION_APPROVE = 0x01;
        public static final int CLICK_ACTION_FILE = 0x02;

        public final ObservableField<String> picture= new ObservableField<>();
        public final ObservableField<String> title = new ObservableField<>();
        public final ObservableField<String> subtitle =new ObservableField<>();
        public final ObservableField<String> serial = new ObservableField<>();
        public final ObservableField<String> remainder = new ObservableField<>();
        public final ObservableInt progressCode = new ObservableInt();
        public final ObservableField<String> progress= new ObservableField<>();

        private int clickAction = 0;
        public void setClickAction(int clickAction){
            this.clickAction = clickAction;
        }

        private String id;
        public void setId(String id){
            this.id = id;
        }
        public String getId(){return id;}

        public void onItemClicked(View view) {
            if(clickAction==CLICK_ACTION_DETAIL) {
                Intent intent = new Intent(view.getContext(), LegalCaseDetailActivity.class);
                intent.putExtra("id", id);
                view.getContext().startActivity(intent);
            }else if(clickAction == CLICK_ACTION_APPROVE) {
                Intent intent = new Intent(view.getContext(), LegalCaseApproveActivity.class);
                intent.putExtra("id", id);
                intent.putExtra("progressCode", progressCode.get());
                ((Activity)view.getContext()).startActivityForResult(intent, REQUEST_CODE_APPROVE);
            }else if(clickAction==CLICK_ACTION_FILE){
                Intent intent = new Intent(view.getContext(), LegalCaseFileActivity.class);
                intent.putExtra("id", id);
                intent.putExtra("showFinishedButton", true);
                ((Activity)view.getContext()).startActivityForResult(intent, REQUEST_CODE_FILE);
            }
        }
    }

    public static class ProgressViewModel extends AbstractViewModel<Activity, ActivityLegalCaseProgressBinding>{

        public final ObservableField<SimpleRecycleViewAdapter> progressAdapter = new ObservableField<>();

        public ProgressViewModel(Activity owner, ActivityLegalCaseProgressBinding binding) {
            super(owner, binding);
        }

        public void init(String id){
            NetworkAccessKit.getData(context, ApiKit.URL_LEGAL_CASE_PROGRESS_LIST(id), new NetworkAccessKit.DefaultCallback<JSONObject>() {

                @Override
                public void onSuccess(JSONObject data) {
                    List<ItemViewModel> datas = new LinkedList<>();

                    JSONArray progresses = data.optJSONArray("data");
                    for (int i = 0; i < progresses.length(); i++) {
                        JSONObject progress = progresses.optJSONObject(i);

                        ItemViewModel ivm = new ItemViewModel();

                        ivm.title.set(MessageFormat.format("{0} - {1}", progress.optString("stage"), progress.optString("name")));
                        if(!"".equals(progress.optString("remark"))){
                            ivm.title.set(MessageFormat.format("{0}：{1}", ivm.title.get(), progress.optString("remark")));
                        }

                        //ivm.status.set(progress.optString("status"));
                        String status = progress.optString("status");
                        if("pass".equals(status)) {
                            ivm.statusCode.set(1);
                        }else if("reject".equals(status)){
                            ivm.statusCode.set(2);
                        }else{
                            ivm.statusCode.set(0);
                        }

                        datas.add(ivm);
                    }

                    datas.get(0).isHeader.set(true);
                    datas.get(datas.size() - 1).isFooter.set(true);

                    progressAdapter.set(new SimpleRecycleViewAdapter<ItemViewModel>(datas, R.layout.item_progress, BR.viewModel));
                }
            });
        }

        public static class ItemViewModel {
            public final ObservableField<String> title = new ObservableField<>();
            public final ObservableInt statusCode = new ObservableInt();

            public final ObservableBoolean isHeader = new ObservableBoolean();
            public final ObservableBoolean isFooter = new ObservableBoolean();
        }
    }

    public static class ApproveViewModel extends AbstractViewModel<Activity, ActivityLegalCaseApproveBinding>{
        public final ObservableField<String> serial = new ObservableField<>();
        public final ObservableField<String> title = new ObservableField<>();
        public final ObservableField<String> party = new ObservableField<>();
        public final ObservableField<String> address = new ObservableField<>();
        public final ObservableField<String> corporation = new ObservableField<>();
        public final ObservableField<String> phone = new ObservableField<>();
        public final ObservableField<String> description = new ObservableField<>();
        public final ObservableField<String> members = new ObservableField<>();

        public final ObservableField<String> memberOpinion = new ObservableField<>();
        public final ObservableField<String> memberOpinion2 = new ObservableField<>();
        public final ObservableField<String> departmentOpinion = new ObservableField<>();
        public final ObservableField<String> departmentOpinion2 = new ObservableField<>();
        public final ObservableField<String> centerOpinion = new ObservableField<>();
        public final ObservableField<String> centerOpinion2 = new ObservableField<>();
        public final ObservableField<String> branchOpioion = new ObservableField<>();
        public final ObservableField<String> branchOpioion2 = new ObservableField<>();
        public final ObservableField<String> primaryOpinion = new ObservableField<>();
        public final ObservableField<String> primaryOpinion2 = new ObservableField<>();

        public final ObservableField<BaseAdapter> punishAdapter = new ObservableField<>();
        public final ObservableField<BaseAdapter> behaviorAdapter = new ObservableField<>();
        public final ObservableField<BaseAdapter> legalProvisionAdapter = new ObservableField<>();

        public final ObservableBoolean buttonEnabled = new ObservableBoolean();

        private String id;

        public ApproveViewModel(Activity owner, ActivityLegalCaseApproveBinding binding) {
            super(owner, binding);
        }

        public void init(String id, int stage, final int step) {
            this.id = id;

            buttonEnabled.set(true);

            if(stage==3 && step==1) {
                NetworkAccessKit.getData(context, ApiKit.URL_ARTICLE(ApiKit.ArticleCategory.PUNISH), new NetworkAccessKit.DefaultCallback<JSONArray>() {

                    @Override
                    public void onSuccess(JSONArray data) {
                        List<String> items = new LinkedList<String>();
                        items.add("请选择");

                        for (int i = 0; i < data.length(); i++) {
                            try {
                                JSONObject jobj = data.getJSONObject(i);
                                items.add(jobj.getString("title"));
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }

                        punishAdapter.set(new ArrayAdapter<String>(context, R.layout.simple_spinner_item_1, items));
                    }
                });

                List<String> items = new LinkedList<String>();
                legalProvisionAdapter.set(new ArrayAdapter<String>(context, R.layout.simple_spinner_item_1, items));
            }else if(stage==4 && step==1){
                List<String> items = new LinkedList<String>();
                behaviorAdapter.set(new ArrayAdapter<String>(context, R.layout.simple_spinner_item_1, items));

                items = new LinkedList<>();
                punishAdapter.set(new ArrayAdapter<String>(context, R.layout.simple_spinner_item_1, items));

                items = new LinkedList<>();
                legalProvisionAdapter.set(new ArrayAdapter<String>(context, R.layout.simple_spinner_item_1, items));
            }

            NetworkAccessKit.getData(context, ApiKit.URL_LEGAL_CASE_DETAIL(id), new NetworkAccessKit.DefaultCallback<JSONObject>() {
                @Override
                public void onSuccess(JSONObject data) {
                    title.set(data.optString("title"));
                    party.set(data.optString("caseParties"));
                    address.set(data.optString("address"));
                    corporation.set(data.optString("caseLegalAgent"));
                    phone.set(data.optString("phoneNumber"));
                    description.set(MessageFormat.format("{0} 违反了 {1} \r\n{2}",
                            data.optString("normCaseDescPart1"),
                            data.optString("normCaseDescPart2"),
                            data.optString("caseDescription")));
                    members.set(data.optString("assigneeNames"));
                    serial.set(data.optString("caseDocNo"));

                    switch (data.optString("caseStage")) {
                        case "1":
                            departmentOpinion2.set(data.optString("institutionRegOption"));
                            branchOpioion2.set(data.optString("deptLeaderRegOption"));
                            primaryOpinion2.set(data.optString("mainLeaderRegOption"));

                            memberOpinion.set("");
                            centerOpinion.set("");
                            break;
                        case "3":
                            departmentOpinion2.set(data.optString("institutionPenalOption"));
                            branchOpioion2.set(data.optString("deptLeaderPenalOption"));
                            primaryOpinion2.set(data.optString("mainLeaderPenalOption"));
                            centerOpinion2.set(data.optString("caseMgtCenterPenalOption"));

                            if(step==1){
                                ((ArrayAdapter<String>)legalProvisionAdapter.get()).add(data.optString("normCaseDescPart2"));
                                memberOpinion2.set("");
                            }else{
                                memberOpinion2.set(MessageFormat.format("依据 {0} 给予 {1} 之处罚\r\n{2}",
                                        data.optString("normCaseDescPart2"),
                                        data.optString("normAssigneePenalOptPart2"),
                                        data.optString("assigneePenalOption")));
                            }
                            break;
                        case "4":
                            departmentOpinion2.set(data.optString("institutionCloseCaseOption"));
                            primaryOpinion2.set(data.optString("mainLeaderCloseCaseOption"));
                            centerOpinion2.set(data.optString("caseMgtCenterCloseCaseOption"));

                            branchOpioion.set("");

                            if(step==1){
                                ((ArrayAdapter<String>)behaviorAdapter.get()).add(data.optString("normCaseDescPart1"));
                                ((ArrayAdapter<String>)legalProvisionAdapter.get()).add(data.optString("normCaseDescPart2"));
                                ((ArrayAdapter<String>)punishAdapter.get()).add(data.optString("normAssigneePenalOptPart2"));

                                memberOpinion2.set("");
                            }else{
                                memberOpinion2.set(MessageFormat.format("{0} 依据 {1} 给予 {2} 之处罚\r\n{3}",
                                        data.optString("normCaseDescPart1"),
                                        data.optString("normCaseDescPart2"),
                                        data.optString("normAssigneePenalOptPart2"),
                                        data.optString("assigneeCloseCaseOption")));
                            }
                            break;
                        default:
                            break;
                    }
                }
            });
        }

        public void onDeclineClicked(final View view) {
            View pview = (View) view.getParent().getParent().getParent();
            ActivityLegalCaseApproveBinding binging = DataBindingUtil.getBinding(pview);

            int stage = binging.getStage();
            int step = binging.getStep();

            if(step==2 && TextUtils.isEmpty(departmentOpinion.get().trim())) {
                DialogKit.showMessage(context, "请输入审批意见。");
                return;
            }else if(step==3 && TextUtils.isEmpty(centerOpinion.get().trim())) {
                DialogKit.showMessage(context, "请输入审批意见。");
                return;
            }else if(step==4 && TextUtils.isEmpty(branchOpioion.get().trim())) {
                DialogKit.showMessage(context, "请输入审批意见。");
                return;
            }else if(step==5 && TextUtils.isEmpty(primaryOpinion.get().trim())) {
                DialogKit.showMessage(context, "请输入审批意见。");
                return;
            }

            buttonEnabled.set(false);

            Map<String , Object> userMap = new HashMap<>();
            userMap.put("id", UserData.getInstance().getId());
            userMap.put("officeId", UserData.getInstance().getOfficeId());

            Map<String, Object> legalCaseMap = new HashMap<>();
            legalCaseMap.put("id", id);
            legalCaseMap.put("approve", "reject");
            legalCaseMap.put("step", MessageFormat.format("{0},{1}", stage, step));

            if(stage==3 && step==1){
                legalCaseMap.put("opinion",memberOpinion.get());
                legalCaseMap.put("provision",binging.legalProvisionSpinner.getSelectedItem().toString());
                legalCaseMap.put("punish",binging.punishSpinner.getSelectedItem().toString());
            }else if(stage==4 && step==1){
                legalCaseMap.put("opinion",memberOpinion.get());
                legalCaseMap.put("behavior",binging.behaviorSpinner.getSelectedItem().toString());
                legalCaseMap.put("provision",binging.legalProvisionSpinner2.getSelectedItem().toString());
                legalCaseMap.put("punish",binging.punishSpinner2.getSelectedItem().toString());
            }else if(step==2){
                legalCaseMap.put("opinion", departmentOpinion.get());
            }else if(step==3){
                legalCaseMap.put("opinion", centerOpinion.get());
            }else if(step==4){
                legalCaseMap.put("opinion", branchOpioion.get());
            }else if(step==5){
                legalCaseMap.put("opinion", primaryOpinion.get());
            }

            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("user", new JSONObject(userMap));
                jsonObject.put("legalCase", new JSONObject(legalCaseMap));
            }catch (Exception e){
                e.printStackTrace();
            }

            NetworkAccessKit.postData(context, ApiKit.URL_LEGAL_CASE_APPROVE, jsonObject, new NetworkAccessKit.DefaultCallback<JSONObject>(){

                @Override
                public void onSuccess(JSONObject data) {
                    Toast.makeText(view.getContext(), "案件审批成功。", Toast.LENGTH_LONG).show();

                    Intent intent = new Intent();
                    intent.putExtra("id", id);
                    owner.setResult(Activity.RESULT_OK, intent);
                    owner.finish();
                }

                @Override
                public void onFailure(int code, String remark) {
                    super.onFailure(code, remark);

                    if(code==CODE_SUCCESS){
                        if("this step has approved".equals(remark)){
                            new AlertDialog.Builder(context).setTitle("提示").setMessage("案件已经被审批！").setPositiveButton("确定", null).show();
                        }
                    }else{
                        new AlertDialog.Builder(context).setTitle("提示").setMessage(remark).setPositiveButton("确定", null).show();
                    }
                }

                @Override
                public void onError(String message) {
                    super.onError(message);

                    new AlertDialog.Builder(context).setTitle("提示").setMessage(message).setPositiveButton("确定", null).show();
                }
            });
        }

        public void onSubmitClicked(final View view) {
            View pview = (View) view.getParent().getParent().getParent();
            ActivityLegalCaseApproveBinding binging = DataBindingUtil.getBinding(pview);

            int stage = binging.getStage();
            int step = binging.getStep();

            if(step==1) {
                if (stage == 3 && "请选择".equals(binging.punishSpinner.getSelectedItem().toString())) {
                    DialogKit.showMessage(context, "请选择处罚类型。");
                    return;
                }

                if (TextUtils.isEmpty(memberOpinion.get().trim())) {
                    DialogKit.showMessage(context, "请输入审批意见。");
                    return;
                }
            }else if(step==2 && TextUtils.isEmpty(departmentOpinion.get().trim())) {
                DialogKit.showMessage(context, "请输入审批意见。");
                return;
            }else if(step==3 && TextUtils.isEmpty(centerOpinion.get().trim())) {
                DialogKit.showMessage(context, "请输入审批意见。");
                return;
            }else if(step==4 && TextUtils.isEmpty(branchOpioion.get().trim())) {
                DialogKit.showMessage(context, "请输入审批意见。");
                return;
            }else if(step==5 && TextUtils.isEmpty(primaryOpinion.get().trim())) {
                DialogKit.showMessage(context, "请输入审批意见。");
                return;
            }

            buttonEnabled.set(false);

            Map<String , Object> userMap = new HashMap<>();
            userMap.put("id", UserData.getInstance().getId());
            userMap.put("officeId", UserData.getInstance().getOfficeId());

            Map<String, Object> legalCaseMap = new HashMap<>();
            legalCaseMap.put("id", id);
            legalCaseMap.put("approve", "pass");
            legalCaseMap.put("step", MessageFormat.format("{0},{1}", stage, step));

            if(stage==3 && step==1){
                legalCaseMap.put("opinion",memberOpinion.get());
                legalCaseMap.put("provision",binging.legalProvisionSpinner.getSelectedItem().toString());
                legalCaseMap.put("punish",binging.punishSpinner.getSelectedItem().toString());
            }else if(stage==4 && step==1){
                legalCaseMap.put("opinion",memberOpinion.get());
                legalCaseMap.put("behavior",binging.behaviorSpinner.getSelectedItem().toString());
                legalCaseMap.put("provision",binging.legalProvisionSpinner2.getSelectedItem().toString());
                legalCaseMap.put("punish",binging.punishSpinner2.getSelectedItem().toString());
            }else if(step==2){
                legalCaseMap.put("opinion", departmentOpinion.get());
            }else if(step==3){
                legalCaseMap.put("opinion", centerOpinion.get());
            }else if(step==4){
                legalCaseMap.put("opinion", branchOpioion.get());
            }else if(step==5){
                legalCaseMap.put("opinion", primaryOpinion.get());
            }

            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("user", new JSONObject(userMap));
                jsonObject.put("legalCase", new JSONObject(legalCaseMap));
            }catch (Exception e){
                e.printStackTrace();
            }

            NetworkAccessKit.postData(context, ApiKit.URL_LEGAL_CASE_APPROVE, jsonObject, new NetworkAccessKit.DefaultCallback<JSONObject>(){

                @Override
                public void onSuccess(JSONObject data) {
                    Toast.makeText(view.getContext(), "案件审批成功。", Toast.LENGTH_LONG).show();

                    Intent intent = new Intent();
                    intent.putExtra("id", id);
                    owner.setResult(Activity.RESULT_OK, intent);
                    owner.finish();
                }

                @Override
                public void onFailure(int code, String remark) {
                    super.onFailure(code, remark);

                    if(code==CODE_SUCCESS){
                        if("this step has approved".equals(remark)){
                            new AlertDialog.Builder(context).setTitle("提示").setMessage("案件已经被审批！").setPositiveButton("确定", null).show();
                        }
                    }else{
                        new AlertDialog.Builder(context).setTitle("提示").setMessage(remark).setPositiveButton("确定", null).show();
                    }
                }

                @Override
                public void onError(String message) {
                    super.onError(message);

                    new AlertDialog.Builder(context).setTitle("提示").setMessage(message).setPositiveButton("确定", null).show();
                }
            });
        }
    }

    public static class FileViewModel extends AbstractViewModel<Activity, ActivityLegalCaseFileBinding>{
//        public final ObservableField<String> party = new ObservableField<>();
//        public final ObservableField<String> address = new ObservableField<>();
//        public final ObservableField<String> corporation = new ObservableField<>();
//        public final ObservableField<String> phone = new ObservableField<>();
//        public final ObservableField<String> description = new ObservableField<>();
//        public final ObservableField<String> members = new ObservableField<>();
        public final ObservableBoolean showFinishedButton = new ObservableBoolean();

        public final ObservableField<SimpleRecycleViewAdapter> documentAdapter = new ObservableField<>();
        public final ObservableField<SimpleRecycleViewAdapter> pictureAdapter = new ObservableField<>();
        public final ObservableField<SimpleRecycleViewAdapter> videoAdapter = new ObservableField<>();

        public final ObservableBoolean buttonEnabled = new ObservableBoolean();

        private String id;

        public FileViewModel(Activity owner, ActivityLegalCaseFileBinding binding) {
            super(owner, binding);
        }

        public void init(String id) {
            this.id = id;

            buttonEnabled.set(true);

            binding.documentRecyclerView.setLayoutManager(new GridLayoutManager(context, 4));
            binding.pictureRecyclerView.setLayoutManager(new GridLayoutManager(context, 4));
            binding.videoRecyclerView.setLayoutManager(new GridLayoutManager(context, 4));

            List<AttachmentViewModel> datas = new LinkedList<>();
            AttachmentViewModel flivm = new AttachmentViewModel(context);
            flivm.type.set(AttachmentViewModel.TYPE_ADD_BUTTON);
            datas.add(flivm);
            documentAdapter.set(new SimpleRecycleViewAdapter<AttachmentViewModel>(datas, R.layout.item_attachment, BR.viewModel));

            datas = new LinkedList<>();
            flivm = new AttachmentViewModel(context);
            flivm.type.set(AttachmentViewModel.TYPE_ADD_BUTTON);
            datas.add(flivm);
            pictureAdapter.set(new SimpleRecycleViewAdapter<AttachmentViewModel>(datas, R.layout.item_attachment, BR.viewModel));

            datas = new LinkedList<>();
            flivm = new AttachmentViewModel(context);
            flivm.type.set(AttachmentViewModel.TYPE_ADD_BUTTON);
            datas.add(flivm);
            videoAdapter.set(new SimpleRecycleViewAdapter<AttachmentViewModel>(datas, R.layout.item_attachment, BR.viewModel));

            NetworkAccessKit.getData(context, ApiKit.URL_LEGAL_CASE_FILE_LIST(id), new NetworkAccessKit.DefaultCallback<JSONObject>() {

                @Override
                public void onSuccess(JSONObject data) {
                    try{
                        SimpleRecycleViewAdapter<AttachmentViewModel> adapter = documentAdapter.get();
                        List<AttachmentViewModel> datas = adapter.getData();

                        JSONArray array = data.getJSONArray("documents");
                        if(array.length()>0) {
                            for (int i = 0; i < array.length(); i++) {
                                String url = array.getString(i);

                                AttachmentViewModel flivm = new AttachmentViewModel(context);

                                if (FileKit.isBitmap(url) || FileKit.isVideo(url)) {
                                    flivm.imageUrl.set(url);
                                } else {
                                    flivm.imageRes.set(FileKit.getFileIcon(url));
                                }

                                flivm.remoteUrl.set(url);
                                flivm.type.set(AttachmentViewModel.TYPE_ITEM);
                                flivm.showDelete.set(true);
                                datas.add(datas.size() - 1, flivm);
                            }

                            adapter.notifyDataSetChanged();
                        }

                        adapter = pictureAdapter.get();
                        datas = adapter.getData();

                        array = data.getJSONArray("photos");
                        if(array.length()>0) {
                            for (int i = 0; i < array.length(); i++) {
                                String url = array.getString(i);

                                AttachmentViewModel flivm = new AttachmentViewModel(context);

                                if (FileKit.isBitmap(url) || FileKit.isVideo(url)) {
                                    flivm.imageUrl.set(url);
                                } else {
                                    flivm.imageRes.set(FileKit.getFileIcon(url));
                                }

                                flivm.remoteUrl.set(url);
                                flivm.type.set(AttachmentViewModel.TYPE_ITEM);
                                flivm.showDelete.set(true);
                                datas.add(datas.size() - 1, flivm);
                            }

                            adapter.notifyDataSetChanged();
                        }

                        adapter = videoAdapter.get();
                        datas = adapter.getData();

                        array = data.getJSONArray("videos");
                        if(array.length()>0) {
                            for (int i = 0; i < array.length(); i++) {
                                String url = array.getString(i);

                                AttachmentViewModel flivm = new AttachmentViewModel(context);

                                if (FileKit.isBitmap(url) || FileKit.isVideo(url)) {
                                    flivm.imageUrl.set(url);
                                } else {
                                    flivm.imageRes.set(FileKit.getFileIcon(url));
                                }

                                flivm.remoteUrl.set(url);
                                flivm.type.set(AttachmentViewModel.TYPE_ITEM);
                                flivm.showDelete.set(true);
                                datas.add(datas.size() - 1, flivm);
                            }

                            adapter.notifyDataSetChanged();
                        }
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
            });
        }

        public void onSubmitClicked(final View view) {
            buttonEnabled.set(false);

            //获取待上传的文件列表
            List<Map<String, Object>> fileList = new LinkedList<>();

            SimpleRecycleViewAdapter<AttachmentViewModel> adapter = (SimpleRecycleViewAdapter<AttachmentViewModel>) binding.documentRecyclerView.getAdapter();
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

            adapter = (SimpleRecycleViewAdapter<AttachmentViewModel>) binding.pictureRecyclerView.getAdapter();
            datas = adapter.getData();
            for(AttachmentViewModel attachmentViewModel : datas){
                if(attachmentViewModel.type.get()==AttachmentViewModel.TYPE_ITEM){
                    if(attachmentViewModel.localFile.get()!=null){
                        Map<String, Object> m = new HashMap<>();
                        m.put("viewModel", attachmentViewModel);

                        fileList.add(m);
                    }
                }
            }

            adapter = (SimpleRecycleViewAdapter<AttachmentViewModel>) binding.videoRecyclerView.getAdapter();
            datas = adapter.getData();
            for(AttachmentViewModel attachmentViewModel : datas){
                if(attachmentViewModel.type.get()==AttachmentViewModel.TYPE_ITEM){
                    if(attachmentViewModel.localFile.get()!=null){
                        Map<String, Object> m = new HashMap<>();
                        m.put("viewModel", attachmentViewModel);

                        fileList.add(m);
                    }
                }
            }

            postFiles(fileList);
        }

        public void onFinishedClicked(final View view){
            new AlertDialog.Builder(context).setTitle("提示").setMessage("确认完成案件调查，进入下一个流程吗？").setPositiveButton("确定", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    buttonEnabled.set(false);

                    try{
                        JSONObject jsonUser = new JSONObject();
                        jsonUser.put("id", UserData.getInstance().getId());
                        jsonUser.put("officeId", UserData.getInstance().getOfficeId());

                        JSONObject jsonCase = new JSONObject();
                        jsonCase.put("id", id);
                        jsonCase.put("step", "2,1");

                        JSONObject jsonData = new JSONObject();
                        jsonData.put("user", jsonUser);
                        jsonData.put("legalCase", jsonCase);

                        NetworkAccessKit.postData(context, ApiKit.URL_LEGAL_CASE_APPROVE, jsonData, new NetworkAccessKit.DefaultCallback<JSONObject>() {
                            @Override
                            public void onSuccess(JSONObject data) {
                                Toast.makeText(context, "调查完成，进入下一流程。", Toast.LENGTH_LONG).show();

                                Intent intent = new Intent();
                                intent.putExtra("id", id);
                                owner.setResult(Activity.RESULT_OK, intent);
                                owner.finish();
                            }
                        });
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
            }).setNegativeButton("取消", null).show();
        }

        public void onAttachmentClicked(View view){
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
                        ((Activity) context).startActivityForResult(intent, AttachmentViewModel.REQUEST_CAPTURE_PICTURE);
                    } else if (which == 1) {
                        Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
                        ((Activity) context).startActivityForResult(intent, AttachmentViewModel.REQUEST_CAPTURE_VIDEO);
                    }else if (which==2){
                        Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                        ((Activity) context).startActivityForResult(intent, AttachmentViewModel.REQUEST_PICK);
                    }else if(which==3){
                        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                        intent.setType("*/*");//设置类型，我这里是任意类型，任意后缀的可以这样写。
                        intent.addCategory(Intent.CATEGORY_OPENABLE);

                        try {
                            ((Activity) context).startActivityForResult(intent, AttachmentViewModel.REQUEST_OPEN_FILE);
                        } catch (ActivityNotFoundException e) {
                            new AlertDialog.Builder(context).setTitle("提示").setMessage("本机没有安装文件管理器。").setPositiveButton("确定", null).show();
                        }
                    }
                }
            }).setTitle("来源").show();
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

                        viewModel.remoteUrl.set(remoteUrl);
                        viewModel.localFile.set(null);

                        postFiles(fileList);
                    }

                    @Override
                    public void onFailure(int code, String remark) {
                        super.onFailure(code, remark);

                        if(code==CODE_INVALID_FILE_TYPE){
                            Toast.makeText(context,"不支持的文件格式。",Toast.LENGTH_LONG).show();
                        }else{
                            Toast.makeText(context,remark,Toast.LENGTH_LONG).show();
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
                updateLegalCase();
            }
        }

        private void updateLegalCase(){
            try {
                JSONObject jsonData = new JSONObject();

                JSONObject legalCaseObject = new JSONObject();
                legalCaseObject.put("id", id);

                SimpleRecycleViewAdapter<AttachmentViewModel> adapter = (SimpleRecycleViewAdapter<AttachmentViewModel>) binding.documentRecyclerView.getAdapter();
                List<AttachmentViewModel> datas = adapter.getData();
                JSONArray fileArray = new JSONArray();
                for (AttachmentViewModel data : datas) {
                    if (data.type.get() == AttachmentViewModel.TYPE_ITEM) {
                        fileArray.put(data.remoteUrl.get());
                    }
                }
                legalCaseObject.put("documents", fileArray);

                adapter = (SimpleRecycleViewAdapter<AttachmentViewModel>) binding.pictureRecyclerView.getAdapter();
                datas = adapter.getData();
                fileArray = new JSONArray();
                for (AttachmentViewModel data : datas) {
                    if (data.type.get() == AttachmentViewModel.TYPE_ITEM) {
                        fileArray.put(data.remoteUrl.get());
                    }
                }
                legalCaseObject.put("photos", fileArray);

                adapter = (SimpleRecycleViewAdapter<AttachmentViewModel>) binding.videoRecyclerView.getAdapter();
                datas = adapter.getData();
                fileArray = new JSONArray();
                for (AttachmentViewModel data : datas) {
                    if (data.type.get() == AttachmentViewModel.TYPE_ITEM) {
                        fileArray.put(data.remoteUrl.get());
                    }
                }
                legalCaseObject.put("videos", fileArray);

                jsonData.put("legalCase", legalCaseObject);

                JSONObject userObject = new JSONObject();
                userObject.put("id", UserData.getInstance().getId());
                userObject.put("officeId", UserData.getInstance().getOfficeId());

                jsonData.put("user", userObject);

                NetworkAccessKit.postData(context, ApiKit.URL_LEGAL_CASE_UPDATE_FILES, jsonData, new NetworkAccessKit.DefaultCallback() {
                    @Override
                    public void onSuccess(Object data) {
                        Toast.makeText(context, "文件更新成功。", Toast.LENGTH_LONG).show();

                        owner.setResult(Activity.RESULT_CANCELED);
                        owner.finish();
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
