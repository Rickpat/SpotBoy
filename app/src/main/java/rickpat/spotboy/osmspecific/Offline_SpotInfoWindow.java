package rickpat.spotboy.osmspecific;

import android.app.Activity;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;

import rickpat.spotboy.R;
import rickpat.spotboy.spotspecific.Spot;
import rickpat.spotboy.spotspecific.SpotMarker;
import rickpat.spotboy.utilities.Utilities;

import org.osmdroid.bonuspack.overlays.InfoWindow;
import org.osmdroid.views.MapView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class Offline_SpotInfoWindow extends InfoWindow {

    private InfoCallback infoCallback;
    private Activity activity;

    public interface InfoCallback{
        void infoCallback(Spot remote);
    }

    public Offline_SpotInfoWindow(int layoutResId, MapView mapView, Activity activity) {
        super(layoutResId, mapView);
        this.infoCallback = (InfoCallback)activity;
        this.activity = activity;
    }

    @Override
    public void onOpen(Object item) {
        final SpotMarker myMarker = (SpotMarker)item;
        Button btnMoreInfo = (Button) mView.findViewById(R.id.infoWindow_moreButton);
        final ImageView imageView = (ImageView) mView.findViewById(R.id.infoWin_image);
        TextView catTextView = (TextView) mView.findViewById(R.id.infoWin_cat);
        TextView notesTextView = (TextView) mView.findViewById(R.id.infoWin_notes);
        TextView dateTextView = (TextView) mView.findViewById(R.id.infoWin_time);

        DateFormat df = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.GERMAN);

        if ( myMarker.getSpot().getUrlList().size() > 0 ){
            Glide.with(activity).load(myMarker.getSpot().getUrlList().get(0)).override(300,200).into(imageView);
        }

        catTextView.setText(myMarker.getSpot().getSpotType().toString());
        notesTextView.setText(myMarker.getSpot().getNotes());
        dateTextView.setText(df.format(myMarker.getSpot().getDate()));

        btnMoreInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                infoCallback.infoCallback(myMarker.getSpot());
            }
        });
    }

    @Override
    public void onClose() {

    }
}
