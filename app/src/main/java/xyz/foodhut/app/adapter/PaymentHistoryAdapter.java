package xyz.foodhut.app.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;

import java.util.ArrayList;

import xyz.foodhut.app.R;
import xyz.foodhut.app.model.PaymentDetails;
import xyz.foodhut.app.model.Review;

public class PaymentHistoryAdapter extends RecyclerView.Adapter<PaymentHistoryAdapter.ViewHolder> {
    private Context context;
    private ArrayList<PaymentDetails> data;


    public PaymentHistoryAdapter(Context context, ArrayList<PaymentDetails> data) {
        this.context = context;
        this.data = data;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.layout_payment_history, parent, false);
        return new ViewHolder(view);
    }


    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {

        final PaymentDetails obj = data.get(position);

        String amount="৳ "+obj.withdrawRequest;

        holder.amount.setText(amount);
        holder.method.setText(obj.method);
        holder.time.setText(obj.time);
    }



    @Override
    public int getItemCount() {
        return data.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView amount, method,time;
        CardView card;


        public ViewHolder(View itemView) {
            super(itemView);
            amount = itemView.findViewById(R.id.txtAmount);
            method = itemView.findViewById(R.id.txtMethod);
            time = itemView.findViewById(R.id.txtTime);

            //card = itemView.findViewById(R.id.cardMenu);
        }

    }


}
