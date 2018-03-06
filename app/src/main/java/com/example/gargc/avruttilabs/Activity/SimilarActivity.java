package com.example.gargc.avruttilabs.Activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
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

import com.example.gargc.avruttilabs.Fragments.BasicComponentsFragment;
import com.example.gargc.avruttilabs.Model.Offer;
import com.example.gargc.avruttilabs.R;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.like.LikeButton;
import com.squareup.picasso.Picasso;

public class SimilarActivity extends AppCompatActivity {

    Offer offer;

    TextView category,subcategory;
    RecyclerView subcategoryRecyclerView;

    DatabaseReference mProductsDatabase;
    Query query;

    GridLayoutManager mLayoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_similar);

        offer=(Offer) getIntent().getSerializableExtra("model");

        category=(TextView) findViewById(R.id.app_bar_textview);
        subcategory=(TextView) findViewById(R.id.similar_subcategory);

        subcategoryRecyclerView=(RecyclerView) findViewById(R.id.similar_list);

        mProductsDatabase= FirebaseDatabase.getInstance().getReference().child("Products").child(offer.getCategory());
        query = mProductsDatabase.orderByChild("subcategory").equalTo(offer.getSubcategory());



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
