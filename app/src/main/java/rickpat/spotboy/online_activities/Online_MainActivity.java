package rickpat.spotboy.online_activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;

import rickpat.spotboy.activities.AboutActivity;
import rickpat.spotboy.R;
import rickpat.spotboy.activities.KMLActivity;
import rickpat.spotboy.enums.SpotType;
import rickpat.spotboy.osmspecific.MyPositionOverlay;
import rickpat.spotboy.osmspecific.Offline_SpotInfoWindow;
import rickpat.spotboy.osmspecific.Online_SpotInfoWindow;
import rickpat.spotboy.osmspecific.SpotCluster;
import rickpat.spotboy.spotspecific.Spot;
import rickpat.spotboy.spotspecific.SpotMarker;
import rickpat.spotboy.utilities.Utilities;
import rickpat.spotboy.utilities.VolleyResponseParser;

import org.json.JSONObject;
import org.osmdroid.bonuspack.kml.KmlDocument;
import org.osmdroid.bonuspack.overlays.FolderOverlay;
import org.osmdroid.bonuspack.overlays.MapEventsOverlay;
import org.osmdroid.bonuspack.overlays.MapEventsReceiver;
import org.osmdroid.bonuspack.overlays.Marker;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static rickpat.spotboy.utilities.Constants.*;
import static rickpat.spotboy.utilities.SpotBoy_Server_Constants.PHP_GET_ALL_SPOTS;

public class Online_MainActivity extends AppCompatActivity implements MapEventsReceiver, Online_SpotInfoWindow.InfoCallback, Response.ErrorListener, Response.Listener<JSONObject> {

    private String log = "MainActivity_LOG";
    private MyPositionOverlay myPositionOverlay;            //shows the users position
    private MapView map;                                    //manages the map
    private AlertDialog markerDialog;                       //to select the spot layer
    private AlertDialog newMarkerDialog;                    //pops up by long press on map.
    private HashMap<SpotType,SpotCluster> clusterHashMap;   //links spot type to SpotCluster
    private File kmlFile;                                   //a kml file. its default -> null
    private FolderOverlay kmlOverlay;                       //KML layer
    private String googleId;                                //users google id. nothing else needed

    /*
    * saves states
    * */
    @Override   //after onPause
    protected void onSaveInstanceState(Bundle outState) {
        if ( kmlFile != null ) {
            outState.putString(KML_FILE, new Gson().toJson(kmlFile));
        }
        outState.putString(GOOGLE_ID,googleId);
        outState.putString(GEOPOINT, new Gson().toJson(map.getMapCenter()));
        outState.putInt(ZOOM_LEVEL, map.getZoomLevel());
        super.onSaveInstanceState(outState);
        Log.d(log, "onSaveInstanceState");
    }

    /*
    * called if user leaves the activity
    * removes all layer from map to avoid redundancies and turns off GPS
    * */
    @Override   //called by action
    protected void onPause() {
        super.onPause();
        Log.d(log, "onPause");
        closeAllInfoWindows();
        myPositionOverlay.disableMyLocation();
        removeAllClusters();
        removeKMLOverlay();
    }

    @Override   //first call
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(log, "onCreate");
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_main);
        setSupportActionBar(toolbar);
        Bundle bundle = getIntent().getExtras();
        if ( bundle.containsKey(GOOGLE_ID) ){
            googleId = bundle.getString(GOOGLE_ID);
            Log.d(log,googleId);
        }
        clusterHashMap = new HashMap<>();
        setMap();
        setMarkerDialog();
        setFloatingActionButton();
        //next onStart
    }

    @Override   //after onCreate
    protected void onStart() {
        super.onStart();
        Log.d(log, "onStart");
        // next onRestoreInstanceState after e.g. screen orientation changed
        // otherwise onResume
    }


    /*
    * restores states and files
    * */
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        kmlFile = new Gson().fromJson(savedInstanceState.getString(KML_FILE,""),File.class);
        map.getController().setCenter(new Gson().fromJson(savedInstanceState.getString(GEOPOINT, ""), GeoPoint.class));
        map.getController().setZoom(savedInstanceState.getInt(ZOOM_LEVEL, 18));
        googleId = savedInstanceState.getString(GOOGLE_ID,"-1");
        super.onRestoreInstanceState(savedInstanceState);
        Log.d(log, "onRestoreInstanceState");
        //next onResume()
    }

    /*
    * activates GPS
    * creates overlays / layers
    * */
    @Override   //after onStart or onRestoreInstanceState
    protected void onResume() {
        super.onResume();
        Log.d(log, "onResume");
        myPositionOverlay.enableMyLocation();
        loadSpotsFromDatabase();
        createKMLOverlay();
        //now activity is running till onPause
    }

    /*
    * called when user returns from a started activity like
     * - NewActivity
     * - HubActivity
     * - InfoActivity
     * - AboutActivity      // not necessary
     * - SettingsActivity   // not implemented yet
    * */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(log, "onActivityResult");

        if ( requestCode == HUB_REQUEST ){
            switch (resultCode){
                case HUB_SHOW_ON_MAP:
                    Bundle bundle = data.getExtras();
                    if (bundle.containsKey(SPOT)){
                        Spot spot = new Gson().fromJson(bundle.getString(SPOT),Spot.class);
                        Log.d(log,"received spot with id: " + spot.getId());
                        map.getController().animateTo(spot.getGeoPoint());
                    }
                    break;
                case HUB_MODIFIED_DATASET:
                    //todo test
                    break;
            }
        }

        if ( requestCode == INFO_ACTIVITY_REQUEST){
            switch (resultCode){
                case INFO_ACTIVITY_SPOT_MODIFIED:
                    Log.d(log,"spot modified");
                    break;
                case INFO_ACTIVITY_SPOT_DELETED:
                    Log.d(log,"spot deleted");
            }
        }

        if ( requestCode == KML_REQUEST ){
            switch (resultCode){
                case KML_RESULT_LOAD:
                    Log.d(log,"KML_LOAD");
                    Bundle bundle = data.getExtras();
                    if (bundle != null){
                        if (bundle.containsKey(KML_FILE)){
                            kmlFile = new Gson().fromJson(bundle.getString(KML_FILE,""),File.class);
                            Log.d(log,kmlFile.getName() + " parsed to KmlDocument");
                        }
                    }
                    break;
                case KML_RESULT_REMOVE:
                    Log.d(log,"KML_REMOVE");
                    removeKMLOverlay();
                    kmlFile = null;
                    break;
            }
        }
    }

    private void setMarkerDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final String[] items = Utilities.getSpotTypes();
        //todo save and restore selection
        boolean[] selectedItems = new boolean[items.length];
        for (int i = 0 ; i < selectedItems.length ; i++){
            selectedItems[i] = true;
        }
        markerDialog = builder
                .setMultiChoiceItems(items, selectedItems, new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int indexSelected, boolean isChecked) {
                        SpotType spotTypeSelection = Utilities.parseSpotTypeString(items[indexSelected]);
                        if (isChecked) {
                            Log.d(log,"checked item id: " + indexSelected + " value: " + items[indexSelected]);
                            addCluster( spotTypeSelection );
                        } else {
                            removeCluster( spotTypeSelection );
                        }
                    }
                }).setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        markerDialog.cancel();
                    }
                }).create();
    }

    /*
    * removes a SpotCluster from map by given SpotType
    * */
    private void removeCluster(SpotType spotTypeSelection) {
        if( map.getOverlays().contains(clusterHashMap.get(spotTypeSelection)) ){
            map.getOverlays().remove(clusterHashMap.get(spotTypeSelection));
            map.invalidate();
        }
    }

    /*
    * adds a SpotCluster to map by given SpotType
    * */
    private void addCluster(SpotType spotTypeSelection) {
        if (!map.getOverlays().contains(clusterHashMap.get(spotTypeSelection))){
            map.getOverlays().add(clusterHashMap.get(spotTypeSelection));
            map.invalidate();
        }
    }

    /*
    * removes all SpotClusters from map
    * */
    private void removeAllClusters(){
        for (SpotCluster spotCluster : clusterHashMap.values()){
            map.getOverlays().remove(spotCluster);
        }
        clusterHashMap.clear();
    }

    /*
    * called by Volley result
    * creates all SpotMarkers and SpotClusters and adds them to map
    * */
    private void createAllClusters( List<Spot> spotList) {
        if ( spotList.size() > 0 ){
            map.getController().animateTo(spotList.get(spotList.size()-1).getGeoPoint());
        }
        clusterHashMap = new HashMap<>();
        for (SpotType spotType : SpotType.values()){
            SpotCluster spotCluster = new SpotCluster(this);
            spotCluster.setName(spotType + "Cluster");
            spotCluster.setIcon(Utilities.getClusterIcon(getApplicationContext(), spotType));
            clusterHashMap.put(spotType, spotCluster);
        }
        for (Spot spot : spotList){
            Log.d(log, "Spot id: " + spot.getId() + " type: " + spot.getSpotType());
            SpotMarker spotMarker = new SpotMarker(map, spot);
            spotMarker.setIcon(Utilities.getMarkerIcon(getApplicationContext(),spot.getSpotType()));
            spotMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
            spotMarker.setInfoWindow(new Online_SpotInfoWindow(R.layout.info_window,map,this));
            clusterHashMap.get(spot.getSpotType()).add(spotMarker);
        }
        for (SpotCluster spotCluster : clusterHashMap.values()){
            Log.d(log, "cluster: " + spotCluster.getName() + " items: " + spotCluster.getItems().size());
            map.getOverlays().add(spotCluster);
        }
        map.invalidate();
    }

    /*
    * creates the KML overlay / layer if available
    * */
    private void createKMLOverlay() {
        if ( kmlFile != null ) {
            KmlDocument kmlDocument = new KmlDocument();
            kmlDocument.parseKMLFile(kmlFile);
            kmlOverlay = (FolderOverlay) kmlDocument.mKmlRoot.buildOverlay(map, null, null, kmlDocument);
            map.getOverlays().add(kmlOverlay);
            map.invalidate();
            Log.d(log, "kml overlay created");
        }
    }

    /*
    * removes the KML overlay / layer if available
    * */
    private void removeKMLOverlay() {
        if ( kmlFile != null ){
            if (map.getOverlays().contains(kmlOverlay)){
                map.getOverlays().remove(kmlOverlay);
                map.invalidate();
                Log.d(log,"kml overlay removed");
            }
        }
    }

    private void setFloatingActionButton() {
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final GeoPoint geoPoint = myPositionOverlay.getMyLocation();
                if (geoPoint != null){
                    map.getController().animateTo(geoPoint);
                }else{
                    CoordinatorLayout coordinatorLayout = (CoordinatorLayout)findViewById(R.id.main_coordinator);
                    Snackbar.make(coordinatorLayout, getString(R.string.waiting_for_gps_signal_message), Snackbar.LENGTH_SHORT)
                            .show();
                }
            }
        });
        fab.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                markerDialog.show();
                return false;
            }
        });
    }

    /*
    * sets the default zoom level
    * adds a MapEventsOverlay to receive tab events on map
    * */
    private void setMap() {
        map = (MapView) findViewById(R.id.map);
        map.setTileSource(TileSourceFactory.MAPNIK);
        map.setMultiTouchControls(true);
        map.getController().setZoom(15);
        MapEventsOverlay mapEventsOverlay = new MapEventsOverlay(this, this);
        map.getOverlays().add(0, mapEventsOverlay);


        myPositionOverlay = new MyPositionOverlay(this, map);
        myPositionOverlay.enableMyLocation();
        map.getOverlays().add(myPositionOverlay);
    }

    /*
    * creates the toolbar menu
    * */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    /*
    * receives toolbar item selections and starts corresponding activities
    * */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_about:
                Intent intent = new Intent(this,AboutActivity.class);
                startActivity(intent);
                break;
            case R.id.action_hub:
                Intent hubIntent = new Intent(this, Online_HubActivity.class);
                hubIntent.putExtra(GOOGLE_ID,googleId);
                startActivityForResult(hubIntent,HUB_REQUEST);
                break;
            case R.id.action_new:
                GeoPoint geoPoint = myPositionOverlay.getMyLocation();
                if ( geoPoint != null) {
                    Log.d(log, "starting NewActivity with geoPoint: " + geoPoint);
                    Intent newSpotIntent = new Intent(this, Online_NewActivity.class);
                    newSpotIntent.putExtra(GEOPOINT, new Gson().toJson(geoPoint));
                    newSpotIntent.putExtra(GOOGLE_ID,googleId);
                    startActivityForResult(newSpotIntent, NEW_SPOT_REQUEST);
                }
                break;
            case R.id.action_kml:
                Intent kmlIntent = new Intent(this,KMLActivity.class);
                startActivityForResult(kmlIntent,KML_REQUEST );
        }
        return super.onOptionsItemSelected(item);
    }

    /*
    * MapEventsOverlay listener
    * the overlay is on the bottom of the layer stack
    * all other layers are above
    * if a press event happens on the map, the map processes the event from top of the layer stack
    * to bottom. if no other listener grabs the event, it lands here.
    *
    * */
    @Override
    public boolean singleTapConfirmedHelper(GeoPoint p) {
        closeAllInfoWindows();
        return false;
    }

    private void closeAllInfoWindows() {
        for (SpotCluster spotCluster : clusterHashMap.values()){
            for (Marker spotMarker : spotCluster.getItems()){
                spotMarker.closeInfoWindow();
            }
        }
    }

    /*
    * long press event on bottom layer shows newMarkerDialog -> links to NewActivity
    * */
    @Override
    public boolean longPressHelper(final GeoPoint p) {
        closeAllInfoWindows();

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        newMarkerDialog = builder
                .setTitle(getString(R.string.new_marker_alert_message))
                .setNegativeButton(getText(R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        newMarkerDialog.cancel();
                    }
                }).setPositiveButton(getText(R.string.ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (p != null) {
                            Intent newSpotIntent = new Intent(Online_MainActivity.this, Online_NewActivity.class);
                            newSpotIntent.putExtra(GEOPOINT, new Gson().toJson(p));
                            startActivityForResult(newSpotIntent, NEW_SPOT_REQUEST);
                        }
                    }
                }).create();
        newMarkerDialog.show();
        return false;
    }

    /*
    * A callback from SpotInfoWindow to start InfoActivity
    * */
    @Override
    public void infoCallback(Spot spot) {
        Intent infoIntent = new Intent(this,Online_InfoActivity.class);
        infoIntent.putExtra(SPOT,new Gson().toJson(spot));
        infoIntent.putExtra(GOOGLE_ID, googleId);
        startActivityForResult(infoIntent, INFO_ACTIVITY_REQUEST);
    }

    private void loadSpotsFromDatabase() {
        //todo volley request
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST,PHP_GET_ALL_SPOTS,null,this,this);
        Volley.newRequestQueue(this).add(jsonObjectRequest);
    }

    @Override
    public void onErrorResponse(VolleyError error) {
        Log.d(log,"volley error: " + error.getMessage());
    }

    @Override
    public void onResponse(JSONObject response) {
        Log.d(log,"onResonse: " + response);
        List<Spot> spotList;
        spotList = VolleyResponseParser.parseVolleySpotListResponse(response);
        createAllClusters(spotList);
    }
}
