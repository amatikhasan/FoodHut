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
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import xyz.foodhut.app.R;
import xyz.foodhut.app.data.StaticConfig;
import xyz.foodhut.app.model.MenuCustomer;
import xyz.foodhut.app.model.MenuProvider;
import xyz.foodhut.app.model.ScheduleProvider;
import xyz.foodhut.app.ui.provider.AddMenu;

import static android.content.ContentValues.TAG;

public class MenuAdapterProvider extends RecyclerView.Adapter<MenuAdapterProvider.ViewHolder> {
    private Context contex;
    private ArrayList<MenuProvider> data;

    public MenuAdapterProvider(Context contex, ArrayList<MenuProvider> data) {
        this.contex = contex;
        this.data = data;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(contex);
        View view = inflater.inflate(R.layout.menu_item_provider, parent, false);
        return new ViewHolder(view);
    }


    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        providerInfo();

        final MenuProvider obj = data.get(position);
        Log.d("check", "onBindViewHolder: " + obj.name + " " + obj.id + " " + obj.imageUrl);

        String price = obj.price + " TK";
        holder.foodName.setText(obj.name);
        holder.price.setText(price);
        holder.type.setText(obj.type);
        holder.desc.setText(obj.desc);
        holder.ratingCount.setText(String.valueOf(obj.ratingCount));
        holder.ratingBar.setRating((float) obj.rating);
        Picasso.get().load(obj.imageUrl).placeholder(R.drawable.image).into(holder.image);
        //Glide.with(contex).load(obj.imageUrl).into(holder.image);


        holder.card.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(contex, AddMenu.class);
                intent.putExtra("name", obj.name);
                intent.putExtra("price", obj.price);
                intent.putExtra("id", obj.id);
                intent.putExtra("desc", obj.desc);
                intent.putExtra("type", obj.type);
                intent.putExtra("extraItem", obj.extraItem);
                intent.putExtra("extraItemPrice", obj.extraItemPrice);
                intent.putExtra("url", obj.imageUrl);

                Log.d(TAG, "onClick: name: " + obj.name + " id: " + obj.id + " url " + obj.imageUrl);

                contex.startActivity(intent);

            }
        });

        holder.addToSchedule.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LayoutInflater layoutInflater = LayoutInflater.from(contex);
                View view = layoutInflater.inflate(R.layout.add_schedule_input_layout, null);

                AlertDialog.Builder builder = new AlertDialog.Builder(contex);
                builder.setView(view);

                final int[] day = new int[1];
                final int[] month = new int[1];
                final int[] year = new int[1];
                final String[] selected = new String[1];
                List<String> days = new ArrayList<String>();
                days.add("Today");
                days.add("Tomorrow");

                final Spinner date = view.findViewById(R.id.spDaySchedule);

                ArrayAdapter<String> adapter = new ArrayAdapter<String>(contex, android.R.layout.simple_spinner_item, days);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                date.setAdapter(adapter);

                date.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        selected[0] =date.getSelectedItem().toString();
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {

                    }
                });

                builder.setCancelable(false)
                        .setPositiveButton("Add", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // DBHelper dbHelper=new DBHelper(contex);

                                // Get the calander
                                Calendar c = Calendar.getInstance();

                                // From calander get the year, month, day, hour, minute
                                year[0] = c.get(Calendar.YEAR);
                                month[0] = c.get(Calendar.MONTH);

                                if(selected[0].equals("Today")) {
                                    day[0] = c.get(Calendar.DAY_OF_MONTH);
                                }
                                if (selected[0].equals("Tomorrow")){
                                    day[0] = c.get(Calendar.DAY_OF_MONTH)+1;
                                }

                                c.set(year[0], month[0], day[0]);
                                String date = day[0] + "-" + (month[0] + 1) + "-" + year[0];

                                String formattedDate;
                                SimpleDateFormat sdtf = new SimpleDateFormat("dd MMM yyyy");
                                Date now = c.getTime();
                                formattedDate = sdtf.format(now);

                                Log.d("check", "onClick: date " + date + " " + formattedDate);


                                ScheduleProvider schedule=new ScheduleProvider();
                                schedule.id=obj.id;
                                schedule.name=obj.name;
                                schedule.price=obj.price;
                                schedule.type=obj.type;
                                schedule.imageUrl=obj.imageUrl;
                                schedule.date=date;


                                String pushkey= FirebaseDatabase.getInstance().getReference().push().getKey();
                                FirebaseDatabase.getInstance().getReference().child("providers").child(StaticConfig.UID).child("schedule").child(pushkey).setValue(schedule);

                                MenuCustomer customer=new MenuCustomer();
                                customer.id=obj.id;
                                customer.name=obj.name;
                                customer.price=obj.price;
                                customer.type=obj.type;
                                customer.imageUrl=obj.imageUrl;
                                customer.schedule=date;
                                customer.desc=obj.desc;
                                customer.extraItem=obj.extraItem;
                                customer.extraItemPrice=obj.extraItemPrice;
                                customer.rating=obj.rating;
                                customer.ratingCount=obj.ratingCount;

                                providerInfo();

                                customer.providerId=StaticConfig.UID;
                                customer.providerName=StaticConfig.NAME;
                                customer.providerAddress=StaticConfig.ADDRESS;
                                customer.providerAvatar=StaticConfig.AVATAR;
                                customer.location=StaticConfig.LOCATION;

                                FirebaseDatabase.getInstance().getReference().child("schedule").child(pushkey).setValue(customer);


                                //int id=dbHelper.addPurchased(obj.getId(),obj.getName(),obj.getQuantity(),price.getText().toString(),formattedDate,obj.getImage());
                                //Log.d("check", "onClick: "+id);


                                //contex.startActivity(new Intent(contex, Schedule.class));
                                //((Activity) contex).finish();
                                Log.d("check", "onClick: schedule "+day[0]+" "+formattedDate);
                                Toast.makeText(contex, "Menu Added in Schedule", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });
                AlertDialog alertDialog = builder.create();
                alertDialog.show();
            }
        });
    }

    public void providerInfo(){
        FirebaseDatabase.getInstance().getReference("providers/"+ StaticConfig.UID).child("about")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.getValue() != null) {

                            HashMap mapUserInfo = (HashMap) dataSnapshot.getValue();
                            StaticConfig.NAME = (String) mapUserInfo.get("name");
                            StaticConfig.ADDRESS = (String) mapUserInfo.get("address");
                            StaticConfig.AVATAR=(String) mapUserInfo.get("avatar");
                            StaticConfig.LOCATION=(String) mapUserInfo.get("location");

                            Log.d("check", "ProviderInfo: "+StaticConfig.NAME+" "+StaticConfig.ADDRESS);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
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
            desc = itemView.findViewById(R.id.mFoodDesc);
            ratingCount = itemView.findViewById(R.id.mRatingCount);
            ratingBar = itemView.findViewById(R.id.ratingBar);
            image = (ImageView) itemView.findViewById(R.id.ivItemImage);
            addToSchedule = (ImageView) itemView.findViewById(R.id.addToSchedule);
            card = itemView.findViewById(R.id.cardMenu);
        }

    }


}
