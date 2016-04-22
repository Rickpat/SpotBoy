package rickpat.spotboy.utilities;


import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.osmdroid.util.GeoPoint;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import rickpat.spotboy.R;
import rickpat.spotboy.enums.SpotType;
import rickpat.spotboy.spotspecific.Spot;

import static rickpat.spotboy.utilities.SpotBoy_Server_Constants.*;

public class VolleyResponseParser {
    private VolleyResponseParser(){}
    private static String log  = "VolleyResponseParser";

    /*
    * parser for volley get spots response
    * calls createSpotList if result contains spots and query was successful
    * */
    public static List<Spot> parseVolleySpotListResponse(JSONObject jsonObject){
        Log.d(log,jsonObject.toString());
        List<Spot> spotList = new ArrayList<>();
        try {
            boolean success = jsonObject.getString(PHP_SUCCESS).equalsIgnoreCase("1");
            String message = jsonObject.getString(PHP_MESSAGE);
            String resultCode = jsonObject.getString(PHP_RESULT_CODE);

            if (success) {
                switch (resultCode) {
                    case PHP_RESULT_SQL_SUCCESS_ITEMS:
                        spotList = createSpotList(jsonObject);
                        break;
                    case PHP_RESULT_SQL_SUCCESS_NO_ITEMS:
                        Log.d(log,message);
                        break;
                    case PHP_RESULT_REQUIRED_FIELDS_MISSING:
                        Log.d(log,message);
                        break;
                }
            }

        } catch (JSONException e) {
            Log.d(log,e.getMessage());
        }
        return spotList;
    }

    private static List<Spot> createSpotList( JSONObject jsonObject){
        List<Spot> spotList = new ArrayList<>();
        try {
            JSONArray spotArray = jsonObject.getJSONArray(PHP_SPOT_ARRAY);
            int size = spotArray.length();
            for ( int i = 0 ; size > i ; i ++){
                JSONObject jsonSpot = spotArray.getJSONObject(i);
                String id = jsonSpot.getString(PHP_ID);
                String googleId = jsonSpot.getString(PHP_GOOGLE_ID);
                GeoPoint geoPoint = new Gson().fromJson(jsonSpot.getString(PHP_GEO_POINT), GeoPoint.class);
                SpotType spotType = Utilities.parseSpotTypeString(jsonSpot.getString(PHP_SPOT_TYPE));
                String notes = jsonSpot.getString(PHP_NOTES);
                List<String> imgURLList = createImgUrlList(jsonSpot.getString(PHP_IMG_URL_LIST));
                Date creationTime = new Date(Long.valueOf(jsonSpot.getString(PHP_CREATION_TIME)));
                Spot spot = new Spot(googleId,id,geoPoint,notes,imgURLList,creationTime,spotType);
                spotList.add(spot);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return spotList;
    }

    private static List<String> createImgUrlList(String string) {
        List<String> imgPathList = new ArrayList<>();
        try {
            JSONArray imgArray = new JSONArray(string);
            Log.d(log,"img array size: " + imgArray.length());
            for ( int i = 0 ; i < imgArray.length(); i ++ ){
                imgPathList.add(imgArray.getString(i));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return imgPathList;
    }

    public static boolean parseDeleteSpotResult(String response) {
        boolean success = false;
        try {
            JSONObject jsonObject = new JSONObject(response);
            success = jsonObject.getString(PHP_SUCCESS).equalsIgnoreCase("1");
            String message = jsonObject.getString(PHP_MESSAGE);
            String resultCode = jsonObject.getString(PHP_RESULT_CODE);
            switch (resultCode){
                case PHP_RESULT_SPOT_DELETED:
                    break;
                case PHP_RESULT_REQUIRED_FIELDS_MISSING:
                    break;
            }


        } catch (JSONException e) {
            e.printStackTrace();
        }
        return success;
    }


    /*
    * @param value = null if not updated
    * */
    public static String parseSpotUpdateResult( String response ){
        String value = null;
        try {
            JSONObject jsonResponse = new JSONObject(response);
            boolean success = jsonResponse.getString(PHP_SUCCESS).equalsIgnoreCase("1");
            String message = jsonResponse.getString(PHP_MESSAGE);
            String resultCode = jsonResponse.getString(PHP_RESULT_CODE);
            String id = jsonResponse.getString(PHP_ID);
            value = jsonResponse.getString(PHP_VALUE);
            Log.d(log,"spot id: " + id + " message: " + message + " success: " + success);
            switch (resultCode){
                case PHP_RESULT_NOTES_UPDATED:
                    Log.d(log,"notes update");
                    break;
                case PHP_RESULT_SPOT_TYPE_UPDATED:
                    Log.d(log,"spot type update");
                    break;
                case PHP_RESULT_REQUIRED_FIELDS_MISSING:
                    Log.d(log,"fields missing");
                    break;
                default:
                    Log.d(log,"unknown result code");
            }

        } catch (JSONException e) {
            Log.d(log,e.getMessage());
        }
        return value;
    }
}
