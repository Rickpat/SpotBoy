package rickpat.spotboy.online_activities;


import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
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
import org.osmdroid.util.GeoPoint;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import rickpat.spotboy.R;
import rickpat.spotboy.enums.SpotType;
import rickpat.spotboy.offline_fragments.GalleryItemFragment;
import rickpat.spotboy.spotspecific.Spot;
import rickpat.spotboy.utilities.DB_ACTION;
import rickpat.spotboy.utilities.ScreenSlidePagerAdapter;
import rickpat.spotboy.utilities.Utilities;

import static rickpat.spotboy.utilities.Constants.*;
import static rickpat.spotboy.utilities.SpotBoy_Server_Constants.*;



public class Online_NewActivity extends AppCompatActivity implements View.OnClickListener, Response.ErrorListener, Response.Listener<String> {

    private AlertDialog typeDialog;             //spot type dialog
    private GeoPoint geoPoint;                  //spot coordinates
    private ViewPager mPager;                   //container for images
    private ArrayList<String> urlList;          //paths of takes images
    private ArrayList<String> urlHashList;      //hash list of the image paths to check upload status
    private List<Fragment> viewPagerFragments;  //every fragment takes an image
    private String log = "NEW_ACTIVITY";
    private Spot spot;                          //container for all spot related infomation
    private String googleId;                    //creators googleId
    private ProgressDialog progressDialog;      //shows upload progress

    @Override   //first
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_new);
        setSupportActionBar(toolbar);
        try{
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }catch (NullPointerException e){}
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        Bundle bundle = getIntent().getExtras();
        if (bundle.containsKey(GEOPOINT) && bundle.containsKey( GOOGLE_ID )){
            geoPoint = new Gson().fromJson(bundle.getString(GEOPOINT), GeoPoint.class);
            googleId = bundle.getString(GOOGLE_ID);
            ((TextView)findViewById(R.id.new_spot_lat)).setText(String.valueOf(geoPoint.getLatitude()));
            ((TextView)findViewById(R.id.new_spot_lon)).setText(String.valueOf(geoPoint.getLongitude()));
        }else {
            finish();
        }
        urlList = new ArrayList<>();
        urlHashList = new ArrayList<>();

        findViewById(R.id.new_spot_cat_layout).setOnClickListener(this);
        findViewById(R.id.new_spot_fab_photo).setOnClickListener(this);
        mPager = (ViewPager) findViewById(R.id.new_spot_viewPager);
        viewPagerFragments = new Vector<>(VIEW_PAGER_MAX_FRAGMENTS);
        createDialogs();
        //next onStart
    }

    @Override   //After onCreate
    protected void onStart() {
        super.onStart();
        //next onRestoreInstanceState or onResume
    }

    /*
    * restore values
    * */
    @Override       //After onStart... but not on first start
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        urlList = savedInstanceState.getStringArrayList(URL_LIST);
        urlHashList = savedInstanceState.getStringArrayList(URL_HASH_LIST);
        googleId = savedInstanceState.getString(GOOGLE_ID);
        geoPoint = new Gson().fromJson(savedInstanceState.getString(GEOPOINT, "zero"), GeoPoint.class);
        String spotTypeString = savedInstanceState.getString(PREF_SPOT_TYPE);
        super.onRestoreInstanceState(savedInstanceState);

        ((TextView) findViewById(R.id.new_spot_type_textView)).setText(spotTypeString);
        //next onResume
    }

    @Override
    protected void onResume() {
        super.onResume();
        setViewPagerContent();
        //app's now ready
    }

    @Override
    protected void onPause() {
        super.onPause();
        //next onSaveInstanceState
    }

    /*
    * save values
    * */
    @Override       //After onPause
    protected void onSaveInstanceState(Bundle outState) {
        String spotTypeString = ((TextView) findViewById(R.id.new_spot_type_textView)).getText().toString().trim();

        outState.putString(PREF_SPOT_TYPE,spotTypeString);
        outState.putStringArrayList(URL_LIST, urlList);
        outState.putStringArrayList(URL_HASH_LIST, urlHashList);
        outState.putString(GOOGLE_ID, googleId);
        outState.putString(GEOPOINT, new Gson().toJson(geoPoint));
        super.onSaveInstanceState(outState);
    }

    //----------------

    /*
    * creates the path and file for an image
    * */
    private Uri getOutputMediaFileUri() {
        return Uri.fromFile(getOutputMediaFile());
    }

    /*
    * creates a new directory if necessary
    * */
    private File getOutputMediaFile() {
        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "SBL_PHOTOS");
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                return null;
            }
        }
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.ENGLISH).format(new Date());
        File mediaFile;
        mediaFile = new File(mediaStorageDir.getPath() + File.separator + "IMG_" + timeStamp + ".jpg");
        return mediaFile;
    }

    /*
    * sets te menu items in the toolbar
    * */
    @Override       //After onRestoreInstanceState
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_new, menu);
        return true;
    }

    /*
    * called by toolbar item selection
    *
    * at least one image needed to create a spot
    * */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.action_create:
                if ( urlList.size() > 0 ){
                    showProgressDialog();
                    startUpload();
                }
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /*
    * max VIEW_PAGER_MAX_FRAGMENTS images possible
    * */
    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.new_spot_cat_layout:
                typeDialog.show();
                break;
            case R.id.new_spot_fab_photo:
                if ( urlList.size() < VIEW_PAGER_MAX_FRAGMENTS ){
                    Uri uri = getOutputMediaFileUri();
                    urlList.add(uri.getEncodedPath());
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
                    startActivityForResult(intent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
                }
                break;
        }
    }

    /*
    * called when started camera finishes
    * */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE) {
            if (resultCode != RESULT_OK) {
                Toast.makeText(this,getString(R.string.error_message_img),Toast.LENGTH_LONG).show();
                if ( resultCode == NEW_SPOT_CREATED){
                    Toast.makeText(this,getString(R.string.spot_created_message),Toast.LENGTH_SHORT).show();
                }
            }
        }
    }


    /*
    * For each image a fragment in the view pager
    * An adapter cares about the fragments inside the view pager.
    * */
    private void setViewPagerContent() {
        for ( String url : urlList ){
            Bundle page = new Bundle();
            page.putString(IMG_URL, url);
            viewPagerFragments.add(Fragment.instantiate(this, GalleryItemFragment.class.getName(), page));
        }
        PagerAdapter mPagerAdapter = new ScreenSlidePagerAdapter(getSupportFragmentManager(), viewPagerFragments);
        mPager.setAdapter(mPagerAdapter);
        mPager.invalidate();
    }

    /*
    * creates a list with hash values of the image paths to check if all images are stored on ftp.
    * server receives on every image upload request image data, db id entry and image path hash value
    * and sends - among other things - id and hash value back if the upload was successfully.
    *
    * converts list to set and back to list to avoid duplicate items
    * sets can't take redundant values.
    * */
    private void createHashList() {
        Set<String> stringSet = new HashSet<>();
        stringSet.addAll(urlList);
        urlList.clear();
        urlList.addAll(stringSet);
        for(String urlStr : urlList ){
            urlHashList.add(String.valueOf(urlStr.hashCode()));
        }
    }

    /*
    * initialises upload process
    * first db entry and then images
    * */
    private void startUpload() {
        createHashList();
        String spotTypeString = ((TextView)findViewById(R.id.new_spot_type_textView)).getText().toString().trim();
        SpotType spotType = Utilities.parseSpotTypeString(spotTypeString);
        String notes = ((EditText)findViewById(R.id.new_spot_notes_editText)).getText().toString().trim();
        spot = new Spot(googleId,"",geoPoint,notes,urlList,new Date(),spotType);
        createDBEntry(spot);
    }

    /*
    * volley: create and send db entry request
    * */
    private void createDBEntry( final Spot spot ) {
        StringRequest dbEntryRequest = new StringRequest(Request.Method.POST,PHP_CREATE_DB_ENTRY,this,this){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String,String> params = new HashMap<>();
                params.put(PHP_GOOGLE_ID,spot.getGoogleId());
                params.put(PHP_GEO_POINT,new Gson().toJson(spot.getGeoPoint()));
                params.put(PHP_SPOT_TYPE,spot.getSpotType().toString());
                params.put(PHP_NOTES,spot.getNotes());
                params.put(PHP_IMG_URL_LIST, new Gson().toJson(spot.getUrlList()));
                params.put(PHP_CREATION_TIME, String.valueOf(spot.getDate().getTime()));
                return params;
            }
        };
        Volley.newRequestQueue(this).add(dbEntryRequest);
    }

    /*
    * volley: creates ands sends image upload requests
    *
    * images get resized and converted to strings
    * */
    private void startImgUpload() {
        for (final String imgPath : urlList){
            StringRequest stringRequest = new StringRequest(Request.Method.POST,PHP_UPLOAD_IMAGE,this,this){
                @Override
                protected Map<String, String> getParams() throws AuthFailureError {
                    Map<String,String> params = new HashMap<>();
                    Bitmap bitmap = Utilities.decodeSampledBitmapFromResource(getResources(), imgPath, 400, 400);
                    String image = Utilities.getStringImage(bitmap);
                    String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.ENGLISH).format(new Date());
                    params.put(PHP_IMAGE_NAME, timeStamp + "_" + spot.getGoogleId() );
                    params.put(PHP_ID,spot.getId());
                    params.put(PHP_IMAGE,image);
                    params.put(PHP_IMAGE_HASH, String.valueOf(imgPath.hashCode()));
                    return params;
                }
            };

            Volley.newRequestQueue(this).add(stringRequest);
        }
    }

    /*
    * volley: called by error
    * */
    @Override
    public void onErrorResponse(VolleyError error) {
        Toast.makeText(this,error.getMessage(),Toast.LENGTH_LONG).show();
        progressDialog.dismiss();
    }

    /*
    * volley: response from server
    * */
    @Override
    public void onResponse(String response) {
        try {
            JSONObject jsonResponse = new JSONObject(response);
            String message = jsonResponse.getString(PHP_MESSAGE);
            String action = jsonResponse.getString(PHP_ACTION);
            String id = jsonResponse.getString(PHP_ID);
            boolean success = jsonResponse.getString(PHP_SUCCESS).equalsIgnoreCase("1");

            if ( action.equalsIgnoreCase(DB_ACTION.CREATE_SPOT.toString()) && success){
                /*
                * db entry created starting upload
                * */
                spot.setId(id);
                showImgUploadProgress();
                startImgUpload();

            }else if ( action.equalsIgnoreCase(DB_ACTION.IMAGE_UPLOAD.toString()) && success){
                /*
                * -image uploaded
                * -check and remove hash set entry
                * -update progress bar
                * -show toast if it was the last response
                * */
                String responseImgHash = jsonResponse.getString(PHP_IMAGE_HASH);
                Iterator<String> iterator = urlHashList.iterator();
                while(iterator.hasNext()){
                    String hashString = iterator.next();
                    if ( hashString.equalsIgnoreCase(responseImgHash)){
                        urlHashList.remove(hashString);
                    }
                }

                showImgUploadProgress();

                if (urlHashList.isEmpty()){
                    progressDialog.dismiss();
                    Toast.makeText(this,getString(R.string.spot_created_message),Toast.LENGTH_SHORT).show();
                    /*
                    Intent returnIntent = new Intent();
                    setResult(NEW_SPOT_CREATED, returnIntent);
                    finish();
                    */
                }

            }else {
                /*
                * no success
                * dismissing dialog
                * */
                Log.d(log,response);
                progressDialog.dismiss();

            }
        } catch (JSONException e) {
            /*
            * json error
            * */
            Log.d(log,e.getMessage());
            Log.d(log,response);
        }
    }

    // DIALOGS

    private void createDialogs() {
        String[] spotTypes = Utilities.getSpotTypes();
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        typeDialog = builder.setSingleChoiceItems(spotTypes, 0, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        }).setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                typeDialog.cancel();
            }
        }).setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                ListView listView = ((AlertDialog) dialog).getListView();
                Object object = listView.getAdapter().getItem(listView.getCheckedItemPosition());
                String selection = (String) object;
                ((TextView) findViewById(R.id.new_spot_type_textView)).setText(selection);
            }
        }).create();
    }

    private void showProgressDialog() {
        progressDialog = ProgressDialog.show(this
                , getString(R.string.creating_spot_progressTitle)
                , getString(R.string.creating_db_entry_message),
                true);
    }

    private void showImgUploadProgress() {
        progressDialog.setTitle(getString(R.string.image_upload_started));
        progressDialog.setMessage(urlHashList.size() + " " + getString(R.string.images_left_message));
    }
}

