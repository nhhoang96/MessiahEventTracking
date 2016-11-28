package com.example.hoang.normalapp;

import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;

import java.util.HashMap;
import java.util.Locale;

/**
 * Created by Hoang Nguyen on 11/27/2016.
 */

public class OpeningPage extends AppCompatActivity {

    private TextToSpeech speech;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_logosplash);
        final String openingSpeech = "Welcome to Messiah Event Tracking Application";
        //speech = new TextToSpeech(this, this);
        speech = new TextToSpeech(getBaseContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
                    speech.setLanguage(Locale.US);
                    speakWords(openingSpeech);
                }

            }
        });

        Thread threadSplash = new Thread() {

            public void run() {
                try {
                    sleep(5500);
                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                    startActivity(intent);
                    finish();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };

        threadSplash.start();
    }

    public void speakWords(String speechText) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            speakGreater21(speechText);
        } else {
            speakUnder20(speechText);
        }
    }

    @SuppressWarnings("deprecation")
    private void speakUnder20(String text) {
        HashMap<String, String> map = new HashMap<>();
        map.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "MessageId");
        speech.speak(text, TextToSpeech.QUEUE_FLUSH, map);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void speakGreater21(String text) {
        String utteranceId = this.hashCode() + "";
        speech.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId);
    }

}
