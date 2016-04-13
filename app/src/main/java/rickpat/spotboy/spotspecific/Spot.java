package rickpat.spotboy.spotspecific;

import org.osmdroid.util.GeoPoint;

import java.util.Date;

import rickpat.spotboy.enums.SpotType;

public class Spot {

    private String id;
    private GeoPoint geoPoint;
    private String notes;
    private String uri;
    private Date date;
    private SpotType spotType;

    public Spot(String id, GeoPoint geoPoint, String notes, String uri, Date date, SpotType spotType) {
        this.id = id;
        this.geoPoint = geoPoint;
        this.notes = notes;
        this.uri = uri;
        this.date = date;
        this.spotType = spotType;
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

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
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
