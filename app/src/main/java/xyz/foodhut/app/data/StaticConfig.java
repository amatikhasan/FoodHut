package xyz.foodhut.app.data;


import android.graphics.Bitmap;
import android.util.SparseIntArray;

import java.util.ArrayList;
import java.util.HashMap;

import xyz.foodhut.app.model.MenuCustomer;
import xyz.foodhut.app.model.OrderDetails;
import xyz.foodhut.app.model.OrderItem;
import xyz.foodhut.app.model.OrderMenuDetails;

public class StaticConfig {

    public static Bitmap BITMAP=null;

    public static int SUBTOTAL = 0;
    public static int SUBTOTALOrder = 0;
    public static int TOTAL = 0;
    public static int SELLERTOTAL = 0;
    public static int QTY = 0;

    public static int COUPON = 0;

    public static ArrayList<MenuCustomer> ORDERITEMLIST=new ArrayList<>();
    public static ArrayList<Integer> ITEMQTYLIST=new ArrayList<>();
    public static ArrayList<Integer> EXTRAQTYLIST=new ArrayList<>();
    public static SparseIntArray INDEXLIST=new SparseIntArray();
    public static String SCHEDULEFORONEITEM = "";
    public static int ISORDERED=0;

    public static String STR_DEFAULT_BASE64 = "default";
    public static String UID = "";
    public static String NAME = "";
    public static String KITCHENNAME = "";
    public static String ADDRESS = "";
    public static String LOCATION = "";
    public static String AVATAR = "";
    public static String PHONE = "";
    public static String DATE = "";
    public static String LATITUDE ;
    public static String LONGITUDE ;




}
