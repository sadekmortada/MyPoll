package com.example.mypoll;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import de.hdodenhof.circleimageview.CircleImageView;

public class ParticipantActivity extends AppCompatActivity {
    private CircleImageView circleImageView;
    private String key,choice;
    private TextView pollTitle,pollDetails,wait,date;
    private ArrayList<String> choices;
    private DatabaseReference databaseReference;
    private SharedPreferences sharedPreferences;
    private int position;
    private Button submitVote;
    private ProgressDialog progressDialog;
    private LinearLayout buttonsLayout,containerLayout,choicesLayout;
    private CountDownTimer counter;
    private AlertDialog.Builder builder;
    private HorizontalScrollView horizontalScrollView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_participant);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        initialize();
        showChoices();
        if(counter!=null)
            counter.start();
        checkVoted();
    }
    public void checkVoted(){
        FirebaseDatabase.getInstance().getReference("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("polls").child(key).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.getValue().toString().equals("voted")){
                    submitVote.setAlpha(0);
                    submitVote.setClickable(false);
                    wait.setAlpha(1);
                    for(int i=0;i<buttonsLayout.getChildCount();i++)
                        buttonsLayout.getChildAt(i).setClickable(false);
                    return;
                }
                submitVote.setAlpha(1);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) { }
        });
    }

    public void initialize(){
        horizontalScrollView=findViewById(R.id.choices_scroll);
        circleImageView=findViewById(R.id.participant_poll_image);
        pollTitle=findViewById(R.id.participant_poll_title);
        pollDetails=findViewById(R.id.participant_poll_details);
        submitVote=findViewById(R.id.submit_vote);
        wait=findViewById(R.id.wait);
        date=findViewById(R.id.participant_poll_date);
        Intent intent=getIntent();
        position=intent.getIntExtra("position",0);
        key=intent.getStringExtra("key");
        pollTitle.setText(CurrentFragment.arrayList.get(position).getTitle());
        date.setText(CurrentFragment.arrayList.get(position).getDate());
        String details=CurrentFragment.details.get(position);
        if(!details.equals(""))
            pollDetails.setText("Details: "+details);
        if(CurrentFragment.urls.get(position)!=null) {
            counter = new CountDownTimer(60000, 1000) {
                @Override
                public void onTick(long mSUF) {
                    Bitmap bitmap=CurrentFragment.arrayList.get(position).getBitmap();
                    if (bitmap!=null){
                        circleImageView.setImageBitmap(bitmap);
                        counter.cancel();
                    }
                }
                @Override
                public void onFinish() { }
            };
        }
        databaseReference= FirebaseDatabase.getInstance().getReference("polls").child(key).child("options");
        builder = new AlertDialog.Builder(this);
        builder.setIcon(android.R.drawable.ic_dialog_alert).setPositiveButton("Ok", null);
        choices=new ArrayList<>();
        sharedPreferences=getSharedPreferences("user",MODE_PRIVATE);
        progressDialog=new ProgressDialog(this);
        progressDialog.setMessage("Please wait...");
        progressDialog.setCancelable(false);
    }

    public void showChoices(){
        final String[] options=CurrentFragment.options.get(position).split("#");
        boolean flag=CurrentFragment.types.get(position).equals("single choice");
        if(flag)
            buttonsLayout=new RadioGroup(this);
        else
            buttonsLayout=new LinearLayout(this);
        buttonsLayout.setOrientation(LinearLayout.HORIZONTAL);
        choicesLayout=new LinearLayout(this);
        choicesLayout.setOrientation(LinearLayout.HORIZONTAL);
        containerLayout=new LinearLayout(this);
        containerLayout.setOrientation(LinearLayout.VERTICAL);
        containerLayout.addView(choicesLayout);
        containerLayout.addView(buttonsLayout);
        containerLayout.setPadding(0,10,0,10);
        choicesLayout.setPadding(0,0,0,10);
        containerLayout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT));
        horizontalScrollView.addView(containerLayout);
        for(int i=0;i<options.length;i++) {
            TextView option=new TextView(this);
            option.setText(options[i]);
            option.setTextSize(TypedValue.COMPLEX_UNIT_SP,20);
            option.setWidth(300);
            option.setTextColor(getResources().getColor(R.color.colorPrimaryDark));
            option.setPadding(20,0,20,15);
            choicesLayout.addView(option);
            final int index=i;
            if(flag){
                RadioButton radioButton =new RadioButton(this);
                buttonsLayout.addView(radioButton);
                radioButton.setWidth(320);
                radioButton.setPadding(20,0,20,0);
                radioButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        choice=options[index];
                    }
                });
            }
            else {
                CheckBox checkBox = new CheckBox(this);
                buttonsLayout.addView(checkBox);
                checkBox.setWidth(320);
                checkBox.setPadding(20,0,20,0);
                checkBox.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(((CheckBox)v).isChecked())
                            choices.add(options[index]);
                        else{
                            choices.remove(index);
                        }
                    }
                });
            }
        }
    }

    public void submit(final View view) {
        ConnectivityManager connectivityManager = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        if(!(connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
                connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED)){
            builder.setTitle("No Internet Connection").show();
            return;
        }
        if(choice==null&&choices.size()==0){
            Toast.makeText(this,"You must choose among choices",Toast.LENGTH_SHORT).show();
            return;
        }
        progressDialog.show();
        builder.setTitle("Are you sure?").setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                HashMap<String,Object> hashMap=new HashMap<>();
                hashMap.put(sharedPreferences.getString("user_name",""),"");
                if(choice!=null) // if the poll is multiple choice, then this variable will stay null
                    databaseReference.child(choice).updateChildren(hashMap);
                else { // in the else we will iterate in the choices made by user and associate his name with each one in the database
                    for (int i = 0; i < choices.size(); i++)
                        databaseReference.child(choices.get(i)).updateChildren(hashMap);
                }
                final DatabaseReference db=FirebaseDatabase.getInstance().getReference();
                db.child("polls").child(key).child("owner_id").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        HashMap<String,Object> notify=new HashMap<>();
                        notify.put("title","\""+sharedPreferences.getString("user_name","")+"\" voted on your poll \""+CurrentFragment.arrayList.get(position).getTitle()+"\"");
                        notify.put("body","Check out");
                        notify.put("type","vote");
                        db.child("notifications").child(dataSnapshot.getValue().toString()).push().setValue(notify).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if(task.isSuccessful()){
                                    db.child("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("polls").child(key).setValue("voted");
                                    Toast.makeText(getApplicationContext(),"Voted Successfully",Toast.LENGTH_SHORT).show();
                                    view.setClickable(false);
                                    view.setAlpha(0);
                                    wait.setAlpha(1);
                                    for(int i=0;i<buttonsLayout.getChildCount();i++)
                                        buttonsLayout.getChildAt(i).setClickable(false);
                                }
                                else
                                    Toast.makeText(getApplicationContext(),"Oops, looks like an error happened!",Toast.LENGTH_SHORT).show();
                                progressDialog.dismiss();
                            }
                        });
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) { }
                });
            }
        }).setNegativeButton("Cancel",null).show();
    }
}
