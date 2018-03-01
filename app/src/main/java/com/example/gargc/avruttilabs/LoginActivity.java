package com.example.gargc.avruttilabs;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class LoginActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;


    EditText username,password;
    TextView tvSignUp;

    Button mLoginButton;
    ProgressDialog mLoginProgress;

    DatabaseReference mUserDatabase;

    //ProgressBar

    ProgressBar progressBar;

    TextView forgotPassword;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //firebase
        mAuth = FirebaseAuth.getInstance();
        mUserDatabase= FirebaseDatabase.getInstance().getReference().child("Users");

        username=(EditText) findViewById(R.id.etUsername);
        password=(EditText) findViewById(R.id.etPassword);

        tvSignUp=(TextView) findViewById(R.id.tvSignUp);

        


        mLoginProgress=new ProgressDialog(this);
        progressBar = (ProgressBar) findViewById(R.id.progress);


        mLoginButton=(Button) findViewById(R.id.btSignIn);

        mLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String email=username.getText().toString();
                String password1=password.getText().toString();

                if(!TextUtils.isEmpty(email)&&!TextUtils.isEmpty(password1))
                {
                    if (!email.matches("^(([^<>()\\[\\]\\\\.,;:\\s@\"]+(\\.[^<>()\\[\\]\\\\.,;:\\s@\"]+)*)|(\".+\"))@((\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}])|(([a-zA-Z\\-0-9]+\\.)+[a-zA-Z]{2,}))$")) {
                        Toast.makeText(LoginActivity.this, "Please enter a valid email id", Toast.LENGTH_LONG).show();
                        username.requestFocus();
                        toggleViews(true);
                    }
                    else {

                        toggleViews(false);
                        loginUser(email, password1);
                    }
                }
                else if(TextUtils.isEmpty(email))
                {
                    toggleViews(true);
                    Toast.makeText(LoginActivity.this, "Enter the email id", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    toggleViews(true);
                    Toast.makeText(LoginActivity.this, "Enter the password", Toast.LENGTH_SHORT).show();
                }


            }
        });

        tvSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent=new Intent(LoginActivity.this,SignUpActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }
        });

        forgotPassword=(TextView) findViewById(R.id.tvForgotPassword);
        forgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(LoginActivity.this,ResetPasswordActivity.class);
                //intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }
        });


    }

    private void toggleViews(boolean enabled) {
        progressBar.setVisibility((enabled) ? View.GONE : View.VISIBLE);
        mLoginButton.setVisibility((enabled) ? View.VISIBLE : View.INVISIBLE);
        username.setEnabled(enabled);
        password.setEnabled(enabled);


    }

    private void loginUser(String email, String password) {

        mAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful())
                {
                    //successfully logged in
                    toggleViews(true);

                    Intent mainIntent=new Intent(LoginActivity.this,MainActivity.class);
                    mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(mainIntent);
                    finish();

                }
                else
                {
                    toggleViews(true);
                    Toast.makeText(LoginActivity.this, "Cannot Sign In.. ", Toast.LENGTH_SHORT).show();

                }

            }
        });
    }
}
