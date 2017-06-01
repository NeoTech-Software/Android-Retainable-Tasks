package org.neotech.app.retainabletasksdemo.activity;

import android.support.design.widget.Snackbar;

import org.neotech.app.retainabletasksdemo.ProgressDialog;
import org.neotech.app.retainabletasksdemo.R;
import org.neotech.app.retainabletasksdemo.tasks.SimpleTask;
import org.neotech.library.retainabletasks.Task;
import org.neotech.library.retainabletasks.TaskAttach;
import org.neotech.library.retainabletasks.TaskCancel;
import org.neotech.library.retainabletasks.TaskPostExecute;
import org.neotech.library.retainabletasks.TaskProgress;

/**
 * Created by Rolf Smit on 30-May-17.
 */
public final class ProgressTaskHandler {

    private static final String TASK_PROGRESS = "progress-dialog";
    private static final String DIALOG_PROGRESS = "progress-dialog";

    private ProgressDialog progressDialog;

    private final DemoActivityAnnotations demoActivityAnnotations;

    public ProgressTaskHandler(DemoActivityAnnotations demoActivityAnnotations){
        this.demoActivityAnnotations = demoActivityAnnotations;
    }

    public void execute(){
        if(!demoActivityAnnotations.getTaskManager().isActive(TASK_PROGRESS)) {
            demoActivityAnnotations.getTaskManager().execute(new SimpleTask(TASK_PROGRESS));
        }
    }

    @TaskAttach(TASK_PROGRESS)
    public void onAttach(Task task){
        // Task attaches, make sure to show the progress dialog and update the progress if needed.
        progressDialog = ProgressDialog.showIfNotShowing(demoActivityAnnotations.getSupportFragmentManager(), DIALOG_PROGRESS);
        if(task.getLastKnownProgress() != null) {
            progressDialog.setProgress((Integer) task.getLastKnownProgress());
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
    public void onFinish(SimpleTask task){
        progressDialog.dismiss();
        if(task.isCancelled()) {
            Snackbar.make(demoActivityAnnotations.findViewById(android.R.id.content), demoActivityAnnotations.getString(R.string.toast_task_canceled, demoActivityAnnotations.getString(R.string.task_progress_dialog)), Snackbar.LENGTH_LONG).show();
        }
    }


}
