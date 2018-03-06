package com.example.gargc.avruttilabs.Activity;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.gargc.avruttilabs.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.like.LikeButton;
import com.like.OnLikeListener;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

public class SearchActivity extends AppCompatActivity
{

    private ImageView imageView;
    private TextView item,stockDetails,itemCost;
    private LikeButton likeButton;
    private String productName;
    private DatabaseReference searchDatabase,wishListDatabase;
    private TextView text;
    private CardView cardView;
    private FirebaseAuth mAuth;
    private String uid;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        productName = getIntent().getExtras().getString("productName");
        Log.i("passed",productName+"");

        imageView=(ImageView)findViewById(R.id.search_image);
        item=(TextView)findViewById(R.id.search_title);
        stockDetails=(TextView)findViewById(R.id.search_status);
        itemCost=(TextView)findViewById(R.id.search_cost);
        likeButton=(LikeButton)findViewById(R.id.search_likebutton);
        text=(TextView)findViewById(R.id.search_text);
        cardView=(CardView)findViewById(R.id.search_cardview);

        text.setText("Showing results for "+productName);

        mAuth = FirebaseAuth.getInstance();
        uid = mAuth.getCurrentUser().getUid();
        wishListDatabase = FirebaseDatabase.getInstance().getReference().child("WishList").child(uid);

        searchDatabase = FirebaseDatabase.getInstance().getReference().child("Products");

        searchDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                for (DataSnapshot snapshot : dataSnapshot.getChildren())
                {
                    Log.i("snapshot",snapshot.toString());

                    if(snapshot.child(productName).exists())
                    {
                        imageView.setVisibility(View.VISIBLE);
                        item.setVisibility(View.VISIBLE);
                        itemCost.setVisibility(View.VISIBLE);
                        likeButton.setVisibility(View.VISIBLE);
                        stockDetails.setVisibility(View.VISIBLE);

                        Log.i("product found",snapshot.toString());

                        final DataSnapshot product =  snapshot.child(productName);
                        item.setText(product.child("title").getValue().toString());
                        itemCost.setText(product.child("price").getValue().toString());
                        Picasso.with(SearchActivity.this).load(product.child("image").getValue().toString()).into(imageView);
                        stockDetails.setText(product.child("status").getValue().toString());

                        //setting the heart icon
                        wishListDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                Log.i("datasnapshot",dataSnapshot.toString());
                                if(dataSnapshot.child(productName).exists()) {
                                    likeButton.setLiked(true);
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });


                        likeButton.setOnLikeListener(new OnLikeListener() {
                            @Override
                            public void liked(LikeButton likeButton) {
                                addToWishList(product);

                            }

                            @Override
                            public void unLiked(LikeButton likeButton) {
                                wishListDatabase.child(productName).removeValue();
                                Toast.makeText(SearchActivity.this, "item removed", Toast.LENGTH_SHORT).show();

                            }
                        });
                    }

                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private void addToWishList(DataSnapshot productName)
    {
        DatabaseReference userCartDB = wishListDatabase.child(productName.child("title").getValue().toString());
        HashMap cartMap = new HashMap();
        cartMap.put("title",productName.child("title").getValue().toString());
        cartMap.put("image",productName.child("image").getValue().toString());
        cartMap.put("price",productName.child("price").getValue().toString());
        cartMap.put("status",productName.child("status").getValue().toString());
        cartMap.put("category",productName.child("category").getValue().toString());
        cartMap.put("subCategory",productName.child("subcategory").getValue().toString());
        userCartDB.setValue(cartMap).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task)
            {
                if (task.isSuccessful())
                {
                    Toast.makeText(SearchActivity.this, "Item added to WishList", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    Toast.makeText(SearchActivity.this, "couldnot be added", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

}
