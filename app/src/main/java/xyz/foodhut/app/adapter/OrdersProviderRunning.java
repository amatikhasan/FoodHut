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
import java.util.Locale;

import xyz.foodhut.app.R;
import xyz.foodhut.app.data.StaticConfig;
import xyz.foodhut.app.model.Notification;
import xyz.foodhut.app.model.OrderDetails;
import xyz.foodhut.app.model.OrderDetailsProvider;
import xyz.foodhut.app.ui.customer.OrderStatusCustomer;
import xyz.foodhut.app.ui.provider.OrderStatusProvider;

import static android.content.ContentValues.TAG;
import static xyz.foodhut.app.ui.PhoneAuthActivity.userID;

public class OrdersProviderRunning extends RecyclerView.Adapter<OrdersProviderRunning.ViewHolder> {
    private Context contex;
    private ArrayList<OrderDetails> orderData;

    public OrdersProviderRunning(Context contex, ArrayList<OrderDetails> orderData) {
        this.contex = contex;
        this.orderData = orderData;

        Log.d("check", "OrdersCustomer: size " + orderData.size());
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

        final OrderDetails obj = orderData.get(position);
        //   Log.d("check", "onBindViewHolder: " + obj.name + " " + obj.menuId + " " + obj2.date);

        // Get the calander
        Calendar c = Calendar.getInstance();
        DateFormat dateFormat = SimpleDateFormat.getDateTimeInstance();
        final String date = dateFormat.format(c.getTime());

        holder.foodName.setText(obj.itemName);
        String qty = obj.quantity + " pkt.";
        holder.quantity.setText(qty);

        if (!obj.extraItem.isEmpty()) {
            String extra = "Extra " + obj.extraItem;
            holder.extraItem.setText(extra);
            String pcs = obj.extraQuantity + " pcs.";
            holder.extraQty.setText(pcs);
            holder.extraItem.setVisibility(View.VISIBLE);
            holder.extraQty.setVisibility(View.VISIBLE);
        }

        else  {
            holder.extraItem.setVisibility(View.GONE);
            holder.extraQty.setVisibility(View.GONE);
        }

        String amount = "৳ " + obj.amount;
        holder.amount.setText(amount);
        String time = "Time: " + obj.date + " " + obj.time;
        holder.dateTime.setText(time);

        String orderNo;

        if (obj.menuCount > 0)
            orderNo = "O-" + obj.orderId + " (" + obj.menuCount + ")";
        else
            orderNo = "O-" + obj.orderId;

        holder.orderNo.setText(orderNo);
        //  holder.address.setText(obj2.cAddress);

        if (!obj.status.equals("Pending")) {
            if (!obj.note.isEmpty()) {
                holder.viewNote.setVisibility(View.VISIBLE);
                holder.viewNote.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        viewNote(obj.note);
                    }
                });
            }
        }
        if (obj.status.equals("Pending")) {
            holder.status.setText("Accept");
            holder.status.setBackground(contex.getResources().getDrawable(R.drawable.round_button_primary));
            holder.status.setEnabled(true);
            holder.action.setVisibility(View.VISIBLE);
            holder.action.setText("Reject");
            holder.action.setBackgroundResource(0);
            holder.action.setTextColor(contex.getResources().getColor(R.color.colorPrimary));
        }

        if (obj.status.equals("Accepted")) {
            holder.status.setText(obj.status);
            holder.status.setBackground(contex.getResources().getDrawable(R.drawable.round_button_blue));
            holder.status.setEnabled(false);

            holder.action.setVisibility(View.VISIBLE);
            holder.action.setText("Ready");
            holder.action.setBackground(contex.getResources().getDrawable(R.drawable.round_button_primary));
            holder.action.setTextColor(contex.getResources().getColor(R.color.white));
        }
        if (obj.status.equals("Ready")) {
            holder.status.setText(obj.status);
            holder.status.setBackground(contex.getResources().getDrawable(R.drawable.round_button_green));
            holder.status.setEnabled(false);
            holder.action.setVisibility(View.INVISIBLE);
        }
        if (obj.status.equals("Cancelled") || obj.status.equals("Rejected")) {
            holder.status.setText(obj.status);
            holder.status.setEnabled(false);
            holder.status.setBackground(contex.getResources().getDrawable(R.drawable.round_button_primary));
            holder.viewNote.setVisibility(View.INVISIBLE);
            holder.action.setVisibility(View.INVISIBLE);
        }
        if (obj.status.equals("Shipped")) {
            holder.status.setEnabled(false);
            holder.status.setText(obj.status);
            holder.status.setBackground(contex.getResources().getDrawable(R.drawable.round_button_orange));
            holder.action.setVisibility(View.INVISIBLE);
        }
        if (obj.status.equals("Delivered")) {
            holder.status.setEnabled(false);
            holder.status.setText(obj.status);
            holder.status.setBackground(contex.getResources().getDrawable(R.drawable.round_button_green));
            holder.action.setVisibility(View.INVISIBLE);
        }


        if (obj.status.equals("Pending")) {
            holder.action.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    final android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(contex);
                    builder.setMessage("Are you sure you want to reject this order?")
                            .setCancelable(false)
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                public void onClick(final DialogInterface dialog, final int id) {
                                    Toast.makeText(contex, "Order Rejected!", Toast.LENGTH_SHORT).show();
                                    holder.status.setText("Rejected");
                                    holder.status.setBackground(contex.getResources().getDrawable(R.drawable.round_button_primary));
                                    holder.status.setTextColor(contex.getResources().getColor(R.color.white));
                                    holder.status.setEnabled(false);
                                    holder.action.setVisibility(View.INVISIBLE);

                                    Calendar c = Calendar.getInstance();
                                    int day = c.get(Calendar.DAY_OF_MONTH);
                                    int hour = c.get(Calendar.HOUR_OF_DAY);
                                    int minute = c.get(Calendar.MINUTE);

                                    String month = new SimpleDateFormat("MMM", Locale.US).format(c.getTime());
                                    String time = day + " " + month + " " + formatTime(hour + ":" + minute);

                                    FirebaseDatabase.getInstance().getReference().child("admin/orders").child(obj.date).child(obj.menuId).child("orders").child(obj.orderId).child("status").setValue("Rejected");
                                    FirebaseDatabase.getInstance().getReference().child("providers/" + obj.pId + "/orders").child(obj.date).child(obj.menuId).child("orders").child(obj.orderId).child("status").setValue("Rejected");
                                    FirebaseDatabase.getInstance().getReference().child("customers/" + obj.cId + "/orders").child(obj.date).child(obj.menuId).child("orders").child(obj.orderId).child("status").setValue("Cancelled");

                                    FirebaseDatabase.getInstance().getReference().child("providers/" + obj.pId + "/orders").child(obj.date).child(obj.menuId).child("orders").child(obj.orderId).child("statusTime").setValue(time);
                                    FirebaseDatabase.getInstance().getReference().child("customers/" + obj.cId + "/orders").child(obj.date).child(obj.menuId).child("orders").child(obj.orderId).child("statusTime").setValue(time);

                                    final Notification notification = new Notification();
                                    notification.orderId = obj.orderId;
                                    notification.title = "Your order O-" + obj.orderId + " is Cancelled";
                                    notification.status = "C";
                                    notification.time = date;
                                    FirebaseDatabase.getInstance().getReference().child("customers/" + obj.cId + "/notifications/new").push().setValue(notification);
                                    FirebaseDatabase.getInstance().getReference().child("customers/" + obj.cId + "/notifications/old").push().setValue(notification);


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
                    Toast.makeText(contex, "Order Accepted! Come Back Again When You're Ready", Toast.LENGTH_SHORT).show();
                    holder.status.setText("Accepted");
                    holder.status.setBackground(contex.getResources().getDrawable(R.drawable.round_button_blue));
                    holder.status.setTextColor(contex.getResources().getColor(R.color.white));
                    holder.status.setEnabled(false);
                    holder.action.setVisibility(View.INVISIBLE);

                    if (!obj.note.isEmpty()) {
                        holder.viewNote.setVisibility(View.VISIBLE);
                        holder.viewNote.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                viewNote(obj.note);
                            }
                        });
                    }

                    Calendar c = Calendar.getInstance();
                    int day = c.get(Calendar.DAY_OF_MONTH);
                    int hour = c.get(Calendar.HOUR_OF_DAY);
                    int minute = c.get(Calendar.MINUTE);

                    String month = new SimpleDateFormat("MMM", Locale.US).format(c.getTime());
                    String time = day + " " + month + " " + formatTime(hour + ":" + minute);

                    FirebaseDatabase.getInstance().getReference().child("admin/orders").child(obj.date).child(obj.menuId).child("orders").child(obj.orderId).child("status").setValue("Accepted");
                    FirebaseDatabase.getInstance().getReference().child("providers/" + obj.pId + "/orders").child(obj.date).child(obj.menuId).child("orders").child(obj.orderId).child("status").setValue("Accepted");
                    FirebaseDatabase.getInstance().getReference().child("customers/" + obj.cId + "/orders").child(obj.date).child(obj.menuId).child("orders").child(obj.orderId).child("status").setValue("Accepted");

                    FirebaseDatabase.getInstance().getReference().child("providers/" + obj.pId + "/orders").child(obj.date).child(obj.menuId).child("orders").child(obj.orderId).child("statusTime").setValue(time);
                    FirebaseDatabase.getInstance().getReference().child("customers/" + obj.cId + "/orders").child(obj.date).child(obj.menuId).child("orders").child(obj.orderId).child("statusTime").setValue(time);


                    final Notification notification = new Notification();
                    notification.orderId = obj.orderId;
                    notification.title = "Your order O-" + obj.orderId + " is Accepted";
                    notification.status = "A";
                    notification.time = date;
                    FirebaseDatabase.getInstance().getReference().child("customers/" + obj.cId + "/notifications/new").push().setValue(notification);
                    FirebaseDatabase.getInstance().getReference().child("customers/" + obj.cId + "/notifications/old").push().setValue(notification);

                }
            });
        }

        if (obj.status.equals("Accepted")) {
            holder.action.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(contex, "Order is Ready for Pickup!", Toast.LENGTH_SHORT).show();
                    holder.action.setVisibility(View.INVISIBLE);
                    holder.status.setText("Ready");
                    holder.status.setBackground(contex.getResources().getDrawable(R.drawable.round_button_green));
                    holder.status.setTextColor(contex.getResources().getColor(R.color.white));


                    FirebaseDatabase.getInstance().getReference().child("providers/" + obj.pId + "/orders").child(obj.date).child(obj.menuId).child("orders").child(obj.orderId).child("status").setValue("Ready");
                    FirebaseDatabase.getInstance().getReference().child("admin/orders").child(obj.date).child(obj.menuId).child("orders").child(obj.orderId).child("status").setValue("Ready");
                }
            });
        }
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
        return orderData.size();
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
            //  address = itemView.findViewById(R.id.mCAddress);
            //  icon = itemView.findViewById(R.id.mIcon);
            card = itemView.findViewById(R.id.cardMenu);
        }
    }
}
