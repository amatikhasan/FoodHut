package xyz.foodhut.app.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import xyz.foodhut.app.R;
import xyz.foodhut.app.data.StaticConfig;
import xyz.foodhut.app.model.Notification;
import xyz.foodhut.app.model.OrderDetails;
import xyz.foodhut.app.model.OrderDetailsProvider;
import xyz.foodhut.app.ui.customer.OrderStatusCustomer;
import xyz.foodhut.app.ui.provider.OrderStatusProvider;
import static android.content.ContentValues.TAG;
import static xyz.foodhut.app.ui.PhoneAuthActivity.userID;

public class OrdersProvider extends RecyclerView.Adapter<OrdersProvider.ViewHolder> {
    private Context contex;
    private ArrayList<OrderDetailsProvider> data;
    private ArrayList<OrderDetails> orderData;

    public OrdersProvider(Context contex, ArrayList<OrderDetailsProvider> data, ArrayList<OrderDetails> orderData) {
        this.contex = contex;
        this.data = data;
        this.orderData = orderData;

        Log.d("check", "OrdersCustomer: size " + data.size() + " " + orderData.size());
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(contex);
        View view = inflater.inflate(R.layout.orders_provider_layout, parent, false);
        return new ViewHolder(view);
    }


    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {

        final OrderDetailsProvider obj = data.get(position);
        final OrderDetails obj2 = orderData.get(position);
        Log.d("check", "onBindViewHolder: " + obj.name + " " + obj.menuId + " " + obj2.date);

        // Get the calander
        Calendar c = Calendar.getInstance();
        DateFormat dateFormat = SimpleDateFormat.getDateTimeInstance();
        final String date = dateFormat.format(c.getTime());

        holder.foodName.setText(obj.name);
        String qty = obj2.quantity + " pkt.";
        holder.quantity.setText(qty);
        if (!obj2.extraItem.equals("")) {
            String extra = "Extra " + obj2.extraItem;
            holder.extraItem.setText(extra);
            String pcs = obj2.extraQuantity + " pcs.";
            holder.extraQty.setText(pcs);
        }
        else {
            holder.extraItem.setVisibility(View.GONE);
            holder.extraQty.setVisibility(View.GONE);
        }
        String amount = "৳ " + obj2.amount;
        holder.amount.setText(amount);
        String time = "Time: " + obj2.date + " " + obj2.time;
        holder.dateTime.setText(time);
        String orderNo = "O-" + obj2.orderId;
        holder.orderNo.setText(orderNo);
        holder.address.setText(obj2.cAddress);

        if (!obj2.status.equals("Pending")) {
            if (!obj2.note.isEmpty()) {
                holder.viewNote.setVisibility(View.VISIBLE);
                holder.viewNote.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        viewNote(obj2.note);
                    }
                });
            }
            }


        if (obj2.status.equals("Pending")) {
            holder.status.setText("Accept");
            holder.status.setGravity(Gravity.CENTER);
            holder.status.setTextColor(contex.getResources().getColor(R.color.white));
            holder.status.setBackgroundColor(contex.getResources().getColor(R.color.colorPrimary));
            holder.action.setVisibility(View.VISIBLE);
            holder.icon.setVisibility(View.GONE);
        } else {
            holder.status.setText(obj2.status);
            if (obj2.status.equals("Accepted")) {
                holder.action.setVisibility(View.VISIBLE);
                holder.action.setText("Ready");
                holder.action.setBackgroundColor(contex.getResources().getColor(R.color.colorPrimary));
                holder.action.setTextColor(contex.getResources().getColor(R.color.white));
            }
            if (obj2.status.equals("Ready")) {
                holder.status.setTextColor(contex.getResources().getColor(R.color.green));
                holder.icon.setImageDrawable(contex.getResources().getDrawable(R.drawable.icon_accepted));
                holder.action.setVisibility(View.INVISIBLE);
            }
            if (obj2.status.equals("Cancelled")||obj2.status.equals("Rejected")) {
                holder.status.setTextColor(contex.getResources().getColor(R.color.colorPrimary));
                holder.icon.setImageDrawable(contex.getResources().getDrawable(R.drawable.icon_cancelled));
                holder.viewNote.setVisibility(View.INVISIBLE);
                holder.action.setVisibility(View.INVISIBLE);
            }
            if (obj2.status.equals("Shipped")) {
                holder.status.setTextColor(contex.getResources().getColor(R.color.orange));
                holder.icon.setImageDrawable(contex.getResources().getDrawable(R.drawable.icon_shipped));
                holder.action.setVisibility(View.INVISIBLE);
                holder.status.setPadding(8,8,0,0);
            }
            if (obj2.status.equals("Delivered")) {
                holder.status.setTextColor(contex.getResources().getColor(R.color.greenDark));
                holder.icon.setImageDrawable(contex.getResources().getDrawable(R.drawable.icon_delivered));
                holder.action.setVisibility(View.INVISIBLE);
            }
        }

        if (obj2.status.equals("Pending")){
            holder.action.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    final android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(contex);
                    builder.setMessage("Are you sure you want to reject this order?")
                            .setCancelable(false)
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                public void onClick(final DialogInterface dialog, final int id) {
                                    Toast.makeText(contex, "Order Rejected!", Toast.LENGTH_SHORT).show();
                                    holder.action.setVisibility(View.INVISIBLE);
                                    holder.status.setText("Rejected");
                                    holder.status.setGravity(Gravity.START|Gravity.CENTER_VERTICAL);
                                    holder.status.setTextColor(contex.getResources().getColor(R.color.colorPrimary));
                                    holder.icon.setVisibility(View.VISIBLE);
                                    holder.icon.setImageDrawable(contex.getResources().getDrawable(R.drawable.icon_cancelled));

                                    holder.status.setBackgroundResource(0);
                                    holder.status.setEnabled(false);
                                    holder.action.setVisibility(View.INVISIBLE);

                                    FirebaseDatabase.getInstance().getReference().child("admin/orders").child(obj2.date).child(obj.menuId).child("orders").child(obj2.orderId).child("status").setValue("Rejected");
                                    FirebaseDatabase.getInstance().getReference().child("providers/" + obj.pId + "/orders").child(obj2.date).child(obj2.orderId).child("orderDetails").child("status").setValue("Rejected");
                                    FirebaseDatabase.getInstance().getReference().child("customers/" + obj2.cId + "/orders").child(obj2.date).child(obj2.orderId).child("orderDetails").child("status").setValue("Cancelled");


                                    final Notification notification = new Notification();
                                    notification.orderId = obj2.orderId;
                                    notification.status = "Your order O-"+obj2.orderId+" is Cancelled";
                                    notification.time = date;
                                    FirebaseDatabase.getInstance().getReference().child("customers/" + obj2.cId + "/notifications").push().setValue(notification);

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
            holder.status.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(contex, "Order Accepted!", Toast.LENGTH_SHORT).show();
                    holder.status.setTextColor(contex.getResources().getColor(R.color.green));
                    holder.icon.setVisibility(View.VISIBLE);
                    holder.icon.setImageDrawable(contex.getResources().getDrawable(R.drawable.icon_accepted));
                    holder.status.setText("Accepted");
                    holder.status.setGravity(Gravity.START|Gravity.CENTER_VERTICAL);
                    holder.status.setBackgroundResource(0);
                    holder.status.setEnabled(false);
                    holder.action.setVisibility(View.INVISIBLE);

                    if (!obj2.note.isEmpty()) {
                        holder.viewNote.setVisibility(View.VISIBLE);
                        holder.viewNote.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                viewNote(obj2.note);
                            }
                        });
                    }
                    FirebaseDatabase.getInstance().getReference().child("admin/orders").child(obj2.date).child(obj.menuId).child("orders").child(obj2.orderId).child("status").setValue("Accepted");
                    FirebaseDatabase.getInstance().getReference().child("providers/" + obj.pId + "/orders").child(obj2.date).child(obj2.orderId).child("orderDetails").child("status").setValue("Accepted");
                    FirebaseDatabase.getInstance().getReference().child("customers/" + obj2.cId + "/orders").child(obj2.date).child(obj2.orderId).child("orderDetails").child("status").setValue("Processing");

                    final Notification notification = new Notification();
                    notification.orderId = obj2.orderId;
                    notification.status = "Your order O-"+obj2.orderId+" is Processing";
                    notification.time = date;
                    FirebaseDatabase.getInstance().getReference().child("customers/" + obj2.cId + "/notifications").push().setValue(notification);
                }
            });
        }

        if (obj2.status.equals("Accepted")){
            holder.action.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(contex, "Order is Ready for Pickup!", Toast.LENGTH_SHORT).show();
                    holder.action.setVisibility(View.INVISIBLE);
                    holder.status.setText("Ready");

                    FirebaseDatabase.getInstance().getReference().child("providers/" + obj.pId + "/orders").child(obj2.date).child(obj2.orderId).child("orderDetails").child("status").setValue("Ready");
                    FirebaseDatabase.getInstance().getReference().child("admin/orders").child(obj2.date).child(obj.menuId).child("orders").child(obj2.orderId).child("status").setValue("Ready");
                }
            });
        }
    }

    protected void viewNote(String note) {

        final AlertDialog.Builder builder = new AlertDialog.Builder(contex);
        builder.setMessage(note)
                .setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        dialog.cancel();
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
        Log.d("check method", "from alert");
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView foodName, price, dateTime, orderNo, address, status, action, viewNote, amount, quantity, extraItem, extraQty;
        CardView card;
        ImageView icon;

        public ViewHolder(View itemView) {
            super(itemView);
            foodName = itemView.findViewById(R.id.mFoodName);
            //  price = itemView.findViewById(R.id.mFoodPrice);
            //desc = itemView.findViewById(R.id.mFoodDesc);

            dateTime = itemView.findViewById(R.id.mSchedule);
            orderNo = itemView.findViewById(R.id.mOrderNo);
            status = itemView.findViewById(R.id.mStatus);
            action = itemView.findViewById(R.id.mAction);
            extraQty = itemView.findViewById(R.id.mExtraQty);
            extraItem = itemView.findViewById(R.id.mExtraItem);
            viewNote = itemView.findViewById(R.id.mNote);
            //  payment = itemView.findViewById(R.id.oscPayment);
            amount = itemView.findViewById(R.id.mAmount);
            quantity = itemView.findViewById(R.id.mQty);
            address = itemView.findViewById(R.id.mCAddress);
            icon = itemView.findViewById(R.id.mIcon);
            card = itemView.findViewById(R.id.cardMenu);
        }

    }


}
