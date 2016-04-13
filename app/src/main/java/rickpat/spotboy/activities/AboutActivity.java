package rickpat.spotboy.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;


import rickpat.spotboy.R;
import rickpat.spotboy.utilities.Utilities;

import me.grantland.widget.AutofitTextView;

public class AboutActivity extends AppCompatActivity {

    AlertDialog librariesDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_about);
        setSupportActionBar(toolbar);
        try{
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }catch (NullPointerException e){}
        getSupportActionBar().setDisplayShowHomeEnabled(true);


        setFont();
        createDialog();
        findViewById(R.id.about_libraries_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                librariesDialog.show();
            }
        });
    }

    private void setFont() {
        AutofitTextView txt = (AutofitTextView) findViewById(R.id.about_app_title);
        Typeface font = Typeface.createFromAsset(getAssets(), "fonts/aaaiight-fat.ttf");
        txt.setTypeface(font);
    }

    private void createDialog() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        librariesDialog = alertDialogBuilder
                .setItems(Utilities.getLibrariesStringArray(), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String[] strArray = Utilities.getLibrariesStringArray();
                        String selection = strArray[which];
                        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.google.com"+"/#q="+selection));
                        startActivity(browserIntent);
                    }
                }).create();
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
}
