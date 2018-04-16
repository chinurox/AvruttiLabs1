package com.example.gargc.avruttilabs.Fragments;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Toast;

import com.example.gargc.avruttilabs.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class ContactUs extends Fragment {

    private DatePicker datePicker;
    private EditText edtId;
    private Button btnSubmit;

    public ContactUs() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_contact_us, container, false);

        datePicker = (DatePicker)view.findViewById(R.id.contact_date);
        edtId = (EditText)view.findViewById(R.id.contact_order_id);
        btnSubmit = (Button)view.findViewById(R.id.contact_submit);

        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendMail();
            }
        });

        return view;
    }

    private void sendMail()
    {

        String id = edtId.getText().toString();
        int day = datePicker.getDayOfMonth();
        int month = datePicker.getMonth() + 1;
        int year = datePicker.getYear();
        String date = day+"/"+month+"/"+year;

        if(!TextUtils.isEmpty(id)&&!TextUtils.isEmpty(date))
        {
            Intent i = new Intent(Intent.ACTION_SEND);
            i.setType("message/rfc822");
            i.putExtra(Intent.EXTRA_EMAIL  , new String[]{"avruttielectronics@gmail.com"});
            i.putExtra(Intent.EXTRA_SUBJECT, "subject of email");
            i.putExtra(Intent.EXTRA_TEXT   , "order id : "+id+" date : "+date);
            try {
                startActivity(Intent.createChooser(i, "Send mail..."));
            } catch (android.content.ActivityNotFoundException ex) {
                Toast.makeText(getContext(), "There are no email clients installed.", Toast.LENGTH_SHORT).show();
            }
        }

    }


}
