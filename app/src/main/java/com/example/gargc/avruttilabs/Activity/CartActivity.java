package com.example.gargc.avruttilabs.Activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.gargc.avruttilabs.Model.CartModel;
import com.example.gargc.avruttilabs.R;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

public class CartActivity extends AppCompatActivity
{

    private DatabaseReference cartDatabase;
    private RecyclerView cartList;
    private FirebaseAuth mAuth;
    private String uid;
    private LinearLayout paymentLayout , emptyLayout;
    private Button shopNow;

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

        mAuth = FirebaseAuth.getInstance();
        uid = mAuth.getCurrentUser().getUid();
        cartDatabase = FirebaseDatabase.getInstance().getReference().child("Cart").child(uid);
        Log.i("check",cartDatabase.child("price")+"");

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
                }
                else
                {
                    cartList.setVisibility(View.GONE);
                    paymentLayout.setVisibility(View.GONE);
                    emptyLayout.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        cartList.setLayoutManager(new LinearLayoutManager(this));
        cartList.setHasFixedSize(true);
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

        super.onStart();
    }

    public static class CartViewHolder extends RecyclerView.ViewHolder
    {
        ImageView img;
        TextView title,price,status,qty;
        LinearLayout delete,edit;

        public CartViewHolder(View itemView)
        {
            super(itemView);

            img = (ImageView)itemView.findViewById(R.id.single_cart_image);
            title = (TextView)itemView.findViewById(R.id.single_cart_name);
            price = (TextView)itemView.findViewById(R.id.single_cart_price);
            status = (TextView)itemView.findViewById(R.id.single_cart_status);
            qty = (TextView)itemView.findViewById(R.id.single_cart_quantity);
            delete = (LinearLayout) itemView.findViewById(R.id.single_cart_delete);
            edit = (LinearLayout) itemView.findViewById(R.id.single_cart_edit);
        }

    }

}
