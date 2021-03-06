package com.example.gargc.avruttilabs.Fragments;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.gargc.avruttilabs.Activity.ItemDetailsActivity;
import com.example.gargc.avruttilabs.Activity.LoginActivity;
import com.example.gargc.avruttilabs.Model.Offer;
import com.example.gargc.avruttilabs.Model.SubCategory;
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

/**
 * A simple {@link Fragment} subclass.
 */
public class Sensors extends Fragment {

    RecyclerView subcategoryRecyclerView;
    RecyclerView itemListRecyclerView;
    DatabaseReference mDatabase,mProductsDatabase;
    Query query;

    Context mContext;

    String currentlyClickedButton="";

    View viewClicked;
    int positionClicked;

    private DatabaseReference wishListDatabase;
    private FirebaseAuth mAuth;
    private String uid;

    public Sensors() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view= inflater.inflate(R.layout.fragmentlayout, container, false);
        mContext=getContext();

        Log.i("offers","yu");

        subcategoryRecyclerView=(RecyclerView) view.findViewById(R.id.subcategory_content);
        itemListRecyclerView=(RecyclerView) view.findViewById(R.id.subcategory_item_layout);

        mDatabase = FirebaseDatabase.getInstance().getReference().child("Category").child("Sensors");
        mProductsDatabase=FirebaseDatabase.getInstance().getReference().child("Products").child("Sensors");

        LinearLayoutManager layoutManager= new LinearLayoutManager(getContext(),LinearLayoutManager.HORIZONTAL, false);
        subcategoryRecyclerView.setLayoutManager(layoutManager);
        subcategoryRecyclerView.addItemDecoration(new DividerItemDecoration(getContext(),DividerItemDecoration.VERTICAL));
        subcategoryRecyclerView.hasFixedSize();

        // for products setting layout manager
        GridLayoutManager gridLayoutManager=new GridLayoutManager(getContext(),2);
        itemListRecyclerView.setLayoutManager(gridLayoutManager);
        itemListRecyclerView.addItemDecoration(new DividerItemDecoration(getContext(),DividerItemDecoration.VERTICAL));
        itemListRecyclerView.setHasFixedSize(true);

        mAuth = FirebaseAuth.getInstance();

        if(mAuth.getCurrentUser()==null)
        {
            Intent startIntent=new Intent(getActivity(),LoginActivity.class);
            startActivity(startIntent);
        }
        else {
            uid = mAuth.getCurrentUser().getUid();
            wishListDatabase = FirebaseDatabase.getInstance().getReference().child("WishList").child(uid);
        }

        return view;

    }

    @Override
    public void onStart()
    {
        super.onStart();

        FirebaseRecyclerAdapter<SubCategory,BasicComponentsFragment.MyViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<SubCategory, BasicComponentsFragment.MyViewHolder>
                (
                        SubCategory.class,
                        R.layout.subcategorybtn,
                        BasicComponentsFragment.MyViewHolder.class,
                        mDatabase
                )
        {

            @Override
            protected void populateViewHolder(final BasicComponentsFragment.MyViewHolder viewHolder, SubCategory model, final int position)
            {

                Log.i("data",model.getName());
                viewHolder.btn.setText(model.getName());

                viewHolder.btn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view)
                    {
                        String subcat = viewHolder.btn.getText().toString();

                        query = mProductsDatabase.orderByChild("subcategory").equalTo(subcat);

                        positionClicked=position;
                        if(currentlyClickedButton.equals(""))
                        {
                            try {
                                Log.i("clicked", "firsttime");
                                currentlyClickedButton = subcat;
                                viewHolder.view.setVisibility(View.VISIBLE);
                                viewClicked = view;
                                setSubCategoryProducts();
                            }
                            catch(Exception e) {}
                        }
                        else if(currentlyClickedButton.equals(subcat))
                        {
                            try {
                                Log.i("clicked", "onsamebutton");
                                currentlyClickedButton = "";
                                BasicComponentsFragment.MyViewHolder myViewHolder = (BasicComponentsFragment.MyViewHolder) subcategoryRecyclerView.findContainingViewHolder(viewClicked);
                                myViewHolder.view.setVisibility(View.GONE);
                                viewClicked = null;
                                setProducts();
                            }
                            catch(Exception e) {}
                        }
                        else
                        {
                            try {
                                Log.i("clicked", "differentbutton");
                                currentlyClickedButton = subcat;
                                BasicComponentsFragment.MyViewHolder myViewHolder = (BasicComponentsFragment.MyViewHolder) subcategoryRecyclerView.findContainingViewHolder(viewClicked);
                                myViewHolder.view.setVisibility(View.GONE);
                                viewClicked = view;
                                viewHolder.view.setVisibility(View.VISIBLE);
                                setSubCategoryProducts();
                            }
                            catch(Exception e) {}
                        }

                    }
                });
            }
        };

        firebaseRecyclerAdapter.notifyDataSetChanged();
        subcategoryRecyclerView.setAdapter(firebaseRecyclerAdapter);

        // adapter for products
        setProducts();


    }

    private void setProducts() {

        FirebaseRecyclerAdapter<Offer,BasicComponentsFragment.MyProductHolder> firebaseRecyclerAdapter1=new FirebaseRecyclerAdapter<Offer,BasicComponentsFragment.MyProductHolder>(
                Offer.class,
                R.layout.list_item,
                BasicComponentsFragment.MyProductHolder.class,
                mProductsDatabase
        ) {
            @Override
            protected void populateViewHolder(final BasicComponentsFragment.MyProductHolder viewHolder, final Offer model, int position) {
                Log.i("product",model.toString());
                viewHolder.item.setText(model.getTitle());
                viewHolder.itemCost.setText(model.getPrice());
                viewHolder.stockDetails.setText(model.getStatus());
                Log.i("image",model.getImage());
                Picasso.with(getContext()).load(model.getImage()).placeholder(R.mipmap.image_not_available).into(viewHolder.imageView);

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

                // setting on click on item view
                viewHolder.view.setOnClickListener(new View.OnClickListener() {
                    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
                    @Override
                    public void onClick(View view) {

                        Intent intent=new Intent(getContext(),ItemDetailsActivity.class);
                        intent.putExtra("Item",model);
                        final View sharedView = viewHolder.imageView;
                        ActivityOptionsCompat activityOptionsCompat = ActivityOptionsCompat.makeSceneTransitionAnimation((Activity)mContext, sharedView, "newsPhotoTransitionFromMainActivityToReadNewsActivity");
                        mContext.startActivity(intent, activityOptionsCompat.toBundle());

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
                        Toast.makeText(getContext(), "item removed", Toast.LENGTH_SHORT).show();

                    }
                });

            }


        };
        firebaseRecyclerAdapter1.notifyDataSetChanged();
        itemListRecyclerView.setAdapter(firebaseRecyclerAdapter1);
    }

    private void setSubCategoryProducts()
    {
        FirebaseRecyclerAdapter<Offer,BasicComponentsFragment.MyProductHolder> firebaseRecyclerAdapter1=new FirebaseRecyclerAdapter<Offer,BasicComponentsFragment.MyProductHolder>(
                Offer.class,
                R.layout.list_item,
                BasicComponentsFragment.MyProductHolder.class,
                query
        ) {
            @Override
            protected void populateViewHolder(final BasicComponentsFragment.MyProductHolder viewHolder, final Offer model, int position) {
                Log.i("product",model.toString());
                viewHolder.item.setText(model.getTitle());
                viewHolder.itemCost.setText(model.getPrice());
                viewHolder.stockDetails.setText(model.getStatus());
                Log.i("image",model.getImage());
                Picasso.with(getContext()).load(model.getImage()).placeholder(R.mipmap.image_not_available).into(viewHolder.imageView);

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

                // setting on click on item view
                viewHolder.view.setOnClickListener(new View.OnClickListener() {
                    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
                    @Override
                    public void onClick(View view) {

                        Intent intent=new Intent(getContext(),ItemDetailsActivity.class);
                        intent.putExtra("Item",model);
                        final View sharedView = viewHolder.imageView;
                        ActivityOptionsCompat activityOptionsCompat = ActivityOptionsCompat.makeSceneTransitionAnimation((Activity)mContext, sharedView, "newsPhotoTransitionFromMainActivityToReadNewsActivity");
                        mContext.startActivity(intent, activityOptionsCompat.toBundle());

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
                        Toast.makeText(getContext(), "item removed", Toast.LENGTH_SHORT).show();

                    }
                });


            }

        };
        firebaseRecyclerAdapter1.notifyDataSetChanged();
        itemListRecyclerView.setAdapter(firebaseRecyclerAdapter1);

    }

    private void addToWishList(Offer offer) {
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
                    Toast.makeText(getContext(), "Item added to WishList", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    Toast.makeText(getContext(), "couldnot be added", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder
    {
        Button btn;

        public MyViewHolder(View itemView)
        {
            super(itemView);
            btn = (Button)itemView.findViewById(R.id.subcategorybutton);

            // setting on click in subcategory

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                }
            });
        }

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

    @Override
    public void onResume() {
        super.onResume();
        Log.i("resume","true");
        onStart();
    }


}

