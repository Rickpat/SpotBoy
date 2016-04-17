package rickpat.spotboy.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.google.gson.Gson;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import rickpat.spotboy.R;

import static rickpat.spotboy.utilities.Constants.*;

/*
* This activity manages the kml support
* its purpose is to load or remove kml files.
* */

public class KMLActivity extends AppCompatActivity implements View.OnClickListener {

    private String log = "KMLActivity";
    private AlertDialog kmlDialog;
    private List<File> fileList;

    //todo file chooser support

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(log, "onCreate");
        setContentView(R.layout.activity_kml);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_kml);
        setSupportActionBar(toolbar);
        try{
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }catch (NullPointerException e){}
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        fileList = new ArrayList<>();


        findViewById(R.id.kml_load_button).setOnClickListener(this);
        findViewById(R.id.kml_remove_button).setOnClickListener(this);
        getKMLFiles();
        createDialog();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void getKMLFiles() {
        Log.d(log, "getKMLFiles");
        File environment = new File( Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), "SPOT_BOY_KML");

        if (!environment.exists()){ //file does not exist
            if (!environment.mkdir()) {
                Log.d(log, "file creation error");
            } else {
                Log.d(log, "file created");
            }
        } else {
            Log.d(log, "file found");
            File[] files = environment.listFiles();
            for ( File file : files ) {
                if ( file.isDirectory() ) {
                    Log.d(log,"directory: " + file.getName() + " found");
                }
                if (file.getName().endsWith(".kml")) {
                    Log.d(log, file.getName() + " added");
                    fileList.add(file);
                }
            }
        }
    }

    private void createDialog() {
        Log.d(log, "createDialog");
        final ArrayAdapter<String> adapter = new ArrayAdapter<>(this,android.R.layout.select_dialog_singlechoice);
        for (File file : fileList){
            adapter.add(file.getName());
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        kmlDialog = builder
                .setNegativeButton(getText(R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        kmlDialog.dismiss();
                    }
                }).setAdapter(adapter, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Log.d(log,fileList.get(which).getName() + " chosen");
                        Intent intent = new Intent();
                        intent.putExtra(KML_FILE, new Gson().toJson(fileList.get(which)));
                        setResult(KML_RESULT_LOAD, intent);
                        finish();
                    }
                }).create();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.kml_load_button:
                Log.d(log,"load button");
                kmlDialog.show();
                break;
            case R.id.kml_remove_button:
                Log.d(log,"remove button");
                setResult(KML_RESULT_REMOVE);
                finish();
                break;
        }
    }
}
