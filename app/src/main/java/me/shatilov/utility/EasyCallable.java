package me.shatilov.utility;

/**
 * Created by Kirill on 24-Jan-18.
 *
 * Switching from JS back to Java is hard.
 * All this easy-to-use lambdas in js.
 * Java 8 made some steps to being closer to language that is pleasant to use, but ...
 * As far as my test phone doesn't support Android API 24, I cannot use java.util.function.Function.apply
 * EasyCallable makes it simple to pass single-parametrized functions as a parameters and, more importantly, execute them!
 *
 * @param <T> type of function's parameter
 */
public interface EasyCallable<T> {
    void accept(T param);
}
