package com.example.mypoll;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class ParticipantActivity extends AppCompatActivity {
    public static CircleImageView circleImageView;
    private String key;
    private TextView pollTitle;
    private DatabaseReference databaseReference;
    private int position;
    private LinearLayout choicesLayout;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_participant);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        initialize();
        showChoices();
    }

    public void initialize(){
//        radioGroup=new RadioGroup(this);
//        radioGroup.setOr
        circleImageView=findViewById(R.id.participant_poll_image);
        choicesLayout=findViewById(R.id.choices_layout);
        pollTitle=findViewById(R.id.participant_poll_title);
        Intent intent=getIntent();
        position=intent.getIntExtra("position",0);
//        key=intent.getStringExtra("key");
        pollTitle.setText(CurrentFragment.arrayList.get(position).getTitle());
//        databaseReference= FirebaseDatabase.getInstance().getReference("polls").child(key).child("options");
    }

    public void showChoices(){
        String[] options=CurrentFragment.options.get(position).split("%#&");
        for(int i=0;i<options.length;i++) {
            LinearLayout linearLayout=new LinearLayout(this);
            linearLayout.setOrientation(LinearLayout.VERTICAL);
            TextView option=new TextView(this);
            option.setText(options[i]);
            option.setTextColor(getResources().getColor(R.color.colorPrimaryDark));
            option.setPadding(0,0,0,15);
            linearLayout.addView(option);
//            if(CurrentFragment.types.get(position).equals("single choice")){
//                RadioButton radioButton =new RadioButton(this);
//                radioGroup.addView(radioButton);
//                linearLayout.addView(radioButton);
//            }
                CheckBox checkBox= new CheckBox(this);
                linearLayout.addView(checkBox);
            linearLayout.setPadding(70,20,30,20);
            linearLayout.setBackground(getResources().getDrawable(R.drawable.blueframe));
            choicesLayout.addView(linearLayout);
        }
    }
}
