package com.example.gargc.avruttilabs.Activity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
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
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.gargc.avruttilabs.Adapter.ViewPageAdapter;
import com.example.gargc.avruttilabs.Model.Offer;
import com.example.gargc.avruttilabs.PaymentGateway.Api;
import com.example.gargc.avruttilabs.PaymentGateway.Checksum;
import com.example.gargc.avruttilabs.PaymentGateway.Constants;
import com.example.gargc.avruttilabs.PaymentGateway.Paytm;
import com.example.gargc.avruttilabs.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.paytm.pgsdk.PaytmOrder;
import com.paytm.pgsdk.PaytmPGService;
import com.paytm.pgsdk.PaytmPaymentTransactionCallback;
import com.payu.india.Extras.PayUChecksum;
import com.payu.india.Model.PaymentParams;
import com.payu.india.Model.PayuConfig;
import com.payu.india.Model.PayuHashes;
import com.payu.india.Model.PostData;
import com.payu.india.Payu.Payu;
import com.payu.india.Payu.PayuConstants;
import com.payu.india.Payu.PayuErrors;
import com.payu.payuui.Activity.PayUBaseActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ItemDetailsActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener
{

    private Spinner itemQty;
    private DatabaseReference cartDatabase;
    private FirebaseAuth mAuth;
    private String uid;
    TextView itemTitle,itemCost,status,itemDescription,addCart,btnBuy,qtyTxt;
    ViewPager viewPager;
    LinearLayout sliderDotspanel;
    int dotscount;
    ImageView[] dots;
    Offer offer;
    long amount=0L;
    LinearLayout wishlist,similar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Build.VERSION.SDK_INT >= 21)
            getWindow().setSharedElementEnterTransition(TransitionInflater.from(this).inflateTransition(R.transition.shared_news_photo_transition));

        setContentView(R.layout.activity_item_details);

        offer=(Offer) getIntent().getSerializableExtra("Item");
        Log.i("log",offer.getPrice()+"");

        sliderDotspanel=(LinearLayout) findViewById(R.id.SliderDots);

        btnBuy = (TextView)findViewById(R.id.text_action_bottom2);
        itemTitle = (TextView) findViewById(R.id.itemName);
        itemCost = (TextView) findViewById(R.id.itemCost1);
        status = (TextView) findViewById(R.id.modeOfDelivery);
        itemDescription = (TextView) findViewById(R.id.itemDetails);
        addCart = (TextView) findViewById(R.id.text_action_bottom1);
        itemQty = (Spinner) findViewById(R.id.item_detail_qty);
        qtyTxt = (TextView) findViewById(R.id.item_detail_qty_text);

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

        similar=(LinearLayout) findViewById(R.id.similar);
        wishlist=(LinearLayout) findViewById(R.id.wishlist);

        similar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent =new Intent(ItemDetailsActivity.this,SimilarActivity.class);
                intent.putExtra("model",offer);
                startActivity(intent);

            }
        });

        wishlist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(ItemDetailsActivity.this,WishListActivity.class);
                startActivity(intent);

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

        long qty = Long.parseLong(itemQty.getSelectedItem().toString());
        amount *= qty;

        DatabaseReference userCartDB = cartDatabase.child(offer.getTitle());
        HashMap cartMap = new HashMap();
        cartMap.put("title",offer.getTitle());
        cartMap.put("image",offer.getImage());
        cartMap.put("price","₹ "+amount);
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

        String initCost = itemCost.getText().toString();
        initCost = initCost.substring(2);
        Log.i("initial cost", initCost + "");
        amount = Integer.parseInt(initCost);

        String itemStatus = status.getText().toString();
        if(itemStatus.equalsIgnoreCase("Out Of Stock")){
            Log.i("false","false");
            status.setTextColor(Color.parseColor("#FF0000"));
            addCart.setVisibility(View.GONE);
            btnBuy.setText("Out Of Stock");
            btnBuy.setEnabled(false);
        }else{
            Log.i("true","true");
            status.setTextColor(Color.parseColor("#008000"));
            btnBuy.setText("BUY NOW");
            addCart.setVisibility(View.VISIBLE);
            btnBuy.setEnabled(true);
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l)
    {
        qtyTxt.setText("Quantity "+itemQty.getSelectedItem().toString());
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