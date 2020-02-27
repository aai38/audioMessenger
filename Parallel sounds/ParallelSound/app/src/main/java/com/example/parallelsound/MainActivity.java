package com.example.parallelsound;

import androidx.appcompat.app.AppCompatActivity;

import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    SoundPool soundPool;
    int schleuderSound = -1;
    int halloSound = -1;

    private Button btnPlayRing;
    private Button btnPlayBang;
    private Button btnPlayAll;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        btnPlayRing = (Button) findViewById(R.id.btnRing);
        btnPlayBang = (Button) findViewById(R.id.btnBang);
        btnPlayAll = (Button) findViewById(R.id.btnAll);

        setVolumeControlStream(AudioManager.STREAM_MUSIC);

        soundPool = new SoundPool(2, AudioManager.STREAM_MUSIC, 0);
        halloSound = soundPool.load(this, R.raw.hallo, 1);
        schleuderSound = soundPool.load(this, R.raw.schleudergang, 1);

        btnPlayRing.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                soundPool.play(schleuderSound, 1, 1, 0, 0, 1);
            }
        });

        btnPlayBang.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                soundPool.play(halloSound, 1, 1, 0, 3, 1);
            }
        });

        btnPlayAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                soundPool.play(schleuderSound, 50, 50, 0, 0, 1);
                soundPool.play(halloSound, 1, 1, 0, 3, 1);

            }
        });
    }
}

