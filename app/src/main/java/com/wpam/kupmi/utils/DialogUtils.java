package com.wpam.kupmi.utils;

import android.app.AlertDialog;
import android.content.Context;

public class DialogUtils
{
    public static void showOKDialog(Context context, int titleId, int msgId, int iconId)
    {
        new AlertDialog.Builder(context)
            .setTitle(titleId)
            .setNeutralButton(android.R.string.ok, null)
            .setMessage(msgId)
            .setIcon(iconId)
            .show();
    }
}
