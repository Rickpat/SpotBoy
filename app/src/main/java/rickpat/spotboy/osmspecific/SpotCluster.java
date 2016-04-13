package rickpat.spotboy.osmspecific;


import android.content.Context;

import org.osmdroid.bonuspack.clustering.RadiusMarkerClusterer;

public class SpotCluster extends RadiusMarkerClusterer {

    public SpotCluster(Context ctx) {
        super(ctx);
    }
}
