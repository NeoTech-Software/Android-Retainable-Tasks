package org.neotech.app.retainabletasksdemo;

import android.app.Application;

import com.squareup.leakcanary.LeakCanary;

import org.neotech.library.retainabletasks.TaskManager;

/**
 * Created by Rolf on 29-2-2016.
 */
public final class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        if (LeakCanary.isInAnalyzerProcess(this)) {
            // This process is dedicated to LeakCanary for heap analysis.
            // You should not init your app in this process.
            return;
        }
        LeakCanary.install(this);
        TaskManager.setStrictDebugMode(true);
    }
}
