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
import android.widget.Toast;

import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

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
        this.code=code;
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
       // Log.d("check", "onBindViewHolder: " + obj.name + " " + obj.menuId + " " + obj.imageUrl);

       // if(obj.date.equals(checkDate())) {

            String price = obj.price + " TK";
            holder.foodName.setText(obj.name);
            holder.price.setText(price);
            holder.type.setText(obj.type);
            //holder.desc.setText(obj.desc);
            Picasso.get().load(obj.imageUrl).placeholder(R.drawable.image).into(holder.image);
            //Glide.with(contex).load(obj.imageUrl).into(holder.image);

        holder.deleteSchedule.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseDatabase.getInstance().getReference().child("providers").child(StaticConfig.UID).child("schedule").child(obj.scheduleId).removeValue();
                FirebaseDatabase.getInstance().getReference().child("schedule").child(obj.scheduleId).removeValue();

                Toast.makeText(contex, "Schedule for this item is deleted", Toast.LENGTH_SHORT).show();
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

    public String checkDate(){
        // Get the calander
        int day,month,year;
        String date = null;
        Calendar c = Calendar.getInstance();

        // From calander get the year, month, day, hour, minute
        year= c.get(Calendar.YEAR);
        month = c.get(Calendar.MONTH);
        day = c.get(Calendar.DAY_OF_MONTH);

        if(code==1) {
            c.set(year, month, day);
            date = day + "-" + (month + 1) + "-" + year;
        }
        if(code==2) {
            c.set(year, month, (day+1));
            date = (day+1) + "-" + (month + 1) + "-" + year;
        }

        String formattedDate;
        SimpleDateFormat sdtf = new SimpleDateFormat("dd MMM yyyy");
        Date now = c.getTime();
        formattedDate = sdtf.format(now);

        Log.d("check", "checkDate: "+date+" "+formattedDate);

        return date;
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView foodName, price, type, desc, ratingCount;
        RatingBar ratingBar;
        ImageView image, deleteSchedule;
        CardView card;


        public ViewHolder(View itemView) {
            super(itemView);
            foodName = itemView.findViewById(R.id.mFoodName);
            price = itemView.findViewById(R.id.mFoodPrice);
            type = itemView.findViewById(R.id.mFoodType);
           //desc = itemView.findViewById(R.id.mFoodDesc);

            deleteSchedule = (ImageView) itemView.findViewById(R.id.deleteSchedule);
            image = (ImageView) itemView.findViewById(R.id.mItemImage);
            card = itemView.findViewById(R.id.cardMenu);
        }

    }


}
