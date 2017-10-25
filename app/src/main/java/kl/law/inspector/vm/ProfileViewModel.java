package kl.law.inspector.vm;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.databinding.ObservableField;
import android.databinding.ObservableInt;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.view.View;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import kl.law.inspector.R;
import kl.law.inspector.activity.LoginActivity;
import kl.law.inspector.databinding.FragmentProfileBinding;
import kl.law.inspector.tools.SectionAdapter;
import kl.law.inspector.tools.UserData;

/**
 * Created by yinyy on 2017/10/9.
 */

public class ProfileViewModel extends AbstractViewModel<FragmentProfileBinding> {
    public ProfileViewModel(Context context, FragmentProfileBinding binding) {
        super(context, binding);
    }

    public void init(){
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(context));

        Map<Integer, Integer> layouts = new HashMap<>();
        layouts.put(1, R.layout.profile_header);
        layouts.put(2, R.layout.item_profile);
        layouts.put(4, R.layout.profile_decoration);
        layouts.put(5, R.layout.profile_personal);

        List<SectionAdapter.Element> items = new LinkedList<>();
        items.add(new SectionAdapter.Element(1));

        PersonalViewModel viewModel = new PersonalViewModel();
        viewModel.name.set(UserData.getInstance().getName());
        viewModel.department.set(UserData.getInstance().getOfficeName());
        viewModel.picture.set("http://www.qqzhi.com/uploadpic/2014-09-22/113849945.jpg");
        items.add(new SectionAdapter.Element<PersonalViewModel>(5, viewModel));

        items.add(new SectionAdapter.Element(1));

        ItemViewModel ivm = new ItemViewModel("fun1");
        ivm.title.set(MessageFormat.format("所在部门：{0}", UserData.getInstance().getOfficeName()));
        ivm.icon.set(R.drawable.ic_department);
        items.add(new SectionAdapter.Element(2, ivm));

        items.add(new SectionAdapter.Element(4));

        ivm = new ItemViewModel("fun2");
        ivm.title.set(MessageFormat.format("登录名：{0}", UserData.getInstance().getUsername()));
        ivm.icon.set(R.drawable.ic_login_name);
        items.add(new SectionAdapter.Element(2, ivm));

        items.add(new SectionAdapter.Element(4));

        ivm = new ItemViewModel("fun3");
        ivm.title.set(MessageFormat.format("编号：{0}", UserData.getInstance().getNo()));
        ivm.icon.set(R.drawable.ic_serial);
        items.add(new SectionAdapter.Element(2, ivm));

        items.add(new SectionAdapter.Element(1));

        ivm = new ItemViewModel("quit");
        ivm.title.set("退出");
        ivm.icon.set(R.drawable.ic_quit);
        items.add(new SectionAdapter.Element(2, ivm));

        SectionAdapter adapter= new SectionAdapter(items, layouts);
        binding.recyclerView.setAdapter(adapter);
    }

    public static class PersonalViewModel{
        public final ObservableField<String> name = new ObservableField<>();
        public final ObservableField<String> department = new ObservableField<>();
        public final ObservableField<String> picture = new ObservableField<>();
    }

    public static class ItemViewModel{
        public final ObservableField<String> title = new ObservableField<>();
        public final ObservableInt icon = new ObservableInt();
        private String tag;

        public ItemViewModel(String tag){
            this.tag = tag;
        }

        public void onItemClicked(final View view){
            if("quit".equals(tag)){
                new AlertDialog.Builder(view.getContext()).setTitle("提示").setMessage("确认退出当前用户吗？").setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        SharedPreferences pref = view.getContext().getSharedPreferences("profile.data", Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = pref.edit();

                        editor.putBoolean("isRememberPassword", false);
                        editor.commit();

                        Intent intent = new Intent(view.getContext(), LoginActivity.class);
                        view.getContext().startActivity(intent);

                        ((Activity)view.getContext()).finish();
                    }
                }).setNegativeButton("取消", null).show();
            }else if("upload".equals(tag)){

            }
        }
    }
}