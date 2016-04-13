package rickpat.spotboy.offline_activities;


import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import rickpat.spotboy.R;
import rickpat.spotboy.enums.SpotType;
import rickpat.spotboy.offline_database.SpotBoyDBHelper;
import rickpat.spotboy.offline_fragments.GalleryItemFragment;
import rickpat.spotboy.spotspecific.Spot;
import rickpat.spotboy.utilities.ScreenSlidePagerAdapter;
import rickpat.spotboy.utilities.Utilities;

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

import static rickpat.spotboy.utilities.Constants.*;

/* this activity supports multiple image storage up to 3 pictures */

public class Offline_NewActivity extends AppCompatActivity implements View.OnClickListener {

    private String PREF_SPOT_TYPE = "PREF_SPOT_TYPE";
    private String PREF_NOTES = "PREF_NOTES";

    private AlertDialog catDialog;
    private GeoPoint geoPoint;
    private static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 100;
    private String log = "Offline_NewActivity";

    private ViewPager mPager;

    private Set<String> uriSet;
    private List<Fragment> viewPagerFragments;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(log, "onCreate");
        setContentView(R.layout.activity_new);

        uriSet = new HashSet<>();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_new);
        setSupportActionBar(toolbar);
        try{
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }catch (NullPointerException e){}
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        Bundle bundle = getIntent().getExtras();
        if (bundle.containsKey(GEOPOINT)){
            geoPoint = new Gson().fromJson(bundle.getString(GEOPOINT),GeoPoint.class);
            ((TextView)findViewById(R.id.new_spot_lat)).setText(String.valueOf(geoPoint.getLatitude()));
            ((TextView)findViewById(R.id.new_spot_lon)).setText(String.valueOf(geoPoint.getLongitude()));
        }
        findViewById(R.id.new_spot_cat_layout).setOnClickListener(this);
        findViewById(R.id.new_spot_fab_photo).setOnClickListener(this);


        mPager = (ViewPager) findViewById(R.id.new_spot_viewPager);
        createDialogs();
    }

    @Override       //After onStart... but not on first start
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        Log.d(log, "onRestoreInstanceState");
        SharedPreferences preferences = getSharedPreferences(PREFERENCES, MODE_PRIVATE);
        geoPoint = new Gson().fromJson(preferences.getString(GEOPOINT, "zero"), GeoPoint.class);
        ((TextView) findViewById(R.id.new_spot_cat_textView)).setText(preferences.getString(PREF_SPOT_TYPE, ""));
        ((EditText) findViewById(R.id.new_spot_notes_editText)).setText(preferences.getString(PREF_NOTES, ""));
        uriSet = preferences.getStringSet(URI_SET, new HashSet<String>());
        printSet(uriSet);
        setViewPagerContent();
    }

    private void printSet(Set<String> uriSet) {
        for (String anUriSet : uriSet) {
            Log.d(log, "uri: " + anUriSet + " saved");
        }
    }

    @Override       //After onPause
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.d(log, "onSaveInstanceState");
        SharedPreferences preferences = getSharedPreferences(PREFERENCES, MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putStringSet(URI_SET,uriSet);
        editor.putString(PREF_SPOT_TYPE, ((TextView) findViewById(R.id.new_spot_cat_textView)).getText().toString().trim());
        editor.putString(PREF_NOTES, ((EditText) findViewById(R.id.new_spot_notes_editText)).getText().toString().trim());
        editor.putString(GEOPOINT, new Gson().toJson(geoPoint));
        editor.apply();
    }

    @Override   //After onCreate
    protected void onStart() {
        super.onStart();
        Log.d(log, "onStart");
        viewPagerFragments = new Vector<>(VIEW_PAGER_MAX_FRAGMENTS);

        setViewPagerContent();
    }

    private Uri getOutputMediaFileUri() {
        Log.d(log, "getOutputMediaFileUri");
        return Uri.fromFile(getOutputMediaFile());
    }

    private File getOutputMediaFile() {

        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "SBO_PHOTOS");

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

    @Override       //After onRestoreInstanceState
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.d(log,"onCreateOptionsMenu");
        getMenuInflater().inflate(R.menu.menu_new, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.d(log,"onOptionsItemSelected");
        switch (item.getItemId()) {
            case android.R.id.home:
                Log.d(log,"onOptionsItemSelected HOME");
                onBackPressed();
                return true;
            case R.id.action_create:
                long result = createDBEntry();
                //result = id of the created row
                if ( result > 0 ){
                    Log.d(log,"onOptionsMenuItem CREATE OK" );
                    int resultCode = result>0?NEW_SPOT_CREATED:NEW_SPOT_CANCELED;
                    Intent returnIntent = new Intent();
                    setResult(resultCode, returnIntent);
                    finish();
                }else {
                    Toast.makeText(this,getString(R.string.local_db_error_message),Toast.LENGTH_SHORT).show();
                }
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.new_spot_cat_layout:
                catDialog.show();
                break;
            case R.id.new_spot_fab_photo:
                Log.d(log,"onClick FAB");
                if ( uriSet.size() < 3 ){
                    Log.d(log, "adding new uri to list + starting camera");
                    Uri uri = getOutputMediaFileUri();
                    uriSet.add(uri.getEncodedPath());
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);

                    startActivityForResult(intent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
                }else {
                    Log.d(log, "list is full");
                }
                break;
        }
    }

    private long createDBEntry() {
        SpotBoyDBHelper local_db = new SpotBoyDBHelper(getApplicationContext(), null, null, 0);
        String cat = ((TextView)findViewById(R.id.new_spot_cat_textView)).getText().toString().trim();
        SpotType spotType = Utilities.parseSpotTypeString(cat);
        String notes = ((EditText)findViewById(R.id.new_spot_notes_editText)).getText().toString().trim();
        List<String> urlList = new ArrayList<>();
        urlList.addAll(uriSet);
        Spot spotLocal = new Spot("",geoPoint,notes,urlList,new Date(),spotType);

        return local_db.addSpotMultipleImages(spotLocal);
    }

    private void createDialogs() {

        String[] spotTypes = Utilities.getSpotTypes();

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        catDialog = builder.setSingleChoiceItems(spotTypes, 0, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        }).setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                catDialog.cancel();
            }
        }).setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                ListView listView = ((AlertDialog) dialog).getListView();
                Object object = listView.getAdapter().getItem(listView.getCheckedItemPosition());
                String selection = (String) object;
                ((TextView) findViewById(R.id.new_spot_cat_textView)).setText(selection);
            }
        }).create();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(log,"onActivityResult");
        if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE) {
            if (resultCode != RESULT_OK) {
                Toast.makeText(this,getString(R.string.error_message_img),Toast.LENGTH_LONG).show();
                if ( resultCode == NEW_SPOT_CREATED){
                    Toast.makeText(this,getString(R.string.spot_created_message),Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private void setViewPagerContent() {
        Log.d(log,"setViewPagerContent");
        List<String> imgURLList = new ArrayList<>();
        imgURLList.addAll(uriSet);
        for ( String url : imgURLList ){
            Bundle page = new Bundle();
            page.putString(IMG_URL, url);
            Log.d(log,"adding fragment for img: " + url);
            viewPagerFragments.add(Fragment.instantiate(this, GalleryItemFragment.class.getName(), page));
        }

        PagerAdapter mPagerAdapter = new ScreenSlidePagerAdapter(getSupportFragmentManager(), viewPagerFragments);

        mPager.setAdapter(mPagerAdapter);
    }
}

