package rickpat.spotboy.online_fragments;

import java.util.List;

import rickpat.spotboy.spotspecific.Spot;

public interface IHub {
    String getUserGoogleId();
    List<Spot> getSpotList();
    void updateList();
}
