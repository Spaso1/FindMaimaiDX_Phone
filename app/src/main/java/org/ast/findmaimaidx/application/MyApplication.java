package org.ast.findmaimaidx.application;

import android.app.Application;

public class MyApplication extends Application {
    private static MyApplication instance;
    private String sessionId;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
    }

    public static synchronized MyApplication getInstance() {
        return instance;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }
}
