package kl.law.inspector.activity;

import android.app.Activity;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Toast;

import com.baidu.android.pushservice.PushConstants;
import com.baidu.android.pushservice.PushManager;

import java.util.List;

import kl.law.inspector.R;
import kl.law.inspector.databinding.ActivityMainBinding;
import kl.law.inspector.databinding.FragmentDocumentBinding;
import kl.law.inspector.databinding.FragmentHomeBinding;
import kl.law.inspector.tools.PushKit;
import kl.law.inspector.tools.RefreshRecyclerViewAdapter;
import kl.law.inspector.vm.DocumentViewModel;
import kl.law.inspector.vm.LegalCaseViewModel;
import kl.law.inspector.vm.MainActivityViewModel;

public class MainActivity extends AppCompatActivity{

    private boolean isQuit=false;
    private Handler handler = new Handler();

    private ActivityMainBinding binding;
    private MainActivityViewModel viewModel;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        PushManager.startWork(getApplicationContext(), PushConstants.LOGIN_TYPE_API_KEY, PushKit.getMetaValue(this, "api_key"));

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        viewModel = new MainActivityViewModel(this, binding);
        viewModel.init();

        binding.setViewModel(viewModel);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode==KeyEvent.KEYCODE_BACK){
            if(isQuit){
                finish();
            }else{
                isQuit = true;

                Toast.makeText(this, "再按一次返回键退出程序。", Toast.LENGTH_SHORT).show();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        isQuit = false;
                    }
                }, 1000);
            }
        }

        return false;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == LegalCaseViewModel.REQUEST_CODE_APPROVE) {
                String id = data.getStringExtra("id");

                View view= (binding.viewPager.getChildAt(0));
                FragmentHomeBinding binding = DataBindingUtil.getBinding(view);
                SwipeRefreshLayout swipeRefreshLayout = (SwipeRefreshLayout) binding.viewPager.getChildAt(0);
                RecyclerView recyclerView = (RecyclerView) swipeRefreshLayout.getChildAt(0);
                RefreshRecyclerViewAdapter<LegalCaseViewModel.ItemViewModel> adapter = (RefreshRecyclerViewAdapter<LegalCaseViewModel.ItemViewModel>) recyclerView.getAdapter();
                List<LegalCaseViewModel.ItemViewModel> datas = adapter.getData();
                for (LegalCaseViewModel.ItemViewModel itemViewModel : datas) {
                    if (itemViewModel.getId().equals(id)) {
                        datas.remove(itemViewModel);
                        adapter.notifyDataSetChanged();
                        break;
                    }
                }
            }else if (requestCode == LegalCaseViewModel.REQUEST_CODE_FILE) {
                String id = data.getStringExtra("id");

                View view= (binding.viewPager.getChildAt(0));
                FragmentHomeBinding binding = DataBindingUtil.getBinding(view);
                binding.getViewModel().loadLegalCase();
            }else if (requestCode == DocumentViewModel.REQUEST_CODE_CREATE) {
                View view= (binding.viewPager.getChildAt(2));
                FragmentDocumentBinding binding = DataBindingUtil.getBinding(view);
                binding.getViewModel().refreshDocument();
            }else if (requestCode == DocumentViewModel.REQUEST_CODE_APPROVE) {
                View view= (binding.viewPager.getChildAt(0));
                FragmentHomeBinding binding = DataBindingUtil.getBinding(view);
                binding.getViewModel().loadDocument();
            }
        }
    }

//        else if(binding.tabLayout.getSelectedTabPosition()==1) {
//            RefreshRecyclerViewAdapter<DocumentViewModel.ItemViewModel> adapter = (RefreshRecyclerViewAdapter<DocumentViewModel.ItemViewModel>) recyclerView.getAdapter();
//            List<DocumentViewModel.ItemViewModel> datas = adapter.getData();
//            for (DocumentViewModel.ItemViewModel itemViewModel : datas) {
//                if (itemViewModel.getId().equals(id)) {
//                    datas.remove(itemViewModel);
//                    adapter.notifyDataSetChanged();
//                    break;
//                }
//            }
//        }else if(binding.tabLayout.getSelectedTabPosition()==2) {
//            RefreshRecyclerViewAdapter<DocumentViewModel.ItemViewModel> adapter = (RefreshRecyclerViewAdapter<DocumentViewModel.ItemViewModel>) recyclerView.getAdapter();
//            List<DocumentViewModel.ItemViewModel> datas = adapter.getData();
//            for (DocumentViewModel.ItemViewModel itemViewModel : datas) {
//                if (itemViewModel.getId().equals(id)) {
//                    datas.remove(itemViewModel);
//                    adapter.notifyDataSetChanged();
//                    break;
//                }
//            }
//        }
}
