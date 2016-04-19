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

/*
* HubActivity shows all spots from local database in an recycler view with card views
* An Adapter cares about the recyclers content.
* */

public class Offline_HubActivity extends AppCompatActivity implements SpotHubAdapter.IHubAdapter {

    private SpotHubAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hub);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_hub);
        setSupportActionBar(toolbar);
        try{
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }catch (NullPointerException e){}
        getSupportActionBar().setDisplayShowHomeEnabled(true);
    }

    @Override
    protected void onResume() {
        super.onResume();
        setContent();
    }

    private void setContent() {
        SpotBoyDBHelper spotBoyDBHelper = new SpotBoyDBHelper(this, null, null, 1);
        RecyclerView mRecyclerView = (RecyclerView) findViewById(R.id.hub_recycler_view);
        mRecyclerView.setHasFixedSize(true);    // enhances performance
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);
        // cares about the card views in the recycler
        mAdapter = new SpotHubAdapter(spotBoyDBHelper.getSpotListMultipleImages(), this);
        mRecyclerView.setAdapter(mAdapter);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;

        }
        return super.onOptionsItemSelected(item);
    }

    /*
    * called by more button on card view
    * starts InfoActivity
    * */

    @Override
    public void moreButtonCallback(Spot spot) {
        Intent infoIntent = new Intent(this,Offline_InfoActivity.class);
        infoIntent.putExtra(SPOT,new Gson().toJson(spot));
        startActivityForResult(infoIntent, INFO_ACTIVITY_REQUEST);

    }

    /*
    * called by marker button on card view
    * informs MainActivity to show given spot
    * on map and finishes HubActivity.
    * */

    @Override
    public void markerButtonCallback(Spot spot) {
        Intent showMarkerIntent = new Intent();
        showMarkerIntent.putExtra(SPOT,new Gson().toJson(spot));
        setResult(HUB_SHOW_ON_MAP, showMarkerIntent);
        finish();
    }

    /*
    * if necessary HubActivity updates the recycler
    *
    * this can happen when a started InfoActivity deletes or modifies a spot.
    * */

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        boolean update = false;
        if ( requestCode == INFO_ACTIVITY_REQUEST ) {
            switch (resultCode){
                case INFO_ACTIVITY_SPOT_DELETED:
                    update = true;
                    break;
                case INFO_ACTIVITY_SPOT_MODIFIED:
                    update = true;
                    break;
            }
        }
        if (update){
            SpotBoyDBHelper spotBoyDBHelper = new SpotBoyDBHelper(this,null,null,1);
            mAdapter.updateList(spotBoyDBHelper.getSpotListMultipleImages());
            mAdapter.notifyDataSetChanged();
        }
    }
}
