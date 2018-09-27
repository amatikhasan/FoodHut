package xyz.foodhut.app.model;

public class OrderDetails {

    public String status;
    public String orderId;
    public int amount;
    public int coupon;
    public String time;
    public int quantity;
    public String payment;
    public String extraItem;
    public int extraQuantity;
    public String customer;
    public String cId;
    public String cAddress;
    public String cPhone;
    public String date;
    public String statusTime;
    public String note;

  //  public  String scheduleId;
  //  public String rating;
   // public String review;



    public OrderDetails(String id, String customer, String cAddress, String cPhone,
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

    public OrderDetails(String id, String customer,String cId, String cAddress, String cPhone,
                        int quantity,String extraItem,int extraQuantity,String note,int amount,String payment,String date, String time,String status,String statusTime) {
        this.customer = customer;
        this.cId=cId;
        this.cAddress = cAddress;
        this.orderId = id;
        this.cPhone = cPhone;
        this.quantity = quantity;
        this.amount = amount;
        this.payment=payment;
        this.date=date;
        this.time=time;
        this.status=status;
        this.statusTime=statusTime;
        this.extraItem=extraItem;
        this.extraQuantity=extraQuantity;
        this.note=note;
    }

    public OrderDetails(String id, String customer,String cId, String cAddress, String cPhone,
                        int quantity,String extraItem,int extraQuantity,String note,int amount,int coupon,String payment,String date, String time,String status,String statusTime) {
        this.customer = customer;
        this.cId=cId;
        this.cAddress = cAddress;
        this.orderId = id;
        this.cPhone = cPhone;
        this.quantity = quantity;
        this.amount = amount;
        this.coupon=coupon;
        this.payment=payment;
        this.date=date;
        this.time=time;
        this.status=status;
        this.statusTime=statusTime;
        this.extraItem=extraItem;
        this.extraQuantity=extraQuantity;
        this.note=note;
    }

    public OrderDetails(){

    }
}
