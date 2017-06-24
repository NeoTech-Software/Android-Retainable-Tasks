package org.neotech.library.retainabletasks.internal.utilities;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import android.support.annotation.RestrictTo;

/**
 * A simple class that implements the methods in the {@link Application.ActivityLifecycleCallbacks}
 * interface but does nothing. So you can easily override only the methods you need :)
 *
 * Created by Rolf Smit on 22-May-17.
 */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
class EmptyActivityLifecycleCallbacks implements Application.ActivityLifecycleCallbacks {

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {

    }

    @Override
    public void onActivityStarted(Activity activity) {

    }

    @Override
    public void onActivityResumed(Activity activity) {

    }

    @Override
    public void onActivityPaused(Activity activity) {

    }

    @Override
    public void onActivityStopped(Activity activity) {

    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

    }

    @Override
    public void onActivityDestroyed(Activity activity) {

    }
}
