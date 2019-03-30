package com.luyucheng.acgclub;

import android.content.Context;


public class Application extends android.app.Application {
    public static Application mContext;

    public static Context getInstance() {
        return mContext;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = this;
    }
}
