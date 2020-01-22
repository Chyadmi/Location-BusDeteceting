package com.example.location_busdeteceting;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    TextToSpeech txtSpeech;
    ImageView bgapp, clover;
    LinearLayout textsplash, texthome, menus,location,bus;
    Animation frombottom;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initializeTextToSpeech();


        frombottom = AnimationUtils.loadAnimation(this, R.anim.frombottom);


        bgapp = (ImageView) findViewById(R.id.bgapp);
        clover = (ImageView) findViewById(R.id.clover);
        textsplash = (LinearLayout) findViewById(R.id.textsplash);
        texthome = (LinearLayout) findViewById(R.id.texthome);
        menus = (LinearLayout) findViewById(R.id.menus);
        location=findViewById(R.id.loc);
        bus=findViewById(R.id.bus);

        bgapp.animate().translationY(-2000).setDuration(2000).setStartDelay(300);
        clover.animate().translationX(-2000).alpha(0).setDuration(3000).setStartDelay(600);
        textsplash.animate().translationY(140).alpha(0).setDuration(800).setStartDelay(300);
        texthome.startAnimation(frombottom);
        menus.startAnimation(frombottom);


        location.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                speake("location service");
            }
        });   ///the speak fct see bottom
        bus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                speake("bus service");
            }
        });///the speak fct see bottom

    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        setContentView(R.layout.activity_main);
    }

    /////speake fct to speake a msg
    private void speake(String message) {
        if(Build.VERSION.SDK_INT>21){
            txtSpeech.speak(message,TextToSpeech.QUEUE_FLUSH,null,null);
        }
        else{
            txtSpeech.speak(message,TextToSpeech.QUEUE_FLUSH,null);

        }
    }

    //initialise txtToSpeech to a language
    private void initializeTextToSpeech() {
        txtSpeech=new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(txtSpeech.getEngines().size()==0){
                    Toast.makeText(MainActivity.this, "no tts", Toast.LENGTH_SHORT).show();
                    finish();
                }
                else{
                    txtSpeech.setLanguage(Locale.US);      //you can choose any language
                    speake("Welcome I am ready");
                }

            }
        });
    }
}
