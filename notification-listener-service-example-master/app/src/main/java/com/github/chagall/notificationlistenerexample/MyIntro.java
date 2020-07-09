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
                        "Nach dem Ton sprichst du deine Nachricht ein. Anschließend kannst du den Kontakt via Sprachbefehl eingeben.",
                R.drawable.iconfinder_mic_1055024,
                Color.parseColor("black")));

        addSlide(AppIntroFragment.newInstance(
                "Nachricht schreiben",
                "Wenn du sowohl deine Nachricht als auch den Kontakt eingegeben hast, wird dir beides nochmal vorgelesen. Anschließend musst du das Absenden der Nachricht bestätigen oder verweigern.",
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
                "Favoriten festlegen",
                "Du kannst über das Stern-Icon einmalig 3 Favoriten festlegen. Es öffnet sich eine Liste mit allen Kontakten, aus der du deine 3 Favoriten wählen kannst.",
                R.drawable.iconfinder_star_381628_large,
                Color.parseColor("black")));

        addSlide(AppIntroFragment.newInstance(
                "Informationen abrufen",
                "Du kannst über das Info-Icon bestimmte Informationen abrufen. Es öffnet sich ein Fenster, welches Informationen über die Häufigkeiten der App-Nutzung anzeigt.",
                R.drawable.info_circle_large,
                Color.parseColor("black")));

        addSlide(AppIntroFragment.newInstance(
                "Aktiv Modus",
                "Über den Slider Aktiv Modus, kannst du die App auf aktiv bzw. inaktiv setzen.",
                R.drawable.active_mode,
                Color.parseColor("black")));

        addSlide(AppIntroFragment.newInstance(
                "Abbruch",
                "Du hast bei jeder Aktion die Möglichkeit das Signalwort 'Abbruch' zu nennen. Daraufhin wird die laufende Aktion sofort abgebrochen.",
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
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    @Override
    public void onSkipPressed(Fragment currentFragment){
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }
}
