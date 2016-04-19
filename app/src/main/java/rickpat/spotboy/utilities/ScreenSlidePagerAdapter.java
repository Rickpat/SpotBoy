package rickpat.spotboy.utilities;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.view.ViewGroup;

import java.util.List;

/*
* adapter for view pager with in e.g. InfoActivity or NewActivity
* */

public class ScreenSlidePagerAdapter extends FragmentStatePagerAdapter {

    private List<Fragment> fragmentList;

    public ScreenSlidePagerAdapter(FragmentManager fm, List<Fragment> viewPagerFragments) {
        super(fm);
        this.fragmentList = viewPagerFragments;
    }

    @Override
    public Fragment getItem(int position) {
        return fragmentList.get(position);
    }

    @Override
    public int getCount() {
        return fragmentList.size();
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        return super.instantiateItem(container, position);

    }
}
