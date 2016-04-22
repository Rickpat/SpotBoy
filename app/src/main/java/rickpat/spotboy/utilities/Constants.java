package rickpat.spotboy.utilities;

import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;

public class Constants {

    private Constants(){}

    public static final int MAIN_ACTIVITY_SIGNED_REQUEST = 101;
    public static final int MAIN_ACTIVITY_SIGNED_RESULT = 102;
    public static final int MAIN_ACTIVITY_UNSIGNED_REQUEST = 103;

    public static final int NEW_SPOT_REQUEST = 201;
    public static final int NEW_SPOT_CREATED = 202;
    public static final int NEW_SPOT_CANCELED = 203;

    public static final int INFO_ACTIVITY_REQUEST = 301;
    public static final int INFO_ACTIVITY_SPOT_DELETED = 302;
    public static final int INFO_ACTIVITY_SPOT_MODIFIED = 303;

    public static final int HUB_REQUEST = 401;
    public static final int HUB_SHOW_ON_MAP = 402;
    public static final int HUB_MODIFIED_DATASET = 403;

    public static final int KML_REQUEST = 501;
    public static final int KML_RESULT_LOAD = 502;
    public static final int KML_RESULT_CREATE = 503;
    public static final int KML_RESULT_REMOVE = 504;

    public static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 100;

    public static final String GOOGLE_NAME ="GOOGLE_NAME";
    public static final String GOOGLE_ID = "GOOGLE_ID";

    public static String PREFERENCES = "PREFERENCES";
    public static String PREF_SPOT_TYPE = "PREF_SPOT_TYPE";
    public static String PREF_NOTES = "PREF_NOTES";
    public static String GEOPOINT = "GEOPOINT";
    public static String ZOOM_LEVEL = "ZOOM_LEVEL";
    public static String MODIFIED = "MODIFIED";

    public static final String KML_FILE = "KML_FILE";

    public static final String IMG_URL = "IMG_URL";
    public static final String URL_LIST = "URL_LIST";
    public static final String URL_HASH_LIST = "URL_HASH_LIST";
    public static final String SPOT = "SPOT";

    public static final int VIEW_PAGER_MAX_FRAGMENTS = 3;

    public static final String TIME_FORMAT_INFO = "EEE, dd MMM yyyy HH:mm:ss z";

    //FOR SERVICE RESPONSE INTENT FILTER
    public static String NOTIFICATION = "rickpat.spotboy";
    public static String BROADCAST = "BROADCAST";
    public static String JSON_OBJECT_RESPONSE = "JSON_OBJECT_RESPONSE";

}
