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

/**
 * Created by Rolf on 3-3-2016.
 */
public final class TaskManagerLifeCycleProxy implements LifecycleObserver {

    private BaseTaskManager fragmentTaskManager;

    private final TaskManager.TaskAttachListener attachListener = new TaskManager.TaskAttachListener() {
        @Override
        public Task.Callback onPreAttach(@NonNull Task<?, ?> task) {
            if(generatedCodeTarget != null){
                return generatedCodeTarget.getListenerFor(task, true);
            }
            //throw new IllegalStateException("Executing task without")
            return provider.onPreAttach(task);
        }
    };

    private final TaskManager.TaskAttachListener firstAttachListener = new TaskManager.TaskAttachListener() {
        @Override
        public Task.Callback onPreAttach(@NonNull Task<?, ?> task) {
            if(generatedCodeTarget != null){
                return generatedCodeTarget.getListenerFor(task, false);
            }
            //throw new IllegalStateException("Executing task without")
            return provider.onPreAttach(task);
        }
    };

    private final TaskManagerOwner provider;
    private boolean uiReady = false;
    private TaskAttachBinding generatedCodeTarget;

    public TaskManagerLifeCycleProxy(@NonNull final TaskManagerOwner provider){
        if(!(provider instanceof FragmentActivity || provider instanceof Activity || provider instanceof Fragment || provider instanceof android.app.Fragment)){
            throw new IllegalArgumentException("The TaskManagerProvider needs to be an instance of android.app.Activity (including the derived support library activities), android.app.Fragment or android.support.v4.app.Fragment!");
        }
        this.provider = provider;

        try {
            Class classType = Class.forName(provider.getClass().getName() + "_TaskBinding");
            Constructor test = classType.getConstructor(provider.getClass());
            generatedCodeTarget = (TaskAttachBinding) test.newInstance(provider);

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

    @MainThread
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public TaskManager getTaskManager(){
        if(fragmentTaskManager != null) {
            return fragmentTaskManager;
        }
        if(provider instanceof FragmentActivity){
            fragmentTaskManager = (BaseTaskManager) TaskManager.getActivityTaskManager((FragmentActivity) provider);
        } else if(provider instanceof Fragment){
            fragmentTaskManager = (BaseTaskManager) TaskManager.getFragmentTaskManager((Fragment) provider);
        } else if(provider instanceof Activity){
            fragmentTaskManager = (BaseTaskManager) TaskManager.getActivityTaskManager((Activity) provider);
        } else if(provider instanceof android.app.Fragment){
            fragmentTaskManager = (BaseTaskManager) TaskManager.getFragmentTaskManager((android.app.Fragment) provider);
        } /* else {
            //This should never happen as the constructor checks everything.
        }
        */
        fragmentTaskManager.setDefaultCallbackProvider(firstAttachListener);
        fragmentTaskManager.setUIReady(uiReady);
        return fragmentTaskManager;
    }
}
