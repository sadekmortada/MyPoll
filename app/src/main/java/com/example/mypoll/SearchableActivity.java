package com.example.mypoll;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ListActivity;
import android.app.SearchManager;
import android.content.Intent;
import android.os.Bundle;

import java.util.ArrayList;

public class SearchableActivity extends ListActivity {
    private PollAdapter pollAdapter;
    private ArrayList<PollView> arrayList;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_searchable);
        arrayList=new ArrayList<>();
        setListAdapter(pollAdapter);
        pollAdapter=new PollAdapter(this,CurrentFragment.arrayList);
        Intent intent = getIntent();
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            doMySearch(query);
        }
    }
    public void doMySearch(String query){
        for(int i=0;i<CurrentFragment.arrayList.size();i++)
            if(CurrentFragment.arrayList.get(i).getTitle().contains(query))
                arrayList.add(CurrentFragment.arrayList.get(i));
        for(int i=0;i<HistoryFragment.arrayList.size();i++)
            if(HistoryFragment.arrayList.get(i).getTitle().contains(query))
                arrayList.add(HistoryFragment.arrayList.get(i));
            pollAdapter.notifyDataSetChanged();
    }
}
