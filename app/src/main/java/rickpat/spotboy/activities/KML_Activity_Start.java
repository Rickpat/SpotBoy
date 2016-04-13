package rickpat.spotboy.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.google.gson.Gson;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import rickpat.spotboy.R;

import static rickpat.spotboy.utilities.Constants.*;

public class KML_Activity_Start extends AppCompatActivity implements View.OnClickListener {

    private String log = "KML_Activity_Start";
    private AlertDialog kmlFileDialog;
    private ArrayAdapter<File> kmlArrayAdapter;
    private List<File> kmlFileList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(log, "onCreate");
        setContentView(R.layout.activity_kml);
        findViewById(R.id.kml_create_kml_button).setOnClickListener(this);
        findViewById(R.id.kml_load_kml_button).setOnClickListener(this);
        findViewById(R.id.kml_remove_current_kml_overlay).setOnClickListener(this);
        kmlFileList = getKMLFileList();
        createKMLArrayAdapter(kmlFileList);
        createKMLFileDialog();
    }

    private void createKMLArrayAdapter(List<File> kmlFileList) {
        kmlArrayAdapter = new ArrayAdapter<>(KML_Activity_Start.this,android.R.layout.select_dialog_singlechoice);
        if(!kmlFileList.isEmpty()){
            for (File file : kmlFileList){
                kmlArrayAdapter.add(file);
            }
        }
    }

    private void createKMLFileDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        kmlFileDialog = builder
                .setTitle(getString(R.string.kml_file_dialog_title))
                .setAdapter(kmlArrayAdapter, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Log.d(log, kmlFileList.get(which).getName() + " chosen");
                        kmlFileDialog.dismiss();
                        Intent loadIntent = new Intent();
                        loadIntent.putExtra(KML_URL, new Gson().toJson(kmlFileList.get(which)));
                        setResult(KML_ACTIVITY_RESULT_LOAD, loadIntent);
                        finish();

                    }
                })
                .create();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.kml_create_kml_button:
                Log.d(log,"create button onclick");
                setResult(KML_ACTIVITY_RESULT_CREATE);
                finish();
                break;
            case R.id.kml_load_kml_button:
                Log.d(log, "load button onclick");
                if (!kmlFileList.isEmpty()){
                    kmlFileDialog.show();
                }else {
                    Toast.makeText(this,getString(R.string.no_file_found_message),Toast.LENGTH_SHORT).show();
                }

                break;
            case R.id.kml_remove_current_kml_overlay:
                setResult(KML_ACTIVITY_RESULT_REMOVE);
                finish();
        }
    }

    private List<File> getKMLFileList() {
        List <File> fileList = new ArrayList<>();
        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOCUMENTS), "SPOT_BOY_KML");

        if (!mediaStorageDir.exists()) {
            Log.d(log, "SPOT_BOY_KML not found");
            if (!mediaStorageDir.mkdirs()) {
                Log.d(log, "failed to create directory");
                return null;
            }else {
                Log.d(log, "SPOT_BOY_KML folder created");
            }
        } else {
            Log.d(log, "SPOT_BOY_KML found");
            fileList = getKMLFileUrlList(mediaStorageDir);
        }
        return fileList;
    }

    private List<File> getKMLFileUrlList(File mediaStorageDir) {
        List<File> fileList = new ArrayList<>();
        File[] fileArray = mediaStorageDir.listFiles();
        for (File file : fileArray){
            if (file.isDirectory()){
                Log.d(log,"directory " + file.toString() + " found");
            }

            if (file.getName().endsWith(".kml")){
                Log.d(log,"kml file: " + file.getName() + " found and added");
                fileList.add(file);
            }
        }

        return fileList;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

    }
}
