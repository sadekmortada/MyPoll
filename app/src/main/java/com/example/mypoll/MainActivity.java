package com.example.mypoll;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;
    private ViewPager viewPager;
    private TabLayout tabLayout;
    private Toolbar toolbar;
    private MyViewPagerAdapter myViewPagerAdapter;
    private DatabaseReference databaseReference;
    private SharedPreferences sharedPreferences;
    public boolean reset=true; // boolean used to know if we want to reset the notification listener if the user logged out and logged in again
    private static Notification.Builder builder;
    private static Notification notification;
    private static NotificationManager notificationManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initialize();
    }

    public void notification(){
        databaseReference= FirebaseDatabase.getInstance().getReference("notifications").child(firebaseUser.getUid());
        databaseReference.setValue(""); // erase the notifications database before listening because there maybe old notifications still saved and we want to listen for new ones
        databaseReference.addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                    builder.setContentTitle(dataSnapshot.child("title").getValue().toString());
                    builder.setContentText(dataSnapshot.child("body").getValue().toString());
                    notification = builder.build();
                    notificationManager.notify(0, notification);
                    databaseReference.setValue(""); //erase the database after listening
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
    }

    public void initialize(){
        toolbar=findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar);
        firebaseAuth=FirebaseAuth.getInstance();
        sharedPreferences=getSharedPreferences("user",MODE_PRIVATE);
        viewPager=findViewById(R.id.viewPager);
        tabLayout=findViewById(R.id.tabLayout);
        tabLayout.setupWithViewPager(viewPager);
        myViewPagerAdapter=new MyViewPagerAdapter(getSupportFragmentManager());
        viewPager.setOffscreenPageLimit(3);
        viewPager.setAdapter(myViewPagerAdapter);
        firebaseUser=firebaseAuth.getCurrentUser();
        Intent notificationIntent = new Intent(this,MainActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        Resources res = getResources();
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            notificationManager = getSystemService(NotificationManager.class);
            NotificationChannel notificationChannel=new NotificationChannel("1","channel1",NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(notificationChannel);
            builder=new Notification.Builder(this,"1");
        }
        else {
            builder = new Notification.Builder(this);
            notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        }
        builder.setContentIntent(contentIntent)
                .setSmallIcon(R.drawable.notificationicon)
                .setLargeIcon(BitmapFactory.decodeResource(res, R.drawable.notificationicon))
                .setWhen(System.currentTimeMillis())
                .setAutoCancel(true);
    }

    @Override
    protected void onStart() {
        super.onStart();
        firebaseUser=firebaseAuth.getCurrentUser();
        if(firebaseUser==null)
            startActivity(new Intent(this,LoginActivity.class));
        else{
            if(reset) {
                notification();
                reset=false;
            }
        }
    }

    public static void moveToHistory(String key){  // we call this function twice, once in the owner activity when owner closes his poll, second in CurrentFragment when a poll is closed
        int position=getPosition(CurrentFragment.arrayList,key);
        HistoryFragment.arrayList.add(CurrentFragment.arrayList.remove(position));
        CurrentFragment.pollAdapter.notifyDataSetChanged();
        HistoryFragment.pollAdapter.notifyDataSetChanged();

    }

    public static int getPosition(ArrayList<PollView> arrayList, String key){ // method returns poll position in arrayList if found, else returns -1
        for(int i=0;i<arrayList.size();i++)
            if(arrayList.get(i).getKey().equals(key))
                return i;
        Log.i("Poll Not Found!","Poll Not Found!");
        return -1;
    }

    @Override
    public boolean onCreateOptionsMenu( Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.help:
                AlertDialog.Builder builder=new AlertDialog.Builder(this);
                TextView textView=new TextView(this);
                textView.setText("  Welcome to \"MyPoll\" app\n\n  By \"Mypoll\", the decision between a group of people on a meet, trip, or anything else becomes much easier:\n\n" +
                        "  -Got an occasion soon?. Create a new poll with available choices to be voted and share it with your friends. See what they preferred among the choices.\n\n" +
                        "  -It requires two clicks for participating in polls you've been invited to.\n\n" +
                        "  -Manage your Current and history polls with no hardworking.\n\n"+
                        "  YES IT'S THAT SIMPLE ! :)");
                textView.setTextSize(20);
                textView.setBackground(getResources().getDrawable(R.drawable.bluebg));
                textView.setTextColor(getResources().getColor(R.color.colorPrimaryDark));
                textView.setPadding(20,20,20,20);
                builder.setView(textView).show();
                break;
            case R.id.sign_out:
                reset=true;
                sharedPreferences.edit().clear().apply(); // clear the shared preferences, which contains the user name
                firebaseAuth.signOut();
                startActivity(new Intent(this,LoginActivity.class));
                break;
        }
        return true;
    }
}
