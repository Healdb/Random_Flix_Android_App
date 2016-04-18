package com.healdb.benjamin.randomflix;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
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
    private Menu menu;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_message);
        //Create the toolbar at the top of the page
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //Get the intent that triggered this activity
        Intent intent = getIntent();
        //The show data that was passed to this activity
        String message = intent.getStringExtra(SearchShowActivity.EXTRA_MESSAGE);
        this.data=message;

        //Apply the show's data to the textview
        TextView t = (TextView) findViewById(R.id.show_name);
        //Grab the show's name the data is formatted as such - "Star Trek($)374823"
        t.setText(message.substring(0,message.indexOf("($)")));
        System.out.println(t.getText());
        //Set the page title
        toolbar.setTitle(t.getText());
        //Choose a random episode
        randomEpisode();
        /*RelativeLayout layout = (RelativeLayout) findViewById(R.id.content);
        layout.addView(textView);*/
        //Make AD
        /*AdView mAdView = (AdView) findViewById(R.id.adView2);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);*/
    }
    //This method is neccessary because the random episode button activates this from XML,
    //Not a listener
    public void randomEpisode(View view){
        randomEpisode();
    }
    //Picks a random episode
    public void randomEpisode(){
        //Grab the show ID
        String showID = data.substring(data.indexOf("($)") + 3, data.length());
        AssetManager assetManager = getAssets();
        StringBuilder total = new StringBuilder();
        try{
            //Find the show's data file in the resources
            InputStream input = assetManager.open(showID+".json");
            BufferedReader r = new BufferedReader(new InputStreamReader(input));
            String line;
            //Make one big string that Java can later read as a JSON object
            while ((line = r.readLine()) != null) {
                total.append(line);
            }
        }
        //If the file does not exist
        catch(IOException ioe){
            TextView t = (TextView) findViewById(R.id.show_data);
            t.setText("Error, please try again");
        }
        //Convert from stringbuilder to string
        String t = total.toString();
        try {
            //Read data from the show's data file
            JSONObject reader = new JSONObject(t);
            //Pick the season number
            JSONObject video  = reader.getJSONObject("video");
            JSONArray seasons = video.getJSONArray("seasons");
            int chosenSeasonNum = (int)(Math.random()*seasons.length());
            System.out.println(chosenSeasonNum);
            //Pick the episode number
            JSONObject season = (JSONObject)seasons.get(chosenSeasonNum);
            JSONArray episodes = season.getJSONArray("episodes");
            int chosenEpisodeNum = (int)(Math.random()*episodes.length());
            System.out.println(chosenEpisodeNum);

            //Get the chosen episode
            JSONObject episode = (JSONObject)episodes.get(chosenEpisodeNum);
            int episodeID = episode.getInt("id");
            String info = "S:"+(chosenSeasonNum+1) +" E:"+(chosenEpisodeNum+1)+"<br>";

            //Apply the data
            info+= (String)episode.get("title");
            info+= "<br>"+(String)episode.get("synopsis");
            info+="\n\n<br><br><a href=https://www.netflix.com/watch/"+episodeID+">\nOpen in Netflix</a>";
            TextView te = (TextView) findViewById(R.id.show_data);
            te.setText(Html.fromHtml(info));
            //Make the link clickable
            te.setMovementMethod(LinkMovementMethod.getInstance());

        }
        //If it fails
        catch(JSONException je){
            TextView te = (TextView) findViewById(R.id.show_data);
            te.setText("Error, please try again");
        }

    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.menu = menu;
        // Inflate the menu; this adds items to the toolbar if it is present.
        getMenuInflater().inflate(R.menu.menu_show, menu);
        //If the show is not in the favorites
        if (!MainActivity.data.contains(this.data)) {
            MenuItem item = menu.findItem(R.id.action_show);
            Drawable icon = ContextCompat.getDrawable(DisplayMessageActivity.this, R.mipmap.ic_star_off);
            //set the icon to off, set the title to add
            item.setIcon(icon);
            item.setTitle("Add");
        }
        //If the show is a favorite
        else{
            //Set the icon to on, set the title to remove
            MenuItem item = menu.findItem(R.id.action_show);
            Drawable icon = ContextCompat.getDrawable(DisplayMessageActivity.this, R.mipmap.ic_star_on);
            item.setIcon(icon);
            item.setTitle("Remove");
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        //If the button in the toolbar pressed is not the back button
        if(item.getTitle()!=null) {

            //If we are adding the show to our favorites
            if (item.getTitle().equals("Add")) {
                AlertDialog.Builder adb = new AlertDialog.Builder(this);
                System.out.println("Add show");
                String showName = data.substring(0, data.indexOf("($)"));
                if (!MainActivity.data.contains(this.data)) {
                    MainActivity.addFavorite(data, DisplayMessageActivity.this);
                    Drawable icon = ContextCompat.getDrawable(this, R.mipmap.ic_star_on);;
                    item.setIcon(icon);
                    item.setTitle("Remove");
                } else {
                    adb.setTitle("Already in Favorites");
                    adb.setMessage(showName + " is already in your favorites!");
                    adb.setNeutralButton("Okay", null);
                    adb.show();
                }
            }
            //If we are removing the show from the favorites
            else if (item.getTitle().equals("Remove")) {
                System.out.println("Remove show");
                AlertDialog.Builder adb = new AlertDialog.Builder(this);
                adb.setTitle("Unfavorite?");
                adb.setMessage("Are you sure you want to unfavorite " + data.substring(0, data.indexOf("($)")));
                adb.setNegativeButton("No", null);
                //Confirm removal
                adb.setPositiveButton("Yes", new AlertDialog.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        MainActivity.removeShowData(DisplayMessageActivity.this, data);
                        Drawable icon = ContextCompat.getDrawable(DisplayMessageActivity.this, R.mipmap.ic_star_off);
                        item.setIcon(icon);
                        item.setTitle("Add");
                    }
                });
                adb.show();
            }
        }
        //If the back button was pressed, kill this current activity (Basically what the native android back button does)
        else{
            this.finish();
        }

        return super.onOptionsItemSelected(item);
    }

}
