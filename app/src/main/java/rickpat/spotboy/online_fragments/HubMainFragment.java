package rickpat.spotboy.online_fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import rickpat.spotboy.R;
import rickpat.spotboy.online_activities.Online_InfoActivity;
import rickpat.spotboy.spotspecific.Spot;
import rickpat.spotboy.utilities.SpotBoy_Server_Constants;
import rickpat.spotboy.utilities.SpotHubAdapter;
import rickpat.spotboy.utilities.VolleyResponseParser;

import static rickpat.spotboy.utilities.Constants.*;

/*
* Fragment in the view pager of Online_HubActivity that has a recycler view with card views.
* content loaded from remote spot boy database by volley
* */

public class HubMainFragment extends Fragment implements SpotHubAdapter.IHubAdapter, Response.ErrorListener, Response.Listener<JSONObject>{

    private RecyclerView mRecyclerView;
    private SpotHubAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private String googleId;
    private List<Spot> spotList;
    
    private String log = "HUB_MAIN_FRAGMENT";

    @Override
    public void onPause() {
        super.onPause();
        Log.d(log,"onPause");
        //next onSaveInstanceState
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putString(GOOGLE_ID, googleId);
        super.onSaveInstanceState(outState);
        Log.d(log, "onSaveInstanceState");
        //app's off
    }

    @Override   //first
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        spotList = new ArrayList<>();
        Log.d(log,"onCreate");
    }

    /*
    * creates recycler view with adapter and layout manager
    * */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(log,"onCreateView");
        View rootView = inflater.inflate(R.layout.content_hub_main_fragment, container, false);
        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.hub_main_recycler_view);
        mLayoutManager = new LinearLayoutManager(this.getContext());
        mAdapter = new SpotHubAdapter(spotList,this);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mAdapter);
        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d(log, "onStart");
    }

    @Override
    public void onResume() {
        Log.d(log,"onResume");
        super.onResume();
        loadSpots();
        //app's ready and waiting for response
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        Log.d(log, "onViewCreated");
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        Log.d(log,"onActivityCreated");
        super.onActivityCreated(savedInstanceState);
        //googleId = savedInstanceState.getString(GOOGLE_ID);
    }

    private void loadSpots() {
        Log.d(log,"loadSpots");
        String uri = SpotBoy_Server_Constants.PHP_GET_ALL_SPOTS;
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, uri, null,this,this);
        Volley.newRequestQueue(this.getContext()).add(request);
    }

    @Override
    public void moreButtonCallback(Spot spot) {
        Log.d(log, "moreButtonCallback");
        String googleId = ((IHub)getActivity()).getUserGoogleId();

        Intent infoIntent = new Intent(this.getContext(),Online_InfoActivity.class);
        infoIntent.putExtra(SPOT,new Gson().toJson(spot));
        infoIntent.putExtra(GOOGLE_ID,googleId);
        startActivityForResult(infoIntent, INFO_ACTIVITY_REQUEST);
    }



    @Override
    public void markerButtonCallback(Spot spot) {
        Log.d(log, "markerButtonCallback");
        Intent showMarkerIntent = new Intent();
        showMarkerIntent.putExtra(SPOT, new Gson().toJson(spot));
        getActivity().setResult(HUB_SHOW_ON_MAP, showMarkerIntent);
        getActivity().finish();
    }

    @Override
    public void onErrorResponse(VolleyError error) {
        Log.d(log, error.getMessage());
    }

    @Override
    public void onResponse(JSONObject response) {
        Log.d(log,"onResponse");
        spotList = VolleyResponseParser.parseVolleySpotListResponse(response);
        mAdapter.updateList(spotList);
    }

    /*
    * todo reload specific spot not all
    * */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if ( requestCode == INFO_ACTIVITY_REQUEST) {
            Log.d(log,"INFO_ACTIVITY_REQUEST");

            switch (resultCode){
                case INFO_ACTIVITY_SPOT_DELETED:
                    Log.d(log, "INFO_ACTIVITY_SPOT_DELETED");
                    loadSpots();
                    break;
                case INFO_ACTIVITY_SPOT_MODIFIED:
                    Log.d(log, "INFO_ACTIVITY_SPOT_MODIFIED");
                    loadSpots();
            }
        }
    }
}
