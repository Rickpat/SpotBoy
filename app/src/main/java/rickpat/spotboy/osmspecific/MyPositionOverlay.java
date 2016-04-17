package rickpat.spotboy.osmspecific;

import android.content.Context;
import android.location.Location;

import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.mylocation.IMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

public class MyPositionOverlay extends MyLocationNewOverlay {


    public MyPositionOverlay(Context context, MapView mapView) {
        super(context, mapView);
    }

    @Override
    public void onLocationChanged(Location location, IMyLocationProvider source) {
        super.onLocationChanged(location, source);
    }
}
