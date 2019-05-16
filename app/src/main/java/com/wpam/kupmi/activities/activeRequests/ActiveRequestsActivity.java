package com.wpam.kupmi.activities.activeRequests;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import com.wpam.kupmi.R;
import com.wpam.kupmi.activities.MainActivity;
import com.wpam.kupmi.lib.Constants;
import com.wpam.kupmi.model.RequestUserKind;
import com.wpam.kupmi.model.User;
import java.util.Objects;

import static com.wpam.kupmi.utils.DialogUtils.showOKDialog;

public class ActiveRequestsActivity extends AppCompatActivity {

    // Public consts
    public static final String USER_KIND_PARAM = "userKind";

    // Private fields

    private FragmentManager fragmentManager;
    private ViewPager viewPager;
    private PagerAdapter adapterViewPager;
    private User user;

    // Override AppCompatActivity
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_active_requests);

        user = (User) Objects.requireNonNull(getIntent().getExtras()).getSerializable(Constants.USER);
        if (user == null)
        {
            showOKDialog(this, R.string.error_title, R.string.authorize_user_error,
                    android.R.drawable.ic_dialog_alert);
            returnToMainActivity();
        }

        viewPager = findViewById(R.id.activity_active_requests_viewpager);
        adapterViewPager = new PagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(adapterViewPager);
    }

    // Internal / private classes
    static class PagerAdapter extends FragmentPagerAdapter {
        private static int NUM_ITEMS = 2;

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
                    return getActiveRequestsFragmentNewInstance(RequestUserKind.REQUESTER);
                case 1:
                    return getActiveRequestsFragmentNewInstance(RequestUserKind.SUPPLIER);
                default:
                    return null;
            }
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return RequestUserKind.REQUESTER.firstCapitalLetterName();
                case 1:
                    return RequestUserKind.SUPPLIER.firstCapitalLetterName();
                default:
                    return null;
            }
        }

    }

    // Public methods
    public User getUser() {
        return user;
    }

    // Private methods
    private void returnToMainActivity()
    {
        this.startActivity(new Intent(this, MainActivity.class));
        this.finish();
    }

    private static ActiveRequestsFragment getActiveRequestsFragmentNewInstance(RequestUserKind userKind)
    {
        ActiveRequestsFragment fragment = new ActiveRequestsFragment();

        Bundle args = new Bundle();
        args.putString(USER_KIND_PARAM, userKind.name());
        fragment.setArguments(args);

        return fragment;
    }
}
