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
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
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
    public static PollAdapter pollAdapter;
    public static ArrayList<PollView> arrayList;
    public static ArrayList<String> keys,options,urls,types,details;
    private ListView listView;
    private DatabaseReference databaseReference;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;
    private String title, owner, date,time,detail;
    public static int pos=0,historyPos=0;
    public static boolean reset=false,download=true;/* reset is used to know if user logged out in order to reset the listView and the arrayLists..
                                                    download is used to know if user created poll to prevent from downloading it since created polls are directly sent to
                                                    the listView in the CurrentFragment*/
    @SuppressLint("ResourceAsColor")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_current, container, false);
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        setHasOptionsMenu(true);
        initialize(view);
        initialize();
        if(firebaseUser!=null)
            fillArray();
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent;
                if(arrayList.get(position).getOwner().equals("by you"))
                    intent = new Intent(getContext(), OwnerActivity.class);
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
        options=new ArrayList<>();
        urls=new ArrayList<>();
        types=new ArrayList<>();
        details=new ArrayList<>();
    }

    public void initialize(){
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        if(firebaseUser!=null)
            databaseReference = FirebaseDatabase.getInstance().getReference("users").child(firebaseUser.getUid()).child("polls");
    }

    public void fillArray() {
        databaseReference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                if(download) { //to prevent downloading created polls by user, since they are passed to the arrayList of current polls with no need to download them again
                    final String pollKey = dataSnapshot.getKey();
                    DatabaseReference dbr = FirebaseDatabase.getInstance().getReference("polls").child(pollKey);
                    dbr.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            owner = dataSnapshot.child("owner_name").getValue().toString();
                            title = dataSnapshot.child("title").getValue().toString();
                            date = dataSnapshot.child("date").getValue().toString();
                            time = dataSnapshot.child("time").getValue().toString();
                            detail = dataSnapshot.child("details").getValue().toString();
                            if (dataSnapshot.child("owner_id").getValue().toString().equals(firebaseUser.getUid()))
                                owner = "by you";
                            else
                                owner = "by \"" + owner + "\"";
                            Object url = dataSnapshot.child("image_url").getValue();
                            String state=dataSnapshot.child("state").getValue().toString();
                            if (state.equals("opened")||state.equals("auto")) {
                                arrayList.add(new PollView(null, title, owner, date + "\n" + time));
                                if (url != null) {
                                    urls.add(url.toString());
                                    new ImageDownloader().execute(url.toString(), "" + pos);
                                } else
                                    urls.add(null);
                                pos++;
                                types.add(dataSnapshot.child("type").getValue().toString());
                                keys.add(pollKey);
                            }
                            else {
                                HistoryFragment.arrayList.add(new PollView(null, title, owner, date + "\n" + time));
                                if (url != null) {
                                    HistoryFragment.urls.add(url.toString());
                                    new HistoryImageDownloader().execute(url.toString(), "" + historyPos);
                                } else
                                    HistoryFragment.urls.add(null);
                                historyPos++;
                                HistoryFragment.keys.add(pollKey);
                            }
                            Iterator iterator = dataSnapshot.child("options").getChildren().iterator();
                            String string = "";
                            while (iterator.hasNext())
                                string += ((DataSnapshot) iterator.next()).getKey() + "#";
                            if (state.equals("opened")||state.equals("auto")) {
                                options.add(string);
                                pollAdapter.notifyDataSetChanged();
                                details.add(detail);
                            } else {
                                HistoryFragment.options.add(string);
                                HistoryFragment.pollAdapter.notifyDataSetChanged();
                                HistoryFragment.details.add(detail);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                        }
                    });
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
    }

    public class ImageDownloader extends AsyncTask<String,Void, Bitmap> {
        int i;
        @Override
        protected Bitmap doInBackground(String... strings) {
            i=Integer.parseInt(strings[1]);
            String string=strings[0];
            if(string!=null){
                try{
                    URL url=new URL(string);
                    HttpURLConnection urlConnection=(HttpURLConnection) url.openConnection();
                    InputStream inputStream=urlConnection.getInputStream();
                    return (BitmapFactory.decodeStream(inputStream));
                }catch (Exception e) { }
            }
            return null;
        }
        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);
            arrayList.get(i).setBitmap(bitmap);
            pollAdapter.notifyDataSetChanged();
        }
    }

    public class HistoryImageDownloader extends AsyncTask<String,Void, Bitmap> {
        int i;
        @Override
        protected Bitmap doInBackground(String... strings) {
            i=Integer.parseInt(strings[1]);
            String string=strings[0];
            if(string!=null){
                try{
                    URL url=new URL(string);
                    HttpURLConnection urlConnection=(HttpURLConnection) url.openConnection();
                    InputStream inputStream=urlConnection.getInputStream();
                    return (BitmapFactory.decodeStream(inputStream));
                }catch (Exception e) { }
            }
            return null;
        }
        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);
            HistoryFragment.arrayList.get(i).setBitmap(bitmap);
            HistoryFragment.pollAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if(reset){ //this is true when user logs out and logs in again, so we need to clear all variables and fill the listViews with new data of the logged in account
            historyPos=0;
            HistoryFragment.arrayList.clear();
            HistoryFragment.keys.clear();
            HistoryFragment.options.clear();
            HistoryFragment.urls.clear();
            HistoryFragment.pollAdapter.notifyDataSetChanged();
            HistoryFragment.details.clear();
            pos=0;
            arrayList.clear();
            keys.clear();
            options.clear();
            urls.clear();
            types.clear();
            details.clear();
            pollAdapter.notifyDataSetChanged();
            initialize(); // this initializes the firebase user
            fillArray();
            reset=false;
        }
    }


}