package com.wpam.kupmi.utils;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

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

    public static void showYesNoDialog(Context context, int titleId, int msgId, int iconId,
                                       DialogInterface.OnClickListener positiveListener,
                                       DialogInterface.OnClickListener negativeListener)
    {
        new AlertDialog.Builder(context)
            .setTitle(titleId)
                .setPositiveButton(android.R.string.yes, positiveListener)
                .setNegativeButton(android.R.string.no, negativeListener)
                .setMessage(msgId)
                .setIcon(iconId)
                .show();
    }
}
