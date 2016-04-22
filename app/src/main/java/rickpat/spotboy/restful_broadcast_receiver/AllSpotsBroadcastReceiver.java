package rickpat.spotboy.restful_broadcast_receiver;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import rickpat.spotboy.restful_post_services.interfaces.IAllSpots;
import rickpat.spotboy.spotspecific.Spot;
import rickpat.spotboy.utilities.VolleyResponseParser;

import static rickpat.spotboy.utilities.Constants.JSON_OBJECT_RESPONSE;

public class AllSpotsBroadcastReceiver extends BroadcastReceiver {

    private IAllSpots callback;

    public AllSpotsBroadcastReceiver( Activity activity ) {
        this.callback = (IAllSpots)activity;
    }

    /*
    * called by GetAllSpots_Service onResponse
    * */
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getExtras().containsKey(JSON_OBJECT_RESPONSE)) {
            String responseString = intent.getExtras().getString(JSON_OBJECT_RESPONSE);
            JSONObject response = null;
            try {
                response = new JSONObject(responseString);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            List<Spot> spotList;
            spotList = VolleyResponseParser.parseVolleySpotListResponse(response);
            callback.setSpotList(spotList);
        } else {
            callback.errorCallback("no value");
        }

    }
}
