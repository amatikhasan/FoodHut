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
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
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
import java.util.Locale;

import xyz.foodhut.app.R;

import xyz.foodhut.app.data.StaticConfig;
import xyz.foodhut.app.ui.customer.MenuDetails;

import static android.content.ContentValues.TAG;

public class MenuCustomer extends RecyclerView.Adapter<MenuCustomer.ViewHolder> {
    private Context contex;
    private ArrayList<xyz.foodhut.app.model.MenuCustomer> data;
    private ArrayList<xyz.foodhut.app.model.MenuCustomer> tempList;
    private float menuRating;
    private TextView tvQty, tvPrice,tvCart;
    private RelativeLayout rlCart;
    private int mAvailable;
    private FirebaseAuth firebaseAuth;
    private boolean isFav=false;

    public MenuCustomer(Context contex, ArrayList<xyz.foodhut.app.model.MenuCustomer> data,
                        TextView qty, TextView price, RelativeLayout rlCart,TextView tvCart) {
        this.contex = contex;
        this.data = data;
        //this.tempList=new ArrayList<MenuCustomer>();
        //this.tempList.addAll(data);
        this.tempList = data;
        this.tvQty = qty;
        this.tvPrice = price;
        this.rlCart = rlCart;
        this.tvCart=tvCart;
    }

    public MenuCustomer(Context contex, ArrayList<xyz.foodhut.app.model.MenuCustomer> data,Boolean isFav) {
        this.contex = contex;
        this.data = data;
        //this.tempList=new ArrayList<MenuCustomer>();
        //this.tempList.addAll(data);
        this.tempList = data;
        this.isFav=isFav;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(contex);
        View view = inflater.inflate(R.layout.menu_item_customer, parent, false);
        return new ViewHolder(view);
    }


    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {

        final xyz.foodhut.app.model.MenuCustomer obj = data.get(position);
        Log.d("check", "onBindViewHolder: " + obj.name + " " + obj.id + " " + obj.imageUrl);
        Log.d("check", "onBindViewHolder: Extra " + obj.extraItem);

        final String price = "৳ " + obj.price;
        holder.foodName.setText(obj.name);
        holder.price.setText(price);
        holder.desc.setText(obj.desc);
        String qty = obj.foodQty + " Available";
        holder.available.setText(qty);
        //  holder.date.setText(obj.schedule);
        // holder.providerName.setText(obj.providerName);
        //  holder.providerAddress.setText(obj.providerAddress);
        //   holder.ratingCount.setText(String.valueOf(obj.ratingCount));
        checkRating(obj.providerId, obj.id, position, holder);

        if (isFav){
            holder.llOrder.setVisibility(View.INVISIBLE);
        }

        //holder.ratingBar.setRating((float) obj.rating);
        // Transformation transformation=new RoundedTransformationBuilder().cornerRadius(12).oval(false).build();

       // Picasso.get().load(obj.imageUrl).placeholder(R.drawable.loading).into(holder.image);
        Glide.with(contex).load(obj.imageUrl).placeholder(R.drawable.loading).into(holder.image);

        updateProvider(obj.providerId, holder);

//check date to tag them
//        checkDate(obj.schedule, holder);

        //  if (obj.providerAvatar!=null&&!obj.providerAvatar.equals("default")&&!obj.providerAvatar.isEmpty())
        //  Picasso.get().load(obj.providerAvatar).placeholder(R.drawable.kitchen_icon_colour).into(holder.providerAvatar);

        checkFav(obj.id, holder);

        holder.fav.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                firebaseAuth = FirebaseAuth.getInstance();
                FirebaseUser user = firebaseAuth.getCurrentUser();

                FirebaseDatabase.getInstance().getReference().child("customers/" + user.getUid() + "/favourites").child(obj.id)
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                //fetch files from firebase database and push in tempList
                                String key = FirebaseDatabase.getInstance().getReference().push().getKey();
                                if (dataSnapshot.getValue() == null) {
                                    holder.fav.setImageDrawable(contex.getResources().getDrawable(R.drawable.fav_64_filled_white));
                                    FirebaseDatabase.getInstance().getReference().child("customers/" + StaticConfig.UID + "/favourites").child(obj.id).setValue(obj.id);
                                }
                                if (dataSnapshot.getValue() != null) {
                                    holder.fav.setImageDrawable(contex.getResources().getDrawable(R.drawable.fav_64_empty_white));
                                    FirebaseDatabase.getInstance().getReference().child("customers/" + StaticConfig.UID + "/favourites").child(obj.id).removeValue();
                                }


                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
            }
        });

        holder.llOrder.setVisibility(View.GONE);

        holder.llOrder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (StaticConfig.SCHEDULEFORONEITEM.equals(""))
                    StaticConfig.SCHEDULEFORONEITEM = obj.schedule;

                String mTime;
                Calendar calendar = Calendar.getInstance();
                final int hour = calendar.get(Calendar.HOUR_OF_DAY);
                final int minute = calendar.get(Calendar.MINUTE);
                SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy");
                final String date = sdf.format(calendar.getTime());

                // From calander get the year, month, day, hour, minute

                if (minute < 10) {
                    mTime = formatTime(String.valueOf(hour) + ":" + "0" + String.valueOf(minute));
                    //mTime = formatTime(time);
                    //mTime=time;
                } else {
                    mTime = formatTime(String.valueOf(hour) + ":" + String.valueOf(minute));
                    // mTime = formatTime(time);
                    //mTime=time;
                }

                SimpleDateFormat stf = new SimpleDateFormat("hh:mm a");
                Date date1 = null;
                Date date2 = null;

                try {
                    date2 = stf.parse(obj.lastOrderTime);
                    date1 = stf.parse(mTime);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                Log.d("check", "order: " + date + " " + date1 + " " + date2);

                if (date.equals(obj.schedule)) {
                    if (date1.before(date2)) {
                        if (obj.foodQty > 0) {

                            if (StaticConfig.SCHEDULEFORONEITEM.equals(obj.schedule)) {

                                holder.llOrder.setVisibility(View.GONE);
                                holder.llQty.setVisibility(View.VISIBLE);

                                rlCart.setVisibility(View.VISIBLE);
                                tvCart.setVisibility(View.VISIBLE);

                                StaticConfig.QTY++;
                                StaticConfig.SUBTOTAL += Integer.parseInt(obj.price);
                                StaticConfig.ORDERITEMLIST.add(obj);
                                StaticConfig.ITEMQTYLIST.add(1);

                                if (StaticConfig.INDEXLIST.size() == 0)
                                    StaticConfig.INDEXLIST.put(position, 0);
                                else
                                    StaticConfig.INDEXLIST.put(position, StaticConfig.INDEXLIST.get(position) + 1);

                                String qty = StaticConfig.QTY + " Items";
                                String price = "৳ " + StaticConfig.SUBTOTAL;
                                tvQty.setText(qty);
                                tvPrice.setText(price);

                                getAvailable(obj.scheduleId);

                            } else
                                Toast.makeText(contex, "Ordering multiple item from different dates in a single order is not possible", Toast.LENGTH_SHORT).show();

                        } else {
                            Toast.makeText(contex, "Sorry, This food is not available for now", Toast.LENGTH_SHORT).show();
                            StaticConfig.SCHEDULEFORONEITEM = "";
                        }
                    } else {
                        Toast.makeText(contex, "Sorry, You're late to order this food", Toast.LENGTH_SHORT).show();
                        StaticConfig.SCHEDULEFORONEITEM = "";
                    }
                } else {
                    if (obj.foodQty > 0) {

                        if (StaticConfig.SCHEDULEFORONEITEM.equals(obj.schedule)) {

                            StaticConfig.SCHEDULEFORONEITEM = obj.schedule;

                            holder.llOrder.setVisibility(View.GONE);
                            holder.llQty.setVisibility(View.VISIBLE);

                            rlCart.setVisibility(View.VISIBLE);
                            tvCart.setVisibility(View.VISIBLE);

                            StaticConfig.QTY++;
                            StaticConfig.SUBTOTAL += Integer.parseInt(obj.price);
                            StaticConfig.ORDERITEMLIST.add(obj);
                            StaticConfig.ITEMQTYLIST.add(1);

                            if (StaticConfig.INDEXLIST.size() == 0)
                                StaticConfig.INDEXLIST.put(position, 0);
                            else
                                StaticConfig.INDEXLIST.put(position, StaticConfig.INDEXLIST.get(position) + 1);

                            String qty = StaticConfig.QTY + " Items";
                            String price = "৳ " + StaticConfig.SUBTOTAL;
                            tvQty.setText(qty);
                            tvPrice.setText(price);

                            getAvailable(obj.scheduleId);

                        } else
                            Toast.makeText(contex, "Ordering multiple item from different dates in a single order is not possible", Toast.LENGTH_SHORT).show();

                    } else {
                        Toast.makeText(contex, "Sorry, This food is not available for now", Toast.LENGTH_SHORT).show();
                        StaticConfig.SCHEDULEFORONEITEM = "";
                    }
                }
            }
        });

        holder.qtyPlus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int itemQty = Integer.parseInt(holder.qty.getText().toString());

                if (mAvailable > itemQty) {
                    holder.qty.setText(String.valueOf(itemQty + 1));

                    StaticConfig.QTY++;
                    StaticConfig.SUBTOTAL += Integer.parseInt(obj.price);
                    int index = StaticConfig.INDEXLIST.get(position);
                    StaticConfig.ITEMQTYLIST.set(index, StaticConfig.ITEMQTYLIST.get(index) + 1);

                    String qty = StaticConfig.QTY + " Items";
                    String price = "৳ " + StaticConfig.SUBTOTAL;
                    tvQty.setText(qty);
                    tvPrice.setText(price);
                } else
                    Toast.makeText(contex, "Sorry, no more food available for now", Toast.LENGTH_SHORT).show();


            }
        });

        holder.qtyMinus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (StaticConfig.QTY > 0) {
                    int itemQty = Integer.parseInt(holder.qty.getText().toString());
                    if (itemQty > 0) {
                        int index = StaticConfig.INDEXLIST.get(position);
                        if (StaticConfig.ITEMQTYLIST.get(index)>0) {
                            holder.qty.setText(String.valueOf(itemQty - 1));

                            StaticConfig.QTY--;
                            StaticConfig.SUBTOTAL -= Integer.parseInt(obj.price);

                            StaticConfig.ITEMQTYLIST.set(index, StaticConfig.ITEMQTYLIST.get(index) - 1);

                /*    if (StaticConfig.ITEMQTYLIST.get(index)==0){
                        StaticConfig.ITEMQTYLIST.remove(index);
                        StaticConfig.ORDERITEMLIST.remove(index);
                    }
                    */

                            String qty = StaticConfig.QTY + " Items";
                            String price = "৳ " + StaticConfig.SUBTOTAL;
                            tvQty.setText(qty);
                            tvPrice.setText(price);


                            if (StaticConfig.QTY == 0) {
                                holder.llOrder.setVisibility(View.VISIBLE);
                                holder.llQty.setVisibility(View.GONE);
                                rlCart.setVisibility(View.GONE);
                                tvCart.setVisibility(View.GONE);

                                StaticConfig.SUBTOTAL = 0;
                                StaticConfig.ORDERITEMLIST.clear();
                                StaticConfig.ITEMQTYLIST.clear();
                                StaticConfig.INDEXLIST.clear();

                                StaticConfig.SCHEDULEFORONEITEM = "";

                                holder.qty.setText("1");
                            } else if (Integer.parseInt(holder.qty.getText().toString()) == 0) {
                                holder.llOrder.setVisibility(View.VISIBLE);
                                holder.llQty.setVisibility(View.GONE);

                                holder.qty.setText("1");
                                StaticConfig.SCHEDULEFORONEITEM = "";
                            }
                        }


                    }
                }
            }
        });


        holder.card.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(contex, MenuDetails.class);
                intent.putExtra("name", obj.name);
                intent.putExtra("price", obj.price);
                intent.putExtra("sellerPrice", obj.sellerPrice);
                intent.putExtra("menuId", obj.id);
                intent.putExtra("scheduleId", obj.scheduleId);
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
                intent.putExtra("available", obj.foodQty);
                intent.putExtra("pkgSize", obj.pkgSize);
                intent.putExtra("category", obj.category);
                intent.putExtra("lastTime", obj.lastOrderTime);

                Log.d(TAG, "onClick: name: " + obj.name + " id: " + obj.id + " url " + obj.imageUrl);

                contex.startActivity(intent);

            }
        });

    }

    //formate time with AM,PM for button
    public String formatTime(String time) {
        String format, formattedTime, minutes;
        String[] dateParts = time.split(":");
        int hour = Integer.parseInt(dateParts[0]);
        int minute = Integer.parseInt(dateParts[1]);
        if (hour == 0) {
            hour += 12;
            format = "AM";
        } else if (hour == 12) {
            format = "PM";
        } else if (hour > 12) {
            hour -= 12;
            format = "PM";
        } else {
            format = "AM";
        }

        if (minute < 10)
            minutes = "0" + minute;
        else
            minutes = String.valueOf(minute);
        formattedTime = hour + ":" + minutes + " " + format;

        return formattedTime;
    }

    public void getAvailable(String mScheduleId) {
        FirebaseDatabase.getInstance().getReference().child("schedule").child(mScheduleId).child("foodQty").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.getValue() != null) {

                    mAvailable = Integer.parseInt(snapshot.getValue().toString());
                    Log.d("check", "available: " + mAvailable);

                }
            }

            public void onCancelled(DatabaseError arg0) {
            }
        });
    }

    public void updateProvider(String providerId, final ViewHolder holder) {
        FirebaseDatabase.getInstance().getReference().child("providers/" + providerId).child("about")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        //fetch files from firebase database and push in tempList
                        if (dataSnapshot.getValue() != null) {

                            HashMap hashUser = (HashMap) dataSnapshot.getValue();
                            String name = (String) hashUser.get("kitchenName");
                            String address = (String) hashUser.get("address");
                            String avatar = (String) hashUser.get("avatar");

                            String parts[] = address.split(",");
                            String newAddress = parts[1] + ", " + parts[2];

                            holder.providerName.setText(name);
                            holder.providerAddress.setText(newAddress);

                            if (avatar != null && !avatar.equals("default") && !avatar.isEmpty())
                                Picasso.get().load(avatar).placeholder(R.drawable.kitchen_icon_colour).into(holder.providerAvatar);
                            else
                                holder.providerAvatar.setImageDrawable(contex.getResources().getDrawable(R.drawable.kitchen_icon_colour));


                            Log.d("check", "provider: " + name);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                });
    }

    public void checkRating(String providerId, String menuId, int position, final ViewHolder holder) {
        FirebaseDatabase.getInstance().getReference().child("providers/" + providerId).child("menu").child(menuId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        //fetch files from firebase database and push in tempList
                        if (dataSnapshot.getValue() != null) {

                            HashMap hashUser = (HashMap) dataSnapshot.getValue();
                            menuRating = Float.parseFloat((String) hashUser.get("rating"));

                            if (menuRating > 0)
                                holder.ratingBar.setRating(menuRating);
                            else {
                                holder.ratingBar.setVisibility(View.GONE);
                                holder.noRating.setVisibility(View.VISIBLE);
                            }

                            Log.d("check", "rating: " + menuRating);

                        }

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }

    public void checkDate(String date, final ViewHolder holder) {
        // Get the calander
        Calendar c = Calendar.getInstance();
        // From calander get the year, month, day, hour, minute
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);
        c.set(year, month, day);
        Date now = c.getTime();

        String today;
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy");
        today = dateFormat.format(now);

        Date date1 = null;
        Date date2 = null;
        try {
            date1 = dateFormat.parse(today);
            date2 = dateFormat.parse(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        if (date1.equals(date2)) {
            holder.date.setText("Today");
        } else {
            String tomorrow;
            c.add(Calendar.DATE, 1);
            Date now2 = c.getTime();
            tomorrow = dateFormat.format(now2);

            Date date3 = null;
            try {
                date3 = dateFormat.parse(tomorrow);
            } catch (ParseException e) {
                e.printStackTrace();
            }

            if (date3.equals(date2)) {
                holder.date.setText("Tomorrow");
            } else {
                holder.date.setText(date);
            }
        }


        Log.d("check", "onClick: date " + date + " " + today);
    }

    public void checkFav(String menuId, final ViewHolder holder) {
        Log.d(TAG, "checkFav: " + StaticConfig.UID + " " + menuId);
        FirebaseDatabase.getInstance().getReference().child("customers/" + StaticConfig.UID + "/favourites").child(menuId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        //fetch files from firebase database and push in tempList

                        if (dataSnapshot.getValue() != null) {
                            holder.fav.setImageDrawable(contex.getResources().getDrawable(R.drawable.fav_64_filled_white));
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

        TextView foodName, price, qty, available, desc, ratingCount, noRating, date, providerName, providerAddress;
        RatingBar ratingBar;
        ImageView image, providerAvatar, qtyPlus, qtyMinus;
        LinearLayout llOrder, llQty;
        CardView card;
        ImageView fav;


        public ViewHolder(View itemView) {
            super(itemView);
            foodName = itemView.findViewById(R.id.mFoodName);
            price = itemView.findViewById(R.id.mFoodPrice);

            desc = itemView.findViewById(R.id.desc);
            providerName = itemView.findViewById(R.id.mProviderName);
            providerAddress = itemView.findViewById(R.id.mProviderAddress);
            date = itemView.findViewById(R.id.date);
            //  ratingCount = itemView.findViewById(R.id.mRatingCount);
            available = itemView.findViewById(R.id.mAvailable);
            ratingBar = itemView.findViewById(R.id.mRatingBar);
            noRating = itemView.findViewById(R.id.mNoRating);
            image = (ImageView) itemView.findViewById(R.id.mItemImage);
            providerAvatar = (ImageView) itemView.findViewById(R.id.providerAvatar);
            fav = itemView.findViewById(R.id.myFav);
            card = itemView.findViewById(R.id.cardMenu);

            qty = itemView.findViewById(R.id.oiQuantity);
            qtyMinus = (ImageView) itemView.findViewById(R.id.oiQtyMinus);
            qtyPlus = (ImageView) itemView.findViewById(R.id.oiQtyPlus);
            llOrder = itemView.findViewById(R.id.llOrder);
            llQty = itemView.findViewById(R.id.llQty);

        }

    }

    // Filter Class to filter and show the result in list view
    public void filterList(ArrayList<xyz.foodhut.app.model.MenuCustomer> filteredList) {
        data = filteredList;
        notifyDataSetChanged();
    }

    public void filter(String charText) {
        charText = charText.toLowerCase(Locale.getDefault());
        data.clear();
        if (charText.length() == 0) {
            data.addAll(tempList);
        } else {
            for (xyz.foodhut.app.model.MenuCustomer obj : tempList) {
                if (obj.name.toLowerCase(Locale.getDefault()).contains(charText)) {
                    data.add(obj);
                }
            }
        }
        notifyDataSetChanged();
    }
}
