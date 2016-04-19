package rickpat.spotboy.offline_activities;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import rickpat.spotboy.R;
import rickpat.spotboy.offline_database.SpotBoyDBHelper;
import rickpat.spotboy.offline_fragments.GalleryItemFragment;
import rickpat.spotboy.spotspecific.Spot;
import rickpat.spotboy.utilities.ScreenSlidePagerAdapter;
import rickpat.spotboy.utilities.Utilities;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import java.util.Vector;

import static rickpat.spotboy.utilities.Constants.*;

/*
* InfoActivity shows detailed information about the spot.
* It shows the captured images on top in an view pager.
*
* Notes and spot type are editable by AlertDialogs.
*
* */

public class Offline_InfoActivity extends AppCompatActivity implements View.OnClickListener {

    private Spot spot;                      //The received spot
    private boolean modified = false;       //changes to true if the spot content gets edited
    private AlertDialog spotTypeAlertDialog;
    private AlertDialog notesAlertDialog;
    /*
    * The view pager that contains fragments. for each taken photo it creates a fragment
    * which works as a container.
    * */
    private ViewPager mPager;

    @Override
    protected void onPause() {
        super.onPause();
        //next onSaveInstance...
    }

    @Override   //after onPause
    protected void onSaveInstanceState(Bundle outState) {
        outState.putBoolean(MODIFIED,modified);
        super.onSaveInstanceState(outState);
    }

    @Override   //first call
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_info);
        setSupportActionBar(toolbar);
        try{
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }catch (NullPointerException e){}
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        /*The activity stops when there's no given spot to show */
        Bundle bundle = getIntent().getExtras();
        if (bundle.containsKey(SPOT)){
            spot = new Gson().fromJson(bundle.getString(SPOT), Spot.class);
        }else {
            finish();
        }
        mPager = (ViewPager) findViewById(R.id.info_viewPager);
    }

    @Override   //after onCreate or onRestoreInstanceState
    protected void onStart() {
        super.onStart();
        //next onRestoreInstance... or onResume
    }

    @Override   //after onCreate if screen orientation changed
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        modified = savedInstanceState.getBoolean(MODIFIED);
        if (modified){
            SpotBoyDBHelper spotBoyDBHelper = new SpotBoyDBHelper(Offline_InfoActivity.this, null, null, 1);
            spot = spotBoyDBHelper.getMultipleImagesSpot(spot.getId());
        }
        //next onResume
    }

    @Override   //after onStart or onRestoreInstance...
    protected void onResume() {
        super.onResume();
        setContent();
        setDialogs();
        setViewPagerContent();
    }

    private void setContent() {
        ((TextView) findViewById(R.id.info_catTextView)).setText(spot.getSpotType().toString());
        ((TextView)findViewById(R.id.info_notesTextView)).setText(spot.getNotes());
        DateFormat df = new SimpleDateFormat(TIME_FORMAT_INFO, Locale.GERMAN);
        ((TextView)findViewById(R.id.info_dateTextView)).setText(df.format(spot.getDate()));
        findViewById(R.id.info_type_fab_edit).setOnClickListener(this);
        findViewById(R.id.info_notes_fab_edit).setOnClickListener(this);
    }

    private void setDialogs() {
        AlertDialog.Builder catBuilder = new AlertDialog.Builder(this);
        final String[] catItems = Utilities.getSpotTypes();
        int selectedItem = getSelection(catItems);
        spotTypeAlertDialog = catBuilder
                .setTitle(getString(R.string.new_cat_alert_title))
                .setSingleChoiceItems(catItems, selectedItem, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                }).setNegativeButton(getText(R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        spotTypeAlertDialog.cancel();
                    }
                }).setPositiveButton(getText(R.string.ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ListView listView = ((AlertDialog) dialog).getListView();
                        Object object = listView.getAdapter().getItem(listView.getCheckedItemPosition());
                        String selection = (String) object;
                        spot.setSpotType(Utilities.parseSpotTypeString(selection));
                        SpotBoyDBHelper spotBoyDBHelper = new SpotBoyDBHelper(Offline_InfoActivity.this, null, null, 1);
                        long dbResult = spotBoyDBHelper.updateSpotMultipleImages(spot);
                        if (dbResult > 0) {
                            ((TextView) findViewById(R.id.info_catTextView)).setText(selection);
                            modified = true;
                        }
                    }
                }).create();

        AlertDialog.Builder notesBuilder = new AlertDialog.Builder(this);
        View notesContent = getLayoutInflater().inflate(R.layout.dialog_input,null);
        final EditText editText = (EditText)notesContent.findViewById(R.id.dialog_editText);
        editText.setText(spot.getNotes());
        notesAlertDialog = notesBuilder
                .setTitle(getString(R.string.new_notes_dialog_title))
                .setView(notesContent)
                .setPositiveButton(getText(R.string.ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String notes = editText.getText().toString().trim();
                        spot.setNotes(notes);
                        SpotBoyDBHelper spotBoyDBHelper = new SpotBoyDBHelper(Offline_InfoActivity.this, null, null, 1);
                        long dbResult = spotBoyDBHelper.updateSpotMultipleImages(spot);
                        if (dbResult > 0) {
                            ((TextView) findViewById(R.id.info_notesTextView)).setText(notes);
                            modified = true;
                        }
                    }
                }).setNegativeButton(getText(R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        notesAlertDialog.cancel();
                    }
                })
                .create();
    }

    /*
    * For each image a fragment in the view pager
    * An adapter cares about the fragments inside the view pager.
    * */
    private void setViewPagerContent() {
        List<Fragment> viewPagerFragments = new Vector<>(VIEW_PAGER_MAX_FRAGMENTS);
        for ( String url : spot.getUrlList() ){
            Bundle page = new Bundle();
            page.putString(IMG_URL, url);
            viewPagerFragments.add(Fragment.instantiate(this, GalleryItemFragment.class.getName(), page));
        }
        PagerAdapter mPagerAdapter = new ScreenSlidePagerAdapter(getSupportFragmentManager(), viewPagerFragments);
        mPager.setAdapter(mPagerAdapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_info, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if (modified){
                    setResult(INFO_ACTIVITY_SPOT_MODIFIED);
                    finish();
                }
                onBackPressed();
                return true;
            case R.id.action_delete:
                SpotBoyDBHelper spotBoyDBHelper = new SpotBoyDBHelper(Offline_InfoActivity.this, null, null, 1);
                long dbResult = spotBoyDBHelper.deleteSpot(Integer.valueOf(spot.getId()));
                if (dbResult > 0){
                    Toast.makeText(this,getString(R.string.spot_deleted_message),Toast.LENGTH_SHORT).show();
                    setResult(INFO_ACTIVITY_SPOT_DELETED);
                    finish();
                }
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    //a little helper for the spot type dialog
    public int getSelection(String[] typeItems) {
        int help=0;
        for (String type : typeItems){
            if (type.equalsIgnoreCase(spot.getSpotType().toString())){
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
                spotTypeAlertDialog.show();
                break;
            case R.id.info_notes_fab_edit:
                notesAlertDialog.show();
                break;
        }
    }
}