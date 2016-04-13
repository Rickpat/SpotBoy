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
import rickpat.spotboy.utilities.SpotBoy_Server_URIs;
import rickpat.spotboy.utilities.SpotHubAdapter;
import rickpat.spotboy.utilities.Utilities;

import static rickpat.spotboy.utilities.Constants.*;

public class HubMainFragment extends Fragment implements SpotHubAdapter.IHubAdapter, Response.ErrorListener, Response.Listener<JSONObject>{

    protected RecyclerView mRecyclerView;
    protected SpotHubAdapter mAdapter;
    protected RecyclerView.LayoutManager mLayoutManager;
    protected List<Spot> spotList;
    private String log = "HUB_MAIN_FRAGMENT";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        spotList = new ArrayList<>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.content_hub_main_fragment, container, false);
        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.hub_main_recycler_view);

        mLayoutManager = new LinearLayoutManager(this.getContext());

        mAdapter = new SpotHubAdapter(spotList,this.getActivity());


        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mAdapter);
        loadSpots();

        return rootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    private void loadSpots() {
        String uri = SpotBoy_Server_URIs.PHP_GET_ALL_SPOTS;
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, uri, null,this,this);
        Volley.newRequestQueue(this.getContext()).add(request);
    }

    @Override
    public void moreButtonCallback(Spot spot) {
        Log.d(log, "moreButtonCallback");

        String googleName = ((IHub)getActivity()).getUserGoogleName();
        String googleId = ((IHub)getActivity()).getUserGoogleId();

        Intent infoIntent = new Intent(this.getContext(),Online_InfoActivity.class);
        infoIntent.putExtra(SPOT,new Gson().toJson(spot));
        infoIntent.putExtra(GOOGLE_NAME, googleName);
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
        Log.d(log, "volley response: " + response.toString());
        try {
            String rawSuccess = response.getString("success");
            Log.d(log,"success string: " + rawSuccess);
            switch (rawSuccess){
                case "0":
                    Toast.makeText(this.getContext(), getString(R.string.remote_db_error_message), Toast.LENGTH_SHORT).show();
                    break;
                case "1":
                    spotList = Utilities.createSpotListFromJSONResult(response);
                    mAdapter.updateList(spotList);
                    break;
                case "2":
                    Toast.makeText(this.getContext(),getString(R.string.no_spot_found_message), Toast.LENGTH_SHORT).show();
                    break;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

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
