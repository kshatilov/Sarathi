package me.shatilov.utility.android;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.support.design.widget.BottomNavigationView;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.RelativeLayout;

import java.util.List;

import me.shatilov.utility.EasyCallable;

/**
 * Created by Kirill on 26-Jan-18.
 */

public class BottomNavigationViewWrapper extends BottomNavigationView {

    private final int menuId;
    private final Palette palette;

    public BottomNavigationViewWrapper(Context context, List<TabsWrapper.Tab.Header> headers, EasyCallable<Integer> onClick, Palette palette) {
        super(context);
        this.palette = palette;
        Menu menu = this.getMenu();
        int index = 0;
        menuId = (int) Math.floor(Math.random());
        for (TabsWrapper.Tab.Header header : headers) {
            menu.add(menuId, menuId + index, index++, header.getTitle()).setIcon(header.getIcon());
        }

        this.setOnNavigationItemSelectedListener(
                item -> {
                    onClick.accept(item.getItemId() - menuId);
                    item.setChecked(true);
                    handleIconsColor();
                    return false;
                }
        );

        this.setBackgroundColor(palette.getPrimaryColor());
        this.setItemTextColor(ColorStateList.valueOf(palette.getLightColor()));
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
        params.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
        params.addRule(RelativeLayout.ALIGN_PARENT_START, RelativeLayout.TRUE);
        this.setLayoutParams(params);

        handleIconsColor();
    }

    private void handleIconsColor() {
        Menu menu = this.getMenu();
        for (int i = 0; i < menu.size(); i++) {
            MenuItem item = menu.getItem(i);
            Drawable icon = item.getIcon();
            icon.mutate();
            icon.setColorFilter(
                    item.getItemId() == this.getSelectedItemId() ? palette.getLightColor() : palette.getDarkColor(),
                    PorterDuff.Mode.SRC_IN);
            item.setIcon(icon);
        }
    }

}
