package rickpat.spotboy.spotspecific;

import org.osmdroid.util.GeoPoint;

import java.util.Date;
import java.util.List;

import rickpat.spotboy.enums.SpotType;

public class Spot {

    /*
    * Base class
    *
    * it contains two constructors for:
    * - locally or
    * - server sided stored information about a spot
    * */

    private String googleId;        //creator googleId
    private String id;              //database row
    private GeoPoint geoPoint;      //geo information
    private String notes;           //extra notes
    private List<String> urlList;   //images
    private Date date;              //creation time
    private SpotType spotType;      //type

    /*
    * this constructor is used for locally stored spots
    * */
    public Spot(String id, GeoPoint geoPoint, String notes, List<String> urlList, Date date, SpotType spotType) {
        this.googleId = "-1";   // this can help to identify it's source
        this.id = id;
        this.geoPoint = geoPoint;
        this.notes = notes;
        this.urlList = urlList;
        this.date = date;
        this.spotType = spotType;
    }

    /*
    * this constructor is for spots which are stored on server.
    * it contains a field for the creators google id.
    * */
    public Spot(String googleId, String id, GeoPoint geoPoint, String notes, List<String> urlList, Date date, SpotType spotType) {
        this.googleId = googleId;
        this.id = id;
        this.geoPoint = geoPoint;
        this.notes = notes;
        this.urlList = urlList;
        this.date = date;
        this.spotType = spotType;
    }

    public String getGoogleId() {
        return googleId;
    }

    public void setGoogleId(String googleId) {
        this.googleId = googleId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public List<String> getUrlList() {
        return urlList;
    }

    public void setUrlList(List<String> urlList) {
        this.urlList = urlList;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public GeoPoint getGeoPoint() {
        return geoPoint;
    }

    public void setGeoPoint(GeoPoint geoPoint) {
        this.geoPoint = geoPoint;
    }

    public SpotType getSpotType() {
        return spotType;
    }

    public void setSpotType(SpotType spotType) {
        this.spotType = spotType;
    }
}
