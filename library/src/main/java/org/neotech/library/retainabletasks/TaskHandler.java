package org.neotech.library.retainabletasks;

import android.support.annotation.MainThread;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;

/**
 * Created by Rolf on 29-2-2016.
 */
public abstract class TaskHandler {

    @MainThread
    public abstract Task<?, ?> getTask(@NonNull String tag);

    @MainThread
    public abstract Task<?, ?> attachListener(@NonNull String tag, @NonNull Task.Callback callback);

    @MainThread
    public abstract Task<?, ?> cancel(@NonNull String tag);

    @MainThread
    public abstract <Progress, Result> void execute(@NonNull Task<Progress, Result> task, @NonNull Task.Callback callback);

    @MainThread
    public abstract boolean isRunning(@NonNull String tag);

    private static volatile BaseTaskHandler globalInstance;

    /**
     * Returns the {@link TaskHandler} associated with the Activity the given FragmentManger belongs to. You
     * should never try to supply a child/Fragment FragmentManger here.
     * @param manager The FragmentManger of the Activity you would like to get the TaskHandler for.
     * @return The TaskHandler instance associated with the given Activity's FragmentManger.
     */
    @MainThread
    public static TaskHandler getActivityTaskHandler(@NonNull FragmentManager manager){
        return TaskFragment.getInstance(manager);
    }

    /**
     * Returns the global TaskHandler. The global TaskHandler isn't bound to the lifecycle of an
     * Activity and won't automatically remove
     * {@link org.neotech.library.retainabletasks.Task.Callback} set to
     * {@link android.support.v4.app.NotificationManagerCompat.Task Tasks} which are executed
     * trough this TaskHandler. You should be aware of this behaviour and remove any callback, which
     * might leak an Activity, Fragment, View etc., yourself. Good use cases for the global
     * TaskHandler are Tasks which don't necessarily need to update the UI. But in most cases you
     * really want to use the Activity TaskHandler.
     * @return The global TaskHandler instance.
     * @see #getActivityTaskHandler(FragmentManager)
     */
    public static TaskHandler getGlobalTaskHandler(){
        /**
         * Double checked locking/synchronization.
         * Without the volatile keyword the first if statement could fail, because the state of the
         * static field would not necessary be reflected by other threads.
         */
        if(globalInstance != null){
            return globalInstance;
        }
        synchronized (TaskHandler.class){
            if(globalInstance == null){
                globalInstance = new BaseTaskHandler();
            }
            return globalInstance;
        }
    }
}
