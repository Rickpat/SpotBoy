package rickpat.spotboy.offline_activities;


import android.content.DialogInterface;
import android.content.Intent;
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

/*
 * this activity supports multiple image storage up to as many pictures you want
 * but it's limited to 3 to avoid memory issues
  * */

public class Offline_NewActivity extends AppCompatActivity implements View.OnClickListener {

    private AlertDialog catDialog;
    private GeoPoint geoPoint;
    private ViewPager mPager;
    private ArrayList<String> urlList;
    private List<Fragment> viewPagerFragments;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new);
        urlList = new ArrayList<>();
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
        viewPagerFragments = new Vector<>(VIEW_PAGER_MAX_FRAGMENTS);
        createDialogs();
        //next onStart
    }

    @Override   //After onCreate
    protected void onStart() {
        super.onStart();
        //next onRestoreInstanceState or onResume
    }

    @Override       //After onStart... but not on first start
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        geoPoint = new Gson().fromJson(savedInstanceState.getString(GEOPOINT, "zero"), GeoPoint.class);
        urlList = savedInstanceState.getStringArrayList(URL_LIST);
        ((TextView) findViewById(R.id.new_spot_type_textView)).setText(savedInstanceState.getString(SPOT_TYPE, ""));
        ((EditText) findViewById(R.id.new_spot_notes_editText)).setText(savedInstanceState.getString(NOTES, ""));
        super.onRestoreInstanceState(savedInstanceState);
        //next onResume
    }

    @Override
    protected void onResume() {
        super.onResume();
        setViewPagerContent();
    }

    @Override
    protected void onPause() {
        super.onPause();
        //next onSaveInstanceState
    }

    @Override       //After onPause
    protected void onSaveInstanceState(Bundle outState) {
        outState.putStringArrayList(URL_LIST, urlList);
        outState.putString(SPOT_TYPE, ((TextView) findViewById(R.id.new_spot_type_textView)).getText().toString().trim());
        outState.putString(NOTES, ((EditText) findViewById(R.id.new_spot_notes_editText)).getText().toString().trim());
        outState.putString(GEOPOINT,new Gson().toJson(geoPoint));
        super.onSaveInstanceState(outState);
    }

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
    * */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.action_create:
                long result = createDBEntry();
                /*
                * result = id of the created row
                * at least 1 photo
                * */
                if ( result > 0 ){
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

    private long createDBEntry() {
        SpotBoyDBHelper local_db = new SpotBoyDBHelper(getApplicationContext(), null, null, 0);
        String spotTypeString = ((TextView)findViewById(R.id.new_spot_type_textView)).getText().toString().trim();
        SpotType spotType = Utilities.parseSpotTypeString(spotTypeString);
        String notes = ((EditText)findViewById(R.id.new_spot_notes_editText)).getText().toString().trim();
        List<String> urlList = new ArrayList<>();
        urlList.addAll(this.urlList);
        Spot spot = new Spot("",geoPoint,notes,urlList,new Date(),spotType);
        return local_db.addSpotMultipleImages(spot);
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
                ((TextView) findViewById(R.id.new_spot_type_textView)).setText(selection);
            }
        }).create();
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
        List<String> imgURLList = new ArrayList<>();
        imgURLList.addAll(urlList);
        for ( String url : imgURLList ){
            Bundle page = new Bundle();
            page.putString(IMG_URL, url);
            viewPagerFragments.add(Fragment.instantiate(this, GalleryItemFragment.class.getName(), page));
        }
        PagerAdapter mPagerAdapter = new ScreenSlidePagerAdapter(getSupportFragmentManager(), viewPagerFragments);
        mPager.setAdapter(mPagerAdapter);
    }

    /*
    *
    * */
    private void removeRedundantPaths() {
        Set<String> stringSet = new HashSet<>();
        stringSet.addAll(urlList);
        urlList.clear();
        urlList.addAll(stringSet);
    }
}
