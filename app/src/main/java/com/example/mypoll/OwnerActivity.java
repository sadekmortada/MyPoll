package com.example.mypoll;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

public class OwnerActivity extends AppCompatActivity {
    private String key;
    private TextView pollKey,pollTitle;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_owner);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        initialize();
    }

    public void initialize(){
        Intent intent=getIntent();
        pollTitle=findViewById(R.id.owner_poll_title);
        key=intent.getStringExtra("key");
        pollKey=findViewById(R.id.poll_key);
        pollKey.setText(key);
        pollTitle.setText(CurrentFragment.arrayList.get(intent.getIntExtra("position",0)).getTitle());
    }

    public void copyKey(View view) {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("key",key);
        clipboard.setPrimaryClip(clip);
        Toast.makeText(this,"Copied to clipboard",Toast.LENGTH_SHORT).show();
    }
}
