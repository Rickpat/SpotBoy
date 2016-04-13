package rickpat.spotboy.online_activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.provider.Settings;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;

import rickpat.spotboy.AboutActivity;
import rickpat.spotboy.R;
import rickpat.spotboy.enums.SpotType;
import rickpat.spotboy.osmspecific.MyLocationOverlay;
import rickpat.spotboy.osmspecific.SpotCluster;
import rickpat.spotboy.spotspecific.Spot;
import rickpat.spotboy.spotspecific.SpotRemote;
import rickpat.spotboy.spotspecific.SpotInfoWindow;
import rickpat.spotboy.spotspecific.SpotMarker;
import rickpat.spotboy.utilities.SpotBoy_Server_URIs;
import rickpat.spotboy.utilities.Utilities;

import org.json.JSONException;
import org.json.JSONObject;
import org.osmdroid.api.IMapController;
import org.osmdroid.bonuspack.overlays.MapEventsOverlay;
import org.osmdroid.bonuspack.overlays.MapEventsReceiver;
import org.osmdroid.bonuspack.overlays.Marker;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static rickpat.spotboy.utilities.Constants.*;

public class Online_MainActivity extends AppCompatActivity implements MapEventsReceiver, View.OnClickListener,
        MyLocationOverlay.IMyLocationCallback, View.OnLongClickListener, SpotInfoWindow.InfoCallback, Response.ErrorListener, Response.Listener<JSONObject> {

    private String log ="Online_MainActivity";
    private MyLocationOverlay myLocationOverlay;
    private MapView map;
    private Location cachedLastFix;
    private AlertDialog spotLayerDialog;
    private AlertDialog cachedGPSDialog;
    private AlertDialog activateGPSDialog;

    private String googleId;
    private String googleName;

    //takes all spots
    private List<SpotRemote> spotList;
    private HashMap<SpotType, SpotCluster> spotClusterHashMap;


    //todo save and restore layer selection

    @Override   //After onStart... but not on first start
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        SharedPreferences preferences = getSharedPreferences(PREFERENCES, MODE_PRIVATE);
        if(preferences.contains(CACHED_LAST_FIX)){
            cachedLastFix = new Gson().fromJson(preferences.getString(CACHED_LAST_FIX,""),Location.class);
        }
        map.getController().setCenter(new Gson().fromJson(preferences.getString(CACHED_MAP_CAMERA_GEOPOINT, ""), GeoPoint.class));
        map.getController().setZoom(preferences.getInt(CACHED_MAP_CAMERA_ZOOM, 15));

        googleId = preferences.getString(GOOGLE_ID,null);
        googleName = preferences.getString(GOOGLE_NAME,null);
        if (googleName == null || googleId == null){
            finish();
        }

        Log.d(log, "onRestoreInstanceState");
    }

    @Override   //After onPause
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.d(log, "onSaveInstanceState");
        SharedPreferences preferences = getSharedPreferences(PREFERENCES, MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        if ( myLocationOverlay.getLastFix() != null ){
            editor.putString(CACHED_LAST_FIX, new Gson().toJson(myLocationOverlay.getLastFix()));
        }
        editor.putString(CACHED_MAP_CAMERA_GEOPOINT, new Gson().toJson(map.getMapCenter()));
        editor.putInt(CACHED_MAP_CAMERA_ZOOM, map.getZoomLevel());

        editor.putString(GOOGLE_ID,googleId);
        editor.putString(GOOGLE_NAME,googleName);

        editor.apply();
    }

    @Override   //After onCreate
    protected void onStart() {
        super.onStart();
        Log.d(log, "onStart");
        myLocationOverlay.enableMyLocation();
        loadSpots();

        int off = 0;
        try {
            off = Settings.Secure.getInt(getContentResolver(), Settings.Secure.LOCATION_MODE);
            if(off==0){
                activateGPSDialog.show();
            }
        } catch (Settings.SettingNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(log, "onPause");
        myLocationOverlay.disableMyLocation();
        //todo remove //
        //removeAllClusters();
    }

    @Override   //After onRestoreInstanceState
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.action_settings:
                break;
            case R.id.action_about:
                Intent aboutIntent = new Intent(this, AboutActivity.class);
                /*
                * about activity don't care about online or offline mode
                * */
                startActivity(aboutIntent);
                break;
            case R.id.action_hub:
                Intent hubIntent = new Intent(this,Online_HubActivity.class);
                hubIntent.putExtra(GOOGLE_NAME,googleName);
                hubIntent.putExtra(GOOGLE_ID,googleId);
                startActivityForResult(hubIntent, HUB_REQUEST);
                break;
            case R.id.action_new:
                Intent newIntent = new Intent(this,Online_NewActivity.class);
                Location loc;
                if ( myLocationOverlay.getLastFix() != null ){
                    loc = myLocationOverlay.getLastFix();
                }else if(  cachedLastFix != null ){
                    /*
                    * the app caches received gps data so that users don't have to wait for fresh
                    * data after screen rotation.
                    * if the user wants to save a new spot but app hasn't received a gps signal yet
                    * but got cached data, it will ask if it's ok to use the cached file from
                    * preferences to save the spot.
                    * */
                    cachedGPSDialog.show();
                    break;
                }else{
                    Toast.makeText(this,getString(R.string.gps_not_ready_message), Toast.LENGTH_SHORT).show();
                    break;
                }

                newIntent.putExtra(GEOPOINT,new Gson().toJson(new GeoPoint(loc)));
                newIntent.putExtra(GOOGLE_NAME,googleName);
                newIntent.putExtra(GOOGLE_ID,googleId);
                startActivityForResult(newIntent, NEW_SPOT_REQUEST);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar mToolbar = (Toolbar) findViewById(R.id.main_toolbar);
        setSupportActionBar(mToolbar);

        Bundle bundle = getIntent().getExtras();

        if (bundle.containsKey(GOOGLE_ID) && bundle.containsKey(GOOGLE_NAME)){
            googleId = bundle.getString(GOOGLE_ID);
            googleName = bundle.getString(GOOGLE_NAME);
            Log.d(log , "googleId: " + googleId + " googleName: " + googleName);
        }else{
            finish();
        }

        spotList = new ArrayList<>();

        loadSpots();
        initMap();
        setFloatingActionButton();
        createAlertDialogs();
        //next -> onStart
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if ( requestCode == HUB_REQUEST && resultCode == HUB_SHOW_ON_MAP){
            // show spot on map result
            Bundle bundle = data.getExtras();
            if (bundle.containsKey(SPOT)){
                SpotRemote remote = new Gson().fromJson(bundle.getString(SPOT),SpotRemote.class);
                map.getController().animateTo(remote.getGeoPoint());
            }
        }
        if ( requestCode == INFO_ACTIVITY_REQUEST ){
            switch (resultCode){
                case INFO_ACTIVITY_SPOT_DELETED:
                    Log.d(log,"spot deleted");
                    Toast.makeText(this, getString(R.string.spot_deleted_message),Toast.LENGTH_SHORT).show();
                    loadSpots();
                    break;
                case INFO_ACTIVITY_SPOT_MODIFIED:
                    Log.d(log,"spot modified...refreshing spotList");
                    //Toast.makeText(this,getString(R.string.refreshing),Toast.LENGTH_SHORT).show();
                    loadSpots();
                    break;
            }
        }

        if ( requestCode == HUB_REQUEST ){
            Log.d(log,"back from hub");
            switch (resultCode){
                case HUB_MODIFIED_DATASET:
                    Log.d(log,"hub:modified data set...");
                    loadSpots();
                    break;
            }
        }

        if ( requestCode == NEW_SPOT_REQUEST && resultCode == NEW_SPOT_CREATED){
            loadSpots();
        }
    }

    //------------------------------------------------

    private void createAlertDialogs() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final String[] items = Utilities.getSpotTypes();
        boolean[] selectedItems = new boolean[items.length];
        for (int i = 0 ; i < selectedItems.length ; i++){
            selectedItems[i] = true;
        }

        //todo fix selection after screen rotation
        spotLayerDialog = builder
                .setMultiChoiceItems(items, selectedItems, new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int indexSelected, boolean isChecked) {
                        SpotType spotTypeSelection = Utilities.parseSpotTypeString(items[indexSelected]);
                        if (isChecked) {
                            Log.d(log, "checked item id: " + indexSelected + " value: " + items[indexSelected]);
                            addCluster(spotTypeSelection);
                        } else {
                            removeCluster(spotTypeSelection);
                        }
                    }
                }).setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        //markerDialog.cancel();
                    }
                }).create();

        AlertDialog.Builder cachedGPSDialogBuilder = new AlertDialog.Builder(this);
        cachedGPSDialog = cachedGPSDialogBuilder
                .setTitle(getString(R.string.use_cached_gps_question))
                .setNegativeButton(getText(R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        cachedGPSDialog.dismiss();
                    }
                })
                .setPositiveButton(getText(R.string.ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent newIntent = new Intent(Online_MainActivity.this, Online_NewActivity.class);
                        newIntent.putExtra(GOOGLE_NAME,googleName);
                        newIntent.putExtra(GOOGLE_ID,googleId);

                        if (myLocationOverlay.getLastFix() != null) {
                            // if the the device has received a signal in the meanwhile
                            newIntent.putExtra(GEOPOINT, new Gson().toJson(new GeoPoint(myLocationOverlay.getLastFix())));
                            startActivityForResult(newIntent, NEW_SPOT_REQUEST);
                        }else if ( cachedLastFix != null ) {
                            // otherwise...use cached data
                            newIntent.putExtra(GEOPOINT, new Gson().toJson(new GeoPoint(cachedLastFix)));
                            startActivityForResult(newIntent, NEW_SPOT_REQUEST);
                        }
                    }
                })
                .create();

        AlertDialog.Builder activateGPSDialogBuilder = new AlertDialog.Builder(this);
        activateGPSDialog = activateGPSDialogBuilder.setTitle(getString(R.string.activate_gps_dialog_question))
                .setNegativeButton(getText(R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        activateGPSDialog.dismiss();
                    }
                })
                .setPositiveButton(getText(R.string.ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent onGPS = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivity(onGPS);
                    }
                }).create();
    }

    private void setFloatingActionButton() {
        FloatingActionButton fab = (FloatingActionButton)findViewById(R.id.main_fab);
        fab.setOnClickListener(this);
        fab.setOnLongClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.main_fab:
                Log.d(log, "ontouch fab");
                if (myLocationOverlay.getMyLocation() != null){
                    map.getController().animateTo(myLocationOverlay.getMyLocation());
                }
                break;
        }
    }

    @Override
    public boolean onLongClick(View v) {
        if (v.getId() == R.id.main_fab){
            spotLayerDialog.show();
        }
        return false;
    }

    private void initMap() {
        map = (MapView) findViewById(R.id.main_map);
        map.setTileSource(TileSourceFactory.MAPNIK);
        map.setMultiTouchControls(true);

        final IMapController mapController = map.getController();
        mapController.setZoom(4);
        GeoPoint startPoint = new GeoPoint(48.2205994,16.2396321);
        mapController.setCenter(startPoint);
        MapEventsOverlay mapEventsOverlay = new MapEventsOverlay(this, this);
        myLocationOverlay = new MyLocationOverlay(this, map);
        myLocationOverlay.enableMyLocation();

        map.getOverlays().add(mapEventsOverlay);
        map.getOverlays().add(myLocationOverlay);
        map.invalidate();
    }

    @Override
    public void setFixedLocationIcon() {
        ((FloatingActionButton)findViewById(R.id.main_fab))
                .setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_gps_fixed_white_24dp));
    }

    //--CLUSTER

    private void loadSpots() {
        loadRemoteSpots();
    }

    private void loadRemoteSpots() {
        Log.d(log,"loading spots from serves...");
        String uri = SpotBoy_Server_URIs.PHP_GET_ALL_SPOTS;
        JsonObjectRequest request = new JsonObjectRequest( Request.Method.GET, uri, null,this,this);
        Volley.newRequestQueue(this).add(request);
    }

    private void createSpotCluster() {
        Log.d(log, "createSpotCluster");
        createRemoteSpotClusters();
    }

    private void createRemoteSpotClusters() {
        Log.d(log, "createRemoteSpotClusters");
        spotClusterHashMap = new HashMap<>();
        for (SpotType spotType : SpotType.values()){
            SpotCluster spotCluster = new SpotCluster(this);
            spotCluster.setName(spotType.toString());
            spotCluster.setIcon(Utilities.getClusterIcon(getApplicationContext(), spotType));
            spotClusterHashMap.put(spotType, spotCluster);
        }
        for (SpotRemote localSpot : spotList){
            //Log.d(log, "SpotRemote id: " + remote.getId() + " type: " + remote.getSpotType());
            SpotMarker spotMarker = new SpotMarker(map, localSpot);
            spotMarker.setIcon(Utilities.getMarkerIcon(getApplicationContext(), localSpot.getSpotType()));
            spotMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
            SpotInfoWindow infoWindow = new SpotInfoWindow(R.layout.info_window,map,this);
            infoWindow.setIsRemoteSpot(true);
            spotMarker.setInfoWindow(infoWindow);
            spotClusterHashMap.get(localSpot.getSpotType()).add(spotMarker);
        }
        for (SpotCluster spotCluster : spotClusterHashMap.values()){
            //Log.d(log, "cluster: " + spotCluster.getName() + " items: " + spotCluster.getItems().size());
            map.getOverlays().add(spotCluster);
        }
        map.invalidate();
    }

    private void removeAllClusters(){
        Log.d(log,"removeAllClusters (all)");
        for (SpotCluster spotCluster : spotClusterHashMap.values()){
            map.getOverlays().remove(spotCluster);
        }
        spotClusterHashMap.clear();

        map.invalidate();
    }

    private void removeCluster(SpotType spotTypeSelection) {
        Log.d(log, "removeCluster type: " + spotTypeSelection);
        if( map.getOverlays().contains(spotClusterHashMap.get(spotTypeSelection)) ){
            map.getOverlays().remove(spotClusterHashMap.get(spotTypeSelection));
            map.invalidate();
        }
    }

    private void addCluster(SpotType spotTypeSelection) {
        Log.d(log, "addCluster type: " + spotTypeSelection);
        if (!map.getOverlays().contains(spotClusterHashMap.get(spotTypeSelection))){
            map.getOverlays().add(spotClusterHashMap.get(spotTypeSelection));
            map.invalidate();
        }
    }


    @Override
    public void infoCallback(Spot spot) {
        /*
        * a callback from infoWindow class to start InfoXXXActivity* with more information and
        * the possibility to edit.
        *
        * "*" either Offline_InfoActivity or InfoOnlineActivity
        *
        * In this case OnLine_InfoActivity
        * */

        closeAllInfoWindows();
        Intent infoIntent = new Intent(this,Online_InfoActivity.class);
        infoIntent.putExtra(SPOT, new Gson().toJson(spot));
        infoIntent.putExtra(GOOGLE_NAME,googleName);
        infoIntent.putExtra(GOOGLE_ID,googleId);
        startActivityForResult(infoIntent, INFO_ACTIVITY_REQUEST);
    }

    private void closeAllInfoWindows() {
        for (SpotCluster spotCluster : spotClusterHashMap.values()){
            for (Marker spotMarker : spotCluster.getItems()){
                spotMarker.closeInfoWindow();
            }
        }
    }

    @Override   //Touch on Map
    public boolean singleTapConfirmedHelper(GeoPoint p) {
        Log.d(log,"singleTapConfirmedHelper closing all info windows");
        closeAllInfoWindows();
        return false;
    }

    @Override   //Touch on Map
    public boolean longPressHelper(GeoPoint p) {
        Log.d(log, "longPressHelper");
        return false;
    }

    @Override
    public void onErrorResponse(VolleyError error) {
        Log.d(log,"volley error: " + error.getMessage());
    }

    @Override
    public void onResponse(JSONObject response) {
        Log.d(log,"volley response: " + response.toString());
        try {
            String rawSuccess = response.getString("success");
            Log.d(log,"success string: " + rawSuccess);
            switch (rawSuccess){
                case "0":
                    Toast.makeText(this,getString(R.string.remote_db_error_message), Toast.LENGTH_SHORT).show();
                    break;
                case "1":
                    spotList.clear();
                    spotList = Utilities.createSpotListFromJSONResult(response);
                    break;
                case "2":
                    Toast.makeText(this,getString(R.string.no_spot_found_message), Toast.LENGTH_SHORT).show();
                    break;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        createSpotCluster();
    }
}
