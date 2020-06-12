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
                "Du kannst zu jeder Zeit auf das Mikrofon klicken und bestimmte Signalwörter einsprechen. Die verfügbaren Signalwörter sind 'Schreibe', 'Abhören', 'Alle'.",
                R.drawable.iconfinder_mic_1055024,
                Color.parseColor("black")));

        addSlide(AppIntroFragment.newInstance(
                "Nachricht schreiben",
                "Wenn du eine Nachricht schreiben möchtest, spreche das Wort 'Schreibe'. Im Anschluss kommt ein Signalton. " +
                        "Nach dem Ton sprichst du deine gewünschte Nachricht ein. Anschließend kannst du den Kontakt auswählen.",
                R.drawable.iconfinder_mic_1055024,
                Color.parseColor("black")));

        addSlide(AppIntroFragment.newInstance(
                "Nachrichten abhören",
                "Du kannst das Signalwort 'Abhören' sprechen und nach dem Signalton den Namen des Kontaktes nennen, von dem du die Nachrichten abhören möchtest. ",
                R.drawable.iconfinder_mic_1055024,
                Color.parseColor("black")));

        addSlide(AppIntroFragment.newInstance(
                "Favoriten festlegen",
                "Du kannst über das Stern-Icon deine Favoriten festlegen. Es öffnet sich eine Liste mit allen Kontakten, aus der du deine Favoriten wählen kannst.",
                R.drawable.iconfinder_star_381628,
                Color.parseColor("black")));

        addSlide(AppIntroFragment.newInstance(
                "Informationen abrufen",
                "Du kannst über das Info-Icon bestimmte Informationen abrufen. Es öffnet sich ein Fenster, welches Informationen über die Häufigkeiten der App-Nutzung anzeigt.",
                R.drawable.info_circle,
                Color.parseColor("black")));

        addSlide(AppIntroFragment.newInstance(
                "Aktiv Modus",
                "Über den Slider Aktiv Modus, kannst du die App auf aktiv bzw. inaktiv setzen.",
                R.drawable.active_mode,
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
