package com.example.gargc.avruttilabs.Activity;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;

import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.gargc.avruttilabs.Fragments.BasicComponentsFragment;
import com.example.gargc.avruttilabs.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {


    public static final String MY_PREFS_NAME = "MyINTRODUCTION";
    public static final String INTRODUCTION = "INTRODUCTION";
    private static final String TAG = "MainActivity";

    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle actionBarDrawerToggle;
    private NavigationView navigationView;

    //Firebase Setup
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    private ActionBar actionBar;
    private Toolbar mToolbar;
    private ViewPager mPager;
    private MainPageAdapter mPageAdapter;
    private TabLayout mTabLayout;
    private RelativeLayout noConLayout;
    private TextView tvNoNet,userName;
    private ImageView imgNoNet;
    private Button btnNoNet;
    private SearchView searchView;
    private Menu menu;
    private DatabaseReference userDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();

        if (mAuth.getCurrentUser() == null) {
            sendToStart();
        }

        else
        {
            userDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(mAuth.getCurrentUser().getUid());
            noConLayout = (RelativeLayout) findViewById(R.id.no_network_container);
            tvNoNet = (TextView) findViewById(R.id.no_connection_tv);
            imgNoNet = (ImageView) findViewById(R.id.no_connection_img);
            btnNoNet = (Button) findViewById(R.id.retry_icon);

            mToolbar = (Toolbar) findViewById(R.id.main_toolbar);
            setSupportActionBar(mToolbar);
            actionBar = getSupportActionBar();
            actionBar.setTitle("");

            mPager = (ViewPager) findViewById(R.id.main_pager);
            mPageAdapter = new MainPageAdapter(getSupportFragmentManager());
            mPager.setAdapter(mPageAdapter);

            mTabLayout = (TabLayout) findViewById(R.id.main_tabs);
            mTabLayout.setupWithViewPager(mPager);

            navigationView = (NavigationView) findViewById(R.id.nav_view);
            drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

            actionBarDrawerToggle = new ActionBarDrawerToggle(MainActivity.this, drawerLayout, mToolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
            drawerLayout.setDrawerListener(actionBarDrawerToggle);
            actionBarDrawerToggle.syncState();

            navigationView.setNavigationItemSelectedListener(this);
            View headerView = navigationView.getHeaderView(0);
            userName = (TextView) headerView.findViewById(R.id.nav_item_name);

            userDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Log.i("datasnapshot",dataSnapshot.toString());
                    userName.setText(dataSnapshot.child("username").getValue().toString());
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

            if (!isNetworkAvailable()) {
                tvNoNet.setVisibility(View.VISIBLE);
                imgNoNet.setVisibility(View.VISIBLE);
                btnNoNet.setVisibility(View.VISIBLE);
                noConLayout.setVisibility(View.VISIBLE);

                noConLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        tvNoNet.setVisibility(View.GONE);
                        imgNoNet.setVisibility(View.GONE);
                        btnNoNet.setVisibility(View.GONE);
                        noConLayout.setVisibility(View.GONE);
                        startActivity(new Intent(MainActivity.this, MainActivity.class));
                    }
                });
            }

        }

    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);

        this.menu = menu;

        searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (!TextUtils.isEmpty(query)) {
                    Intent searchIntent = new Intent(MainActivity.this, SearchActivity.class);
                    Log.i("query", query + "");
                    searchIntent.putExtra("productName", "" + query);
                    startActivity(searchIntent);
                }
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_cart:
                startActivity(new Intent(MainActivity.this, CartActivity.class));
                break;

        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onStart() {
        super.onStart();
        //mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        /*if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }*/
    }


    private void sendToStart() {
        Intent startIntent = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(startIntent);
        finish();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {

            case R.id.my_cart:
                startActivity(new Intent(MainActivity.this, CartActivity.class));
                break;

            case R.id.logout_btn:
                mAuth.signOut();
                Intent loginIntent = new Intent(MainActivity.this, LoginActivity.class);
                loginIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(loginIntent);
                finish();
                break;

            case R.id.my_wishlist:
                startActivity(new Intent(MainActivity.this, WishListActivity.class));
                break;

            case R.id.my_orders :
                startActivity(new Intent(MainActivity.this, OrdersActivity.class));
                break;

            case R.id.contact_us : startActivity(new Intent(MainActivity.this, ContactUsActivity.class));
                break;

            case R.id.terms_conditions : startActivity(new Intent(MainActivity.this, TermsAndConditionsActivity.class));
                break;


        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    public boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }




}
