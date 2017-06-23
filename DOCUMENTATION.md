# Android-Retainable-Tasks Documentation
When you are new to this library the advice is to read at least chapter *"1. Basic usage"*. Besides this documentation make sure to try out the sample app and check the source code. If something is not clear to you or if something is wrong within the documentation please don't keep it for yourself but instead create and issue or even a pull-request, thanks.

## Index

1. [Basic usage](#1-basic-usage)
    1. [Creating the task](#1-1-creating-the-task)
    2. [Extending from the TaskActivityCompat class](#1-2-extending-from-the-taskactivitycompat-class)
    3. [Callback](#1-3-callback)
    4. [Executing the task](#1-4-executing-the-task)
2. [Tips and tricks](#2-tips-and-tricks)
    1. [Getting the task result](#1-getting-the-task-result)
    2. [Getting the task state](#2-getting-the-task-state)
    3. [Getting the task last progress update](#3-getting-the-task-last-progress-update)
    4. [AdvancedCallback](#4-advancedcallback)
    5. [Annotations outside Activities or Fragments](#5-annotations-outside-activities-or-fragments)
    6. [TaskExecutor & Executor](#6-taskexecutor-executor)
    7. [Using the TaskManagerLifeCycleProxy to mimic the TaskActivityCompat](#7-using-the-taskmanagerlifecycleproxy-to-mimic-the-taskactivitycompat)
3. [How it works](#3-how-it-works)
4. [FAQ](#4-faq)


## 1. Basic usage
To execute a task (which is bound to the user-interface and is automatically retained across configuration changes) you need to do follow these 4 simple steps:


1. Create an implementation of the `Task` class;
2. Make your Activity extend the `TaskActivityCompat` class (or for Fragments the `TaskFragmentCompat` class);

**Annotation based**

<ol start="3">
  <li>Create one or more annotated methods with the following annotations: <code>@TaskPreExecute</code>, <code>@TaskPostExecute</code>, <code>@TaskCancel</code>, <code>@TaskProgress</code> and <code>@TaskAttach</code>;</li>
  <li>Execute the task using the <code>TaskManager</code>;</li>
</ol>


**Listener based**

<ol start="3">
  <li>Implement the <code>Callback</code> interface somewhere and provide a new callback when the Activity is restarted using <code>TaskActivityCompat.onPreAttach()</code>;</li>
  <li>Execute the task using the <code>TaskManager</code> and point it to your <code>Callback</code> implementation;</li>
</ol>


### 1.1 Creating the task

You will need to extend the `Task` class to create a custom task. The `Task` class is heavily based on the default Android </code>
```java
private class ExampleTask extends Task<Integer, String> {
    
    public ExampleTask(String tag){
        super(tag);
    }
    
    protected String doInBackground() {
        for(int i = 0; i < 100; i++) {
            if(isCancelled()){
                break;
            }
            SystemClock.sleep(50);
            publishProgress(i);
        }
        return "Result";
    }
}
```

>**Note:** 
>The `Task` class does not come with a generic type for input parameters like the Android `AsyncTask`, instead you should provide input when constructing the `Task` (using the constructor for example).

### 1.2 Extending from the TaskActivityCompat class
The `TaskActivityCompat` class is the easiest way to use this library, make sure your Activity extends from it and it wil take care of retaining all `Tasks` started by the Activity's `TaskManager`.

```java
public class Main extends TaskActivityCompat {

}
```

>**Help, I already extend some custom Activity implementation!** 
>Don't worry, you can easily add the `TaskActivityCompat` behaviour to any Activity or Fragment by using the `TaskManagerLifeCycleProxy` class . Check out [this sample](#using-the-taskmanagerlifecycleproxy-to-mimic-the-taskactivitycompat).

### 1.3 Callback
When working with annotations this step is slightly easier. In order to get feedback from your `Task` you either need to implement the `Callback` interface or create annotated methods. When working with
the `Callback` interface you also need to provide the `TaskManager` with an `Callback` instance after a configuration change occurred such as rotation, you can do this by overriding the `TaskActivityCompat.onPreAttach()` method. When working with annotation you don't need to do this as this is done automatically based on the annotations.

**Listener based**

```java
public class Main extends TaskActivityCompat implements Task.Callback {
    
    @Override
    public Task.Callback onPreAttach(Task<?, ?> task) {
        if("activity-unique-tag".equals(task.getTag())) {
            //Restore the user-interface based on the tasks state
            return this; //This Activity implements Task.Callback for the given task
        }
         // Other (unknown) tasks, code won't reach this if you execute just one task
        return null;
    }
    
    @Override
    public void onPreExecute(Task<?, ?> task) {
        //Task started
    }
    
    @Override
    public void onPostExecute(Task<?, ?> task) {
        //Task finished
    }
}
```

**Annotation based**  

```java
public class Main extends TaskActivityCompat {
    
    @TaskPreExecute("activity-unique-tag")
    public void onStart(ExampleTask task){
        // Task started
    }
    
    @TaskPostExecute("activity-unique-tag")
    public void onFinish(ExampleTask task){
        // Task finished
    }
}
```

> **In-depth:**
> The `TaskManger` (or actually a internal class) will detect when the Activity stops (`onStop()`). and will automatically remove all `Callback` listeners when this happens. Removing the listeners is needed to avoid memory leaks, as listeners could reference to Activities or Fragments which are about to be destroyed by the Android system. Removing the listeners in `onStop()` also avoids having tasks report their result while the Activity is in the background. As soon as `onStart()` is called the `TaskManager` takes care of setting new listeners on all the tasks. You will need to provide these new listeners when the `onPreAttach()` method is called. If annotations are used this process is a bit more automated and you won't need to override `onPreAttach()`.

### 1.4 Executing the task
Executing a `Task` is extremely easy, just obtain an instance of the `TaskManager` and call one of the `execute()` methods. When working with `Callback` listeners instead of annotations you will need to provide the initial `Callback` listener when executing a `Task`, when working with annotations this is done automatically. Preferably your activity implements the `Callback` interface when working without annotations, but this isn't necessarily needed.

**Listener based**

```java
public class Main extends TaskActivityCompat implements Task.Callback {
    
    @Override
    public void onClick(View view){
        // 'this' class is also our listener
        getTaskManager().execute(new ExampleTask("activity-unique-tag"), this);
    }
}
```

**Annotation based**

```java
public class Main extends TaskActivityCompat implements Task.Callback {
    
    @Override
    public void onClick(View view){
        // No need to provide a listener when working with annotations.
        getTaskManager().execute(new ExampleTask("activity-unique-tag"));
    }
}
```


>**Tip:**
>You can also make your Fragment extend the `TaskFragmentCompat` class and use a Fragment to execute and retain your task in. It works exactly the same, but keep in mind that the `Callback` listeners are removed as soon as the Fragments stops (`onStop()`). 


## 2. Tips and tricks
---
Besides the basics there are some more advanced API's you will probably need.

####**1. Getting the task result**
Unlike the default Android `AsyncTask` implementation you don't get `Task` results as a parameter, instead you will need to call the `Task.getResult()` method, which returns the tasks result.

####**2. Getting the task state**
The Android `AsyncTask` API provides the `AsyncTask.getStatus()` method which returns an enum value which can be used to determinate the tasks current state. Instead of using that method combined with an enum you can use on of the following methods:

* `isFinished()`
* `isRunning()`
* `isReady()`
* `isResultDelivered()`
* `isCanceled()`

####**3. Getting the task last progress update**
To get the tasks most recent progress update use the `getLastKnownProgress()` method, this method returns null when no last know progress is available.

####**4. AdvancedCallback**
If you need the `onProgressUpdated` and `onCanceled` callback methods you can implement the `AdvancedCallback` interface, which is an extension of the `Callback` interface.

####**5. Annotations outside Activities or Fragments**
By default annotated methods are only resolved if they are added to a `TaskActivityCompat` or `TaskFragmentCompat`, but you can register custom classes using the `TaskActivityCompat.bindTaskTarget()` method you must call this method as soon as possible, for example in the constructor of the Activity to prevent missing callbacks. You obviously need to re-register the object when a configuration change occurs like rotation.

####**6. TaskExecutor & Executor**
You can also execute tasks without using a `TaskManager` this means that you are responsible for removing and setting the `Callback` listener. Executing tasks without using the `TaskManager` is handy when you don't necessarily need to get any feedback to the user-interface.

```java
TaskExecutor.executeParallel(new ExampleTask());
```
```java
TaskExecutor.executeSerial(new ExampleTask());
```
```java
//Alias for calling executeParallel
TaskExecutor.execute(new ExampleTask());
```

You can also use a custom java `Executor` to execute tasks with:

```java
TaskExecutor.executeOnExecutor(new ExampleTask(), yourExecutor);
```

####**7. Using the TaskManagerLifeCycleProxy to mimic the TaskActivityCompat**
If you already use some custom Activity or Fragment implementation you might not be able to use the `TaskActivityCompat` or `TaskFragmentCompat` class. To overcome this problem you can implement the behaviour of the `TaskActivityCompat` yourself using the `TaskManagerLifeCycleProxy` class.

Create a new `TaskManagerLifeCycleProxy` instance and let your Activity (or Fragment) implement the `TaskManagerOwner` interface. Override the`onStart()` and `onStop()` methods and proxy those together with the `getTaskManager()` method  to the `TaskManagerLifeCycleProxy` instance.

```java
public class MyBaseActivity extends SomeActivity implements TaskManagerOwner {

    private TaskManagerLifeCycleProxy proxy = new TaskManagerLifeCycleProxy(this);

    @Override
    protected void onStart() {
        super.onStart();
        proxy.onStart();
    }

    @Override
    protected void onStop() {
        proxy.onStop();
        super.onStop();
    }

    @Override
    public TaskManager getTaskManager() {
        return proxy.getTaskManager();
    }

    @Override
    public Task.Callback onPreAttach(@NonNull Task<?, ?> task) {
        return null;
    }
}
```


## 3. How it works
How this library works is not extremely complicated it can however be quite difficult to understand correctly if you have limited knowledge about the Android Activity and Fragment life-cycle and how Android manages these objects.

####**How are task objects retained?**
The first thing to understand is how `Task` objects are kept alive when the Activity is destroyed by the system when a configuration change occurs (like rotation). Often used solutions include using static variables, the `Application` class or the `Activity.onRetainNonConfigurationInstance()` method  to store objects in. These methods are however non optimal and Google suggests using the Fragment API in combination with `Fragment.setRetainInstanceState(true)` instead. Using the Fragment API avoids keeping objects alive after the Activity or Fragment is destroyed for good which can happen when the `Application` class or static variables are used. Google demonstrates the practise of using the Fragment API in combination with `setRetainInstanceState(true)` in the excellent [Android Architecture Lifecycle](https://developer.android.com/reference/android/arch/lifecycle/Lifecycle.html) library.

This library leverages the same principle and uses so called *"no-ui-fragments"* which are retained using `setRetainInstanceState(true)` to store objects in. The objects stored in these Fragments are `TaskManager` objects which in their turn store `Task` objects. The creation of these no-ui-fragments happens as soon as the first call to one of these methods is made:

 - `getTaskManager()` in the following classes:
     - `TaskActivity` & `TaskActivityCompat`
     - `TaskFragment` & `TaskFragmentCompat`
 - `TaskManagerLifeCycleProxy.getTaskManager()`
 - `TaskManager.getActivityTaskManager()` <sub>(super-advanced usage)</sub>
 - `TaskManager.getFragmentTaskManager()` <sub>(super-advanced usage)</sub>

Essentially any time you request a `TaskManager`. You should however note that the library itself also internally calls these methods.

####**What about the Callback listeners?**
Each `Task` has a listener attached to it (`Callback` interface), these listeners must not leak between Activity instances, especially since a listener might hold a reference to an Activity causing it to leak the Activity object. Therefor the `TaskManager` makes sure listeners must be removed from a task as soon as the Activity is being destroyed. A new listener is attached to the `Task` when the new Activity is created, so that the `Task` can still report it's result. To prevent Tasks from reporting their results before the Activity is started the `Callback` listeners are removed in `onStop()` and attached in `onStart()`.

The new listeners that need to be attached to a `Task` are acquired during `onStart()` the Activity (or Fragment) TaskManager will call the `onPreAttach(Task)` method which then should return the new listeners for that specific `Task`. When annotations are used `Callback` listeners are automatically generated at compile time and automatically attached to `Tasks` so there is no need to override `onPreAttach(Task)`.

####**What happens when a Task without Callback finishes?**
When a `Task` does not have a `Callback` listener attached to it (after `onStop()` and before `onStart()` is called) it will skip/wait with the delivery and deliver the results as soon as a new listener is attached. This happens right after the `onPreAttach(Task)` method returns, as the `TaskManager` will immediately attach the newly provided `Callback` listener to the `Task` and the `Task` can immediately fire the listener if it was waiting for it. Because this all happens during the Activity or Fragment `onStart()` you need to be sure that at this point the user-interface is ready. If you manually call one of the `TaskManager.attach()` methods a `Task` might also immediately fire the new listener.

**Important:** Only the `onPostExecute()` and `onCanceled()` methods will wait for delivery, other method's like `onProgressUpdate` won't wait for delivery and will be skipped if no `Callback` listener is attached to the `Task`. You can restore a tasks progress using the `Task.getLastKnownProgress()` method.

####**How does the Task and Callback life-cycle work?**
A `Task` basically has four life-cycle methods *(its heavily based on Android's AsyncTask)*:

* `onPreExecute()` *[ui-thread]*
* `doInBackground()` *[executor-thread]*
* `onProgressUpdate()` *[ui-thread]*
* `onPostExecute()` or `onCanceled()` *[ui-thread]*

The `Task` and `Callback` class have these methods in common, except for the `doInBackground()` method. When a `Callback` listener is attached to a `Task`, both the `Callback` and the `Task` methods will be called. But when the listener is detached from the `Task` only the tasks methods will be called. However as stated before the `Task` will wait with calling the `onPostExecute()` and `onCanceled()` methods if the `Task` currently does not have a listener attached to it.


## 4. FAQ

####**Why does the Task class have the same methods that are already available in the Callback interface?**

Although the `Callback` interface provides these methods sometimes you don't need any callback to the Activity's user-interface, in these scenarios the `Task` methods come in handy. It also gives a `Task` the change to modify it's state or store it's progress values, for example:

```java
private class VerySimpleTask extends Task<Integer, Integer> {

    private final ArrayList<Integer> progressValues = new ArrayList<>();
    
    public ExampleTask(String tag){
        super(tag);
    }
	
    @Override
    protected Boolean doInBackground() {
	    for(int i = 0; i < 10; i++){
            publishProgress(i);
            SystemClock.sleep(500);
        }
        return 10;
    }
	
    @Override
    protected void onProgressUpdate(Integer value) {
        progressValues.add(value);
    }
    
    public List<Integer> getProgressCache(){
         // This method is safe to call on the ui-thread because the
         // onProgressUpdate method is executed on the same thread.
        return progressValues;
    }
}
```