package rickpat.spotboy.online_activities;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
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

import com.google.gson.Gson;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import java.util.Vector;

import rickpat.spotboy.R;
import rickpat.spotboy.offline_fragments.GalleryItemFragment;
import rickpat.spotboy.restful_broadcast_receiver.DeleteSpotBroadcastReceiver;
import rickpat.spotboy.restful_broadcast_receiver.UpdateSpotBroadcastReceiver;
import rickpat.spotboy.restful_post_services.interfaces.IDelete;
import rickpat.spotboy.restful_post_services.interfaces.IUpdate;
import rickpat.spotboy.restful_post_services.DeleteSpot_Service;
import rickpat.spotboy.restful_post_services.GetSpot_Service;
import rickpat.spotboy.restful_post_services.UpdateSpot_Service;
import rickpat.spotboy.spotspecific.Spot;
import rickpat.spotboy.utilities.ScreenSlidePagerAdapter;
import rickpat.spotboy.utilities.Utilities;

import static rickpat.spotboy.utilities.Constants.GOOGLE_ID;
import static rickpat.spotboy.utilities.Constants.IMG_URL;
import static rickpat.spotboy.utilities.Constants.INFO_ACTIVITY_SPOT_DELETED;
import static rickpat.spotboy.utilities.Constants.INFO_ACTIVITY_SPOT_MODIFIED;
import static rickpat.spotboy.utilities.Constants.NOTIFICATION;
import static rickpat.spotboy.utilities.Constants.SPOT;

import static rickpat.spotboy.utilities.Constants.TIME_FORMAT_INFO;
import static rickpat.spotboy.utilities.Constants.VIEW_PAGER_MAX_FRAGMENTS;

public class Online_InfoActivity extends AppCompatActivity implements View.OnClickListener,IDelete,IUpdate {

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

    private DeleteSpotBroadcastReceiver deleteReceiver;
    private UpdateSpotBroadcastReceiver updateReceiver;


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
        updateReceiver = new UpdateSpotBroadcastReceiver(this);
        deleteReceiver = new DeleteSpotBroadcastReceiver(this);
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

        registerReceiver(updateReceiver, new IntentFilter(NOTIFICATION));
        registerReceiver(deleteReceiver, new IntentFilter(NOTIFICATION));
        //app's ready now
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(deleteReceiver);
        unregisterReceiver(updateReceiver);
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
                        updateSpotTypeRequest(selectedSpotType);
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
                        progressDialog = ProgressDialog.show(Online_InfoActivity.this,getString(R.string.sending),getString(R.string.new_spot_information),false,false);
                        final String notes = editText.getText().toString().trim();
                        createUpdateNotesRequest(notes);
                    }
                }).create();
    }

    private void updateSpotTypeRequest(final String selectedSpotType){
        Log.d(log, "new spot type: " + selectedSpotType + " id: " + spot.getId());
        Intent updateIntent = new Intent(this, UpdateSpot_Service.class);
        updateIntent.putExtra(UpdateSpot_Service.SPOT_ID,spot.getId());
        updateIntent.putExtra(UpdateSpot_Service.SPOT_TYPE,selectedSpotType);
        startService(updateIntent);
    }

    private void createUpdateNotesRequest(final String notes) {
        Log.d(log, "new notes: " + notes + " id: " + spot.getId());
        Intent updateIntent = new Intent(this, UpdateSpot_Service.class);
        updateIntent.putExtra(UpdateSpot_Service.SPOT_ID,spot.getId());
        updateIntent.putExtra(UpdateSpot_Service.NOTES,notes);
        startService(updateIntent);
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
        Intent serviceIntent = new Intent(this, DeleteSpot_Service.class);
        serviceIntent.putExtra(DeleteSpot_Service.SPOT_ID,spot.getId());
        startService(serviceIntent);
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
    public void deleteCallback(boolean success) {
        Log.d(log,"deleteCallback success: " + success);
        if (success){
            setResult(INFO_ACTIVITY_SPOT_DELETED);
            finish();
        }
    }

    /*
    * callback from by IUpdate. called by UpdateSpotBroadcastReceiver
    * */
    @Override
    public void setNotes(String value) {
        progressDialog.dismiss();
        isModified = true;
        spot.setNotes(value);
        ((TextView)findViewById(R.id.info_notesTextView)).setText(spot.getNotes());
    }

    /*
    * callback from by IUpdate. called by UpdateSpotBroadcastReceiver
    * */
    @Override
    public void setSpotType(String value) {
        progressDialog.dismiss();
        isModified = true;
        spot.setSpotType(Utilities.parseSpotTypeString(value));
        ((TextView)findViewById(R.id.info_catTextView)).setText(spot.getSpotType().toString());
    }

    /*
    * callback from by IUpdate. called by UpdateSpotBroadcastReceiver
    * */
    @Override
    public void errorCallback(String message) {
        progressDialog.dismiss();
        Log.d(log,"errorCallback" + message);
    }
}
