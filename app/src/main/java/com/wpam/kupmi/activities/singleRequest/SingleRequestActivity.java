package com.wpam.kupmi.activities.singleRequest;

import android.graphics.pdf.PdfDocument;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;

import com.wpam.kupmi.R;
import com.wpam.kupmi.lib.Constants;
import com.wpam.kupmi.model.Request;
import com.wpam.kupmi.model.RequestUserKind;

import java.util.Objects;

public class SingleRequestActivity extends AppCompatActivity {

    private ViewPager viewPager;
    private SingleRequestActivity.PagerAdapter adapterViewPager;

    private Request request;
    private boolean partialDataAvailable = false;
    private RequestUserKind requestUserKind;

    private SingleRequestFragment singleRequestFragment = new SingleRequestFragment();
    private SingleRequestUserFragment singleRequestUserFragment = new SingleRequestUserFragment();
    private SingleRequestMapFragment singleRequestMapFragment = new SingleRequestMapFragment();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_request);

        request = (Request) Objects.requireNonNull(getIntent().getExtras()).getSerializable(Constants.REQUEST);
        partialDataAvailable = getIntent().getExtras().getBoolean(Constants.REQUEST_PARTIAL_DATA_FLAG);
        requestUserKind = (RequestUserKind) getIntent().getExtras().getSerializable(Constants.USER_KIND_PARAM);

        viewPager = findViewById(R.id.single_request_viewpager);
        adapterViewPager = new SingleRequestActivity.PagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(adapterViewPager);

    }

    class PagerAdapter extends FragmentPagerAdapter {
        private int NUM_ITEMS = 3;

        PagerAdapter(FragmentManager fragmentManager) {
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
                    return singleRequestFragment;
                case 1:
                    return singleRequestUserFragment;
                case 2:
                    return singleRequestMapFragment;
                default:
                    return null;
            }
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "Request";
                case 1:
                    return "User";
                case 2:
                    return "Map";
                default:
                    return null;
            }
        }

    }

    public Request getRequest() {
        return request;
    }

    public boolean isPartialDataAvailable() {
        return partialDataAvailable;
    }

    public RequestUserKind getRequestUserKind() {
        return requestUserKind;
    }

    public void updateUserData(){
        singleRequestUserFragment.updateUserData();
    }

}
