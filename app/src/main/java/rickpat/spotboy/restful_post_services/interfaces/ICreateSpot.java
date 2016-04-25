package rickpat.spotboy.restful_post_services.interfaces;

public interface ICreateSpot extends IBasicCallbacks {
    void spotCreatedCallback();
    void progressUpdate( String message );
}
