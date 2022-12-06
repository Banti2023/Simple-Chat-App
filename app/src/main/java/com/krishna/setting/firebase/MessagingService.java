package com.krishna.setting.firebase;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.krishna.setting.R;
import com.krishna.setting.activities.ChatActivity;
import com.krishna.setting.models.User;
import com.krishna.setting.utilities.constants;

import java.util.Random;

public class MessagingService extends FirebaseMessagingService {
    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
    }
    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        User user= new User();
        user.id=remoteMessage.getData().get(constants.KEY_USER_ID);
        user.name=remoteMessage.getData().get(constants.KEY_NAME);
        user.token=remoteMessage.getData().get(constants.KEY_FCM_TOKEN);
        int notificationId= new Random().nextInt();
        String channelId="chat_message";

        Intent intent= new Intent(this, ChatActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.putExtra(constants.KEY_USER,user);
        PendingIntent pendingIntent=PendingIntent.getActivity(this,0,intent,0);
        NotificationCompat.Builder builder= new NotificationCompat.Builder(this,channelId);
        builder.setSmallIcon(R.drawable.ic_round_notifications_active_24);
        builder.setContentTitle(user.name);
        builder.setContentText(remoteMessage.getData().get(constants.KEY_MESSAGE));
        builder.setStyle(new NotificationCompat.BigTextStyle().bigText(
                remoteMessage.getData().get(constants.KEY_MESSAGE)
        ));
        builder.setPriority(NotificationCompat.PRIORITY_DEFAULT);
        builder.setContentIntent(pendingIntent);
        builder.setAutoCancel(true);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            CharSequence channelName ="Chat message";
            String channelDescription="This notification channel is used for chat message notification";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel= new NotificationChannel(channelId,channelName,importance);
            channel.setDescription(channelDescription);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);

        }

        NotificationManagerCompat notificationManagerCompat= NotificationManagerCompat.from(this);
        notificationManagerCompat.notify(notificationId,builder.build());
    }
}
