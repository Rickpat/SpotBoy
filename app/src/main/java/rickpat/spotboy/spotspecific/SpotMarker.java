package rickpat.spotboy.spotspecific;


import org.osmdroid.bonuspack.overlays.Marker;
import org.osmdroid.views.MapView;

public class SpotMarker extends Marker {

    private Spot spot;

    public SpotMarker(MapView mapView, Spot spot) {
        super(mapView);
        this.spot = spot;
        this.setPosition(spot.getGeoPoint());
    }

    public Spot getSpot() {
        return spot;
    }
}
