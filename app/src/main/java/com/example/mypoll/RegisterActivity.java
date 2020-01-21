package com.example.mypoll;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class RegisterActivity extends AppCompatActivity {
    private ProgressDialog progressDialog;
    private AlertDialog.Builder builder;
    private String email,password,confirmPassword,username;
    private EditText registerEmail,registerPassword,registerConfirmPassword,registerName;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        initialize();
    }

    private void initialize(){
        registerEmail=findViewById(R.id.register_email);
        registerPassword=findViewById(R.id.register_password);
        registerConfirmPassword=findViewById(R.id.confirm_password);
        registerName=findViewById(R.id.register_name);
        firebaseAuth=FirebaseAuth.getInstance();
        databaseReference=FirebaseDatabase.getInstance().getReference("users");
        progressDialog=new ProgressDialog(this);
        progressDialog.setTitle("Please wait...");
        progressDialog.setCancelable(false);
        builder = new AlertDialog.Builder(this);
        builder.setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) { }
                });
    }

    public void toLogin(View view) {
        finish();
    }

    public void register(View view) {
        ConnectivityManager connectivityManager = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        if(!(connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
                connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED)){
            builder.setIcon(R.drawable.nowifismall).setTitle("No Internet Connection").show();
            return;
        }
        progressDialog.show();
        username=registerName.getText().toString();
        email=registerEmail.getText().toString();
        password=registerPassword.getText().toString();
        confirmPassword=registerConfirmPassword.getText().toString();
        if(email.equals("")|| password.equals("") || confirmPassword.equals("")){
            progressDialog.dismiss();
            builder.setTitle("Please fill the fields before submitting").show();
            return;
        }
        if( !confirmPassword.equals(password)){
            progressDialog.dismiss();
            builder.setTitle("Passwords does not match").show();
            return;
        }
        if(password.length()<6) {
            progressDialog.dismiss();
            builder.setTitle("Password can't be less than 6 characters").show();
            return;
        }
        firebaseAuth.createUserWithEmailAndPassword(email+"@mypoll.com",password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                progressDialog.dismiss();
                if(task.isSuccessful()){
                    Toast.makeText(getApplicationContext(),"account created successfully",Toast.LENGTH_LONG).show();
                    firebaseUser=firebaseAuth.getCurrentUser();
                    databaseReference.child(firebaseUser.getUid()).child("name").setValue(username).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                        if(!task.isSuccessful())
                            Toast.makeText(getApplicationContext(),"Can't create account\n\nTry again later",Toast.LENGTH_LONG).show();
                        }
                    });
                    finish();
                }
                else{
                    Toast.makeText(getApplicationContext(),"Account already exists",Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}
