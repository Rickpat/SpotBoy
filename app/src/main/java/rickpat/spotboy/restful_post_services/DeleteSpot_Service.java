package rickpat.spotboy.restful_post_services;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import rickpat.spotboy.restful_broadcast_receiver.DeleteSpotBroadcastReceiver;

import static rickpat.spotboy.utilities.Constants.BROADCAST;
import static rickpat.spotboy.utilities.Constants.NOTIFICATION;
import static rickpat.spotboy.utilities.Constants.JSON_OBJECT_RESPONSE;
import static rickpat.spotboy.utilities.SpotBoy_Server_Constants.PHP_DELETE_DB_ENTRY;
import static rickpat.spotboy.utilities.SpotBoy_Server_Constants.PHP_ID;

public class DeleteSpot_Service extends IntentService implements Response.ErrorListener, Response.Listener<String> {

    public static String SPOT_ID = "SPOT_ID";
    private String log = "SERVICE_DELETE";

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public DeleteSpot_Service(String name) {
        super(name);
    }

    public DeleteSpot_Service() {
        super("DeleteSpot_Service");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        final String id = intent.getExtras().getString(SPOT_ID);
        if ( id != null ) {
            StringRequest stringRequest = new StringRequest(Request.Method.POST, PHP_DELETE_DB_ENTRY, this, this){
                @Override
                protected Map<String, String> getParams() throws AuthFailureError {
                    Map<String,String> params = new HashMap<>();
                    params.put(PHP_ID, id);
                    return params;
                }
            };
            Volley.newRequestQueue(this).add(stringRequest);
        }
    }

    @Override
    public void onErrorResponse(VolleyError error) {

    }

    @Override
    public void onResponse(String response) {
        publishResults(response);
    }

    /*
    * sends data to broadcast receiver
    * */
    private void publishResults(String response) {
        Log.d(log,"response: " + response);
        Intent intent = new Intent(NOTIFICATION);
        intent.putExtra(JSON_OBJECT_RESPONSE, response);
        intent.putExtra(BROADCAST, DeleteSpotBroadcastReceiver.IDENTIFIER);
        sendBroadcast(intent);
    }
}