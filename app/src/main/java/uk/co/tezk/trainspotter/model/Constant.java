package uk.co.tezk.trainspotter.model;

/**
 * Created by tezk on 10/05/17.
 */

public class Constant {
    public enum CURRENT_ACTION {
        INITIALISING, CLASS_LIST, LOG_SPOT, TRAIN_INFO, TRAIN_LIST, INVALID;

        public static CURRENT_ACTION fromInteger(int i) {
            switch (i) {
                case 1 :
                    return INITIALISING;
                case 2 :
                    return CLASS_LIST;
                case 3 :
                    return LOG_SPOT;
                case 4 :
                    return TRAIN_INFO;
                case 5 :
                    return TRAIN_LIST;
            }
            return INVALID;
        }
        public static int toInteger(CURRENT_ACTION c) {
            switch (c) {
                case INITIALISING:
                    return 1;
                case CLASS_LIST:
                    return 2;
                case LOG_SPOT:
                    return 3;
                case TRAIN_INFO:
                    return 4;
                case TRAIN_LIST:
                    return 5;
            }
            return 0;
        }
    };
    public static final String API_KEY = "gavffvfavaawjihwj0[";

    public static final String CURRENT_ACTION_KEY = "current_action";
    public static final String TRAIN_SPOTTING_BASE_URL = "http://api.tezk.co.uk:8080/MyTrainApi/train/";
    public static final String CLASS_LIST_API = "classes";
    public static final String TRAIN_LIST_API = "class/{classId}";
    public static final String TRAIN_DETAIL_API = "engine/{classId}/{trainId}";
    public static final String TRAIN_SIGHTING_API = "sighting/{classId}/{trainId}";
}
