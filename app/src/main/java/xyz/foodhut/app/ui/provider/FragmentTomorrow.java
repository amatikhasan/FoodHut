package xyz.foodhut.app.ui.provider;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import xyz.foodhut.app.R;
import xyz.foodhut.app.adapter.ScheduleProvider;
import xyz.foodhut.app.data.StaticConfig;


public class FragmentTomorrow extends Fragment {
    RecyclerView recyclerView;
    ArrayList<xyz.foodhut.app.model.ScheduleProvider> arrayList = new ArrayList<>();
    String userID=null;
    ScheduleProvider adapter;

    private DatabaseReference databaseReference;
    private FirebaseAuth firebaseAuth;
    private Context context;

    private ProgressDialog dialog;

    public FragmentTomorrow() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        //userDB = FirebaseDatabase.getInstance().getReference().child("user").child(StaticConfig.UID);
        // userDB.addListenerForSingleValueEvent(userListener);
        firebaseAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference();


        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_tomorrow, container, false);
        context = view.getContext();

        dialog=new ProgressDialog(context);
        dialog.setMessage("Loading Menu...");

        arrayList=new ArrayList<>();
        recyclerView=view.findViewById(R.id.rvTomorrow);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter= new ScheduleProvider(getContext(),arrayList,2);
        recyclerView.setAdapter(adapter);

        getSchedule();

        return view;
    }

    public void getSchedule(){

        FirebaseUser user = firebaseAuth.getCurrentUser();

        if(user!=null) {
            userID = user.getUid();
        }



        if(StaticConfig.UID!=null) {
            dialog.show();
            FirebaseDatabase.getInstance().getReference("providers/" + StaticConfig.UID).child("schedule")
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            dialog.dismiss();
                            //fetch files from firebase database and push in arraylist
                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                xyz.foodhut.app.model.ScheduleProvider scheduleProvider = snapshot.getValue(xyz.foodhut.app.model.ScheduleProvider.class);
                                arrayList.add(scheduleProvider);
                                //menuAdapter.notifyDataSetChanged();
                                Log.d("Check list", "onDataChange: " + arrayList.size());
                            }

                            for(int i=0;i<arrayList.size();i++){
                                if(!arrayList.get(i).date.equals(checkDate())){
                                    arrayList.remove(i);
                                }
                            }

                            //  Collections.reverse(arrayList);

                            //bind the data in adapter
                            Log.d("Check list", "out datachange: " + arrayList.size());
                            adapter.notifyDataSetChanged();
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            dialog.dismiss();
                        }
                    });
        }
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

        c.set(year, month, day);
        date = (day+1) + "-" + (month + 1) + "-" + year;

        String formattedDate;
        SimpleDateFormat sdtf = new SimpleDateFormat("dd MMM yyyy");
        Date now = c.getTime();
        formattedDate = sdtf.format(now);

        Log.d("check", "checkDate: "+date+" "+formattedDate);

        return date;
    }

    @Override
    public void onDestroyView (){
        super.onDestroyView();
    }

    @Override
    public void onDestroy (){
        super.onDestroy();
    }

    /*
    public class UserInfoAdapter extends RecyclerView.Adapter<UserInfoAdapter.ViewHolder>{
        private List<Configuration> profileConfig;

        public UserInfoAdapter(List<Configuration> profileConfig){
            this.profileConfig = profileConfig;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.list_info_item_layout, parent, false);
            return new ViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {

        }



        @Override
        public int getItemCount() {
            return profileConfig.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            // each data item is just a string in this case
            public TextView label, value;
            public ImageView icon;
            public ViewHolder(View view) {
                super(view);
                //label = (TextView)view.findViewById(R.id.tv_title);
               // value = (TextView)view.findViewById(R.id.tv_detail);
               // icon = (ImageView)view.findViewById(R.id.img_icon);
            }
        }

    }

    */

}
