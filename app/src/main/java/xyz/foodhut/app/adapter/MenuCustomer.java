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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

import xyz.foodhut.app.R;

import xyz.foodhut.app.data.StaticConfig;
import xyz.foodhut.app.ui.customer.MenuDetails;

import static android.content.ContentValues.TAG;

public class MenuCustomer extends RecyclerView.Adapter<MenuCustomer.ViewHolder> {
    private Context contex;
    private ArrayList<xyz.foodhut.app.model.MenuCustomer> data;
    private ArrayList<xyz.foodhut.app.model.MenuCustomer> arraylist;
    private float menuRating;

    public MenuCustomer(Context contex, ArrayList<xyz.foodhut.app.model.MenuCustomer> data) {
        this.contex = contex;
        this.data = data;
        //this.arraylist=new ArrayList<MenuCustomer>();
        //this.arraylist.addAll(data);
        this.arraylist = data;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(contex);
        View view = inflater.inflate(R.layout.menu_item_customer, parent, false);
        return new ViewHolder(view);
    }


    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {

        final xyz.foodhut.app.model.MenuCustomer obj = data.get(position);
        Log.d("check", "onBindViewHolder: " + obj.name + " " + obj.id + " " + obj.imageUrl);
        Log.d("check", "onBindViewHolder: Extra " + obj.extraItem);

        String price = "৳ " + obj.price;
        holder.foodName.setText(obj.name);
        holder.price.setText(price);
        //  holder.type.setText(obj.type);
        //  holder.date.setText(obj.schedule);
        holder.providerName.setText(obj.providerName);
        holder.providerAddress.setText(obj.providerAddress);
        //   holder.ratingCount.setText(String.valueOf(obj.ratingCount));
        checkRating(obj.providerId, obj.id, position, holder);

        //holder.ratingBar.setRating((float) obj.rating);
       // Transformation transformation=new RoundedTransformationBuilder().cornerRadius(12).oval(false).build();

        Picasso.get().load(obj.imageUrl).placeholder(R.drawable.image).into(holder.image);

        //Glide.with(contex).load(obj.imageUrl).into(holder.image);

        checkFav(obj.scheduleId,holder);

        holder.fav.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                FirebaseDatabase.getInstance().getReference().child("customers/" + StaticConfig.UID + "/favourites").child(obj.scheduleId)
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                //fetch files from firebase database and push in arraylist
                                String key=FirebaseDatabase.getInstance().getReference().push().getKey();
                                if (dataSnapshot.getValue() == null) {
                                    holder.fav.setImageDrawable(contex.getResources().getDrawable(R.drawable.fav_64_filled_red));
                                    FirebaseDatabase.getInstance().getReference().child("customers/" + StaticConfig.UID + "/favourites").child(obj.scheduleId).setValue(obj.id);
                                 }
                                if (dataSnapshot.getValue() != null) {
                                    holder.fav.setImageDrawable(contex.getResources().getDrawable(R.drawable.fav_64_empty_white));
                                    FirebaseDatabase.getInstance().getReference().child("customers/" + StaticConfig.UID + "/favourites").child(obj.scheduleId).removeValue();
                                }


                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
            }
        });

        holder.card.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(contex, MenuDetails.class);
                intent.putExtra("name", obj.name);
                intent.putExtra("price", obj.price);
                intent.putExtra("menuId", obj.id);
               // intent.putExtra("scheduleId", obj.scheduleId);
                intent.putExtra("desc", obj.desc);
                intent.putExtra("extraItem", obj.extraItem);
                intent.putExtra("extraItemPrice", obj.extraItemPrice);
                intent.putExtra("type", obj.type);
                intent.putExtra("imageUrl", obj.imageUrl);
                intent.putExtra("date", obj.schedule);
                intent.putExtra("providerName", obj.providerName);
                intent.putExtra("providerAddress", obj.providerAddress);
                intent.putExtra("providerPhone", obj.providerPhone);
                intent.putExtra("providerId", obj.providerId);
                intent.putExtra("rating", obj.rating);
                intent.putExtra("ratingCount", obj.ratingCount);


                Log.d(TAG, "onClick: name: " + obj.name + " id: " + obj.id + " url " + obj.imageUrl);

                contex.startActivity(intent);

            }
        });

    }

    public void checkRating(String providerId, String menuId, int position, final ViewHolder holder) {
        FirebaseDatabase.getInstance().getReference().child("providers/" + providerId).child("menu").child(menuId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        //fetch files from firebase database and push in arraylist
                        if (dataSnapshot.getValue() != null) {

                            HashMap hashUser = (HashMap) dataSnapshot.getValue();
                            menuRating = Float.parseFloat((String) hashUser.get("rating"));

                            holder.ratingBar.setRating(menuRating);

                            Log.d("check", "rating: " + menuRating);

                        }

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }

    public void checkFav(String sheduleId, final ViewHolder holder){
        Log.d(TAG, "checkFav: "+StaticConfig.UID+" "+sheduleId);
        FirebaseDatabase.getInstance().getReference().child("customers/" + StaticConfig.UID + "/favourites").child(sheduleId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        //fetch files from firebase database and push in arraylist

                        if (dataSnapshot.getValue() != null) {
                            holder.fav.setImageDrawable(contex.getResources().getDrawable(R.drawable.fav_64_filled_red));
                        }
                        if (dataSnapshot.getValue() == null) {
                            holder.fav.setImageDrawable(contex.getResources().getDrawable(R.drawable.fav_64_empty_white));
                        }


                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }

    @Override
    public int getItemCount() {
        // return data.size();
        return data != null ? data.size() : 0;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView foodName, price, type, desc, ratingCount, date, providerName, providerAddress;
        RatingBar ratingBar;
        ImageView image, providerAvatar;
        CardView card;
        ImageView fav;


        public ViewHolder(View itemView) {
            super(itemView);
            foodName = itemView.findViewById(R.id.mFoodName);
            price = itemView.findViewById(R.id.mFoodPrice);
            // type = itemView.findViewById(R.id.mFoodType);
            // desc = itemView.findViewById(R.id.mFoodDesc);
            providerName = itemView.findViewById(R.id.mProviderName);
            providerAddress = itemView.findViewById(R.id.mProviderAddress);
            // date = itemView.findViewById(R.id.mDate);
            //  ratingCount = itemView.findViewById(R.id.mRatingCount);
            ratingBar = itemView.findViewById(R.id.mRatingBar);
            image = (ImageView) itemView.findViewById(R.id.mItemImage);
            providerAvatar = (ImageView) itemView.findViewById(R.id.providerAvatar);
            fav = itemView.findViewById(R.id.myFav);
            card = itemView.findViewById(R.id.cardMenu);
        }

    }

    // Filter Class to filter and show the result in list view
    public void filter(String filter, String charText) {
        charText = charText.toLowerCase(Locale.getDefault());
        Log.d("check", "in filter: " + filter + " " + charText);

        data.clear();
        if (charText.length() == 0) {
            data.addAll(arraylist);
        } else {
            if (filter.equals("all")) {
                data.addAll(arraylist);
                notifyDataSetChanged();
            }
            if (filter.equals("location")) {
                for (xyz.foodhut.app.model.MenuCustomer wp : arraylist) {
                    if (wp.location.toLowerCase(Locale.getDefault()).contains(charText)) {
                        data.add(wp);
                        notifyDataSetChanged();
                    }
                }
            }

            if (filter.equals("type")) {
                for (xyz.foodhut.app.model.MenuCustomer wp : arraylist) {
                    if (wp.type.toLowerCase(Locale.getDefault()).contains(charText)) {
                        data.add(wp);
                        notifyDataSetChanged();
                    }
                }
            }
        }
        notifyDataSetChanged();
    }

}
