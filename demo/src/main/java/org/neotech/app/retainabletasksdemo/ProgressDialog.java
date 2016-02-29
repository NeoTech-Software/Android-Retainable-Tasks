package org.neotech.app.retainabletasksdemo;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

/**
 * Created by Rolf on 29-2-2016.
 */
public class ProgressDialog extends DialogFragment implements DialogInterface.OnClickListener {

    public static ProgressDialog getExistingInstance(FragmentManager fragmentManager, String tag){
        return (ProgressDialog) fragmentManager.findFragmentByTag(tag);
    }

    public static ProgressDialog showIfNotShowing(FragmentManager fragmentManager, String tag){
        ProgressDialog fragment = (ProgressDialog) fragmentManager.findFragmentByTag(tag);

        if (fragment == null) {
            fragment = new ProgressDialog();
        } else if(fragment.isAdded()){
            return fragment;
        }

        fragment.show(fragmentManager, tag);
        return fragment;
    }

    private TextView progressPercentage;
    private TextView progressCount;
    private ProgressBar progressBar;
    private int progress = 0;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View view = LayoutInflater.from(builder.getContext()).inflate(R.layout.dialog_progress, null, false);
        progressPercentage = (TextView) view.findViewById(R.id.progress_percentage);
        progressCount = (TextView) view.findViewById(R.id.progress_count);
        progressBar = (ProgressBar) view.findViewById(android.R.id.progress);
        progressBar.setMax(100);
        progressBar.setIndeterminate(false);
        builder.setView(view);
        builder.setPositiveButton("Cancel", this);
        setCancelable(false);
        return builder.create();
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        if(savedInstanceState != null) {
            progress = savedInstanceState.getInt("progress");
        }
        setProgress(progress);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("progress", progress);
    }

    public void setProgress(int progress){
        if(getDialog() != null) {
            progressBar.setProgress(progress);
            int percentage = (int) Math.round(((double) progress / (double) progressBar.getMax()) * 100.0);
            progressPercentage.setText("" + percentage + "%");
            progressCount.setText(progress + "/" + progressBar.getMax());
        }
        this.progress = progress;
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        ((OnAlertDialogClickListener) getActivity()).onDialogFragmentClick(this, which);
    }
}
