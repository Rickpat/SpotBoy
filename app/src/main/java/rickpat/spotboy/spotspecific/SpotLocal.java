package rickpat.spotboy.spotspecific;


import rickpat.spotboy.enums.SpotType;

import org.osmdroid.util.GeoPoint;

import java.util.Date;

public class SpotLocal extends Spot {

    public SpotLocal(String id, GeoPoint geoPoint, String notes, String uri, Date date, SpotType spotType) {
        super(id, geoPoint, notes, uri, date, spotType);
    }



    /*
     * SpotLocal describes an spot by its category and some notes that can be added
     * plus longitude and latitude.
     * It is stored locally
     * */

}
