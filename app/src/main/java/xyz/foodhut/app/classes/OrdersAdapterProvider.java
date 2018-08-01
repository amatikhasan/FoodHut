package xyz.foodhut.app.classes;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import xyz.foodhut.app.R;
import xyz.foodhut.app.model.OrderDetailsProvider;
import xyz.foodhut.app.ui.provider.AddMenu;

import static android.content.ContentValues.TAG;

public class OrdersAdapterProvider extends RecyclerView.Adapter<OrdersAdapterProvider.ViewHolder> {
    private Context contex;
    private ArrayList<OrderDetailsProvider> data;

    public OrdersAdapterProvider(Context contex, ArrayList<OrderDetailsProvider> data) {
        this.contex = contex;
        this.data = data;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(contex);
        View view = inflater.inflate(R.layout.orders_provider_layout, parent, false);
        return new ViewHolder(view);
    }


    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

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

                Intent intent = new Intent(contex, AddMenu.class);
                intent.putExtra("name", obj.name);
                intent.putExtra("price", obj.price);
                intent.putExtra("id", obj.menuId);
                //intent.putExtra("desc", obj.desc);
                intent.putExtra("type", obj.type);
               // intent.putExtra("extraItem", obj.extraItem);
               // intent.putExtra("extraItemPrice", obj.extraItemPrice);
                intent.putExtra("url", obj.imageUrl);

                Log.d(TAG, "onClick: name: " + obj.name + " id: " + obj.menuId + " url " + obj.imageUrl);

              //  contex.startActivity(intent);

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
