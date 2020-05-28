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
                "Sie können zu jeder Zeit auf das Mikrofon klicken und bestimmte Signalwörter einsprechen. Die verfügbaren Signalwörter sind 'Schreibe', 'Abhören', 'Alle'.",
                R.drawable.iconfinder_mic_1055024,
                Color.parseColor("black")));

        addSlide(AppIntroFragment.newInstance(
                "Nachricht schreiben",
                "Wenn Sie eine Nachricht schreiben möchten, sprechen Sie das Wort 'Schreibe' ein. Im Anschluss kommt ein Signalton. " +
                        "Nach dem Ton sprechen Sie Ihre gewünschte Nachricht ein. Anschließend können Sie den Kontakt auswählen.",
                R.drawable.iconfinder_mic_1055024,
                Color.parseColor("black")));

        addSlide(AppIntroFragment.newInstance(
                "Nachrichten abhören",
                "Sie können das Signalwort 'Abhören' sprechen und nach dem Signalton den Namen des Kontaktes nennen, von dem Sie die Nachrichten abhören möchten. ",
                R.drawable.iconfinder_mic_1055024,
                Color.parseColor("black")));

        addSlide(AppIntroFragment.newInstance(
                "Favoriten festlegen",
                "Sie können über das Stern-Icon Ihre Favoriten festlegen. Es öffnet sich eine Liste mit allen Kontakten, aus der Sie ihre Favoriten wählen können.",
                R.drawable.iconfinder_star_381628,
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
