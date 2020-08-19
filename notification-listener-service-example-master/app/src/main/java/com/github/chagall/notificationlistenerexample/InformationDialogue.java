package com.github.chagall.notificationlistenerexample;

import android.app.AlertDialog;
import android.app.Application;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatDialogFragment;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.logging.Logger;

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
        String message = "Aufrufe: " + calls_before + "\nAntworten: " + answers_before + "\nAbbruch: " + number_cancel + "\nAlle abhören: "
                + number_hearall + "\nSchreibe: " + number_write + "\nEine abhören: " + number_hearone + "\nError: " + number_error
                + "\nFalscher Kontakt/Nachricht: " + number_falseContact;
        builder.setMessage(message)
                .setTitle("Informationen")
                .setPositiveButton("Send E-Mail", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        writeStringAsFile(message, "data.txt", getContext());
                        Intent intent = new Intent(Intent.ACTION_SEND);
                        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        intent.setType("message/rfc822");
                        intent.putExtra(Intent.EXTRA_EMAIL  , new String[]{"karolin.bartlmae@uni-ulm.de"});
                        intent.putExtra(Intent.EXTRA_SUBJECT, "Daten für Studie");
                        intent.putExtra(Intent.EXTRA_TEXT   , "Hallo, \n \n anbei die Daten von heute. \n \n Liebe Grüße\n");
                        Uri u = FileProvider.getUriForFile(getContext(), BuildConfig.APPLICATION_ID + ".provider", getContext().getFileStreamPath("data.txt"));
                        intent.putExtra(Intent.EXTRA_STREAM   , u);
                        try {
                            startActivity(Intent.createChooser(intent, "Sende Mail..."));
                        } catch (android.content.ActivityNotFoundException ex) {
                            Toast.makeText(getContext(), "Es sind keine Mail-Clients installiert, weshalb die Mail nicht versendet werden kann.", Toast.LENGTH_LONG).show();
                        }
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                });
        AlertDialog dialog = builder.create();

        return builder.create();
    }

    public static void writeStringAsFile(final String fileContents, String fileName, Context context) {

        try {
            FileOutputStream fos = new FileOutputStream(fileName);
            fos.write(fileContents.getBytes());
            fos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
