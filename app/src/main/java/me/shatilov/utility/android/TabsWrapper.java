package me.shatilov.utility.android;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.RelativeLayout;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Kirill on 26-Jan-18.
 */

public class TabsWrapper {
    public static class Tab {
        public static class Header {
            private Drawable icon;
            private String title;

            public Header(Drawable icon, String title) {
                this.icon = icon;
                this.title = title;
            }

            public Drawable getIcon() {
                return icon;
            }

            public String getTitle() {
                return title;
            }
        }

        private View view;
        private Header header;

        public Tab(View view, Drawable icon, String title) {
            this.view = view;
            this.header = new Header(icon, title);
        }

        public View getView() {
            return view;
        }

        public Header getHeader() {
            return header;
        }
    }

    private List<Tab> tabs;
    private int activeTab;
    private Context context;
    private Palette palette;

    public TabsWrapper(List<Tab> tabs, Context context) {
        this(tabs, context, new Palette());
    }

    public TabsWrapper(List<Tab> tabs, Context context, Palette palette) {
        this.tabs = tabs;
        this.context = context;
        this.activeTab = 0;
        this.palette = palette;
    }

    /**
     * Returns properly places and fully operational tab menu!
     */
    public BottomNavigationViewWrapper getMenu() {
        //thx API 23, I could have just used dis: tabs.stream().map(e -> e.getHeader()).collect(Collectors.toList())
        List<Tab.Header> headers = new ArrayList<>();
        for (Tab tab : tabs) {
            headers.add(tab.getHeader());
        }

        return new BottomNavigationViewWrapper(context, headers, this::setActiveTab, this.palette);
    }

    /**
     * For explicit outer navigation.
     *
     * @param title Title of the tab, meh
     */
    public void setActiveTab(String title) {
        for (int i = 0; i < tabs.size(); i++) {
            if (tabs.get(i).getHeader().getTitle().equals(title)) {
                setActiveTab(i);
            }
        }
    }

    /**
     * Active tab switcher.
     *
     * @param index Index of the tab, lul
     */
    public void setActiveTab(int index) {
        assert index >= 0;
        assert index < this.tabs.size();
        activeTab = index;
        for (int i = 0; i < tabs.size(); i++) {
            tabs.get(i).getView().setVisibility(i == activeTab ? View.VISIBLE : View.GONE);
        }
    }

}
