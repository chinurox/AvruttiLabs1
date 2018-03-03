package com.example.gargc.avruttilabs.Activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.gargc.avruttilabs.Model.CartModel;
import com.example.gargc.avruttilabs.R;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

public class CartActivity extends AppCompatActivity
{

    private DatabaseReference cartDatabase;
    private RecyclerView cartList;
    private FirebaseAuth mAuth;
    private String uid;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        cartList = (RecyclerView)findViewById(R.id.cart_list);

        mAuth = FirebaseAuth.getInstance();
        uid = mAuth.getCurrentUser().getUid();
        cartDatabase = FirebaseDatabase.getInstance().getReference().child("Cart").child(uid);

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
                Picasso.with(CartActivity.this).load(model.getImage()).into(viewHolder.img);
                viewHolder.title.setText(model.getTitle());
                viewHolder.price.setText(model.getPrice());
                viewHolder.status.setText(model.getStatus());

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
        TextView title,price,status;
        Button delete,buy;

        public CartViewHolder(View itemView)
        {
            super(itemView);

            img = (ImageView)itemView.findViewById(R.id.single_cart_image);
            title = (TextView)itemView.findViewById(R.id.single_cart_name);
            price = (TextView)itemView.findViewById(R.id.single_cart_price);
            status = (TextView)itemView.findViewById(R.id.single_cart_status);
            delete = (Button)itemView.findViewById(R.id.single_cart_btn_delete);
            buy = (Button)itemView.findViewById(R.id.single_cart_btn_buy);
        }

    }

}
