package com.github.chagall.notificationlistenerexample;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AppCompatDialogFragment;

public class WriteDialog extends AppCompatDialogFragment {

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Nachricht schreiben").setMessage("Du hast das Schlüsselwort 'Schreibe' gesagt. Sobald du diesen Dialog geschlossen hast kommt ein Signalton. " +
                "Nach dem Ton sprichst du deine Nachricht ein. Diese wird dir nochmal vorgelesen. Anschließend gibst du per Sprachbefehl deinen Kontakt ein. Dieser wird dir ebenfalls nochmal vorgelesen. " +
                "Falls der Kontakt falsch erkannt wurde, 'Nein' oder 'Stop' sagen. Du kannst den Kontakt erneut eingeben. Wenn er dreimal falsch erkannt wurde, kannst du den Kontakt von Hand auswählen.").setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                MainActivity.waitForDialog = false;
            }
        });
        return builder.create();
    }
}
