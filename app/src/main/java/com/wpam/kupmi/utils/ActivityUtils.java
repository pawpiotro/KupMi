package com.wpam.kupmi.utils;

import android.app.Activity;
import android.content.Intent;
import com.wpam.kupmi.activities.MainActivity;

public class ActivityUtils
{
    public static void returnToMainActivity(Activity activity)
    {
        Intent intent = new Intent(activity, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        activity.startActivity(intent);
        activity.finish();
    }
}
