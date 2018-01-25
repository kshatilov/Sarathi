package me.shatilov.symlab.sarathi.mqqt;

/**
 * Created by Kirill on 24-Jan-18.
 */

public interface EasyCallable<T> {
    void accept(T param);
}
