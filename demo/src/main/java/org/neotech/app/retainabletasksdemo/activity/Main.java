package org.neotech.app.retainabletasksdemo.activity;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import org.neotech.app.retainabletasksdemo.ExtendedHtml;
import org.neotech.app.retainabletasksdemo.R;
import org.neotech.library.retainabletasks.Task;
import org.neotech.library.retainabletasks.providers.TaskActivityCompat;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Rolf on 16-3-2016.
 */
public class Main extends TaskActivityCompat implements Task.Callback {

    private ViewSwitcher vSwitcher;
    private ListView list;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        vSwitcher = (ViewSwitcher) findViewById(R.id.switcher);
        list = (ListView) findViewById(android.R.id.list);

        if(!getTaskManager().isActive("list-loader")) {
            getTaskManager().execute(new UselessLoadingTask("list-loader", this), this);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.action_github){
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/NeoTech-Software/Android-Retainable-Tasks")));
        }
        return super.onOptionsItemSelected(item);
    }

    public void setListShown(boolean shown){
        if(vSwitcher.getCurrentView() == list && !shown){
            vSwitcher.showNext();
        } else if(vSwitcher.getCurrentView() != list && shown){
            vSwitcher.showNext();
        }
    }

    @Override
    public Task.Callback onPreAttach(@NonNull Task<?, ?> task) {
        setListShown(false);
        return this;
    }

    @Override
    public void onPreExecute(Task<?, ?> task) {

    }

    @Override
    public void onPostExecute(Task<?, ?> raw) {
        UselessLoadingTask task = (UselessLoadingTask) raw;
        list.setAdapter(new DemoAdapter(task.getResult()));
        setListShown(true);
    }

    /**
     * Task just to demonstrate the principe of starting a task before the UI is ready.
     */
    private static class UselessLoadingTask extends Task<Void, ArrayList<Demo>> {

        private final Context context;

        public UselessLoadingTask(String tag, Context context) {
            super(tag);
            this.context = context.getApplicationContext();
        }

        @Override
        protected ArrayList<Demo> doInBackground() {
            SystemClock.sleep(1500);
            ArrayList<Demo> demos = new ArrayList<>(4);
            demos.add(new Demo(context, R.string.demo_examples_title, R.string.demo_examples_description, "org/neotech/app/retainabletasksdemo/activity/DemoActivityBasic.java", new Intent(context, DemoActivityBasic.class)));
            demos.add(new Demo(context, R.string.demo_serial_title, R.string.demo_serial_description, "org/neotech/app/retainabletasksdemo/activity/DemoActivitySerial.java", new Intent(context, DemoActivitySerial.class)));
            demos.add(new Demo(context, R.string.demo_fragments_title, R.string.demo_fragments_description, "org/neotech/app/retainabletasksdemo/activity/DemoActivityFragments.java", new Intent(context, DemoActivityFragments.class)));
            demos.add(new Demo(context, R.string.demo_no_compat_title, R.string.demo_no_compat_description, "org/neotech/app/retainabletasksdemo/activity/DemoActivityV11.java", new Intent(context, DemoActivityLegacy.class)));
            return demos;
        }
    }

    private static class DemoAdapter extends BaseAdapter implements View.OnClickListener {

        private static class ViewHolder {
            final Button buttonStart;
            final Button buttonView;
            final TextView title;
            final TextView description;

            public ViewHolder(View view){
                buttonStart = (Button) view.findViewById(R.id.button_start_demo);
                buttonView = (Button) view.findViewById(R.id.button_view_code);
                title = (TextView) view.findViewById(android.R.id.title);
                description = (TextView) view.findViewById(android.R.id.message);
            }
        }

        private final ArrayList<Demo> demos;
        private LayoutInflater inflater;

        public DemoAdapter(ArrayList<Demo> demos){
            this.demos = demos;
        }

        public DemoAdapter(){
            this.demos = new ArrayList<>();
        }

        public List<Demo> getItems(){
            return demos;
        }

        @Override
        public int getCount() {
            return demos.size();
        }

        @Override
        public Demo getItem(int position) {
            return demos.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if(convertView == null){
                if(inflater == null){
                    inflater = LayoutInflater.from(parent.getContext());
                }
                convertView = inflater.inflate(R.layout.list_item_demo, parent, false);
                convertView.setTag(holder = new ViewHolder(convertView));
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            Demo demo = getItem(position);
            holder.title.setText(demo.title);
            holder.description.setText(demo.description);
            holder.buttonStart.setTag(demo.intentStart);
            holder.buttonStart.setOnClickListener(this);
            holder.buttonView.setTag(demo.intentView);
            holder.buttonView.setOnClickListener(this);
            return convertView;
        }

        @Override
        public void onClick(View v) {
            v.getContext().startActivity((Intent) v.getTag());
        }
    }

    private static class Demo {
        final Intent intentStart;
        final Intent intentView;
        final Spanned title;
        final Spanned description;

        public Demo(Context context, int titleResId, int descriptionResId, String uri, Intent intent){
            this.intentStart = intent;
            this.intentView = new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/NeoTech-Software/Android-Retainable-Tasks/tree/master/demo/src/main/java/" + uri));
            this.title = ExtendedHtml.fromHtml(context.getString(titleResId), ExtendedHtml.FROM_HTML_MODE_LEGACY);
            this.description = ExtendedHtml.fromHtml(context.getString(descriptionResId), ExtendedHtml.FROM_HTML_MODE_LEGACY);
        }
    }
}
