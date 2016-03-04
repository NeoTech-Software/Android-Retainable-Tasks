# Android-Retainable-Tasks
[ ![Download](https://api.bintray.com/packages/rolf-smit/maven/android-retainable-tasks/images/download.svg) ](https://bintray.com/rolf-smit/maven/android-retainable-tasks/_latestVersion)

*Version 0.2.0 has quite some changes compared to version 0.1.0, so please read this document very carefully.*

Android-Retainable-Tasks is an easy to use mini-library for easy asynchronous background tasking with callback support to the UI. This library is based on the Android `AsyncTask` implementation but with support for retaining tasks and therefore surviving configuration changes (orientation).

*Key features:*

 - Light weight
 - Same Task API on all Android versions, based on the Marshmallow AsyncTask implementation.
 - Simple API
 - Supports API 9+ <sub>(or 11+ if you not use the support library based classes)</sub>

**Add it to your project**

Android-Retainable-Tasks is available on jCenter, just add the following compile dependency to your modules build.gradle file.

```groovy
dependencies {
    compile 'org.neotech.library:android-retainable-tasks:0.2.0'
}
```

**Index**

1. [Basic usage](#1-basic-usage)
2. [How it works](#2-how-it-works)
3. [Advanced usage](#3-advanced-usage)
4. [FAQ](#4-faq)
5. [ToDo](#5-to-do)

>**Why use this library?**

>*Always! its awesome!!!*

>This library is useful if you need to do stuff in the background which is heavily bound to the user-interface, like: Refreshing large lists from a database, loading an article from a network source or decoding an image. You need to use an additional library if you need: task scheduling, automatic retries, task persistence across reboots, task constrains (network availability) etc.




## 1. Basic usage
---
In order to, execute a task which modifies the user-interface and to retain it across configuration changes you will need to do three things:

1. Create an implementation of the `Task` class;
2. Make your Activity extend the `TaskActivityCompat` class (or for Fragments the `TaskFragmentCompat` class);
3. Implement the `Callback` interface somewhere and execute the task using the `TaskManager`;


### 1.1 Creating the task

You will need to extend the `Task` class to create a custom task. The `Task` class is heavily based on the default Android `AsyncTask` class and you will need to override at least the `doInBackground` method and provide an appropriate constructor. 

>**Note:** 
>A`Task` doesn't come with a generic type for input parameters like the Android `AsyncTask`, instead you should provide input when constructing the `Task` (using the constructor for example).

**Example:**

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

### 1.2 Extending from the TaskActivityCompat class
The `TaskActivityCompat` class is the most easy way to use this library, make sure your Activity extends from it and it wil take care of retaining all `Tasks` started by the Activity's `TaskManager`.

```java
public class Main extends TaskActivityCompat {

}
```

>**Help, I already extend some custom Activity implementation!** 
>Don't worry, you can easily add the `TaskActivityCompat` behaviour to any Activity or Fragment by using the `TaskManagerLifeCycleProxy` class . Check out [this sample](#using-the-taskmanagerlifecycleproxy-to-mimic-the-taskactivity).

### 1.3 Execute the Task and receive callback
Before you can execute the `ExampleTask` you first need to get the current Activity's `TaskManager`. A `TaskManager` keeps references to tasks and executes them. You can obtain the current Activity's `TaskManager` using the `TaskActivityCompat.getTaskManager()` method.

Then you can execute the task using the `TaskManager.execute()` method. This method needs two arguments, the task to execute and a `Callback` listener to send feedback to. Preferably your activity implements the `Callback` interface, but this isn't necessarily needed. The `TaskManager` currently always executes tasks in parallel *(work in progress to make it a option)*.


```java
public class Main extends AppCompatActivity implements Task.Callback {

	@Override
	public void onClick(View view){
		ExampleTask task = new ExampleTask("activity-unique-tag");
		getTaskManager().execute(task, this);
	}

	@Override
    public void onPreExecute(Task<?, ?> task) {
        //Task started
    }

    @Override
    public void onPostExecute(Task<?, ?> task) {
        //Task finished
        Toast.makeText(this, "Task finished", Toast.LENGTH_SHORT).show();
    }
}
```


>**Tip:**
>You can also make your Fragment extend the `TaskFragmentCompat` class and use a Fragment to execute and retain your task in. It works exactly the same, but keep in mind that the `Callback` listeners are removed as soon as the Fragments stops (`onStop()`). 


### 1.4 Retaining the task
When the configuration changes (device rotates) the `TaskManager` will automatically remove the `Callback` listeners from all active tasks and retain all `Tasks`. Removing the `Callback` is needed otherwise the tasks could leak Activity, Fragment or other references. 

> **In-depth:**
> The `TaskManger` (or actually a internal class) will detect when the Activity stops (`onStop()`). and will automatically remove all `Callback` listeners when this happens. At this moment the user-interface has become *"unstable"*,  this means that some user-interface functionality stops working. For example, the `FragmentManager` refuses at this point to add new Fragments because the Activity's `onSaveInstanceState()` method already has been called. If the `Callback` listener is not removed by the `TaskManager` before this point, then a `DialogFragment.show()` call will throw an exception when called in the `onPostExecute()` method. This is why the `Callback` listeners are removed when the Activity stops.

Although tasks are automatically retained, you will still need to provide a new `Callback` listener for each `Task`. You can easily do this by implementing (overriding) the `TaskActivityCompat` (or `TaskFragmentCompat`) `onPreAttachTask(Task)` method and return a `Callback` instance. At this point you can also use the `onPreAttachTask(Task)` method to restore the user-interface state according to the `Tasks` state. The `onPreAttachTask(Task)` method will be called for each task that isn't finished (didn't deliver).

```java
public class Main extends AppCompatActivity implements Task.Callback {

    @Override
    public Task.Callback onPreAttach(Task<?, ?> task) {
	    //Restore the user-interface based on the tasks state
        return this; //This Activity implements Task.Callback
    }
}
```


## 2. How it works
---
####**How are tasks retained?**
Tasks are are stored in `FragmentManagers` which are stored in a *"no-ui-fragment"* this fragment retained across configuration changes and is added to your Activity's `FragmentManager` the first time you call:

 - `getTaskManager()` in the following classes:
     - `TaskActivity` & `TaskActivityCompat`
     - `TaskFragment` & `TaskFragmentCompat`
 - `TaskManagerLifeCycleProxy.getTaskManager()`
 - `TaskManager.getActivityTaskManager()` <sub>(super-advanced usage)</sub>
 - `TaskManager.getFragmentTaskManager()` <sub>(super-advanced usage)</sub>

Essentially any time you request a `TaskManager`.

The *"no-ui-fragment"* is from that point on bound to the Activity's life-cycle and keeps track of all `TaskManager` instances. It also makes sure that all internal `TaskManagers` remove all `Callback` listeners as soon as the Activity is stopping (`onStop()`). It might also throw an exception if a `Fragment` `TaskManger` did not remove the `Callback` listeners, so that you (the developer) know you've messed up.

####**What happens when a Task without callback finishes?**
When a Task doesn't have a `Callback` listener to deliver it's results to it will skip the delivery and redeliver the results as soon as a new listener is attached. This happens for example when the `onPreAttachTask(Task)` method returns, the newly provided `Callback` listener will be fired and you need to be sure that the user-interface is ready. It might also happen if you manually call one of the `TaskManager.attach()` methods (advanced usage).

**Important:** Only the `onPostExecute()` and `onCanceled()` methods will be redelivered, other methodes won't be redelivered. You can restore a tasks progress using the `Task.getLastKnownProgress()` method.

####**What does the Task and Callback life-cycle look like?**
A `Task` basically has four life-cycle methods *(its heavily based on Android's AsyncTask)*:

* `onPreExecute()` *[ui-thread]*
* `doInBackground()` *[executor-thread]*
* `onProgressUpdate()` *[ui-thread]*
* `onPostExecute()` or `onCanceled()` *[ui-thread]*

A `Callback` listener has the same life-cycle methods as the`Task`. All `Callback` methods are executed on the user interface thread. When a `Callback` listener is attached to the task, both the `Callback` and the `Task` methods will be called. But when the listener is detached from the task only the tasks methods will be called. With exception of the `onPostExecute()` and `onCanceled()` methods which can be redelivered.


## 3. Advanced usage
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
To get the tasks most recent progress update use the `getLastKnownProgress()` method.

####**AdvancedCallback**
If you need the `onProgressUpdated` and `onCanceled` callback methods you can implement the `AdvancedCallback` interface, which is an extension of the `Callback` interface.

####**TaskExecutor & Executor**
You can also execute tasks without using a `TaskManager` this means that you are responsible for removing and setting the `Callback` listener. Executing tasks without using the `TaskManager` is handy when you don't perse need to get any feedback to the user-interface.

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

####**Using the TaskManagerLifeCycleProxy to mimic the TaskActivity**
If you already use some custom Activity or Fragment implementation you might not be able to use the `TaskActivity` or `TaskFragment` class. To overcome this problem you can implement the behaviour of the `TaskActivity` yourself using the `TaskManagerLifeCycleProxy` class.

Create a new `TaskManagerLifeCycleProxy` instance and let your Activity (or Fragment) implement the `TaskManagerProvider` interface. Override the`onStart()` and `onStop()` methods and proxy those together with the `getTaskManager()` method  to the `TaskManagerLifeCycleProxy` instance.

```java
public class MyBaseActivity extends SomeActivity implements TaskManagerProvider {

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
        /**
         * This method is safe to call on the ui-thread because the
         * onProgressUpdate method is executed on the same thread.
         */
        return progressValues;
    }
}
```

## 5. To-Do

*“As long as I am breathing, in my eyes, I am just beginning.”*

 - Add custom Executors to the TaskManager;
 - Finish documentation;
 - Write real tests for the library besides having only a demo app;
