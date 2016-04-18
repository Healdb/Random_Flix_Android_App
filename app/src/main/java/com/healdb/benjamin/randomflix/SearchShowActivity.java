package com.healdb.benjamin.randomflix;

import android.annotation.TargetApi;
import android.content.Context;
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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;

import com.healdb.benjamin.randomflix.R;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class SearchShowActivity extends AppCompatActivity {
    StableArrayAdapter adapter;
    public final static String EXTRA_MESSAGE = "com.healdb.benjamin.randomflix.SEARCH";
    final ArrayList<String> nameList = new ArrayList<String>();
    final ArrayList<String> dataList = new ArrayList<String>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_show);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        EditText inputSearch = (EditText) findViewById(R.id.input_search);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        AssetManager assetManager = getAssets();
        try{
            InputStream input = assetManager.open("showtitles.txt");
            BufferedReader r = new BufferedReader(new InputStreamReader(input));
            //StringBuilder total = new StringBuilder();
            String line;
            while ((line = r.readLine()) != null) {
                //String[] values = line.split("($)");
                //.out.println(line.substring(0,line.indexOf("($)")));
                //line.substring(0,line.indexOf("($)"));
                nameList.add(line.substring(0,line.indexOf("($)")));
                dataList.add(line);
            }
        }
        catch(IOException ioe){

        }
        final ListView listview = (ListView) findViewById(R.id.listview);
        adapter = new StableArrayAdapter(this,
                android.R.layout.simple_list_item_1, nameList);
        listview.setAdapter(adapter);

        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
            @Override
            public void onItemClick(AdapterView<?> parent, final View view, int position, long id) {
                final String item = (String) parent.getItemAtPosition(position);
                int index = nameList.indexOf(item);
                showShow(dataList.get(index));
            }

        });
        /**
         * Enabling Search Filter
         * */
        inputSearch.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence cs, int arg1, int arg2, int arg3) {
                // When user changed the Text
                SearchShowActivity.this.adapter.getFilter().filter(cs);
            }

            @Override
            public void beforeTextChanged(CharSequence arg0, int arg1, int arg2,
                                          int arg3) {
                // TODO Auto-generated method stub

            }

            @Override
            public void afterTextChanged(Editable arg0) {
                // TODO Auto-generated method stub
            }
        });
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

    /** Called when the user clicks the Submit button */
   /* public void sendMessage(View view) {
        Intent intent = new Intent(this, DisplayMessageActivity.class);
        EditText editText = (EditText) findViewById(R.id.edit_message);
        String message = editText.getText().toString();
        intent.putExtra(EXTRA_MESSAGE, message);
        startActivity(intent);
    }*/

}
