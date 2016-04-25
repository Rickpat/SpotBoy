package rickpat.spotboy.online_activities;


import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
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

import com.android.volley.Response;
import com.google.gson.Gson;

import org.osmdroid.util.GeoPoint;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.Vector;

import rickpat.spotboy.R;
import rickpat.spotboy.enums.SpotType;
import rickpat.spotboy.offline_fragments.GalleryItemFragment;
import rickpat.spotboy.restful_broadcast_receiver.CreateSpotBroadcastReceiver;
import rickpat.spotboy.restful_post_services.CreateSpot_Service;
import rickpat.spotboy.restful_post_services.interfaces.ICreateSpot;
import rickpat.spotboy.spotspecific.Spot;
import rickpat.spotboy.utilities.ScreenSlidePagerAdapter;
import rickpat.spotboy.utilities.Utilities;

import static rickpat.spotboy.utilities.Constants.*;

//// TODO: 4/18/2016 zeige progress dialog bis neuer pfad eingetroffen ist.


public class Online_NewActivity extends AppCompatActivity implements ICreateSpot,View.OnClickListener {

    private AlertDialog typeDialog;             //spot type dialog
    private GeoPoint geoPoint;                  //spot coordinates
    private ViewPager mPager;                   //container for images
    private ArrayList<String> urlList;          //paths of takes images
    private List<Fragment> viewPagerFragments;  //every fragment takes an image
    private String log = "NEW_ACTIVITY";
    private Spot spot;                          //container for all spot related infomation
    private String googleId;                    //creators googleId
    private ProgressDialog progressDialog;      //shows upload progress
    private CreateSpotBroadcastReceiver broadcastReceiver; //receiver for create spot service

    @Override
    protected void onDestroy() {
        unregisterReceiver(broadcastReceiver);
        super.onDestroy();
    }

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
        broadcastReceiver = new CreateSpotBroadcastReceiver(this);

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
        registerReceiver(broadcastReceiver, new IntentFilter(NOTIFICATION));
        //next onRestoreInstanceState or onResume
    }

    /*
    * restore values
    * */
    @Override       //After onStart... but not on first start
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        urlList = savedInstanceState.getStringArrayList(URL_LIST);
        googleId = savedInstanceState.getString(GOOGLE_ID);
        geoPoint = new Gson().fromJson(savedInstanceState.getString(GEOPOINT, "zero"), GeoPoint.class);
        String spotTypeString = savedInstanceState.getString(SPOT_TYPE);
        super.onRestoreInstanceState(savedInstanceState);

        ((TextView) findViewById(R.id.new_spot_type_textView)).setText(spotTypeString);
        //next onResume
    }

    @Override
    protected void onResume() {
        super.onResume();
        setViewPagerContent();
        Log.d(log, urlList.size() + " paths in list");
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

        outState.putString(SPOT_TYPE,spotTypeString);
        outState.putStringArrayList(URL_LIST, urlList);
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
                    createSpot();
                return true;
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
        removeRedundantPaths();
        for ( String url : urlList ){
            Log.d(log,"fragments gets url:" + url);
            Bundle page = new Bundle();
            page.putString(IMG_URL, url);
            viewPagerFragments.add(Fragment.instantiate(this, GalleryItemFragment.class.getName(), page));
        }
        PagerAdapter mPagerAdapter = new ScreenSlidePagerAdapter(getSupportFragmentManager(), viewPagerFragments);
        mPager.setAdapter(mPagerAdapter);
        mPager.invalidate();
    }

    /*
    *
    * */
    private void removeRedundantPaths() {
        Log.d(log, "removeRedundantPaths");
        Log.d(log, "before: " + urlList.size());
        Set<String> stringSet = new HashSet<>();
        stringSet.addAll(urlList);
        urlList.clear();
        urlList.addAll(stringSet);
        Log.d(log, "after: " + urlList.size());
    }

    /*
    * initialises upload process
    * first db entry and then images
    * */
    private void createSpot() {
        String spotTypeString = ((TextView)findViewById(R.id.new_spot_type_textView)).getText().toString().trim();
        SpotType spotType = Utilities.parseSpotTypeString(spotTypeString);
        String notes = ((EditText)findViewById(R.id.new_spot_notes_editText)).getText().toString().trim();
        spot = new Spot(googleId,"",geoPoint,notes,urlList,new Date(),spotType);
        startDBEntryService(spot);
    }

    /*
    * start service to create spot
    * */
    private void startDBEntryService(final Spot spot) {
        Intent serviceIntent = new Intent(this, CreateSpot_Service.class);
        serviceIntent.putExtra(SPOT,new Gson().toJson(spot));
        startService(serviceIntent);
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
                , getString(R.string.please_wait_message),
                true);
    }

    /*
    * ICreateSpot
    * */
    @Override
    public void spotCreatedCallback() {
        Log.d(log,"spotCreatedCallback");
        progressDialog.dismiss();
        setResult(NEW_SPOT_CREATED);
        finish();
    }

    /*
    * ICreateSpot
    * */
    @Override
    public void progressUpdate(String message) {
        Log.d(log,"progressUpdate");
    }

    /*
    * ICreateSpot
    * */
    @Override
    public void errorCallback(String message) {
        Log.d(log,"errorCallback");
        progressDialog.dismiss();
    }
}

