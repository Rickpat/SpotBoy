package rickpat.spotboy.restful_post_services;

import android.app.IntentService;
import android.content.Intent;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

import static rickpat.spotboy.utilities.Constants.NOTIFICATION;
import static rickpat.spotboy.utilities.SpotBoy_Server_Constants.PHP_GET_ALL_SPOTS;
import static rickpat.spotboy.utilities.Constants.JSON_OBJECT_RESPONSE;

public class GetAllSpots_Service extends IntentService implements Response.ErrorListener, Response.Listener<JSONObject> {
    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public GetAllSpots_Service(String name) {
        super(name);
    }

    public GetAllSpots_Service() {
        super("GetAllSpots_Service");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST,PHP_GET_ALL_SPOTS,null,this,this);
        Volley.newRequestQueue(this).add(jsonObjectRequest);
    }

    @Override
    public void onErrorResponse(VolleyError error) {

    }

    @Override
    public void onResponse(JSONObject response) {
        publishResults(response);
    }

    /*
    * sends data to broadcast receiver
    * */
    private void publishResults(JSONObject jsonObject) {
        Intent intent = new Intent(NOTIFICATION);
        intent.putExtra(JSON_OBJECT_RESPONSE,jsonObject.toString());
        sendBroadcast(intent);
    }
}

