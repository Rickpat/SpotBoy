package rickpat.spotboy.utilities;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.Log;
import android.view.Display;


import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.osmdroid.util.GeoPoint;

import rickpat.spotboy.enums.Library;
import rickpat.spotboy.enums.SpotType;
import rickpat.spotboy.R;
import rickpat.spotboy.spotspecific.SpotRemote;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class Utilities {
    private static String log = "UTILITIES";

    private Utilities(){}


    public static int getDeviceWidth( Activity activity){
        Display display = activity.getWindowManager().getDefaultDisplay();
        Point point = new Point();
        display.getSize(point);
        return point.x;
    }

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

    public static String getTimeString(){
        DateFormat df = new SimpleDateFormat(Constants.TIME_FORMAT, Locale.UK);
        return df.format(new Date());
    }

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
            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    public static Bitmap getClusterIcon(Context applicationContext, SpotType spotType) {
        Bitmap icon;
        int resourceId = 0;
        switch (spotType){
            case DIRT:
                resourceId = R.drawable.ic_dirtcluster;
                break;
            case PARK:
                resourceId = R.drawable.ic_parkcluster;
                break;
            case STREET:
                resourceId = R.drawable.ic_streetcluster;
                break;
            case FLAT:
                resourceId = R.drawable.ic_flatcluster;
        }
        icon = BitmapFactory.decodeResource(applicationContext.getResources(), resourceId);
        return icon;
    }

    public static Drawable getMarkerIcon(Context applicationContext, SpotType spotType) {
        Drawable icon;
        int resourceId = 0;
        switch (spotType){
            case DIRT:
                resourceId = R.drawable.ic_dirtmarker;
                break;
            case PARK:
                resourceId = R.drawable.ic_parkmarker;
                break;
            case STREET:
                resourceId = R.drawable.ic_streetmarker;
                break;
            case FLAT:
                resourceId = R.drawable.ic_flatmarker;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            icon = applicationContext.getResources().getDrawable(resourceId,applicationContext.getTheme());
        }else{
            icon = applicationContext.getResources().getDrawable(resourceId);
        }
        return icon;
    }

    public static SpotType parseSpotTypeString(String spotType){
        SpotType type = null;
        for (SpotType item : SpotType.values()){
            if (spotType.equalsIgnoreCase(item.toString())){
                return item;
            }
        }
        return null;
    }

    public static List<SpotRemote> createSpotListFromJSONResult( JSONObject rawJson ){
        List<SpotRemote> remoteList = new ArrayList<>();
        try {
            JSONArray jsonArray = rawJson.getJSONArray("spots");
            Log.d(log, "spot json array elements: " + jsonArray.length());
            for( int i = 0 ; i < jsonArray.length(); i++ ){
                JSONObject jsonSpot = jsonArray.getJSONObject(i);
                String id = jsonSpot.getString("id");
                String googleId = jsonSpot.getString("googleId");
                GeoPoint geoPoint = new Gson().fromJson(jsonSpot.getString("geoPoint"),GeoPoint.class);
                SpotType spotType = parseSpotTypeString(jsonSpot.getString("spotType"));
                String notes = jsonSpot.getString("notes");
                String imgURL = "";
                if (jsonSpot.has("imgURL")) {
                    imgURL = jsonSpot.getString("imgURL");
                }
                String creationTime = jsonSpot.getString("creationTime");
                Date date = new Date(Long.parseLong(creationTime));

                remoteList.add(new SpotRemote(googleId,id,geoPoint,notes,imgURL,date, spotType));
                Log.d(log,"spot " + i + " googleId: " + googleId);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return remoteList;
    }
}
