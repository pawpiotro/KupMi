package com.wpam.kupmi.utils;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.InputType;
import android.view.View;
import android.widget.EditText;

import com.wpam.kupmi.R;

public class DialogUtils
{

    public static void showOKDialog(Context context, int titleId, int msgId, int iconId)
    {
        showOKDialog(context, titleId, msgId, iconId, null);
    }

    public static void showOKDialog(Context context, int titleId, int msgId, int iconId,
                                    DialogInterface.OnClickListener listener)
    {
        new AlertDialog.Builder(context)
                .setTitle(titleId)
                .setNeutralButton(R.string.ok, listener)
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
                .setPositiveButton(R.string.yes, positiveListener)
                .setNegativeButton(R.string.no, negativeListener)
                .setMessage(msgId)
                .setIcon(iconId)
                .show();
    }

    public static void showInputDialog(Context context, int titleId, int msgId, int iconId,
                                          View inputContainer,
                                          DialogInterface.OnClickListener positiveListener,
                                          DialogInterface.OnClickListener negativeListener)
    {
        new AlertDialog.Builder(context)
                .setTitle(titleId)
                .setView(inputContainer)
                .setPositiveButton(R.string.ok, positiveListener)
                .setNegativeButton(R.string.cancel, negativeListener)
                .setMessage(msgId)
                .setIcon(iconId)
                .show();
    }
}
