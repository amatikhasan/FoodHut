package xyz.foodhut.app.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import xyz.foodhut.app.R;
import xyz.foodhut.app.data.StaticConfig;
import xyz.foodhut.app.model.OrderDetails;
import xyz.foodhut.app.model.OrderDetailsProvider;
import xyz.foodhut.app.ui.customer.OrderStatusCustomer;
import xyz.foodhut.app.ui.provider.OrderStatusProvider;

import static android.content.ContentValues.TAG;

public class OrdersCustomer extends RecyclerView.Adapter<OrdersCustomer.ViewHolder> {
    private Context contex;
    private ArrayList<OrderDetailsProvider> data;
    private ArrayList<OrderDetails> orderData;


    public OrdersCustomer(Context contex, ArrayList<OrderDetailsProvider> data,ArrayList<OrderDetails> orderData) {
        this.contex = contex;
        this.data = data;
        this.orderData=orderData;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(contex);
        View view = inflater.inflate(R.layout.order_status_customer, parent, false);
        return new ViewHolder(view);
    }


    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {

        final OrderDetailsProvider obj = data.get(position);
        final OrderDetails obj2 = orderData.get(position);
        Log.d("check", "onBindViewHolder: " + obj.name + " " + obj.menuId + " " + obj2.date);

        SimpleDateFormat dateFormat=new SimpleDateFormat("dd-MM-yyyy");
        Date date1= null;
        try {
            date1 = dateFormat.parse(obj2.date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        SimpleDateFormat newFormate=new SimpleDateFormat("dd MMM, yyyy");
        String date2=newFormate.format(date1);

        holder.foodName.setText(obj.name);
        String price= String.valueOf(obj.price);
        holder.price.setText(price);

        holder.quantity.setText(String.valueOf(obj2.quantity));
        holder.amount.setText(String.valueOf(obj2.amount));
        holder.date.setText(date2);
        holder.time.setText(obj2.statusTime);
        String orderNo="O-"+obj2.orderId;
        holder.orderNo.setText(orderNo);
        holder.status.setText(obj2.status);
       // holder.payment.setText(obj2.payment);
        holder.providerName.setText(obj.provider);
        holder.address.setText(obj2.cAddress);



    /*    holder.card.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                 Intent intent = new Intent(contex, OrderStatusCustomer.class);
                 intent.putExtra("name", obj.name);
                 intent.putExtra("price", obj.price);
                 intent.putExtra("menuId", obj.menuId);
                 intent.putExtra("provider", obj.provider);
                 intent.putExtra("type", obj.type);
                 // intent.putExtra("extraItem", obj.extraItem);
                 // intent.putExtra("extraItemPrice", obj.extraItemPrice);
                 intent.putExtra("date", StaticConfig.DATE);
                 intent.putExtra("orderId", StaticConfig.ORDERID.get(position));
                 intent.putExtra("url", obj.imageUrl);
                 intent.putExtra("providerId", obj.pId);

                 Log.d(TAG, "onClick: name: " + obj.name + " menuid: " + obj.menuId+" "+StaticConfig.ORDERID.get(position) + " url " + obj.imageUrl);

                 // contex.startActivity(intent);
             }
        });   */


    }


    @Override
    public int getItemCount() {
        return data.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView foodName, price, date,time, orderNo, providerName,status,payment,amount,quantity,address,review;
        CardView card;


        public ViewHolder(View itemView) {
            super(itemView);
            foodName = itemView.findViewById(R.id.oscFood);
            price = itemView.findViewById(R.id.oscUnitPrice);
            providerName = itemView.findViewById(R.id.oscKitchen);
            date = itemView.findViewById(R.id.oscDate);
           time = itemView.findViewById(R.id.oscTime);
            orderNo = itemView.findViewById(R.id.oscOderNo);
            status = itemView.findViewById(R.id.oscStatus);
          //  payment = itemView.findViewById(R.id.oscPayment);
            amount = itemView.findViewById(R.id.oscAmount);
            quantity = itemView.findViewById(R.id.oscQty);
            address = itemView.findViewById(R.id.oscAddress);
            review = itemView.findViewById(R.id.oscReview);

            //card = itemView.findViewById(R.id.cardMenu);
        }

    }


}
