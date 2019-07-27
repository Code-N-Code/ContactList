package com.codencode.contactlist;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.Toast;

import java.util.ArrayList;

public class MySQLiteHelper extends SQLiteOpenHelper {
    Context context;
    final static private String DATABASE_NAME = "ContactDetails";
    final static private String TABLE_NAME = "ContactDetails";
    final static private String UID = "_id";
    final static private int VERSION = 2;
    final static private String NAME_COLUMN = "Name";
    final static private String PHONE_COLUMN = "Phone";
    final static private String EMAIL_COLUMN = "Email";
    final static private String URI_COLUMN = "Uri";

    final static private String CREATE_TABLE = "CREATE TABLE " +  TABLE_NAME  + " ( "+UID+ " INTEGER PRIMARY KEY AUTOINCREMENT , "+ NAME_COLUMN + " VARCHAR(255) , " + PHONE_COLUMN + " VARCHAR(255) , " + EMAIL_COLUMN+" VARCHAR(255) , "+URI_COLUMN+" VARCHAR(255) ) ;" ;
    final static private String DROP_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;
    public MySQLiteHelper(Context context) {
        super(context, DATABASE_NAME, null, VERSION);
        this.context = context;
    }

    public long insertData(String name , String phone , String email, String imgUri)
    {
        ContentValues contentValues = new ContentValues();
        contentValues.put(NAME_COLUMN , name);
        contentValues.put(PHONE_COLUMN , phone);
        contentValues.put(EMAIL_COLUMN , email);
        contentValues.put(URI_COLUMN , imgUri);
        long id = getWritableDatabase().insert(TABLE_NAME , null , contentValues);
        return id;
    }

    public ArrayList<ContactInfo> loadData()
    {
        ArrayList<ContactInfo> _contacts = new ArrayList<>();
        SQLiteDatabase db = getWritableDatabase();
        String[] columns = {NAME_COLUMN , PHONE_COLUMN , EMAIL_COLUMN , URI_COLUMN};
        Cursor cursor = db.query(TABLE_NAME , columns , null , null , null , null , NAME_COLUMN + " ASC");

        while(cursor.moveToNext())
        {
            ContactInfo info = new ContactInfo();
            int nameIdx = cursor.getColumnIndex(NAME_COLUMN);
            int phoneIdx = cursor.getColumnIndex(PHONE_COLUMN);
            int emailIdx = cursor.getColumnIndex(EMAIL_COLUMN);
            int uriIdx   = cursor.getColumnIndex(URI_COLUMN);

            info.setContactName(cursor.getString(nameIdx));
            info.setPhoneNumber(cursor.getString(phoneIdx));
            info.setEmailAddress(cursor.getString(emailIdx));
            info.setImgURI(cursor.getString(uriIdx));

            _contacts.add(info);
        }
        return _contacts;
    }

    public int update(ContactInfo oldInfo , ContactInfo newInfo)
    {
        SQLiteDatabase database = getWritableDatabase();
        ContentValues contentValues = new ContentValues();

        contentValues.put(NAME_COLUMN , newInfo.getContactName());
        contentValues.put(PHONE_COLUMN , newInfo.getPhoneNumber());
        contentValues.put(URI_COLUMN , newInfo.getImgURI());
        contentValues.put(EMAIL_COLUMN , newInfo.getEmailAddress());

        String whereClause = NAME_COLUMN + " =? AND " + PHONE_COLUMN + " =? ";
        String[] whereArgs = {oldInfo.getContactName() , oldInfo.getPhoneNumber()};

        int val = database.update(TABLE_NAME , contentValues ,whereClause , whereArgs);
        return val;
    }

    public int delete(ContactInfo info)
    {
        SQLiteDatabase database = getWritableDatabase();
        String whereClause = NAME_COLUMN + " =? AND " + PHONE_COLUMN + " =? ";
        String[] whereArgs = {info.getContactName() , info.getPhoneNumber()};
        int deletedCount = database.delete(TABLE_NAME , whereClause ,whereArgs);
        return deletedCount;
    }
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(DROP_TABLE);
        onCreate(db);
    }
}
