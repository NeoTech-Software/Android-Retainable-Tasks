package org.neotech.library.retainabletasks;

import android.os.*;
import android.support.annotation.MainThread;
import android.support.annotation.Nullable;
import android.support.annotation.WorkerThread;
import android.util.Log;

import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * <p>The Task class is based on the default Android AsyncTask implementation. An AsyncTask can't be
 * retained across configuration changes and it's very difficult to handle proper ui callbacks. This
 * class tries to solve these problems. The Task class is very much based on the AsyncTask class and
 * has a very similar usage patterns. It might come in handy to read the {@link AsyncTask}
 * documentation very carefully before using this class.</p>
 *
 * <p>Differences:</p>
 * <ul>
 *     <li>A callback listener can be added to and removed from a Task;</li>
 *     <li>Tasks can be retained and executed using a {@link TaskManager};</li>
 *     <li>No {@link AsyncTask#getStatus()} method use {@link #isReady()}, {@link #isRunning()},
 *     {@link #isFinished()} etc. methods;</li>
 *     <li>Tasks are executed using a {@link TaskExecutor};</li>
 *     <li>No generic input Params, you should set any input before executing the task;</li>
 *     <li>No Result method parameter, you should use the {@link #getResult()} method;</li>
 *     <li>Any callback listener which is an instance of an Activity, Fragment, View etc. needs to
 *     be removed and re-added when the application changes configuration. Otherwise the Task leaks
 *     those components, most of this process is automatic if you use a {@link TaskManager};</li>
 *     <li>{@link #publishProgress(Object)} takes a single unit as parameter instead of multiple;</li>
 * </ul>
 *
 * <h2>Usage</h2>
 * <p>The Task class must be subclassed to be used. The subclass will override at least
 * one method ({@link #doInBackground}) and the constructor. Sometimes you may want to override
 * {@link #onPostExecute}, {@link #onPreExecute} or {@link #onProgressUpdate(Object)} these
 * methods are called on the ui-thread but are not actually meant for touching any views as the Task
 * might be attached to a new Activity instance, leaking those views. For updating view state you
 * will either need to use {@link org.neotech.library.retainabletasks.Task.Callback} or
 * {@link org.neotech.library.retainabletasks.Task.AdvancedCallback}.</p>
 *
 * <p>Here is an example of subclassing:</p>
 * <pre class="prettyprint">
 * private class ExampleTask extends Task&lt;Integer, String&gt; {
 *     protected String doInBackground() {
 *         for(int i = 0; i &lt; 100; i++) {
 *              if(isCancelled()){
 *                  break;
 *              }
 *              SystemClock.sleep(50);
 *              publishProgress(i);
 *          }
 *          return "Result";
 *     }
 * }
 * </pre>
 *
 * <p>Once created, a task is executed very simply:</p>
 * <pre class="prettyprint">
 * TaskExecutor.execute(new ExampleTask());
 * </pre>
 *
 * <p>The problem is that you don't get any feedback for the user interface when executing tasks
 * like this. The preferred way is to use a TaskManager which is loosely coupled to the Activity
 * lifecycle using a internal Fragment which is retained accros configuration changes.</p>
 *
 * <pre class="prettyprint">
 * public class Main extends Activity implements Task.Callback, View.OnClickListener {
 *
 *     private static final String TASK_DEMO = "demo-task";
 *
 *     //... onCreate etc.
 *
 *     public TaskManager getTaskManager(){
 *         return TaskManager.getActivityTaskManager(getSupportFragmentManager());
 *     }
 *
 *     protected void onStart() {
 *         super.onStart();
 *         //Attach this activity as listener for the Task identified by tag TASK_DEMO
 *         getTaskManager().attachListener(TASK_DEMO, this);
 *     }
 *
 *     public void onClick(View v) {
 *         //Create a new task and execute it through the TaskManager,
 *         //making this activity instance the tasks listener.
 *         ExampleTask task = new ExampleTask(TASK_DEMO);
 *         getTaskManager().execute(task, this);
 *     }
 *
 *     public void onPreExecute(Task&lt;?, ?&gt; task) {
 *         //Task started
 *     }
 *
 *     public void onPostExecute(Task&lt;?, ?&gt; task) {
 *         //Task finished
 *         Toast.makeText(this, "Task finished", Toast.LENGTH_SHORT).show();
 *     }
 * }
 * </pre>
 *
 * <h2>AsyncTask's generic types</h2>
 * <p>The three types used by an asynchronous task are the following:</p>
 * <ol>
 *     <li><code>Progress</code>, the type of the progress unit published during
 *     the background computation.</li>
 *     <li><code>Result</code>, the type of the result of the background
 *     computation.</li>
 * </ol>
 * <p>Not all types are always used by an asynchronous task. To mark a type as unused,
 * simply use the type {@link Void}:</p>
 * <pre>
 * private class MyTask extends AsyncTask&lt;Void, Void, Void&gt; { ... }
 * </pre>
 *
 * <h2>The 4 steps</h2>
 * <p>When an asynchronous task is executed, the task goes through 4 steps:</p>
 * <ol>
 *     <li>{@link #onPreExecute()}, invoked on the UI thread before the task
 *     is executed. This step is normally used to setup the task, for instance by
 *     showing a progress bar in the user interface.</li>
 *     <li>{@link #doInBackground}, invoked on the background thread
 *     immediately after {@link #onPreExecute()} finishes executing. This step is used
 *     to perform background computation that can take a long time. The parameters
 *     of the asynchronous task are passed to this step. The result of the computation must
 *     be returned by this step and will be passed back to the last step. This step
 *     can also use {@link #publishProgress} to publish one or more units
 *     of progress. These values are published on the UI thread, in the
 *     {@link #onProgressUpdate} step.</li>
 *     <li>{@link #onProgressUpdate}, invoked on the UI thread after a
 *     call to {@link #publishProgress}. The timing of the execution is
 *     undefined. This method is used to display any form of progress in the user
 *     interface while the background computation is still executing. For instance,
 *     it can be used to animate a progress bar or show logs in a text field.</li>
 *     <li>{@link #doPostExecute}, invoked on the UI thread after the background
 *     computation finishes. The result of the background computation is passed to
 *     this step as a parameter.</li>
 * </ol>
 *
 * <h2>Cancelling a task</h2>
 * <p>A task can be cancelled at any time by invoking {@link #cancel(boolean)}. Invoking
 * this method will cause subsequent calls to {@link #isCancelled()} to return true.
 * After invoking this method, {@link #doCancelled()}, instead of
 * {@link #doPostExecute()} will be invoked after {@link #doInBackground()}
 * returns. To ensure that a task is cancelled as quickly as possible, you should always
 * check the return value of {@link #isCancelled()} periodically from
 * {@link #doInBackground()}, if possible (inside a loop for instance.)</p>
 *
 * <h2>Threading rules</h2>
 * <p>For the Task class to work properly there are a few threading rules, just like the
 * Android AsyncTask implementation, that must be followed:</p>
 * <ul>
 *     <li>The task instance must be created on the UI thread.</li>
 *     <li>{@link TaskExecutor#execute} or similar calls must be invoked on the UI thread.</li>
 *     <li>Do not call {@link #onPreExecute()}, {@link #doPostExecute},
 *     {@link #doInBackground}, {@link #onProgressUpdate} manually.</li>
 *     <li>The task can be executed only once (an exception will be thrown if
 *     a second execution is attempted.)</li>
 * </ul>
 *
 * <h2>Memory observability</h2>
 * <p>Just like the standard Android AsyncTask a Task guarantees that all task lifecycle calls are
 * synchronized in such a way that the following operations are safe without explicit
 * synchronizations.</p>
 * <ul>
 *     <li>Set member fields in the constructor or {@link #onPreExecute}, and refer to them
 *     in {@link #doInBackground}.
 *     <li>Set member fields in {@link #doInBackground}, and refer to them in
 *     {@link #onProgressUpdate} and {@link #onPostExecute}.
 * </ul>
 * <p>Modifications of the member fields are also safe without explicit synchronization when
 * receiving a UI-callback using one of the callback listeners:
 * {@link org.neotech.library.retainabletasks.Task.Callback} or
 * {@link org.neotech.library.retainabletasks.Task.AdvancedCallback}</p>
 *
 *
 * <h2>Execution order</h2>
 * <p>Depending on how you execute the task execution can be both serial
 * {@link TaskExecutor#executeSerial(Task)} or parallel {@link TaskExecutor#executeParallel(Task)},
 * if executed using {@link TaskExecutor#execute(Task)} the default Executor will be used which is
 * a parallel executor.</p>
 * <p>Note: execution is by default parallel which means that the order of execution is not defined.
 * This can cause severe problems, for example: task one is meant to delete a specific old file, task
 * two creates a new file with the same name. If the order of execution is reversed the new file
 * will be delete. In this case you will need to use serial execution!
 * </p>
 */
public abstract class Task<Progress, Result> {

    private static final String TAG = "Task";

    private static final int MESSAGE_POST_RESULT = 0x1;
    private static final int MESSAGE_POST_PROGRESS = 0x2;

    private static InternalUiHandler sHandler;

    private final FutureTask<Result> mFuture;

    private volatile int mStatus = STATUS_PENDING;

    private final AtomicBoolean mCancelled = new AtomicBoolean();

    private Callback callback;
    private boolean shouldDeliverResult = false;
    private final String tag;

    private volatile Result result;
    private volatile Progress lastProgress;

    private static final byte STATUS_PENDING = 0;
    private static final byte STATUS_RUNNING = 1;
    private static final byte STATUS_FINISHED = 2;

    private static Handler getHandler() {
        synchronized (Task.class) {
            if (sHandler == null) {
                sHandler = new InternalUiHandler();
            }
            return sHandler;
        }
    }

    /**
     * Creates a new Task. This constructor must be invoked on the UI thread.
     * @param tag A unique tag, which is used for retaining and identifying tasks across
     *            configuration changes. The tag needs to be unique on Activity level if you use
     *            an Activity bounded TaskManager to execute this task. If you use an Application
     *            bounded TaskManager the tag needs to be unique across the complete Application.
     */
    @MainThread
    public Task(String tag) {
        this.tag = tag;
        final Callable<Result> mWorker = new Callable<Result>() {
            public Result call() throws Exception {
                //mTaskInvoked.set(true);
                //android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);
                //noinspection unchecked
                Result result = doInBackground();

                // Flush any pending messages send to the UI handler (like progress messages)
                Binder.flushPendingCommands();
                return result;
            }
        };

        mFuture = new FutureTask<Result>(mWorker) {
            @Override
            protected void done() {
                try {
                    postResultIfNotInvoked(get());
                } catch (InterruptedException e) {
                    Log.w(TAG, e);
                } catch (ExecutionException e) {
                    throw new RuntimeException("An error occurred while executing doInBackground()", e.getCause());
                } catch (CancellationException e) {
                    postResultIfNotInvoked(null);
                }
            }
        };
    }

    /**
     * Returns this tasks tag, which is used for retaining tasks across configuration changes.
     * @return This tasks tag.
     */
    public final String getTag(){
        return tag;
    }

    private void postResultIfNotInvoked(Result result) {
        //final boolean wasTaskInvoked = mTaskInvoked.get();
        //if (!wasTaskInvoked) {
            postResult(result);
       //}
    }

    private Result postResult(Result result) {
        this.result = result;
        Message message = getHandler().obtainMessage(MESSAGE_POST_RESULT, this);
        message.sendToTarget();
        return result;
    }

    /**
     * Method for checking if the task has finished execution. This method can return true, while
     * the result might not yet been delivered to its listening callback.
     * @return true if the task has finished execution, false if not.
     * @see Task#isResultDelivered()
     */
    public final boolean isFinished() {
        return mStatus == STATUS_FINISHED;
    }

    /**
     * Method for checking if the result of this task has been delivered to a listening callback.
     * @return true if the task is finished and has delivered its result, false if not.
     * @see Task#isFinished()
     */
    public final boolean isResultDelivered() {
        return mStatus == STATUS_FINISHED && !shouldDeliverResult;
    }

    /**
     * Method to check if the task is currently being executed. If this method returns false
     * {@link #isFinished()} will always return true, although {@link #isResultDelivered()} might
     * return false because no listener was attached to this task when it finished.
     * @return true if the task is being executed, false if not.
     * @see Task#isFinished()
     * @see Task#isResultDelivered()
     */
    public final boolean isRunning(){
        return mStatus == STATUS_RUNNING;
    }

    /**
     * Method to check if this task is ready for execution. Task can only run once.
     * @return true if the task is ready, false if the task already did run, is canceled or is
     * finished.
     */
    public final boolean isReady(){
        return mStatus == STATUS_PENDING;
    }

    /**
     * Returns the tasks result if any. This method is safe to call in {@link #onPostExecute()} task
     * lifecycle method or the callback equivalent ({@link Callback#onPostExecute(Task)}). This
     * method throws an exception if called after the task is canceled or when the task did not
     * finish execution.
     *
     * @return The tasks result as returned by {@link #doInBackground()}.
     * @throws IllegalStateException if the task did not finished execution or if the task is
     * canceled.
     */
    public @Nullable Result getResult(){
        if(!isFinished() && isCancelled()){
            throw new IllegalStateException("Result not available because the task did not complete execution.");
        }
        return result;
    }

    /**
     * Returns the tasks last known progress if any. This method is safe to call in either the task
     * lifecycle methods or one of the callback methods. But throws an exception if called before
     * execution has started.
     * @return The last known progress.
     * @throws IllegalStateException if called before execution has started.
     */
    public @Nullable Progress getLastKnownProgress(){
        /**
         * It's arguable that an exception should be thrown in this case, because even if the task
         * did start, it might never have published any progress.
         */
        if(isReady()){
            throw new IllegalStateException("Progress not available because the task did not start execution.");
        }
        return lastProgress;
    }

    /**
     * Override this method to perform a computation on a background thread.
     *
     * This method can call {@link #publishProgress} to publish updates
     * on the UI thread.
     *
     * @return A result, defined by the subclass of this task.
     *
     * @see #onPreExecute()
     * @see #doPostExecute
     * @see #publishProgress
     */
    @WorkerThread
    protected abstract Result doInBackground();

    /**
     * Runs on the UI thread before {@link #doInBackground}.
     *
     * @see #doPostExecute
     * @see #doInBackground
     */
    @MainThread
    protected void onPreExecute() {
    }

    @MainThread
    private void doPreExecute() {
        onPreExecute();
        Callback callback = getCallback();
        if (callback != null){
            callback.onPreExecute(this);
        }
    }

    /**
     * <p>Runs on the UI thread after {@link #doInBackground}. The
     * result returned by {@link #doInBackground} can be accessed using {@link #getResult()}.</p>
     *
     * <p>This method won't be invoked if the task was cancelled.</p>
     *
     * @see #onPreExecute
     * @see #doInBackground
     * @see #doCancelled()
     */
    @MainThread
    protected void onPostExecute() {
    }

    @MainThread
    private void doPostExecute() {
        onPostExecute();
        Callback callback = getCallback();
        if (callback == null) {
            shouldDeliverResult = true;
        } else {
            shouldDeliverResult = false;
            callback.onPostExecute(this);
        }
    }

    /**
     * Runs on the UI thread after {@link #publishProgress} is invoked.
     * The specified values are the values passed to {@link #publishProgress}.
     *
     * @param values The values indicating progress.
     *
     * @see #publishProgress
     * @see #doInBackground
     */
    @MainThread
    protected void onProgressUpdate(Progress values) {
    }

    @MainThread
    private void doProgressUpdate(Progress values) {
        onProgressUpdate(values);
        Callback callback = getCallback();
        if (callback != null && callback instanceof AdvancedCallback) {
            ((AdvancedCallback) callback).onProgressUpdate(this, values);
        }
    }

    /**
     * <p>Runs on the UI thread after {@link #cancel(boolean)} is invoked and
     * {@link #doInBackground()} has finished.</p>
     *
     * @see #cancel(boolean)
     * @see #isCancelled()
     */
    @MainThread
    protected void onCancelled() {

    }

    @MainThread
    private void doCancelled() {
        onCancelled();
        Callback callback = getCallback();
        if (callback instanceof AdvancedCallback) {
            shouldDeliverResult = false;
            ((AdvancedCallback) callback).onCanceled(this);
        } else {
            shouldDeliverResult = true;
        }
    }

    /**
     * Returns <tt>true</tt> if this task was cancelled before it completed
     * normally. If you are calling {@link #cancel(boolean)} on the task,
     * the value returned by this method should be checked periodically from
     * {@link #doInBackground()} to end the task as soon as possible.
     *
     * @return <tt>true</tt> if task was cancelled before it completed
     *
     * @see #cancel(boolean)
     */
    public final boolean isCancelled() {
        return mCancelled.get();
    }

    /**
     * <p>Attempts to cancel execution of this task.  This attempt will
     * fail if the task has already completed, already been cancelled,
     * or could not be cancelled for some other reason. If successful,
     * and this task has not started when <tt>cancel</tt> is called,
     * this task should never run. If the task has already started,
     * then the <tt>mayInterruptIfRunning</tt> parameter determines
     * whether the thread executing this task should be interrupted in
     * an attempt to stop the task.</p>
     *
     * <p>Calling this method will result in {@link #doCancelled()} being
     * invoked on the UI thread after {@link #doInBackground()}
     * returns. Calling this method guarantees that {@link #doPostExecute()}
     * is never invoked. After invoking this method, you should check the
     * value returned by {@link #isCancelled()} periodically from
     * {@link #doInBackground()} to finish the task as early as
     * possible.</p>
     *
     * @param mayInterruptIfRunning <tt>true</tt> if the thread executing this
     *        task should be interrupted; otherwise, in-progress tasks are allowed
     *        to complete.
     *
     * @return <tt>false</tt> if the task could not be cancelled,
     *         typically because it has already completed normally;
     *         <tt>true</tt> otherwise
     *
     * @see #isCancelled()
     * @see #doCancelled()
     */
    public final boolean cancel(boolean mayInterruptIfRunning) {
        mCancelled.set(true);
        return mFuture.cancel(mayInterruptIfRunning);
    }

    /**
     * Waits if necessary for the computation to complete, and then
     * retrieves its result.
     *
     * @return The computed result.
     *
     * @throws CancellationException If the computation was cancelled.
     * @throws ExecutionException If the computation threw an exception.
     * @throws InterruptedException If the current thread was interrupted
     *         while waiting.
     */
    public final Result get() throws InterruptedException, ExecutionException {
        return mFuture.get();
    }

    /**
     * Returns the currently set callback listener.
     * @return The currently set callback listener.
     */
    @MainThread
    public Callback getCallback(){
        return callback;
    }

    /**
     * Sets the callback listener. This can either be an implementation of {@link Callback} or
     * {@link AdvancedCallback}. If the task did not deliver its result the new listener will
     * directly receive either a call to the {@link Callback#onPostExecute(Task)} or
     * {@link AdvancedCallback#onCanceled(Task)} method.
     *
     * @param callback The new callback to set.
     * @see #removeCallback()
     */
    @MainThread
    public final void setCallback(@Nullable Callback callback) {
        this.callback = callback;
        if(callback == null){
            return;
        }
        if (shouldDeliverResult) {
            shouldDeliverResult = false;
            if(!isCancelled()) {
                callback.onPostExecute(this);
            } else if(callback instanceof AdvancedCallback){
                ((AdvancedCallback) callback).onCanceled(this);
            }
        }
    }

    /**
     * Removes the currently set callback listener from this task. If the task did not finish before
     * this method was called and no new listener is set before the task is finished. The next
     * listener set using {@link #setCallback(Callback)} will receive the
     * {@link Callback#onPostExecute(Task)} or
     * {@link AdvancedCallback#onCanceled(Task)} call directly after setting the listener. Other
     * listener callback calls are lost.
     */
    @MainThread
    public final void removeCallback() {
        callback = null;
    }

    /**
     * Waits if necessary for at most the given time for the computation
     * to complete, and then retrieves its result.
     *
     * @param timeout Time to wait before cancelling the operation.
     * @param unit The time unit for the timeout.
     *
     * @return The computed result.
     *
     * @throws CancellationException If the computation was cancelled.
     * @throws ExecutionException If the computation threw an exception.
     * @throws InterruptedException If the current thread was interrupted
     *         while waiting.
     * @throws TimeoutException If the wait timed out.
     */
    public final Result get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        return mFuture.get(timeout, unit);
    }

    /**
     * Executes the task with the specified parameters. The task returns
     * itself (this) so that the caller can keep a reference to it.
     *
     * <p>This method is typically used with {@link TaskExecutor#THREAD_POOL_EXECUTOR} to
     * allow multiple tasks to run in parallel on a pool of threads managed by
     * AsyncTask, however you can also use your own {@link Executor} for custom
     * behavior.
     *
     * <p><em>Warning:</em> Allowing multiple tasks to run in parallel from
     * a thread pool is generally <em>not</em> what one wants, because the order
     * of their operation is not defined.  For example, if these tasks are used
     * to modify any state in common (such as writing a file due to a button click),
     * there are no guarantees on the order of the modifications.
     * Without careful work it is possible in rare cases for the newer version
     * of the data to be over-written by an older one, leading to obscure data
     * loss and stability issues.  Such changes are best
     * executed in serial; to guarantee such work is serialized regardless of
     * platform version you can use this function with {@link TaskExecutor#SERIAL_EXECUTOR}.
     *
     * <p>This method must be invoked on the UI thread.
     *
     * @param exec The executor to use.  {@link TaskExecutor#THREAD_POOL_EXECUTOR} is available as a
     *              convenient process-wide thread pool for tasks that are loosely coupled.
     *
     * @return This instance of AsyncTask.
     *
     * @throws IllegalStateException If {@link #isRunning()} or {@link #isFinished()} ()} returns
     * true.
     *
     * @see TaskExecutor#execute(Task)
     */
    @MainThread
    public final Task<Progress, Result> executeOnExecutor(Executor exec) {
        if (mStatus != STATUS_PENDING) {
            switch (mStatus) {
                case STATUS_RUNNING:
                    throw new IllegalStateException("Cannot execute task: the task is already running.");
                case STATUS_FINISHED:
                    throw new IllegalStateException("Cannot execute task: the task has already been executed (a task can be executed only once)");
            }
        }

        mStatus = STATUS_RUNNING;

        doPreExecute();
        exec.execute(mFuture);

        return this;
    }

    /**
     * This method can be invoked from {@link #doInBackground} to
     * publish updates on the UI thread while the background computation is
     * still running. Each call to this method will trigger the execution of
     * {@link #onProgressUpdate} on the UI thread.
     *
     * {@link #onProgressUpdate} will not be called if the task has been
     * canceled.
     *
     * @param value The progress value to update the UI with.
     *
     * @see #onProgressUpdate
     * @see #doInBackground
     */
    @WorkerThread
    protected final void publishProgress(Progress value) {
        if (!isCancelled()) {
            lastProgress = value;
            getHandler().obtainMessage(MESSAGE_POST_PROGRESS, new TaskProgress<>(this, value)).sendToTarget();
        }
    }

    private void finish() {
        if (isCancelled()) {
            doCancelled();
        } else {
            doPostExecute();
        }
        mStatus = STATUS_FINISHED;
    }

    /**
     * Generic interface for receiving callbacks from the Task.
     */
    public interface Callback {
        void onPreExecute(Task<?, ?> task);
        void onPostExecute(Task<?, ?> task);
    }

    /**
     * An extension of the {@link org.neotech.library.retainabletasks.Task.Callback} interface.
     */
    public interface AdvancedCallback extends Callback {
        void onCanceled(Task<?, ?> task);
        void onProgressUpdate(Task<?, ?> task, Object progress);
    }

    private static class InternalUiHandler extends Handler {

        public InternalUiHandler() {
            super(Looper.getMainLooper());
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MESSAGE_POST_RESULT:
                    ((Task<?, ?>) msg.obj).finish();
                    break;
                case MESSAGE_POST_PROGRESS:
                    TaskProgress<?> progress = (TaskProgress<?>) msg.obj;
                    //noinspection unchecked
                    progress.mTask.doProgressUpdate(progress.mData);
                    break;
            }
        }
    }

    private static class TaskProgress<Data> {
        final Task mTask;
        final Data mData;

        TaskProgress(Task task, Data data) {
            mTask = task;
            mData = data;
        }
    }
}
