package uk.co.tezk.trainspotter.content_provider;

import android.database.MatrixCursor;

import static uk.co.tezk.trainspotter.content_provider.TrainContent.COLUMN_CLASS_NUM;
import static uk.co.tezk.trainspotter.content_provider.TrainContent.COLUMN_ID;
import static uk.co.tezk.trainspotter.content_provider.TrainContent.COLUMN_TRAIN_NAME;
import static uk.co.tezk.trainspotter.content_provider.TrainContent.COLUMN_TRAIN_NUM;

/**
 * Created by tezk on 06/06/17.
 *     public static final String COLUMN_ID = "_id";
 public static final String COLUMN_CLASS_NUM = "class_num";
 public static final String COLUMN_TRAIN_NUM = "train_num";
 public static final String COLUMN_TRAIN_NAME = "train_name";
 */

public class TrainCursor extends MatrixCursor {
    // id, class, number, name
    private static final String [] columnNames = {COLUMN_ID, COLUMN_CLASS_NUM, COLUMN_TRAIN_NUM, COLUMN_TRAIN_NAME};

    public TrainCursor() {
        super(columnNames);
    }


}
