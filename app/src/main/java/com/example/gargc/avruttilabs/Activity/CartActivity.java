package com.example.gargc.avruttilabs.Activity;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.gargc.avruttilabs.Model.CartModel;
import com.example.gargc.avruttilabs.R;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.payu.india.Extras.PayUChecksum;
import com.payu.india.Model.PaymentParams;
import com.payu.india.Model.PayuConfig;
import com.payu.india.Model.PayuHashes;
import com.payu.india.Model.PostData;
import com.payu.india.Payu.PayuConstants;
import com.payu.india.Payu.PayuErrors;
import com.payu.payuui.Activity.PayUBaseActivity;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;

public class CartActivity extends AppCompatActivity
{

    private String merchantKey, userCredentials;
    // These will hold all the payment parameters
    private PaymentParams mPaymentParams;
    // This sets the configuration
    private PayuConfig payuConfig;
    // Used when generating hash from SDK
    private PayUChecksum checksum;

    private DatabaseReference cartDatabase,emailDatabase,userDatabase,ordersDatabase,couponDatabase;
    private RecyclerView cartList;
    private FirebaseAuth mAuth;
    private String uid , message = "";
    private LinearLayout paymentLayout,emptyLayout,radioHolder,couponHolder;
    private Button shopNow;
    private TextView cartCost,cartCheckout;
    private Long totalCost = 0l;
    private int count;
    private EditText couponName;
    private Button couponApply;
    private RadioButton standard,express;
    private boolean isStandard=false,isExpress=false,isApplied=false;

    String addressName;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);
        Log.i("hello","hello");

        cartList = (RecyclerView)findViewById(R.id.cart_list);
        paymentLayout = (LinearLayout)findViewById(R.id.cart_layout_payment);
        emptyLayout = (LinearLayout)findViewById(R.id.layout_cart_empty);
        shopNow = (Button)findViewById(R.id.start_shop_btn);
        cartCost = (TextView)findViewById(R.id.cart_cost);
        cartCheckout = (TextView)findViewById(R.id.cart_checkout);
        standard = (RadioButton)findViewById(R.id.standard_radio_button);
        express = (RadioButton)findViewById(R.id.express_radio_button);
        radioHolder = (LinearLayout)findViewById(R.id.radio_holder);
        couponHolder = (LinearLayout)findViewById(R.id.coupon_holder);
        couponApply = (Button)findViewById(R.id.item_single_coupon_apply);
        couponName = (EditText)findViewById(R.id.item_single_coupon_name);

        mAuth = FirebaseAuth.getInstance();
        uid = mAuth.getCurrentUser().getUid();
        userDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(uid);
        cartDatabase = FirebaseDatabase.getInstance().getReference().child("Cart").child(uid);
        emailDatabase = FirebaseDatabase.getInstance().getReference().child("Email");
        ordersDatabase = FirebaseDatabase.getInstance().getReference().child("Orders").child(uid);
        couponDatabase = FirebaseDatabase.getInstance().getReference().child("Coupons");

        shopNow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                finish();
            }
        });

        cartDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                if(dataSnapshot.hasChildren())
                {
                    cartList.setVisibility(View.VISIBLE);
                    paymentLayout.setVisibility(View.VISIBLE);
                    emptyLayout.setVisibility(View.GONE);
                    radioHolder.setVisibility(View.VISIBLE);
                    couponHolder.setVisibility(View.VISIBLE);
                }
                else
                {
                    cartList.setVisibility(View.GONE);
                    paymentLayout.setVisibility(View.GONE);
                    emptyLayout.setVisibility(View.VISIBLE);
                    radioHolder.setVisibility(View.GONE);
                    couponHolder.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        cartList.setLayoutManager(new LinearLayoutManager(this));
        cartList.setHasFixedSize(true);

        couponApply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                applyCoupon();
            }
        });

        cartCheckout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                Log.i("working","working");

                if(isExpress)
                {
                    totalCost -= 110;
                    isExpress = false;
                }
                else if(isStandard)
                {
                    totalCost -= 60;
                    isStandard = false;
                }

                if(standard.isChecked()) {
                    totalCost += 60;
                }
                else if(express.isChecked()){
                    totalCost += 110;
                }
                else {
                    Toast.makeText(CartActivity.this, "please select a shipping option", Toast.LENGTH_SHORT).show();
                    return;
                }

                Log.i("finalcost",""+totalCost);

                AlertDialog.Builder alertDialogBuilder=new AlertDialog.Builder(CartActivity.this);

                alertDialogBuilder.setTitle("Please Enter the Address Details");
                final View inflater=View.inflate(CartActivity.this,R.layout.alertdialogview,null);
                alertDialogBuilder.setView(inflater).
                        setPositiveButton("OK", new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i)
                            {
                                EditText edtAddress = (EditText)inflater.findViewById(R.id.address);
                                EditText edtCity = (EditText)inflater.findViewById(R.id.city);
                                EditText edtState = (EditText)inflater.findViewById(R.id.state);
                                EditText edtPincode =(EditText)inflater.findViewById(R.id.pincode);
                                EditText edtCountry=(EditText)inflater.findViewById(R.id.country);

                                String address = edtAddress.getText().toString();
                                String city = edtCity.getText().toString();
                                String state = edtState.getText().toString();
                                String pincode = edtPincode.getText().toString();
                                String country = edtCountry.getText().toString();

                                addressName="Address : "+address+"  City : "+city+"  State : "+state+"  Country : "+country
                                        +"  Pincode : "+pincode;

                                if(TextUtils.isEmpty(address)||TextUtils.isEmpty(city)||TextUtils.isEmpty(state)||
                                        TextUtils.isEmpty(pincode)||TextUtils.isEmpty(country))
                                {
                                    Toast.makeText(CartActivity.this, "All the fields are mandatory!", Toast.LENGTH_SHORT).show();
                                }
                                else
                                {
                                    navigateToBaseActivity(view);
                                }

                            }
                        }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.cancel();

                    }
                });

                AlertDialog alertDialog=alertDialogBuilder.create();
                alertDialog.show();

            }
        });
    }

    private void applyCoupon()
    {
        final String cName = couponName.getText().toString();

        if(TextUtils.isEmpty(cName))
        {
            return;
        }

        couponDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                if(dataSnapshot.child(cName).exists()) {
                    DatabaseReference coupon = couponDatabase.child(cName);

                    if (!isApplied)
                    {

                        Log.i("datasnapshot", dataSnapshot.toString());

                        coupon.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                String status = dataSnapshot.child("status").getValue().toString();
                                if (status.equalsIgnoreCase("true")) {
                                    int disc = Integer.parseInt(dataSnapshot.child("discount").getValue().toString());
                                    totalCost = totalCost - ((totalCost * disc) / 100);
                                    cartCost.setText("Rs " + totalCost);
                                    isApplied = true;
                                    Toast.makeText(CartActivity.this, "Coupon Applied!!!", Toast.LENGTH_SHORT).show();
                                }
                                else
                                {
                                    Toast.makeText(CartActivity.this, "The Coupon is not valid", Toast.LENGTH_SHORT).show();
                                }

                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });

                    } else {
                        Toast.makeText(CartActivity.this, "Coupon does not exists or you have already applied one coupon", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    @Override
    protected void onResume()
    {
        super.onResume();

        isApplied = false;
        applyCoupon();
    }

    @Override
    protected void onStart()
    {
        FirebaseRecyclerAdapter<CartModel,CartViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<CartModel, CartViewHolder>
                (
                        CartModel.class,
                        R.layout.cart_single_item,
                        CartViewHolder.class,
                        cartDatabase
                )
        {
            @Override
            protected void populateViewHolder(CartViewHolder viewHolder, final CartModel model, int position)
            {
                Log.i("going","working");

                Picasso.with(CartActivity.this).load(model.getImage()).into(viewHolder.img);
                viewHolder.title.setText(model.getTitle());
                viewHolder.price.setText(model.getPrice());
                viewHolder.status.setText(model.getStatus());
                viewHolder.qty.setText("quantity : "+model.getQuantity());

                viewHolder.delete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view)
                    {
                        cartDatabase.child(model.getTitle()).removeValue();
                        Toast.makeText(CartActivity.this, "item removed", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        };

        cartList.setAdapter(firebaseRecyclerAdapter);

        DatabaseReference priceDatabase = cartDatabase;
        priceDatabase.addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                totalCost = 0l;

                for (DataSnapshot snapshot : dataSnapshot.getChildren())
                {
                    Log.i("snapshot",snapshot.toString());

                    String priceStr = snapshot.child("price").getValue().toString();
                    Log.i("before price",priceStr);
                    priceStr = priceStr.substring(2);
                    Log.i("price",priceStr);

                    Long price = Long.parseLong(priceStr);
                    totalCost = totalCost + price;
                }

                cartCost.setText("Rs"+" "+totalCost+"");
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        super.onStart();
    }

    public static class CartViewHolder extends RecyclerView.ViewHolder
    {
        ImageView img;
        TextView title,price,status,qty;
        LinearLayout delete;

        public CartViewHolder(View itemView)
        {
            super(itemView);

            img = (ImageView)itemView.findViewById(R.id.single_cart_image);
            title = (TextView)itemView.findViewById(R.id.single_cart_name);
            price = (TextView)itemView.findViewById(R.id.single_cart_price);
            status = (TextView)itemView.findViewById(R.id.single_cart_status);
            qty = (TextView)itemView.findViewById(R.id.single_cart_quantity);
            delete = (LinearLayout) itemView.findViewById(R.id.single_cart_delete);
        }

    }

    //----------------PAYU PAYMENT GATEWAY------------------------------

    /**
     * This method prepares all the payments params to be sent to PayuBaseActivity.java
     */
    public void navigateToBaseActivity(View view) {

        // merchantKey="";
        merchantKey = "eVzZtWh1";
        Log.i("cost",""+totalCost);
        String costSt = totalCost+"";
        Log.i("cost",""+costSt);
        String email = "payutest@gmail.com";

        int environment= PayuConstants.PRODUCTION_ENV;
        // int environment= PayuConstants.STAGING_ENV;
        userCredentials = merchantKey + ":" + email;

        //TODO Below are mandatory params for hash genetation
        mPaymentParams = new PaymentParams();
        /**
         * For Test Environment, merchantKey = please contact mobile.integration@payu.in with your app name and registered email id
         */
        mPaymentParams.setKey(merchantKey);
        mPaymentParams.setAmount(totalCost+"");
        mPaymentParams.setProductInfo("product_info");
        mPaymentParams.setFirstName("firstname");
        mPaymentParams.setEmail("test@gmail.com");
        mPaymentParams.setPhone("");


        /*
        * Transaction Id should be kept unique for each transaction.
        * */
        mPaymentParams.setTxnId("" + System.currentTimeMillis());

        /**
         * Surl --> Success url is where the transaction response is posted by PayU on successful transaction
         * Furl --> Failre url is where the transaction response is posted by PayU on failed transaction
         */
        mPaymentParams.setSurl("https://payu.herokuapp.com/success");
        mPaymentParams.setFurl("https://payu.herokuapp.com/failure");
        mPaymentParams.setNotifyURL(mPaymentParams.getSurl());  //for lazy pay

        /*
         * udf1 to udf5 are options params where you can pass additional information related to transaction.
         * If you don't want to use it, then send them as empty string like, udf1=""
         * */
        mPaymentParams.setUdf1("udf1");
        mPaymentParams.setUdf2("udf2");
        mPaymentParams.setUdf3("udf3");
        mPaymentParams.setUdf4("udf4");
        mPaymentParams.setUdf5("udf5");

        /**
         * These are used for store card feature. If you are not using it then user_credentials = "default"
         * user_credentials takes of the form like user_credentials = "merchant_key : user_id"
         * here merchant_key = your merchant key,
         * user_id = unique id related to user like, email, phone number, etc.
         * */
        mPaymentParams.setUserCredentials(userCredentials);

        //TODO Pass this param only if using offer key
        //mPaymentParams.setOfferKey("cardnumber@8370");

        //TODO Sets the payment environment in PayuConfig object
        payuConfig = new PayuConfig();
        payuConfig.setEnvironment(environment);
        //   payuConfig.setEnvironment(PayuConstants.MOBILE_STAGING_ENV);
        //TODO It is recommended to generate hash from server only. Keep your key and salt in server side hash generation code.
        generateHashFromServer(mPaymentParams);

        /**
         * Below approach for generating hash is not recommended. However, this approach can be used to test in PRODUCTION_ENV
         * if your server side hash generation code is not completely setup. While going live this approach for hash generation
         * should not be used.
         * */
        //String salt = "eCwWELxi";
        //   generateHashFromSDK(mPaymentParams, salt);

    }

    /******************************
     * Client hash generation
     ***********************************/
    // Do not use this, you may use this only for testing.
    // lets generate hashes.
    // This should be done from server side..
    // Do not keep salt anywhere in app.
    public void generateHashFromSDK(PaymentParams mPaymentParams, String salt) {
        PayuHashes payuHashes = new PayuHashes();
        PostData postData = new PostData();

        // payment Hash;
        checksum = null;
        checksum = new PayUChecksum();
        checksum.setAmount(mPaymentParams.getAmount());
        checksum.setKey(mPaymentParams.getKey());
        checksum.setTxnid(mPaymentParams.getTxnId());
        checksum.setEmail(mPaymentParams.getEmail());
        checksum.setSalt(salt);
        checksum.setProductinfo(mPaymentParams.getProductInfo());
        checksum.setFirstname(mPaymentParams.getFirstName());
        checksum.setUdf1(mPaymentParams.getUdf1());
        checksum.setUdf2(mPaymentParams.getUdf2());
        checksum.setUdf3(mPaymentParams.getUdf3());
        checksum.setUdf4(mPaymentParams.getUdf4());
        checksum.setUdf5(mPaymentParams.getUdf5());

        postData = checksum.getHash();
        if (postData.getCode() == PayuErrors.NO_ERROR) {
            payuHashes.setPaymentHash(postData.getResult());
        }

        // checksum for payemnt related details
        // var1 should be either user credentials or default
        String var1 = mPaymentParams.getUserCredentials() == null ? PayuConstants.DEFAULT : mPaymentParams.getUserCredentials();
        String key = mPaymentParams.getKey();

        if ((postData = calculateHash(key, PayuConstants.PAYMENT_RELATED_DETAILS_FOR_MOBILE_SDK, var1, salt)) != null && postData.getCode() == PayuErrors.NO_ERROR) // Assign post data first then check for success
            payuHashes.setPaymentRelatedDetailsForMobileSdkHash(postData.getResult());
        //vas
        if ((postData = calculateHash(key, PayuConstants.VAS_FOR_MOBILE_SDK, PayuConstants.DEFAULT, salt)) != null && postData.getCode() == PayuErrors.NO_ERROR)
            payuHashes.setVasForMobileSdkHash(postData.getResult());

        // getIbibocodes
        if ((postData = calculateHash(key, PayuConstants.GET_MERCHANT_IBIBO_CODES, PayuConstants.DEFAULT, salt)) != null && postData.getCode() == PayuErrors.NO_ERROR)
            payuHashes.setMerchantIbiboCodesHash(postData.getResult());

        if (!var1.contentEquals(PayuConstants.DEFAULT)) {
            // get user card
            if ((postData = calculateHash(key, PayuConstants.GET_USER_CARDS, var1, salt)) != null && postData.getCode() == PayuErrors.NO_ERROR) // todo rename storedc ard
                payuHashes.setStoredCardsHash(postData.getResult());
            // save user card
            if ((postData = calculateHash(key, PayuConstants.SAVE_USER_CARD, var1, salt)) != null && postData.getCode() == PayuErrors.NO_ERROR)
                payuHashes.setSaveCardHash(postData.getResult());
            // delete user card
            if ((postData = calculateHash(key, PayuConstants.DELETE_USER_CARD, var1, salt)) != null && postData.getCode() == PayuErrors.NO_ERROR)
                payuHashes.setDeleteCardHash(postData.getResult());
            // edit user card
            if ((postData = calculateHash(key, PayuConstants.EDIT_USER_CARD, var1, salt)) != null && postData.getCode() == PayuErrors.NO_ERROR)
                payuHashes.setEditCardHash(postData.getResult());
        }

        if (mPaymentParams.getOfferKey() != null) {
            postData = calculateHash(key, PayuConstants.OFFER_KEY, mPaymentParams.getOfferKey(), salt);
            if (postData.getCode() == PayuErrors.NO_ERROR) {
                payuHashes.setCheckOfferStatusHash(postData.getResult());
            }
        }

        if (mPaymentParams.getOfferKey() != null && (postData = calculateHash(key, PayuConstants.CHECK_OFFER_STATUS, mPaymentParams.getOfferKey(), salt)) != null && postData.getCode() == PayuErrors.NO_ERROR) {
            payuHashes.setCheckOfferStatusHash(postData.getResult());
        }

        // we have generated all the hases now lest launch sdk's ui
        launchSdkUI(payuHashes);
    }

    // deprecated, should be used only for testing.
    private PostData calculateHash(String key, String command, String var1, String salt) {
        checksum = null;
        checksum = new PayUChecksum();
        checksum.setKey(key);
        checksum.setCommand(command);
        checksum.setVar1(var1);
        checksum.setSalt(salt);
        return checksum.getHash();
    }

    /**
     * This method generates hash from server.
     *
     * @param mPaymentParams payments params used for hash generation
     */
    public void generateHashFromServer(PaymentParams mPaymentParams) {
        //nextButton.setEnabled(false); // lets not allow the user to click the button again and again.

        // lets create the post params
        StringBuffer postParamsBuffer = new StringBuffer();
        postParamsBuffer.append(concatParams(PayuConstants.KEY, mPaymentParams.getKey()));
        postParamsBuffer.append(concatParams(PayuConstants.AMOUNT, mPaymentParams.getAmount()));
        postParamsBuffer.append(concatParams(PayuConstants.TXNID, mPaymentParams.getTxnId()));
        postParamsBuffer.append(concatParams(PayuConstants.EMAIL, null == mPaymentParams.getEmail() ? "" : mPaymentParams.getEmail()));
        postParamsBuffer.append(concatParams(PayuConstants.PRODUCT_INFO, mPaymentParams.getProductInfo()));
        postParamsBuffer.append(concatParams(PayuConstants.FIRST_NAME, null == mPaymentParams.getFirstName() ? "" : mPaymentParams.getFirstName()));
        postParamsBuffer.append(concatParams(PayuConstants.UDF1, mPaymentParams.getUdf1() == null ? "" : mPaymentParams.getUdf1()));
        postParamsBuffer.append(concatParams(PayuConstants.UDF2, mPaymentParams.getUdf2() == null ? "" : mPaymentParams.getUdf2()));
        postParamsBuffer.append(concatParams(PayuConstants.UDF3, mPaymentParams.getUdf3() == null ? "" : mPaymentParams.getUdf3()));
        postParamsBuffer.append(concatParams(PayuConstants.UDF4, mPaymentParams.getUdf4() == null ? "" : mPaymentParams.getUdf4()));
        postParamsBuffer.append(concatParams(PayuConstants.UDF5, mPaymentParams.getUdf5() == null ? "" : mPaymentParams.getUdf5()));
        postParamsBuffer.append(concatParams(PayuConstants.USER_CREDENTIALS, mPaymentParams.getUserCredentials() == null ? PayuConstants.DEFAULT : mPaymentParams.getUserCredentials()));

        // for offer_key
        if (null != mPaymentParams.getOfferKey())
            postParamsBuffer.append(concatParams(PayuConstants.OFFER_KEY, mPaymentParams.getOfferKey()));

        String postParams = postParamsBuffer.charAt(postParamsBuffer.length() - 1) == '&' ? postParamsBuffer.substring(0, postParamsBuffer.length() - 1).toString() : postParamsBuffer.toString();

        // lets make an api call
        CartActivity.GetHashesFromServerTask getHashesFromServerTask = new CartActivity.GetHashesFromServerTask();
        getHashesFromServerTask.execute(postParams);
    }


    protected String concatParams(String key, String value) {
        return key + "=" + value + "&";
    }

    /**
     * This AsyncTask generates hash from server.
     */
    private class GetHashesFromServerTask extends AsyncTask<String, String, PayuHashes> {
        private ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(CartActivity.this);
            progressDialog.setMessage("Please wait...");
            progressDialog.show();
        }

        @Override
        protected PayuHashes doInBackground(String... postParams) {
            PayuHashes payuHashes = new PayuHashes();
            try {

                //TODO Below url is just for testing purpose, merchant needs to replace this with their server side hash generation url
                // URL url = new URL("https://tsd.payu.in/GetHash");
                URL url = new URL("https://payu.herokuapp.com/get_hash");

                // get the payuConfig first
                String postParam = postParams[0];

                byte[] postParamsByte = postParam.getBytes("UTF-8");

                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                conn.setRequestProperty("Content-Length", String.valueOf(postParamsByte.length));
                conn.setDoOutput(true);
                conn.getOutputStream().write(postParamsByte);

                InputStream responseInputStream = conn.getInputStream();
                StringBuffer responseStringBuffer = new StringBuffer();
                byte[] byteContainer = new byte[1024];
                for (int i; (i = responseInputStream.read(byteContainer)) != -1; ) {
                    responseStringBuffer.append(new String(byteContainer, 0, i));
                }

                JSONObject response = new JSONObject(responseStringBuffer.toString());

                Iterator<String> payuHashIterator = response.keys();
                while (payuHashIterator.hasNext()) {
                    String key = payuHashIterator.next();
                    switch (key) {
                        //TODO Below three hashes are mandatory for payment flow and needs to be generated at merchant server
                        /**
                         * Payment hash is one of the mandatory hashes that needs to be generated from merchant's server side
                         * Below is formula for generating payment_hash -
                         *
                         * sha512(key|txnid|amount|productinfo|firstname|email|udf1|udf2|udf3|udf4|udf5||||||SALT)
                         *
                         */
                        case "payment_hash":
                            payuHashes.setPaymentHash(response.getString(key));
                            break;
                        /**
                         * vas_for_mobile_sdk_hash is one of the mandatory hashes that needs to be generated from merchant's server side
                         * Below is formula for generating vas_for_mobile_sdk_hash -
                         *
                         * sha512(key|command|var1|salt)
                         *
                         * here, var1 will be "default"
                         *
                         */
                        case "vas_for_mobile_sdk_hash":
                            payuHashes.setVasForMobileSdkHash(response.getString(key));
                            break;
                        /**
                         * payment_related_details_for_mobile_sdk_hash is one of the mandatory hashes that needs to be generated from merchant's server side
                         * Below is formula for generating payment_related_details_for_mobile_sdk_hash -
                         *
                         * sha512(key|command|var1|salt)
                         *
                         * here, var1 will be user credentials. If you are not using user_credentials then use "default"
                         *
                         */
                        case "payment_related_details_for_mobile_sdk_hash":
                            payuHashes.setPaymentRelatedDetailsForMobileSdkHash(response.getString(key));
                            break;

                        //TODO Below hashes only needs to be generated if you are using Store card feature
                        /**
                         * delete_user_card_hash is used while deleting a stored card.
                         * Below is formula for generating delete_user_card_hash -
                         *
                         * sha512(key|command|var1|salt)
                         *
                         * here, var1 will be user credentials. If you are not using user_credentials then use "default"
                         *
                         */
                        case "delete_user_card_hash":
                            payuHashes.setDeleteCardHash(response.getString(key));
                            break;
                        /**
                         * get_user_cards_hash is used while fetching all the cards corresponding to a user.
                         * Below is formula for generating get_user_cards_hash -
                         *
                         * sha512(key|command|var1|salt)
                         *
                         * here, var1 will be user credentials. If you are not using user_credentials then use "default"
                         *
                         */
                        case "get_user_cards_hash":
                            payuHashes.setStoredCardsHash(response.getString(key));
                            break;
                        /**
                         * edit_user_card_hash is used while editing details of existing stored card.
                         * Below is formula for generating edit_user_card_hash -
                         *
                         * sha512(key|command|var1|salt)
                         *
                         * here, var1 will be user credentials. If you are not using user_credentials then use "default"
                         *
                         */
                        case "edit_user_card_hash":
                            payuHashes.setEditCardHash(response.getString(key));
                            break;
                        /**
                         * save_user_card_hash is used while saving card to the vault
                         * Below is formula for generating save_user_card_hash -
                         *
                         * sha512(key|command|var1|salt)
                         *
                         * here, var1 will be user credentials. If you are not using user_credentials then use "default"
                         *
                         */
                        case "save_user_card_hash":
                            payuHashes.setSaveCardHash(response.getString(key));
                            break;

                        //TODO This hash needs to be generated if you are using any offer key
                        /**
                         * check_offer_status_hash is used while using check_offer_status api
                         * Below is formula for generating check_offer_status_hash -
                         *
                         * sha512(key|command|var1|salt)
                         *
                         * here, var1 will be Offer Key.
                         *
                         */
                        case "check_offer_status_hash":
                            payuHashes.setCheckOfferStatusHash(response.getString(key));
                            break;
                        default:
                            break;
                    }
                }

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (ProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return payuHashes;
        }

        @Override
        protected void onPostExecute(PayuHashes payuHashes) {
            super.onPostExecute(payuHashes);

            progressDialog.dismiss();
            launchSdkUI(payuHashes);
        }
    }

    /**
     * This method adds the Payuhashes and other required params to intent and launches the PayuBaseActivity.java
     *
     * @param payuHashes it contains all the hashes generated from merchant server
     */
    public void launchSdkUI(PayuHashes payuHashes) {

        Intent intent = new Intent(this, PayUBaseActivity.class);
        intent.putExtra(PayuConstants.PAYU_CONFIG, payuConfig);
        intent.putExtra(PayuConstants.PAYMENT_PARAMS, mPaymentParams);
        intent.putExtra(PayuConstants.PAYU_HASHES, payuHashes);

        startActivityForResult(intent,PayuConstants.PAYU_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, final Intent data) {
        if (requestCode == PayuConstants.PAYU_REQUEST_CODE) {
            if (data != null) {

                /**
                 * Here, data.getStringExtra("payu_response") ---> Implicit response sent by PayU
                 * data.getStringExtra("result") ---> Response received from merchant's Surl/Furl
                 *
                 * PayU sends the same response to merchant server and in app. In response check the value of key "status"
                 * for identifying status of transaction. There are two possible status like, success or failure
                 * */

                if(resultCode==RESULT_OK)
                {
                    emailDatabase.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {

                            Log.i("check", "======="+dataSnapshot.child("email").getValue());
                            Log.i("check", "======="+dataSnapshot.child("password").getValue());

                            sendEmail(dataSnapshot.child("email").getValue().toString(),dataSnapshot.child("password").getValue().toString());

                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }

                if(resultCode==RESULT_CANCELED) {

                    Log.i("fail", "fail");
                    Toast.makeText(this, "Payment Was Not Successful", Toast.LENGTH_LONG).show();

//                    emailDatabase.addValueEventListener(new ValueEventListener() {
//                        @Override
//                        public void onDataChange(DataSnapshot dataSnapshot) {
//
//                            Log.i("check", "======="+dataSnapshot.child("email").getValue());
//                            Log.i("check", "======="+dataSnapshot.child("password").getValue());
//
//                            sendEmail(dataSnapshot.child("email").getValue().toString(),dataSnapshot.child("password").getValue().toString());
//
//                        }
//
//                        @Override
//                        public void onCancelled(DatabaseError databaseError) {
//
//                        }
//                    });


                }

            }
            else {
                Toast.makeText(this, getString(R.string.could_not_receive_data), Toast.LENGTH_LONG).show();
            }
        }
    }

    protected void sendEmail(String email1, final String password1)
    {
        final String email=email1;
        final String password=password1;

        final String emailOfSender = mAuth.getCurrentUser().getEmail().toString();
        Log.i("mailofsender",emailOfSender);
        final String email3="Avruttilabs@gmail.com";
        final String subject = "Placement Of Order";
        message = "Customer Details";

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
                SendMail sm = new SendMail(CartActivity.this, email3, subject, message,email,password);

                //Executing sendmail to send email
                sm.execute();

                //Creating SendMail object
                String subject1="Thank you for shopping from Avrutti.You will receive the shipping details shortly";
                SendMail sm1 = new SendMail(CartActivity.this, emailOfSender,subject1 , message,email,password);

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
                message=message+"\n\nTotal price "+totalCost;

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        priceDatabase.removeValue();

//        //Creating SendMail object
//        SendMail sm = new SendMail(this, email, subject, message,email,password);
//
//        //Executing sendmail to send email
//        sm.execute();
    }


}