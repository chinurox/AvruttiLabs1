package com.example.gargc.avruttilabs.Activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.http.SslError;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.SslErrorHandler;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.example.gargc.avruttilabs.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.apache.http.util.EncodingUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Set;

public class PayUPaymentActivity extends AppCompatActivity
{

    WebView webView;
    Context activity;
    /**
     * Order Id
     * To Request for Updating Payment Status if Payment Successfully Done
     */
    int mId; //Getting from Previous Activity
    /**
     * Required Fields
     */
    // Test Variables
    /*
    */

    // Final Variables
    private String mMerchantKey = "eVzZtWh1";
    private String mSalt = "eiW32eMgYP";
    private String mBaseURL = "https://secure.payu.in";

    private String mAction = ""; // For Final URL
    private String mTXNId; // This will create below randomly
    private String mHash; // This will create below randomly
    private String mProductInfo = "Food Items"; //Passing String only
    private String mFirstName; // From Previous Activity
    private String mEmailId; // From Previous Activity
    private double mAmount; // From Previous Activity
    private String mPhone; // From Previous Activity
    private String mServiceProvider = "payu_paisa";
    private String mSuccessUrl = "https://payu.herokuapp.com/success";
    private String mFailedUrl = "https://payu.herokuapp.com/failure";

    private FirebaseAuth mAuth;
    private String uid , message = "",addressName;
    private DatabaseReference ordersDatabase,cartDatabase,userDatabase,emailDatabase;
    private int count;

    //new
    String hash;

    @SuppressLint({"AddJavascriptInterface", "SetJavaScriptEnabled", "JavascriptInterface"})
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pay_upayment);

        webView = (WebView) findViewById(R.id.payumoney_webview);
        mEmailId=getIntent().getStringExtra("email");
        mPhone=getIntent().getStringExtra("phone");

        Log.i("email1",mEmailId);
        Log.i("mPhone",mPhone);


        /**
         * Context Variable
         */
        activity = getApplicationContext();

        Random rand = new Random();
        String randomString = Integer.toString(rand.nextInt()) + (System.currentTimeMillis() / 1000L);
        mTXNId = hashCal("SHA-256", randomString).substring(0, 20);
        Log.i("checking","working");

        mAuth = FirebaseAuth.getInstance();
        uid = mAuth.getCurrentUser().getUid();
        userDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(uid);
        ordersDatabase = FirebaseDatabase.getInstance().getReference().child("Orders").child(uid);
        cartDatabase = FirebaseDatabase.getInstance().getReference().child("Cart").child(uid);
        emailDatabase = FirebaseDatabase.getInstance().getReference().child("Email");

        Intent intent = getIntent();
        // mAmount = 1L;
        mAmount = Double.parseDouble(intent.getExtras().getString("cost"));
        addressName = intent.getExtras().getString("address");

        userDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mFirstName = dataSnapshot.child("name").getValue().toString();
                mEmailId = dataSnapshot.child("emailId").getValue().toString();
                mPhone = dataSnapshot.child("phonenumber").getValue().toString();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        /**
         * Creating Hash Key
         */
        mHash = hashCal("SHA-512", mMerchantKey + "|" +
                mTXNId + "|" +
                mAmount + "|" +
                mProductInfo + "|" +
                mFirstName + "|" +
                mEmailId + "|||||||||||" +
                mSalt);



        hash=hashCal("SHA-512",mHash);


        /**
         * Final Action URL...
         */
        mAction = mBaseURL.concat("/_payment");

        /**
         * WebView Client
         */
        webView.setWebViewClient(new WebViewClient() {

            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                super.onReceivedError(view, request, error);
                Toast.makeText(activity, "Oh no! " + error, Toast.LENGTH_SHORT).show();
                Log.i("checking","working1");

            }

            @Override
            public void onReceivedSslError(WebView view,
                                           SslErrorHandler handler, SslError error) {
                Toast.makeText(activity, "SSL Error! " + error, Toast.LENGTH_SHORT).show();
                Log.i("checking","working2");

                handler.proceed();
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                return super.shouldOverrideUrlLoading(view, url);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                Log.i("checking","working3");
                Log.i("checking",url);



                if (url.equals(mSuccessUrl)) {
                    Log.i("going","finish success");
                    Log.i("checking","working4");

                    send();



                }
                else {
                    Log.i("going","finish failure");
                    Log.i("checking","working5");

                    //send();


                }
                super.onPageFinished(view, url);
            }
        });

        webView.setVisibility(View.VISIBLE);
        webView.getSettings().setBuiltInZoomControls(true);
        webView.getSettings().setCacheMode(WebSettings.LOAD_DEFAULT);
        webView.getSettings().setDomStorageEnabled(true);
        webView.clearHistory();
        webView.clearCache(true);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setSupportZoom(true);
        webView.getSettings().setUseWideViewPort(false);
        webView.getSettings().setLoadWithOverviewMode(false);
        webView.addJavascriptInterface(new PayUJavaScriptInterface(this), "PayUMoney");
        Log.i("checking","working6");

        String post_Data = "hash="+hash+"&key="+mMerchantKey+"&txnid="+mTXNId+"&amount="+mAmount+
                "&productinfo="+mProductInfo+"&firstname="+mFirstName+ "&email="+mEmailId+"&phone="+mPhone+
                "&surl="+mSuccessUrl+"&furl="+ mFailedUrl+ "&service_provider="+ "payu_paisa";


        webView.postUrl("https://secure.payu.in/_payment", EncodingUtils.getBytes(post_Data, "base64"));


        /**
         * Mapping Compulsory Key Value Pairs
         */
        Map<String, String> mapParams = new HashMap<>();

        mapParams.put("key", mMerchantKey);
        mapParams.put("txnid", mTXNId);
        mapParams.put("amount", String.valueOf(mAmount));
        mapParams.put("productinfo", mProductInfo);
        mapParams.put("firstname", mFirstName);
        mapParams.put("email", mEmailId);
        mapParams.put("phone", mPhone);
        mapParams.put("surl", mSuccessUrl);
        mapParams.put("furl", mFailedUrl);
        mapParams.put("hash", mHash);
        mapParams.put("service_provider", mServiceProvider);

        webViewClientPost(webView, mAction, mapParams.entrySet());
    }


    /**
     * Posting Data on PayUMoney Site with Form
     *  @param webView
     * @param url
     * @param postData
     */
    public void webViewClientPost(WebView webView, String url,
                                  Set<Map.Entry<String, String>> postData) {
        StringBuilder sb = new StringBuilder();
        Log.i("checking","working7");


        sb.append("<html><head></head>");
        sb.append("<body onload='form1.submit()'>");
        sb.append(String.format("<form id='form1' action='%s' method='%s'>", url, "post"));

        for (Map.Entry<String, String> item : postData) {
            sb.append(String.format("<input name='%s' type='hidden' value='%s' />", item.getKey(), item.getValue()));
        }
        sb.append("</form></body></html>");

        Log.d("TAG", "webViewClientPost called: " + sb.toString());
        webView.loadData(sb.toString(), "text/html", "utf-8");
    }

    public void send()
    {
        //---------------MAIL PART-----------------------
        emailDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                Log.i("check", "======="+dataSnapshot.child("email").getValue());
                Log.i("check", "======="+dataSnapshot.child("password").getValue());

                sendEmail(dataSnapshot.child("email").getValue().toString(),dataSnapshot.child("password").getValue().toString(),mTXNId);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        //---------------MAIL PART-----------------------

        Intent intent = new Intent(PayUPaymentActivity.this, MainActivity.class);
        startActivity(intent);
        finish();

    }

    /**
     * Hash Key Calculation
     *
     * @param type
     * @param str
     * @return
     */
    public String hashCal(String type, String str) {
        byte[] hashSequence = str.getBytes();
        StringBuffer hexString = new StringBuffer();
        try {
            MessageDigest algorithm = MessageDigest.getInstance(type);
            algorithm.reset();
            algorithm.update(hashSequence);
            Log.i("checking","working8");

            byte messageDigest[] = algorithm.digest();

            for (int i = 0; i < messageDigest.length; i++) {
                String hex = Integer.toHexString(0xFF & messageDigest[i]);
                if (hex.length() == 1)
                    hexString.append("0");
                hexString.append(hex);
            }
        } catch (NoSuchAlgorithmException NSAE) {
        }
        return hexString.toString();
    }

    @Override
    public void onBackPressed() {
        onPressingBack();
    }

    /**
     * On Pressing Back
     * Giving Alert...
     */
    private void onPressingBack() {

        final Intent intent;

        intent = new Intent(PayUPaymentActivity.this, CartActivity.class);

        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(PayUPaymentActivity.this);
        Log.i("checking","working9");


        // Setting Dialog Title
        alertDialog.setTitle("Warning");

        // Setting Dialog Message
        alertDialog.setMessage("Do you cancel this transaction?");

        // On pressing Settings button
        alertDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                finish();
                startActivity(intent);
            }
        });

        // on pressing cancel button
        alertDialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        // Showing Alert Message
        alertDialog.show();
    }

    public void sendEmail(String email1, final String password1,String txnId)
    {
        final String email=email1;
        final String password=password1;
        Log.i("checking","working10");


        final String emailOfSender = mAuth.getCurrentUser().getEmail().toString();
        Log.i("mailofsender",emailOfSender);

        final String email3="avruttielectronics@gmail.com";
       // final String email3="cheenug10@gmail.com";

        final String subject = "Placement Of Order";
        message = "Transaction Id --> "+txnId+"\n\nCustomer Details";

        DatabaseReference userDetails = userDatabase;
        final DatabaseReference userOrders = ordersDatabase;

        userDetails.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                message = message + "\n\nCustomer name --> "+dataSnapshot.child("name").getValue()
                        +"\n\nCustomer email--> "+dataSnapshot.child("emailId").getValue()
                        +"\n\nCustomer phone number--> "+dataSnapshot.child("phonenumber").getValue()
                        +"\n\nCustomer Address-->"+addressName;

                Log.i("message",message);

                //Creating SendMail object
                SendMail sm = new SendMail(PayUPaymentActivity.this, email3, subject, message,email,password);

                //Executing sendmail to send email
                sm.execute();

                //Creating SendMail object
                String subject1="Thank you for shopping from Avrutti.You will receive the shipping details shortly";
                SendMail sm1 = new SendMail(PayUPaymentActivity.this, emailOfSender,subject1 , message,email,password);

                //Executing sendmail to send email
                sm1.execute();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        DatabaseReference priceDatabase = cartDatabase;
        priceDatabase.addListenerForSingleValueEvent(new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {

                for (DataSnapshot snapshot : dataSnapshot.getChildren())
                {
                    Log.i("snapshot",snapshot.toString());

                    count++;
                    message = message +"\n\nProduct --> "+count +"\n\nName -->"+snapshot.child("title").getValue()
                            +"\n\nquantity --> "+snapshot.child("quantity").getValue()
                            +"\n\nprice --> "+snapshot.child("price").getValue();
                    //+"\n\nTotal price "+totalCost;

                    HashMap orderMap = new HashMap();
                    orderMap.put("category",snapshot.child("category").getValue().toString());
                    orderMap.put("image",snapshot.child("image").getValue().toString());
                    orderMap.put("price",snapshot.child("price").getValue().toString());
                    orderMap.put("quantity",snapshot.child("quantity").getValue().toString());
                    orderMap.put("subcategory",snapshot.child("subCategory").getValue().toString());
                    orderMap.put("title",snapshot.child("title").getValue().toString());

                    userOrders.child(snapshot.child("title").getValue().toString()).setValue(orderMap);
                }
                message=message+"\n\nTotal price "+mAmount;

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        priceDatabase.removeValue();

    }

    public class PayUJavaScriptInterface {
        Context mContext;
        Handler mHandler = new Handler();

        /** Instantiate the interface and set the context */
        PayUJavaScriptInterface(Context c) {
            mContext = c;
        }


        public void success(long id, final String paymentId) {
            Log.i("checking","success1");

            mHandler.post(new Runnable() {

                public void run() {
                    Log.i("checking","success");

                    mHandler = null;

                    Intent intent = new Intent(PayUPaymentActivity.this, PayUPaymentActivity.class);

                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

                    intent.putExtra("result", "success");

                    intent.putExtra("paymentId", paymentId);

                    startActivity(intent);

                    finish();

                }

            });

        }

    }
}




