package xyz.foodhut.app.ui;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.FirebaseDatabase;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import xyz.foodhut.app.R;
import xyz.foodhut.app.data.StaticConfig;
import xyz.foodhut.app.ui.provider.HomeProvider;

public class FeedBack extends AppCompatActivity {

    EditText etFeedback;
    TextView submit;
    String mFeedBack = "", user;
    Bundle extras;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feed_back);

        etFeedback = findViewById(R.id.etFeedBack);
        submit = findViewById(R.id.fbSubmit);

        extras = getIntent().getExtras();
        if (extras != null) {
            user = extras.getString("user");
        }

        // Get the calander
        Calendar c = Calendar.getInstance();

        DateFormat dateFormat = SimpleDateFormat.getDateTimeInstance();
        final String time = dateFormat.format(c.getTime());

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mFeedBack = etFeedback.getText().toString().trim();

                if (!mFeedBack.equals("")) {
                    String key = FirebaseDatabase.getInstance().getReference().push().getKey();
                    xyz.foodhut.app.model.FeedBack feedBack = new xyz.foodhut.app.model.FeedBack();
                    feedBack.id = StaticConfig.UID;
                    feedBack.name = StaticConfig.NAME;
                    feedBack.message = mFeedBack;
                    feedBack.time = time;
                    FirebaseDatabase.getInstance().getReference().child("admin/feedBack").child(key).setValue(feedBack);

                    Toast.makeText(FeedBack.this, "Your message is sent to us!", Toast.LENGTH_SHORT).show();
                    etFeedback.setEnabled(false);
                    submit.setEnabled(false);
                } else {
                    Toast.makeText(FeedBack.this, "Please write your message", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void goBack(View view) {
        if (user.equals("provider"))
            startActivity(new Intent(this, HomeProvider.class));
        else
            startActivity(new Intent(this, HomeProvider.class));

    }
}
