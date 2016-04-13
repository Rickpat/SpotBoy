package rickpat.spotboy.spotspecific;

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

import rickpat.spotboy.R;
import rickpat.spotboy.utilities.Utilities;

import org.osmdroid.bonuspack.overlays.InfoWindow;
import org.osmdroid.views.MapView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class SpotInfoWindow extends InfoWindow {

    private InfoCallback infoCallback;
    private Resources resources;
    private boolean isRemoteSpot;
    private Activity activity;
    private String log = "SpotInfoWindow";
    private int maxW;
    private int maxH;

    public interface InfoCallback{
        void infoCallback(Spot remote);
    }

    public SpotInfoWindow(int layoutResId, MapView mapView , Activity activity) {
        super(layoutResId, mapView);
        this.infoCallback = (InfoCallback)activity;
        this.resources = activity.getResources();
        this.activity = activity;
        maxH = 0;   //default
        maxW = (int)resources.getDimension(R.dimen.infoWindow_imageDimMax);
    }

    public void setIsRemoteSpot(boolean isRemoteSpot) {
        this.isRemoteSpot = isRemoteSpot;
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

        if ( !isRemoteSpot && myMarker.getSpot().getUri() != null){ //local stored spot with image
            imageView.setVisibility(View.VISIBLE);
            Bitmap bitmap = Utilities.decodeSampledBitmapFromResource(resources, myMarker.getSpot().getUri(), maxW, maxH);
            Drawable drawable = new BitmapDrawable(resources, bitmap);
            imageView.setImageDrawable(drawable);
        }else if ( isRemoteSpot && myMarker.getSpot().getUri() != null){ //spot with image is stored on server
            imageView.setVisibility(View.VISIBLE);
            String uri = myMarker.getSpot().getUri();
            ImageRequest imageRequest = new ImageRequest(uri, new Response.Listener<Bitmap>() {
                @Override
                public void onResponse(Bitmap response) {
                    imageView.setImageBitmap(response);
                }
            }, maxH, maxW, ImageView.ScaleType.CENTER, Bitmap.Config.ARGB_8888, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.d(log,"url: " + myMarker.getSpot().getUri());
                }
            });

            Volley.newRequestQueue(activity).add(imageRequest);
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
