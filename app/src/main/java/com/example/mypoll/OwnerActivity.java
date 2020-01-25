package com.example.mypoll;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.provider.ContactsContract;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
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
import java.util.HashMap;
import java.util.Iterator;

import de.hdodenhof.circleimageview.CircleImageView;

public class OwnerActivity extends AppCompatActivity {
    private CircleImageView circleImageView;
    private String key;
    private TextView pollKey,pollTitle;
    private DatabaseReference databaseReference;
    private int position;
    private LinearLayout optionsLayout;
    private ArrayList<ArrayList<String>> voters;
    private CountDownTimer counter;
    private AlertDialog.Builder builder;
    private Button button;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_owner);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        initialize();
        fillOptions();
        if(counter!=null) {
            button.setClickable(false);
            counter.start();
        }
    }

    public void initialize(){
        circleImageView=findViewById(R.id.owner_poll_image);
        optionsLayout=findViewById(R.id.options_layout);
        pollTitle=findViewById(R.id.owner_poll_title);
        pollKey=findViewById(R.id.poll_key);
        button=findViewById(R.id.close);
        Intent intent=getIntent();
        position=intent.getIntExtra("position",0);
        key=intent.getStringExtra("key");
        pollKey.setText(key);
        pollTitle.setText(CurrentFragment.arrayList.get(position).getTitle());
        databaseReference= FirebaseDatabase.getInstance().getReference("polls").child(key);
        voters=new ArrayList<>();
        if(CurrentFragment.urls.get(position)!=null) {
            counter = new CountDownTimer(60000, 1000) {
                @Override
                public void onTick(long mSUF) {
                    Bitmap bitmap=CurrentFragment.arrayList.get(position).getBitmap();
                    if (bitmap!=null){
                        button.setClickable(true);
                        circleImageView.setImageBitmap(bitmap);
                        counter.cancel();
                    }
                }
                @Override
                public void onFinish() { }
            };
        }
        builder = new AlertDialog.Builder(this);
        builder.setIcon(android.R.drawable.ic_dialog_alert);
    }

    public void fillOptions(){
        String[] options=CurrentFragment.options.get(position).split("%#&");
        for(int i=0;i<options.length;i++) {
            voters.add(new ArrayList<String>());
            final LinearLayout linearLayout=new LinearLayout(this);
            linearLayout.setOrientation(LinearLayout.VERTICAL);
            TextView option=new TextView(this);
            final TextView votes=new TextView(this);
            TextView viewNames=new TextView(this);
            final int index=i;
            databaseReference.child("options").child(options[i]).addChildEventListener(new ChildEventListener() {
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

            votes.setText("votes: 0");
            viewNames.setTextColor(getResources().getColor(R.color.colorPrimaryDark));
            option.setTextColor(getResources().getColor(R.color.colorPrimary));
            votes.setTextColor(getResources().getColor(R.color.colorPrimary));
            option.setText("\""+options[i]+"\"");
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
            votes.setPadding(0,20,0,20);
            option.setTextAlignment(TextView.TEXT_ALIGNMENT_CENTER);
            votes.setTextAlignment(TextView.TEXT_ALIGNMENT_CENTER);
            viewNames.setTextAlignment(TextView.TEXT_ALIGNMENT_CENTER);
            option.setTextSize(20);
            votes.setTextSize(20);
            viewNames.setTextSize(20);
            linearLayout.setPadding(15,70,15,70);
            linearLayout.setBackground(getResources().getDrawable(R.drawable.smallwoodenbg));
            linearLayout.setLayoutParams(new LinearLayout.LayoutParams(500, LinearLayout.LayoutParams.MATCH_PARENT));
            optionsLayout.addView(linearLayout);
        }
    }

    public void viewVoters(int i){
        AlertDialog.Builder builder=new AlertDialog.Builder(this);
        ListView listView=new ListView(this);
        listView.setAdapter(new ArrayAdapter<>(this,android.R.layout.simple_list_item_1,voters.get(i)));
        builder.setView(listView).show();
    }

    public void copyKey(View view) {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("key",key);
        clipboard.setPrimaryClip(clip);
        Toast.makeText(this,"Copied to clipboard",Toast.LENGTH_SHORT).show();
    }


    public void closePoll(View view) {
        ConnectivityManager connectivityManager = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        if(!(connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
                connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED)){
            builder.setIcon(R.drawable.nowifismall).setTitle("No Internet Connection").setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) { }
            }).show();
            return;
        }
        builder.setIcon(android.R.drawable.ic_dialog_alert).setTitle("Are you sure?").setMessage("If you proceed, all the participants will be notified.\nYou can't reopen the poll again");
        builder.setPositiveButton("I'm sure", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                databaseReference.child("state").setValue("closed");
                databaseReference.child("participants").addListenerForSingleValueEvent(new ValueEventListener() { //inform all participants
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        Iterator iterator=dataSnapshot.getChildren().iterator();
                        DatabaseReference db=FirebaseDatabase.getInstance().getReference("notifications");
                        while(iterator.hasNext()){
                            HashMap<String,Object> hashMap=new HashMap<>();
                            hashMap.put("title","Poll \""+pollTitle.getText().toString()+"\" is closed!");
                            hashMap.put("body","Check Out");
                            hashMap.put("type","close");
                            db.child(((DataSnapshot)iterator.next()).getKey()).push().setValue(hashMap);
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) { }
                });
                HistoryFragment.arrayList.add(CurrentFragment.arrayList.remove(position));
                CurrentFragment.pollAdapter.notifyDataSetChanged();
                HistoryFragment.pollAdapter.notifyDataSetChanged();
                HistoryFragment.urls.add(CurrentFragment.urls.remove(position));
                HistoryFragment.keys.add(CurrentFragment.keys.remove(position));
                HistoryFragment.options.add(CurrentFragment.options.remove(position));
                CurrentFragment.types.remove(position);
                CurrentFragment.pos--;
                Intent intent=new Intent(getApplicationContext(),ResultActivity.class);
                intent.putExtra("key",key);
                intent.putExtra("position",CurrentFragment.historyPos);
                CurrentFragment.historyPos++;
                startActivity(intent);
                finish();
            }
        }).setNegativeButton("Cancel",null).show();
    }
}
