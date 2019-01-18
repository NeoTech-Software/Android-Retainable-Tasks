package org.neotech.app.retainabletasksdemo.activity;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.snackbar.Snackbar;
import androidx.fragment.app.DialogFragment;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;

import org.neotech.app.retainabletasksdemo.OnAlertDialogClickListener;
import org.neotech.app.retainabletasksdemo.ProgressDialog;
import org.neotech.app.retainabletasksdemo.R;
import org.neotech.app.retainabletasksdemo.tasks.SimpleTask;
import org.neotech.library.retainabletasks.*;

/**
 * This demo activity shows how the lifecycle library from the Google Architecture Library can be
 * used to hook the activity lifecycle calls to the TaskManagerLifeCycleProxy. This example also
 * uses the annotations just like the DemoActivityAnnotations.
 * <p>
 * Created by Rolf Smit on 8-Nov-17.
 */
public final class DemoActivityLifeCycleLibrary extends AppCompatActivity implements View.OnClickListener, OnAlertDialogClickListener, TaskManagerOwner {

    private static final String TASK_PROGRESS = "progress-dialog";
    private static final String DIALOG_PROGRESS = "progress-dialog";

    private ProgressDialog progressDialog;

    private final TaskManagerLifeCycleProxy taskManagerLifeCycleProxy = new TaskManagerLifeCycleProxy(this);

    public DemoActivityLifeCycleLibrary() {
        getLifecycle().addObserver(taskManagerLifeCycleProxy);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_demo_annotations);
        findViewById(R.id.button_progress_task).setOnClickListener(this);

        if (savedInstanceState == null) {
            // After starting this activity directly start the task.
            execute();
        }
    }

    @Override
    public void onClick(View v) {
        // On click execute the task
        execute();
    }

    @Override
    public void onDialogFragmentClick(DialogFragment fragment, int which) {
        getTaskManager().cancel(TASK_PROGRESS);
    }

    @Override
    public TaskManager getTaskManager() {
        return taskManagerLifeCycleProxy.getTaskManager();
    }

    @Override
    public Task.Callback onPreAttach(@NonNull Task<?, ?> task) {
        return null;
    }

    public void execute(){
        if(!getTaskManager().isActive(TASK_PROGRESS)) {
            getTaskManager().execute(new SimpleTask(TASK_PROGRESS));
        }
    }

    @TaskAttach(TASK_PROGRESS)
    public void onAttach(SimpleTask task){
        // Task attaches, make sure to show the progress dialog and update the progress if needed.
        progressDialog = ProgressDialog.showIfNotShowing(getSupportFragmentManager(), DIALOG_PROGRESS);
        if(task.getLastKnownProgress() != null) {
            progressDialog.setProgress(task.getLastKnownProgress());
        }
    }

    @TaskProgress(TASK_PROGRESS)
    public void onProgress(SimpleTask task){
        progressDialog.setProgress(task.getLastKnownProgress());
    }

    // Now this is cool, we can have a single method handle both the normal onPostExecute and the
    // onCancelled call.
    @TaskPostExecute(TASK_PROGRESS)
    @TaskCancel(TASK_PROGRESS)
    public void onFinish(SimpleTask task){
        progressDialog.dismiss();
        if(task.isCancelled()) {
            Snackbar.make(findViewById(android.R.id.content), getString(R.string.toast_task_canceled, getString(R.string.task_progress_dialog)), Snackbar.LENGTH_LONG).show();
        }
    }
}
