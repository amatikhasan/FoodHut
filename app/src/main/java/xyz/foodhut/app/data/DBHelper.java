package xyz.foodhut.app.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;

import xyz.foodhut.app.model.MenuProvider;


/**
 * Created by User on 6/5/2017.
 */

public class DBHelper extends SQLiteOpenHelper {

    private static String DATABASE_NAME = "mydb.db";
    private static String TABLE_NAME_LIST= "list";
    private static String TABLE_NAME_PURCHASED= "purchased";
    private static String ID = "id";
    private static String Name = "name";
    private static String Type = "quantity";
    private static String Desc = "desc";
    private static String Image = "image";
    private static String Price = "price";
    private static String Date = "date";
    private static String IsPurchased = "is_purchased";
    String isPurchasedFalse="false";
    String isPurchasedTrue="true";


    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String query = "create table " + TABLE_NAME_LIST + " ( " + ID + " integer primary key autoincrement, " + Name + " text not null, " + Type + " text not null, " + Price + " text, " + Image + " blob)";
        db.execSQL(query);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        String query = " Drop table if exists " + TABLE_NAME_LIST;
        db.execSQL(query);
    }


    public int addMenu(MenuProvider data) {
        SQLiteDatabase sd = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(Name, data.name);
        values.put(Type, data.type);
        values.put(Price, data.price);
       // values.put(Image, data.image);
        values.put(Desc, data.desc);


        long id = sd.insert(TABLE_NAME_LIST, null, values);
        sd.close();
        Log.d("check", String.valueOf(id));
        return (int) id;
    }

    public void updateMenu(MenuProvider data) {
        SQLiteDatabase sd = getWritableDatabase();
        ContentValues values = new ContentValues();

        //int code=data.id;

        values.put(Name, data.name);
        values.put(Type, data.type);
        values.put(Price, data.price);
        //values.put(Image, data.image);
        values.put(Desc, data.desc);

        //long id = sd.update(TABLE_NAME_LIST, values,"id=" + code, null );
        sd.close();
        //Log.d("check", String.valueOf(id));
        //return (int) id;
    }

    public ArrayList<MenuProvider> showMenu() {
        SQLiteDatabase sd = getReadableDatabase();
        String query = "Select * from " + TABLE_NAME_LIST + "  ";
        Cursor cur = sd.rawQuery(query, null);
        ArrayList<MenuProvider> data = new ArrayList<>();

        cur.moveToFirst();

        if (cur.moveToFirst()) {
            do {
                int code = cur.getInt(0);
                String name = cur.getString(1);
                String type = cur.getString(2);
                String price = cur.getString(3);
                byte[] image = cur.getBlob(4);
                String desc = cur.getString(3);
                //String note = cur.getString(5);
                //String active = cur.getString(6);
                //data.add(new MenuProvider(code,name,type,price,desc,image));

            } while (cur.moveToNext());
        }
        cur.close();
        return data;
    }

    public void deleteMenu(int id) {
        SQLiteDatabase sd = getWritableDatabase();
        String query = " Delete from " + TABLE_NAME_LIST + " where " + ID + " = '" + id + "'";
       sd.execSQL(query);
        sd.close();
    }


    public ArrayList<MenuProvider> filterPurchase() {
        SQLiteDatabase sd = getReadableDatabase();
        String query = "Select * from " + TABLE_NAME_PURCHASED + " where date   ";
        Cursor cur = sd.rawQuery(query, null);
        ArrayList<MenuProvider> data = new ArrayList<>();

        cur.moveToFirst();

        if (cur.moveToFirst()) {
            do {
                int code = cur.getInt(0);
                String name = cur.getString(1);
                String quantity = cur.getString(2);

                String price = cur.getString(3);
                byte[] image = cur.getBlob(4);
                String date = cur.getString(5);
                //String note = cur.getString(5);
                //String active = cur.getString(6);
                //data.add(new MenuProvider(code,name,quantity,price,date,image));

            } while (cur.moveToNext());
        }
        cur.close();
        return data;
    }

    public int getPricePerMonth(String month) {
        SQLiteDatabase sd = getReadableDatabase();
        String query = "Select sum(price) as price from " + TABLE_NAME_PURCHASED + " where date like '%"+month+"%'  ";
        Cursor cur = sd.rawQuery(query, null);
        int price = 0;

        cur.moveToFirst();

        if (cur.moveToFirst()) {
            do {
                price = cur.getInt(0);
                Log.d("check", "getMonth: "+price);

            } while (cur.moveToNext());
        }
        cur.close();
        return price;
    }




}
