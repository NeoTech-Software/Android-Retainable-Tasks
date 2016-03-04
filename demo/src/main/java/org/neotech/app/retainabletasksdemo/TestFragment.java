package org.neotech.app.retainabletasksdemo;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import org.neotech.app.retainabletasksdemo.tasks.CountDownTask;
import org.neotech.library.retainabletasks.Task;
import org.neotech.library.retainabletasks.providers.TaskFragmentCompat;

/**
 * Created by Rolf on 3-3-2016.
 */
public class TestFragment extends TaskFragmentCompat implements View.OnClickListener, Task.AdvancedCallback {

    private Button button;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_test, container, false);
        button = (Button) view.findViewById(R.id.button_task);
        button.setOnClickListener(this);
        return view;
    }

    @Override
    public void onClick(View v) {
        CountDownTask task = new CountDownTask("CountDownTask", 10);
        getTaskManager().execute(task, this);
        button.setEnabled(false);
    }

    @Override
    public Task.Callback onPreAttach(@NonNull Task<?, ?> task) {
        button.setEnabled(false);
        button.setText("" + task.getLastKnownProgress());
        return this;
    }

    @Override
    public void onPreExecute(Task<?, ?> task) {

    }

    @Override
    public void onPostExecute(Task<?, ?> task) {
        button.setEnabled(true);
        button.setText(R.string.task_fragment_based);
        Snackbar.make(getView(), getString(R.string.toast_task_finished, getString(R.string.task_fragment_based)), Snackbar.LENGTH_LONG).show();
    }

    @Override
    public void onCanceled(Task<?, ?> task) {

    }

    @Override
    public void onProgressUpdate(Task<?, ?> task, Object progress) {
        button.setText("" + (int) progress);
    }
}
