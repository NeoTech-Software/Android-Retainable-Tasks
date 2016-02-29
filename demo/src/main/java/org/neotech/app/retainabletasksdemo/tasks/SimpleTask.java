package org.neotech.app.retainabletasksdemo.tasks;

import android.os.SystemClock;
import android.util.Log;

import org.neotech.library.retainabletasks.Task;

/**
 * Created by Rolf on 29-2-2016.
 */
public class SimpleTask extends Task<Integer, String> {

    public SimpleTask(String tag) {
        super(tag);
    }

    @Override
    protected String doInBackground() {
        for(int i = 0; i < 100; i++) {
            if(isCancelled()){
                break;
            }
            SystemClock.sleep(50);
            publishProgress(i);
        }
        return "Result";
    }

    @Override
    protected void onPostExecute() {
        Log.i("SimpleTask", "SimpleTask.onPostExecute()");
    }
}
