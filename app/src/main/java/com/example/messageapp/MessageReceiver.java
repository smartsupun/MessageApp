package com.example.messageapp;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.telephony.SmsMessage;
import android.widget.Toast;



public class MessageReceiver extends BroadcastReceiver {

    private ContentResolver contentResolver;

    @Override
    public void onReceive(Context context, Intent intent) {

        String sms = "", phone = "";
        final Bundle bundle = intent.getExtras();
        final SmsMessage[] messages;
        contentResolver = context.getContentResolver();
        ContentValues values;

        if (bundle != null) {

            final Object[] pdus = (Object[]) bundle.get("pdus");
            messages = new SmsMessage[pdus.length];
            for (int i = 0; i < messages.length; i++) {
                messages[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
                phone += messages[i].getOriginatingAddress();
                sms += messages[i].getMessageBody();
                values = new ContentValues();
                values.put("address", phone);
                values.put("date", System.currentTimeMillis());
                values.put("body", sms);
                contentResolver.insert(Uri.parse("content://sms/inbox"), values);
            }

            Toast.makeText(context, "You have a new message from - " + getContactName(phone) + " .", Toast.LENGTH_SHORT).show();
        }

    }

    private String getContactName(String number) {
        final String name;
        final Uri uri_cont = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(number));

        if (uri_cont != null) {
            final Cursor cs = contentResolver.query(uri_cont, new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME}, ContactsContract.PhoneLookup.NUMBER + "='" + number + "'", null, null);

            if (cs != null && cs.moveToFirst()) {
                name = cs.getString(cs.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME));
                cs.close();
            } else {
                name = number;
            }
        } else {
            name = number;
        }
        return name;
    }
}
