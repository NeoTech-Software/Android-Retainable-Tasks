package org.neotech.library.retainabletasks.internal.utilities;

import android.app.Activity;
import android.app.Application;
import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Build;
import androidx.annotation.RestrictTo;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Rolf Smit on 23-May-17.
 *
 * This class is a legacy version of the {@link HolderFragmentManager} class which in turn is
 * based upon Google's Lifecycle HolderFragment.FragmentHolderManager class, which can be found in
 * the Lifecycle Architecture library.
 *
 * This class differs from the {@link HolderFragmentManager} quite allot because for legacy
 * fragments there is no way to monitor their lifecycle because
 * {@link androidx.fragment.app.FragmentManager.FragmentLifecycleCallbacks} is a support library
 * only feature.
 *
 */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
public final class HolderFragmentManagerLegacy<F extends Fragment> {

    private static final String TAG = HolderFragmentManagerLegacy.class.getSimpleName();

    private final String desiredTagInFragmentManager;
    private final Class<F> holderFragmentClass;

    private final Map<Activity, F> mNotCommittedActivityHolders = new HashMap<>();

    public HolderFragmentManagerLegacy(Class<F> holderFragmentClass, String desiredTagInFragmentManager){
        this.desiredTagInFragmentManager = desiredTagInFragmentManager;
        this.holderFragmentClass = holderFragmentClass;
    }

    private Application.ActivityLifecycleCallbacks mActivityCallbacks =
            new EmptyActivityLifecycleCallbacks() {
                @Override
                public void onActivityDestroyed(Activity activity) {
                    F fragment = mNotCommittedActivityHolders.remove(activity);
                    if (fragment != null) {
                        Log.e(TAG, "Failed to save a ViewModel for " + activity);
                    }
                }
            };

    private boolean mActivityCallbacksIsAdded = false;

    private F findHolderFragment(FragmentManager manager) {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN_MR1 && manager.isDestroyed()) {
            throw new IllegalStateException("Can't access ViewModels from onDestroy");
        }

        return (F) manager.findFragmentByTag(desiredTagInFragmentManager);
    }

    private F createHolderFragmentInstance(){
        try {
            return holderFragmentClass.newInstance();
        } catch (InstantiationException e) {
            throw new IllegalStateException("Could not create an instance of the given holder fragment class, make sure the class has an public empty constructor!", e);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("Could not create an instance of the given holder fragment class, make sure the class has an public empty constructor!", e);
        }
    }

    private F createHolderFragment(FragmentManager fragmentManager) {
        F holder = createHolderFragmentInstance();
        fragmentManager.beginTransaction().add(holder, desiredTagInFragmentManager).commitAllowingStateLoss();
        return holder;
    }

    public F retainableFragmentFor(Activity activity) {
        FragmentManager fm = activity.getFragmentManager();
        F holder = findHolderFragment(fm);
        if (holder != null) {
            return holder;
        }
        holder = mNotCommittedActivityHolders.get(activity);
        if (holder != null) {
            return holder;
        }

        if (!mActivityCallbacksIsAdded) {
            mActivityCallbacksIsAdded = true;
            activity.getApplication().registerActivityLifecycleCallbacks(mActivityCallbacks);
        }
        holder = createHolderFragment(fm);
        mNotCommittedActivityHolders.put(activity, holder);
        return holder;
    }

    public F retainableFragmentFor(Fragment fragment) {
        final Activity root;
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            //Get the root activity
            while (fragment.getParentFragment() != null) {
                fragment = fragment.getParentFragment();
            }
            root = fragment.getActivity();
        } else {
            root = fragment.getActivity();
        }

        return retainableFragmentFor(root);
    }
}
