package rickpat.spotboy.osmspecific;

import android.content.Context;
import android.location.Location;

import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.mylocation.IMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

public class MyLocationOverlay extends MyLocationNewOverlay {

    private IMyLocationCallback callback;

    public interface IMyLocationCallback{
        void setFixedLocationIcon();
    }

    public MyLocationOverlay(Context context, MapView mapView) {
        super(context, mapView);
        callback = (IMyLocationCallback)context;
    }

    @Override
    public void onLocationChanged(Location location, IMyLocationProvider source) {
        super.onLocationChanged(location, source);
        if (location != null) {
            callback.setFixedLocationIcon();
        }
    }
}
