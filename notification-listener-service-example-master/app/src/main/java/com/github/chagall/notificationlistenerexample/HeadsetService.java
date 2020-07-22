package com.github.chagall.notificationlistenerexample;

import android.app.Service;
import android.content.Intent;
import android.media.SoundPool;
import android.media.session.MediaSession;
import android.media.session.PlaybackState;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.Toast;
import com.github.chagall.notificationlistenerexample.MainActivity;

import static android.media.AudioManager.STREAM_MUSIC;

public final class HeadsetService extends Service {
    String TAG = "HeadsetService";
    private int answerModeActiveEarcon;
    private SoundPool spool;

    private static MediaSession mediaSession;
    public static MainActivity mA;

    @Override
    public void onCreate() {
        spool = new SoundPool(1, STREAM_MUSIC, 0);
        answerModeActiveEarcon = spool.load(this, R.raw.earcon_answer_mode, 1);

        startService(new Intent(this, MainActivity.class));
        // Instantiate new MediaSession object.
        configureMediaSession();

    }

    @Override
    public void onDestroy() {
        if (mediaSession != null)
            mediaSession.release();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void configureMediaSession() {
        mediaSession = new MediaSession(this, TAG);

        // Overridden methods in the MediaSession.Callback class.
        mediaSession.setCallback(new MediaSession.Callback() {
            @Override
            public boolean onMediaButtonEvent(Intent mediaButtonIntent) {
                Log.d(TAG, "onMediaButtonEvent called: " + mediaButtonIntent);
                KeyEvent ke = mediaButtonIntent.getParcelableExtra(Intent.EXTRA_KEY_EVENT);
                if (ke != null && ke.getAction() == KeyEvent.ACTION_DOWN) {
                    int keyCode = ke.getKeyCode();
                    Log.d(TAG, "onMediaButtonEvent Received command: " + ke);
                    if(!MainActivity.isBusy) {
                        MainActivity.isBusy = true;
                        switch (keyCode){
                            case KeyEvent.KEYCODE_MEDIA_PLAY:
                                //alle
                                mA.reactToKeyword(3, false, 0);
                                MainActivity.isBusy = false;
                                TelegramListener.playNextMessage(true);
                                return true;
                            case KeyEvent.KEYCODE_MEDIA_NEXT:
                                //antworten
                                spool.play(answerModeActiveEarcon, 0.3f,0.3f,0,0,1.5f);
                                mA.reactToKeyword(0, true, TelegramListener.lastMessage.chatId);
                                MainActivity.isBusy = false;
                                TelegramListener.playNextMessage(true);
                                return true;
                            case KeyEvent.KEYCODE_MEDIA_PREVIOUS:
                                //schreibe
                                spool.play(answerModeActiveEarcon, 0.3f,0.3f,0,0,1.5f);
                                mA.reactToKeyword(2, false, 0);
                                MainActivity.isBusy = false;
                                TelegramListener.playNextMessage(true);
                                return true;
                        }

                    }

                }
                return super.onMediaButtonEvent(mediaButtonIntent);
            }

            /*@Override
            public void onSkipToNext() {
                Log.d(TAG, "onSkipToNext called (media button pressed)");
                Toast.makeText(getApplicationContext(), "onSkipToNext called", Toast.LENGTH_SHORT).show();
                super.onSkipToNext();
            }

            @Override
            public void onSkipToPrevious() {
                Log.d(TAG, "onSkipToPrevious called (media button pressed)");
                Toast.makeText(getApplicationContext(), "onSkipToPrevious called", Toast.LENGTH_SHORT).show();
                super.onSkipToPrevious();
            }

            @Override
            public void onPause() {
                Log.d(TAG, "onPause called (media button pressed)");
                Toast.makeText(getApplicationContext(), "onPause called", Toast.LENGTH_SHORT).show();
                super.onPause();
            }

            @Override
            public void onPlay() {
                Log.d(TAG, "onPlay called (media button pressed)");
                super.onPlay();
            }

            @Override
            public void onStop() {
                Log.d(TAG, "onStop called (media button pressed)");
                super.onStop();
            }*/
        });

        mediaSession.setFlags(MediaSession.FLAG_HANDLES_MEDIA_BUTTONS | MediaSession.FLAG_HANDLES_TRANSPORT_CONTROLS);
        mediaSession.setActive(true);
    }

    private void setPlaybackState(@NonNull final int stateValue) {
        PlaybackState state = new PlaybackState.Builder()
                .setActions(PlaybackState.ACTION_PLAY | PlaybackState.ACTION_SKIP_TO_NEXT
                        | PlaybackState.ACTION_PAUSE | PlaybackState.ACTION_SKIP_TO_PREVIOUS
                        | PlaybackState.ACTION_STOP | PlaybackState.ACTION_PLAY_PAUSE)
                .setState(stateValue, PlaybackState.PLAYBACK_POSITION_UNKNOWN, 0)
                .build();

        mediaSession.setPlaybackState(state);
    }
}
