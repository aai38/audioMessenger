package com.github.chagall.notificationlistenerexample;

import android.content.Intent;
import android.graphics.Color;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.github.paolorotolo.appintro.AppIntro;
import com.github.paolorotolo.appintro.AppIntroFragment;

public class MyIntro extends AppIntro {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addSlide(AppIntroFragment.newInstance(
                "Kurzanleitung",
                "Du kannst zu jeder Zeit auf das Mikrofon klicken und bestimmte Signalwörter einsprechen. Die verfügbaren Signalwörter sind 'Schreibe', 'Abhören', 'Alle' und 'Abbruch'.",
                R.drawable.iconfinder_mic_1055024,
                Color.parseColor("black")));

        addSlide(AppIntroFragment.newInstance(
                "Nachricht schreiben",
                "Um eine Nachricht zu schreiben, sag 'Schreibe'. Im Anschluss kommt ein Signalton. " +
                        "Nach dem Ton sprichst du deine Nachricht ein. Diese wird dir nochmal vorgelesen.",
                R.drawable.iconfinder_mic_1055024,
                Color.parseColor("black")));

        addSlide(AppIntroFragment.newInstance(
                "Nachricht schreiben",
                "Anschließend gibst du per Sprachbefehl deinen Kontakt ein. Dieser wird dir ebenfalls nochmal vorgelesen. Falls der Kontakt falsch erkannt wurde, sage 'Nein' oder 'Stop'." +
                        "Du kannst den Kontakt erneut eingeben. Wenn er dreimal falsch erkannt wurde, kannst du den Kontakt von Hand auswählen.",
                R.drawable.iconfinder_mic_1055024,
                Color.parseColor("black")));

        addSlide(AppIntroFragment.newInstance(
                "Nachrichten abhören",
                "Du kannst das Signalwort 'Abhören' sprechen und nach dem Signalton den Namen des Kontaktes nennen, von dem du die Nachrichten abhören möchtest. " +
                        "Wenn du alle neuen Nachrichten abhören möchtest, spreche stattdessen das Signalwort 'Alle'.",
                R.drawable.iconfinder_mic_1055024,
                Color.parseColor("black")));

        addSlide(AppIntroFragment.newInstance(
                "Nachrichten beantworten",
                "Nachdem dir die Nachricht eines Kontaktes vorgelesen wurde, kannst du direkt darauf antworten. Dafür sagst du direkt nach der Ausgabe der Nachricht" +
                        " 'Antworten'. Daraufhin wird ein Signalton ausgegeben, jetzt kannst du deine Antwort per Sprachbefehl eingeben.",
                R.drawable.iconfinder_mic_1055024,
                Color.parseColor("black")));

        addSlide(AppIntroFragment.newInstance(
                "Abbruch",
                "Du hast bei jeder Spracheingabe die Möglichkeit das Signalwort 'Abbruch' zu nennen. Daraufhin wird die laufende Aktion sofort abgebrochen.",
                R.drawable.baseline_clear_white,
                Color.parseColor("black")));

        addSlide(AppIntroFragment.newInstance(
                "Headset",
                "Du kannst die App auch über ein Headset verwenden. Über 'Play/Pause' kannst du alle Nachrichten abhören. " +
                        "Über die Taste 'Vorwärts' kannst du auf eine Nachricht antworten und über die 'Zurück'-Taste kannst du eine Nachricht schreiben.",
                R.drawable.headset_keys,
                Color.parseColor("black")));

        addSlide(AppIntroFragment.newInstance(
                "Tutorial",
                "Du kannst jederzeit dieses Tutorial wieder aufrufen. Dazu klickst du auf den Button 'Anleitung'.",
                R.drawable.tutorial_button,
                Color.parseColor("black")));


        showStatusBar(false);
        setBarColor(Color.parseColor("black"));
        setSeparatorColor(Color.parseColor("black"));

    }

    @Override
    public void onDonePressed() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        startActivity(intent);
        finish();
    }

    @Override
    public void onSkipPressed(Fragment currentFragment){
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        startActivity(intent);
    }
}
