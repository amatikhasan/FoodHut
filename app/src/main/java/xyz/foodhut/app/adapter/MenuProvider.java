package xyz.foodhut.app.adapter;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.provider.Settings;
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

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import xyz.foodhut.app.R;
import xyz.foodhut.app.data.StaticConfig;
import xyz.foodhut.app.model.MenuCustomer;
import xyz.foodhut.app.model.ScheduleProvider;
import xyz.foodhut.app.ui.provider.AddMenu;
import xyz.foodhut.app.ui.provider.Menus;

import static android.content.ContentValues.TAG;
import static xyz.foodhut.app.ui.PhoneAuthActivity.userID;

public class MenuProvider extends RecyclerView.Adapter<MenuProvider.ViewHolder> {
    private Context contex;
    private ArrayList<xyz.foodhut.app.model.MenuProvider> data;

    public MenuProvider(Context contex, ArrayList<xyz.foodhut.app.model.MenuProvider> data) {
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

        final xyz.foodhut.app.model.MenuProvider obj = data.get(position);
        Log.d("check", "onBindViewHolder: " + obj.name + " " + obj.id + " " + obj.imageUrl);

        String price ="৳ "+ obj.price;
        holder.foodName.setText(obj.name);
        holder.price.setText(price);
      //  holder.type.setText(obj.type);
        holder.desc.setText(obj.desc);
        holder.ratingCount.setText(String.valueOf(obj.ratingCount));
        holder.ratingBar.setRating(Float.parseFloat(obj.rating));
        Picasso.get().load(obj.imageUrl).placeholder(R.drawable.image).into(holder.image);
        //Glide.with(contex).load(obj.imageUrl).into(holder.image);


        holder.edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(contex, AddMenu.class);
                intent.putExtra("name", obj.name);
                intent.putExtra("price", obj.price);
                intent.putExtra("id", obj.id);
                intent.putExtra("desc", obj.desc);
                intent.putExtra("type", obj.type);
                intent.putExtra("pkgSize", obj.pkgSize);
                intent.putExtra("extraItem", obj.extraItem);
                intent.putExtra("extraItemPrice", obj.extraItemPrice);
                intent.putExtra("url", obj.imageUrl);

                Log.d(TAG, "onClick: name: " + obj.name + " id: " + obj.id + " url " + obj.imageUrl);

                contex.startActivity(intent);

            }
        });

        holder.delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                final android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(contex);
                builder.setMessage("Are you sure you want to delete this menu?")
                        .setCancelable(false)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(final DialogInterface dialog, final int id) {
                                deleteItem(obj);

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
                days.add("Day After Tomorrow");


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
                                if (selected[0].equals("Day After Tomorrow")){
                                    day[0] = c.get(Calendar.DAY_OF_MONTH)+2;
                                }

                                c.set(year[0], month[0], day[0]);
                                String date = day[0] + "-" + (month[0] + 1) + "-" + year[0];

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

                                Log.d("check", "onClick: date " + date + " " + formattedDate);


                                ScheduleProvider schedule=new ScheduleProvider();
                                schedule.id=obj.id;
                                schedule.name=obj.name;
                                schedule.price=obj.price;
                                schedule.type=obj.type;
                                schedule.imageUrl=obj.imageUrl;
                                schedule.date=formattedDate;
                                schedule.desc=obj.desc;

                                String pushkey= FirebaseDatabase.getInstance().getReference().push().getKey();
                                schedule.scheduleId=pushkey;

                                FirebaseDatabase.getInstance().getReference().child("providers").child(StaticConfig.UID).child("schedule").child(formattedDate).child(pushkey).setValue(schedule);

                                MenuCustomer customer=new MenuCustomer();
                                customer.id=obj.id;
                                customer.name=obj.name;
                                customer.price=obj.price;
                                customer.type=obj.type;
                                customer.imageUrl=obj.imageUrl;
                                customer.schedule=formattedDate;
                                customer.scheduleId =pushkey;
                                customer.desc=obj.desc;
                                customer.extraItem=obj.extraItem;
                                customer.extraItemPrice=obj.extraItemPrice;
                                customer.rating= Float.parseFloat(obj.rating);
                                customer.ratingCount= Integer.parseInt(obj.ratingCount);

                                providerInfo();

                                customer.providerId=StaticConfig.UID;
                                customer.providerName=StaticConfig.NAME;
                                customer.providerAddress=StaticConfig.ADDRESS;
                                customer.providerAvatar=StaticConfig.AVATAR;
                                customer.location=StaticConfig.LOCATION;
                                customer.providerPhone=StaticConfig.PHONE;

                                FirebaseDatabase.getInstance().getReference().child("schedule").child(pushkey).setValue(customer);
                                //FirebaseDatabase.getInstance().getReference().child("locations").child(StaticConfig.LOCATION).setValue(StaticConfig.LOCATION);


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

    public void deleteItem(xyz.foodhut.app.model.MenuProvider obj) {

        //displaying a progress dialog while upload is going on
        final ProgressDialog progressDialog = new ProgressDialog(contex);
        progressDialog.setMessage("Please Wait...");

        FirebaseDatabase.getInstance().getReference().child("providers/" + StaticConfig.UID).child("menu").child(obj.id).removeValue();

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
                            StaticConfig.PHONE=(String) mapUserInfo.get("phone");

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

        TextView foodName, price, edit, desc, ratingCount,addToSchedule,delete;
        RatingBar ratingBar;
        ImageView image;
        CardView card;


        public ViewHolder(View itemView) {
            super(itemView);
            foodName = itemView.findViewById(R.id.mFoodName);
            price = itemView.findViewById(R.id.mFoodPrice);
            edit = itemView.findViewById(R.id.mEdit);
            delete = itemView.findViewById(R.id.mDelete);
            desc = itemView.findViewById(R.id.mFoodDesc);
            ratingCount = itemView.findViewById(R.id.mRatingCount);
            ratingBar = itemView.findViewById(R.id.ratingBar);
            image = (ImageView) itemView.findViewById(R.id.ivItemImage);
            addToSchedule =  itemView.findViewById(R.id.addToSchedule);
            card = itemView.findViewById(R.id.cardMenu);
        }

    }


}
