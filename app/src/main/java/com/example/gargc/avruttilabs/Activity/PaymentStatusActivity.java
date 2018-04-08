package com.example.gargc.avruttilabs.Activity;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.gargc.avruttilabs.R;

public class PaymentStatusActivity extends AppCompatActivity
{

    private String txnId,id,status;
    private TextView tvStatus,tvTxnId,tvId;
    private Button btnOk;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment_status);

        Intent intent = getIntent();
        status = intent.getExtras().getString("status");
        txnId = intent.getExtras().getString("transaction_id");
        id = intent.getExtras().getString("id");

        tvStatus = (TextView)findViewById(R.id.status_txt_status);
        tvTxnId = (TextView)findViewById(R.id.status_txt_txnid);
        tvId = (TextView)findViewById(R.id.status_txt_id);
        btnOk = (Button)findViewById(R.id.status_btn_ok);

        if(status.equals("true"))
        {
            tvStatus.setText("Transaction Successful");
            tvTxnId.setText(txnId);
            tvId.setText(id);
        }
        else {
            tvStatus.setText("Transaction Was Not Successful");
        }

        btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent mainIntent = new Intent(PaymentStatusActivity.this, MainActivity.class);
                mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(mainIntent);
            }
        });

    }

}
