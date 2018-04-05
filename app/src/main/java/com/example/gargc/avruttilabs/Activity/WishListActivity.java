package com.example.gargc.avruttilabs.Activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.gargc.avruttilabs.Model.WishListModel;
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
import com.squareup.picasso.Picasso;

import java.util.HashMap;

public class WishListActivity extends AppCompatActivity {

    private DatabaseReference wishListDatabase,cartDatabase;
    private RecyclerView wishList;
    private FirebaseAuth mAuth;
    String uid;

    private LinearLayout emptyLayout;
    private Button shopNow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wish_list);

        mAuth = FirebaseAuth.getInstance();
        uid = mAuth.getCurrentUser().getUid();
        wishListDatabase = FirebaseDatabase.getInstance().getReference().child("WishList").child(uid);
        cartDatabase = FirebaseDatabase.getInstance().getReference().child("Cart").child(uid);

        wishList=(RecyclerView) findViewById(R.id.wish_list);
        emptyLayout = (LinearLayout)findViewById(R.id.empty_container1);
        shopNow = (Button)findViewById(R.id.start_shop_btn);

        shopNow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent=new Intent(WishListActivity.this,MainActivity.class);
                startActivity(intent);
                finish();

            }
        });


        wishListDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                if(dataSnapshot.hasChildren())
                {
                    wishList.setVisibility(View.VISIBLE);
                    emptyLayout.setVisibility(View.GONE);
                }
                else
                {
                    wishList.setVisibility(View.GONE);
                    emptyLayout.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        wishList.setLayoutManager(new LinearLayoutManager(this));
        wishList.setHasFixedSize(true);


    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseRecyclerAdapter<WishListModel,WishViewHolder> firebaseRecyclerAdapter=new FirebaseRecyclerAdapter<WishListModel, WishViewHolder>(
                WishListModel.class,
                R.layout.wishlist_single_item,
                WishViewHolder.class,
                wishListDatabase
        ) {
            @Override
            protected void populateViewHolder(WishViewHolder viewHolder, final WishListModel model, int position) {

                Picasso.with(WishListActivity.this).load(model.getImage()).into(viewHolder.wishListImage);
                viewHolder.title.setText(model.getTitle());
                viewHolder.price.setText(model.getPrice());
                viewHolder.status.setText(model.getStatus());

                viewHolder.delete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        wishListDatabase.child(model.getTitle()).removeValue();
                        Toast.makeText(WishListActivity.this, "item removed", Toast.LENGTH_SHORT).show();
                    }
                });

                viewHolder.addCart.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view)
                    {
                        final ProgressDialog mProgress = new ProgressDialog(WishListActivity.this);

                        mProgress.setTitle("adding to cart...");
                        mProgress.setCanceledOnTouchOutside(false);
                        mProgress.show();

                        DatabaseReference userCartDB = cartDatabase.child(model.getTitle());
                        HashMap cartMap = new HashMap();
                        cartMap.put("title",model.getTitle());
                        cartMap.put("image",model.getImage());
                        cartMap.put("price",model.getPrice());
                        cartMap.put("status",model.getStatus());
                        cartMap.put("category",model.getCategory());
                        cartMap.put("quantity","1");
                        cartMap.put("subCategory",model.getSubCategory());
                        userCartDB.setValue(cartMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task)
                            {
                                if (task.isSuccessful())
                                {
                                    mProgress.dismiss();
                                    Toast.makeText(WishListActivity.this, "Item added to cart", Toast.LENGTH_SHORT).show();
                                }
                                else
                                {
                                    mProgress.dismiss();
                                    Toast.makeText(WishListActivity.this, "couldnot be added", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }
                });

            }
        };

        wishList.setAdapter(firebaseRecyclerAdapter);

    }

    public static class WishViewHolder extends RecyclerView.ViewHolder
    {
        TextView delete,title,price,status;
        ImageView wishListImage;
        Button addCart;

        public WishViewHolder(View itemView)
        {
            super(itemView);

            delete=(TextView) itemView.findViewById(R.id.Delete);
            title=(TextView) itemView.findViewById(R.id.single_wishlist_name);
            price=(TextView) itemView.findViewById(R.id.single_wishlist_price);
            status=(TextView) itemView.findViewById(R.id.single_wishlist_status);
            wishListImage=(ImageView) itemView.findViewById(R.id.single_wishlist_image);
            addCart = (Button) itemView.findViewById(R.id.add_cart);
        }

    }

}
