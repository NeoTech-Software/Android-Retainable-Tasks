package org.neotech.library.retainabletasks;

import android.annotation.TargetApi;
import android.app.Activity;
import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LifecycleObserver;
import android.arch.lifecycle.OnLifecycleEvent;
import android.os.Build;
import android.support.annotation.MainThread;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;

import org.neotech.library.retainabletasks.internal.BaseTaskManager;
import org.neotech.library.retainabletasks.internal.TaskAttachBinding;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.HashSet;

/**
 * Created by Rolf on 3-3-2016.
 */
public final class TaskManagerLifeCycleProxy implements LifecycleObserver {

    private static final HashMap<Class<?>, Constructor> BINDING_CACHE = new HashMap<>();
    private BaseTaskManager taskManager;

    /**
     * This {@link TaskManager.TaskAttachListener} is used to find the {@link Task.Callback}
     * listener for a specific task when it's being re-attached.
     */
    private final TaskManager.TaskAttachListener attachListener = new TaskManager.TaskAttachListener() {
        @Override
        public Task.Callback onPreAttach(@NonNull Task<?, ?> task) {
            return handleAttach(task, true);
        }
    };

    /**
     * This {@link TaskManager.TaskAttachListener} is set on the TaskManager that this
     * TaskManagerLifeCycleProxy owns and is used by the TaskManager to find the initial
     * {@link Task.Callback} listener for a specific task that has just been added for execution.
     */
    private final TaskManager.TaskAttachListener initialAttachListener = new TaskManager.TaskAttachListener() {
        @Override
        public Task.Callback onPreAttach(@NonNull Task<?, ?> task) {
            return handleAttach(task, false);
        }
    };

    private final TaskManagerOwner provider;
    private boolean uiReady = false;
    private final TaskAttachBinding generatedCodeTarget;
    private HashSet<TaskAttachBinding> additionalBindings = new HashSet<>();

    public TaskManagerLifeCycleProxy(@NonNull final TaskManagerOwner provider){
        if(!(provider instanceof FragmentActivity || provider instanceof Activity || provider instanceof Fragment || provider instanceof android.app.Fragment)){
            throw new IllegalArgumentException("The TaskManagerProvider needs to be an instance of android.app.Activity (including the derived support library activities), android.app.Fragment or android.support.v4.app.Fragment!");
        }
        this.provider = provider;
        this.generatedCodeTarget = getBindingFor(provider);
    }

    private Task.Callback handleAttach(Task<?, ?> task, boolean isReAttach){
        if(generatedCodeTarget != null){
            Task.Callback callback = generatedCodeTarget.getListenerFor(task, isReAttach);
            if(callback != null){
                return callback;
            }
        }
        for(TaskAttachBinding binding: additionalBindings){
            Task.Callback callback = binding.getListenerFor(task, isReAttach);
            if(callback != null){
                return callback;
            }
        }
        // Fallback to the provider implementation.
        return provider.onPreAttach(task);
    }

    public void bindTaskTarget(Object object){
        final TaskAttachBinding binding = TaskManagerLifeCycleProxy.getBindingFor(object);
        if(binding != null) {
            additionalBindings.add(binding);
        }
    }

    private static TaskAttachBinding getBindingFor(Object target){
        //noinspection TryWithIdenticalCatches (not supported prior to Android API 19)
        try {
            Constructor bindingClassConstructor = BINDING_CACHE.get(target.getClass());
            if(bindingClassConstructor == null) {
                final Class<?> classType = Class.forName(target.getClass().getName() + "_TaskBinding");
                bindingClassConstructor = classType.getConstructor(target.getClass());
                BINDING_CACHE.put(target.getClass(), bindingClassConstructor);
            }
            return (TaskAttachBinding) bindingClassConstructor.newInstance(target);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        return null;
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    @MainThread
    public void onStart(){
        uiReady = true;
        ((BaseTaskManager) getTaskManager()).setUIReady(uiReady);
        ((BaseTaskManager) getTaskManager()).attach(attachListener);
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    @MainThread
    public void onStop(){
        uiReady = false;
        ((BaseTaskManager) getTaskManager()).setUIReady(uiReady);
        ((BaseTaskManager) getTaskManager()).detach();
    }

    /**
     * This method is optional to call and only clear a reference which is already set as "weak".
     * Calling this method might improve GC performance?
     */
    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    @MainThread
    public void onDestroy(){
        // The TaskManager is retained so remove our reference to it, even though the
        // BaseTaskManager already holds this things as "weak" reference.
        ((BaseTaskManager) getTaskManager()).setInitialCallbackProvider(null);
    }

    @MainThread
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public TaskManager getTaskManager(){
        if(taskManager != null) {
            return taskManager;
        }
        if(provider instanceof FragmentActivity){
            taskManager = (BaseTaskManager) TaskManager.getActivityTaskManager((FragmentActivity) provider);
        } else if(provider instanceof Fragment){
            taskManager = (BaseTaskManager) TaskManager.getFragmentTaskManager((Fragment) provider);
        } else if(provider instanceof Activity){
            taskManager = (BaseTaskManager) TaskManager.getActivityTaskManager((Activity) provider);
        } else if(provider instanceof android.app.Fragment){
            taskManager = (BaseTaskManager) TaskManager.getFragmentTaskManager((android.app.Fragment) provider);
        }
        assert taskManager != null;
        // else case is not needed as everything is checked by the constructor.

        // Because the TaskManager is retained across configuration changes this call could leak the
        // TaskAttachListener, but because the BaseTaskManager implementation keeps a weak-reference
        // to the TaskAttachListener it doesn't leak the TaskAttachListener. This however means that
        // the TaskManagerLifeCycleProxy must keep a strong-reference to the TaskAttachListener.
        taskManager.setInitialCallbackProvider(initialAttachListener);
        taskManager.setUIReady(uiReady);
        return taskManager;
    }
}
