package rickpat.spotboy.online_activities;

import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import rickpat.spotboy.R;
import rickpat.spotboy.offline_database.SpotBoyDBHelper;
import rickpat.spotboy.online_fragments.HubMainFragment;
import rickpat.spotboy.online_fragments.HubUserFragment;
import rickpat.spotboy.online_fragments.IHub;

import static rickpat.spotboy.utilities.Constants.*;

/*
* Online_HubActivity cares about a tab layout with view pager. there are two tabs.
* one for all spots and another one for spots created by signed user(googleId).
* Each tab manages a recycler view with card views.
* */

public class Online_HubActivity extends AppCompatActivity implements IHub {

    private Toolbar toolbar;
    private TabLayout tabLayout;
    private ViewPager viewPager;
    private String googleId;

    @Override
    protected void onPause() {
        super.onPause();
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

        viewPager = (ViewPager) findViewById(R.id.hub_viewpager);
        setupViewPager(viewPager);
        tabLayout = (TabLayout) findViewById(R.id.hub_tabs);
        tabLayout.setupWithViewPager(viewPager);
        //next onStart
    }

    @Override
    protected void onStart() {
        super.onStart();
        //next onRestoreInstanceState or onResume
    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putString(GOOGLE_ID,googleId);
        super.onSaveInstanceState(outState);
        //next onResume
    }

    @Override
    protected void onResume() {
        super.onResume();
        //app's ready now
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
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
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
    }
}