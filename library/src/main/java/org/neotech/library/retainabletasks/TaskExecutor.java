package org.neotech.library.retainabletasks;

import android.support.annotation.MainThread;

import java.util.ArrayDeque;
import java.util.concurrent.BlockingQueue;
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

    private static final ThreadFactory sThreadFactory = new ThreadFactory() {
        private final AtomicInteger mCount = new AtomicInteger(1);

        public Thread newThread(Runnable r) {
            return new Thread(r, "Task #" + mCount.getAndIncrement());
        }
    };

    private static final BlockingQueue<Runnable> sPoolWorkQueue = new LinkedBlockingQueue<Runnable>(128);

    /**
     * An {@link Executor} that can be used to execute tasks in parallel.
     */
    public static final Executor THREAD_POOL_EXECUTOR
            = new ThreadPoolExecutor(CORE_POOL_SIZE, MAXIMUM_POOL_SIZE, KEEP_ALIVE,
            TimeUnit.SECONDS, sPoolWorkQueue, sThreadFactory);


    public static final Executor SERIAL_EXECUTOR = new SerialExecutor();

    private static class SerialExecutor implements Executor {
        final ArrayDeque<Runnable> mTasks = new ArrayDeque<Runnable>();
        Runnable mActive;

        public synchronized void execute(final Runnable r) {
            mTasks.offer(new Runnable() {
                public void run() {
                    try {
                        r.run();
                    } finally {
                        scheduleNext();
                    }
                }
            });
            if (mActive == null) {
                scheduleNext();
            }
        }

        protected synchronized void scheduleNext() {
            if ((mActive = mTasks.poll()) != null) {
                THREAD_POOL_EXECUTOR.execute(mActive);
            }
        }
    }

    private static volatile Executor sDefaultExecutor = THREAD_POOL_EXECUTOR;

    private static TaskExecutor instance;

    public static void setDefaultExecutor(Executor exec) {
        sDefaultExecutor = exec;
    }

    public static TaskExecutor getInstance(){
        synchronized (TaskExecutor.class) {
            if (instance == null) {
                instance = new TaskExecutor();
            }
            return instance;
        }
    }

    private TaskExecutor() {

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
     * on its use.
     *
     * <p>This method must be invoked on the UI thread.
     *
     * @return This instance of AsyncTask.
     *
     * @throws IllegalStateException If {@link Task#isRunning()} or {@link Task#isFinished()} ()} returns
     * true.
     *
     * @see Task#executeOnExecutor(java.util.concurrent.Executor)
     */
    @MainThread
    private <Progress, Result> Task<Progress, Result> executeOnExecutor(Task<Progress, Result> task, Executor executor) {
        return task.executeOnExecutor(executor);
    }

    @MainThread
    public static <Progress, Result> Task<Progress, Result> execute(Task<Progress, Result> task) {
        return getInstance().executeOnExecutor(task, sDefaultExecutor);
    }

    @MainThread
    public static <Progress, Result> Task<Progress, Result> executeSerial(Task<Progress, Result> task) {
        return getInstance().executeOnExecutor(task, SERIAL_EXECUTOR);
    }


    @MainThread
    public static <Progress, Result> Task<Progress, Result> executeParallel(Task<Progress, Result> task) {
        return getInstance().executeOnExecutor(task, THREAD_POOL_EXECUTOR);
    }
}
