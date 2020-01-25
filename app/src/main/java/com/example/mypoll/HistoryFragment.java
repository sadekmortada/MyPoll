package com.example.mypoll;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;


public class HistoryFragment extends Fragment {
    public static ArrayList<PollView> arrayList;
    public static ArrayList<String> urls,options,keys;
    public static PollAdapter pollAdapter;
    private static ListView listView;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view= inflater.inflate(R.layout.fragment_history, container, false);
        setHasOptionsMenu(true);
        initialize(view);
        return view;
    }
    public void initialize(View view){
        listView=view.findViewById(R.id.history_listview);
        listView.setAdapter(pollAdapter);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.history_menu,menu);
//        MenuItem menuItem=menu.findItem(R.id.app_bar_search);
//        SearchView searchView=(SearchView) menuItem.getActionView();
//        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
//            @Override
//            public boolean onQueryTextSubmit(String query) {
//                return false; }
//            @Override
//            public boolean onQueryTextChange(String newText) {
//                Toast.makeText(getContext(),"new",Toast.LENGTH_SHORT).show();
//                return false;
//            }
//        });
    }
}
