package rickpat.spotboy.online_activities;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import rickpat.spotboy.R;
import rickpat.spotboy.spotspecific.SpotRemote;
import rickpat.spotboy.utilities.Utilities;

import static rickpat.spotboy.utilities.Constants.GOOGLE_ID;
import static rickpat.spotboy.utilities.Constants.GOOGLE_NAME;
import static rickpat.spotboy.utilities.Constants.INFO_ACTIVITY_SPOT_DELETED;
import static rickpat.spotboy.utilities.Constants.INFO_ACTIVITY_SPOT_MODIFIED;
import static rickpat.spotboy.utilities.Constants.SPOT;

import static rickpat.spotboy.utilities.SpotBoy_Server_URIs.*;

public class Online_InfoActivity extends AppCompatActivity implements View.OnClickListener {

    private SpotRemote spot;

    private String log="Online_InfoActivity";

    private AlertDialog catAlertDialog;
    private AlertDialog notesAlertDialog;
    private String googleId;
    private String googleName;
    private boolean hasAccess = false;
    private boolean isModified = false;
    private ProgressDialog progressDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_info);
        setSupportActionBar(toolbar);
        try{
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }catch (NullPointerException e){}
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        Bundle bundle = getIntent().getExtras();

        if ( bundle.containsKey(SPOT) && bundle.containsKey(GOOGLE_NAME) && bundle.containsKey(GOOGLE_ID) ){
            spot = new Gson().fromJson(bundle.getString(SPOT), SpotRemote.class);
            googleId = bundle.getString(GOOGLE_ID);
            googleName = bundle.getString(GOOGLE_NAME);
            Log.d(log,"id: " + spot.getId() + " spotCreatorGoogleId: " + spot.getGoogleId() + "\nuser googleId: " + googleId + " userName: " + googleName);
        }else {
            finish();
        }

        if (googleId.equalsIgnoreCase(spot.getGoogleId())) {
            Log.d(log, "user has access..setting dialogs");
            hasAccess = true;
        }

        setContent();

        if (hasAccess){setDialogs();}
    }

    private void setContent() {
        ((TextView)findViewById(R.id.info_spotTypeTextView)).setText(spot.getSpotType().toString());
        ((TextView)findViewById(R.id.info_notesTextView)).setText(spot.getNotes());
        if (spot.getUri() != null){
            setImage();
        }

        DateFormat df = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.GERMAN);

        ((TextView)findViewById(R.id.info_dateTextView)).setText(df.format(spot.getDate()));

        if (hasAccess){
            findViewById(R.id.info_cat_fab_edit).setOnClickListener(this);
            findViewById(R.id.info_notes_fab_edit).setOnClickListener(this);
        }else{
            findViewById(R.id.info_cat_fab_edit).setVisibility(View.GONE);
            findViewById(R.id.info_notes_fab_edit).setVisibility(View.GONE);
        }

    }

    private void setImage() {
        Log.d(log, "setImage()");
        int maxW = Utilities.getDeviceWidth(this);
        //Bitmap bitmap = Utilities.decodeSampledBitmapFromResource(getResources(), spot.getUri(), displayW, 500);
        ImageRequest imageRequest = new ImageRequest(spot.getUri(), new Response.Listener<Bitmap>() {
            @Override
            public void onResponse(Bitmap response) {
                Log.d(log, "volley/ onResponse");
                ((ImageView)findViewById(R.id.info_imageView)).setImageBitmap(response);
                findViewById(R.id.info_imageView).setBackground(null);
            }
        }, maxW, 0, ImageView.ScaleType.CENTER, Bitmap.Config.ARGB_8888, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(log, "volley/ onErrorResponse" + error.getMessage());
            }
        });
        Volley.newRequestQueue(this).add(imageRequest);
    }

    private void setDialogs() {
        AlertDialog.Builder catBuilder = new AlertDialog.Builder(this);
        final String[] catItems = Utilities.getSpotTypes();
        int selectedItem = getSelection(catItems);
        catAlertDialog = catBuilder
                .setTitle(getString(R.string.new_cat_alert_title))
                .setSingleChoiceItems(catItems, selectedItem, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Log.d(log, "selection: " + catItems[which]);
                    }
                }).setNegativeButton(getText(R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        catAlertDialog.cancel();
                    }
                }).setPositiveButton(getText(R.string.ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ListView listView = ((AlertDialog) dialog).getListView();
                        Object object = listView.getAdapter().getItem(listView.getCheckedItemPosition());
                        final String selectedSpotType = (String) object;
                        progressDialog = ProgressDialog.show(Online_InfoActivity.this,getString(R.string.sending),getString(R.string.new_spot_information),false,false);
                        StringRequest stringRequest = new StringRequest(Request.Method.POST, PHP_UPDATE_DB_ENTRY, new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                Log.d(log,"response: " + response);
                                progressDialog.dismiss();
                                try {
                                    JSONObject jsonResponse = new JSONObject(response);
                                    boolean success = jsonResponse.getString("success").equalsIgnoreCase("1");
                                    String message = jsonResponse.getString("message");
                                    if (success){
                                        isModified = true;
                                        spot.setSpotType(Utilities.parseSpotTypeString(selectedSpotType));
                                        ((TextView)findViewById(R.id.info_spotTypeTextView)).setText(selectedSpotType);
                                        Toast.makeText(Online_InfoActivity.this,message,Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(Online_InfoActivity.this,getString(R.string.db_error),Toast.LENGTH_SHORT).show();
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        }, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                progressDialog.dismiss();
                                Log.d(log,"volley db error: " + error.getMessage());
                            }
                        }){
                            @Override
                            protected Map<String, String> getParams() throws AuthFailureError {
                                Map<String,String> params = new HashMap<String, String>();
                                Log.d(log,"getParams()... spotType: " + selectedSpotType + " id: " + spot.getId());
                                params.put("spotType",selectedSpotType);
                                params.put("id",spot.getId());
                                return params;
                            }
                        };
                        Volley.newRequestQueue(Online_InfoActivity.this).add(stringRequest);
                    }
                }).create();

        AlertDialog.Builder notesBuilder = new AlertDialog.Builder(this);
        View notesContent = getLayoutInflater().inflate(R.layout.dialog_input, null);
        final EditText editText = (EditText)notesContent.findViewById(R.id.dialog_editText);
        editText.setText(spot.getNotes());
        notesAlertDialog = notesBuilder
                .setTitle(getString(R.string.new_notes_dialog_title))
                .setView(notesContent)
                .setPositiveButton(getText(R.string.ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        final String notes = editText.getText().toString().trim();
                        Log.d(log, "new notes: " + notes);
                        progressDialog = ProgressDialog.show(Online_InfoActivity.this,getString(R.string.sending),getString(R.string.new_spot_information),false,false);
                        StringRequest stringRequest = new StringRequest(Request.Method.POST, PHP_UPDATE_DB_ENTRY, new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                progressDialog.dismiss();
                                Log.d(log,"response: " + response);
                                try {
                                    JSONObject jsonResponse = new JSONObject(response);
                                    boolean success = jsonResponse.getString("success").equalsIgnoreCase("1");
                                    String message = jsonResponse.getString("message");
                                    if (success){
                                        isModified = true;
                                        spot.setNotes(notes);
                                        ((TextView)findViewById(R.id.info_notesTextView)).setText(notes);
                                        Toast.makeText(Online_InfoActivity.this,message,Toast.LENGTH_SHORT).show();
                                    }else {
                                        Toast.makeText(Online_InfoActivity.this,getString(R.string.db_error),Toast.LENGTH_SHORT).show();
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        }, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                progressDialog.dismiss();
                                Log.d(log,"volles db error: " + error.getMessage());
                            }
                        }){
                            @Override
                            protected Map<String, String> getParams() throws AuthFailureError {
                                Map<String,String> params = new HashMap<String, String>();
                                params.put("notes",notes);
                                params.put("id",spot.getId());
                                return params;
                            }
                        };
                        Volley.newRequestQueue(Online_InfoActivity.this).add(stringRequest);
                    }
                }).setNegativeButton(getText(R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        notesAlertDialog.cancel();
                    }
                })
                .create();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_info, menu);
        if (!hasAccess){
            menu.getItem(R.id.action_delete).setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // this takes the user 'back', as if they pressed the left-facing triangle icon on the main android toolbar.
                // if this doesn't work as desired, another possibility is to call `finish()` here.
                if (isModified){
                    setResult(INFO_ACTIVITY_SPOT_MODIFIED);
                }
                onBackPressed();
                return true;
            case R.id.action_delete:
                if (hasAccess){
                    deleteSpot();
                }

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void deleteSpot() {
        StringRequest stringRequest = new StringRequest(Request.Method.POST, PHP_DELETE_DB_ENTRY, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d(log,"delete spot response: " + response);
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    boolean success = jsonObject.getString("success").equalsIgnoreCase("1");
                    if (success){
                        Toast.makeText(Online_InfoActivity.this,getString(R.string.spot_deleted_message),Toast.LENGTH_SHORT).show();
                        setResult(INFO_ACTIVITY_SPOT_DELETED);
                        finish();
                    } else {
                        Toast.makeText(Online_InfoActivity.this,getString(R.string.db_error),Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(log,"onErrorResponse: " + error);
            }
        }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String,String> params = new HashMap<>();
                params.put("id",spot.getId());
                return params;
            }
        };
        Volley.newRequestQueue(this).add(stringRequest);
    }

    public int getSelection(String[] catItems) {
        int help=0;
        for (String str : catItems){
            if (str.equalsIgnoreCase(spot.getSpotType().toString())){
                return help;
            }
            help++;
        }
        return help;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.info_cat_fab_edit:
                catAlertDialog.show();
                break;
            case R.id.info_notes_fab_edit:
                notesAlertDialog.show();
                break;
        }
    }
}
