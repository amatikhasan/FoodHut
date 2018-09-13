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

import android.widget.TextView;

import java.util.ArrayList;

import xyz.foodhut.app.R;
import xyz.foodhut.app.ui.customer.OrdersCustomer;
import xyz.foodhut.app.ui.provider.OrdersProvider;


public class OrderDateProvider extends RecyclerView.Adapter<OrderDateProvider.ViewHolder> {
    private Context contex;
    private ArrayList<String> data=new ArrayList<>();
    private String type;

    public OrderDateProvider(Context contex, ArrayList<String> data, String type) {
        this.contex = contex;
        this.data = data;
        this.type=type;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(contex);
        View view = inflater.inflate(R.layout.orders_date_layout, parent, false);
        return new ViewHolder(view);
    }


    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {


        holder.date.setText(data.get(position));

        Log.d("check", "onBindViewHolder: data "+data.get(position));

        final String date=data.get(position);


        holder.card.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(type.equals("customer")) {
                    Intent intent = new Intent(contex, OrdersCustomer.class);
                    intent.putExtra("date", date);
                    contex.startActivity(intent);
                }
                if(type.equals("provider")) {
                    Intent intent = new Intent(contex, OrdersProvider.class);
                    intent.putExtra("date", date);
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

        TextView date;
        CardView card;


        public ViewHolder(View itemView) {
            super(itemView);
            date = itemView.findViewById(R.id.txtOrderLayout);

            card = itemView.findViewById(R.id.cardOrderProvider);
        }

    }


}
