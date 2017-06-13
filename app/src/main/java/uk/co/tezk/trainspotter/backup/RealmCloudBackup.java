package uk.co.tezk.trainspotter.backup;

import android.app.backup.BackupAgentHelper;
import android.app.backup.FileBackupHelper;
import android.util.Log;

import io.realm.Realm;

/**
 * BackupAgentHelper to backup the realm data file. If the user re-installs the app, this allows them to restore their sightings and images automatically
 */

public class RealmCloudBackup extends BackupAgentHelper {

    private final String filename;

    // A key to uniquely identify the set of backup data
    static final String FILES_BACKUP_KEY = "co.uk.tezk.trainspotter.REALM_BACKUP";


    public RealmCloudBackup() {
        Realm realm = Realm.getDefaultInstance();
        filename = realm.getPath();
        Log.i("RCB", "realm filename = "+filename+", realm = "+realm);
        realm.close();
    }

    @Override
    public void onCreate() {
        Log.i("RCB", "onCreate called - realm = "+filename);
        FileBackupHelper helper = new FileBackupHelper(this, filename);
        addHelper(FILES_BACKUP_KEY, helper);

        super.onCreate();
    }
}
