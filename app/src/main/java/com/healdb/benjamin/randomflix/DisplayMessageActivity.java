package com.healdb.benjamin.randomflix;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.healdb.benjamin.randomflix.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class DisplayMessageActivity extends AppCompatActivity {
    private String data;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_message);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        Intent intent = getIntent();
        String message = intent.getStringExtra(SearchShowActivity.EXTRA_MESSAGE);
        this.data=message;
        TextView t = (TextView) findViewById(R.id.show_name);
        //TextView textView = new TextView(this);
        t.setText(message.substring(0,message.indexOf("($)")));
        System.out.println(t.getText());
        toolbar.setTitle(t.getText());
        randomEpisode();
        /*RelativeLayout layout = (RelativeLayout) findViewById(R.id.content);
        layout.addView(textView);*/
        //Make AD
        /*AdView mAdView = (AdView) findViewById(R.id.adView2);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);*/
    }
    public void randomEpisode(View view){
        randomEpisode();
    }
    public void randomEpisode(){
        String showID = data.substring(data.indexOf("($)") + 3, data.length());
        AssetManager assetManager = getAssets();
        StringBuilder total = new StringBuilder();
        try{
            InputStream input = assetManager.open(showID+".json");
            BufferedReader r = new BufferedReader(new InputStreamReader(input));
            String line;
            while ((line = r.readLine()) != null) {
                total.append(line);
            }
        }
        catch(IOException ioe){
            TextView t = (TextView) findViewById(R.id.show_data);
            t.setText("Error, please try again");
        }
        String t = total.toString();
        try {
            JSONObject reader = new JSONObject(t);
            JSONObject video  = reader.getJSONObject("video");
            JSONArray seasons = video.getJSONArray("seasons");
            int chosenSeasonNum = (int)(Math.random()*seasons.length());
            System.out.println(chosenSeasonNum);
            JSONObject season = (JSONObject)seasons.get(chosenSeasonNum);
            JSONArray episodes = season.getJSONArray("episodes");
            int chosenEpisodeNum = (int)(Math.random()*episodes.length());
            System.out.println(chosenEpisodeNum);
            JSONObject episode = (JSONObject)episodes.get(chosenEpisodeNum);
            int episodeID = episode.getInt("id");
            String info = "S:"+(chosenSeasonNum+1) +" E:"+(chosenEpisodeNum+1)+"<br>";
            info+= (String)episode.get("title");
            info+= "<br>"+(String)episode.get("synopsis");
            info+="\n\n<br><br><a href=https://www.netflix.com/watch/"+episodeID+">\nOpen in Netflix</a>";
            TextView te = (TextView) findViewById(R.id.show_data);
            te.setText(Html.fromHtml(info));
            te.setMovementMethod(LinkMovementMethod.getInstance());
            //System.out.println(t);

        }
        catch(JSONException je){
            TextView te = (TextView) findViewById(R.id.show_data);
            te.setText("Error, please try again");
        }

    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        if(MainActivity.data.indexOf(data)!=-1) {
            getMenuInflater().inflate(R.menu.menu_remove, menu);
        }
        else {
            getMenuInflater().inflate(R.menu.menu_show, menu);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        final boolean reload = true;
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_search) {
            AlertDialog.Builder adb = new AlertDialog.Builder(this);
            System.out.println("Add show");
            String showName = data.substring(0, data.indexOf("($)"));
            if(!MainActivity.data.contains(this.data)) {
                adb.setMessage(showName + " has been added to your favorites.");
                adb.setPositiveButton("Okay", new AlertDialog.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        MainActivity.addFavorite(data, DisplayMessageActivity.this);
                    }
                });
                adb.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        MainActivity.addFavorite(data, DisplayMessageActivity.this);
                    }
                });
            } else {
                adb.setTitle("Already in Favorites");
                adb.setMessage(showName + " is already in your favorites!");
                adb.setNeutralButton("Okay", null);
            }
            adb.show();
        }
        if (id == R.id.action_remove) {
            System.out.println("Remove show");
            AlertDialog.Builder adb = new AlertDialog.Builder(this);
            adb.setTitle("Unfavorite?");
            adb.setMessage("Are you sure you want to unfavorite " + data.substring(0, data.indexOf("($)")));
            adb.setNegativeButton("No", null);
            adb.setPositiveButton("Yes", new AlertDialog.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    MainActivity.removeShowReset(DisplayMessageActivity.this, data);
                }
            });
            adb.show();
        }

        return super.onOptionsItemSelected(item);
    }

}
