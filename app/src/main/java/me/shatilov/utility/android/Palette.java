package me.shatilov.utility.android;

/**
 * Created by Kirill on 26-Jan-18.
 */

public class Palette {
    private int dark;
    private int light;
    private int primary;

    public Palette(int dark, int light, int primary) {
        this.dark = dark;
        this.light = light;
        this.primary = primary;
    }

    public Palette() {
        primary = -14192464;
        dark = -15713712;
        light = -5383689;
    }

    public int getDarkColor() {
        return dark;
    }

    public int getLightColor() {
        return light;
    }

    public int getPrimaryColor() {
        return primary;
    }
}
