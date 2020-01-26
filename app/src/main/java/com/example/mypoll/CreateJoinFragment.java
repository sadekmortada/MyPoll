package com.example.mypoll;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;

import android.provider.ContactsContract;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.EventListener;
import java.util.HashMap;
import java.util.Iterator;


/**
 * A simple {@link Fragment} subclass.
 */
public class CreateJoinFragment extends Fragment {
    private Button create,join;
    private String title,key;
    private EditText editText1,editText2;
    private DatabaseReference databaseReference;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;
    private SharedPreferences sharedPreferences;
    private NestedScrollView nestedScrollView;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view=inflater.inflate(R.layout.fragment_create_join, container, false);
        initialize(view);
        create.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                title=editText1.getText().toString();
                if(!title.equals("")) {
                    Intent intent = new Intent(getContext(), CreatePollActivity.class);
                    intent.putExtra("title", title);
                    startActivity(intent);
                    editText1.setText("");
                }
                else
                    Toast.makeText(getContext(),"Title can't be empty",Toast.LENGTH_SHORT).show();
            }
        });

        join.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                key=editText2.getText().toString();
                if(!key.equals("")) {
                    v.setClickable(false);
                    databaseReference.child("polls").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            Iterator iterator=dataSnapshot.getChildren().iterator();
                            boolean exist=false;
                            while(iterator.hasNext()){
                                DataSnapshot ds=(DataSnapshot)iterator.next();
                                String string=ds.getKey();
                                if(key.equals(string)){
                                    exist=true;
                                    String ownerId=ds.child("owner_id").getValue().toString();
                                    if(!firebaseUser.getUid().equals(ds.child("owner_id").getValue().toString())) {
                                        if(ds.child("state").getValue().equals("opened")) {
                                            final Intent intent=new Intent(getContext(),ParticipantActivity.class);
                                            intent.putExtra("key",key);
                                            intent.putExtra("position",CurrentFragment.pos);
                                            boolean flag=false;
                                            for(int i=0;i<CurrentFragment.keys.size();i++){
                                                if(key.equals(CurrentFragment.keys.get(i))){
                                                    Toast.makeText(getContext(),"You already joined before",Toast.LENGTH_SHORT).show();
                                                    flag=true;
                                                    v.setClickable(true);
                                                    break;
                                                }
                                            }
                                            if(!flag) {
                                                databaseReference.child("users").child(firebaseUser.getUid()).child("polls").child(key).setValue("");
                                                databaseReference.child("polls").child(key).child("participants").child(firebaseUser.getUid()).setValue("");
                                                HashMap<String,Object> hashMap=new HashMap<>();
                                                hashMap.put("title","\""+sharedPreferences.getString("user_name","")+"\" joined your poll \""+ds.child("title").getValue().toString()+"\"");
                                                hashMap.put("body","Check out");
                                                hashMap.put("type","join");
                                                databaseReference.child("notifications").child(ownerId).push().setValue(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if(task.isSuccessful())
                                                            startActivity(intent);
                                                        v.setClickable(true);
                                                    }
                                                });
                                            }
                                        }
                                        else {
                                            Toast.makeText(getContext(), "This poll is closed", Toast.LENGTH_SHORT).show();
                                            v.setClickable(true);
                                        }
                                    }
                                    else {
                                        Toast.makeText(getContext(), "You own this poll !", Toast.LENGTH_SHORT).show();
                                        v.setClickable(true);
                                    }
                                    break;
                                }
                            }
                            if(!exist) {
                                v.setClickable(true);
                                Toast.makeText(getContext(), "Wrong key", Toast.LENGTH_SHORT).show();
                            }
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                        }
                    });
                }
                else
                    Toast.makeText(getContext(),"Key can't be empty",Toast.LENGTH_SHORT).show();
                editText1.setText("");
            }
        });
        return view;
    }

    public void initialize(View view){
        editText1=view.findViewById(R.id.create_poll);
        editText2=view.findViewById(R.id.join_poll);
        create=view.findViewById(R.id.create_poll_button);
        join=view.findViewById(R.id.join_poll_button);
        sharedPreferences=getContext().getSharedPreferences("user", Context.MODE_PRIVATE);
        nestedScrollView=view.findViewById(R.id.create_join_scroll);
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        nestedScrollView.animate().alpha(1).setDuration(1000);
        databaseReference= FirebaseDatabase.getInstance().getReference();
        firebaseAuth=FirebaseAuth.getInstance();
        firebaseUser=firebaseAuth.getCurrentUser();
    }


}
