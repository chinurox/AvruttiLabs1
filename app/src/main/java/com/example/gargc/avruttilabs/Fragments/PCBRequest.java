package com.example.gargc.avruttilabs.Fragments;


import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.gargc.avruttilabs.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;

import static android.app.Activity.RESULT_OK;

/**
 * A simple {@link Fragment} subclass.
 */
public class PCBRequest extends Fragment
{

    private EditText edtName,edtQty;
    private Button btnSubmit,btnFile;
    private Uri fileUri = null,path=null;
    private DatabaseReference mDatabase;
    private StorageReference mStorage;
    private FirebaseAuth mAuth;
    private String uid;

    public PCBRequest() {}


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_pcbrequest, container, false);

        edtName = (EditText)view.findViewById(R.id.pcb_name);
        edtQty = (EditText)view.findViewById(R.id.pcb_qty);
        btnFile = (Button)view.findViewById(R.id.pcb_upload_file);
        btnSubmit = (Button)view.findViewById(R.id.pcb_submit);

        mAuth = FirebaseAuth.getInstance();
        uid = mAuth.getCurrentUser().getUid();
        mDatabase = FirebaseDatabase.getInstance().getReference().child("PCBRequest").child(uid);
        mStorage = FirebaseStorage.getInstance().getReference();

        btnFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("file/*");
                startActivityForResult(intent,1);
            }
        });

        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                sendEmail();
            }
        });

        return  view;
    }

    private void sendEmail()
    {
        final String name = edtName.getText().toString();
        final String quantity = edtQty.getText().toString();

        if (!TextUtils.isEmpty(name) && !TextUtils.isEmpty(quantity) && fileUri != null)
        {
            Intent emailIntent = new Intent(Intent.ACTION_SEND);
            // set the type to 'email'
            emailIntent.setType("vnd.android.cursor.dir/email");
            String to[] = {"avruttielectronics@gmail.com"};
            emailIntent.putExtra(Intent.EXTRA_EMAIL, to);
            // the attachment
            emailIntent.putExtra(Intent.EXTRA_STREAM, fileUri);
            //the mail body
            emailIntent.putExtra(Intent.EXTRA_TEXT, "name :"+name+" quantity :"+quantity);
            // the mail subject
            emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Subject");
            try {
                startActivity(Intent.createChooser(emailIntent, "Choose Gmail only..."));
            } catch (android.content.ActivityNotFoundException ex) {
                Toast.makeText(getContext(), "There are no email clients installed.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void upload()
    {
        final String name = edtName.getText().toString();
        final String quantity = edtQty.getText().toString();

        if(!TextUtils.isEmpty(name)&&!TextUtils.isEmpty(quantity)&&fileUri!=null)
        {
            StorageReference filepath = mStorage.child("Requests").child(fileUri.getLastPathSegment());
            filepath.putFile(fileUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot)
                {

                    @SuppressWarnings("VisibleForTests") String url = taskSnapshot.getDownloadUrl().toString();

                    DatabaseReference userRequest = mDatabase.push();
                    HashMap requestMap = new HashMap();
                    requestMap.put("name",name);
                    requestMap.put("quantity",quantity);
                    requestMap.put("file",url);
                    userRequest.setValue(requestMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()) {
                                Toast.makeText(getContext(), "Request Sent", Toast.LENGTH_SHORT).show();
                            }
                            else {
                                Toast.makeText(getContext(), "Could not be sent", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            });
        }
        else
        {
            Toast.makeText(getContext(), "Please enter all the fields", Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode==1 && resultCode==RESULT_OK)
        {
            fileUri = data.getData();
        }
    }

}
