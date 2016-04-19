package rickpat.spotboy.utilities;

import android.app.Activity;
import android.content.Context;
import android.graphics.Point;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;


import com.bumptech.glide.Glide;

import java.util.List;

import rickpat.spotboy.R;
import rickpat.spotboy.spotspecific.Spot;

/*
* adapter for spot hub recycler view. cares about card views
* */

public class SpotHubAdapter extends RecyclerView.Adapter<SpotHubAdapter.ViewHolder> {
    private List<Spot> spotList;
    private IHubAdapter callback;
    private Context context;

    public interface IHubAdapter{
        void moreButtonCallback(Spot spot);
        void markerButtonCallback(Spot spot);
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

    public SpotHubAdapter(List<Spot> spotList, Activity activity) {
        this.spotList = spotList;
        this.callback = (IHubAdapter)activity;
        this.context = activity.getApplicationContext();
    }

    public SpotHubAdapter(List<Spot> spotList, Fragment fragment) {
        this.spotList = spotList;
        this.callback = (IHubAdapter)fragment;
        this.context = fragment.getContext();
    }

    @Override
    public SpotHubAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.hub_cardview, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        final Spot spot = spotList.get(position);
        holder.catTextView.setText(spot.getSpotType().toString());

        if (spot.getUrlList().size() > 0 ){
            Glide.with(context).load(spot.getUrlList().get(0)).into(holder.imageView);
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

    public void updateList(List<Spot> data) {
        spotList = data;
        notifyDataSetChanged();
    }
}
