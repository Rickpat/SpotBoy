package rickpat.spotboy.online_activities;


import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;
import org.osmdroid.util.GeoPoint;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Hashtable;
import java.util.Locale;
import java.util.Map;

import rickpat.spotboy.R;
import rickpat.spotboy.enums.SpotType;
import rickpat.spotboy.spotspecific.SpotRemote;
import rickpat.spotboy.utilities.Utilities;

import static rickpat.spotboy.utilities.Constants.*;
import static rickpat.spotboy.utilities.SpotBoy_Server_URIs.*;

public class Online_NewActivity extends AppCompatActivity implements View.OnClickListener, Response.ErrorListener, Response.Listener<String> {

    private String prefImagePath = "imagePath";
    private String cat = "cat";
    private String notes = "notes";

    private String KEY_IMAGE = "image";
    private String KEY_GOOGLE_ID = "googleId";
    private String KEY_GEO_POINT = "geoPoint";
    private String KEY_SPOT_TYPE = "spotType";
    private String KEY_NOTES = "notes";
    private String KEY_TIME = "creationTime";
    private String KEY_ID = "rowId";

    private AlertDialog catDialog;
    private ImageView imageView;
    private GeoPoint geoPoint;
    private static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 100;
    private Uri fileUri;

    private String googleId;
    private String googleName;

    private ProgressDialog progressDialog;
    private String log = "Online_NewActivity";

    private SpotRemote spotRemote;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_new);
        setSupportActionBar(toolbar);
        try{
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }catch (NullPointerException e){}
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        Bundle bundle = getIntent().getExtras();
        if (bundle.containsKey(GEOPOINT)){
            geoPoint = new Gson().fromJson(bundle.getString(GEOPOINT),GeoPoint.class);
            ((TextView)findViewById(R.id.new_spot_lat)).setText(String.valueOf(geoPoint.getLatitude()));
            ((TextView)findViewById(R.id.new_spot_lon)).setText(String.valueOf(geoPoint.getLongitude()));

            googleId = bundle.getString(GOOGLE_ID);
            googleName = bundle.getString(GOOGLE_NAME);
            Log.d(log,"googleId: " + googleId + " googleName: " + googleName);
        }else {
            finish();
        }
        findViewById(R.id.new_spot_cat_layout).setOnClickListener(this);
        findViewById(R.id.new_spot_fab_photo).setOnClickListener(this);
        imageView = (ImageView) findViewById(R.id.new_spot_imageView);
        createDialogs();
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        SharedPreferences preferences = getSharedPreferences(PREFERENCES, MODE_PRIVATE);
        String path = preferences.getString(prefImagePath, "zero");
        geoPoint = new Gson().fromJson(preferences.getString(GEOPOINT,"zero"),GeoPoint.class);
        if (path != "zero") {
            fileUri = Uri.parse(path);
            Glide.with(this).load(fileUri.getEncodedPath()).into(imageView);
        }
        ((TextView) findViewById(R.id.new_spot_cat_textView)).setText(preferences.getString(cat, ""));
        ((EditText) findViewById(R.id.new_spot_notes_editText)).setText(preferences.getString(notes, ""));

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        SharedPreferences preferences = getSharedPreferences(PREFERENCES, MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(cat, ((TextView) findViewById(R.id.new_spot_cat_textView)).getText().toString().trim());
        editor.putString(notes, ((EditText) findViewById(R.id.new_spot_notes_editText)).getText().toString().trim());
        editor.putString(GEOPOINT, new Gson().toJson(geoPoint));
        if (fileUri != null) {
            editor.putString(prefImagePath, fileUri.getEncodedPath());
        } else {
            editor.remove(prefImagePath);
        }
        editor.apply();
    }

    private Uri getOutputMediaFileUri() {
        return Uri.fromFile(getOutputMediaFile());
    }

    private File getOutputMediaFile() {

        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "SBO_PHOTOS");

        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.d("MyCameraApp", "failed to create directory");
                return null;
            }
        }

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.ENGLISH).format(new Date());
        File mediaFile;
        mediaFile = new File(mediaStorageDir.getPath() + File.separator + "IMG_" + timeStamp + ".jpg");
        return mediaFile;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.d(log,"onCreateOptionsMenu");
        getMenuInflater().inflate(R.menu.menu_new, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.d(log,"onOptionsItemSelected");
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.action_create:
                createDBEntry();
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.new_spot_cat_layout:
                catDialog.show();
                break;
            case R.id.new_spot_fab_photo:
                fileUri = getOutputMediaFileUri();
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
                startActivityForResult(intent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
                break;
        }
    }

    private void createDialogs() {
        String[] spotTypes = Utilities.getSpotTypes();
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        catDialog = builder.setSingleChoiceItems(spotTypes, 0, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        }).setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                catDialog.cancel();
            }
        }).setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                ListView listView = ((AlertDialog) dialog).getListView();
                Object object = listView.getAdapter().getItem(listView.getCheckedItemPosition());
                String selection = (String) object;
                ((TextView) findViewById(R.id.new_spot_cat_textView)).setText(selection);
            }
        }).create();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE) {
            if (resultCode != RESULT_OK) {
                Toast.makeText(this,getString(R.string.error_message_img),Toast.LENGTH_LONG).show();
                if ( resultCode == NEW_SPOT_CREATED){
                    Toast.makeText(this,getString(R.string.spot_created_message),Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

//SPOT CREATION METHODS

    private void createDBEntry() {
        String cat = ((TextView)findViewById(R.id.new_spot_cat_textView)).getText().toString().trim();

        SpotType spotType = Utilities.parseSpotTypeString(cat);

        String notes = ((EditText)findViewById(R.id.new_spot_notes_editText)).getText().toString().trim();

        String uri = fileUri == null?null:fileUri.getEncodedPath();

        spotRemote = new SpotRemote(googleId,"", geoPoint, notes, uri , new Date(),spotType);



        StringRequest stringRequest = new StringRequest(Request.Method.POST,PHP_CREATE_DB_ENTRY,this,this){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                //Creating parameters
                Map<String,String> params = new Hashtable<String, String>();

                //Adding parameters
                params.put(KEY_GOOGLE_ID, spotRemote.getGoogleId());
                params.put(KEY_GEO_POINT, spotRemote.getJSONGeoPoint());
                params.put(KEY_SPOT_TYPE, spotRemote.getSpotType().toString());
                params.put(KEY_NOTES , spotRemote.getNotes());
                params.put(KEY_TIME, String.valueOf(spotRemote.getDate().getTime()));

                //returning parameters
                return params;
            }
        };

        if (fileUri != null){
            progressDialog = ProgressDialog.show(this,getString(R.string.creating_db_entry_message),getString(R.string.uploading),false,false);
            Volley.newRequestQueue(this).add(stringRequest);
        }else{
            Toast.makeText(this,getString(R.string.take_a_photo_message),Toast.LENGTH_SHORT).show();
        }
    }


    public String getStringImage(Bitmap bmp){
        /*
        * converts image to string
        * */

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
        byte[] imageBytes = byteArrayOutputStream.toByteArray();
        String encodedImage = Base64.encodeToString(imageBytes, Base64.DEFAULT);
        return encodedImage;
    }

    private void uploadImage(){
        //Showing the progress dialog
        StringRequest stringRequest = new StringRequest(Request.Method.POST, PHP_UPLOAD_IMAGE, this, this){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                //Converting Bitmap to String
                Bitmap bitmap = Utilities.decodeSampledBitmapFromResource(getResources(), spotRemote.getUri(), 600, 600);

                String image = getStringImage(bitmap);

                //Creating parameters
                Map<String,String> params = new Hashtable<String, String>();

                //Adding parameters
                params.put(KEY_IMAGE, image);
                params.put(KEY_ID, spotRemote.getId());

                //returning parameters
                return params;
            }
        };

        //Creating a Request Queue
        RequestQueue requestQueue = Volley.newRequestQueue(this);

        //Adding request to the queue
        requestQueue.add(stringRequest);
    }

    @Override
    public void onErrorResponse(VolleyError error) {
        //Dismissing the progress dialog
        progressDialog.dismiss();

        Log.d(log,error.getMessage());
        //Showing toast
        //Toast.makeText(this, error.getMessage().toString(), Toast.LENGTH_LONG).show();
    }

    @Override
    public void onResponse(String response) {
        progressDialog.dismiss();

        Log.d(log+"_error",response);
        try {
            JSONObject jsonObject = new JSONObject(response);
            boolean success = jsonObject.getString("success").equalsIgnoreCase("1");
            //String message = jsonObject.getString("message");
            String rowId = jsonObject.getString("rowId");
            switch (jsonObject.getString("action")){
                case PHP_ACTION_CREATE_SPOT:
                    if (success){
                        spotRemote.setId(rowId);
                        progressDialog.setTitle(getString(R.string.image_upload_started));
                        progressDialog.setMessage(getString(R.string.uploading));
                        progressDialog.show();
                        uploadImage();
                    }
                    break;
                case PHP_ACTION_IMAGE_UPLOAD:
                    progressDialog.dismiss();
                    if(success){
                        Toast.makeText(this,getString(R.string.spot_created_message),Toast.LENGTH_SHORT).show();
                        setResult(NEW_SPOT_CREATED);
                        finish();
                    }
                    break;
            }



        } catch (JSONException e) {
            e.printStackTrace();
        }
        //Toast.makeText(this, response , Toast.LENGTH_LONG).show();

        //uploadImage(spotRemote);
    }
}

