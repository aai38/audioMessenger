package com.github.chagall.notificationlistenerexample;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AppCompatDialogFragment;

public class PlayingMessagesDialog extends AppCompatDialogFragment {

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Nachrichten abhören").setMessage("Du hast das Signalwort 'Abhören' eingesprochen. Nachdem du diesen Dialog geschlossen hast kommt ein Signalton. " +
                "Nach diesem kannst du den Namen des Kontaktes nennen, von dem du die Nachrichten abhören möchtest.").setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                MainActivity.waitForDialog = false;
            }
        });
        return builder.create();
    }
}
