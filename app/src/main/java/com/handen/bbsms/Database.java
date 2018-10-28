package com.handen.bbsms;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class Database {
    private static SQLiteDatabase mDatabase;
    private static Context mContext;
//    ArrayList <String> smsList;

/*
    public static void init() {
        mContext = App.getContext();

        if (mContext != null)
          mDatabase = new DatabaseHelper(mContext.getApplicationContext()).getWritableDatabase();
    }
*/

//    public ArrayList<String> getSMSs() {
//        return smsList;
//    }

    public Database (Context context) {
        mContext = context.getApplicationContext();
        mDatabase = new DatabaseHelper(mContext).getWritableDatabase();
        Log.e("BBSMS", "DATABASE mDatabase=" + mDatabase.toString());
    }

    /*
    public synchronized void readAllSMS() {
        if(!isInitialyzed) {
            Uri inboxURI = Uri.parse("content://sms/inbox");
            String[] reCols = new String[]{"_id", "date", "body"};

            Cursor cur = mContext.getContentResolver().query(inboxURI, reCols, "address LIKE 'ASB.BY' ", null, null);
            if(cur != null) {
                while(cur.moveToNext()) {
                    insert (cur.getString(0), cur.getString(1), cur.getString(2));
                }
            }
            else {
                // Insert code here to report an error if the cursor is null or the provider threw an exception.
            }
        }

    }
*/
//    private void insert(String id, String date, String body) {
//        smsList.add (body);
//    }

    public static class SMS {
        public float summ;
        public String curr; // валюта
        public Integer card;
        public Date date;
        public String receiver;
        public String receiverCode;
        public float balance;
        public String result;

        public SMS(SMS sms2) {
            summ = sms2.summ;
            balance = sms2.balance;
            card = sms2.card;
            date = sms2.date;
            curr = sms2.curr;
            receiver = sms2.receiver;
            receiverCode = sms2.receiverCode;
            result = sms2.result;
        }

        public SMS(String string) {
            result = "OK";
            receiver = "";
            String[] separated = string.split(" ");
            if (separated.length > 8) {
                if (separated[0].equals("OPLATA") || separated[0].equals("NALICHNYE")) {
                    summ = Float.parseFloat(separated[1]);
                    curr = separated[2];
                    card = Integer.parseInt(separated[4].substring(1));
                    try {
                        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss"); // here set the pattern as you date in string was containing like date/month/year
                        date = sdf.parse(separated[6] + " " + separated[7]);
                        for (int i = 8; i < separated.length - 3; i++) {
                            if (receiver.length() > 0)
                                receiver += " ";
                            receiver += separated[i];
                        }
                        String sl[] = receiver.split(">");
                        if (sl.length == 3) { // убираем типа "Столовая 49>SOLIGORSK>BY
                            receiver = sl[0];
                        }
                        if (separated[separated.length - 3].equals("OSTATOK")) {
                            balance = Float.parseFloat(separated[separated.length - 2]);
                        } else {
                            result = "!OSTATOK";
                        }

                    } catch (ParseException ex) { // handle parsing exception if date string was different from the pattern applying into the SimpleDateFormat contructor
                        result = "SimpleDateFormat";
                    }
                } else {
                    if (!separated[0].equals("OTMENA"))
                        result = "!OPLATA: \"" + separated[0] + "\"";
                }
            } else {
                result = "!slit";
            }
            if (date == null)
            {
                String s = "";
            }
        }
    }

    static void updateRanames(String original, String renamed) {
//        if (mDatabase == null)
//            init ();
        if (mDatabase != null) {
            if (renamed.length() == 0 || original.equals(renamed))
                mDatabase.execSQL("delete from renames where orig=" + original);
            else {
                ContentValues initialValues = new ContentValues();
                initialValues.put("ORIG", original); // the execution is different if _id is 2
                initialValues.put("RENAMED", renamed);

                int id = (int) mDatabase.insertWithOnConflict("RENAMES", null, initialValues, SQLiteDatabase.CONFLICT_IGNORE);
                if (id == -1) {
                    mDatabase.update("RENAMES", initialValues, "ORIG=?", new String[]{original});  // number 1 is the _id here, update to variable for your code
                }
            }
        }
    }

    static String getRenames(String original) {
//        if (mDatabase == null)
//            init ();
        String ret = null;
        if (mDatabase != null) {
            String[] columns = new String[]{"RENAMED"};
            try {
                Cursor cursor = mDatabase.query(true,
                        "RENAMES",
                        columns,
                        "ORIG=" + original,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null);

                if (cursor != null) {
                    if (cursor.moveToFirst()) {
                        ret = cursor.getString(0);
                    }
                    cursor.close();
                } else
                    Log.d("BBB", "Cursor is null");

            } catch (SQLException e) {
                Log.e("BBB", "read Exception >>" + e.toString());
                e.printStackTrace();
            }
        }
        return ret;
    }

    static String getComment(SMS sms) {
//        if (mDatabase == null)
//            init ();
        String ret = "";
        if (mDatabase != null) {
            String[] columns = new String[]{"COMMENT"};
            try {
                SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss dd.MM.yyyy");
                Cursor cursor = mDatabase.query(true,
                        "COMMENTS",
                        columns,
                        "DATE=\"" + formatter.format(sms.date) + "\"",
                        null,
                        null,
                        null,
                        null,
                        null,
                        null);

                if (cursor != null) {
                    if (cursor.moveToFirst()) {
                        ret = cursor.getString(0);
                    }
                    cursor.close();
                } else
                    Log.d("BBB", "Cursor is null");

            } catch (SQLException e) {
                Log.e("BBB", "read Exception >>" + e.toString());
                e.printStackTrace();
            }
        }
        return ret;
    }

    static void updateComment(SMS sms, String comment) {
//        if (mDatabase == null)
//            init ();
        SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss dd.MM.yyyy");
        if (mDatabase != null) {
            if (comment.length() == 0)
                mDatabase.execSQL("delete from COMMENTS where DATE=\"" + formatter.format(sms.date) + "\"");
            else {
                ContentValues initialValues = new ContentValues();
                initialValues.put("DATE", formatter.format(sms.date)); // the execution is different if _id is 2
                initialValues.put("COMMENT", comment);

                int id = (int) mDatabase.insertWithOnConflict("COMMENTS", null, initialValues, SQLiteDatabase.CONFLICT_IGNORE);
                if (id == -1) {
                    mDatabase.update("COMMENTS", initialValues, "DATE=?", new String[]{formatter.format(sms.date)});  // number 1 is the _id here, update to variable for your code
                }
            }
        }
    }

    static ArrayList <String> getComments () {
//        if (mDatabase == null)
//            init ();
        ArrayList <String> ret = new ArrayList<>();
        if (mDatabase != null) {
            String[] columns = new String[]{"COMMENT", "DATE"};
            try {
                Cursor cursor = mDatabase.query(true,
                        "COMMENTS",
                        columns,
                        null,
                        null,
                        null,
                        null,
                        "comment",
                        null,
                        null);

                if (cursor != null) {
                    if (cursor.moveToFirst()) {
                        do {
                            String s = cursor.getString(0);
                            ret.add(s);
                            s =  cursor.getString(1);
                        } while (cursor.moveToNext());
                    }
                    cursor.close();
                } else
                    Log.d("BBB", "Cursor is null");

            } catch (SQLException e) {
                Log.e("BBB", "read Exception >>" + e.toString());
                e.printStackTrace();
            }
        }
        return ret;
    }
}
