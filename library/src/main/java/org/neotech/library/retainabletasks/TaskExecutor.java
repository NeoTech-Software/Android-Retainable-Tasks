package org.neotech.library.retainabletasks;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;

import java.util.ArrayDeque;
import java.util.Queue;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by Rolf on 29-2-2016.
 */
public class TaskExecutor {

    private static final int CPU_COUNT = Runtime.getRuntime().availableProcessors();
    private static final int CORE_POOL_SIZE = CPU_COUNT + 1;
    private static final int MAXIMUM_POOL_SIZE = CPU_COUNT * 2 + 1;
    private static final int KEEP_ALIVE = 1;

    private static final ThreadFactory threadFactory = new ThreadFactory() {
        private final AtomicInteger threadCount = new AtomicInteger(1);

        public Thread newThread(@NonNull Runnable runnable) {
            return new Thread(runnable, "Task #" + threadCount.getAndIncrement());
        }
    };

    /**
     * An {@link Executor} that can be used to execute tasks in parallel.
     */
    public static final Executor THREAD_POOL_EXECUTOR = new ThreadPoolExecutor(
            CORE_POOL_SIZE,
            MAXIMUM_POOL_SIZE,
            KEEP_ALIVE, TimeUnit.SECONDS,
            new LinkedBlockingQueue<Runnable>(128),
            threadFactory){

        @Override
        protected void beforeExecute(Thread t, Runnable r) {
            android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);
            super.beforeExecute(t, r);
        }
    };

    /**
     * An {@link Executor} that can be used to execute tasks in serial. This Executor internally uses the {@link TaskExecutor#THREAD_POOL_EXECUTOR} for executing it's tasks.
     */
    public static final Executor SERIAL_EXECUTOR = new SerialExecutor();

    private static class SerialExecutor implements Executor {
        private final Queue<Runnable> taskQueue = new ArrayDeque<>();
        private Runnable activeRunnable;

        public synchronized void execute(@NonNull final Runnable r) {
            taskQueue.offer(new Runnable() {
                public void run() {
                    try {
                        r.run();
                    } finally {
                        scheduleNext();
                    }
                }
            });
            if (activeRunnable == null) {
                scheduleNext();
            }
        }

        private synchronized void scheduleNext() {
            if ((activeRunnable = taskQueue.poll()) != null) {
                THREAD_POOL_EXECUTOR.execute(activeRunnable);
            }
        }
    }

    private static volatile Executor defaultExecutor = THREAD_POOL_EXECUTOR;

    private TaskExecutor() {

    }

    /**
     * Sets the default {@link Executor} to use when executing {@link Task Tasks} using this class.
     * <strong>Important:</strong> The {@link TaskManager} uses this class to execute Tasks,
     * changing the default Executor changes the executing behaviour of all TaskMangers.
     * @param executor The {@link Executor} to use as default.
     */
    public static void setDefaultExecutor(@NonNull Executor executor) {
        defaultExecutor = executor;
    }

    /**
     * Returns the currently set default {@link Executor}. This is by default, if not changed using the
     * {@link TaskExecutor#setDefaultExecutor(Executor)} method, the
     * {@link TaskExecutor#THREAD_POOL_EXECUTOR}.
     * @return The default {@link Executor}.
     */
    public static @NonNull Executor getDefaultExecutor(){
        return defaultExecutor;
    }

    /**
     * Executes the task with the specified parameters. The task returns
     * itself (this) so that the caller can keep a reference to it.
     *
     * <p>Note: this function schedules the task on a queue for a single background
     * thread or pool of threads depending on the platform version.  When first
     * introduced, AsyncTasks were executed serially on a single background thread.
     * Starting with {@link android.os.Build.VERSION_CODES#DONUT}, this was changed
     * to a pool of threads allowing multiple tasks to operate in parallel. Starting
     * {@link android.os.Build.VERSION_CODES#HONEYCOMB}, tasks are back to being
     * executed on a single thread to avoid common application errors caused
     * by parallel execution.  If you truly want parallel execution, you can use
     * the {@link Task#executeOnExecutor} version of this method
     * with {@link #THREAD_POOL_EXECUTOR}; however, see commentary there for warnings
     * on its use.</p>
     *
     * <p>This method must be invoked on the UI thread.</p>
     *
     * @param task the task to execute on the given {@link Executor}.
     * @param executor the executor to execute the given {@link Task} on.
     * @param <Progress> the progress type.
     * @param <Result> the result type.
     * @return This instance of AsyncTask.
     * @throws IllegalStateException If {@link Task#isRunning()} or {@link Task#isFinished()}
     * returns true.
     * @see Task#executeOnExecutor(java.util.concurrent.Executor)
     */
    @MainThread
    public static <Progress, Result> Task<Progress, Result> executeOnExecutor(@NonNull Task<Progress, Result> task, @NonNull Executor executor) {
        return task.executeOnExecutor(executor);
    }

    @MainThread
    public static <Progress, Result> Task<Progress, Result> execute(@NonNull Task<Progress, Result> task) {
        return executeOnExecutor(task, defaultExecutor);
    }

    @MainThread
    public static <Progress, Result> Task<Progress, Result> executeSerial(@NonNull Task<Progress, Result> task) {
        return executeOnExecutor(task, SERIAL_EXECUTOR);
    }

    @MainThread
    public static <Progress, Result> Task<Progress, Result> executeParallel(@NonNull Task<Progress, Result> task) {
        return executeOnExecutor(task, THREAD_POOL_EXECUTOR);
    }
}
