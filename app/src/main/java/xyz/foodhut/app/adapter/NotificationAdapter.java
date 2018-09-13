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

import com.amulyakhare.textdrawable.TextDrawable;
import com.amulyakhare.textdrawable.util.ColorGenerator;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import xyz.foodhut.app.R;
import xyz.foodhut.app.data.StaticConfig;
import xyz.foodhut.app.model.Notification;
import xyz.foodhut.app.model.OrderDetailsProvider;
import xyz.foodhut.app.ui.customer.OrderStatusCustomer;
import xyz.foodhut.app.ui.provider.OrderStatusProvider;

import static android.content.ContentValues.TAG;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.ViewHolder> {
    private Context context;
    private ArrayList<Notification> data;
    private String user;


    public NotificationAdapter(Context context, ArrayList<Notification> data, String user) {
        this.context = context;
        this.data = data;
        this.user=user;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.layout_notification, parent, false);
        return new ViewHolder(view);
    }


    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {

        final Notification obj = data.get(position);

        holder.setTextDrawable(obj.status);

        String title="Your order O-"+obj.orderId+" is "+obj.status;
        holder.title.setText(title);
        holder.time.setText(obj.time);
    }



    @Override
    public int getItemCount() {
        return data.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView title, time;
        ImageView drawable;
        CardView card;
        private TextDrawable textDrawable;
        private ColorGenerator mColorGenerator = ColorGenerator.DEFAULT;


        public ViewHolder(View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.notiTitle);
            time = itemView.findViewById(R.id.notiTime);

            drawable = (ImageView) itemView.findViewById(R.id.notiDrawable);
            //card = itemView.findViewById(R.id.cardMenu);
        }


        // Set reminder title view
        public void setTextDrawable(String title) {
            String letter = "N";

            if(title != null && !title.isEmpty()) {
                letter = title.substring(0, 1);
            }

            int color = mColorGenerator.getRandomColor();

            // Create a circular icon consisting of  a random background colour and first letter of title
            textDrawable = TextDrawable.builder()
                    .buildRound(letter, color);
            drawable.setImageDrawable(textDrawable);
        }

    }


}
