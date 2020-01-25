package com.example.mypoll;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
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

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class ParticipantActivity extends AppCompatActivity {
    private CircleImageView circleImageView;
    private String key;
    private TextView pollTitle;
    private DatabaseReference databaseReference;
    private int position;
    private LinearLayout choicesLayout;
    private CountDownTimer counter;
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
    }

    public void initialize(){
        horizontalScrollView=findViewById(R.id.choices_scroll);
        circleImageView=findViewById(R.id.participant_poll_image);
        pollTitle=findViewById(R.id.participant_poll_title);
        Intent intent=getIntent();
        position=intent.getIntExtra("position",0);
        key=intent.getStringExtra("key");
        pollTitle.setText(CurrentFragment.arrayList.get(position).getTitle());
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
    }

    public void showChoices(){
        String[] options=CurrentFragment.options.get(position).split("%#&");
        if(CurrentFragment.types.get(position).equals("single choice"))
            choicesLayout=new RadioGroup(this);
        else
            choicesLayout=new LinearLayout(this);
        choicesLayout.setOrientation(LinearLayout.HORIZONTAL);
        horizontalScrollView.addView(choicesLayout);
        for(int i=0;i<options.length;i++) {
            LinearLayout linearLayout=new LinearLayout(this);
            linearLayout.setOrientation(LinearLayout.VERTICAL);
            TextView option=new TextView(this);
            option.setText(options[i]);
            option.setTextColor(getResources().getColor(R.color.colorPrimaryDark));
            option.setPadding(0,0,0,15);
            linearLayout.addView(option);
            if(CurrentFragment.types.get(position).equals("single choice")){
                RadioButton radioButton =new RadioButton(this);
                linearLayout.addView(radioButton);
            }
            else {
                CheckBox checkBox = new CheckBox(this);
                linearLayout.addView(checkBox);
            }
            linearLayout.setPadding(15,70,15,70);
            linearLayout.setBackground(getResources().getDrawable(R.drawable.smallwoodenbg));
            linearLayout.setLayoutParams(new LinearLayout.LayoutParams(500, LinearLayout.LayoutParams.MATCH_PARENT));
            choicesLayout.addView(linearLayout);
        }
    }
}
