package xyz.foodhut.app.model;

public class MenuProvider {

    public String name;
    public String id;
    public String imageUrl;
    public String type;
    public String price;
    public String sellerPrice;
    public String desc;
    public String extraItem;
    public String extraItemPrice;
    public String pkgSize;
    public String category;
    public String rating;
    public String ratingCount;

    /*
    public MenuProvider(int id,String name,String type, String price, String desc, byte[] image) {
        this.name = name;
        this.image = image;
        this.id = id;
        this.type = type;
        this.price = price;
        this.desc = desc;
    }  */

    public MenuProvider(String id, String name, String type, String price,String sellerPrice, String desc,String extraItem,String extraItemPrice,String pkgSize,String category, String imageUrl, String rating, String ratingCount) {
        this.name = name;
        this.imageUrl = imageUrl;
        this.id = id;
        this.type = type;
        this.price = price;
        this.sellerPrice = sellerPrice;
        this.desc = desc;
        this.rating=rating;
        this.ratingCount=ratingCount;
        this.extraItem=extraItem;
        this.extraItemPrice=extraItemPrice;
        this.pkgSize=pkgSize;
        this.category=category;
    }

    public MenuProvider() {
    }
}
