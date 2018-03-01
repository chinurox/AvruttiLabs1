package com.example.gargc.avruttilabs.Fragments;


import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.app.Fragment;
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

import com.example.gargc.avruttilabs.Model.Offer;
import com.example.gargc.avruttilabs.Model.SubCategory;
import com.example.gargc.avruttilabs.R;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.like.LikeButton;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 */


public class Offers extends Fragment {

    RecyclerView subcategoryRecyclerView;
    RecyclerView itemListRecyclerView;
    ArrayList<String> subCategoryName;
    DatabaseReference mDatabase,mProductsDatabase;

    public Offers() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view= inflater.inflate(R.layout.fragmentlayout, container, false);

        Log.i("offers","yu");

        subcategoryRecyclerView=(RecyclerView) view.findViewById(R.id.subcategory_content);
        itemListRecyclerView=(RecyclerView) view.findViewById(R.id.subcategory_item_layout);

        mDatabase = FirebaseDatabase.getInstance().getReference().child("Category").child("category 1");
        mProductsDatabase=FirebaseDatabase.getInstance().getReference().child("Products").child("category 1");

        LinearLayoutManager layoutManager= new LinearLayoutManager(getContext(),LinearLayoutManager.HORIZONTAL, false);
        subcategoryRecyclerView.setLayoutManager(layoutManager);
        subcategoryRecyclerView.hasFixedSize();

        // for products setting layout manager
        GridLayoutManager gridLayoutManager=new GridLayoutManager(getContext(),2);
        itemListRecyclerView.setLayoutManager(gridLayoutManager);
        itemListRecyclerView.setHasFixedSize(true);



        return view;

    }

    @Override
    public void onStart()
    {
        super.onStart();

        FirebaseRecyclerAdapter<SubCategory,MyViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<SubCategory, MyViewHolder>
                (
                    SubCategory.class,
                    R.layout.subcategorybtn,
                    MyViewHolder.class,
                    mDatabase
                )
        {

            @Override
            protected void populateViewHolder(MyViewHolder viewHolder, SubCategory model, int position)
            {

                Log.i("data",model.getName());
                viewHolder.btn.setText(model.getName());
            }
        };

        firebaseRecyclerAdapter.notifyDataSetChanged();
        subcategoryRecyclerView.setAdapter(firebaseRecyclerAdapter);

        // adapter for products

        FirebaseRecyclerAdapter<Offer,MyProductHolder> firebaseRecyclerAdapter1=new FirebaseRecyclerAdapter<Offer,MyProductHolder>(
                Offer.class,
                R.layout.list_item,
                MyProductHolder.class,
                mProductsDatabase
        ) {
            @Override
            protected void populateViewHolder(MyProductHolder viewHolder, Offer model, int position) {
                Log.i("product",model.toString());
                viewHolder.item.setText(model.getTitle());
                viewHolder.itemCost.setText(model.getPrice());
                viewHolder.stockDetails.setText(model.getStatus());
                Log.i("image",model.getImage());
                Picasso.with(getContext()).load(model.getImage()).placeholder(R.mipmap.image_not_available).into(viewHolder.imageView);


            }


        };
        firebaseRecyclerAdapter1.notifyDataSetChanged();
        itemListRecyclerView.setAdapter(firebaseRecyclerAdapter1);
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder
    {
        Button btn;

        public MyViewHolder(View itemView)
        {
            super(itemView);

            btn = (Button)itemView.findViewById(R.id.subcategorybutton);
        }

    }

    public static class MyProductHolder extends RecyclerView.ViewHolder{
        ImageView imageView;
        TextView item,stockDetails,itemCost;
        LikeButton likeButton;

        public MyProductHolder(View itemView) {
            super(itemView);

            imageView=(ImageView) itemView.findViewById(R.id.image1);
            item=(TextView) itemView.findViewById(R.id.item);
            stockDetails=(TextView) itemView.findViewById(R.id.stockdetails);
            itemCost=(TextView) itemView.findViewById(R.id.itemcost);
            likeButton=(LikeButton) itemView.findViewById(R.id.likebutton);
        }
    }

}
