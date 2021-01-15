package com.example.messageapp;



import android.app.ListActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class Settings extends ListActivity {

    String stuff[] = {"Under construction"};

    @Override
    protected void onCreate(Bundle sets) {
        super.onCreate(sets);
        setListAdapter(new ArrayAdapter<String>(Settings.this, android.R.layout.simple_list_item_checked, stuff));
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
    }

    @Override
    protected void onPause() {
        super.onPause();
        finish();
    }
}
