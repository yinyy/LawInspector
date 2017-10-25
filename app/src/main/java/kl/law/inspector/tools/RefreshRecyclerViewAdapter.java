package kl.law.inspector.tools;

import android.databinding.DataBindingUtil;
import android.databinding.ObservableInt;
import android.databinding.ViewDataBinding;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

/**
 * Created by yinyy on 2017/10/18.
 */

public class RefreshRecyclerViewAdapter<DATA> extends RecyclerView.Adapter<RefreshRecyclerViewAdapter.MyRecycleViewHolder> {
    public static final int VIEW_TYPE_ITEM = 0x00;
    public static final int VIEW_TYPE_FOOTER = 0x02;

    private List<DATA> data;
    private int itemRes;
    private int footerRes;
    private int itemViewModelBr;
    private int footerViewModelBr;
    private FooterViewModel footerViewModel;

    public RefreshRecyclerViewAdapter(List<DATA> data, int itemRes, int footerRes, int itemViewModelBr, int footerViewModelBr){
        this.data = data;
        this.itemRes = itemRes;
        this.footerRes = footerRes;
        this.itemViewModelBr = itemViewModelBr;
        this.footerViewModelBr = footerViewModelBr;

        this.footerViewModel = new FooterViewModel();
    }

    public FooterViewModel getFooterViewModel(){
        return footerViewModel;
    }

    @Override
    public MyRecycleViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        ViewDataBinding binding = null;

        switch (viewType){
            case VIEW_TYPE_ITEM:
                binding = DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), itemRes,  parent, false);
                break;
            case VIEW_TYPE_FOOTER:
                binding = DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), footerRes,  parent, false);
                break;
        }

        return new MyRecycleViewHolder(binding.getRoot());
    }

    @Override
    public void onBindViewHolder(MyRecycleViewHolder holder, int position) {
        if(position==data.size()) {
            ViewDataBinding binding = DataBindingUtil.getBinding(holder.itemView);
            binding.setVariable(footerViewModelBr, footerViewModel);
            binding.executePendingBindings();
        }else{
            Object obj = data.get(position);

            ViewDataBinding binding = DataBindingUtil.getBinding(holder.itemView);
            binding.setVariable(itemViewModelBr, obj);
            binding.executePendingBindings();
        }
    }

    @Override
    public int getItemCount() {
        return data.size() + 1;
    }

    public List<DATA> getData(){
        return data;
    }

    @Override
    public int getItemViewType(int position) {
        if(position==data.size()){
            return VIEW_TYPE_FOOTER;
        }

        return VIEW_TYPE_ITEM;
    }

    public static class MyRecycleViewHolder extends RecyclerView.ViewHolder{
        public MyRecycleViewHolder(View itemView) {
            super(itemView);
        }
    }

    public static class FooterViewModel{
        public static final int STATUS_HAS_MORE_ELEMENTS = 0x01;
        public static final int STATUS_NO_MORE_ELEMENTS = 0x02;

        private FooterViewModel(){
            status.set(STATUS_HAS_MORE_ELEMENTS);
        }

        public final ObservableInt status = new ObservableInt();
    }
}
