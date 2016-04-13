package rickpat.spotboy.utilities;

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

    public static final int KML_ACTIVITY_REQUEST = 501;
    public static final int KML_ACTIVITY_RESULT_LOAD = 502;
    public static final int KML_ACTIVITY_RESULT_CREATE = 503;
    public static final int KML_ACTIVITY_RESULT_REMOVE = 504;

    public static final int KML_LOAD_ACTIVITY_REQUEST = 601;
    public static final int KML_LOAD_ACTIVITY_RESULT = 602;

    public static final String GOOGLE_NAME ="GOOGLE_NAME";
    public static final String GOOGLE_ID = "GOOGLE_ID";

    public static final String PREFERENCES = "PREFERENCES";
    public static final String CACHED_LAST_FIX = "CACHED_LAST_FIX";
    public static final String MODIFIED = "MODIFIED";

    public static final String CACHED_MAP_CAMERA_GEOPOINT = "CACHED_MAP_CAMERA_GEOPOINT";
    public static final String CACHED_MAP_CAMERA_ZOOM = "CACHED_MAP_CAMERA_ZOOM";

    public static final String GEOPOINT = "GEOPOINT";
    public static final String NO_GPS = "NO_GPS";
    public static final String SPOT = "SPOT";
    public static final String TIME_FORMAT = "HH:mm";


    public static final String KML_URL = "KML_URL";
    public static final String KML_FILE = "KML_FILE";

    public static final String URI_SET ="URI_SET";

    public static final String IMG_URL = "IMG_URL";


    public static final String PHP_ACTION_CREATE_SPOT ="CREATE_SPOT";
    public static final String PHP_ACTION_IMAGE_UPLOAD ="IMAGE_UPLOAD";

    public static final int VIEW_PAGER_MAX_FRAGMENTS = 3;

}
