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

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import xyz.foodhut.app.R;
import xyz.foodhut.app.data.StaticConfig;
import xyz.foodhut.app.model.OrderDetailsProvider;
import xyz.foodhut.app.ui.customer.OrderStatusCustomer;
import xyz.foodhut.app.ui.provider.OrderStatusProvider;

import static android.content.ContentValues.TAG;

public class OrdersProvider extends RecyclerView.Adapter<OrdersProvider.ViewHolder> {
    private Context contex;
    private ArrayList<OrderDetailsProvider> data;
    private String user;

    public OrdersProvider(Context contex, ArrayList<OrderDetailsProvider> data, String user) {
        this.contex = contex;
        this.data = data;
        this.user=user;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(contex);
        View view = inflater.inflate(R.layout.orders_provider_layout, parent, false);
        return new ViewHolder(view);
    }


    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {

        final OrderDetailsProvider obj = data.get(position);
        Log.d("check", "onBindViewHolder: " + obj.name + " " + obj.menuId + " " + obj.imageUrl);

        String price = obj.price + " TK";
        holder.foodName.setText(obj.name);
        holder.price.setText(price);
        holder.type.setText(obj.type);
        //holder.desc.setText(obj.desc);
        Picasso.get().load(obj.imageUrl).placeholder(R.drawable.image).into(holder.image);
        //Glide.with(contex).load(obj.imageUrl).into(holder.image);


        holder.card.setOnClickListener(new View.OnClickListener() {
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
                 intent.putExtra("providerId", obj.pId);

                 Log.d(TAG, "onClick: name: " + obj.name + " menuid: " + obj.menuId+" "+StaticConfig.ORDERID.get(position) + " url " + obj.imageUrl);

                  contex.startActivity(intent);
             }
                if(user.equals("provider")) {
                    Intent intent = new Intent(contex, OrderStatusProvider.class);
                    intent.putExtra("name", obj.name);
                    intent.putExtra("price", obj.price);
                    intent.putExtra("menuId", obj.menuId);
                    //intent.putExtra("desc", obj.desc);
                    intent.putExtra("type", obj.type);
                    // intent.putExtra("extraItem", obj.extraItem);
                    // intent.putExtra("extraItemPrice", obj.extraItemPrice);
                    intent.putExtra("date", StaticConfig.DATE);
                    intent.putExtra("url", obj.imageUrl);


                    Log.d(TAG, "onClick: name: " + obj.name + " id: " + obj.menuId + " url " + obj.imageUrl);

                    contex.startActivity(intent);
                }

            }
        });


    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView foodName, price, type, desc, ratingCount;
        RatingBar ratingBar;
        ImageView image, addToSchedule;
        CardView card;


        public ViewHolder(View itemView) {
            super(itemView);
            foodName = itemView.findViewById(R.id.mFoodName);
            price = itemView.findViewById(R.id.mFoodPrice);
            type = itemView.findViewById(R.id.mFoodType);
           //desc = itemView.findViewById(R.id.mFoodDesc);

            image = (ImageView) itemView.findViewById(R.id.ivItemImage);
            card = itemView.findViewById(R.id.cardMenu);
        }

    }


}
