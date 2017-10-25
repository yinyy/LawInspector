package kl.law.inspector.tools;

import android.databinding.DataBindingUtil;
import android.databinding.ViewDataBinding;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

/**
 * Created by yinyy on 2017/9/19.
 */

public class SimpleRecycleViewAdapter<T> extends RecyclerView.Adapter<SimpleRecycleViewAdapter.MyRecycleViewHolder> {
    private List<T> data;
    private int layoutRes;
    private int viewModelBr;

    public SimpleRecycleViewAdapter(List<T> data, int layoutRes, int viewModelBr){
        this.data = data;
        this.layoutRes = layoutRes;
        this.viewModelBr = viewModelBr;
    }

    @Override
    public MyRecycleViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        ViewDataBinding binding = DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), layoutRes,  parent, false);
        return new MyRecycleViewHolder(binding.getRoot());
    }

    @Override
    public void onBindViewHolder(MyRecycleViewHolder holder, int position) {
        Object obj = data.get(position);

        ViewDataBinding binding = DataBindingUtil.getBinding(holder.itemView);
        binding.setVariable(viewModelBr, obj);
        binding.executePendingBindings();
    }

    @Override
    public int getItemCount() {
        if(data==null){
            return -1;
        }

        return data.size();
    }

    public List<T> getData(){
        return data;
    }

    public static class MyRecycleViewHolder extends RecyclerView.ViewHolder{
        public MyRecycleViewHolder(View itemView) {
            super(itemView);
        }
    }
}
