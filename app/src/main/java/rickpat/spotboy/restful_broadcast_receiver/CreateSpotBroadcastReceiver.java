package rickpat.spotboy.restful_broadcast_receiver;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import rickpat.spotboy.restful_post_services.interfaces.ICreateSpot;
import rickpat.spotboy.utilities.Constants;
import rickpat.spotboy.utilities.DB_ACTION;

import static rickpat.spotboy.utilities.Constants.*;
import static rickpat.spotboy.utilities.SpotBoy_Server_Constants.*;

public class CreateSpotBroadcastReceiver extends BroadcastReceiver {

    private String log = "CreateSpotBroadcastReceiver";
    public static final String BROADCAST = "CREATE_SPOT_BROADCAST_RECEIVER";
    public static final String SPOT_CREATED_RESULT = "SPOT_CREATED";
    public static final String ERROR_RESULT = "CREATE_SPOT_DB_ERROR";
    /*
    *
    * */
    private ICreateSpot callback;

    public CreateSpotBroadcastReceiver(Activity activity) {
        this.callback = (ICreateSpot)activity;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(log, "onReceive");
        Bundle bundle = intent.getExtras();
        //check if its an app broadcast and contains resultCode from service
        if ( bundle.containsKey(Constants.BROADCAST) && bundle.containsKey( SERVICE_RESULT_CODE )) {
            String broadcast = bundle.getString(Constants.BROADCAST);
            String serviceResult = bundle.getString(SERVICE_RESULT_CODE);
            //null check
            if (broadcast != null && serviceResult != null) {
                //check if broadcast is addressed for this receiver
                if (broadcast.equalsIgnoreCase(BROADCAST)) {
                    //check resultCode
                    if (serviceResult.equalsIgnoreCase(SPOT_CREATED_RESULT)) {
                        //spot created
                        spotCreatedResponse();
                    } else {
                        //error
                        errorResponse(bundle);
                    }
                }
            }
        }
    }

    private void errorResponse(Bundle bundle) {
        if ( bundle.containsKey( JSON_OBJECT_RESPONSE)){
            String json = bundle.getString(JSON_OBJECT_RESPONSE);
            assert json != null;
            parseDBError(json);
        }else{
            parseDBError("zero");
        }
    }

    private void parseDBError(String response) {
        try {
            JSONObject jsonResponse = new JSONObject(response);
            String message = jsonResponse.getString(PHP_MESSAGE);
            String action = jsonResponse.getString(PHP_ACTION);
            String resultCode = jsonResponse.getString(PHP_RESULT_CODE);
            if ( action.equalsIgnoreCase(DB_ACTION.CREATE_SPOT.toString())){
                switch (resultCode){
                    case PHP_RESULT_SQL_ERROR:
                        callback.errorCallback("sql error, message: " + message);
                        break;
                    default:
                        callback.errorCallback("unknown resultCode: " + resultCode + " message: " + message);
                }

            }else if ( action.equalsIgnoreCase(DB_ACTION.IMAGE_UPLOAD.toString())){
                switch (resultCode){
                    case PHP_RESULT_SQL_ERROR:
                        callback.errorCallback("sql error, message: " + message);
                        break;
                    case PHP_SPOT_NOT_FOUND_CODE:
                        callback.errorCallback("spot not found code " + message);
                        break;
                    case PHP_RESULT_IMAGE_FTP_ERROR:
                        callback.errorCallback("ftp store error " + message);
                    default:
                        callback.errorCallback("unknown resultCode: " + resultCode + " message: " + message);
                }

            }else {
                Log.d(log, message);
                Log.d(log,resultCode);
                switch (resultCode){
                    //todo DO SOMETHING!!!
                    case PHP_RESULT_FTP_LOGIN_FAIL:

                        break;
                    case PHP_RESULT_SQL_SUCCESS_NO_ITEMS:

                        break;
                    case PHP_RESULT_SQL_ERROR:

                        break;
                    case PHP_RESULT_REQUIRED_FIELDS_MISSING:

                        break;
                    default:
                        Log.d(log,"unknown resultCode: " + resultCode);
                }
                callback.errorCallback(resultCode);

            }
        } catch (JSONException e) {
            callback.errorCallback("json error");
        }
    }

    private void spotCreatedResponse() {
        callback.spotCreatedCallback();
    }
}
