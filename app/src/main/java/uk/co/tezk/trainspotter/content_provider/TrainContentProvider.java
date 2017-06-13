package uk.co.tezk.trainspotter.content_provider;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.Arrays;
import java.util.HashSet;

import io.realm.Realm;
import io.realm.RealmResults;
import uk.co.tezk.trainspotter.model.TrainListItem;

/**
 * Created by tezk on 06/06/17.
 */

public class TrainContentProvider extends ContentProvider {
    // used for the UriMacher
    private static final int TRAINS = 10;
    private static final int TRAIN_ID = 20;
    private static final int TRAIN_SEARCH = 30;

    public static final String AUTHORITY = "uk.co.tezk.trainspotter.provider";
    public static final String BASE_PATH = "trains";
    public static final String SEARCH = "search";

    public static final Uri BASE_URI = Uri.parse("content://"+AUTHORITY+"/"+BASE_PATH);

    public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE
            + "/trains";
    public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE
            + "/train";

    private static final UriMatcher sURIMatcher = new UriMatcher(
            UriMatcher.NO_MATCH);
    static {
        sURIMatcher.addURI(AUTHORITY, BASE_PATH, TRAINS);
        sURIMatcher.addURI(AUTHORITY, BASE_PATH + "/#", TRAIN_ID);
        sURIMatcher.addURI(AUTHORITY, BASE_PATH + "/" + SEARCH + "/#", TRAIN_SEARCH);
    }

    private String[] availableColumnsString = {
            TrainContent.COLUMN_CLASS_NUM,
            TrainContent.COLUMN_ID,
            TrainContent.COLUMN_TRAIN_NAME,
            TrainContent.COLUMN_TRAIN_NUM};
    HashSet<String> availableColumns = new HashSet<String>(
            Arrays.asList(availableColumnsString));


    @Override
    public boolean onCreate() {

        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {

        // check if the caller has requested a column which does not exists
        checkColumns(projection);

        Realm realm = Realm.getDefaultInstance();
        RealmResults <TrainListItem>results = null;

        int uriType = sURIMatcher.match(uri);
        switch (uriType) {
            case TRAINS:
                results = realm.where(TrainListItem.class).findAll();
                break;
            case TRAIN_ID:
                // adding the ID to the original query
                String[] split = uri.getLastPathSegment().split("-"); // id should be class-train
                if (split==null || split.length<2)
                    throw new IllegalArgumentException("Illegal train id: "+ uri);

                results = realm.where(TrainListItem.class)
                        .contains("number", split[1])
                        .findAll();
                break;
            case TRAIN_SEARCH:
                results = realm.where(TrainListItem.class)
                        .contains("name", uri.getLastPathSegment())
                        .or()
                        .contains("number", uri.getLastPathSegment())
                        .findAll();
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }

        TrainCursor cursor = new TrainCursor();
        for (TrainListItem each : results) {
            Object newRow[] = {
                    each.getClass_()+"-"+each.getNumber(),
                    each.getClass_(),
                    each.getNumber(),
                    each.getName()
            };
            cursor.newRow().add(newRow);
        }
        return cursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        return null;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        return 0;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        return 0;
    }

    private void checkColumns(String[] projection) {
        if (projection != null) {
            HashSet<String> requestedColumns = new HashSet<String>(
                    Arrays.asList(projection));
            // check if all columns which are requested are available
            if (!availableColumns.containsAll(requestedColumns)) {
                throw new IllegalArgumentException(
                        "Unknown columns in projection");
            }
        }
    }
}
