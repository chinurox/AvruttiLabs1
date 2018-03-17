package com.example.gargc.avruttilabs.Activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.gargc.avruttilabs.Model.Orders;
import com.example.gargc.avruttilabs.R;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

public class OrdersActivity extends AppCompatActivity
{

    private DatabaseReference ordersDatabase;
    private RecyclerView ordersList;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_orders);

        mAuth = FirebaseAuth.getInstance();
        ordersDatabase = FirebaseDatabase.getInstance().getReference().child("Orders").child(mAuth.getCurrentUser().getUid());
        ordersList = (RecyclerView)findViewById(R.id.orders_list);
        ordersList.setLayoutManager(new LinearLayoutManager(this));
        ordersList.setHasFixedSize(true);

    }

    @Override
    protected void onStart()
    {
        super.onStart();

        FirebaseRecyclerAdapter<Orders,OrdersViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Orders, OrdersViewHolder>
                (
                        Orders.class,
                        R.layout.order_single_item,
                        OrdersViewHolder.class,
                        ordersDatabase
                )
        {
            @Override
            protected void populateViewHolder(OrdersViewHolder viewHolder, Orders model, int position)
            {
                viewHolder.name.setText(model.getTitle());
                viewHolder.price.setText(model.getPrice());
                viewHolder.qty.setText(model.getQuantity());
                Picasso.with(OrdersActivity.this).load(model.getImage()).into(viewHolder.img);
            }
        };

        ordersList.setAdapter(firebaseRecyclerAdapter);
    }

    public static class OrdersViewHolder extends RecyclerView.ViewHolder
    {
        ImageView img;
        TextView name,price,qty;

        public OrdersViewHolder(View itemView)
        {
            super(itemView);

            img = (ImageView)itemView.findViewById(R.id.single_order_image);
            name = (TextView)itemView.findViewById(R.id.single_order_name);
            price = (TextView)itemView.findViewById(R.id.single_order_price);
            qty = (TextView)itemView.findViewById(R.id.single_order_quantity);
        }
    }

}
