package uk.co.tezk.trainspotter.model;

/**
 * Created by tezk on 10/05/17.
 */

public class Constant {
    public enum CURRENT_ACTION {
        INITIALISING, CLASS_LIST, LOG_SPOT, TRAIN_DETAIL, TRAIN_LIST, HISTORY, INVALID;

        public static CURRENT_ACTION fromInteger(int i) {
            switch (i) {
                case 0 :
                    return INITIALISING;
                case 1 :
                    return CLASS_LIST;
                case 2 :
                    return LOG_SPOT;
                case 3 :
                    return TRAIN_DETAIL;
                case 4 :
                    return TRAIN_LIST;
                case 5 :
                    return HISTORY;
            }
            return INVALID;
        }

        public static int toInteger(CURRENT_ACTION c) {
            return c.ordinal();
        }
    };
    public static final String API_KEY = "gavffvfavaawjihwj0";
    public static final String GEOCODER_API_KEY = "2edb0bfa07934ee4988ad7fd12ce3090";

    public static final String CURRENT_ACTION_KEY = "current_action";
    public static final String SECONDARY_ACTION_KEY = "secondary_action";

    public static final String TRAIN_SPOTTING_BASE_URL = "http://api.tezk.co.uk:8080/MyTrainApi/train/";
    public static final String CLASS_LIST_API = "classes";
    public static final String TRAIN_LIST_API = "class/{classId}";
    public static final String TRAIN_DETAIL_API = "engine/{classId}/{trainId}";
    public static final String TRAIN_SIGHTING_API = "spotted/{classId}/{trainId}";


    //http://api.opencagedata.com/geocode/v1/json?q=53.0+-1.2&key=2edb0bfa07934ee4988ad7fd12ce3090
    public static final String GEOCODE_BASE_URL = "http://api.opencagedata.com/";
    public static final String GEOCODE_API = "geocode/v1/json";

    // keys for attribute maps
    public static final int MY_PERMISSIONS_REQUEST_LOCATION_FROM_SPOT = 22;
    public static final int MY_PERMISSIONS_REQUEST_LOCATION_FROM_DETAILS = 23;
    public static final int MY_PERMISSIONS_REQUEST_EXTERNAL_WRITE_FROM_MAIN = 24;
    public static final int MY_PERMISSIONS_REQUEST_EXTERNAL_WRITE_FROM_SPOT = 25;
    public static final String SHOW_CLASS = "1";
    public static final String SHOW_ENGINE = "2";
    public static final String CLASS_NUM_KEY = "classkey";
    public static final String TRAIN_NUM_KEY = "trainkey";
    public static final String TRAIN_PARCEL_KEY = "ptrain";

    public static final String FRAG_TAG_DATE_PICKER = "Date picker";

    public static final int REQUEST_IMAGE_CAPTURE_FROM_MAIN = 66;
    public static final int REQUEST_IMAGE_CAPTURE_FROM_SPOT = 68;
    public static final int PICK_IMAGE_FROM_GALLERY = 67;

    public static final String TAKE_PHOTO = "TAKE_PHOTO";

    //keys for onInstanceSave of map
    public static final String MAP_VIEW_PARCELABLE_KEY = "MAP_PARCELABLE";
    public static final String ENGINE_NUMBER_KEY = "ENGINE_NO";
    public static final String DATE_RECORDED_KEY = "DATE";
    public static final String IMAGES_KEY = "IMAGES";

    // Result of success from API
    public static final String SUCCESS_MESSAGE = "success";

    public static final int DIESEL[][]={{1,70},{96,97}};
    public static final int ELECTRIC[][]={{71,92}};
    public static final int DMU [][]={{121,185}};
    public static final int EMU [][]={{313,508},{507,508},{700,707},{800,801}};
    public static final int DEMU [][]={{220,230}};
    public static final int DVT [][]={{822,823}};

    public static final String DIESEL_TITLE = "Diesel";
    public static final String ELECTRIC_TITLE = "Electric";
    public static final String DMU_TITLE = "Diesel multiple unit";
    public static final String EMU_TITLE = "Electric multiple unit";
    public static final String DEMU_TITLE = "Diesel electric multiple unit";
    public static final String DVT_TITLE = "Driving van trailer";
}
