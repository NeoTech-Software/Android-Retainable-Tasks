package org.neotech.app.retainabletasksdemo.activity;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
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

import org.neotech.app.retainabletasksdemo.ExtendedHtml;
import org.neotech.app.retainabletasksdemo.R;

import java.util.ArrayList;

/**
 * Created by Rolf on 16-3-2016.
 */
public class Main extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));

        ArrayList<Demo> demos = new ArrayList<>(1);
        demos.add(new Demo(this, R.string.demo_examples_title, R.string.demo_examples_description, "https://github.com/NeoTech-Software/Android-Retainable-Tasks", new Intent(this, DemoActivityBasic.class)));
        demos.add(new Demo(this, R.string.demo_serial_title, R.string.demo_serial_description, "https://github.com/NeoTech-Software/Android-Retainable-Tasks", new Intent(this, DemoActivitySerial.class)));
        demos.add(new Demo(this, R.string.demo_fragments_title, R.string.demo_fragments_description, "https://github.com/NeoTech-Software/Android-Retainable-Tasks", new Intent(this, DemoActivityFragments.class)));
        demos.add(new Demo(this, R.string.demo_no_compat_title, R.string.demo_no_compat_description, "https://github.com/NeoTech-Software/Android-Retainable-Tasks", new Intent(this, DemoActivityV11.class)));


        ListView list = (ListView) findViewById(android.R.id.list);
        list.setAdapter(new DemoAdapter(demos));
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
            this.intentView = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
            this.title = ExtendedHtml.fromHtml(context.getString(titleResId));
            this.description = ExtendedHtml.fromHtml(context.getString(descriptionResId));
        }
    }
}
