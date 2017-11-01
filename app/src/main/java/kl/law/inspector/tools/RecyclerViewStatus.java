package kl.law.inspector.tools;

/**
 * Created by yinyy on 2017/10/31.
 */

public class RecyclerViewStatus {
    private boolean loading;
    private boolean moreElements;
    private boolean loaded;
    private int page;

    public RecyclerViewStatus(){
        setLoading(false);
        setHasMoreElements(false);
        setLoaded(false);
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

    public boolean isLoaded() {
        return loaded;
    }

    public void setLoaded(boolean loaded) {
        this.loaded = loaded;
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
}
