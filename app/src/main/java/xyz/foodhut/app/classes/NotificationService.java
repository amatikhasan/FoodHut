package xyz.foodhut.app.classes;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Random;

import xyz.foodhut.app.data.SharedPreferenceHelper;
import xyz.foodhut.app.data.StaticConfig;
import xyz.foodhut.app.ui.MainActivity;
import xyz.foodhut.app.ui.customer.HomeCustomer;
import xyz.foodhut.app.ui.provider.HomeProvider;

public class NotificationService extends Service {

    private DatabaseReference databaseReference;
    private String id = StaticConfig.UID;
    private NotificationManager notificationManager;

    private static final String ADMIN_CHANNEL_ID = "admin_channel";


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Let it continue running until it is stopped.

        checkNotification();

        Log.d("check", "onStartCommand: service started");
        return START_STICKY;
    }

    private void checkNotification() {

        String type = SharedPreferenceHelper.getInstance(getApplicationContext()).getType();

        if (type.equals("customer")) {
            FirebaseDatabase.getInstance().getReference("customers/" + id + "/notifications/new")
                    .addChildEventListener(new ChildEventListener() {
                        @Override
                        public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                            String key = dataSnapshot.getKey();

                            getNotification(key);

                            Log.d("check", "onChildAdded: " + key);
                        }

                        @Override
                        public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                        }

                        @Override
                        public void onChildRemoved(DataSnapshot dataSnapshot) {

                        }

                        @Override
                        public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
        }

        if (type.equals("provider")) {
            FirebaseDatabase.getInstance().getReference("providers/" + id + "/notifications/new")
                    .addChildEventListener(new ChildEventListener() {
                        @Override
                        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                            String key = dataSnapshot.getKey();

                            getNotification(key);

                            Log.d("check", "onChildAdded: " + key);
                        }

                        @Override
                        public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                        }

                        @Override
                        public void onChildRemoved(DataSnapshot dataSnapshot) {

                        }

                        @Override
                        public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
        }
    }

    public void getNotification(String key) {

        String type = SharedPreferenceHelper.getInstance(getApplicationContext()).getType();

        if (type.equals("customer")) {

            FirebaseDatabase.getInstance().getReference("customers/" + id + "/notifications/new").child(key).child("title")
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {

                            if (dataSnapshot.getValue() != null) {
                                String title = String.valueOf(dataSnapshot.getValue());

                                Log.d("check", "service : customer" + title);

                                sendNotification(title);
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
        }

        if (type.equals("provider")) {

            FirebaseDatabase.getInstance().getReference("providers/" + id + "/notifications/new").child(key).child("title")
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {

                            if (dataSnapshot.getValue() != null) {
                                String title = String.valueOf(dataSnapshot.getValue());

                                Log.d("check", "service : provider" + title);

                                sendNotification(title);
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
        }
    }

    public void sendNotification(String title) {

        Log.d("check", "service : sending" + title);
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationCompat.Builder notificationBuilder = null;

        String channelId = getString(xyz.foodhut.app.R.string.default_notification_channel_id);
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        // Since android Oreo notification channel is needed.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId,
                    "Notification from FoodHut",
                    NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);
        }

        String type = SharedPreferenceHelper.getInstance(getApplicationContext()).getType();

        Intent intent = null;
        if (type.equals("customer"))
            intent = new Intent(this, HomeCustomer.class);
        if (type.equals("provider"))
            intent = new Intent(this, HomeProvider.class);
        if (intent != null) {
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        }
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                PendingIntent.FLAG_ONE_SHOT);


        notificationBuilder =
                new NotificationCompat.Builder(this, channelId)
                        .setSmallIcon(xyz.foodhut.app.R.mipmap.icon)
                        .setContentTitle("FoodHut")
                        .setContentText(title)
                        .setAutoCancel(true)
                        .setSound(defaultSoundUri)
                        .setContentIntent(pendingIntent);

        notificationManager.notify(0 /* ID of notification */, notificationBuilder.build());
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void setupChannels() {
        CharSequence adminChannelName = "FoodHut";
        String adminChannelDescription = "Notification from FoodHut";

        NotificationChannel adminChannel;
        adminChannel = new NotificationChannel(ADMIN_CHANNEL_ID, adminChannelName, NotificationManager.IMPORTANCE_HIGH);
        adminChannel.setDescription(adminChannelDescription);
        adminChannel.enableLights(true);
        adminChannel.setLightColor(Color.RED);
        adminChannel.enableVibration(true);
        if (notificationManager != null) {
            notificationManager.createNotificationChannel(adminChannel);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        Log.d("check", "onStartCommand: service stopped");
    }
}
