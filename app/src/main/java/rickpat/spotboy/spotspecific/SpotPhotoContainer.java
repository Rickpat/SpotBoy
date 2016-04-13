package rickpat.spotboy.spotspecific;

import android.net.Uri;

import java.util.List;

public class SpotPhotoContainer {
    private List<Uri> imgUriList;
    private int capacity;

    public SpotPhotoContainer(List<Uri> imgUriList, int capacity) {
        this.imgUriList = imgUriList;
        this.capacity = capacity;
    }

    public List<Uri> getImgUriList() {
        return imgUriList;
    }

    public void setImgUriList(List<Uri> imgUriList) {
        this.imgUriList = imgUriList;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public boolean addImageUri( Uri uri ){
        return this.imgUriList.add(uri);
    }
}
