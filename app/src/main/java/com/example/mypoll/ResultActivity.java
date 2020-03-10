package com.example.mypoll;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
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
    private TextView pollTitle,pollDetails;
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
        setSupportActionBar((Toolbar) findViewById(R.id.results_toolbar));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true); // this is for the back button in the action bar
        initialize();
        fillOptions();
        if(counter!=null)
            counter.start();
    }

    public void initialize(){
        circleImageView=findViewById(R.id.result_poll_image);
        resultsLayout=findViewById(R.id.results_layout);
        pollTitle=findViewById(R.id.result_poll_title);
        pollDetails=findViewById(R.id.result_poll_details);
        Intent intent=getIntent();
        key=intent.getStringExtra("key");
        position=MainActivity.getPosition(HistoryFragment.arrayList,key);
        pollTitle.setText(HistoryFragment.arrayList.get(position).getTitle());
        databaseReference= FirebaseDatabase.getInstance().getReference("polls").child(key).child("options");
        voters=new ArrayList<>();
        String details=HistoryFragment.arrayList.get(position).getDetails();
        if(!details.equals(""))
            pollDetails.setText("Details: "+details);
        if(HistoryFragment.arrayList.get(position).getUrl()!=null) {
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
        String[] choices=HistoryFragment.arrayList.get(position).getChoices().split("#");
        for(int i=0;i<choices.length;i++) {
            voters.add(new ArrayList<String>());
            final LinearLayout linearLayout=new LinearLayout(this);
            linearLayout.setOrientation(LinearLayout.VERTICAL);
            TextView option=new TextView(this);
            final TextView votes=new TextView(this);
            TextView viewNames=new TextView(this);
            final int index=i;
            databaseReference.child(choices[i]).addChildEventListener(new ChildEventListener() {
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
            option.setText("\""+choices[i]+"\"");
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
            option.setTextAlignment(TextView.TEXT_ALIGNMENT_CENTER);
            votes.setTextAlignment(TextView.TEXT_ALIGNMENT_CENTER);
            viewNames.setTextAlignment(TextView.TEXT_ALIGNMENT_CENTER);
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.result_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId()==R.id.delete){
            HistoryFragment.arrayList.remove(position);
            HistoryFragment.pollAdapter.notifyDataSetChanged();
            ConnectivityManager connectivityManager = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
            if(!(connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
                    connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED)){
                android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
                builder.setIcon(R.drawable.nowifismall).setTitle("No Internet Connection").setMessage("Poll deleted locally from your device.\nYou must be connected to the internet to remove it from the cloud..").setPositiveButton("Ok", null).show();
                return true;
            }
            FirebaseDatabase.getInstance().getReference("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("polls").child(key).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful()){
                        Toast.makeText(getApplicationContext(),"Deleted Successfully",Toast.LENGTH_SHORT).show();
                        finish();
                    }
                }
            });
        }
        return super.onOptionsItemSelected(item);
    }
}
