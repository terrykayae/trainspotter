package uk.co.tezk.trainspotter.firebase;

import android.app.PendingIntent;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;
import java.util.Set;

import uk.co.tezk.trainspotter.MainActivity;
import uk.co.tezk.trainspotter.R;

/**
 * Created by tezk on 19/05/17.
 */

public class TrainspotterFirebaseMessagingService extends FirebaseMessagingService {
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        //super.onMessageReceived(remoteMessage);
        Log.i("TSFMS", "Recevied message from : "+remoteMessage.getFrom());
        Log.i("TSFMS", "Recevied message type : "+remoteMessage.getMessageType());
        Log.i("TSFMS", "body : "+remoteMessage.getNotification().getBody());
        Log.i("TSFMS", ""+remoteMessage.getData());
        Set<String> keySet = remoteMessage.getData().keySet();
        for (String each : keySet) {
            Log.i("TSFMS", "> "+each+" = "+remoteMessage.getData().get(each));
        }
        Log.i("TSFMS", "");

    }

    private void sendNotification(String message, Map data) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /*requestcode*/, intent,PendingIntent.FLAG_ONE_SHOT);

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.train_tracks)
                .setContentTitle("FCM Message")
                .setContentText(message)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent);


    }
}
