package rickpat.spotboy.offline_activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;

import com.google.gson.Gson;

import rickpat.spotboy.R;
import rickpat.spotboy.offline_database.SpotBoyDBHelper;
import rickpat.spotboy.spotspecific.Spot;
import rickpat.spotboy.utilities.SpotHubAdapter;

import static rickpat.spotboy.utilities.Constants.*;

public class Offline_HubActivity extends AppCompatActivity implements SpotHubAdapter.IHubAdapter {

    private SpotHubAdapter mAdapter;
    private String log = "OFFLINE_HUB_ACTIVITY";
    private boolean modified_db = false;


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        SharedPreferences preferences = getSharedPreferences(PREFERENCES,MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(MODIFIED,modified_db);
        modified_db = preferences.getBoolean(MODIFIED, false);
        editor.apply();
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        SharedPreferences preferences = getSharedPreferences(PREFERENCES,MODE_PRIVATE);
        modified_db = preferences.getBoolean(MODIFIED, false);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(log,"onCreate");
        setContentView(R.layout.activity_hub);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_hub);
        setSupportActionBar(toolbar);
        try{
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }catch (NullPointerException e){}
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        SpotBoyDBHelper spotBoyDBHelper = new SpotBoyDBHelper(this, null, null, 1);

        RecyclerView mRecyclerView = (RecyclerView) findViewById(R.id.hub_recycler_view);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mRecyclerView.setHasFixedSize(true);

        // use a linear layout manager
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        // specify an adapter (see also next example)
        mAdapter = new SpotHubAdapter(spotBoyDBHelper.getSpotListMultipleImages(), this);
        mRecyclerView.setAdapter(mAdapter);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                Log.d(log,"home button...modified db: " + modified_db);
                if (modified_db){
                    setResult(HUB_MODIFIED_DATASET);
                    finish();
                }
                // this takes the user 'back', as if they pressed the left-facing triangle icon on the main android toolbar.
                // if this doesn't work as desired, another possibility is to call `finish()` here.
                onBackPressed();
                return true;

        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void moreButtonCallback(Spot remote) {
        Log.d(log,"moreButtonCallback");
        Intent infoIntent = new Intent(this,Offline_InfoActivity.class);
        infoIntent.putExtra(SPOT,new Gson().toJson(remote));
        startActivityForResult(infoIntent, INFO_ACTIVITY_REQUEST);

    }

    @Override
    public void markerButtonCallback(Spot remote) {
        Log.d(log,"markerButtonCallback");
        Intent showMarkerIntent = new Intent();
        showMarkerIntent.putExtra(SPOT,new Gson().toJson(remote));
        setResult(HUB_SHOW_ON_MAP, showMarkerIntent);
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if ( requestCode == INFO_ACTIVITY_REQUEST) {
            Log.d(log,"INFO_ACTIVITY_REQUEST");
            SpotBoyDBHelper spotBoyDBHelper = new SpotBoyDBHelper(this, null, null, 1);
            switch (resultCode){
                case INFO_ACTIVITY_SPOT_DELETED:
                    Log.d(log,"INFO_ACTIVITY_SPOT_DELETED");
                    mAdapter.updateList(spotBoyDBHelper.getSpotListMultipleImages());
                    modified_db = true;
                    break;
                case INFO_ACTIVITY_SPOT_MODIFIED:
                    Log.d(log,"INFO_ACTIVITY_SPOT_MODIFIED");
                    mAdapter.updateList(spotBoyDBHelper.getSpotListMultipleImages());
                    modified_db = true;
            }
        }
    }
}
