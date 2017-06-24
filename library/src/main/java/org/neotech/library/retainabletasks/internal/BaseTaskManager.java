package org.neotech.library.retainabletasks.internal;

import android.os.Looper;
import android.support.annotation.MainThread;
import android.support.annotation.NonNull;
import android.support.annotation.RestrictTo;
import android.util.Log;
import android.util.Pair;

import org.neotech.library.retainabletasks.Task;
import org.neotech.library.retainabletasks.TaskExecutor;
import org.neotech.library.retainabletasks.TaskManager;
import org.neotech.library.retainabletasks.TaskManagerOwner;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;

/**
 * Created by Rolf on 29-2-2016.
 */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
public final class BaseTaskManager extends TaskManager {

    private static final String TAG = "BaseTaskManager";

    private final HashMap<String, Task<?, ?>> tasks = new HashMap<>();
    private boolean isUIReady = true;
    private WeakReference<TaskManager.TaskAttachListener> initialCallbackProvider;

    /**
     * Set the initial callback provider ({@link TaskManager.TaskAttachListener}). The
     * {@link TaskManagerOwner} is responsible for setting the initial callback provider as soon as
     * it takes ownership of the TaskManager and should preferably set it to null as soon as the
     * ownership is ended.
     * @param initialCallbackProvider the initial callback provider that the {@link BaseTaskManager}
     *                                should use to resolve the initial {@link Task.Callback} for a
     *                                given {@link Task}.
     */
    public void setInitialCallbackProvider(TaskManager.TaskAttachListener initialCallbackProvider){
        this.initialCallbackProvider = new WeakReference<>(initialCallbackProvider);
    }

    /**
     * Returns the initial callback provider ({@link TaskManager.TaskAttachListener}). The initial
     * callback provider must be used when no {@link Task.Callback} is provided when
     * {@link #execute} is called.
     * @return The initial callback provider or null if the TaskManager is currently not associated
     * with a {@link TaskManagerOwner}.
     */
    private @NonNull TaskManager.TaskAttachListener getInitialCallbackProvider() {
        final TaskManager.TaskAttachListener attachListener = initialCallbackProvider == null ? null : initialCallbackProvider.get();
        if(attachListener == null){
            throw new IllegalStateException("Cannot call TaskManager.execute() after the TaskManagerOwner (Activity, Fragment etc.) instance does no longer own the TaskManager (has been destroyed).");
        }
        return attachListener;
    }

    @Override
    public Task<?, ?> getTask(@NonNull String tag) {
        return tasks.get(tag);
    }

    public void attach(TaskAttachListener taskManagerOwner){
        if(TaskManager.isStrictDebugModeEnabled()){
            assertMainThread();
        }
        /*
         * If this loop would directly set new Callback listeners it could cause a task to deliver
         * its result. Meaning that the removeFinishedTask method can be called causing the 'tasks'
         * Map to change which results in a ConcurrentModificationException. Hence the second loop
         * for actually attaching the Callback listeners.
         *
         * This behaviour has been confirmed on Android 2.2 (API 8).
         *
         * Note: synchronization is not needed (and wouldn't help) as all methods (attach,
         * removeFinishedTask, etc.) are supposed to be called on the UI thread.
         */
        final ArrayList<Pair<Task<?, ?>, Task.Callback>> attachQueue = new ArrayList<>(tasks.size());

        for (Map.Entry<String, Task<?, ?>> task : tasks.entrySet()) {
            Task.Callback callback = taskManagerOwner.onPreAttach(task.getValue());
            if (callback == null) {
                throw new IllegalArgumentException("Could not attach Task '" + task.getKey() + "' because onPreAttach did not return a valid Callback listener! Did you override onPreAttach()?");
            }
            attachQueue.add(new Pair<Task<?, ?>, Task.Callback>(task.getValue(), callback));
        }
        for(Pair<Task<?, ?>, Task.Callback> task: attachQueue){
            attach(task.first, task.second);
        }
    }

    @Override
    @MainThread
    public Task<?, ?> attach(@NonNull String tag, @NonNull Task.Callback callback){
        final Task<?, ?> task = tasks.get(tag);
        if(task == null){
            return null;
        }
        return attach(task, callback);
    }

    @Override
    @MainThread
    public Task<?, ?> attach(@NonNull String tag, @NonNull TaskAttachListener listener){
        final Task<?, ?> task = tasks.get(tag);
        if(task == null){
            return null;
        }
        return attach(task, listener.onPreAttach(task));
    }

    @Override
    public Task<?, ?> attach(@NonNull Task<?, ?> task, @NonNull Task.Callback callback) {
        if(TaskManager.isStrictDebugModeEnabled()){
            assertMainThread();
        }
        task.setCallback(new CallbackShadow(callback));
        return task;
    }

    @Override
    public void attachAll(@NonNull Task.Callback callback, @NonNull String... tags) {
        for(String tag: tags){
            attach(tag, callback);
        }
    }

    @Override
    public void attachAll(@NonNull TaskAttachListener attachListener, @NonNull String... tags) {
        for(String tag: tags){
            attach(tag, attachListener);
        }
    }

    @Override
    public Task<?, ?> detach(@NonNull String tag) {
        if(TaskManager.isStrictDebugModeEnabled()){
            assertMainThread();
        }
        final Task<?, ?> task = tasks.get(tag);
        if(task != null){
            task.removeCallback();
        }
        return task;
    }

    @Override
    public void detachAll(@NonNull String... tags) {
        for(String tag: tags){
            detach(tag);
        }
    }

    @MainThread
    public <Progress, Result> void execute(@NonNull Task<Progress, Result> task){
        execute(task, getInitialCallbackProvider().onPreAttach(task), TaskExecutor.getDefaultExecutor());
    }

    @MainThread
    public <Progress, Result> void execute(@NonNull Task<Progress, Result> task, @NonNull Executor executor){
        execute(task, getInitialCallbackProvider().onPreAttach(task), executor);
    }

    @Override
    @MainThread
    public <Progress, Result> void execute(@NonNull Task<Progress, Result> task, @NonNull Task.Callback callback){
        execute(task, callback, TaskExecutor.getDefaultExecutor());
    }

    @Override
    public <Progress, Result> void execute(@NonNull Task<Progress, Result> task, @NonNull Task.Callback callback, @NonNull Executor executor) {
        if(TaskManager.isStrictDebugModeEnabled()){
            assertMainThread();
        }
        final Task currentTask = tasks.get(task.getTag());
        if(currentTask != null && currentTask.isRunning()){
            throw new IllegalStateException("Task with an equal tag: '" + task.getTag() + "' has already been added and is currently running or finishing.");
        }
        tasks.put(task.getTag(), task);
        if(isUIReady){
            task.setCallback(new CallbackShadow(callback));
        } else {
            task.removeCallback();
        }
        TaskExecutor.executeOnExecutor(task, executor);
    }


    @Override
    @MainThread
    public boolean isActive(@NonNull String tag) {
        if(TaskManager.isStrictDebugModeEnabled()){
            assertMainThread();
        }
        return tasks.get(tag) != null;
    }

    @Override
    @MainThread
    public boolean isResultDelivered(@NonNull String tag) {
        if(TaskManager.isStrictDebugModeEnabled()){
            assertMainThread();
        }
        Task task = tasks.get(tag);
        return task != null && task.isResultDelivered();
    }

    @Override
    @MainThread
    public boolean isRunning(@NonNull String tag) {
        if (TaskManager.isStrictDebugModeEnabled()) {
            assertMainThread();
        }
        Task task = tasks.get(tag);
        return task != null && task.isRunning();
    }

    @Override
    @MainThread
    public Task<?, ?> cancel(@NonNull String tag){
        if(TaskManager.isStrictDebugModeEnabled()){
            assertMainThread();
        }
        final Task<?, ?> task = tasks.remove(tag);
        if(task != null){
            task.cancel(false);
        }
        return task;
    }


    private static void assertMainThread() throws IllegalStateException {
        if(Looper.getMainLooper() != Looper.myLooper()){
            throw new IllegalStateException("Method not called on the UI-thread!");
        }
    }

    @Override
    @MainThread
    public void assertAllTasksDetached() throws IllegalStateException {
        for(Map.Entry<String, Task<?, ?>> entry: tasks.entrySet()){
            final Task task = entry.getValue();
            if(task.getCallback() != null){
                throw new IllegalStateException("Task '" + task.getTag() + "' isn't detached and references a Callback listener: " + task.getCallback());
            }
        }
    }

    @MainThread
    public void cancelAll(){
        if(TaskManager.isStrictDebugModeEnabled()){
            assertMainThread();
        }
        for(Map.Entry<String, Task<?, ?>> task: tasks.entrySet()){
            task.getValue().cancel(true);
        }
    }

    @MainThread
    public void detach(){
        if(TaskManager.isStrictDebugModeEnabled()){
            assertMainThread();
        }
        // The problem described in the attach(TaskManagerProvider) doesn't apply to this method as
        // all TaskManager methods should be executed on the UI-thread, meaning that no other method
        // can be called while inside this method.
        for(Map.Entry<String, Task<?, ?>> task: tasks.entrySet()){
            task.getValue().removeCallback();
        }
    }

    private void removeFinishedTask(Task expectedTask){
        final Task task = tasks.get(expectedTask.getTag());
        if (task != null && task != expectedTask) {
            Log.i(TAG, "Task '" + expectedTask.getTag() + "' has already been removed, because another task with the same tag has been added while this task was finishing.");
        } else {
            tasks.remove(expectedTask.getTag());
        }
    }

    public void setUIReady(boolean isReady){
        this.isUIReady = isReady;
    }

    private final class CallbackShadow implements Task.AdvancedCallback  {

        private final Task.Callback callback;

        CallbackShadow(Task.Callback callback) {
            this.callback = callback;
        }

        @Override
        public void onPreExecute(Task<?, ?> task) {
            callback.onPreExecute(task);
        }

        @Override
        public void onPostExecute(Task<?, ?> task) {
            removeFinishedTask(task);
            callback.onPostExecute(task);
        }

        @Override
        public void onProgressUpdate(Task<?, ?> task, Object progress) {
            if(callback instanceof Task.AdvancedCallback) {
                ((Task.AdvancedCallback) callback).onProgressUpdate(task, progress);
            }
        }

        @Override
        public void onCanceled(Task<?, ?> task) {
            removeFinishedTask(task);
            if(callback instanceof Task.AdvancedCallback) {
                ((Task.AdvancedCallback) callback).onCanceled(task);
            }
        }
    }
}
