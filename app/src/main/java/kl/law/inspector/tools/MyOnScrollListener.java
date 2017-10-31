package kl.law.inspector.tools;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

/**
 * Created by yinyy on 2017/10/31.
 */

public abstract class MyOnScrollListener extends RecyclerView.OnScrollListener {
    public boolean isLoading = false;
    public int currentPage;
    public boolean hasMoreElemets = true;

    public MyOnScrollListener(){
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

            loadOnScroll();
        }
    }

    public abstract void loadOnScroll();
}