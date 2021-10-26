package akshay.kumar.eatit.Helper;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;

import androidx.annotation.RequiresApi;


import akshay.kumar.eatit.R;


public class NotificationHelper extends ContextWrapper {

    private static final String Channel_Id = "akshay.kumar.eatit.Akshay";
    private static final String Channel_Name = "Eat it";
    private NotificationManager manager;

    @RequiresApi(api = Build.VERSION_CODES.O)
    public NotificationHelper(Context base) {
        super(base);
        createChannels();


    }

    @RequiresApi(api = Build.VERSION_CODES.O)
   public void createChannels() {
        NotificationChannel hanishChannel = new NotificationChannel(Channel_Id, Channel_Name, NotificationManager.IMPORTANCE_DEFAULT);
        hanishChannel.enableLights(false);
        hanishChannel.enableVibration(true);
        hanishChannel.setLightColor(Color.RED);
        hanishChannel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);

        getManager().createNotificationChannel(hanishChannel);
    }

    public NotificationManager getManager() {
        if (manager == null)
            manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        return manager;
    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    public Notification.Builder getHanishChannelNotification(String title, String body, PendingIntent pendingIntent, Uri defaultSoundUri)
    {

        return new Notification.Builder(getApplicationContext(), Channel_Id)
                .setAutoCancel(false)
                .setWhen(System.currentTimeMillis())
                .setTicker("Akshay")
                .setContentText(body)
                .setContentTitle(title)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent)
                .setSmallIcon(R.mipmap.ic_launcher);
    }
}
