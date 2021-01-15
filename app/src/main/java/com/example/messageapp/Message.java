package com.example.messageapp;


import android.app.Activity;
import android.app.ListActivity;
import android.app.LoaderManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.telephony.SmsManager;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;

//import messageapp.example.com.R;


public class Message extends ListActivity implements
        LoaderManager.LoaderCallbacks<Cursor> {

    private static final int URL_LOADER = 0;
    //Contact List
    private final ArrayList<String> contacts = new ArrayList<String>();
    private final String SENT = "SMS_SENT";
    private final String DELIVERED = "SMS_DELIVERED";
    private EditText phoneReceived, smsReceived;
    private String phoneSent, smsSent;
    private BroadcastReceiver sentReceiver;
    private BroadcastReceiver deliveredReceiver;
    private Cursor cur;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.message);

        initializeList();

        registerReceivers();

        phoneReceived = (EditText) findViewById(R.id.textphone);
        smsReceived = (EditText) findViewById(R.id.textsms);

        getLoaderManager().initLoader(URL_LOADER, null, this);

    }

    private void initializeList() {


        final ContentResolver cr = getContentResolver();


        if (cur != null && cur.getCount() > 0) {
            while (cur.moveToNext()) {

                final String id = cur.getString(
                        cur.getColumnIndex(ContactsContract.Contacts._ID));
                final String name = cur.getString(
                        cur.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));

                if (Integer.parseInt(cur.getString(
                        cur.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0) {

                    final Cursor pCur = cr.query(
                            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                            null,
                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                            new String[]{id}, null);


                    while (pCur.moveToNext()) {
                        String number = pCur.getString(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                        final String deviceType = cellType(pCur.getInt(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE)));
                        number = number.replaceAll("-", "");

                        contacts.add(name + " : " + number + " \n " + deviceType);
                    }
                    pCur.close();
                }
            }
            cur.close();
        }
        //eliminating duplicates and sort list
        final HashSet hs = new HashSet();
        hs.addAll(contacts);

        contacts.clear();
        contacts.addAll(hs);

        Collections.sort(contacts);
    }

    private void autoCompleteCleaner() {
        final ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line, contacts);
        final AutoCompleteTextView textView = (AutoCompleteTextView) findViewById(R.id.textphone);
        textView.setAdapter(adapter);


        textView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            // --------- Eliminating cell type on contact field
            @Override
            public void onItemClick(AdapterView<?> av, View arg1, int index, long arg3) {

                String number = (String) av.getItemAtPosition(index);
                number = number != null ? number.replaceAll("( \n [a-zA-Z]*)", "") : "";
                textView.setText(number);

            }
        });
    }

    private String cellType(int type) {


        String stringType = "";


        switch (type) {
            case ContactsContract.CommonDataKinds.Phone.TYPE_HOME:
                stringType = "Home";
                break;
            case ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE:
                stringType = "Mobile";
                break;
            case ContactsContract.CommonDataKinds.Phone.TYPE_WORK:
                stringType = "Work";
                break;
            case ContactsContract.CommonDataKinds.Phone.TYPE_FAX_HOME:
                stringType = "Home Fax";
                break;
            case ContactsContract.CommonDataKinds.Phone.TYPE_FAX_WORK:
                stringType = "Work Fax";
                break;
            case ContactsContract.CommonDataKinds.Phone.TYPE_MAIN:
                stringType = "Main";
                break;
            case ContactsContract.CommonDataKinds.Phone.TYPE_OTHER:
                stringType = "Other";
                break;
            case ContactsContract.CommonDataKinds.Phone.TYPE_CUSTOM:
                stringType = "Custom";
                break;
            case ContactsContract.CommonDataKinds.Phone.TYPE_PAGER:
                stringType = "Pager";
                break;
        }

        return stringType;
    }


    public void buttonSendOnClick(View v) {


        phoneSent = String.valueOf(phoneReceived.getText()).replaceAll("[^0-9+]", "");

        smsSent = String.valueOf(smsReceived.getText());

        if (phoneSent.isEmpty()) {
            Toast.makeText(getBaseContext(), "Please choose a valid number.",
                    Toast.LENGTH_SHORT).show();
        } else if (smsSent.isEmpty()) {
            Toast.makeText(getBaseContext(), "Message field is empty.",
                    Toast.LENGTH_SHORT).show();
        } else {
            sendMessage();
        }
    }


    private void registerReceivers() {
        sentReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context arg0, Intent arg1) {
                switch (getResultCode()) {
                    case Activity.RESULT_OK:
                        Toast.makeText(getBaseContext(), "SMS sent",
                                Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                        Toast.makeText(getBaseContext(), "Failure",
                                Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_NO_SERVICE:
                        Toast.makeText(getBaseContext(), "No service",
                                Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_NULL_PDU:
                        Toast.makeText(getBaseContext(), "Failure",
                                Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_RADIO_OFF:
                        Toast.makeText(getBaseContext(), "Airplane mode On",
                                Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        };

        deliveredReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context arg0, Intent arg1) {
                switch (getResultCode()) {
                    case Activity.RESULT_OK:
                        Toast.makeText(getBaseContext(), "SMS delivered",
                                Toast.LENGTH_SHORT).show();
                        break;
                    case Activity.RESULT_CANCELED:
                        Toast.makeText(getBaseContext(), "SMS not delivered",
                                Toast.LENGTH_SHORT).show();
                        break;
                }

            }
        };

        //---when the SMS has been sent---
        registerReceiver(sentReceiver, new IntentFilter(SENT));

        //---when the SMS has been delivered---
        registerReceiver(deliveredReceiver, new IntentFilter(DELIVERED));
    }

    private void sendMessage() {

        final SmsManager smsMan = SmsManager.getDefault();

        final PendingIntent sentSms = PendingIntent.getBroadcast(this, 0,
                new Intent(SENT), 0);

        final PendingIntent deliveredSms = PendingIntent.getBroadcast(this, 0,
                new Intent(DELIVERED), 0);

        try {
            smsMan.sendTextMessage(phoneSent, null, smsSent, sentSms, deliveredSms);
            final ContentValues values = new ContentValues();
            values.put("address", phoneSent);
            values.put("date", System.currentTimeMillis());
            values.put("body", smsSent);
            getContentResolver().insert(Uri.parse("content://sms/sent"), values);
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }

        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (sentReceiver != null && deliveredReceiver != null) {
            unregisterReceiver(sentReceiver);
            unregisterReceiver(deliveredReceiver);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int loaderID, Bundle bundle) {
        /*
         *   Takes action based on the ID of the Loader that's being created
         */
        switch (loaderID) {
            case URL_LOADER:
                // Returns a new CursorLoader
                return new CursorLoader(
                        this,   // Parent activity context
                        ContactsContract.Contacts.CONTENT_URI,        // Table to query
                        null,     // Projection to return
                        null,            // No selection clause
                        null,            // No selection arguments
                        null             // Default sort order
                );
            default:
                // An invalid id was passed in
                return null;
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

        cur = data;

        initializeList();
        autoCompleteCleaner();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
}
