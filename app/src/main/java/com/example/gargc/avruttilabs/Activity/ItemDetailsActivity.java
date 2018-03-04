package com.example.gargc.avruttilabs.Activity;

import android.app.ProgressDialog;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.transition.TransitionInflater;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.gargc.avruttilabs.Adapter.ViewPageAdapter;
import com.example.gargc.avruttilabs.Model.Offer;
import com.example.gargc.avruttilabs.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class ItemDetailsActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    private Spinner itemQty;
    private DatabaseReference cartDatabase;
    private FirebaseAuth mAuth;
    private String uid;
    TextView itemTitle,itemCost,status,itemDescription,addCart;
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

        itemTitle = (TextView) findViewById(R.id.itemName);
        itemCost = (TextView) findViewById(R.id.itemCost1);
        status = (TextView) findViewById(R.id.modeOfDelivery);
        itemDescription = (TextView) findViewById(R.id.itemDetails);
        addCart = (TextView) findViewById(R.id.text_action_bottom1);
        itemQty = (Spinner) findViewById(R.id.item_detail_qty);

        mAuth = FirebaseAuth.getInstance();
        uid = mAuth.getCurrentUser().getUid();
        cartDatabase = FirebaseDatabase.getInstance().getReference().child("Cart").child(uid);

        viewPager=(ViewPager) findViewById(R.id.viewpager);

        itemQty.setOnItemSelectedListener(this);

        // Spinner Drop down elements
        List<String> categories = new ArrayList<String>();
        categories.add("1");
        categories.add("2");
        categories.add("3");
        categories.add("4");
        categories.add("5");
        categories.add("6");
        categories.add("7");
        categories.add("8");
        categories.add("9");
        categories.add("10");
        // Creating adapter for spinner
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, categories);

        // Drop down layout style - list view with radio button
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // attaching data adapter to spinner
        itemQty.setAdapter(dataAdapter);

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

        addCart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                addToCart();
            }
        });

    }

    private void addToCart()
    {
        final ProgressDialog mProgress = new ProgressDialog(ItemDetailsActivity.this);

        mProgress.setTitle("adding to cart...");
        mProgress.setCanceledOnTouchOutside(false);
        mProgress.show();

        Log.i("item qty",itemQty.getSelectedItem().toString());

        DatabaseReference userCartDB = cartDatabase.child(offer.getTitle());
        HashMap cartMap = new HashMap();
        cartMap.put("title",offer.getTitle());
        cartMap.put("image",offer.getImage());
        cartMap.put("price",offer.getPrice());
        cartMap.put("status",offer.getStatus());
        cartMap.put("category",offer.getCategory());
        cartMap.put("quantity",itemQty.getSelectedItem().toString());
        cartMap.put("subCategory",offer.getSubcategory());
        userCartDB.setValue(cartMap).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task)
            {
                if (task.isSuccessful())
                {
                    mProgress.dismiss();
                    Toast.makeText(ItemDetailsActivity.this, "Item added to cart", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    mProgress.dismiss();
                    Toast.makeText(ItemDetailsActivity.this, "couldnot be added", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        itemTitle.setText(offer.getTitle());
        itemCost.setText(offer.getPrice());
        status.setText(offer.getStatus());
        itemDescription.setText(offer.getDescription());
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {}

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
