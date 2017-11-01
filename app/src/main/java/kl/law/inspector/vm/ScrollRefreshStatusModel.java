package kl.law.inspector.vm;

/**
 * Created by yinyy on 2017/10/31.
 */

public class ScrollRefreshStatusModel {
    private boolean loading;
    private boolean moreElements;
    private int page;
    private boolean first = true;

    public ScrollRefreshStatusModel(){
        setLoading(false);
        setHasMoreElements(false);
        setPage(0);
    }

    public boolean isLoading() {
        return loading;
    }

    public void setLoading(boolean loading) {
        this.loading = loading;
    }

    public boolean hasMoreElements() {
        return moreElements;
    }

    public void setHasMoreElements(boolean moreElements) {
        this.moreElements = moreElements;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int nextPage(){
        page++;
        return page;
    }

    public boolean isFirst(){
        if(first){
            first = false;
            return true;
        }else{
            return false;
        }
    }
}
