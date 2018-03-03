package com.example.gargc.avruttilabs.Activity;

import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.transition.TransitionInflater;
import android.util.Log;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.gargc.avruttilabs.Adapter.ViewPageAdapter;
import com.example.gargc.avruttilabs.Model.Offer;
import com.example.gargc.avruttilabs.R;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class ItemDetailsActivity extends AppCompatActivity {

    TextView itemTitle,itemCost,status,itemDescription;

    ViewPager viewPager;
    LinearLayout sliderDotspanel;
    int dotscount;
    ImageView[] dots;
    Offer offer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Build.VERSION.SDK_INT >= 21)
            getWindow().setSharedElementEnterTransition(TransitionInflater.from(this).inflateTransition(R.transition.shared_news_photo_transition));

        setContentView(R.layout.activity_item_details);

        offer=(Offer) getIntent().getSerializableExtra("Item");
        Log.i("log",offer.getPrice());

        sliderDotspanel=(LinearLayout) findViewById(R.id.SliderDots);

        itemTitle=(TextView) findViewById(R.id.itemName);
        itemCost=(TextView) findViewById(R.id.itemCost1);
        status=(TextView) findViewById(R.id.modeOfDelivery);
        itemDescription=(TextView) findViewById(R.id.itemDetails);

        viewPager=(ViewPager) findViewById(R.id.viewpager);

        ArrayList<String> arrayList=new ArrayList<>();
        arrayList.add(offer.getImage());
        arrayList.add(offer.getImage2());

        ViewPageAdapter viewPageAdapter=new ViewPageAdapter(getApplicationContext(),arrayList);

        viewPager.setAdapter(viewPageAdapter);
        dotscount=viewPageAdapter.getCount();
        dots=new ImageView[dotscount];

        for(int i=0;i<dotscount;i++)
        {
            dots[i]=new ImageView(this);
            dots[i].setImageDrawable(ContextCompat.getDrawable(getApplicationContext(),R.drawable.nonactive_dots));

            LinearLayout.LayoutParams params=new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT);
            params.setMargins(8,0,8,0);

            sliderDotspanel.addView(dots[i],params);

        }
        dots[0].setImageDrawable(ContextCompat.getDrawable(getApplicationContext(),R.drawable.active_dots));

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                for(int i=0;i<dotscount;i++)
                {
                    dots[i].setImageDrawable(ContextCompat.getDrawable(getApplicationContext(),R.drawable.nonactive_dots));

                }
                dots[position].setImageDrawable(ContextCompat.getDrawable(getApplicationContext(),R.drawable.active_dots));

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        Timer timer=new Timer();
        timer.scheduleAtFixedRate(new MyTimerTask(),2000,4000);



    }

    @Override
    protected void onStart() {
        super.onStart();
        itemTitle.setText(offer.getTitle());
        itemCost.setText(offer.getPrice());
        status.setText(offer.getStatus());
        itemDescription.setText(offer.getDescription());
    }

    private class MyTimerTask extends TimerTask {
        @Override
        public void run() {
            ItemDetailsActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if(viewPager.getCurrentItem()==0)
                    {
                        viewPager.setCurrentItem(1);
                    }
                    else if(viewPager.getCurrentItem()==1)
                    {
                        viewPager.setCurrentItem(0);
                    }
                    else {
                        viewPager.setCurrentItem(0);
                    }

                }
            });
        }
    }
}
