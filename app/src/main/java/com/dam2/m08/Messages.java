package com.dam2.m08;

import android.content.Context;
import android.widget.Toast;

public class Messages {
    public static void showMessage(Context context, String error) {
        Toast.makeText(context, error, Toast.LENGTH_LONG).show();
    }
}
