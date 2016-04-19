package rickpat.spotboy.online_activities;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Vector;

import rickpat.spotboy.R;
import rickpat.spotboy.offline_fragments.GalleryItemFragment;
import rickpat.spotboy.spotspecific.Spot;
import rickpat.spotboy.utilities.DB_ACTION;
import rickpat.spotboy.utilities.ScreenSlidePagerAdapter;
import rickpat.spotboy.utilities.Utilities;
import rickpat.spotboy.utilities.VolleyResponseParser;

import static rickpat.spotboy.utilities.Constants.GOOGLE_ID;
import static rickpat.spotboy.utilities.Constants.GOOGLE_NAME;
import static rickpat.spotboy.utilities.Constants.IMG_URL;
import static rickpat.spotboy.utilities.Constants.INFO_ACTIVITY_SPOT_DELETED;
import static rickpat.spotboy.utilities.Constants.INFO_ACTIVITY_SPOT_MODIFIED;
import static rickpat.spotboy.utilities.Constants.SPOT;

import static rickpat.spotboy.utilities.Constants.TIME_FORMAT_INFO;
import static rickpat.spotboy.utilities.Constants.VIEW_PAGER_MAX_FRAGMENTS;
import static rickpat.spotboy.utilities.SpotBoy_Server_Constants.*;

public class Online_InfoActivity extends AppCompatActivity implements View.OnClickListener, Response.Listener<String>, Response.ErrorListener {

    private Spot spot;

    private String log="Online_InfoActivity";
    private ViewPager mPager;                   //container for images
    private List<Fragment> viewPagerFragments;  //every fragment takes an image
    private AlertDialog catAlertDialog;
    private AlertDialog notesAlertDialog;
    private String googleId;
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

        if ( bundle.containsKey(SPOT) && bundle.containsKey(GOOGLE_ID) ){
            spot = new Gson().fromJson(bundle.getString(SPOT), Spot.class);
            googleId = bundle.getString(GOOGLE_ID);
            Log.d(log,"id: " + spot.getId() + " spotCreatorGoogleId: " + spot.getGoogleId() + "\nuser googleId: " + googleId );
        }else {
            finish();
        }

        mPager = (ViewPager) findViewById(R.id.info_viewPager);
        viewPagerFragments = new Vector<>(VIEW_PAGER_MAX_FRAGMENTS);
    }

    @Override   //after onCreate
    protected void onStart() {
        super.onStart();
        if (googleId.equalsIgnoreCase(spot.getGoogleId())) {
            Log.d(log, "user has access..setting dialogs");
            hasAccess = true;
        }
        //next onResume or onRestoreInstanceSave
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        googleId = savedInstanceState.getString(GOOGLE_ID);
        spot = new Gson().fromJson(savedInstanceState.getString(SPOT),Spot.class);
        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    protected void onResume() {
        super.onResume();
        setContent();
        setViewPagerContent();
        if (hasAccess){setDialogs();}
        //app's ready now
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putString(GOOGLE_ID, googleId);
        outState.putString(SPOT, new Gson().toJson(spot));
        super.onSaveInstanceState(outState);
    }

    //-----------------------------------

    private void setContent() {
        ((TextView)findViewById(R.id.info_catTextView)).setText(spot.getSpotType().toString());
        ((TextView)findViewById(R.id.info_notesTextView)).setText(spot.getNotes());

        DateFormat df = new SimpleDateFormat(TIME_FORMAT_INFO, Locale.ENGLISH);

        ((TextView)findViewById(R.id.info_dateTextView)).setText(df.format(spot.getDate()));

        if (hasAccess){
            findViewById(R.id.info_type_fab_edit).setOnClickListener(this);
            findViewById(R.id.info_notes_fab_edit).setOnClickListener(this);
        }else{
            findViewById(R.id.info_type_fab_edit).setVisibility(View.GONE);
            findViewById(R.id.info_notes_fab_edit).setVisibility(View.GONE);
        }
    }

    /*
    * For each image, a fragment in the view pager
    * An adapter cares about the fragments inside the view pager.
    * */
    private void setViewPagerContent() {
        for ( String url : spot.getUrlList() ){
            Bundle page = new Bundle();
            page.putString(IMG_URL, url);
            viewPagerFragments.add(Fragment.instantiate(this, GalleryItemFragment.class.getName(), page));
        }
        PagerAdapter mPagerAdapter = new ScreenSlidePagerAdapter(getSupportFragmentManager(), viewPagerFragments);
        mPager.setAdapter(mPagerAdapter);
        mPager.invalidate();
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
                        createSpotTypeRequest(selectedSpotType);
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
                        createUpdateNotesRequest(notes);
                    }
                }).create();
    }

    private void createSpotTypeRequest( final String selectedSpotType ){
        StringRequest stringRequest = new StringRequest(Request.Method.POST, PHP_UPDATE_DB_ENTRY, this,this){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String,String> params = new HashMap<String, String>();
                Log.d(log,"getParams()... spotType: " + selectedSpotType + " id: " + spot.getId());
                params.put(PHP_SPOT_TYPE,selectedSpotType);
                params.put(PHP_ID,spot.getId());
                return params;
            }
        };
        Volley.newRequestQueue(this).add(stringRequest);

    }

    private void createUpdateNotesRequest(final String notes) {
        Log.d(log, "new notes: " + notes);
        progressDialog = ProgressDialog.show(Online_InfoActivity.this,getString(R.string.sending),getString(R.string.new_spot_information),false,false);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, PHP_UPDATE_DB_ENTRY, this,this){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String,String> params = new HashMap<String, String>();
                params.put(PHP_NOTES,notes);
                params.put(PHP_ID,spot.getId());
                return params;
            }
        };
        Volley.newRequestQueue(this).add(stringRequest);
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
        StringRequest stringRequest = new StringRequest(Request.Method.POST, PHP_DELETE_DB_ENTRY, this, this){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String,String> params = new HashMap<>();
                params.put(PHP_ID,spot.getId());
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
            case R.id.info_type_fab_edit:
                catAlertDialog.show();
                break;
            case R.id.info_notes_fab_edit:
                notesAlertDialog.show();
                break;
        }
    }

    @Override
    public void onResponse(String response) {
        progressDialog.dismiss();
        Log.d(log,"delete spot response: " + response);
        String action = "action";
        String resultCode = "000";
        String value = "value";
        try {
            JSONObject jsonObject = new JSONObject(response);
            action = jsonObject.getString(PHP_ACTION);
            resultCode = jsonObject.getString(PHP_RESULT_CODE);
            value = jsonObject.getString(PHP_VALUE);
            Log.d(log,"ACTION: " + action + " RESULT CODE: " + resultCode + " VALUE: " + value);
        } catch (JSONException e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            String exceptionDetails = sw.toString();
            Log.d(log,exceptionDetails);
        }

        if (action.equalsIgnoreCase(DB_ACTION.DELETE_SPOT.toString())) {
            if (VolleyResponseParser.parseDeleteSpotResult(response)) {
                setResult(INFO_ACTIVITY_SPOT_DELETED);
                finish();
            }
        }else if( action.equalsIgnoreCase(DB_ACTION.UPDATE_SPOT.toString())){
            if (VolleyResponseParser.parseSpotUpdateResult(response)) {
                isModified = true;
                Log.d(log,"update content");
                switch (resultCode) {
                    case PHP_RESULT_NOTES_UPDATED:
                        spot.setNotes(value);
                        ((TextView)findViewById(R.id.info_notesTextView)).setText(spot.getNotes());
                        break;
                    case PHP_RESULT_SPOT_TYPE_UPDATED:
                        spot.setSpotType(Utilities.parseSpotTypeString(value));
                        ((TextView)findViewById(R.id.info_catTextView)).setText(spot.getSpotType().toString());
                        break;
                }
            }
        }
    }

    @Override
    public void onErrorResponse(VolleyError error) {
        Log.d(log,"onErrorResponse: " + error);
    }
}
