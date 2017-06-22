# Android-Retainable-Tasks Documentation

**Index**

1. [Basic usage](#1-basic-usage)
2. [How it works](#2-how-it-works)
3. [Tips and tricks](#3-tips-and-tricks)
4. [FAQ](#4-faq)


## 1. Basic usage
To execute a task (which is bound to the user-interface and is automatically retained across configuration changes) you need to do follow these 4 simple steps:

**Annotation based**

1. Create an implementation of the `Task` class;
2. Make your Activity extend the `TaskActivityCompat` class (or for Fragments the `TaskFragmentCompat` class);
3. Create one or more annotated methods with the following annotations: `@TaskPreExecute`, `@TaskPostExecute`, `@TaskCancel`, `@TaskProgress` and `@TaskAttach`;  
4. Execute the task using the `TaskManager`;
    
**Listener based**

1. Create an implementation of the `Task` class;
2. Make your Activity extend the `TaskActivityCompat` class (or for Fragments the `TaskFragmentCompat` class);
3. Implement the `Callback` interface somewhere and provide a new callback when the Activity is restarted using `TaskActivityCompat.onPreAttach()`;  
4. Execute the task using the `TaskManager` and point it to your `Callback` implementation;

> Step 1 and 2 are the same in both styles: annotation and listener based.


### 1.1 Creating the task

You will need to extend the `Task` class to create a custom task. The `Task` class is heavily based on the default Android `AsyncTask` class and you will need to override at least the `doInBackground` method and provide an appropriate constructor. 

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
        //Restore the user-interface based on the tasks state
        return this; //This Activity implements Task.Callback
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
    
    // Now this is cool, we can have a single method handle both calls,
    // also note how this method will still be called in the new
    // Activity instance after after rotation.
    @TaskPostExecute("task-id")
    public void onFinish(ExampleTask task){
    
    }
    
    @TaskCancel("task-id")
    public void onFinish(ExampleTask task){
        // Task finished
    }
}
```

> **In-depth:**
> The `TaskManger` (or actually a internal class) will detect when the Activity stops (`onStop()`). and will automatically remove all `Callback` listeners when this happens. Removing the listeners is needed to avoid memory leaks, as listeners could reference to Activities or Fragments which are about to be destroyed by the Android system. Removing the listeners in `onStop()` also avoids having tasks report their result while the Activity is in the background. As soon as `onStart()` is called the `TaskManager` takes care of setting new listeners on all the tasks. You will need to provide these new listeners when the `onPreAttach()` method is called. If annotations are used this process is a bit more automated and you won't need to override `onPreAttach()`.

### 1.4 Executing the Task
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

## 2. How it works
---
####**How are tasks retained?**
Tasks are retained across configuration changes using a similar method as `ViewModels` are being retained in the [Android Architecture Lifecycle](https://developer.android.com/reference/android/arch/lifecycle/Lifecycle.html) library. In short `Task` objects are stored in `TaskManagers` which are stored in so called *"no-ui-fragments"* (Fragments which have called `setRetainInstanceState(true)`). The creation of these no-ui-fragments happens as soon as the first call to one of these methods is made:

 - `getTaskManager()` in the following classes:
     - `TaskActivity` & `TaskActivityCompat`
     - `TaskFragment` & `TaskFragmentCompat`
 - `TaskManagerLifeCycleProxy.getTaskManager()`
 - `TaskManager.getActivityTaskManager()` <sub>(super-advanced usage)</sub>
 - `TaskManager.getFragmentTaskManager()` <sub>(super-advanced usage)</sub>

Essentially any time you request a `TaskManager`. You should however note that the library itself also internally calls these methods.

The `onStart()` and `onStop()` methods are forwarded from `Activities` and `Fragments` to the different `TaskManagers` that are retained using the no-ui-fragments. The `TaskManagers` then take care of removing and setting all `Callback` listeners from the different `Tasks`.

####**What happens when a Task without callback finishes?**
When a Task currently does not have a `Callback` listener to deliver it's results to it will wait with the delivery and deliver the results as soon as a new listener is attached. This happens for example right after the `onPreAttachTask(Task)` method returns, the newly provided `Callback` listener will then be attached to the `Task` and the `Task` immediately fires the listener. This all happens during the Activity or Fragment `onStart()` method and you need to be sure that at this point the user-interface is ready. If you manually call one of the `TaskManager.attach()` methods a `Task` might also immediately fire the new listener.

**Important:** Only the `onPostExecute()` and `onCanceled()` methods will wait for delivery, other method's like `onProgressUpdate` won't wait for delivery and will be skipped if no `Callback` listener is attached to the `Task`. You can restore a tasks progress using the `Task.getLastKnownProgress()` method.

####**What does the Task and Callback life-cycle look like?**
A `Task` basically has four life-cycle methods *(its heavily based on Android's AsyncTask)*:

* `onPreExecute()` *[ui-thread]*
* `doInBackground()` *[executor-thread]*
* `onProgressUpdate()` *[ui-thread]*
* `onPostExecute()` or `onCanceled()` *[ui-thread]*

The `Task` and `Callback` class have these methods in common, except for the `doInBackground()` method. When a `Callback` listener is attached to a `Task`, both the `Callback` and the `Task` methods will be called. But when the listener is detached from the `Task` only the tasks methods will be called. However as stated before the `Task` will wait with calling the `onPostExecute()` and `onCanceled()` if the `Task` currently does not have a callback until a new `Callback` is attached to the `Task`.


## 3. Tips and tricks
---
Besides the basics there are some more advanced API's you will probably need.

####**Getting task results**
Unlike the default Android `AsyncTask` implementation you don't get `Task` results as a parameter, instead you will need to call the `Task.getResult()` method, which returns the tasks result.

####**Getting the tasks current state**
The Android `AsyncTask` API provides the `AsyncTask.getStatus()` method which returns an enum value which can be used to determinate the tasks current state. Instead of using that method combined with an enum you can use on of the following methods:

* `isFinished()`
* `isRunning()`
* `isReady()`
* `isResultDelivered()`
* `isCanceled()`

####**Getting the tasks last progress update**
To get the tasks most recent progress update use the `getLastKnownProgress()` method, this method returns null when no last know progress is available.

####**AdvancedCallback**
If you need the `onProgressUpdated` and `onCanceled` callback methods you can implement the `AdvancedCallback` interface, which is an extension of the `Callback` interface.

####**TaskExecutor & Executor**
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

####**Using the TaskManagerLifeCycleProxy to mimic the TaskActivityCompat**
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

## 4. FAQ

####**Why does the Task class still have the onPostExecute and onPreExecute etc. methods?**

Although the `Callback` interface provides these methods sometimes you don't need any callback to the Activity's user-interface, at that moment the Task methods come in handy. It also gives a `Task` the change to modify it's state or store it's progress values, for example:
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