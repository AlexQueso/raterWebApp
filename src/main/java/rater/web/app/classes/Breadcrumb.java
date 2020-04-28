package rater.web.app.classes;

public class Breadcrumb {
    private String tag;
    private String href;

    public Breadcrumb(String tag, String href) {
        this.tag = tag;
        this.href = href;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getHref() {
        return href;
    }

    public void setHref(String href) {
        this.href = href;
    }
}
