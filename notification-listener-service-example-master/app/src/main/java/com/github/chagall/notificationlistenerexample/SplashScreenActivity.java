package com.github.chagall.notificationlistenerexample;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

public class SplashScreenActivity extends AppCompatActivity {
    private static int SPLASH_TIME_OUT = 4000;
    Animation startAnimation;
    ImageView image;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_splash_screen);

        //Animation
        startAnimation = AnimationUtils.loadAnimation(this, R.anim.start_animation);

        image = findViewById(R.id.imageStartScreen);

        image.setAnimation(startAnimation);

        //Splash Screen
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run(){
                SharedPreferences getPrefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
                boolean isFirstStart = getPrefs.getBoolean("firstStart", true);
                //show the intro only at the first start
                if(isFirstStart) {
                    startActivity(new Intent(SplashScreenActivity.this, MyIntro.class));
                    SharedPreferences.Editor editor = getPrefs.edit();
                    editor.putBoolean("firstStart", false);
                    editor.apply();
                }
                //else start the MainActivity
                else startActivity(new Intent(SplashScreenActivity.this, MainActivity.class));

                //Intent homeIntent = new Intent(SplashScreenActivity.this, MyIntro.class);
                //startActivity(homeIntent);
                finish();
            }
        }, SPLASH_TIME_OUT);
    }
}
