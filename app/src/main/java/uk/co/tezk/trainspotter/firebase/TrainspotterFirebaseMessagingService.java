package uk.co.tezk.trainspotter.firebase;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

import uk.co.tezk.trainspotter.MainActivity;
import uk.co.tezk.trainspotter.R;
import uk.co.tezk.trainspotter.model.Constant;
import uk.co.tezk.trainspotter.parcel.TrainParcel;

/**
 * Created by tezk on 19/05/17.
 */

public class TrainspotterFirebaseMessagingService extends FirebaseMessagingService {

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        //super.onMessageReceived(remoteMessage);
        Log.i("firebaseMessaging", "message received");
   //     Log.i("TSFMS", "Recevied message from : "+remoteMessage.getFrom());
   //     Log.i("TSFMS", "body : "+remoteMessage.getNotification().getBody());
        Log.i("TSFMS", ""+remoteMessage.getData());
   //     for (String each : keySet) {
   //         Log.i("TSFMS", "> "+each+" = "+remoteMessage.getData().get(each));
   //     }

        sendNotification(remoteMessage.getNotification().getBody(), remoteMessage.getData());
    }

    private void sendNotification(String message, Map data) {
        float latFloat = 0;
        float lonFloat = 0;

        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        TrainParcel trainParcel;
        String latString = (String)data.get("lat");
        Log.i("tfms", "latString = "+latString+", "+latString.getClass());
        if (latString!=null) {
            latFloat = Float.parseFloat(latString);
            Log.i("tfms", "Parsed and got " + latFloat);
        } else {
            Log.e("mess", "Missing lat");
        }
        String lonString = (String)data.get("lon");
        if (lonString!=null) {
            lonFloat = Float.parseFloat(lonString);
        } else {
            Log.e("mess", "Missing lon");
        }
        String trainClass = (String)data.get("class");
        String trainNum = (String)data.get("num");

        if (trainClass == null || trainNum == null || latString == null || lonString == null) {
            Log.i("Mess", "something wasn't received");
            return;
        }

             trainParcel = new TrainParcel(
                     latFloat,
                     lonFloat,
                     trainClass,
                     trainNum
            );

        intent.putExtra(Constant.TRAIN_PARCEL_KEY, trainParcel);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 , intent,PendingIntent.FLAG_ONE_SHOT);

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.train_tracks)
                .setContentTitle("FCM Message")
                .setContentText(message)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(0 , notificationBuilder.build());

    }
}
