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

import com.example.gargc.avruttilabs.Activity.ItemDetailsActivity;
import com.example.gargc.avruttilabs.Model.Offer;
import com.example.gargc.avruttilabs.Model.SubCategory;
import com.example.gargc.avruttilabs.R;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.like.LikeButton;
import com.squareup.picasso.Picasso;

import com.example.gargc.avruttilabs.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class Robotics extends Fragment
{

    RecyclerView subcategoryRecyclerView;
    RecyclerView itemListRecyclerView;
    DatabaseReference mDatabase,mProductsDatabase;
    Query query;

    Context mContext;

    String currentlyClickedButton="";

    View viewClicked;
    int positionClicked;

    public Robotics() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view= inflater.inflate(R.layout.fragmentlayout, container, false);
        mContext=getContext();

        Log.i("offers","yu");

        subcategoryRecyclerView=(RecyclerView) view.findViewById(R.id.subcategory_content);
        itemListRecyclerView=(RecyclerView) view.findViewById(R.id.subcategory_item_layout);

        mDatabase = FirebaseDatabase.getInstance().getReference().child("Category").child("Robotics");
        mProductsDatabase=FirebaseDatabase.getInstance().getReference().child("Products").child("Robotics");

        LinearLayoutManager layoutManager= new LinearLayoutManager(getContext(),LinearLayoutManager.HORIZONTAL, false);
        subcategoryRecyclerView.setLayoutManager(layoutManager);
        subcategoryRecyclerView.addItemDecoration(new DividerItemDecoration(getContext(),DividerItemDecoration.VERTICAL));
        subcategoryRecyclerView.hasFixedSize();

        // for products setting layout manager
        GridLayoutManager gridLayoutManager=new GridLayoutManager(getContext(),2);
        itemListRecyclerView.setLayoutManager(gridLayoutManager);
        itemListRecyclerView.addItemDecoration(new DividerItemDecoration(getContext(),DividerItemDecoration.VERTICAL));
        itemListRecyclerView.setHasFixedSize(true);

        if(!isNetworkAvailable())
        {
            showDialog();
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
                            Log.i("clicked","firsttime");
                            currentlyClickedButton=subcat;
                            viewHolder.view.setVisibility(View.VISIBLE);
                            viewClicked=view;
                            setSubCategoryProducts();
                        }
                        else if(currentlyClickedButton.equals(subcat))
                        {
                            Log.i("clicked","onsamebutton");
                            currentlyClickedButton="";
                            BasicComponentsFragment.MyViewHolder myViewHolder=(BasicComponentsFragment.MyViewHolder)subcategoryRecyclerView.findContainingViewHolder(viewClicked);
                            myViewHolder.view.setVisibility(View.GONE);
                            viewClicked=null;
                            setProducts();

                        }
                        else
                        {
                            Log.i("clicked","differentbutton");
                            currentlyClickedButton=subcat;
                            BasicComponentsFragment.MyViewHolder myViewHolder=(BasicComponentsFragment.MyViewHolder) subcategoryRecyclerView.findContainingViewHolder(viewClicked);
                            myViewHolder.view.setVisibility(View.GONE);
                            viewClicked=view;
                            viewHolder.view.setVisibility(View.VISIBLE);
                            setSubCategoryProducts();
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

    public boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public void showDialog()
    {
        final AlertDialog alertDialog = new AlertDialog.Builder(getContext()).create();

        alertDialog.setTitle("No Internet!!");
        alertDialog.setMessage("Please chek you internet connection");
        alertDialog.setButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                alertDialog.dismiss();
            }
        });

        // Showing Alert Message
        alertDialog.show();
    }


}
