package com.example.mypoll;


import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.Icon;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.ExecutionException;


/**
 * A simple {@link Fragment} subclass.
 */
public class CurrentFragment extends Fragment {
    private PollAdapter pollAdapter;
    public static ArrayList<PollView> arrayList;
    public static ArrayList<String> keys;
    private ListView listView;
    private DatabaseReference databaseReference;
    private StorageReference storageReference;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;
    private String title, owner, date,time;
    private static boolean reset=false;

    @SuppressLint("ResourceAsColor")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_current, container, false);
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
//        ConnectivityManager connectivityManager = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
//        if (!(connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
//                connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED)) { return noConnectionView(); }
        initialize(view);
        initialize();
        if(firebaseUser!=null)
            fillArray();
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent;
                if(arrayList.get(position).getOwner().equals("by you"))
                    intent=new Intent(getContext(),OwnerActivity.class);
                else
                    intent=new Intent(getContext(),ParticipantActivity.class);
                intent.putExtra("key",keys.get(position));
                intent.putExtra("position",position);
                startActivity(intent);
            }
        });
        return view;
    }

    public void initialize(View view) {
        arrayList = new ArrayList<>();
        pollAdapter = new PollAdapter(getActivity(), arrayList);
        listView = view.findViewById(R.id.current_listview);
        listView.setAdapter(pollAdapter);
        keys=new ArrayList<>();
    }

    public void initialize(){
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        if(firebaseUser!=null)
            databaseReference = FirebaseDatabase.getInstance().getReference("users").child(firebaseUser.getUid()).child("polls");
        storageReference = FirebaseStorage.getInstance().getReference("polls");
    }

    public void fillArray() {
        databaseReference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                String pollKey=dataSnapshot.getKey();
                keys.add(pollKey);
                DatabaseReference dbr = FirebaseDatabase.getInstance().getReference("polls").child(pollKey);
                dbr.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        owner = dataSnapshot.child("owner_name").getValue().toString();
                        title = dataSnapshot.child("title").getValue().toString();
                        date = dataSnapshot.child("date").getValue().toString();
                        time = dataSnapshot.child("time").getValue().toString();
                        owner = "by \"" + owner + "\"";
                        if (dataSnapshot.child("owner_id").getValue().toString().equals(firebaseUser.getUid()))
                            owner = "by you";
                        Object url = dataSnapshot.child("image_url").getValue();
                        if ( url!= null)
                            new ImageDownloader().execute(url.toString(), title, owner, date, time);
                        else
                            new ImageDownloader().execute(null, title, owner, date, time);
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                    }
                });
            }
            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }
            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }
            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

//    private View noConnectionView() {
//        LinearLayout linearLayout = new LinearLayout(getContext());
//        ImageView imageView = new ImageView(getContext());
//        try {
//            imageView.setBackground(Drawable.createFromStream(getActivity().getAssets().open("nowifibig.png"), null));
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        imageView.setMinimumHeight(600);
//        imageView.setMinimumWidth(600);
//        linearLayout.addView(imageView);
//        linearLayout.setGravity(Gravity.CENTER);
//        return linearLayout;
//    }

    public class ImageDownloader extends AsyncTask<String,Void, Bitmap> {
        private String s1,s2,s3;
        @Override
        protected Bitmap doInBackground(String... strings) {
            s1=strings[1];
            s2=strings[2];
            s3=strings[3]+"\n"+strings[4];
            String string=strings[0];
            if(string!=null){
            try{
                URL url=new URL(string);
                HttpURLConnection urlConnection=(HttpURLConnection) url.openConnection();
                InputStream inputStream=urlConnection.getInputStream();
                return (BitmapFactory.decodeStream(inputStream));
            }catch (Exception e) {
                return null;
            }
            }
            return null;
        }
        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);
            arrayList.add(new PollView(bitmap, s1, s2, s3));
            pollAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if(reset){
            arrayList.clear();
            keys.clear();
            pollAdapter.notifyDataSetChanged();
            initialize();
            fillArray();
            reset=false;
        }
    }

    public static void reset(){
        reset=true;
    }
}