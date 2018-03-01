package com.example.gargc.avruttilabs;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;

public class SplashScreenActivity extends AppCompatActivity implements Animation.AnimationListener{

    Animation animFadeIn;
    LinearLayout linearLayout;

    public static final String MY_PREFS_NAME = "MyINTRODUCTION";
    public static final String INTRODUCTION="INTRODUCTION";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        if (Build.VERSION.SDK_INT < 16) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);

        } else {
            View decorView = getWindow().getDecorView();
            // Hide the status bar.
            int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
            decorView.setSystemUiVisibility(uiOptions);
            // Remember that you should never show the action bar if the
            // status bar is hidden, so hide that too if necessary.
        }
        // load the animation
        animFadeIn = AnimationUtils.loadAnimation(getApplicationContext(),
                R.anim.animation_fade_in);
        // set animation listener
        animFadeIn.setAnimationListener(this);
        // animation for image
        linearLayout = (LinearLayout) findViewById(R.id.layout_linear);
        // start the animation
        linearLayout.setVisibility(View.VISIBLE);
        linearLayout.startAnimation(animFadeIn);

    }

    @Override
    public void onBackPressed() {
        this.finish();
        super.onBackPressed();
    }

    @Override
    public void onAnimationStart(Animation animation) {
        //under Implementation
    }

    public void onAnimationEnd(Animation animation) {
        // Start Main Screen

        SharedPreferences prefs = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE);
        boolean introduction = prefs.getBoolean(INTRODUCTION,true);

        Log.i("intro",introduction+" ");

        if(introduction) {
            SharedPreferences.Editor editor = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE).edit();
            introduction=false;
            editor.putBoolean(INTRODUCTION,introduction);
            editor.apply();

            Intent intent = new Intent(this, WelcomeActivity.class);
            startActivity(intent);

        }
        else {
            Intent i = new Intent(SplashScreenActivity.this, MainActivity.class);
            startActivity(i);
            this.finish();
        }
    }

    @Override
    public void onAnimationRepeat(Animation animation) {
        //under Implementation
    }
}
