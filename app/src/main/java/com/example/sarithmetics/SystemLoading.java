package com.example.sarithmetics;

import android.app.Activity;
import android.app.AlertDialog;
import android.view.LayoutInflater;

public class SystemLoading {

    private Activity activity;
    private AlertDialog dialog;

    SystemLoading(Activity activity) {
        this.activity = activity;
    }

    void startLoadingDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);

        LayoutInflater inflater = activity.getLayoutInflater();
        builder.setView(inflater.inflate(R.layout.loading_layout, null));
        builder.setCancelable(false);

        dialog = builder.create();
        dialog.show();
    }

    void dismissDialog() {
        dialog.dismiss();
    }

}
