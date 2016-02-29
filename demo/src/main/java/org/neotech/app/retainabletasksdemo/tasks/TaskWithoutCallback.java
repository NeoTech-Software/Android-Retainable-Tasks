package org.neotech.app.retainabletasksdemo.tasks;

import android.content.Context;
import android.os.SystemClock;
import android.widget.Toast;

import org.neotech.library.retainabletasks.Task;

/**
 * Created by Rolf on 29-2-2016.
 */
public class TaskWithoutCallback extends Task<Void, Void> {

    private final Context context;

    public TaskWithoutCallback(Context context) {
        super("TaskWithoutCallback");
        this.context = context.getApplicationContext();
    }

    @Override
    protected Void doInBackground() {
        SystemClock.sleep(4000);
        return null;
    }

    @Override
    protected void onPreExecute() {
        Toast.makeText(context, "'Task without UI callback' started.", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onPostExecute() {
        Toast.makeText(context, "'Task without UI callback' finished.", Toast.LENGTH_SHORT).show();
    }
}
