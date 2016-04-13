package rickpat.spotboy.spotspecific;


import com.google.gson.Gson;

import rickpat.spotboy.enums.SpotType;

import org.osmdroid.util.GeoPoint;

import java.util.Date;

public class SpotRemote extends Spot{

    /*
     * SpotRemote describes an spot by its category and some notes that can be added
     * plus longitude and latitude.
     * It is stored on server
     * */

    private String googleId;

    public SpotRemote( String googleId, String id, GeoPoint geoPoint, String notes, String uri, Date date, SpotType spotType) {
        super(id, geoPoint, notes, uri, date, spotType);
        this.googleId = googleId;
    }


    public String getGoogleId() {
        return googleId;
    }

    public String getJSONGeoPoint(){
        return new Gson().toJson(this.getGeoPoint());
    }

    public void setJSONGEOPOINT( String jsonGeoPoint){
        this.setGeoPoint( new Gson().fromJson(jsonGeoPoint,GeoPoint.class));
    }

}
