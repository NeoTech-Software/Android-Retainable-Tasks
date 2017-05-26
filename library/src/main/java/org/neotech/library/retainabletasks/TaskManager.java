package org.neotech.library.retainabletasks;

import android.app.Activity;
import android.support.annotation.CheckResult;
import android.support.annotation.MainThread;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;

import org.neotech.library.retainabletasks.internal.BaseTaskManager;
import org.neotech.library.retainabletasks.internal.TaskRetainingFragment;
import org.neotech.library.retainabletasks.internal.TaskRetainingFragmentLegacy;
import org.neotech.library.retainabletasks.internal.TaskRetainingFragmentLogicLegacy;

import java.util.concurrent.Executor;

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
    public abstract Task<?, ?> attach(@NonNull String tag, @NonNull Task.Callback callback);

    @MainThread
    public abstract Task<?, ?> attach(@NonNull String tag, @NonNull TaskAttachListener attachListener);

    @MainThread
    public abstract Task<?, ?> attach(@NonNull Task<?, ?> task, @NonNull Task.Callback callback);

    @MainThread
    public abstract void attachAll(@NonNull Task.Callback callback, @NonNull String... tags);

    @MainThread
    public abstract void attachAll(@NonNull TaskAttachListener attachListener, @NonNull String... tags);

    @MainThread
    public abstract Task<?, ?> detach(@NonNull String tag);

    @MainThread
    public abstract void detachAll(@NonNull String... tags);

    @MainThread
    public abstract Task<?, ?> cancel(@NonNull String tag);

    /**
     * Start the given task on the default Executor ({@link TaskExecutor#getDefaultExecutor()}). The
     * Task life-cycle events will be delivered to the class owning ({@link TaskManagerOwner}) this
     * {@link TaskManager}.
     *
     * @param task The Task to execute.
     * @see TaskExecutor#setDefaultExecutor(Executor)
     */
    @MainThread
    public abstract <Progress, Result> void execute(@NonNull Task<Progress, Result> task);

    /**
     * Start the given task on the given {@link Executor}. The Task life-cycle events will be
     * delivered to the class owning ({@link TaskManagerOwner}) this {@link TaskManager}.
     *
     * @param task The Task to execute.
     * @param executor The Executor to execute the given Task with.
     */
    @MainThread
    public abstract <Progress, Result> void execute(@NonNull Task<Progress, Result> task, @NonNull Executor executor);

    /**
     * Start the given task on the default Executor
     * ({@link TaskExecutor#getDefaultExecutor()}). The Task life-cycle events will be delivered to
     * the given {@link Task.Callback} listener.
     *
     * @param task The Task to execute.
     * @param callback The Callback listener to deliver the Task events to.
     * @see TaskExecutor#setDefaultExecutor(Executor)
     * @deprecated This method has been deprecated, please use {@link #execute(Task)} instead which
     * supports annotation based Task life-cycle callbacks.
     */
    @MainThread
    @Deprecated
    public abstract <Progress, Result> void execute(@NonNull Task<Progress, Result> task, @NonNull Task.Callback callback);

    /**
     * Start the given task on {@link Executor}
     * ({@link TaskExecutor#setDefaultExecutor(Executor)}). The Task life-cycle events will be
     * delivered to the given {@link Task.Callback} listener.
     *
     * @param task The Task to execute.
     * @param callback The Callback listener to deliver the Task events to.
     * @param executor The Executor to execute the given Task with.
     * @deprecated This method has been deprecated, please use {@link #execute(Task, Executor)}
     * instead which supports annotation based Task life-cycle callbacks.
     */
    @MainThread
    @Deprecated
    public abstract <Progress, Result> void execute(@NonNull Task<Progress, Result> task, @NonNull Task.Callback callback, @NonNull Executor executor);

    /**
     * Checks if the {@link Task} with the given tag has delivered it's result.
     * @param tag The tag which identifies the Task to check.
     * @return true if the Task did deliver it's result, false if the Task did not deliver it's
     * result or the task is not found using {@link #getTask(String)}.
     * @see Task#isResultDelivered()
     * @deprecated Please use {@link #getTask(String)} in combination with
     * {@link Task#isResultDelivered()} to check whether a task is running.
     */
    @Deprecated
    @MainThread
    @CheckResult
    public abstract boolean isResultDelivered(@NonNull String tag);

    /**
     * Checks if the {@link Task} with the given tag is running.
     * @param tag The tag which identifies the Task to check.
     * @return true if the Task is running ({@link Task#doInBackground()} is executing), false if
     * the Task is not running or the task is not found using {@link #getTask(String)}.
     * @see Task#isRunning()
     * @deprecated Please use {@link #getTask(String)} in combination with {@link Task#isRunning()}
     * to check whether a task is running.
     */
    @Deprecated
    @MainThread
    @CheckResult
    public abstract boolean isRunning(@NonNull String tag);


    /**
     * Get if this TaskManager contains an active {@link Task} for the given tag. A {@link Task} is
     * considered active if the Task is either waiting to be executed, running or waiting to deliver
     * it's result. Calling this method is actually the same as calling {@link #getTask(String)}
     * with a null check.
     * @param tag The tag which identifies the Task to check.
     */
    @MainThread
    @CheckResult
    public abstract boolean isActive(@NonNull String tag);

    /**
     * Debug use only. Checks if all {@link Task Tasks} added to this TaskManager have been
     * detached.
     * @throws IllegalStateException if one or more Tasks are not detached.
     */
    @MainThread
    public abstract void assertAllTasksDetached() throws IllegalStateException;

    private static volatile BaseTaskManager globalInstance;
    protected static volatile boolean strictDebug = false;

    /**
     * Enables or disables the strict debug mode. When this mode is enabled the TaskManager does
     * some extra checking and throws exceptions if it detects faulty behaviour. You're encouraged
     * to use this mode when testing your app. Strict mode is disabled by default.
     * @param strictDebug true if the strict mode should be enabled, false if it should be disabled.
     */
    public static void setStrictDebugMode(boolean strictDebug){
        TaskManager.strictDebug = strictDebug;
    }

    /**
     * Returns whether the strict mode is enabled. By default the strict mode is disabled.
     * @return true if strict mode is enabled, false if not.
     * @see TaskManager#setStrictDebugMode(boolean)
     */
    public static boolean isStrictDebugModeEnabled(){
        return strictDebug;
    }

    /**
     * Returns the {@link TaskManager} associated with the given Fragment.
     * @param fragment The Fragment to get the TaskManager for.
     * @return The TaskManager instance associated with the given Fragment.
     */
    @MainThread
    public static TaskManager getFragmentTaskManager(@NonNull Fragment fragment){
        return TaskRetainingFragment.holderFragmentFor(fragment).getTaskManager();
    }

    /**
     * Returns the {@link TaskManager} associated with the given Fragment.
     * @param fragment The Fragment to get the TaskManager for.
     * @return The TaskManager instance associated with the given Fragment.
     */
    @MainThread
    public static TaskManager getFragmentTaskManager(@NonNull android.app.Fragment fragment){
        final String tag = getTaskManagerTag(fragment.getTag());
        return getTaskManager(TaskRetainingFragmentLegacy.holderFragmentFor(fragment), tag);
    }

    private static TaskManager getTaskManager(@NonNull TaskRetainingFragmentLogicLegacy taskRetainingFragment, @NonNull String tag){
        BaseTaskManager manager = (BaseTaskManager) taskRetainingFragment.findTaskManagerByTag(tag);
        if(manager == null){
            manager = new BaseTaskManager();
            taskRetainingFragment.registerTaskManager(tag, manager);
        }
        return manager;
    }

    private static String getTaskManagerTag(@Nullable String fragmentTag) {
        if (fragmentTag != null) {
            return "tag:" + fragmentTag;
        }
        throw new IllegalArgumentException("In order to associate a TaskManger with a Fragment the Fragment needs to have a tag.");
    }


    /**
     * Returns the {@link TaskManager} associated with the Activity the given FragmentManger belongs to. You
     * should never try to supply a child/Fragment FragmentManger here.
     * @param activity The Activity you would like to get the TaskManager for.
     * @return The TaskManager instance associated with the given Activity's FragmentManger.
     */
    @MainThread
    public static TaskManager getActivityTaskManager(@NonNull FragmentActivity activity){
        return TaskRetainingFragment.holderFragmentFor(activity).getTaskManager();
    }

    /**
     * Returns the {@link TaskManager} associated with the Activity the given FragmentManger belongs to. You
     * should never try to supply a child/Fragment FragmentManger here.
     * @param manager The FragmentManger of the Activity you would like to get the TaskManager for.
     * @return The TaskManager instance associated with the given Activity's FragmentManger.
     */
    @MainThread
    public static TaskManager getActivityTaskManager(@NonNull Activity manager){
        return TaskRetainingFragmentLegacy.holderFragmentFor(manager).getTaskManager();
    }

    /**
     * Returns the global TaskManager. The global TaskManager isn't bound to the lifecycle of an
     * Activity and won't automatically remove a {@link Task.Callback} listeners set to
     * {@link Task Tasks} which are executed trough this TaskManager. You should be aware of this
     * behaviour and remove any callback listeners to avoid leaking an Activity, Fragment, View etc.
     * Good use cases for the global TaskManager are Tasks which don't necessarily need to update
     * the UI. But in most cases you really want to use the Activity TaskManager.
     * @return The global TaskManager instance.
     * @see TaskManager#getActivityTaskManager(Activity)
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
