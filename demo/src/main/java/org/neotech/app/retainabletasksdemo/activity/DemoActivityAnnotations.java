package org.neotech.app.retainabletasksdemo.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.DialogFragment;
import android.view.View;

import org.neotech.app.retainabletasksdemo.OnAlertDialogClickListener;
import org.neotech.app.retainabletasksdemo.ProgressDialog;
import org.neotech.app.retainabletasksdemo.R;
import org.neotech.app.retainabletasksdemo.tasks.SimpleTask;
import org.neotech.library.retainabletasks.Task;
import org.neotech.library.retainabletasks.TaskAttach;
import org.neotech.library.retainabletasks.TaskCancel;
import org.neotech.library.retainabletasks.TaskPostExecute;
import org.neotech.library.retainabletasks.TaskProgress;
import org.neotech.library.retainabletasks.providers.TaskActivityCompat;

/**
 * Created by Rolf Smit on 29-May-17.
 */

public class DemoActivityAnnotations extends TaskActivityCompat implements View.OnClickListener, OnAlertDialogClickListener {

    private static final String TASK_PROGRESS = "progress-dialog";
    private static final String DIALOG_PROGRESS = "progress-dialog";

    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_demo_annotations);
        findViewById(R.id.button_progress_task).setOnClickListener(this);


        if(savedInstanceState == null) {
            // After starting this activity directly start the task.
            getTaskManager().execute(new SimpleTask(TASK_PROGRESS));
        }
    }

    @Override
    public void onClick(View v) {
        // On click execute the task
        getTaskManager().execute(new SimpleTask(TASK_PROGRESS));
    }

    @TaskAttach(TASK_PROGRESS)
    public void onAttach(Task<?, ?> rawTask){
        // Task attaches, make sure to show the progress dialog and update the progress if needed.
        final SimpleTask task = (SimpleTask) rawTask;
        progressDialog = ProgressDialog.showIfNotShowing(getSupportFragmentManager(), DIALOG_PROGRESS);
        if(task.getLastKnownProgress() != null) {
            progressDialog.setProgress(task.getLastKnownProgress());
        }
    }

    @TaskProgress(TASK_PROGRESS)
    public void onProgress(Task<?, ?> task, Object progress){
        progressDialog.setProgress((int) progress);
    }

    // Now this is cool, we can have a single method handle both the normal onPostExecute and the
    // onCancelled call.
    @TaskPostExecute(TASK_PROGRESS)
    @TaskCancel(TASK_PROGRESS)
    public void onFinish(Task<?, ?> task){
        progressDialog.dismiss();
        if(task.isCancelled()) {
            Snackbar.make(findViewById(android.R.id.content), getString(R.string.toast_task_canceled, getString(R.string.task_progress_dialog)), Snackbar.LENGTH_LONG).show();
        }
    }


    @Override
    public void onDialogFragmentClick(DialogFragment fragment, int which) {
        getTaskManager().cancel(TASK_PROGRESS);
    }
}
