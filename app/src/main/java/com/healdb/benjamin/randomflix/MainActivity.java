package com.healdb.benjamin.randomflix;

import android.annotation.TargetApi;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.InputStream;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.AssetManager;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.healdb.benjamin.randomflix.R;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.zip.CheckedOutputStream;

public class MainActivity extends AppCompatActivity {
    public final static String EXTRA_MESSAGE = "com.healdb.benjamin.randomflix.SEARCH";
    StableArrayAdapter adapter;
    public static ArrayList<String> favorites= new ArrayList<String>();
    public static ArrayList<String> data= new ArrayList<String>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goToSearchPage();
            }
        });
        setSupportActionBar(toolbar);
        //favorites.add("Star Trek");
        //favorites.add("Frasier");
        //getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        String DATAFILENAME = "data_file.txt";
        String NAMESFILENAME = "names_file.txt";
        try {
            FileInputStream ds = openFileInput(DATAFILENAME);
            ObjectInputStream datafile = new ObjectInputStream(ds);
            FileInputStream ns = openFileInput(NAMESFILENAME);
            ObjectInputStream namesfile = new ObjectInputStream(ns);
            try {
                data = (ArrayList) datafile.readObject();
                favorites = (ArrayList) namesfile.readObject();
                datafile.close();
                namesfile.close();
            }
            catch(ClassNotFoundException cnfe){
                System.out.println("Not able to read object");
            }
        }
        catch(IOException ioe){
            System.out.println("Not initialized yet");
        }
        final ListView listview = (ListView) findViewById(R.id.flistview);
        adapter = new StableArrayAdapter(this,
                android.R.layout.simple_list_item_1, favorites);
        listview.setAdapter(adapter);

        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
            @Override
            public void onItemClick(AdapterView<?> parent, final View view, int position, long id) {
                final String item = (String) parent.getItemAtPosition(position);
                int index = favorites.indexOf(item);
                showShow(data.get(index));

            }

        });
        listview.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View v, int position, long id) {
                final String item = (String) parent.getItemAtPosition(position);
                AlertDialog.Builder adb = new AlertDialog.Builder(MainActivity.this);
                adb.setTitle("Delete?");
                adb.setMessage("Are you sure you want to delete " + item);
                adb.setNegativeButton("Cancel", null);
                adb.setPositiveButton("Ok", new AlertDialog.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        removeShow(MainActivity.this, item);
                    }
                });
                adb.show();
                adapter.notifyDataSetChanged();
                return true;
            }
        });
        //Generate AD
        /*AdView mAdView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);*/
    }
    public static void removeShow(final Context ctx, final String item){
        int index = favorites.indexOf(item);
        data.remove(index);
        favorites.remove(item);
        writeData(ctx);
    }
    public static void removeShowReset(final Context ctx, final String d){
        String item = d.substring(0, d.indexOf("($)"));
        int index = favorites.indexOf(item);
        data.remove(index);
        favorites.remove(item);
        writeData(ctx);
    }
    public void showShow(String showData){
        Intent intent = new Intent(this, DisplayMessageActivity.class);
        intent.putExtra(EXTRA_MESSAGE, showData);
        startActivity(intent);
    }
    private class StableArrayAdapter extends ArrayAdapter<String> {

        HashMap<String, Integer> mIdMap = new HashMap<String, Integer>();

        public StableArrayAdapter(Context context, int textViewResourceId,
                                  List<String> objects) {
            super(context, textViewResourceId, objects);
            for (int i = 0; i < objects.size(); ++i) {
                mIdMap.put(objects.get(i), i);
            }
        }

        @Override
        public long getItemId(int position) {
            String item = getItem(position);
            return mIdMap.get(item);
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }

    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        System.out.println("MENU");
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        System.out.println(item.getTitle());
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_shuffle_fav) {
            if(favorites.size()>0) {
                int random = (int) (Math.random() * data.size());
                String randomShow = data.get(random);
                showShow(randomShow);
                System.out.println("Shuffle fav");
            }
            else{
                AlertDialog.Builder adb = new AlertDialog.Builder(this);
                adb.setTitle("No Favorites Yet");
                adb.setMessage("You have no favorites to shuffle");
                adb.setNeutralButton("Okay", null);
                adb.show();
            }
        }
        if (id == R.id.action_shuffle_all) {
            ArrayList<String> dataList = new ArrayList<String>();
            AssetManager assetManager = getAssets();
            try{
                InputStream input = assetManager.open("showtitles.txt");
                BufferedReader r = new BufferedReader(new InputStreamReader(input));
                //StringBuilder total = new StringBuilder();
                String line;
                while ((line = r.readLine()) != null) {
                    dataList.add(line);
                }
            }
            catch(IOException ioe){

            }
            int random = (int)(Math.random()*dataList.size());
            String randomShow = dataList.get(random);
            showShow(randomShow);
            System.out.println("Shuffle all");
        }

        return super.onOptionsItemSelected(item);
    }
    public void goToSearchPage(){
        Intent intent = new Intent(this, SearchShowActivity.class);
        startActivity(intent);
    }
    public static void writeData(Context ctx){
        try{
            String DATAFILENAME = "data_file.txt";
            String NAMESFILENAME = "names_file.txt";
            FileOutputStream ds = ctx.openFileOutput(DATAFILENAME, Context.MODE_PRIVATE);
            ObjectOutputStream datafile = new ObjectOutputStream(ds);
            FileOutputStream ns = ctx.openFileOutput(NAMESFILENAME, Context.MODE_PRIVATE);
            ObjectOutputStream namesfile = new ObjectOutputStream(ns);
            datafile.writeObject(data);
            namesfile.writeObject(favorites);
            datafile.close();
            namesfile.close();
            System.out.println("Done writing to files");

        }
        catch(Exception ex){
            ex.printStackTrace();
        }
    }

    public static void addFavorite(String d, Context ctx){
        AlertDialog.Builder adb = new AlertDialog.Builder(ctx);
        String showName = d.substring(0, d.indexOf("($)"));
        favorites.add(showName);
        data.add(d);
        writeData(ctx);
    }
}
