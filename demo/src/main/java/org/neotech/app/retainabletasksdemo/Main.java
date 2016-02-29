package org.neotech.app.retainabletasksdemo;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Toast;

import org.neotech.app.retainabletasksdemo.tasks.SimpleTask;
import org.neotech.app.retainabletasksdemo.tasks.TaskWithoutCallback;
import org.neotech.library.retainabletasks.Task;
import org.neotech.library.retainabletasks.TaskExecutor;
import org.neotech.library.retainabletasks.TaskHandler;

public class Main extends AppCompatActivity implements View.OnClickListener, Task.AdvancedCallback, OnAlertDialogClickListener {

    private static final String TASK_PROGRESS = "Demo-task";
    private static final String DIALOG_PROGRESS = "progress-dialog";

    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));

        findViewById(R.id.fab).setOnClickListener(this);

        findViewById(R.id.button_no_ui_task).setOnClickListener(this);
        findViewById(R.id.button_progress_task).setOnClickListener(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        progressDialog = ProgressDialog.getExistingInstance(getSupportFragmentManager(), DIALOG_PROGRESS);
        getTaskHandler().attachListener(TASK_PROGRESS, this);
    }

    @Override
    public void onClick(View v) {
        final int id = v.getId();
        if(id == R.id.fab){
            //TODO info
        } else if(id == R.id.button_progress_task) {
            if (getTaskHandler().isRunning(TASK_PROGRESS)) {
                Toast.makeText(this, "Task already running.", Toast.LENGTH_SHORT).show();
            }
            SimpleTask task = new SimpleTask(TASK_PROGRESS);
            getTaskHandler().execute(task, this);

        } else if(id == R.id.button_no_ui_task){
            TaskWithoutCallback task = new TaskWithoutCallback(this);
            TaskExecutor.execute(task);

        }
    }

    public TaskHandler getTaskHandler(){
        return TaskHandler.getActivityTaskHandler(getSupportFragmentManager());
    }

    @Override
    public void onPreExecute(Task<?, ?> task) {
        progressDialog = ProgressDialog.showIfNotShowing(getSupportFragmentManager(), DIALOG_PROGRESS);
    }

    @Override
    public void onPostExecute(Task<?, ?> task) {
        progressDialog.dismiss();
        Snackbar.make(findViewById(android.R.id.content), "'Progress task' finished.", Snackbar.LENGTH_LONG).show();
    }

    @Override
    public void onCanceled(Task<?, ?> task) {
        progressDialog.dismiss();
        Snackbar.make(findViewById(android.R.id.content), "'Progress task' canceled.", Snackbar.LENGTH_LONG).show();
    }

    @Override
    public void onProgressUpdate(Task<?, ?> task, Object progress) {
        progressDialog.setProgress((int) progress);
    }

    @Override
    public void onDialogFragmentClick(DialogFragment fragment, int which) {
        getTaskHandler().cancel(TASK_PROGRESS);
    }
}
