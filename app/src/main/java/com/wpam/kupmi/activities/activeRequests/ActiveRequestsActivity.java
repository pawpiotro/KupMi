package com.wpam.kupmi.activities.activeRequests;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;

import com.wpam.kupmi.R;

public class ActiveRequestsActivity extends AppCompatActivity {

    private FragmentManager fragmentManager;
    private ViewPager viewPager;
    private PagerAdapter adapterViewPager;

    public static class PagerAdapter extends FragmentPagerAdapter {
        private static int NUM_ITEMS = 2;

        public PagerAdapter(FragmentManager fragmentManager) {
            super(fragmentManager);
        }

        @Override
        public int getCount() {
            return NUM_ITEMS;
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return new ActiveRequestsAsRequester();
                case 1:
                    return new ActiveRequestsAsSupplier();
                default:
                    return null;
            }
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "Requester";
                case 1:
                    return "Supplier";
                default:
                    return null;
            }
        }

    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_active_requests);
        viewPager = (ViewPager) findViewById(R.id.activity_active_requests_viewpager);
        adapterViewPager = new PagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(adapterViewPager);
    }
}
