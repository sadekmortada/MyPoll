package com.example.mypoll;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.ProgressDialog;
import android.content.DialogInterface;
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

public class RegisterActivity extends AppCompatActivity {
    private ProgressDialog progressDialog;
    private String email,password,confirmPassword;
    private EditText registerEmail,registerPassword,registerConfirmPassword;
    private Button registerButton;
    FirebaseAuth firebaseAuth;
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
        registerButton=findViewById(R.id.registerButton);
        firebaseAuth=FirebaseAuth.getInstance();
        progressDialog=new ProgressDialog(this);
        progressDialog.setTitle("Please wait...");
        progressDialog.setCanceledOnTouchOutside(false);
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                register();
            }
        });
    }

    public void toLogin(View view) {
        finish();
    }

    public void register() {
        progressDialog.show();
        email=registerEmail.getText().toString();
        password=registerPassword.getText().toString();
        confirmPassword=registerConfirmPassword.getText().toString();
        if(email.equals("")|| password.equals("") || confirmPassword.equals("")){
            progressDialog.dismiss();
            AlertDialog.Builder builder = new AlertDialog.Builder(this).setIcon(android.R.drawable.ic_dialog_alert).setTitle("Please fill the fields before submitting")
                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) { }
                    });
            builder.show();
        }
        if( !confirmPassword.equals(password)){
            progressDialog.dismiss();
            AlertDialog.Builder builder = new AlertDialog.Builder(this).setIcon(android.R.drawable.ic_dialog_alert).setTitle("Passwords does not match")
                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) { }
                    });
            builder.show();
        }
        firebaseAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                progressDialog.dismiss();
                if(task.isSuccessful()){
                    Toast.makeText(getApplicationContext(),"account created successfully",Toast.LENGTH_LONG).show();
                    finish();
                }
                else{
                    TextView alert=new TextView(getApplicationContext());
                    alert.setText("Account already exists.\nOr\nCheck internet connection");
                    alert.setPadding(10,10,10,10);
                    AlertDialog.Builder builder = new AlertDialog.Builder(getApplicationContext()).setIcon(android.R.drawable.ic_dialog_alert).setTitle("Register failed!")
                            .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) { }
                            }).setView(alert);
                    builder.show();
                }
            }
        });
    }
}
