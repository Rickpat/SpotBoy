package rickpat.spotboy.utilities;


public class SpotBoy_Server_Constants {
    private SpotBoy_Server_Constants(){}

    public static final String PHP_GET_ALL_SPOTS = "http://rickpat.bplaced.net/SpotBoyPHP/getSpots.php";
    public static final String PHP_GET_SPOT = "http://rickpat.bplaced.net/SpotBoyPHP/getSpot.php";
    public static final String PHP_UPLOAD_IMAGE = "http://rickpat.bplaced.net/SpotBoyPHP/imageUpload.php";
    public static final String PHP_CREATE_DB_ENTRY = "http://rickpat.bplaced.net/SpotBoyPHP/createSpot.php";
    public static final String PHP_UPDATE_DB_ENTRY = "http://rickpat.bplaced.net/SpotBoyPHP/updateSpot.php";
    public static final String PHP_DELETE_DB_ENTRY = "http://rickpat.bplaced.net/SpotBoyPHP/deleteSpot.php";

    public static final String PHP_SUCCESS = "SUCCESS";
    public static final String PHP_MESSAGE = "MESSAGE";
    public static final String PHP_ACTION = "ACTION";
    public static final String PHP_SPOT_ARRAY = "SPOT_ARRAY";

    public static final String PHP_ID = "ID";
    public static final String PHP_GOOGLE_ID = "GOOGLE_ID";
    public static final String PHP_GEO_POINT = "GEO_POINT";
    public static final String PHP_SPOT_TYPE = "SPOT_TYPE";
    public static final String PHP_NOTES = "NOTES";
    public static final String PHP_IMG_URL_LIST = "IMG_URL_LIST";
    public static final String PHP_IMAGE = "IMAGE";
    public static final String PHP_IMAGE_NAME = "PHP_IMAGE_NAME";
    public static final String PHP_IMAGE_HASH = "PHP_IMAGE_HASH";
    public static final String PHP_CREATION_TIME = "CREATION_TIME";

    public static final String PHP_VALUE = "VALUE";
    public static final String PHP_RESULT_CODE = "RESULT_CODE";
    public static final String PHP_RESULT_REQUIRED_FIELDS_MISSING = "100";
    public static final String PHP_RESULT_FTP_ERROR = "102";
    public static final String PHP_RESULT_SQL_ERROR = "103";
    public static final String PHP_RESULT_SQL_SUCCESS = "113";
    public static final String PHP_RESULT_FTP_LOGIN_FAIL = "120";
    //php get spots codes
    public static final String PHP_RESULT_SQL_SUCCESS_ITEMS = "101";
    public static final String PHP_RESULT_SQL_SUCCESS_NO_ITEMS = "102";
    public static final String PHP_SPOT_NOT_FOUND_CODE = "999";
    //create spot codes

    //delete spot codes
    public static final String PHP_RESULT_SPOT_DELETED = "101";

    //image upload result codes
    public static final String PHP_RESULT_IMAGE_STORED = "101";
    public static final String PHP_RESULT_IMAGE_DATABASE_ERROR = "103";
    public static final String PHP_RESULT_IMAGE_FTP_ERROR = "112";
    public static final String PHP_RESULT_IMAGE_REQUIRED_FIELD_MISSING = "100";

    //update spot
    public static final String PHP_RESULT_NOTES_UPDATED = "133";
    public static final String PHP_RESULT_SPOT_TYPE_UPDATED = "144";


}
