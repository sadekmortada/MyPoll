package com.example.mypoll;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class PollAdapter extends ArrayAdapter <PollView>{
    private Context context;
    public PollAdapter(Context context, ArrayList<PollView> arrayList) {
        super(context,0,arrayList);
        this.context=context;
    }
    @Override
    public View getView(int position, View convertView, ViewGroup parent){
        PollView pollView=getItem(position);
        convertView = LayoutInflater.from(context).inflate(R.layout.poll_view, parent, false);
        ImageView image = convertView.findViewById(R.id.poll_view_image);
        if(pollView.getBitmap()!=null)
            image.setImageBitmap(pollView.getBitmap());
        TextView textView = convertView.findViewById(R.id.poll_view_title);
        textView.setText(pollView.getTitle());
        textView = convertView.findViewById(R.id.poll_view_owner);
        textView.setText(pollView.getOwner());
        textView = convertView.findViewById(R.id.poll_view_date);
        textView.setText(pollView.getDate());
        return convertView;
    }

}
