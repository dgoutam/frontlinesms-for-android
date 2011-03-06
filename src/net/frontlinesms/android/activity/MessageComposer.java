/**
 * FrontlineSMS <http://www.frontlinesms.com>
 * Copyright 2010, Meta Healthcare Systems Ltd.
 *
 * This file is part of FrontlineSMS for Android.
 *
 * FrontlineSMS is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at
 * your option) any later version.
 *
 * FrontlineSMS is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser
 * General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with FrontlineSMS. If not, see <http://www.gnu.org/licenses/>.
 */
package net.frontlinesms.android.activity;

import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import net.frontlinesms.android.FrontlineSMS;
import net.frontlinesms.android.R;
import net.frontlinesms.android.model.ContactService;
import net.frontlinesms.android.model.model.Contact;

import java.util.ArrayList;
import java.util.Vector;

/**
 * @author Mathias Lin <mathias.lin@metahealthcare.com>
 */
public class MessageComposer extends BaseActivity {

    public final static int RECIPIENT_TYPE_GROUPS = 1;
    public final static int RECIPIENT_TYPE_CONTACTS = 2;

    private Vector<Contact> mContacts = new Vector<Contact>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.message_composer);

        // get recipient groups
        ArrayList<Integer> recipientIds = (ArrayList<Integer>) getIntent().
                getSerializableExtra(FrontlineSMS.EXTRA_SELECTED_ITEMS);

        String recipients = "";
        if (getIntent().getIntExtra(FrontlineSMS.EXTRA_RECIPIENT_TYPE, RECIPIENT_TYPE_GROUPS)
                == RECIPIENT_TYPE_GROUPS) {
            for (int id:recipientIds) {
                recipients += (!"".equals(recipients)?", ":"") + GroupList.groupNameCache.get(id);
            }
        } else {
            Cursor c = ContactService.getContactsById(this, recipientIds.toArray(new Integer[recipientIds.size()]));
            if (c.moveToFirst()) {
                do {
                    Contact contact = new Contact();
                    contact.setId(c.getInt(0));
                    contact.setDisplayName(c.getString(1));
                    mContacts.add(contact);
                    recipients += (!"".equals(recipients)?", ":"") + contact.getDisplayName();
                } while (c.moveToNext());
            }
        }
        ((TextView) findViewById(R.id.txt_recipients)).setText(recipients);
        findViewById(R.id.btn_send_message).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendMessage(mContacts, ((EditText)findViewById(R.id.edt_message)).getText().toString());
            }
        });
    }



    private void sendMessage(Vector<Contact> contacts, String message) {

        Log.d(TAG, "Send message to contacts: " + contacts.size());

        for (Contact contact:contacts) {

            Log.d(TAG, "Send message to contact id: " + contact.getId().toString());

            Cursor pCur = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                    null, ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                    new String[]{contact.getId().toString()}, null);

            if (pCur.moveToFirst()) {
                do {
                    String phone = pCur.getString(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                    Log.d(TAG, "Phone Number: " + phone);
                } while (pCur.moveToNext());
            }


        }
//        ContentValues values = new ContentValues();
//        values.put("address", phone);
//        values.put("body", message);
//        getContentResolver().insert(Uri.parse("content://sms/sent"), values);

//        PendingIntent pi = PendingIntent.getActivity(this, 0,
//            new Intent(this, this.getClass()), 0);
//        SmsManager sms = SmsManager.getDefault();
//        sms.sendTextMessage(phone, null, message, pi, null);
    }
}