package xyz.foodhut.app.model;

public class OrderDetails {

    public String status;
    public String orderId;
    public String menuId;
    public String provider;
    public String pId;
    public int amount;
    public int sellerAmount;
    public int coupon;
    public String time;
    public int quantity;
    public String payment;
    public String paymentStatus;
    public String extraItem;
    public String itemName;
    public int extraQuantity;
    public String customer;
    public String cId;
    public String cAddress;
    public String cPhone;
    public String date;
    public String lastTime;
    public String statusTime;
    public String note;
    public int menuCount;

  //  public  String scheduleId;
  //  public String rating;
   // public String review;



  /*  public OrderDetails(String id, String customer, String cAddress, String cPhone,
                        int quantity,String extraItem,int extraQuantity,int amount,String payment, String time,String status) {
        this.customer = customer;
        this.cAddress = cAddress;
        this.orderId = id;
        this.cPhone = cPhone;
        this.quantity = quantity;
        this.amount = amount;
        this.payment=payment;
        this.time=time;
        this.status=status;
        this.extraItem=extraItem;
        this.extraQuantity=extraQuantity;
    }
    */

    public OrderDetails(String id,String menuId, String customer,String cId, String cAddress, String cPhone,String provider,String pId,int menuCount,
                        String itemName,int quantity,String extraItem,int extraQuantity,String note,int amount,String payment,String date,String lastTime, String time,String status,String statusTime) {
        this.customer = customer;
        this.cId=cId;
        this.cAddress = cAddress;
        this.orderId = id;
        this.menuId=menuId;
        this.cPhone = cPhone;
        this.provider=provider;
        this.pId=pId;
        this.quantity = quantity;
        this.menuCount = menuCount;
        this.amount = amount;
        this.payment=payment;
        this.date=date;
        this.lastTime=lastTime;
        this.time=time;
        this.status=status;
        this.statusTime=statusTime;
        this.itemName=itemName;
        this.extraItem=extraItem;
        this.extraQuantity=extraQuantity;
        this.note=note;
    }

    public OrderDetails(String id,String menuId, String customer,String cId, String cAddress, String cPhone,String provider,String pId,
                        String itemName,int quantity,String extraItem,int extraQuantity,String note,int amount,String payment,String date,String lastTime, String time,String status,String statusTime) {
        this.customer = customer;
        this.cId=cId;
        this.cAddress = cAddress;
        this.orderId = id;
        this.menuId=menuId;
        this.cPhone = cPhone;
        this.provider=provider;
        this.pId=pId;
        this.quantity = quantity;
        this.amount = amount;
        this.payment=payment;
        this.date=date;
        this.lastTime=lastTime;
        this.time=time;
        this.status=status;
        this.statusTime=statusTime;
        this.itemName=itemName;
        this.extraItem=extraItem;
        this.extraQuantity=extraQuantity;
        this.note=note;
    }

    public OrderDetails(String id,String menuId, String customer,String cId, String cAddress, String cPhone,String provider,String pId,
                        String itemName,int quantity,String extraItem,int extraQuantity,String note,int amount,int sellerAmount,int coupon,String payment,String paymentStatus,String date,String lastTime, String time,String status,String statusTime) {
        this.customer = customer;
        this.cId=cId;
        this.cAddress = cAddress;
        this.orderId = id;
        this.menuId=menuId;
        this.cPhone = cPhone;
        this.provider=provider;
        this.pId=pId;
        this.quantity = quantity;
        this.amount = amount;
        this.sellerAmount = sellerAmount;
        this.coupon=coupon;
        this.payment=payment;
        this.paymentStatus=paymentStatus;
        this.date=date;
        this.lastTime=lastTime;
        this.time=time;
        this.status=status;
        this.statusTime=statusTime;
        this.itemName=itemName;
        this.extraItem=extraItem;
        this.extraQuantity=extraQuantity;
        this.note=note;
    }

    public OrderDetails(String id,String menuId, String customer,String cId, String cAddress, String cPhone,String provider,String pId,int menuCount,
                        String itemName,int quantity,String extraItem,int extraQuantity,String note,int amount,int sellerAmount,int coupon,String payment,String paymentStatus,String date,String lastTime, String time,String status,String statusTime) {
        this.customer = customer;
        this.cId=cId;
        this.cAddress = cAddress;
        this.orderId = id;
        this.menuId=menuId;
        this.cPhone = cPhone;
        this.provider=provider;
        this.pId=pId;
        this.menuCount = menuCount;
        this.quantity = quantity;
        this.amount = amount;
        this.sellerAmount = sellerAmount;
        this.coupon=coupon;
        this.payment=payment;
        this.paymentStatus=paymentStatus;
        this.date=date;
        this.lastTime=lastTime;
        this.time=time;
        this.status=status;
        this.statusTime=statusTime;
        this.itemName=itemName;
        this.extraItem=extraItem;
        this.extraQuantity=extraQuantity;
        this.note=note;
    }

    public OrderDetails(){

    }
}
