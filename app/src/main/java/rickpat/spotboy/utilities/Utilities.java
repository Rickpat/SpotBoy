package rickpat.spotboy.utilities;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.support.v4.content.res.ResourcesCompat;
import android.util.Base64;

import rickpat.spotboy.enums.Library;
import rickpat.spotboy.enums.SpotType;
import rickpat.spotboy.R;

import java.io.ByteArrayOutputStream;
import java.util.Arrays;

public class Utilities {
    private static String log = "UTILITIES";

    private Utilities(){}

    /*
    * converts image to string
    * */
    public static String getStringImage(Bitmap bmp){
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
        byte[] imageBytes = byteArrayOutputStream.toByteArray();
        String encodedImage = Base64.encodeToString(imageBytes, Base64.DEFAULT);
        return encodedImage;
    }

    public static Bitmap decodeSampledBitmapFromResource(Resources res, String fileName,
                                                         int reqWidth, int reqHeight) {

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(fileName, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(fileName, options);
    }

    public static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) > reqHeight && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    /*
    * loads items from enum to string array
    * */
    public static String[] getLibrariesStringArray(){
        String[] librariesArray = new String[Library.values().length];
        int help = 0;
        for (Library lib : Library.values()){
            librariesArray[help] = lib.toString();
            help++;
        }
        Arrays.sort(librariesArray);
        return librariesArray;
    }

    /*
    * loads items from enum to string array
    * */
    public static String[] getSpotTypes(){
        String[] items = new String[SpotType.values().length];
        int i = 0;
        for( SpotType category : SpotType.values()){
            items[i] = category.toString().toUpperCase();
            i++;
        }
        Arrays.sort(items);
        return items;
    }

    /*
    * loads Bitmap from resources by given spotType for SpotCluster class
    * */
    public static Bitmap getClusterIcon(Context applicationContext, SpotType spotType) {
        Bitmap icon;
        int resourceId = 0;
        switch (spotType){
            case dirt:
                resourceId = R.drawable.ic_dirtcluster;
                break;
            case park:
                resourceId = R.drawable.ic_parkcluster;
                break;
            case street:
                resourceId = R.drawable.ic_streetcluster;
                break;
            case flat:
                resourceId = R.drawable.ic_flatcluster;
        }
        icon = BitmapFactory.decodeResource(applicationContext.getResources(), resourceId);
        return icon;
    }

    /*
    * loads Drawable from resources by given spotType for SpotMarker class
    * */
    public static Drawable getMarkerIcon(Context applicationContext, SpotType spotType) {
        Drawable icon;
        int resourceId = 0;
        switch (spotType){
            case dirt:
                resourceId = R.drawable.ic_dirtmarker;
                break;
            case park:
                resourceId = R.drawable.ic_parkmarker;
                break;
            case street:
                resourceId = R.drawable.ic_streetmarker;
                break;
            case flat:
                resourceId = R.drawable.ic_flatmarker;
        }

        icon = ResourcesCompat.getDrawable(applicationContext.getResources(), resourceId, null);
        return icon;
    }

    /*
    * takes spot type string and returns SpotType
    * */
    public static SpotType parseSpotTypeString(String spotType){
        SpotType type = null;
        for (SpotType item : SpotType.values()){
            if (spotType.equalsIgnoreCase(item.toString())){
                type = item;
            }
        }
        return type;
    }
}
