package rickpat.spotboy.restful_broadcast_receiver;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import rickpat.spotboy.restful_post_services.interfaces.IDelete;
import rickpat.spotboy.utilities.VolleyResponseParser;


import static rickpat.spotboy.utilities.Constants.BROADCAST;
import static rickpat.spotboy.utilities.Constants.JSON_OBJECT_RESPONSE;

public class DeleteSpotBroadcastReceiver extends BroadcastReceiver {

    private IDelete callback;
    private String log = "BROADCAST_RECEIVER_DELETE";
    public static String IDENTIFIER = "BROADCAST_RECEIVER_DELETE";

    public DeleteSpotBroadcastReceiver( Activity activity) {
        this.callback = (IDelete)activity;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(log, "onReceive");
        Bundle bundle = intent.getExtras();
        if ( bundle.containsKey(BROADCAST) ) {
            String broadcast = bundle.getString(BROADCAST);
            assert broadcast != null;
            if (bundle.containsKey(JSON_OBJECT_RESPONSE) && broadcast.equalsIgnoreCase(IDENTIFIER)) {
                Log.d(log,"broadcast for " + IDENTIFIER );
                String responseString = intent.getExtras().getString(JSON_OBJECT_RESPONSE);
                boolean success = VolleyResponseParser.parseDeleteSpotResult(responseString);
                callback.deleteCallback(success);
            }else {
                callback.errorCallback("delete request fail");
            }
        }
    }
}
