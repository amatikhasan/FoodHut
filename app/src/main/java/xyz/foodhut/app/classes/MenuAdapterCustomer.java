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

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

import xyz.foodhut.app.R;
import xyz.foodhut.app.data.StaticConfig;
import xyz.foodhut.app.model.MenuCustomer;

import xyz.foodhut.app.ui.customer.MenuDetails;

import static android.content.ContentValues.TAG;

public class MenuAdapterCustomer extends RecyclerView.Adapter<MenuAdapterCustomer.ViewHolder> {
    private Context contex;
    private ArrayList<MenuCustomer> data;

    public MenuAdapterCustomer(Context contex, ArrayList<MenuCustomer> data) {
        this.contex = contex;
        this.data = data;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(contex);
        View view = inflater.inflate(R.layout.menu_item_customer, parent, false);
        return new ViewHolder(view);
    }


    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        final MenuCustomer obj = data.get(position);
        Log.d("check", "onBindViewHolder: " + obj.name + " " + obj.id + " " + obj.imageUrl);
        Log.d("check", "onBindViewHolder: Extra "+obj.extraItem);

        String price = obj.price + " TK";
        holder.foodName.setText(obj.name);
        holder.price.setText(price);
        holder.type.setText(obj.type);
        holder.date.setText(obj.schedule);
        holder.providerName.setText(obj.providerName);
        holder.providerAddress.setText(obj.providerAddress);
        holder.ratingCount.setText(String.valueOf(obj.ratingCount));
        holder.ratingBar.setRating((float) obj.rating);
        Picasso.get().load(obj.imageUrl).placeholder(R.drawable.image).into(holder.image);

        //Glide.with(contex).load(obj.imageUrl).into(holder.image);


        holder.card.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(contex, MenuDetails.class);
                intent.putExtra("name", obj.name);
                intent.putExtra("price", obj.price);
                intent.putExtra("menuId", obj.id);
                intent.putExtra("desc", obj.desc);
                intent.putExtra("extraItem", obj.extraItem);
                intent.putExtra("extraItemPrice", obj.extraItemPrice);
                intent.putExtra("type", obj.type);
                intent.putExtra("imageUrl", obj.imageUrl);
                intent.putExtra("date", obj.schedule);
                intent.putExtra("providerName", obj.providerName);
                intent.putExtra("providerAddress", obj.providerAddress);
              //  intent.putExtra("providerPhone", obj.providerPhone);
                intent.putExtra("providerId", obj.providerId);
                intent.putExtra("rating", obj.rating);
                intent.putExtra("ratingCount", obj.ratingCount);


                Log.d(TAG, "onClick: name: " + obj.name + " id: " + obj.id + " url " + obj.imageUrl);

                contex.startActivity(intent);

            }
        });

    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView foodName, price, type, desc, ratingCount,date,providerName,providerAddress;
        RatingBar ratingBar;
        ImageView image, providerAvatar;
        CardView card;


        public ViewHolder(View itemView) {
            super(itemView);
            foodName = itemView.findViewById(R.id.mFoodName);
            price = itemView.findViewById(R.id.mFoodPrice);
            type = itemView.findViewById(R.id.mFoodType);
           // desc = itemView.findViewById(R.id.mFoodDesc);
            providerName = itemView.findViewById(R.id.mProviderName);
            providerAddress = itemView.findViewById(R.id.mProviderAddress);
            date = itemView.findViewById(R.id.mDate);
            ratingCount = itemView.findViewById(R.id.mRatingCount);
            ratingBar = itemView.findViewById(R.id.mRatingBar);
            image = (ImageView) itemView.findViewById(R.id.mItemImage);
            providerAvatar = (ImageView) itemView.findViewById(R.id.providerAvatar);
            card = itemView.findViewById(R.id.cardMenu);
        }

    }


}
