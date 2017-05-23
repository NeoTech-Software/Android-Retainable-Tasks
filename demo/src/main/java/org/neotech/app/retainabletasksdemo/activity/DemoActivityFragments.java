package org.neotech.app.retainabletasksdemo.activity;

import android.content.res.Configuration;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import org.neotech.app.retainabletasksdemo.R;
import org.neotech.app.retainabletasksdemo.TestFragment;

import java.util.HashMap;

public class DemoActivityFragments extends AppCompatActivity  implements NavigationView.OnNavigationItemSelectedListener {

    private static final String[] FRAGMENT_TAGS = new String[]{"Fragment 1", "Fragment 2", "Fragment 3"};

    private FragmentAdapter adapter;
    private ActionBarDrawerToggle toggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_demo_fragments);

        adapter = new FragmentAdapter(getSupportFragmentManager(), R.id.fragment_container, 3);
        for (String FRAGMENT_TAG : FRAGMENT_TAGS) {
            adapter.addFragment(FRAGMENT_TAG, TestFragment.class);
        }

        DrawerLayout drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        NavigationView drawer = (NavigationView) findViewById(R.id.nav_view);
        drawer.setNavigationItemSelectedListener(this);
        drawer.getHeaderView(0).setVisibility(View.GONE);

        if(savedInstanceState == null){
            adapter.setCurrentFragment(FRAGMENT_TAGS[0]);
            drawer.setCheckedItem(R.id.nav_fragment_1);
            setTitle(FRAGMENT_TAGS[0]);
        } else {
            setTitle(adapter.getCurrentFragment().getTag());
        }
    }

    private void setTitle(String title){
        getSupportActionBar().setTitle(title);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        toggle.syncState();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        toggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        final int id = item.getItemId();

        if (id == android.R.id.home) {
            final DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
            final NavigationView navigation = (NavigationView) findViewById(R.id.nav_view);
            if (drawer.isDrawerOpen(navigation)) {
                drawer.closeDrawer(navigation);
            } else {
                drawer.openDrawer(navigation);
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_fragment_1) {
            adapter.setCurrentFragment(FRAGMENT_TAGS[0]);
            setTitle(FRAGMENT_TAGS[0]);
        } else if (id == R.id.nav_fragment_2) {
            adapter.setCurrentFragment(FRAGMENT_TAGS[1]);
            setTitle(FRAGMENT_TAGS[1]);
        } else if (id == R.id.nav_fragment_3) {
            adapter.setCurrentFragment(FRAGMENT_TAGS[2]);
            setTitle(FRAGMENT_TAGS[2]);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private static class FragmentAdapter {

        private final HashMap<String, Class<? extends Fragment>> fragments;

        private final FragmentManager fragmentManager;
        private final int containerId;

        public FragmentAdapter(FragmentManager fragmentManager, int containerViewId, int initialSize){
            this.fragments = new HashMap<>(initialSize);
            this.fragmentManager = fragmentManager;
            this.containerId = containerViewId;
        }

        public void addFragment(String tag, Class<? extends Fragment> fragmentClass){
            fragments.put(tag, fragmentClass);
        }

        public Fragment getCurrentFragment(){
            return fragmentManager.findFragmentById(containerId);
        }

        public Fragment setCurrentFragment(String tag){

            final FragmentTransaction transaction = fragmentManager.beginTransaction();

            // Make sure we have a cool animation
            transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);

            //Retrieve the existing fragment or create a new one.
            Fragment fragment = fragmentManager.findFragmentByTag(tag);
            if(fragment == null) {
                fragment = instantiate(fragments.get(tag));
                transaction.add(containerId, fragment, tag);
            } else {
                transaction.attach(fragment);
            }

            //Check if the currently visible fragment is not equal to the fragment we're about to show.
            if(fragment.equals(fragmentManager.findFragmentById(containerId))){
                Log.i("FragmentAdapter", "Fragment '" + tag + "' not shown because it's already visible.");
                return fragment;
            }

            final Fragment currentFragment = fragmentManager.findFragmentById(containerId);
            if(currentFragment != null){
                transaction.detach(currentFragment);
            }
            transaction.commit();
            return fragment;
        }

        private static Fragment instantiate(Class<? extends Fragment> classType) {
            try {
                return classType.newInstance();
            } catch(Exception e) {
                throw new Fragment.InstantiationException("Unable to instantiate fragment " + classType.getName() + ": make sure class name exists, is public, and has an empty constructor that is public", e);
            }
        }
    }
}
