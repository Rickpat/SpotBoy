package rickpat.spotboy.offline_activities;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import rickpat.spotboy.R;
import rickpat.spotboy.offline_database.SpotBoyDBHelper;
import rickpat.spotboy.spotspecific.SpotLocal;
import rickpat.spotboy.utilities.Utilities;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;

import static rickpat.spotboy.utilities.Constants.*;

public class Offline_InfoActivity extends AppCompatActivity {

    private SpotLocal spot;

    private String log="Offline_InfoActivity";
    private boolean modified = false;

    private AlertDialog catAlertDialog;
    private AlertDialog notesAlertDialog;


    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        SharedPreferences preferences = getSharedPreferences(PREFERENCES,MODE_PRIVATE);
        modified = preferences.getBoolean(MODIFIED,false);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        SharedPreferences preferences = getSharedPreferences(PREFERENCES,MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(MODIFIED, modified);
        editor.apply();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_info);
        setSupportActionBar(toolbar);
        try{
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }catch (NullPointerException e){}
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        Bundle bundle = getIntent().getExtras();

        if (bundle.containsKey(SPOT)){
            spot = new Gson().fromJson(bundle.getString(SPOT), SpotLocal.class);
        }else {
            finish();
        }

        setContent();
        setDialogs();
    }

    private void setContent() {
        ((TextView)findViewById(R.id.info_spotTypeTextView)).setText(spot.getSpotType().toString());
        ((TextView)findViewById(R.id.info_notesTextView)).setText(spot.getNotes());
        if (spot.getUri() != null){
            setImage();
        }

        DateFormat df = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.GERMAN);
        ((TextView)findViewById(R.id.info_dateTextView)).setText(df.format(spot.getDate()));

        findViewById(R.id.info_cat_fab_edit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(log, "info_cat_fab_edit).setOnClickListener(new View.OnClickListener() ");
                catAlertDialog.show();
            }
        });

        findViewById(R.id.info_notes_fab_edit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(log, "info_notes_fab_edit).setOnClickListener(new View.OnClickListener()  ");
                notesAlertDialog.show();
            }
        });

    }

    private void setImage() {
        Display display = getWindowManager().getDefaultDisplay();
        Point point = new Point();
        display.getSize(point);
        int displayW = point.x;
        Bitmap bitmap = Utilities.decodeSampledBitmapFromResource(getResources(), spot.getUri(), displayW, 500);
        Drawable drawable = new BitmapDrawable(getResources(),bitmap);
        ImageView imageView = (ImageView)findViewById(R.id.info_imageView);
        imageView.setImageDrawable(drawable);
        imageView.setBackground(null);
    }

    private void setDialogs() {
        AlertDialog.Builder catBuilder = new AlertDialog.Builder(this);
        final String[] catItems = Utilities.getSpotTypes();
        int selectedItem = getSelection(catItems);
        catAlertDialog = catBuilder
                .setTitle(getString(R.string.new_cat_alert_title))
                .setSingleChoiceItems(catItems, selectedItem, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Log.d(log, "selection: " + catItems[which]);
                    }
                }).setNegativeButton(getText(R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        catAlertDialog.cancel();
                    }
                }).setPositiveButton(getText(R.string.ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ListView listView = ((AlertDialog) dialog).getListView();
                        Object object = listView.getAdapter().getItem(listView.getCheckedItemPosition());
                        String selection = (String) object;
                        spot.setSpotType(Utilities.parseSpotTypeString(selection));
                        SpotBoyDBHelper spotBoyDBHelper = new SpotBoyDBHelper(Offline_InfoActivity.this, null, null, 1);
                        long dbResult = spotBoyDBHelper.updateSpot(spot);
                        if (dbResult > 0) {
                            ((TextView) findViewById(R.id.info_spotTypeTextView)).setText(selection);
                            modified = true;
                        }
                    }
                }).create();

        AlertDialog.Builder notesBuilder = new AlertDialog.Builder(this);
        View notesContent = getLayoutInflater().inflate(R.layout.dialog_input,null);
        final EditText editText = (EditText)notesContent.findViewById(R.id.dialog_editText);
        editText.setText(spot.getNotes());
        notesAlertDialog = notesBuilder
                .setTitle(getString(R.string.new_notes_dialog_title))
                .setView(notesContent)
                .setPositiveButton(getText(R.string.ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String notes = editText.getText().toString().trim();
                        Log.d(log, "new notes: " + notes);
                        spot.setNotes(notes);
                        SpotBoyDBHelper spotBoyDBHelper = new SpotBoyDBHelper(Offline_InfoActivity.this, null, null, 1);

                        long dbResult = spotBoyDBHelper.updateSpot(spot);
                        if (dbResult > 0) {
                            ((TextView) findViewById(R.id.info_notesTextView)).setText(notes);
                            modified = true;
                        }
                    }
                }).setNegativeButton(getText(R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        notesAlertDialog.cancel();
                    }
                })
                .create();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_info, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // this takes the user 'back', as if they pressed the left-facing triangle icon on the main android toolbar.
                // if this doesn't work as desired, another possibility is to call `finish()` here.
                if (modified){
                    setResult(INFO_ACTIVITY_SPOT_MODIFIED);
                }
                finish();
                //onBackPressed();
                return true;
            case R.id.action_delete:
                SpotBoyDBHelper spotBoyDBHelper = new SpotBoyDBHelper(Offline_InfoActivity.this, null, null, 1);
                long dbResult = spotBoyDBHelper.deleteSpot(Integer.valueOf(spot.getId()));
                if (dbResult > 0){
                    Toast.makeText(this,getString(R.string.spot_deleted_message),Toast.LENGTH_SHORT).show();
                    setResult(INFO_ACTIVITY_SPOT_DELETED);
                    finish();
                }
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public int getSelection(String[] catItems) {
        int help=0;
        for (String str : catItems){
            if (str.equalsIgnoreCase(spot.getSpotType().toString())){
                return help;
            }
            help++;
        }
        return help;
    }
}
