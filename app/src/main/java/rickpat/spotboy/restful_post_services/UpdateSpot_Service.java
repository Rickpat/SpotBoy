package rickpat.spotboy.restful_post_services;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import rickpat.spotboy.restful_broadcast_receiver.DeleteSpotBroadcastReceiver;
import rickpat.spotboy.restful_broadcast_receiver.UpdateSpotBroadcastReceiver;

import static rickpat.spotboy.utilities.Constants.BROADCAST;
import static rickpat.spotboy.utilities.Constants.JSON_OBJECT_RESPONSE;
import static rickpat.spotboy.utilities.Constants.NOTIFICATION;
import static rickpat.spotboy.utilities.SpotBoy_Server_Constants.PHP_ID;
import static rickpat.spotboy.utilities.SpotBoy_Server_Constants.PHP_NOTES;
import static rickpat.spotboy.utilities.SpotBoy_Server_Constants.PHP_RESULT_CODE;
import static rickpat.spotboy.utilities.SpotBoy_Server_Constants.PHP_SPOT_TYPE;
import static rickpat.spotboy.utilities.SpotBoy_Server_Constants.PHP_UPDATE_DB_ENTRY;

public class UpdateSpot_Service extends IntentService implements Response.ErrorListener, Response.Listener<String> {

    public static String SPOT_ID = PHP_ID;
    public static String SPOT_TYPE = PHP_SPOT_TYPE;
    public static String NOTES = PHP_NOTES;
    private String log = "UpdateSpot_Service";

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public UpdateSpot_Service(String name) {
        super(name);
    }

    public UpdateSpot_Service() {
        super("UpdateSpot_Service");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Bundle b = intent.getExtras();
        String value;
        String id;
        if ( b.containsKey(SPOT_ID) && b.containsKey(SPOT_TYPE)  ){
            id = b.getString(SPOT_ID);
            value = b.getString(SPOT_TYPE);
            Log.d(log, "new spot type: " + value + " id: " + id);
            sendRequest(id, value,SPOT_TYPE);
        }else if ( b.containsKey(SPOT_ID) && b.containsKey(NOTES) ){
            id = b.getString(SPOT_ID);
            value = b.getString(NOTES);
            Log.d(log, "new notes: " + value + " id: " + id);
            sendRequest(id, value, NOTES);
        }
    }

    private void sendRequest( final String id, final String value, final String column ){
        StringRequest jsonObjectRequest = new StringRequest(Request.Method.POST,PHP_UPDATE_DB_ENTRY,this,this){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String,String> params = new HashMap<>();
                Log.d(log,"column: " + column + " value: "+ value);
                params.put(PHP_ID, id);
                params.put(column,value);
                return params;
            }
        };
        Volley.newRequestQueue(this).add(jsonObjectRequest);
    }

    @Override
    public void onErrorResponse(VolleyError error) {
        Intent intent = new Intent(NOTIFICATION);
        intent.putExtra(BROADCAST, UpdateSpotBroadcastReceiver.IDENTIFIER);
        sendBroadcast(intent);
    }

    @Override
    public void onResponse(String response) {
        String resultCode = null;
        try {
            JSONObject jsonObject = new JSONObject(response);
            resultCode = jsonObject.getString(PHP_RESULT_CODE);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        if ( resultCode != null ){
            Intent intent = new Intent(NOTIFICATION);
            intent.putExtra(JSON_OBJECT_RESPONSE,response);
            intent.putExtra(PHP_RESULT_CODE, resultCode);
            intent.putExtra(BROADCAST, UpdateSpotBroadcastReceiver.IDENTIFIER);
            sendBroadcast(intent);
        }
    }
}
