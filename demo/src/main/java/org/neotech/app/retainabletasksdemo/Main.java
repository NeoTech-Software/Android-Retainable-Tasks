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

    private static final String TASK_SIMPLE = "Demo-task";

    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        progressDialog = ProgressDialog.getExistingInstance(getSupportFragmentManager(), "progress-dialog");
        getTaskHandler().attachListener(TASK_SIMPLE, this);
    }

    @Override
    public void onClick(View v) {
        if(getTaskHandler().isRunning(TASK_SIMPLE)){
            Toast.makeText(this, "Task already running", Toast.LENGTH_SHORT).show();
        }

        SimpleTask task = new SimpleTask(TASK_SIMPLE);
        getTaskHandler().execute(task, this);

        TaskWithoutCallback callback = new TaskWithoutCallback(this);
        TaskExecutor.execute(callback);
    }

    public TaskHandler getTaskHandler(){
        return TaskHandler.getActivityTaskHandler(getSupportFragmentManager());
    }

    @Override
    public void onPreExecute(Task<?, ?> task) {
        progressDialog = ProgressDialog.showIfNotShowing(getSupportFragmentManager(), "progress-dialog");
    }

    @Override
    public void onPostExecute(Task<?, ?> task) {
        progressDialog.dismiss();
        Snackbar.make(findViewById(android.R.id.content), "Task finished.", Snackbar.LENGTH_LONG).show();
    }

    @Override
    public void onCanceled(Task<?, ?> task) {
        progressDialog.dismiss();
        Snackbar.make(findViewById(android.R.id.content), "Task canceled.", Snackbar.LENGTH_LONG).show();
    }

    @Override
    public void onProgressUpdate(Task<?, ?> task, Object progress) {
        progressDialog.setProgress((int) progress);
    }

    @Override
    public void onDialogFragmentClick(DialogFragment fragment, int which) {
        getTaskHandler().cancel(TASK_SIMPLE);
    }
}
