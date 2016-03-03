package org.neotech.app.retainabletasksdemo;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import org.neotech.app.retainabletasksdemo.tasks.CountDownTask;
import org.neotech.app.retainabletasksdemo.tasks.SimpleTask;
import org.neotech.app.retainabletasksdemo.tasks.TaskWithoutCallback;
import org.neotech.library.retainabletasks.Task;
import org.neotech.library.retainabletasks.TaskExecutor;
import org.neotech.library.retainabletasks.TaskManager;

public class Main extends AppCompatActivity implements View.OnClickListener, Task.AdvancedCallback, OnAlertDialogClickListener {

    private static final String TASK_RETAIN_UI_STATE = "retain-ui-state";
    private static final String TASK_PROGRESS = "progress-dialog";
    private static final String DIALOG_PROGRESS = "progress-dialog";

    private ProgressDialog progressDialog;

    private Button retainUserInterfaceButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));

        findViewById(R.id.fab).setOnClickListener(this);
        findViewById(R.id.button_no_ui_task).setOnClickListener(this);
        findViewById(R.id.button_progress_task).setOnClickListener(this);

        retainUserInterfaceButton = (Button) findViewById(R.id.button_retain_ui_state_task);
        retainUserInterfaceButton.setOnClickListener(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        progressDialog = ProgressDialog.getExistingInstance(getSupportFragmentManager(), DIALOG_PROGRESS);
        getTaskManager().attach(TASK_PROGRESS, this);


        getTaskManager().attach(TASK_RETAIN_UI_STATE, new TaskManager.TaskAttachListener() {
            @Override
            public Task.Callback onPreAttach(Task<?, ?> task) {
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
                return Main.this;
            }
        });
    }

    @Override
    public void onClick(View v) {
        final int id = v.getId();
        if(id == R.id.fab){
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/NeoTech-Software/Android-Retainable-Tasks")));
        } else if(id == R.id.button_progress_task) {
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

    public TaskManager getTaskManager(){
        return TaskManager.getActivityTaskManager(getSupportFragmentManager());
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
