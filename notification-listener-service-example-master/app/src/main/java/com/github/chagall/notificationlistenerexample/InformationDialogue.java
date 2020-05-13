package com.github.chagall.notificationlistenerexample;

import android.app.AlertDialog;
import android.app.Application;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatDialogFragment;

public class InformationDialogue extends AppCompatDialogFragment {

    private SharedPreferences shared;
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        shared = getActivity().getPreferences(Context.MODE_PRIVATE);
        int calls_before = shared.getInt("calls", 0);
        int answers_before = shared.getInt("answers", 0);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage("Aufrufe: " + calls_before + "Antworten: " + answers_before)
                .setTitle("Informationen")
                .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                });
        AlertDialog dialog = builder.create();
        return builder.create();
    }
}
