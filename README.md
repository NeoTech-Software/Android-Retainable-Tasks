# Android-Retainable-Tasks
An easy to use micro-library for easy asynchronous background tasking with callbacks to the UI. This library is based on the Android AsyncTask implementation but with support for configuration changes (orientation) and callbacks to the UI. It also support custom Executors.

Usage
--------
Extend the `Task` class to build a custom task. The `Task` class is heavily based on the default Android `AsyncTask` class and you will need to override the `doInBackground` method. Note that the `Task` class doesn't come with a generic type for input parameters, you should provide input when constructing the `Task` instance (using the constructor for example).

```java
private class ExampleTask extends Task<Integer, String> {

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

Then execute the task using the TaskExecutor. You can also execute tasks using a custom Executor.

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

Using the TaskExecutor directly (like in the above example) means that you won't get any feedback to the UI.

The preferred way to use a the `Task` class is by using the `TaskHandler.getActivityTaskHandler(FragmentManger)` method which returns a TaskHandler. The TaskHandler which is returned is loosely coupled to the Activity life-cycle using a internal Fragment which is retained across configuration changes. The `TaskHandler` makes sure you can retain your tasks across configuration changes.

```java
public class Main extends Activity implements Task.Callback, View.OnClickListener {
	private static final String TASK_DEMO = "demo-task";

	//... onCreate etc.

	public TaskHandler getTaskHandler(){
		return TaskHandler.getActivityTaskHandler(getSupportFragmentManager());
	}

	protected void onStart() {
		super.onStart();
		//Attach this activity as listener for the Task identified by tag TASK_DEMO
		getTaskHandler().attachListener(TASK_DEMO, this);
	}

	public void onClick(View v) {
		//Create a new task and execute it through the TaskHandler,
		//making this activity instance the tasks listener.
		ExampleTask task = new ExampleTask(TASK_DEMO);
		getTaskHandler().execute(task, this);
	}

	public void onPreExecute(Task<?, ?> task) {
		//Task started
	}
	
	public void onPostExecute(Task<?, ?> task) {
		//Task finished
		//This method will be called even after a rotation change, but within the new Activity instance)
		Toast.makeText(this, "Task finished", Toast.LENGTH_SHORT).show();
	}
}
```