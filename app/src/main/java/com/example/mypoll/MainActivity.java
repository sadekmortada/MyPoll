package com.example.mypoll;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class MainActivity extends AppCompatActivity {
    private static FirebaseAuth firebaseAuth;
    private static FirebaseUser firebaseUser;
    private ViewPager viewPager;
    private TabLayout tabLayout;
    private Toolbar toolbar;
    private MyViewPagerAdapter myViewPagerAdapter;
    private static DatabaseReference databaseReference;
    private SharedPreferences sharedPreferences;
    private static Notification.Builder builder;
    private static Notification notification;
    private static NotificationManager notificationManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initialize();
        if(firebaseUser!=null)
            notification();
    }
    public static void notification(){
        firebaseUser=firebaseAuth.getCurrentUser();
        databaseReference= FirebaseDatabase.getInstance().getReference("notifications").child(firebaseUser.getUid());
        databaseReference.setValue(""); //erase the database before listening
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
        viewPager.setAdapter(myViewPagerAdapter);
        firebaseUser=firebaseAuth.getCurrentUser();
        Intent notificationIntent = new Intent(this,MainActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        Resources res = getResources();
        builder = new Notification.Builder(this);
        builder.setContentIntent(contentIntent)
                .setSmallIcon(R.drawable.common_google_signin_btn_icon_dark)
                .setLargeIcon(BitmapFactory.decodeResource(res, R.drawable.common_google_signin_btn_icon_dark))
                .setWhen(System.currentTimeMillis())
                .setAutoCancel(true);
    }

    @Override
    protected void onStart() {
        super.onStart();
        firebaseUser=firebaseAuth.getCurrentUser();
        if(firebaseUser==null)
            startActivity(new Intent(this,LoginActivity.class));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu,menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.main_settings:
                break;
            case R.id.help:
                break;
            case R.id.sign_out:
                CurrentFragment.reset();
                sharedPreferences.edit().clear().apply();
                firebaseAuth.signOut();
                startActivity(new Intent(this,LoginActivity.class));
                break;
        }
        return true;
    }// TODO
}
