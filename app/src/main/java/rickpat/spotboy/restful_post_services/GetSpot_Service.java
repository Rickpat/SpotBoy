package rickpat.spotboy.restful_post_services;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import static rickpat.spotboy.utilities.SpotBoy_Server_Constants.PHP_GET_SPOT;
import static rickpat.spotboy.utilities.Constants.NOTIFICATION;

public class GetSpot_Service extends IntentService implements Response.ErrorListener, Response.Listener<String> {
    public static String MESSAGE = "MESSAGE";
    private String log = "GetSpot_Service";

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public GetSpot_Service(String name) {
        super(name);
    }

    public GetSpot_Service() {
        super("GetSpot_Service");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        String message = intent.getExtras().getString(MESSAGE);
        Log.d(log,"onHandleIntent MESSAGE: " + message);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, PHP_GET_SPOT,this,this);


        Volley.newRequestQueue(this).add(stringRequest);
    }

    @Override
    public void onErrorResponse(VolleyError error) {

    }

    @Override
    public void onResponse(String response) {
        Log.d(log,"onResponse response: "+ response);
        publishResults(response,123);
    }

    private void publishResults(String message, int result) {
        Intent intent = new Intent(NOTIFICATION);
        intent.putExtra(MESSAGE, message);
        sendBroadcast(intent);
    }
}
