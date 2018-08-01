package xyz.foodhut.app.model;

public class OrderMenuDetails {

    public String name;
    public String menuId;
    public String imageUrl;
    public String type;
    public int price;
    public String provider;
    public String pAddress;
    public String pPhone;
    public String pId;



    public OrderMenuDetails(String id, String name, String type, int price, String imageUrl,String pId,String provider,String pAddress) {
        this.name = name;
        this.imageUrl = imageUrl;
        this.menuId = id;
        this.type = type;
        this.price = price;
        this.provider = provider;
        this.pAddress=pAddress;
        this.pId=pId;
    }

}


