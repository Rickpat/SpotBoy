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
import java.util.Iterator;
import java.util.List;

import rickpat.spotboy.R;
import rickpat.spotboy.online_activities.Online_InfoActivity;
import rickpat.spotboy.spotspecific.Spot;
import rickpat.spotboy.utilities.SpotBoy_Server_Constants;
import rickpat.spotboy.utilities.SpotHubAdapter;
import rickpat.spotboy.utilities.VolleyResponseParser;

import static rickpat.spotboy.utilities.Constants.GOOGLE_ID;
import static rickpat.spotboy.utilities.Constants.GOOGLE_NAME;
import static rickpat.spotboy.utilities.Constants.HUB_SHOW_ON_MAP;
import static rickpat.spotboy.utilities.Constants.INFO_ACTIVITY_REQUEST;
import static rickpat.spotboy.utilities.Constants.INFO_ACTIVITY_SPOT_DELETED;
import static rickpat.spotboy.utilities.Constants.INFO_ACTIVITY_SPOT_MODIFIED;
import static rickpat.spotboy.utilities.Constants.SPOT;

/*
* it's mainly the same as HubMainFragment. The only difference is, that the received list gets filtered by logged googleId
* */

public class HubUserFragment extends Fragment  implements SpotHubAdapter.IHubAdapter,ISpotFragment{

    private SpotHubAdapter mAdapter;
    private List<Spot> spotList;

    private String log = "HUB_USER_FRAGMENT";

    @Override
    public void onPause() {
        super.onPause();
        Log.d(log,"onPause");
        //next onSaveInstanceState
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
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
        View rootView = inflater.inflate(R.layout.content_hub_user_fragment, container, false);
        RecyclerView mRecyclerView = (RecyclerView) rootView.findViewById(R.id.hub_user_recycler_view);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this.getContext());
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
        Log.d(log, "onActivityCreated");
        super.onActivityCreated(savedInstanceState);
    }

    private void loadSpots() {
        spotList = ((IHub)getActivity()).getSpotList();
        filterList();
        mAdapter.updateList(spotList);
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

    private void filterList() {
        String googleId = ((IHub)getActivity()).getUserGoogleId();

        for ( int i = spotList.size(); i > 0 ; i--){
            if (!spotList.get(i-1).getGoogleId().equalsIgnoreCase(googleId)){
                spotList.remove(spotList.get(i));
            }
        }
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
                    ((IHub)getActivity()).updateList();
                    break;
                case INFO_ACTIVITY_SPOT_MODIFIED:
                    Log.d(log, "INFO_ACTIVITY_SPOT_MODIFIED");
                    ((IHub)getActivity()).updateList();
            }
        }
    }

    @Override
    public void contentUpdate() {
        Log.d(log,"contentUpdate");
        loadSpots();
    }
}
