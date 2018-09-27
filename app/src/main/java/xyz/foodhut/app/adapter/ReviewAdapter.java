package xyz.foodhut.app.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.amulyakhare.textdrawable.TextDrawable;
import com.amulyakhare.textdrawable.util.ColorGenerator;

import java.util.ArrayList;

import xyz.foodhut.app.R;
import xyz.foodhut.app.model.Notification;
import xyz.foodhut.app.model.Review;

public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.ViewHolder> {
    private Context context;
    private ArrayList<Review> data;
    private String user;


    public ReviewAdapter(Context context, ArrayList<Review> data) {
        this.context = context;
        this.data = data;
        this.user=user;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.layout_my_reviews, parent, false);
        return new ViewHolder(view);
    }


    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {

        final Review obj = data.get(position);

        holder.foodName.setText(obj.menuName);
        holder.customerName.setText(obj.customerName);
        holder.review.setText(obj.review);
        holder.ratingBar.setRating(Float.parseFloat(obj.rating));
    }



    @Override
    public int getItemCount() {
        return data.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView foodName, customerName,review;
        RatingBar ratingBar;
        CardView card;


        public ViewHolder(View itemView) {
            super(itemView);
            foodName = itemView.findViewById(R.id.mFoodName);
            customerName = itemView.findViewById(R.id.mCustomer);
            review = itemView.findViewById(R.id.mReview);

            ratingBar = itemView.findViewById(R.id.mRatingBar);
            //card = itemView.findViewById(R.id.cardMenu);
        }

    }


}
