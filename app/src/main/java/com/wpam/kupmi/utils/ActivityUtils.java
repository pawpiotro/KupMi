package com.wpam.kupmi.utils;

import android.app.Activity;
import android.content.Intent;

import com.wpam.kupmi.activities.MainActivity;

public class ActivityUtils
{
    public static void returnToMainActivity(Activity activity)
    {
        activity.startActivity(new Intent(activity, MainActivity.class));
        activity.finish();
    }
}
