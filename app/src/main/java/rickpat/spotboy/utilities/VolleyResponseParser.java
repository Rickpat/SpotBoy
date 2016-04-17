package rickpat.spotboy.utilities;


import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.osmdroid.util.GeoPoint;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import rickpat.spotboy.enums.SpotType;
import rickpat.spotboy.spotspecific.Spot;

import static rickpat.spotboy.utilities.SpotBoy_Server_Constants.*;
import static rickpat.spotboy.utilities.Constants.LIST_TYPE;

public class VolleyResponseParser {
    private VolleyResponseParser(){}
    private static String log  = "VolleyResponseParser";

    public static List<Spot> parseVolleySpotListResponse(JSONObject jsonObject){
        List<Spot> spotList = new ArrayList<>();
        try {
            boolean success = jsonObject.getString(PHP_SUCCESS).equalsIgnoreCase("1");
            String message = jsonObject.getString(PHP_MESSAGE);
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
}
