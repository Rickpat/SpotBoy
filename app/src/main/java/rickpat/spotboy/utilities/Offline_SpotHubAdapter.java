package rickpat.spotboy.utilities;

import android.app.Activity;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;


import java.util.List;

import rickpat.spotboy.R;
import rickpat.spotboy.spotspecific.SpotLocal;

public class Offline_SpotHubAdapter extends RecyclerView.Adapter<Offline_SpotHubAdapter.ViewHolder> {
    private List<SpotLocal> spotList;
    private Resources resources;
    private int displayW;
    private IHubAdapter callback;

    public interface IHubAdapter{
        void moreButtonCallback(SpotLocal remote);
        void markerButtonCallback(SpotLocal remote);
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

    public Offline_SpotHubAdapter(List<SpotLocal> spotList, Activity activity) {

        Display display = activity.getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);

        this.spotList = spotList;
        this.resources = activity.getResources();
        this.displayW = size.x;
        this.callback = (IHubAdapter)activity;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.hub_cardview, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        final SpotLocal spot = spotList.get(position);
        holder.catTextView.setText(spot.getSpotType().toString());

        if (spot.getUri() != null) {
            Bitmap bitmap = Utilities.decodeSampledBitmapFromResource(resources, spot.getUri(), this.displayW-50, 350);
            Drawable drawable = new BitmapDrawable(resources, bitmap);
            holder.imageView.setImageDrawable(drawable);

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

    public void updateList(List<SpotLocal> data) {
        spotList = data;
        notifyDataSetChanged();
    }
}
