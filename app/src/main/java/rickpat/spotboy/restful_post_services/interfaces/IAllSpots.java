package rickpat.spotboy.restful_post_services.interfaces;

import java.util.List;

import rickpat.spotboy.spotspecific.Spot;

public interface IAllSpots extends IBasicCallbacks {
    void setSpotList( List<Spot> spotList);
}
