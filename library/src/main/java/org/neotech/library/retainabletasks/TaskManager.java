package org.neotech.library.retainabletasks;

import android.support.annotation.MainThread;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;

/**
 * Created by Rolf on 29-2-2016.
 */
public abstract class TaskManager {

    /**
     * The TaskAttachListener interface can be used to restore UI state before an actual listener is
     * attached to the task which may cause the task it to deliver its result. Its highly
     * recommended to use the TaskAttachListener instead of manually getting a task from the
     * TaskManager, restore the UI and set the Callback listener.
     */
    public interface TaskAttachListener {
        /**
         * Called prior to the task being attached to its (new) Callback listener. You should
         * restore the UI in this method, based on the tasks current state. This method will only be
         * called if the Task is available and hence did not deliver its result. A delivered result
         * is either a call to the listeners onPostExecute or onCanceled method.

         * @param task the Task which needs to be attached to a listener.
         * @return The new listener to attach the Task to.
         */
        Task.Callback onPreAttach(@NonNull Task<?, ?> task);
    }

    /**
     * Returns the Task instance identified by the given tag if still hold by the TaskManger. This
     * method returns null if the given task did deliver its result (success or canceled).
     * @param tag the unique Task identifier.
     * @return the Task identified by the given tag or null.
     */
    @MainThread
    public abstract Task<?, ?> getTask(@NonNull String tag);

    @MainThread
    public abstract Task<?, ?> attachListener(@NonNull String tag, @NonNull Task.Callback callback);

    @MainThread
    public abstract Task<?, ?> attachListener(@NonNull String tag, @NonNull TaskAttachListener attachListener);

    @MainThread
    public abstract Task<?, ?> attachListener(@NonNull Task<?, ?> task, @NonNull Task.Callback callback);

    @MainThread
    public abstract Task<?, ?> cancel(@NonNull String tag);

    @MainThread
    public abstract <Progress, Result> void execute(@NonNull Task<Progress, Result> task, @NonNull Task.Callback callback);

    @MainThread
    public abstract boolean isResultDelivered(@NonNull String tag);

    @MainThread
    public abstract boolean isRunning(@NonNull String tag);

    private static volatile BaseTaskManager globalInstance;

    /**
     * Returns the {@link TaskManager} associated with the Activity the given FragmentManger belongs to. You
     * should never try to supply a child/Fragment FragmentManger here.
     * @param manager The FragmentManger of the Activity you would like to get the TaskManager for.
     * @return The TaskManager instance associated with the given Activity's FragmentManger.
     */
    @MainThread
    public static TaskManager getActivityTaskManager(@NonNull FragmentManager manager){
        return TaskFragment.getInstance(manager);
    }

    /**
     * Returns the global TaskManager. The global TaskManager isn't bound to the lifecycle of an
     * Activity and won't automatically remove a {@link Task.Callback} listeners set to
     * {@link Task Tasks} which are executed trough this TaskManager. You should be aware of this
     * behaviour and remove any callback listeners to avoid leaking an Activity, Fragment, View etc.
     * Good use cases for the global TaskManager are Tasks which don't necessarily need to update
     * the UI. But in most cases you really want to use the Activity TaskManager.
     * @return The global TaskManager instance.
     * @see TaskManager#getActivityTaskManager(FragmentManager)
     */
    public static TaskManager getGlobalTaskManager(){
        /**
         * Double checked locking/synchronization.
         * Without the volatile keyword the first if statement could fail, because the state of the
         * static field would not necessary be reflected by other threads.
         */
        if(globalInstance != null){
            return globalInstance;
        }
        synchronized (TaskManager.class){
            if(globalInstance == null){
                globalInstance = new BaseTaskManager();
            }
            return globalInstance;
        }
    }
}
