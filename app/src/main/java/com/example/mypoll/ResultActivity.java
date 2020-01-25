package com.example.mypoll;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class ResultActivity extends AppCompatActivity {
    private CircleImageView circleImageView;
    private String key;
    private TextView pollTitle;
    private DatabaseReference databaseReference;
    private int position;
    private LinearLayout resultsLayout;
    private ArrayList<ArrayList<String>> voters;
    private CountDownTimer counter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        initialize();
        fillOptions();
        if(counter!=null)
            counter.start();
    }

    public void initialize(){
        circleImageView=findViewById(R.id.result_poll_image);
        resultsLayout=findViewById(R.id.results_layout);
        pollTitle=findViewById(R.id.result_poll_title);
        Intent intent=getIntent();
        position=intent.getIntExtra("position",0);
        key=intent.getStringExtra("key");
        pollTitle.setText(HistoryFragment.arrayList.get(position).getTitle());
        databaseReference= FirebaseDatabase.getInstance().getReference("polls").child(key).child("options");
        voters=new ArrayList<>();
        if(HistoryFragment.urls.get(position)!=null) {
            counter = new CountDownTimer(60000, 1000) {
                @Override
                public void onTick(long mSUF) {
                    Bitmap bitmap=HistoryFragment.arrayList.get(position).getBitmap();
                    if (bitmap!=null){
                        circleImageView.setImageBitmap(bitmap);
                        counter.cancel();
                    }
                }
                @Override
                public void onFinish() { }
            };
        }
    }

    public void fillOptions(){
        String[] options=HistoryFragment.options.get(position).split("%#&");
        for(int i=0;i<options.length;i++) {
            voters.add(new ArrayList<String>());
            final LinearLayout linearLayout=new LinearLayout(this);
            linearLayout.setOrientation(LinearLayout.VERTICAL);
            TextView option=new TextView(this);
            final TextView votes=new TextView(this);
            TextView viewNames=new TextView(this);
            final int index=i;
            databaseReference.child(options[i]).addChildEventListener(new ChildEventListener() {
                private int counter=0;
                @Override
                public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                    if(!dataSnapshot.getKey().equals("")) {
                        counter++;
                        String temp = votes.getText().toString();
                        temp = temp.substring(0, temp.length() - 1) + counter;
                        votes.setText(temp);
                        voters.get(index).add(dataSnapshot.getKey());
                    }
                }
                @Override
                public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) { }
                @Override
                public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) { }
                @Override
                public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) { }
                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) { }
            });
            option.setText("\""+options[i]+"\"");
            votes.setText("votes: 0");
            viewNames.setTextColor(getResources().getColor(R.color.colorPrimaryDark));
            option.setTextColor(getResources().getColor(R.color.colorPrimary));
            votes.setTextColor(getResources().getColor(R.color.colorPrimary));
            SpannableString spannableString=new SpannableString("view voters");
            spannableString.setSpan(new UnderlineSpan(),0,spannableString.length(),0);
            viewNames.setText(spannableString);
            viewNames.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    viewVoters(index);
                }
            });
            linearLayout.addView(option);
            linearLayout.addView(votes);
            linearLayout.addView(viewNames);
            option.setTextSize(20);
            votes.setTextSize(20);
            viewNames.setTextSize(20);
            linearLayout.setPadding(15,70,15,70);
            linearLayout.setBackground(getResources().getDrawable(R.drawable.smallwoodenbg));
            linearLayout.setLayoutParams(new LinearLayout.LayoutParams(500, LinearLayout.LayoutParams.MATCH_PARENT));
            resultsLayout.addView(linearLayout);
        }
    }

    public void viewVoters(int i){
        AlertDialog.Builder builder=new AlertDialog.Builder(this);
        ListView listView=new ListView(this);
        listView.setAdapter(new ArrayAdapter<>(this,android.R.layout.simple_list_item_1,voters.get(i)));
        builder.setView(listView).show();
    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        getMenuInflater().inflate(R.menu.results_menu,menu);
//        return true;
//    }
}
