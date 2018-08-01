package xyz.foodhut.app.model;

public class MenuProvider {

    public String name;
    public String id;
    public String imageUrl;
    public String type;
    public String price;
    public String desc;
    public String extraItem;
    public String extraItemPrice;
    public float rating;
    public int ratingCount;

    /*
    public MenuProvider(int id,String name,String type, String price, String desc, byte[] image) {
        this.name = name;
        this.image = image;
        this.id = id;
        this.type = type;
        this.price = price;
        this.desc = desc;
    }  */

    public MenuProvider(String id, String name, String type, String price, String desc,String extraItem,String extraItemPrice, String imageUrl, float rating, int ratingCount) {
        this.name = name;
        this.imageUrl = imageUrl;
        this.id = id;
        this.type = type;
        this.price = price;
        this.desc = desc;
        this.rating=rating;
        this.ratingCount=ratingCount;
        this.extraItem=extraItem;
        this.extraItemPrice=extraItemPrice;
    }

    public MenuProvider() {
    }
}