package com.example.gargc.avruttilabs.Activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.gargc.avruttilabs.Fragments.BasicComponentsFragment;
import com.example.gargc.avruttilabs.Model.Offer;
import com.example.gargc.avruttilabs.R;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
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

public class SimilarActivity extends AppCompatActivity {

    private Offer offer;

    private TextView category,subcategory;
    private RecyclerView subcategoryRecyclerView;

    private DatabaseReference mProductsDatabase,wishListDatabase;
    private Query query;
    private FirebaseAuth mAuth;
    private String uid;

    private GridLayoutManager mLayoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_similar);

        offer=(Offer) getIntent().getSerializableExtra("model");

        category=(TextView) findViewById(R.id.app_bar_textview);
        subcategory=(TextView) findViewById(R.id.similar_subcategory);

        subcategoryRecyclerView=(RecyclerView) findViewById(R.id.similar_list);


        mAuth = FirebaseAuth.getInstance();
        uid = mAuth.getCurrentUser().getUid();
        mProductsDatabase= FirebaseDatabase.getInstance().getReference().child("Products").child(offer.getCategory());
        query = mProductsDatabase.orderByChild("subcategory").equalTo(offer.getSubcategory());
        wishListDatabase = FirebaseDatabase.getInstance().getReference().child("WishList").child(uid);

        category.setText(offer.getCategory());
        subcategory.setText("Subcategory"+"-->"+offer.getSubcategory());

        mLayoutManager = new GridLayoutManager(SimilarActivity.this,2);
        subcategoryRecyclerView.setLayoutManager(mLayoutManager);
        subcategoryRecyclerView.addItemDecoration(new DividerItemDecoration(SimilarActivity.this,DividerItemDecoration.VERTICAL));
        subcategoryRecyclerView.hasFixedSize();

    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseRecyclerAdapter<Offer,MyProductHolder> firebaseRecyclerAdapter1=new FirebaseRecyclerAdapter<Offer,MyProductHolder>(
                Offer.class,
                R.layout.list_item,
                MyProductHolder.class,
                query
        ) {
            @Override
            protected void populateViewHolder(final MyProductHolder viewHolder, final Offer model, int position) {
                Log.i("product",model.toString());
                viewHolder.item.setText(model.getTitle());
                viewHolder.itemCost.setText(model.getPrice());
                viewHolder.stockDetails.setText(model.getStatus());
                Log.i("image",model.getImage());
                Picasso.with(SimilarActivity.this).load(model.getImage()).placeholder(R.mipmap.image_not_available).into(viewHolder.imageView);

                wishListDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Log.i("datasnapshot",dataSnapshot.toString());
                        if(dataSnapshot.child(model.getTitle()).exists()) {
                            viewHolder.likeButton.setLiked(true);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

                viewHolder.likeButton.setOnLikeListener(new OnLikeListener() {
                    @Override
                    public void liked(LikeButton likeButton) {
                        addToWishList(model);

                    }

                    @Override
                    public void unLiked(LikeButton likeButton) {

                        wishListDatabase.child(model.getTitle()).removeValue();
                        Toast.makeText(SimilarActivity.this, "item removed", Toast.LENGTH_SHORT).show();

                    }
                });

                // setting on click on item view
                viewHolder.view.setOnClickListener(new View.OnClickListener() {
                    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
                    @Override
                    public void onClick(View view) {

                        Intent intent=new Intent(SimilarActivity.this,ItemDetailsActivity.class);
                        intent.putExtra("Item",model);
                        final View sharedView = viewHolder.imageView;
                        ActivityOptionsCompat activityOptionsCompat = ActivityOptionsCompat.makeSceneTransitionAnimation(SimilarActivity.this, sharedView, "newsPhotoTransitionFromMainActivityToReadNewsActivity");
                        startActivity(intent, activityOptionsCompat.toBundle());

                    }
                });

            }

        };
        firebaseRecyclerAdapter1.notifyDataSetChanged();
        subcategoryRecyclerView.setAdapter(firebaseRecyclerAdapter1);

    }

    private void addToWishList(Offer offer)
    {
        DatabaseReference userCartDB = wishListDatabase.child(offer.getTitle());
        HashMap cartMap = new HashMap();
        cartMap.put("title",offer.getTitle());
        cartMap.put("image",offer.getImage());
        cartMap.put("price",offer.getPrice());
        cartMap.put("status",offer.getStatus());
        cartMap.put("category",offer.getCategory());
        cartMap.put("subCategory",offer.getSubcategory());
        userCartDB.setValue(cartMap).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task)
            {
                if (task.isSuccessful())
                {
                    Toast.makeText(SimilarActivity.this, "Item added to WishList", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    Toast.makeText(SimilarActivity.this, "couldnot be added", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }


    public static class MyProductHolder extends RecyclerView.ViewHolder{
        ImageView imageView;
        TextView item,stockDetails,itemCost;
        LikeButton likeButton;
        View view;

        public MyProductHolder(View itemView) {
            super(itemView);
            this.view=itemView;

            imageView=(ImageView) itemView.findViewById(R.id.image1);
            item=(TextView) itemView.findViewById(R.id.item);
            stockDetails=(TextView) itemView.findViewById(R.id.stockdetails);
            itemCost=(TextView) itemView.findViewById(R.id.itemcost);
            likeButton=(LikeButton) itemView.findViewById(R.id.likebutton);

        }
    }
}
