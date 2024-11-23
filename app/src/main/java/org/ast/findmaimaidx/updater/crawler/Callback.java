package org.ast.findmaimaidx.updater.crawler;

public interface Callback {
    void onResponse(Object result);

    default void onError(Exception error) {
    }

}
