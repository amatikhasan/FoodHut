package xyz.foodhut.app.model;

public class OrderDetails {

    public String status;
    public String orderId;
    public int amount;
    public String time;
    public int quantity;
    public String payment;
    public String extra;
    public String customer;
    public String cAddress;
    public String cPhone;



    public OrderDetails(String id, String customer, String cAddress, String cPhone,
                        int quantity,int amount,String payment, String time,String status) {
        this.customer = customer;
        this.cAddress = cAddress;
        this.orderId = id;
        this.cPhone = cPhone;
        this.quantity = quantity;
        this.amount = amount;
        this.payment=payment;
        this.time=time;
        this.status=status;
    }
}
