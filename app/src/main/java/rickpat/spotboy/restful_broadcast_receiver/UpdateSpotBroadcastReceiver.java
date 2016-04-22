package rickpat.spotboy.restful_broadcast_receiver;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import rickpat.spotboy.restful_post_services.interfaces.IUpdate;
import rickpat.spotboy.utilities.VolleyResponseParser;

import static rickpat.spotboy.utilities.Constants.BROADCAST;
import static rickpat.spotboy.utilities.Constants.JSON_OBJECT_RESPONSE;
import static rickpat.spotboy.utilities.SpotBoy_Server_Constants.*;

public class UpdateSpotBroadcastReceiver extends BroadcastReceiver {

    private IUpdate callback;
    public static String IDENTIFIER = "BROADCAST_RECEIVER_UPDATE";

    private String log = "BROADCAST_RECEIVER_UPDATE";

    public UpdateSpotBroadcastReceiver( Activity activity ) {
        callback = (IUpdate)activity;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(log,"onReceive");
        Bundle bundle = intent.getExtras();
        if ( bundle.containsKey(BROADCAST)) {
            String broadcast = bundle.getString(BROADCAST);
            assert broadcast != null;
            if (bundle.containsKey(JSON_OBJECT_RESPONSE) && broadcast.equalsIgnoreCase(IDENTIFIER)) {
                Log.d(log,"broadcast for " + IDENTIFIER );
                String jsonString = bundle.getString(JSON_OBJECT_RESPONSE);
                String resultCode = bundle.getString(PHP_RESULT_CODE);
                String value = VolleyResponseParser.parseSpotUpdateResult(jsonString);
                if (value != null && resultCode != null) {
                    switch (resultCode) {
                        case PHP_RESULT_NOTES_UPDATED:
                            callback.setNotes(value);
                            break;
                        case PHP_RESULT_SPOT_TYPE_UPDATED:
                            callback.setSpotType(value);
                            break;
                        default:
                            callback.errorCallback("unknown PHP result");
                            break;
                    }
                } else {
                    callback.errorCallback(" value == null ");
                }
            }
        }
    }
}
