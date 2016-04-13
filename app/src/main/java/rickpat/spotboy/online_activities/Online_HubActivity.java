package rickpat.spotboy.online_activities;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import java.util.ArrayList;
import java.util.List;

import rickpat.spotboy.R;
import rickpat.spotboy.online_fragments.HubMainFragment;
import rickpat.spotboy.online_fragments.HubUserFragment;
import rickpat.spotboy.online_fragments.IHub;

import static rickpat.spotboy.utilities.Constants.*;

/*
* todo swipe view implementation
* */

public class Online_HubActivity extends AppCompatActivity implements IHub {

    private Toolbar toolbar;
    private TabLayout tabLayout;
    private ViewPager viewPager;
    private String log = "ONLINE_HUB_ACTIVITY";
    private String googleId;
    private String googleName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_online_hub);

        toolbar = (Toolbar) findViewById(R.id.toolbar_hub);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Bundle bundle = getIntent().getExtras();

        if ( bundle.containsKey(GOOGLE_ID) && bundle.containsKey(GOOGLE_NAME)){
            googleId = bundle.getString(GOOGLE_ID);
            googleName = bundle.getString(GOOGLE_NAME);
        }else{
            finish();
        }

        viewPager = (ViewPager) findViewById(R.id.hub_viewpager);
        setupViewPager(viewPager);

        tabLayout = (TabLayout) findViewById(R.id.hub_tabs);
        tabLayout.setupWithViewPager(viewPager);
    }

    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(new HubMainFragment(), "ALL");
        adapter.addFragment(new HubUserFragment(), "USR");
        viewPager.setAdapter(adapter);
    }

    @Override
    public String getUserGoogleId() {
        return googleId;
    }

    @Override
    public String getUserGoogleName() {
        return googleName;
    }

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