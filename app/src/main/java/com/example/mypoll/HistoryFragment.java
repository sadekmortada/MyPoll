package com.example.mypoll;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;


public class HistoryFragment extends Fragment {
    public static ArrayList<PollView> arrayList;
    public static ArrayList<String> urls,options,keys,details;
    public static PollAdapter pollAdapter;
    private static ListView listView;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view= inflater.inflate(R.layout.fragment_history, container, false);
        setHasOptionsMenu(true);
        initialize(view);
        initialize();
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent;
                intent=new Intent(getContext(),ResultActivity.class);
                intent.putExtra("key",keys.get(position));
                intent.putExtra("position",position);
                startActivity(intent);
            }
        });
        return view;
    }
    public void initialize(View view){
        listView=view.findViewById(R.id.history_listview);
    }
    public void initialize(){
        arrayList = new ArrayList<>();
        options = new ArrayList<>();
        urls = new ArrayList<>();
        keys = new ArrayList<>();
        details = new ArrayList<>();
        pollAdapter = new PollAdapter(getContext(), arrayList);
        listView.setAdapter(pollAdapter);
    }

}
