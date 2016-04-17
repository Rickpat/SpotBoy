package rickpat.spotboy.offline_fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

import rickpat.spotboy.R;


import static rickpat.spotboy.utilities.Constants.IMG_URL;

public class GalleryItemFragment extends Fragment {

    private String log = "GalleryItemFragment";

    private String imgURL;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        imgURL = getArguments().getString(IMG_URL);
        try{
            Log.d(log, "received url: " + imgURL);
        }catch (Exception e){
            Log.d(log, "error: " + e.getMessage());
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.gallery_item, container, false);
        ImageView imageView = (ImageView) rootView.findViewById(R.id.gallery_imageView);
        Glide.with(this).load(imgURL).into(imageView);
        return rootView;
    }
}
