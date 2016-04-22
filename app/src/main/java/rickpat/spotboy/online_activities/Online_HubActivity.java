package rickpat.spotboy.online_activities;

import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;

import java.util.ArrayList;
import java.util.List;

import rickpat.spotboy.R;
import rickpat.spotboy.online_fragments.HubMainFragment;
import rickpat.spotboy.online_fragments.HubUserFragment;
import rickpat.spotboy.online_fragments.IHub;
import rickpat.spotboy.online_fragments.ISpotFragment;
import rickpat.spotboy.restful_broadcast_receiver.AllSpotsBroadcastReceiver;
import rickpat.spotboy.restful_post_services.interfaces.IAllSpots;
import rickpat.spotboy.restful_post_services.GetAllSpots_Service;
import rickpat.spotboy.spotspecific.Spot;

import static rickpat.spotboy.utilities.Constants.*;

/*
* Online_HubActivity cares about a tab layout with view pager. there are two tabs.
* one for all spots and another one for spots created by signed user(googleId).
* Each tab manages a recycler view with card views.
* */

public class Online_HubActivity extends AppCompatActivity implements IHub,IAllSpots {

    private String log = "Online_HubActivity";
    private String googleId;
    private List<Spot> spotList;
    private ViewPagerAdapter adapter;
    private AllSpotsBroadcastReceiver receiver;             //Called by GetAllSpots_Service. Uses IAllSpots callback

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(receiver);
        //next onRestoreInstanceState
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        googleId = savedInstanceState.getString(GOOGLE_ID);
        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override   //first
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_online_hub);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_hub);
        setSupportActionBar(toolbar);
        try{
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }catch (NullPointerException e){}
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        Bundle bundle = getIntent().getExtras();
        if ( bundle.containsKey(GOOGLE_ID)){
            googleId = bundle.getString(GOOGLE_ID);
        }else{
            finish();
        }
        receiver = new AllSpotsBroadcastReceiver(this);

        ViewPager viewPager = (ViewPager) findViewById(R.id.hub_viewpager);
        setupViewPager(viewPager);
        TabLayout tabLayout = (TabLayout) findViewById(R.id.hub_tabs);
        tabLayout.setupWithViewPager(viewPager);
        //next onStart
    }

    @Override
    protected void onStart() {
        super.onStart();
        spotList = new ArrayList<>();
        //next onRestoreInstanceState or onResume
    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putString(GOOGLE_ID, googleId);
        super.onSaveInstanceState(outState);
        //next onResume
    }

    @Override
    protected void onResume() {
        super.onResume();
        startGetAllSpotsService();
        //app's ready now
    }

    private void startGetAllSpotsService() {
        registerReceiver(receiver, new IntentFilter(NOTIFICATION));
        Intent getSpotsIntent = new Intent(this, GetAllSpots_Service.class);
        startService(getSpotsIntent);
    }

    /*
    * callback from AllSpotsBroadcastReceiver
    * */
    @Override
    public void setSpotList(List<Spot> spotList) {
        this.spotList = spotList;
        adapter.notifyDataSetChanged();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /*
    * add tabs to view pager adapter and name them
    * */
    private void setupViewPager(ViewPager viewPager) {
        adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(new HubMainFragment(), "ALL");
        adapter.addFragment(new HubUserFragment(), "USR");
        viewPager.setAdapter(adapter);
    }

    /*
    * callback for tabs/Fragments in view pager to get users google id
    * */
    @Override
    public String getUserGoogleId() {
        return googleId;
    }

    /*
    * callback for AllSpotsBroadcastReceiver
    * */
    @Override
    public List<Spot> getSpotList() {
        return spotList;
    }

    /*
    * callback for AllSpotsBroadcastReceiver
    * called on http request fail
    * */
    @Override
    public void errorCallback(String message) {
        Log.d(log,"errorCallback: " + message);
    }

    /*
    * callback for fragments in tab host
    * */
    @Override
    public void updateList() {
        startGetAllSpotsService();
    }

    /*
    * Adapter that cares about the tabs in the view pager
    * one list for the fragments and another one for the titles.
    * simple as fuck
    * */
    class ViewPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        public ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        public void addFragment(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }

        @Override
        public void notifyDataSetChanged() {
            super.notifyDataSetChanged();
            for (Fragment fragment : mFragmentList){
                if (fragment.isVisible()){
                    Log.d(log,"fragment update");
                    ((ISpotFragment) fragment).contentUpdate();
                }
            }
        }
    }
}