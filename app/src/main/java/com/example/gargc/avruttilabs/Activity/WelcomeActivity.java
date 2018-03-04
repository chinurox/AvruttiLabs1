package com.example.gargc.avruttilabs.Activity;

import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.os.Bundle;

import com.example.gargc.avruttilabs.Fragments.SampleSlide;
import com.example.gargc.avruttilabs.R;
import com.github.paolorotolo.appintro.AppIntro;

public class WelcomeActivity extends AppIntro {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addSlide(SampleSlide.newInstance(R.layout.welcome_slide1));
        addSlide(SampleSlide.newInstance(R.layout.welcome_slide2));
        addSlide(SampleSlide.newInstance(R.layout.welcome_slide3));
        addSlide(SampleSlide.newInstance(R.layout.welcome_slide4));

        setFadeAnimation();

    }

    @Override
    public void onSkipPressed(Fragment currentFragment) {
        super.onSkipPressed(currentFragment);
        Intent i = new Intent(WelcomeActivity.this, MainActivity.class);
        startActivity(i);
        finish();

    }
    @Override
    public void onDonePressed(Fragment currentFragment) {
        super.onDonePressed(currentFragment);
        Intent i = new Intent(WelcomeActivity.this, MainActivity.class);
        startActivity(i);
        finish();
    }

    @Override
    public void onSlideChanged(@Nullable Fragment oldFragment, @Nullable Fragment newFragment) {
        super.onSlideChanged(oldFragment, newFragment);

    }
}
