package rickpat.spotboy.utilities;

import android.graphics.Bitmap;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.Volley;

import java.util.List;

import rickpat.spotboy.R;
import rickpat.spotboy.spotspecific.SpotRemote;

public class Online_SpotHubAdapter extends RecyclerView.Adapter<Online_SpotHubAdapter.ViewHolder> {
    private List<SpotRemote> spotList;
    private Fragment fragment;

    private IHubAdapter callback;
    private String log = "Online_SpotHubAdapter";

    public interface IHubAdapter{
        void moreButtonCallback(SpotRemote remote);
        void markerButtonCallback(SpotRemote remote);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView catTextView;
        public Button moreButton;
        public Button markerButton;
        public ImageView imageView;
        public ViewHolder(View v) {
            super(v);
            catTextView = (TextView)v.findViewById(R.id.hub_card_cat);
            moreButton = (Button)v.findViewById(R.id.hub_card_more_button);
            markerButton = (Button)v.findViewById(R.id.hub_card_marker_button);
            imageView = (ImageView)v.findViewById(R.id.hub_card_imageView);
        }
    }

    public Online_SpotHubAdapter(List<SpotRemote> spotList, Fragment fragment) {
        this.spotList = spotList;
        this.fragment = fragment;
        this.callback = (IHubAdapter)fragment;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.hub_cardview, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {

        final SpotRemote spot = spotList.get(position);
        holder.catTextView.setText(spot.getSpotType().toString());

        if (spot.getUri() != null) {
            int maxW = Utilities.getDeviceWidth(fragment.getActivity());
            ImageRequest imageRequest = new ImageRequest(spot.getUri(), new Response.Listener<Bitmap>() {
                @Override
                public void onResponse(Bitmap response) {
                    Log.d(log, "volley/ onResponse");
                    holder.imageView.setImageBitmap(response);
                    holder.imageView.setBackground(null);
                }
            }, maxW, 0, ImageView.ScaleType.CENTER, Bitmap.Config.ARGB_8888, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.d(log, "volley/ onErrorResponse: " + error.getMessage());
                }
            });
            Volley.newRequestQueue(fragment.getContext()).add(imageRequest);
        }

        holder.markerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callback.markerButtonCallback(spot);
            }
        });

        holder.moreButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callback.moreButtonCallback(spot);
            }
        });


    }

    @Override
    public int getItemCount() {
        return spotList.size();
    }

    public void updateList(List<SpotRemote> data) {
        Log.d(log,"updateList");
        spotList = data;
        notifyDataSetChanged();
    }
}
