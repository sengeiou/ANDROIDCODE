package com.smartism.znzk.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.smartism.znzk.R;
import com.smartism.znzk.activity.speech.IatSpeechActivity;
import com.smartism.znzk.activity.speech.TtsSpeechActivity;
import com.smartism.znzk.listener.speech.IatSettings;
import com.smartism.znzk.listener.speech.TtsSettings;

public class SettingSpeechActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting_speech);
        findViewById(R.id.rl_a1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(getApplicationContext(), IatSpeechActivity.class);
                startActivity(intent);
            }
        });

        findViewById(R.id.rl_a2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(getApplicationContext(), TtsSpeechActivity.class);
                startActivity(intent);
            }
        });

        findViewById(R.id.rl_a3).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(getApplicationContext(), IatSettings.class);
                startActivity(intent);
            }
        });

        findViewById(R.id.rl_a4).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(getApplicationContext(), TtsSettings.class);
                startActivity(intent);
            }
        });
    }
}
