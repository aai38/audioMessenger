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
        int number_cancel = shared.getInt("number_cancel", 0);
        int number_hearall = shared.getInt("number_hearall", 0);
        int number_write = shared.getInt("number_write", 0);
        int number_hearone = shared.getInt("number_hearone", 0);
        int number_error = shared.getInt("number_error", 0);
        int number_falseContact = shared.getInt("number_falseContact", 0);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage("Aufrufe: " + calls_before + "\nAntworten: " + answers_before + "\nAbbruch: " + number_cancel + "\nAlle abhören: "
        + number_hearall + "\nSchreibe: " + number_write + "\nEine abhören: " + number_hearone + "\nError: " + number_error
        + "\nFalscher Kontakt/Nachricht: " + number_falseContact)
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
