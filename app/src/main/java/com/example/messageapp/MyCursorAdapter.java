package com.example.messageapp;


import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;
//import messageapp.example.com.R;


public class MyCursorAdapter extends CursorAdapter {

    public MyCursorAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {

        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        return inflater.inflate(R.layout.item_left, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        String[] uriSms = new String[]{
                "content://sms/inbox",
                "content://sms/sent",
                "content://sms/drafts",
                "content://sms/outbox",
                "content://sms/failed"};
        String thread = cursor.getString(cursor.getColumnIndexOrThrow("thread_id"));
        String number = "";
        String name = "";

        int counter = 0;
        while (number.equals("") && counter < 5) {
            number = getPhoneNumber(uriSms[counter], thread, context);
            counter++;
        }

        // arranjar para suportar varios numeros
        if (!number.equals("")) {
            name = getContactName(number, context);
        }

        TextView contactName = (TextView) view.findViewById(R.id.leftName);
        contactName.setText(name);

        String tempString = cursor.getString(cursor.getColumnIndexOrThrow("body"));
        TextView smsBody = (TextView) view.findViewById(R.id.leftText);
        if (tempString != null && tempString.length() > 100) {
            smsBody.setText(tempString.substring(0, Math.min(tempString.length(), 100)) + " ...");
        } else if (tempString != null) {
            smsBody.setText(tempString);
        }


    }

    public String getPhoneNumber(String uriString, String thread, Context context) {
        Uri uri = Uri.parse(uriString);
        String where = "thread_id=" + thread;
        Cursor cursorPhone = context.getContentResolver().query(uri, null, where, null, null);
        String phone = "";

        if (cursorPhone != null && cursorPhone.moveToFirst()) {
            phone = cursorPhone.getString(cursorPhone.getColumnIndexOrThrow("address"));
            cursorPhone.close();
        }
        return phone;
    }

    private String getContactName(String number, Context context) {
        String name;
        Uri uri_cont = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(number));

        if (uri_cont != null) {
            Cursor cs = context.getContentResolver().query(uri_cont, new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME}, ContactsContract.PhoneLookup.NUMBER + "='" + number + "'", null, null);

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

    @Override
    protected void onContentChanged() {
        super.onContentChanged();
    }
}