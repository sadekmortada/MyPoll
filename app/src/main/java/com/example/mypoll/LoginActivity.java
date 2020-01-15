package com.example.mypoll;

import androidx.annotation.NonNull;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.nio.InvalidMarkException;
import java.sql.Struct;

public class LoginActivity extends AppCompatActivity {
    private String email,password;
    private FirebaseAuth firebaseAuth;
    private ProgressDialog progressDialog;
    private boolean visible=false;
    private EditText loginEmail,loginPassword;
    private Toolbar toolbar;
    private Button loginButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        initialize();
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                login();
            }
        });
    }

    @SuppressLint("ResourceAsColor")
    private void initialize() {
        loginEmail=findViewById(R.id.login_email);
        loginPassword=findViewById(R.id.login_password);
        toolbar=findViewById(R.id.login_toolbar);
        toolbar.setTitle("Let's Sign In First!");
        setSupportActionBar(toolbar);
        firebaseAuth=FirebaseAuth.getInstance();
        progressDialog=new ProgressDialog(this);
        progressDialog.setMessage("Please wait...");
        progressDialog.setCanceledOnTouchOutside(false);
        loginButton=findViewById(R.id.loginButton);
    }

    public void show(View view) {
        if(!visible){
            view.setBackground(null);
            loginPassword.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            visible=true;
        }
    }


    public void login() {
        progressDialog.show();
        email=loginEmail.getText().toString();
        password=loginPassword.getText().toString();
        if(email.equals("") || password.equals((""))){
            progressDialog.dismiss();
            AlertDialog.Builder builder = new AlertDialog.Builder(this).setIcon(android.R.drawable.ic_dialog_alert).setTitle("Please fill the fields before submitting")
                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) { }
                    });
            builder.show();
        }
        firebaseAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                progressDialog.dismiss();
                if(task.isSuccessful()){
                    finish();
                }
                else{
                    TextView alert=new TextView(getApplicationContext());
                    alert.setText("1. Check email and password.\n\n2. Check internet connection");
                    alert.setPadding(10,10,10,10);
                    AlertDialog.Builder builder = new AlertDialog.Builder(getApplicationContext()).setIcon(android.R.drawable.ic_dialog_alert).setTitle("Login failed! Please try:")
                            .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) { }
                            }).setView(alert);
                    builder.show();
                }
            }
        });
    }

    public void toRegister(View view) {
        startActivity(new Intent(this,RegisterActivity.class));
    }
    @Override
    public void onBackPressed() {
    }
}
