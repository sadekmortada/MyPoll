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
    private ListView listView;
    private DatabaseReference databaseReference;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;
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
                intent.putExtra("key",arrayList.get(position).getKey());
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
                final String pollKey = dataSnapshot.getKey();
                DatabaseReference dbr = FirebaseDatabase.getInstance().getReference("polls").child(pollKey);
                dbr.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        String state = dataSnapshot.child("state").getValue().toString();
                        if (download) { //to prevent downloading created polls by user, since they are passed to the arrayList of current polls with no need to download them again
                            String owner = dataSnapshot.child("owner_name").getValue().toString();
                            String ownerId = dataSnapshot.child("owner_id").getValue().toString();
                            String title = dataSnapshot.child("title").getValue().toString();
                            String date = dataSnapshot.child("date").getValue().toString();
                            String time = dataSnapshot.child("time").getValue().toString();
                            String details = dataSnapshot.child("details").getValue().toString();
                            String type = dataSnapshot.child("type").getValue().toString();

                            Iterator iterator = dataSnapshot.child("options").getChildren().iterator();
                            StringBuilder choices = new StringBuilder();
                            while (iterator.hasNext())
                                choices.append(((DataSnapshot) iterator.next()).getKey()).append("#"); //choices concatenated by #
                            if (ownerId.equals(firebaseUser.getUid()))
                                owner = "by you";
                            else
                                owner = "by \"" + owner + "\"";

                            Object u = dataSnapshot.child("image_url").getValue();
                            String url = null;
                            if (u != null)
                                url = u.toString();

                            if (state.equals("opened") || state.equals("auto")) {
                                if (url != null)
                                    new ImageDownloader().execute(pollKey);
                                arrayList.add(new PollView(pollKey, null, title, owner, ownerId, date + "\n" + time, type, choices.toString(), url, details));
                                pollAdapter.notifyDataSetChanged();
                            } else {
                                if (url != null)
                                    new HistoryImageDownloader().execute(url.toString(), pollKey);
                                HistoryFragment.arrayList.add(new PollView(pollKey, null, title, owner, ownerId, date + "\n" + time, type, choices.toString(), url, details));
                                HistoryFragment.pollAdapter.notifyDataSetChanged();
                            }
                        }
                        // now this listener is for opened or auto polls, when closed we need to move them from current to history
                        if (state.equals("opened") || state.equals("auto")) {
                            final DatabaseReference db = FirebaseDatabase.getInstance().getReference("polls").child(pollKey).child("state");
                            db.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.getValue().toString().equals("closed")) {
                                        MainActivity.moveToHistory(pollKey);
                                        db.removeEventListener(this); // when poll closed, remove the listener(no more need for it)
                                    }
                                }
                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) { }
                            });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                    }
                });
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
            i=MainActivity.getPosition(arrayList,strings[0]);
            String string=arrayList.get(i).getUrl();
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
            i=MainActivity.getPosition(HistoryFragment.arrayList,strings[0]);
            String string=HistoryFragment.arrayList.get(i).getUrl();
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
            HistoryFragment.arrayList.clear();
            HistoryFragment.pollAdapter.notifyDataSetChanged();
            arrayList.clear();
            pollAdapter.notifyDataSetChanged();
            initialize(); // this initializes the firebase user
            fillArray();
            reset=false;
        }
    }


}