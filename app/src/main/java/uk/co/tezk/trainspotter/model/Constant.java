package uk.co.tezk.trainspotter.model;

/**
 * Created by tezk on 10/05/17.
 */

public class Constant {
    public enum CURRENT_ACTION {
        INITIALISING, CLASS_LIST, LOG_SPOT, TRAIN_DETAIL, TRAIN_LIST, INVALID;

        public static CURRENT_ACTION fromInteger(int i) {
            switch (i) {
                case 1 :
                    return INITIALISING;
                case 2 :
                    return CLASS_LIST;
                case 3 :
                    return LOG_SPOT;
                case 4 :
                    return TRAIN_DETAIL;
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
                case TRAIN_DETAIL:
                    return 4;
                case TRAIN_LIST:
                    return 5;
            }
            return 0;
        }
    };
    public static final String API_KEY = "gavffvfavaawjihwj0";

    public static final String CURRENT_ACTION_KEY = "current_action";
    public static final String SECONDARY_ACTION_KEY = "secondary_action";
    public static final String TRAIN_SPOTTING_BASE_URL = "http://api.tezk.co.uk:8080/MyTrainApi/train/";
    public static final String CLASS_LIST_API = "classes";
    public static final String TRAIN_LIST_API = "class/{classId}";
    public static final String TRAIN_DETAIL_API = "engine/{classId}/{trainId}";
    public static final String TRAIN_SIGHTING_API = "spotted/{classId}/{trainId}";

    // keys for attribute maps
    public static final int MY_PERMISSIONS_REQUEST_LOCATION_FROM_SPOT = 22;
    public static final int MY_PERMISSIONS_REQUEST_LOCATION_FROM_DETAILS = 23;
    public static final int MY_PERMISSIONS_REQUEST_EXTERNAL_WRITE_FROM_MAIN = 24;
    public static final int MY_PERMISSIONS_REQUEST_EXTERNAL_WRITE_FROM_SPOT = 25;
    public static final String SHOW_CLASS = "1";
    public static final String SHOW_ENGINE = "2";
    public static final String CLASS_NUM_KEY = "classkey";
    public static final String TRAIN_NUM_KEY = "trainkey";

    public static final String FRAG_TAG_DATE_PICKER = "Date picker";

    public static final int REQUEST_IMAGE_CAPTURE_FROM_MAIN = 66;
    public static final int REQUEST_IMAGE_CAPTURE_FROM_SPOT = 68;
    public static final int PICK_IMAGE_FROM_GALLERY = 67;

    public static final String TAKE_PHOTO = "TAKE PHOTO";

    //keys for onInstanceSave of map
    public static final String MAP_VIEW_PARCELABLE_KEY = "MAP_PARCELABLE";
    public static final String ENGINE_NUMBER_KEY = "ENGINE_NO";
    public static final String DATE_RECORDED_KEY = "DATE";
    public static final String IMAGES_KEY = "IMAGES";

    // Result of success from API
    public static final String SUCCESS_MESSAGE = "success";
}
