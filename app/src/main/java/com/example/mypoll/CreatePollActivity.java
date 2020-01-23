package com.example.mypoll;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.widget.NestedScrollView;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.InputType;
import android.text.format.Time;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.TimeZone;

import de.hdodenhof.circleimageview.CircleImageView;

public class CreatePollActivity extends AppCompatActivity {
    private EditText title,details;
    private Spinner spinner;
    private Uri uri=null;
    private ProgressDialog progressDialog;
    private CircleImageView circleImageView;
    private LinearLayout linearLayout;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;
    private DatabaseReference databaseReference,usersReference;
    private StorageReference storageReference;
    private String pollKey,pollTitle,pollDetails;
    private HashMap<String,Object> options;
    private HashMap<String,Object> info;
    private SharedPreferences sharedPreferences;
    private NestedScrollView nestedScrollView;
    private int i=2;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_poll);
        initialize();
    }

    public void initialize(){
        title=findViewById(R.id.poll_title);
        details=findViewById(R.id.poll_details);
        spinner=findViewById(R.id.spinner);
        linearLayout=findViewById(R.id.options_container);
        circleImageView=findViewById(R.id.poll_image);
        nestedScrollView=findViewById(R.id.nested_scroll_view);
        firebaseAuth=FirebaseAuth.getInstance();
        firebaseUser=firebaseAuth.getCurrentUser();
        DatabaseReference db=FirebaseDatabase.getInstance().getReference("polls").push();
        pollKey = db.getKey();
        databaseReference= FirebaseDatabase.getInstance().getReference("polls").child(pollKey);
        storageReference= FirebaseStorage.getInstance().getReference("polls").child(pollKey);
        usersReference=FirebaseDatabase.getInstance().getReference("users").child(firebaseUser.getUid());
        title.setText(getIntent().getStringExtra("title"));
        options=new HashMap<>();
        info=new HashMap<>();
        progressDialog=new ProgressDialog(this);
        progressDialog.setMessage("Please wait...");
        progressDialog.setCancelable(false);
        sharedPreferences=getSharedPreferences("user",MODE_PRIVATE);
    }

    public void addOption(View view) {
        i++;
        LinearLayout temp=new LinearLayout(this);
        temp.setOrientation(LinearLayout.HORIZONTAL);
        EditText editText=new EditText(this);
        ImageButton imageButton=new ImageButton(this);
        temp.addView(editText);
        temp.addView(imageButton);
        linearLayout.addView(temp);
        imageButton.setBackground(getResources().getDrawable(android.R.drawable.ic_menu_my_calendar));
        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addDate(v);
            }
        });
        editText.setHint("option "+i);
        editText.setWidth((((LinearLayout)linearLayout.getChildAt(0)).getChildAt(0)).getWidth());
        editText.setInputType(InputType.TYPE_CLASS_TEXT);
        editText.setTextColor(getResources().getColor(R.color.colorPrimaryDark));
        Toast.makeText(this,"Option added",Toast.LENGTH_SHORT).show();
    }

    public void removeOption(View view){
        if(i!=2){
            linearLayout.removeViewAt(i-1);
            i--;
            Toast.makeText(this,"Option removed",Toast.LENGTH_SHORT).show();
        }
        else
            Toast.makeText(this,"You must have at least two options",Toast.LENGTH_SHORT).show();
    }

    public void uploadPhoto(View view) {
        Intent intent=new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent,1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) { super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==1 && resultCode==RESULT_OK && data!=null && data.getData()!=null){
            uri=data.getData();
            circleImageView.setImageURI(uri);
        }
    }

    public void cancel(View view) {
        finish();
    }

    public void createPoll(View view){
        progressDialog.show();
        pollTitle=title.getText().toString();
        pollDetails=details.getText().toString();
        if(pollTitle.equals("")){
            Toast.makeText(this,"Title is required",Toast.LENGTH_SHORT).show();
            progressDialog.dismiss();
            return;
        }
        for(int i=0;i<linearLayout.getChildCount();i++) {
            String option=((EditText) ((LinearLayout)linearLayout.getChildAt(i)).getChildAt(0)).getText().toString();
            if(option.equals("")){
                Toast.makeText(this,"Options can't be empty",Toast.LENGTH_SHORT).show();
                progressDialog.dismiss();
                return;
            }
            else
                options.put(option, "");
        }
        Calendar calendar=Calendar.getInstance(TimeZone.getTimeZone(Time.getCurrentTimezone()));
        SimpleDateFormat simpleDateFormat=new SimpleDateFormat("dd MMM, yyyy");
        String currentDate=simpleDateFormat.format(calendar.getTime());
        simpleDateFormat=new SimpleDateFormat("hh:mm a");
        String currentTime=simpleDateFormat.format(calendar.getTime());
        info.put("owner_name",sharedPreferences.getString("user_name",""));
        info.put("owner_id",firebaseUser.getUid());
        info.put("title",pollTitle);
        info.put("details",pollDetails);
        info.put("date",currentDate);
        info.put("time",currentTime);
        info.put("state","opened");
        info.put("type",spinner.getSelectedItem().toString());
        final Intent intent=new Intent(this,OwnerActivity.class);
        intent.putExtra("key",pollKey);
        intent.putExtra("position",CurrentFragment.keys.size());
        if(uri!=null) { //uploading image and its url
            storageReference.putFile(uri).continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if (task.isSuccessful())
                        return storageReference.getDownloadUrl();
                    throw task.getException();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()) {
                        info.put("image_url", task.getResult().toString());
                        databaseReference.child("options").setValue(options);
                        databaseReference.updateChildren(info).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                usersReference.child("polls").child(pollKey).setValue("").addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        progressDialog.dismiss();
                                        startActivity(intent);
                                        Toast.makeText(getApplicationContext(), "Published !", Toast.LENGTH_SHORT).show();
                                        finish();
                                    }
                                });
                            }
                        });
                    }
                }
            });
        }
        else {
            databaseReference.child("options").setValue(options);
            databaseReference.updateChildren(info).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    usersReference.child("polls").child(pollKey).setValue("").addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            progressDialog.dismiss();
                            startActivity(intent);
                            Toast.makeText(getApplicationContext(), "Published !", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    });
                }
            });
        }
    }

    public void addDate(final View view){
        final EditText edit=(EditText)((LinearLayout)view.getParent()).getChildAt(0);
        final String old=edit.getText().toString();
        AlertDialog.Builder builder=new AlertDialog.Builder(this);
        Calendar calendar=Calendar.getInstance(TimeZone.getTimeZone(Time.getCurrentTimezone()));
        String[] months={"Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec"};
        SimpleDateFormat simpleDateFormat=new SimpleDateFormat("dd MMM, yyyy");
        String currentDate=simpleDateFormat.format(calendar.getTime());
        int y=Integer.parseInt(currentDate.split(" ")[2]);
        String currentMonth=(currentDate.split(" ")[1]).split(",")[0];
        HorizontalScrollView horizontalScroll=new HorizontalScrollView(this);
        LinearLayout linear=new LinearLayout(this);
        linear.setOrientation(LinearLayout.HORIZONTAL);
        int j;
        for(j=0;j<12;j++)
            if(months[j].equals(currentMonth))
                break;
        boolean sameMonth=true;
        for(int i=j;i<j+3;i++,sameMonth=false){
            String month=null;
            if (i<12)
                month=months[i];
            else {
                month = months[i % 12];
                if(i==12)
                    y++;
            }
            int nbdays=31;
            if(i==1) {
                if (Integer.parseInt(currentDate.split(" ")[2]) % 4 == 0)
                    nbdays = 28;
                else
                    nbdays=29;
            }
            if(i==3||i==5||i==8||i==10)
                nbdays=30;
            linear.addView(myCalendar(month,nbdays,Integer.parseInt(currentDate.split(" ")[0]),sameMonth,edit,y));
        }
        horizontalScroll.addView(linear);
        builder.setView(horizontalScroll).setPositiveButton("finished",null).setNegativeButton("cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                edit.setText(old);
            }
        }).setCancelable(false).show();
    }

    public TableLayout myCalendar(final String month, int nbdays, int day, boolean sameMonth, final EditText view, final int y){
        TableLayout tableLayout=new TableLayout(this);
        tableLayout.setBackground(getResources().getDrawable(R.drawable.blueframe));
        tableLayout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.MATCH_PARENT));
        tableLayout.setPadding(30,30,30,30);
        TableRow monthRow=new TableRow(this);
        TextView monthTextView=new TextView(this);
        monthRow.setLayoutParams(new TableLayout.LayoutParams(TableLayout.LayoutParams.WRAP_CONTENT, TableLayout.LayoutParams.WRAP_CONTENT));
        monthTextView.setText(month);
        monthRow.addView(monthTextView);
        monthRow.setGravity(Gravity.CENTER);
        monthTextView.setTextColor(getResources().getColor(android.R.color.black));
        monthTextView.setTextSize(40);
        monthTextView.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT));
        tableLayout.addView(monthRow);
        for(int i=1;i<=6;i++){
            TableRow tableRow=new TableRow(this);
            tableRow.setLayoutParams(new TableLayout.LayoutParams(TableLayout.LayoutParams.WRAP_CONTENT, TableLayout.LayoutParams.WRAP_CONTENT));
            tableRow.setPadding(0,10,0,10);
            for(int j=1;j<=6&&((i-1)*6+j)<=nbdays;j++){
                final int d=((i-1)*6+j);
                TextView textView=new TextView(this);
                textView.setText(""+d);
                if(!sameMonth || d>day) {
                    textView.setTextColor(getResources().getColor(R.color.colorPrimaryDark));
                    textView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            chooseDate(v,view,d,month,y,(LinearLayout) v.getParent().getParent().getParent());
                        }
                    });
                }
                if(d==day&&sameMonth) {
                    textView.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
                    textView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            chooseDate(v,view,-1,month,y,(LinearLayout) v.getParent().getParent().getParent());
                        }
                    });
                }
                textView.setTextSize(27);
                textView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                textView.setPadding(10,0,10,0);
                textView.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT));
                tableRow.addView(textView);
        }
            tableRow.setWeightSum(1);
            tableLayout.addView(tableRow);
        }
        return tableLayout;
    }

    public void chooseDate(View view,EditText editText,int d,String month,int y,LinearLayout linearLayout){
        editText.setText(d+"/"+month+"/"+y);
        if(d==-1)
            editText.setText("Today");
        for(int i=0;i<3;i++)
            for(int j=0;j<7;j++)
                for(int k=0;k<((TableRow)((TableLayout)linearLayout.getChildAt(i)).getChildAt(j)).getChildCount();k++)
                    ((TableRow)((TableLayout)linearLayout.getChildAt(i)).getChildAt(j)).getChildAt(k).setBackground(null);
        view.setBackground(getResources().getDrawable(android.R.color.holo_blue_light));
    }
}
