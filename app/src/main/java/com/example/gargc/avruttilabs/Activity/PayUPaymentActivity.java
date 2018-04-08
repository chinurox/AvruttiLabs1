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
    private String mSuccessUrl = "your success URL";
    private String mFailedUrl = "Your Failure URL";

    private FirebaseAuth mAuth;
    private String uid , message = "",addressName;
    private DatabaseReference ordersDatabase,cartDatabase,userDatabase;
    private int count;

    @SuppressLint({"AddJavascriptInterface", "SetJavaScriptEnabled", "JavascriptInterface"})
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pay_upayment);

        webView = (WebView) findViewById(R.id.payumoney_webview);

        /**
         * Context Variable
         */
        activity = getApplicationContext();

        Random rand = new Random();
        String randomString = Integer.toString(rand.nextInt()) + (System.currentTimeMillis() / 1000L);
        mTXNId = hashCal("SHA-256", randomString).substring(0, 20);

        mAuth = FirebaseAuth.getInstance();
        uid = mAuth.getCurrentUser().getUid();
        userDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(uid);
        ordersDatabase = FirebaseDatabase.getInstance().getReference().child("Orders").child(uid);
        cartDatabase = FirebaseDatabase.getInstance().getReference().child("Cart").child(uid);

        Intent intent = getIntent();
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
            }

            @Override
            public void onReceivedSslError(WebView view,
                                           SslErrorHandler handler, SslError error) {
                Toast.makeText(activity, "SSL Error! " + error, Toast.LENGTH_SHORT).show();
                handler.proceed();
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                return super.shouldOverrideUrlLoading(view, url);
            }

            @Override
            public void onPageFinished(WebView view, String url) {

                /*if (url.equals(mSuccessUrl)) {
                    Intent intent = new Intent(PayUPaymentActivity.this, PaymentStatusActivity.class);
                    intent.putExtra("status", true);
                    intent.putExtra("transaction_id", mTXNId);
                    intent.putExtra("id", mId);
                    intent.putExtra("isFromOrder", isFromOrder);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                } else if (url.equals(mFailedUrl)) {
                    Intent intent = new Intent(PayUMoneyActivity.this, PaymentStatusActivity.class);
                    intent.putExtra("status", false);
                    intent.putExtra("transaction_id", mTXNId);
                    intent.putExtra("id", mId);
                    intent.putExtra("isFromOrder", isFromOrder);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                }*/
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
        webView.addJavascriptInterface(new PayUJavaScriptInterface(PayUPaymentActivity.this), "PayUMoney");

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

        final String emailOfSender = mAuth.getCurrentUser().getEmail().toString();
        Log.i("mailofsender",emailOfSender);
        final String email3="Avruttilabs@gmail.com";
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

}

class PayUJavaScriptInterface
{
    Context mContext;
    DatabaseReference emailDatabase;

    /**
     * Instantiate the interface and set the context
     */
    PayUJavaScriptInterface(Context c) {
        mContext = c;
    }

    public void success(long id, final String paymentId)
    {
        emailDatabase = FirebaseDatabase.getInstance().getReference().child("Email");

        final Handler[] mHandler = {new Handler()};

        mHandler[0].post(new Runnable() {

            public void run() {
                mHandler[0] = null;

            }
        });

        final PayUPaymentActivity payUPaymentActivity = new PayUPaymentActivity();
        emailDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                Log.i("check", "======="+dataSnapshot.child("email").getValue());
                Log.i("check", "======="+dataSnapshot.child("password").getValue());

                payUPaymentActivity.sendEmail(dataSnapshot.child("email").getValue().toString(),dataSnapshot.child("password").getValue().toString(),paymentId);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

}


