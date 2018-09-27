package xyz.foodhut.app.adapter;

import android.content.Context;
import android.content.DialogInterface;
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
import android.widget.Toast;

import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import xyz.foodhut.app.R;
import xyz.foodhut.app.data.StaticConfig;
import xyz.foodhut.app.ui.provider.AddMenu;

public class ScheduleProvider extends RecyclerView.Adapter<ScheduleProvider.ViewHolder> {
    private Context contex;
    private ArrayList<xyz.foodhut.app.model.ScheduleProvider> data;
    int code;

    public ScheduleProvider(Context contex, ArrayList<xyz.foodhut.app.model.ScheduleProvider> data, int code) {
        this.contex = contex;
        this.data = data;
        this.code = code;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(contex);
        View view = inflater.inflate(R.layout.schedule_item_provider, parent, false);
        return new ViewHolder(view);
    }


    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        final xyz.foodhut.app.model.ScheduleProvider obj = data.get(position);
         Log.d("check", "schedule: " + obj.name + " " + obj.id + " " + obj.date);

        // if(obj.date.equals(checkDate())) {

        String price = "৳ " + obj.price;
        holder.foodName.setText(obj.name);
        holder.price.setText(price);
        //  holder.type.setText(obj.type);
        holder.desc.setText(obj.desc);
        Picasso.get().load(obj.imageUrl).placeholder(R.drawable.image).into(holder.image);
        //Glide.with(contex).load(obj.imageUrl).into(holder.image);

        holder.deleteSchedule.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(contex);
                builder.setMessage("Are you sure you want to delete this menu schedule?")
                        .setCancelable(false)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(final DialogInterface dialog, final int id) {

                                FirebaseDatabase.getInstance().getReference().child("providers").child(StaticConfig.UID).child("schedule").child(obj.date).child(obj.scheduleId).removeValue();
                                FirebaseDatabase.getInstance().getReference().child("schedule").child(obj.scheduleId).removeValue();
                                Toast.makeText(contex, "Schedule for this menu is deleted", Toast.LENGTH_SHORT).show();

                                notifyDataSetChanged();
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            public void onClick(final DialogInterface dialog, final int id) {
                                dialog.cancel();
                            }
                        });
                final android.app.AlertDialog alert = builder.create();
                alert.show();
                Log.d("check method", "from alert");
            }
        });

        holder.card.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(contex, AddMenu.class);
                intent.putExtra("name", obj.name);
                intent.putExtra("price", obj.price);
                //intent.putExtra("id", obj.menuId);
                //intent.putExtra("desc", obj.desc);
                intent.putExtra("type", obj.type);
                // intent.putExtra("extraItem", obj.extraItem);
                // intent.putExtra("extraItemPrice", obj.extraItemPrice);
                intent.putExtra("url", obj.imageUrl);

                //  Log.d(TAG, "onClick: name: " + obj.name + " id: " + obj.menuId + " url " + obj.imageUrl);

                //  contex.startActivity(intent);

            }
        });

        // }

    }

    public String checkDate() {
        // Get the calander
        int day, month, year;
        String date = null;
        Calendar c = Calendar.getInstance();

        // From calander get the year, month, day, hour, minute
        year = c.get(Calendar.YEAR);
        month = c.get(Calendar.MONTH);
        day = c.get(Calendar.DAY_OF_MONTH);

        if (code == 1) {
            c.set(year, month, day);
            date = day + "-" + (month + 1) + "-" + year;
        }
        if (code == 2) {
            c.set(year, month, (day + 1));
            date = (day + 1) + "-" + (month + 1) + "-" + year;
        }

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
        Date date1 = null;
        try {
            date1 = dateFormat.parse(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        String formattedDate;
        SimpleDateFormat sdtf = new SimpleDateFormat("dd MMM yyyy");
        Date now = c.getTime();
        formattedDate = sdtf.format(date1);

        Log.d("check", "checkDate: " + date + " " + formattedDate);

        return date;
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView foodName, price, type, desc, ratingCount, deleteSchedule;
        RatingBar ratingBar;
        ImageView image;
        CardView card;


        public ViewHolder(View itemView) {
            super(itemView);
            foodName = itemView.findViewById(R.id.mFoodName);
            price = itemView.findViewById(R.id.mFoodPrice);
            //  type = itemView.findViewById(R.id.mFoodType);
            desc = itemView.findViewById(R.id.mFoodDesc);

            deleteSchedule = itemView.findViewById(R.id.deleteSchedule);
            image = (ImageView) itemView.findViewById(R.id.mItemImage);
            card = itemView.findViewById(R.id.cardMenu);
        }

    }


}
