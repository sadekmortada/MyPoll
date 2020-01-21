package com.example.mypoll;

import androidx.annotation.NonNull;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;

import java.nio.InvalidMarkException;
import java.sql.Struct;

public class LoginActivity extends AppCompatActivity {
    private String email,password;
    private FirebaseAuth firebaseAuth;
    private ProgressDialog progressDialog;
    private EditText loginEmail,loginPassword;
    private AlertDialog.Builder builder;
    private SharedPreferences sharedPreferences;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        initialize();
    }

    @SuppressLint("ResourceAsColor")
    private void initialize() {
        loginEmail=findViewById(R.id.login_email);
        loginPassword=findViewById(R.id.login_password);
        firebaseAuth=FirebaseAuth.getInstance();
        progressDialog=new ProgressDialog(this);
        progressDialog.setMessage("Please wait...");
        progressDialog.setCancelable(false);
        builder = new AlertDialog.Builder(this);
        builder.setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) { }
                });
        sharedPreferences=getSharedPreferences("user",MODE_PRIVATE);
    }

    public void show(View view) {
            view.setBackground(null);
            view.setClickable(false);
            loginPassword.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
    }


    public void login(View view) {
        ConnectivityManager connectivityManager = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        if(!(connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
                connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED)){
            builder.setIcon(R.drawable.nowifismall).setTitle("No Internet Connection").show();
            return;
        }
        progressDialog.show();
        email=loginEmail.getText().toString();
        password=loginPassword.getText().toString();
        if(email.equals("") || password.equals((""))){
            progressDialog.dismiss();
            builder.setTitle("Please fill the fields before submitting")
                    .show();
            return;
        }
        firebaseAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                progressDialog.dismiss();
                if(task.isSuccessful()) {
                    FirebaseUser firebaseUser=firebaseAuth.getCurrentUser();
                    CurrentFragment.reset();
                    FirebaseDatabase.getInstance().getReference("users").child(firebaseUser.getUid()).child("phone_token").setValue(FirebaseInstanceId.getInstance().getToken());
                    FirebaseDatabase.getInstance().getReference("users").child(firebaseUser.getUid()).child("name").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            sharedPreferences.edit().putString("user_name", dataSnapshot.getValue().toString()).apply();
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) { }
                    });

                    finish();
                }
                else
                    Toast.makeText(getApplicationContext(),"Wrong email/password",Toast.LENGTH_LONG).show();
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
