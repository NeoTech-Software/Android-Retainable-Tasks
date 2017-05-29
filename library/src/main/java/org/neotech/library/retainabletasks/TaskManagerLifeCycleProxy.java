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
import android.util.Log;

import org.neotech.library.retainabletasks.internal.BaseTaskManager;
import org.neotech.library.retainabletasks.internal.TaskAttachBinding;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashSet;

/**
 * Created by Rolf on 3-3-2016.
 */
public final class TaskManagerLifeCycleProxy implements LifecycleObserver {

    private BaseTaskManager taskManager;

    private final TaskManager.TaskAttachListener attachListener = new TaskManager.TaskAttachListener() {
        @Override
        public Task.Callback onPreAttach(@NonNull Task<?, ?> task) {
            if(generatedCodeTarget != null){
                Task.Callback callback = generatedCodeTarget.getListenerFor(task, true);
                if(callback != null){
                    return callback;
                }
                for(TaskAttachBinding binding: additionalBindings){
                    callback = binding.getListenerFor(task, false);
                    if(callback != null){
                        return callback;
                    }
                }
            }
            //throw new IllegalStateException("Executing task without")
            return provider.onPreAttach(task);
        }
    };

    private final TaskManager.TaskAttachListener firstAttachListener = new TaskManager.TaskAttachListener() {
        @Override
        public Task.Callback onPreAttach(@NonNull Task<?, ?> task) {
            if(generatedCodeTarget != null){
                Task.Callback callback = generatedCodeTarget.getListenerFor(task, false);
                if(callback != null){
                    return callback;
                }
                for(TaskAttachBinding binding: additionalBindings){
                    callback = binding.getListenerFor(task, false);
                    if(callback != null){
                        return callback;
                    }
                }

            }
            //throw new IllegalStateException("Executing task without")
            return provider.onPreAttach(task);
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

        TaskAttachBinding binding = null;
        try {
            Class classType = Class.forName(provider.getClass().getName() + "_TaskBinding");
            Constructor test = classType.getConstructor(provider.getClass());
            binding = (TaskAttachBinding) test.newInstance(provider);

            Log.d("Test", "classType: " + classType);
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
        generatedCodeTarget = binding;
    }

    public void addCustomBinding(TaskAttachBinding binding){
        additionalBindings.add(binding);
    }

    public static TaskAttachBinding loadBindingFor(Object target){
        try {
            Class classType = Class.forName(target.getClass().getName() + "_TaskBinding");
            Constructor test = classType.getConstructor(target.getClass());
            return (TaskAttachBinding) test.newInstance(target);
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
        ((BaseTaskManager) getTaskManager()).setDefaultCallbackProvider(null);
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
        // Else case not needed as everything is checked by the constructor.

        // Because the TaskManager is retained across configuration changes this call leaks the
        // TaskAttachListener but the BaseTaskManager implementation keeps a weak reference to the
        // TaskAttachListener which makes sure it doesn't leak the TaskAttachListener.
        taskManager.setDefaultCallbackProvider(firstAttachListener);
        taskManager.setUIReady(uiReady);
        return taskManager;
    }
}
