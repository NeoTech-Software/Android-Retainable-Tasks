# Android-Retainable-Tasks
[ ![Download](https://api.bintray.com/packages/rolf-smit/maven/android-retainable-tasks/images/download.svg) ](https://bintray.com/rolf-smit/maven/android-retainable-tasks/_latestVersion)

Android-Retainable-Tasks is an easy to use mini-library for easy asynchronous background tasking with callback support to the UI. This library is based on the Android `AsyncTask` implementation but with support for retaining tasks and therefore surviving configuration changes (orientation).

**Add it to your project**

Android-Retainable-Tasks is available on jCenter, just add the following compile dependency to your modules build.gradle file.

```groovy
dependencies {
    compile 'org.neotech.library:android-retainable-tasks:0.1.0'
}
```

**Index**

1. [Basic usage](#1-basic-usage)
2. [How it works](#2-how-it-works)
3. [Advanced usage](#3-advanced-usage)
4. [FAQ](#4-faq)

## 1. Basic usage
---
In order to, execute a task which modifies the user-interface and to retain it across configuration changes you will need to do three things:

1. Create an implementation of the `Task` class;
2. Execute the task using the `TaskHandler` in an`Activity` which implements the `Callback` interface;
3. Retain the task when configuration changes by overriding the `onStart()` method and calling the `TaskHandler.attachListener()` method;

The Activity's `TaskHandler` makes sure that tasks can be retained across configuration changes and is responsible for removing the `Callback` listener when the Activity's user-interface is no longer valid. You are however responsible for re-attaching a new `Callback` listener when the Activity 's user-interface is restarted.

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


### 1.2 Executing the task
Before you can execute the `ExampleTask` you first need to get the current Activity's `TaskHandler`. A `TaskHandler` keeps references to tasks and executes them. You can obtain the current Activity's `TaskHandler` using the `TaskHandler.getActivityTaskHandler()` method:

```java
public TaskHandler getTaskHandler(){
    return TaskHandler.getActivityTaskHandler(getSupportFragmentManager());
}
```

Then you can execute the task using the `TaskHandler.execute()` method. This method needs two arguments, the task to execute and a `Callback` listener to send feedback to. Preferably your activity implements the `Callback` interface, but this isn't necessarily needed.

```java
public class Main extends AppCompatActivity implements Task.Callback {

	@Override
	public void onClick(View view){
		ExampleTask task = new ExampleTask("activity-unique-tag");
		getTaskHandler().execute(task, this);
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

### 1.2 Retaining the task
When the configuration changes (device rotates) the Activity's `TaskHandler` will automatically remove the `Callback` listeners from all active tasks. This is needed otherwise the tasks could leak Activity, Fragment or other references.

> **In-depth:**
> The `TaskHandler` will automatically remove the `Callback` listeners when the Activity is stopping (`onStop()`). At this moment the user-interface has become *"unstable"*, for example, when this happens the FragmentManager refuses to add new Fragments because the Activity's `onSaveInstanceState()` method has already been called. If the `Callback` listener is not removed by the `TaskHandler` before this point, then a `DialogFragment.show()` call will throw an exception when called in the `onPostExecute()` method. This is why the `Callback` listeners are removed when the Activity stops.

To retain the task when your Activity is recreated you will need to re-attach a (new) `Callback` listener using the `TaskHandler.attachListener()` method. The best place to do this is in the `onStart()` method right after the user-interface has been created.


```java
@Override
public void onStart(){
    super.onStart();
    //Re-attach the this Activity as listener for task
    getTaskHandler().attachListener("activity-unique-tag", this);
}
```

## 2. How it works
---
####**Retaining tasks**
Tasks retained using the described method are stored in a *"no-ui-fragment"* this fragment retained across configuration changes and is added to your Activity's `FragmentManager` the first time you call `TaskHandler.getActivityTaskHandler()`. This fragment is from that point on bound to the Activity's life-cycle and holds an internal `TaskHandler`. The fragment makes sure that the internal `TaskHandler` removes all `Callback` listeners as soon as the Activity is stopping (`onStop()`).

####**Task without callback finishes**
When a Task doesn't have a `Callback` listener to deliver it's results to it will skip the delivery and redeliver the results as soon as a new listener is attached. If you call the `TaskHandler.attachListener()` method in the `onStart()` method, then the listener will be fired and you need to be sure that the user-interface is ready.

Only the `onPostExecute()` and `onCanceled()` methods will be redelivered, other methodes won't be redelivered.

####**Task and Callback life-cycle**
A `Task` basically has four life-cycle methods:

* `onPreExecute()` *[ui-thread]*
* `doInBackground()` *[executor-thread]*
* `onProgressUpdate()` *[ui-thread]*
* `onPostExecute()` or `onCanceled()` *[ui-thread]*

A `Callback` listener has the same life-cycle methods as the`Task` and reflects those methods to, for example, an Activity. All `Callback` methods are executed on the user interface thread. When a `Callback` listener is attached to the task, both the `Callback` and the `Task` methods will be called. But when the listener is detached from the task only the tasks methods will be called. With the exception of the `onPostExecute()` and `onCanceled()` methods which can be redelivered.


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
You can also execute tasks without using a `TaskHandler` this means that you are responsible for removing and setting the `Callback` listener. Executing tasks without using the `TaskHandler` is handy when you don't need to get any feedback to the Activity's user-interface.

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

## 4. FAQ

####**Why does the Task class still have the onPostExecute and onPreExecute etc. methods?**

Although the `Callback` interface provides these methods sometimes you don't need any callback to the Activity's user-interface, at that moment the Task methods come in handy.
```java
private class VerySimpleTask extends Task<Void, Boolean> {

    private final File fileToRemove;
    private final Context context;
    
    public ExampleTask(String tag, Context context, File fileToRemove){
		super(tag);
		this.fileToRemove = fileToRemove;
		this.context = context.getApplicationContext();
	}
	
	@Override
	protected String doInBackground() {
		return fileToRemove.delete();
	}
	
	@Override
	protected void onPostExecute(){
		Toast.makeText(context, "Removed file: " + getResult(), Toast.LENGTH_SHORT).show();
    }
	
}
```