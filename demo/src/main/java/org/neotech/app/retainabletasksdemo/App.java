package org.neotech.app.retainabletasksdemo;

import android.app.Application;

import com.squareup.leakcanary.LeakCanary;

/**
 * Created by Rolf on 29-2-2016.
 */
public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        LeakCanary.install(this);
    }
}
