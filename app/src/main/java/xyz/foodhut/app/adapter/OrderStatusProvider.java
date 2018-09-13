package xyz.foodhut.app.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import java.util.ArrayList;
import xyz.foodhut.app.R;
import xyz.foodhut.app.model.OrderDetails;

public class OrderStatusProvider extends RecyclerView.Adapter<OrderStatusProvider.ViewHolder> {
    private Context contex;
    private ArrayList<OrderDetails> data;
    private ArrayList<String> status;

    public OrderStatusProvider(Context contex, ArrayList<OrderDetails> data, ArrayList<String> status) {
        this.contex = contex;
        this.data = data;
        this.status=status;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(contex);
        View view = inflater.inflate(R.layout.order_status_provider_layout, parent, false);
        return new ViewHolder(view);
    }


    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {

        final OrderDetails obj = data.get(position);
        Log.d("check", "onBindViewHolder: " + obj.customer + " " + obj.amount + " " + obj.status);

        String price = "amount  "+obj.amount + " TK";
        holder.customer.setText(obj.customer);
        holder.amount.setText(price);
        holder.quantity.setText(String.valueOf(obj.quantity));
        holder.extraItem.setText(obj.extraItem);
        holder.extraQty.setText(String.valueOf(obj.extraQuantity));
        String time="Expected delivery  "+obj.time;
        holder.time.setText(time);
        holder.status.setText(obj.status);
        //holder.status.setText(status.get(position));


   /*     holder.card.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

             if(user.equals("customer")) {
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

                 Log.d(TAG, "onClick: name: " + obj.name + " menuid: " + obj.menuId+" "+StaticConfig.ORDERID.get(position) + " url " + obj.imageUrl);

                  contex.startActivity(intent);
             }


            }
        });

        */


    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView customer, amount, quantity,extraItem, extraQty, time,status;
        //Button status;
        CardView card;


        public ViewHolder(View itemView) {
            super(itemView);
            customer = itemView.findViewById(R.id.osplCustomer);
            quantity = itemView.findViewById(R.id.osplQty);
            extraItem = itemView.findViewById(R.id.osplExtraItem);
            extraQty = itemView.findViewById(R.id.osplExtraQty);
            time = itemView.findViewById(R.id.osplTime);
            status = itemView.findViewById(R.id.osplBtnStatus);
            amount = itemView.findViewById(R.id.osplAmount);

            card = itemView.findViewById(R.id.cardStatus);
        }

    }


}
