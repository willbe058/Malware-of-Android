package com.xpf.me.sms.routine.channel;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;

import com.xpf.me.sms.MainActivity;
import com.xpf.me.sms.R;

/**
 * Created by xgo on 10/30/15.
 */

public class IntentTunnel extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        int notificationId = 001;
        Intent viewIntent = new Intent(context, MainActivity.class);
        PendingIntent viewPendingIntent = PendingIntent.getActivity(context, 0,
                viewIntent, 0);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(
                context).setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("Hey you ! :)")
                .setContentText("POC Android malware ransonware")
                .setContentIntent(viewPendingIntent);

        NotificationManagerCompat notificationManager = NotificationManagerCompat
                .from(context);

        notificationManager.notify(notificationId, notificationBuilder.build());

    }

}
