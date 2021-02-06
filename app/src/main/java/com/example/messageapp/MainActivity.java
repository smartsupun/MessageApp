package com.example.messageapp;

import android.app.ListActivity;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;

public class MainActivity extends ListActivity implements
        LoaderManager.LoaderCallbacks<Cursor> {
    FloatingActionButton add_button;

    private static final int URL_LOADER = 0;
    //Keeps track of list items' thread number, to pass to contact's messages
    private final ArrayList<String> listThreadNumb = new ArrayList<String>();
    //List of array ThreadItems which will serve as list items
    private final ArrayList<ThreadItem> listThreadItems = new ArrayList<ThreadItem>();
    private Class textMessage, contactMessages;
    private Cursor threadCursor;
    //Defining ThreadItem adapter which will handle data of listview
    private ArrayAdapter<ThreadItem> threadAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        add_button = findViewById(R.id.add_button);
        add_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, Message.class);
                startActivity(intent);
            }
        });


        //layout int value not used
        threadAdapter = new ThreadListAdapter(this, 0, listThreadItems);
        setListAdapter(threadAdapter);

        getLoaderManager().initLoader(URL_LOADER, null, this);
    }

    private void createConversations() {

        final String[] uriSms = new String[]{
                "content://sms/inbox",
                "content://sms/sent",
                "content://sms/drafts",
                "content://sms/outbox",
                "content://sms/failed"};

        if (!listThreadItems.isEmpty()) {
            listThreadItems.clear();
            listThreadNumb.clear();
        }


        if (threadCursor != null && threadCursor.getCount() > 0) {

            String smsBody;
            String thread;
            String msgCount;
            String number;
            String name;

            threadCursor.moveToLast();
            do {
                smsBody = AESEngine.getInstance().decryptText(threadCursor.getString(threadCursor.getColumnIndexOrThrow("snippet")));
                thread = threadCursor.getString(threadCursor.getColumnIndexOrThrow("thread_id"));
                msgCount = threadCursor.getString(threadCursor.getColumnIndexOrThrow("msg_count"));
                number = "";
                name = "";

                listThreadNumb.add(thread);

                int counter = 0;
                while (number.equals("")) {
                    number = getPhoneNumber(uriSms[counter], thread);
                    counter++;
                }
                // arranjar para suportar varios numeros
                if (!number.equals("")) {
                    name = getContactName(number);
                }

                listThreadItems.add(new ThreadItem(name, smsBody, msgCount));
            } while (threadCursor.moveToPrevious());

            threadCursor.close();
        }

        threadAdapter.notifyDataSetChanged();
    }

    private String getContactName(String number) {
        final String name;
        final Uri uri_cont = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(number));
        final String contactName = ContactsContract.PhoneLookup.DISPLAY_NAME;
        final String[] projection = {contactName};
        final String selectedRows = ContactsContract.PhoneLookup.NUMBER + "='" + number + "'";

        if (uri_cont != null) {
            final Cursor cs = getContentResolver().query(uri_cont, projection, selectedRows, null, null);

            if (cs != null && cs.moveToFirst()) {
                name = cs.getString(cs.getColumnIndex(contactName));
                cs.close();
            } else {
                name = number;
            }
        } else {
            name = number;
        }

        return name;
    }

    private String getPhoneNumber(String uriString, String thread) {
        final Uri uri = Uri.parse(uriString);
        final String where = "thread_id=" + thread;
        final String[] projection = {"address"};
        final Cursor cursorPhone = getContentResolver().query(uri, projection, where, null, null);
        String phone = "";

        if (cursorPhone != null && cursorPhone.moveToFirst()) {
            phone = cursorPhone.getString(cursorPhone.getColumnIndexOrThrow("address"));
            cursorPhone.close();
        }
        return phone;
    }


    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

        try {
            contactMessages = Class.forName("com.example.messageapp.ContactMessages");
        } catch (ClassNotFoundException e) {
            System.err.println(e.getMessage());
        }

        final Intent contactSms = new Intent(this, contactMessages);
        contactSms.putExtra("thread", listThreadNumb.get(position));
        startActivity(contactSms);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.action_settings:
                final Intent startSettings = new Intent("messageapp.example.test.SETTINGS");
                startActivity(startSettings);
                break;
            case R.id.action_compose:
                try {
                    textMessage = Class.forName("com.example.messageapp.Message");
                } catch (ClassNotFoundException e) {
                    System.err.println(e.getMessage());
                }

                final Intent sms = new Intent(MainActivity.this, textMessage);
                startActivity(sms);
                break;

        }

        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();

        getLoaderManager().destroyLoader(URL_LOADER);
        getLoaderManager().initLoader(URL_LOADER, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int loaderID, Bundle bundle) {
        /*
         * Takes action based on the ID of the Loader that's being created
         */

        final String strUriCon = "content://sms/conversations";
        final Uri uriSmsThreads = Uri.parse(strUriCon);
        final String[] projection = {"snippet", "thread_id", "msg_count"};

        switch (loaderID) {
            case URL_LOADER:
                // Returns a new CursorLoader
                return new CursorLoader(
                        getBaseContext(),   // Parent activity context
                        uriSmsThreads,        // Table to query
                        projection,     // Projection to return
                        null,            // No selection clause
                        null,            // No selection arguments
                        "date"             // Default sort order
                );
            default:
                // An invalid id was passed in
                return null;
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

        threadCursor = data;

        createConversations();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
}
