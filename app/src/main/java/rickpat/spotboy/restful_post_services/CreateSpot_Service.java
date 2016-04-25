package rickpat.spotboy.restful_post_services;

import android.app.IntentService;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import rickpat.spotboy.restful_broadcast_receiver.CreateSpotBroadcastReceiver;
import rickpat.spotboy.spotspecific.Spot;
import rickpat.spotboy.utilities.DB_ACTION;
import rickpat.spotboy.utilities.Utilities;

import static rickpat.spotboy.utilities.Constants.BROADCAST;
import static rickpat.spotboy.utilities.Constants.JSON_OBJECT_RESPONSE;
import static rickpat.spotboy.utilities.Constants.NOTIFICATION;
import static rickpat.spotboy.utilities.Constants.SERVICE_RESULT_CODE;
import static rickpat.spotboy.utilities.Constants.SPOT;

import static rickpat.spotboy.utilities.SpotBoy_Server_Constants.*;

public class CreateSpot_Service extends IntentService implements Response.ErrorListener, Response.Listener<String> {

    private String log = "CreateSpot_Service";
    private Spot spot;

    private ArrayList<String> urlHashList;      //hash list of the image paths to check upload status

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public CreateSpot_Service(String name) {
        super(name);
    }

    public CreateSpot_Service() {
        super("CreateSpot_Service");
    }

    /*
    * creates a list with hash values of the image paths to check if all images are stored on ftp.
    * server receives on every image upload request image data, db id entry and image path hash value
    * and sends - among other things - id and hash value back if the upload was successfully.
    * */
    private void createHashList() {
        for(String urlStr : spot.getUrlList() ){
            urlHashList.add(String.valueOf(urlStr.hashCode()));
        }
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d(log, "onHandleIntent");
        urlHashList = new ArrayList<>();

        Bundle bundle = intent.getExtras();
        if (bundle.containsKey(SPOT)){
            spot = new Gson().fromJson(bundle.getString(SPOT),Spot.class);
        }else{
            this.stopSelf();
        }

        createHashList();

        /*
        * first create db entry.
        * */
        StringRequest dbEntryRequest = new StringRequest(Request.Method.POST,PHP_CREATE_DB_ENTRY,this,this){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String,String> params = new HashMap<>();
                params.put(PHP_GOOGLE_ID,spot.getGoogleId());
                params.put(PHP_GEO_POINT,new Gson().toJson(spot.getGeoPoint()));
                params.put(PHP_SPOT_TYPE,spot.getSpotType().toString().toUpperCase());
                params.put(PHP_NOTES,spot.getNotes());
                params.put(PHP_IMG_URL_LIST, new Gson().toJson(spot.getUrlList()));
                params.put(PHP_CREATION_TIME, String.valueOf(spot.getDate().getTime()));
                return params;
            }
        };
        Volley.newRequestQueue(this).add(dbEntryRequest);
    }

    private void startImgUpload() {
        Log.d(log, "startImgUpload");
        for (final String imgPath : spot.getUrlList()){
            StringRequest stringRequest = new StringRequest(Request.Method.POST,PHP_UPLOAD_IMAGE,this,this){
                @Override
                protected Map<String, String> getParams() throws AuthFailureError {
                    Map<String,String> params = new HashMap<>();
                    Bitmap bitmap = Utilities.decodeSampledBitmapFromResource(getResources(), imgPath, 400, 400);
                    String image = Utilities.getStringImage(bitmap);
                    String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.ENGLISH).format(new Date());
                    params.put(PHP_IMAGE_NAME, timeStamp + "_" + spot.getGoogleId() );
                    params.put(PHP_ID,spot.getId());
                    params.put(PHP_IMAGE,image);
                    params.put(PHP_IMAGE_HASH, String.valueOf(imgPath.hashCode()));
                    return params;
                }
            };
            Volley.newRequestQueue(this).add(stringRequest);
        }
    }

    /*
    * Volley
    * */
    @Override
    public void onErrorResponse(VolleyError error) {
        Log.d(log,"volley error: ");
        publishError("volleyMessage:" + error.getMessage() );
    }

    /*
    * Volley
    * */
    @Override
    public void onResponse(String response) {
        try {
            JSONObject jsonResponse = new JSONObject(response);
            String action = jsonResponse.getString(PHP_ACTION);
            String id = jsonResponse.getString(PHP_ID);
            boolean success = jsonResponse.getString(PHP_SUCCESS).equalsIgnoreCase("1");

            if ( action.equalsIgnoreCase(DB_ACTION.CREATE_SPOT.toString())){
                if (success){
                    /*
                    * db entry created starting upload
                    * */
                    spot.setId(id);
                    startImgUpload();
                }else{
                    publishError(response);
                }

            }else if ( action.equalsIgnoreCase(DB_ACTION.IMAGE_UPLOAD.toString())){
                String responseImgHash = jsonResponse.getString(PHP_IMAGE_HASH);
                if(success) {
                    for (int i = urlHashList.size() ; i > 0 ; i-- ){
                        if ( urlHashList.get(i-1).equalsIgnoreCase(responseImgHash) ){
                            urlHashList.remove(i-1);
                        }
                    }
                    if (urlHashList.isEmpty()) {
                        publishSuccessResult();
                    }
                }
            }
        } catch (JSONException e) {
            publishError("json error");
        }
    }

    /*
    * sends data to broadcast receiver
    * */
    private void publishSuccessResult() {
        Intent intent = new Intent(NOTIFICATION);
        intent.putExtra( BROADCAST, CreateSpotBroadcastReceiver.BROADCAST );
        intent.putExtra( SERVICE_RESULT_CODE, CreateSpotBroadcastReceiver.SPOT_CREATED_RESULT );
        sendBroadcast(intent);
    }

    private void publishError( String response ){
        Intent intent = new Intent( NOTIFICATION );
        intent.putExtra( JSON_OBJECT_RESPONSE, response );  //to parse error
        intent.putExtra( BROADCAST, CreateSpotBroadcastReceiver.BROADCAST );
        intent.putExtra( SERVICE_RESULT_CODE, CreateSpotBroadcastReceiver.ERROR_RESULT);
        sendBroadcast(intent);
    }
}
