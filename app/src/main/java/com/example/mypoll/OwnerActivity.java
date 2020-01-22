package com.example.mypoll;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Iterator;

public class OwnerActivity extends AppCompatActivity {
    private String key;
    private TextView pollKey,pollTitle;
    private DatabaseReference databaseReference;
    private int position;
    private LinearLayout optionsLayout;
    private ArrayList<ArrayList<String>> voters;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_owner);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        initialize();
        fillOptions();
    }

    public void initialize(){
        Intent intent=getIntent();
        position=intent.getIntExtra("position",0);
        pollTitle=findViewById(R.id.owner_poll_title);
        key=intent.getStringExtra("key");
        pollKey=findViewById(R.id.poll_key);
        pollKey.setText(key);
        pollTitle.setText(CurrentFragment.arrayList.get(position).getTitle());
        databaseReference= FirebaseDatabase.getInstance().getReference("polls").child(key).child("options");
        optionsLayout=findViewById(R.id.options_layout);
        voters=new ArrayList<>();
    }

    public void fillOptions(){
        String[] options=CurrentFragment.options.get(position).split("%#&");
        for(int i=0;i<options.length-1;i++) {
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
            option.setText(options[i]);
            votes.setText("votes: 0");
            viewNames.setTextColor(getResources().getColor(R.color.colorPrimaryDark));
            option.setTextColor(getResources().getColor(R.color.colorPrimaryDark));
            votes.setTextColor(getResources().getColor(R.color.colorPrimaryDark));
            SpannableString spannableString=new SpannableString("view voters");
            spannableString.setSpan(new UnderlineSpan(),0,spannableString.length(),0);
            votes.setText(spannableString);
            votes.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    viewVoters(index);
                }
            });
            linearLayout.addView(option);
            linearLayout.addView(votes);
            linearLayout.addView(viewNames);
            optionsLayout.addView(linearLayout);
        }
    }

    public void viewVoters(int i){
        AlertDialog.Builder builder=new AlertDialog.Builder(this);
        ListView listView=new ListView(this);
        listView.setAdapter(new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,voters.get(i)));
        builder.setView(listView).show();
    }

    public void copyKey(View view) {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("key",key);
        clipboard.setPrimaryClip(clip);
        Toast.makeText(this,"Copied to clipboard",Toast.LENGTH_SHORT).show();
    }
}
