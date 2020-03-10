package com.example.mypoll;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;

import com.google.firebase.database.FirebaseDatabase;

public class NotificationBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Intent notificationIntent = new Intent(context,MainActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(context, 0, notificationIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        NotificationManager notificationManager;
        Notification.Builder builder;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            notificationManager = context.getSystemService(NotificationManager.class);
            NotificationChannel notificationChannel=new NotificationChannel("2","channel2",NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(notificationChannel);
            builder=new Notification.Builder(context,"2");
        }
        else {
            builder = new Notification.Builder(context);
            notificationManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
        }
        builder.setContentText(intent.getStringExtra("body"))
                .setContentTitle(intent.getStringExtra("title"))
                .setContentIntent(contentIntent)
                .setSmallIcon(R.drawable.notificationicon)
                .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.notificationicon))
                .setAutoCancel(false);
        Notification notification=builder.build();
        notificationManager.notify(1, notification);
        FirebaseDatabase.getInstance().getReference("polls").child(intent.getStringExtra("key")).child("state").setValue("closed");
    }
}
