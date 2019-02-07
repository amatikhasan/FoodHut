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
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
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
import xyz.foodhut.app.model.OrderItem;
import xyz.foodhut.app.ui.customer.MenuDetails;

import static android.content.ContentValues.TAG;

public class OrderItemAdapter extends RecyclerView.Adapter<OrderItemAdapter.ViewHolder> {
    private Context contex;
    private ArrayList<xyz.foodhut.app.model.MenuCustomer> data;
    private ArrayList<Integer> itemQtyList;
    private TextView subAmount, finalAmount;
    private int mAvailable;

    public OrderItemAdapter(Context contex, ArrayList<xyz.foodhut.app.model.MenuCustomer> data,
                            ArrayList<Integer> itemQtyList, TextView subAmount, TextView finalAmount) {
        this.contex = contex;
        this.data = data;
        this.itemQtyList = itemQtyList;
        this.subAmount = subAmount;
        this.finalAmount = finalAmount;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(contex);
        View view = inflater.inflate(R.layout.layout_order_item, parent, false);
        return new ViewHolder(view);
    }


    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {
        final xyz.foodhut.app.model.MenuCustomer obj = data.get(position);
        Log.d("check", "onBindViewHolder: " + obj.name);
        Log.d("check", "onBindViewHolder: Extra " + obj.extraItem);

        final int[] itemSubTotal = {0};
        final int[] extraSubTotal = {0};
        final int itemPrice = Integer.parseInt(obj.price);
        final int[] itemQty = {itemQtyList.get(position)};

        int extraPrice = 0;
        final int[] extraQty = {0};
        StaticConfig.EXTRAQTYLIST.add(0);


        itemSubTotal[0] = (Integer.parseInt(obj.price) * itemQty[0]);
        StaticConfig.SUBTOTAL = StaticConfig.SUBTOTAL + itemSubTotal[0];
        StaticConfig.TOTAL = StaticConfig.TOTAL + itemSubTotal[0];

        // String price = "৳ " + itemSubTotal[0];

        holder.foodName.setText(obj.name);
        holder.price.setText(obj.price);
        holder.qty.setText(String.valueOf(itemQty[0]));

        getAvailable(obj.scheduleId);

        if (position == data.size() - 1) {
            subAmount.setText(String.valueOf(StaticConfig.SUBTOTAL));
            finalAmount.setText(String.valueOf(StaticConfig.TOTAL));
        }

        if (!obj.extraItem.equals("")) {
            extraPrice = Integer.parseInt(obj.extraItemPrice);
            String ei = "Extra " + obj.extraItem;
            holder.extraItem.setText(ei);
            holder.extraPrice.setText(obj.extraItemPrice);
        } else {
            holder.rlExtra.setVisibility(View.GONE);
        }

        holder.qtyMinus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (itemQty[0] > 1) {

                    itemSubTotal[0] = itemSubTotal[0] - itemPrice;
                    itemQty[0]--;

                    StaticConfig.ITEMQTYLIST.set(position, StaticConfig.ITEMQTYLIST.get(position)-1);

                 //   holder.price.setText(String.valueOf(itemSubTotal[0]));
                    holder.qty.setText(String.valueOf(itemQty[0]));

                    StaticConfig.SUBTOTAL -= itemPrice;
                    StaticConfig.TOTAL -= itemPrice;

                    subAmount.setText(String.valueOf(StaticConfig.SUBTOTAL));
                    finalAmount.setText(String.valueOf(StaticConfig.TOTAL));
                }
            }
        });


        holder.qtyPlus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (mAvailable > itemQty[0]) {

                    itemSubTotal[0] = itemSubTotal[0] + itemPrice;
                    itemQty[0]++;
                    StaticConfig.ITEMQTYLIST.set(position, StaticConfig.ITEMQTYLIST.get(position) + 1);

                 //   holder.price.setText(String.valueOf(itemSubTotal[0]));
                    holder.qty.setText(String.valueOf(itemQty[0]));

                    StaticConfig.SUBTOTAL+= itemPrice;
                    StaticConfig.TOTAL += itemPrice;

                    subAmount.setText(String.valueOf(StaticConfig.SUBTOTAL));
                    finalAmount.setText(String.valueOf(StaticConfig.TOTAL));
                }
                else
                    Toast.makeText(contex, "Sorry, no more food available for now", Toast.LENGTH_SHORT).show();
            }
        });


        final int finalExtraPrice = extraPrice;
        holder.extraQtyPlus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                extraSubTotal[0] = extraSubTotal[0] + finalExtraPrice;
                extraQty[0]++;

                StaticConfig.EXTRAQTYLIST.set(position, StaticConfig.EXTRAQTYLIST.get(position)+1);


           //     holder.extraPrice.setText(String.valueOf(extraSubTotal[0]));
                holder.extraQty.setText(String.valueOf(extraQty[0]));

                StaticConfig.SUBTOTAL += finalExtraPrice;
                StaticConfig.TOTAL += finalExtraPrice;

                subAmount.setText(String.valueOf(StaticConfig.SUBTOTAL));
                finalAmount.setText(String.valueOf(StaticConfig.TOTAL));
            }
        });

        final int finalExtraPrice1 = extraPrice;
        holder.extraQtyMinus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (extraQty[0] > 0) {

                    extraSubTotal[0] = extraSubTotal[0] - finalExtraPrice1;
                    extraQty[0]--;

                    StaticConfig.EXTRAQTYLIST.set(position, StaticConfig.EXTRAQTYLIST.get(position)-1);

               //     holder.extraPrice.setText(String.valueOf(extraSubTotal[0]));
                    holder.extraQty.setText(String.valueOf(extraQty[0]));

                    StaticConfig.SUBTOTAL -= finalExtraPrice1;
                    StaticConfig.TOTAL -= finalExtraPrice1;

                    subAmount.setText(String.valueOf(StaticConfig.SUBTOTAL));
                    finalAmount.setText(String.valueOf(StaticConfig.TOTAL));
                }
            }
        });
    }

    public void getAvailable(String mScheduleId) {
        FirebaseDatabase.getInstance().getReference().child("schedule").child(mScheduleId).child("foodQty").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.getValue() != null) {

                    mAvailable=Integer.parseInt(snapshot.getValue().toString());
                    Log.d("check", "available: " + mAvailable);

                }
            }

            public void onCancelled(DatabaseError arg0) {
            }
        });
    }

    @Override
    public int getItemCount() {
        // return data.size();
        return data.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView foodName, price, qty, extraQty, extraItem, extraPrice;
        ImageView qtyMinus, qtyPlus, extraQtyMinus, extraQtyPlus;
        RelativeLayout rlExtra;


        public ViewHolder(View itemView) {
            super(itemView);
            foodName = itemView.findViewById(R.id.oiItemName);
            price = itemView.findViewById(R.id.oiPrice);
            qty = itemView.findViewById(R.id.oiQuantity);
            extraItem = itemView.findViewById(R.id.oiExtraItem);
            extraPrice = itemView.findViewById(R.id.oiExtraPrice);
            extraQty = itemView.findViewById(R.id.oiExtraQuantity);

            qtyMinus = itemView.findViewById(R.id.oiQtyMinus);
            qtyPlus = itemView.findViewById(R.id.oiQtyPlus);
            extraQtyMinus = itemView.findViewById(R.id.oiExtraQtyMinus);
            extraQtyPlus = itemView.findViewById(R.id.oiExtraQtyPlus);
            rlExtra = itemView.findViewById(R.id.rlExtra);
        }

    }

}
