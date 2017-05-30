package org.neotech.app.retainabletasksdemo.activity;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import org.neotech.app.retainabletasksdemo.OnAlertDialogClickListener;
import org.neotech.app.retainabletasksdemo.ProgressDialog;
import org.neotech.app.retainabletasksdemo.R;
import org.neotech.app.retainabletasksdemo.tasks.CountDownTask;
import org.neotech.app.retainabletasksdemo.tasks.SimpleTask;
import org.neotech.app.retainabletasksdemo.tasks.TaskWithoutCallback;
import org.neotech.library.retainabletasks.Task;
import org.neotech.library.retainabletasks.TaskExecutor;
import org.neotech.library.retainabletasks.providers.TaskActivityCompat;

public final class DemoActivityBasic extends TaskActivityCompat implements View.OnClickListener, Task.AdvancedCallback, OnAlertDialogClickListener {

    private static final String TASK_RETAIN_UI_STATE = "retain-ui-state";
    private static final String TASK_PROGRESS = "progress-dialog";

    private static final String DIALOG_PROGRESS = "progress-dialog";

    private ProgressDialog progressDialog;
    private Button retainUserInterfaceButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_demo_basic);

        findViewById(R.id.button_no_ui_task).setOnClickListener(this);
        findViewById(R.id.button_progress_task).setOnClickListener(this);

        retainUserInterfaceButton = (Button) findViewById(R.id.button_retain_ui_state_task);
        retainUserInterfaceButton.setOnClickListener(this);
    }

    @Override
    public Task.Callback onPreAttach(@NonNull Task<?, ?> task) {
        if(task.getTag().equals(TASK_RETAIN_UI_STATE)){
            /**
             * the onPreAttach method will only be called if the task did not deliver its result
             * and thus is still available/referenced by the TaskManger.
             *
             * At this point the UI can be restored to the "task is running" state.
             */
            if (!task.isResultDelivered()) { //This call isn't necessary.
                retainUserInterfaceButton.setEnabled(false);
                retainUserInterfaceButton.setText("" + task.getLastKnownProgress());
            }
        } else if(task.getTag().equals(TASK_PROGRESS)){
            progressDialog = ProgressDialog.getExistingInstance(getSupportFragmentManager(), DIALOG_PROGRESS);
        }
        return this;
    }

    @Override
    public void onClick(View v) {
        final int id = v.getId();
        if(id == R.id.button_progress_task) {
            if (getTaskManager().isRunning(TASK_PROGRESS)) {
                Toast.makeText(this, R.string.toast_task_already_running, Toast.LENGTH_SHORT).show();
            }
            SimpleTask task = new SimpleTask(TASK_PROGRESS);
            getTaskManager().execute(task, this);

        } else if(id == R.id.button_no_ui_task){
            TaskWithoutCallback task = new TaskWithoutCallback(this);
            TaskExecutor.execute(task);
        } else if(id == R.id.button_retain_ui_state_task){
            CountDownTask task = new CountDownTask(TASK_RETAIN_UI_STATE, 10);
            getTaskManager().execute(task, this);
            retainUserInterfaceButton.setEnabled(false);
        }
    }

    @Override
    public void onPreExecute(Task<?, ?> task) {
        if(task.getTag().equals(TASK_PROGRESS)) {
            progressDialog = ProgressDialog.showIfNotShowing(getSupportFragmentManager(), DIALOG_PROGRESS);
        }
    }

    @Override
    public void onPostExecute(Task<?, ?> task) {
        if(task.getTag().equals(TASK_PROGRESS)) {
            progressDialog.dismiss();
            Snackbar.make(findViewById(android.R.id.content), getString(R.string.toast_task_finished, getString(R.string.task_progress_dialog)), Snackbar.LENGTH_LONG).show();
        } else if(task.getTag().equals(TASK_RETAIN_UI_STATE)){
            retainUserInterfaceButton.setEnabled(true);
            retainUserInterfaceButton.setText(R.string.task_retain_ui_state);
        }
    }

    @Override
    public void onCanceled(Task<?, ?> task) {
        if(task.getTag().equals(TASK_PROGRESS)) {
            progressDialog.dismiss();
            Snackbar.make(findViewById(android.R.id.content), getString(R.string.toast_task_canceled, getString(R.string.task_progress_dialog)), Snackbar.LENGTH_LONG).show();
        }
    }

    @Override
    public void onProgressUpdate(Task<?, ?> task, Object progress) {
        if(task.getTag().equals(TASK_PROGRESS)) {
            progressDialog.setProgress((int) progress);
        } else if(task.getTag().equals(TASK_RETAIN_UI_STATE)){
            retainUserInterfaceButton.setText("" + (int) progress);
        }
    }

    @Override
    public void onDialogFragmentClick(DialogFragment fragment, int which) {
        getTaskManager().cancel(TASK_PROGRESS);
    }
}