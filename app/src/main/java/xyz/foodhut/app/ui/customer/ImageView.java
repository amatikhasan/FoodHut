package xyz.foodhut.app.ui.customer;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.github.chrisbanes.photoview.PhotoView;
import com.squareup.picasso.Picasso;

import xyz.foodhut.app.R;



public class ImageView extends AppCompatActivity {

    PhotoView mImageView;
    String imageUrl;
    Bundle extras;
    Button close;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_view);

        extras = getIntent().getExtras();
        if (extras != null) {
            imageUrl = extras.getString("imageUrl");
        }

        close=findViewById(R.id.dummy_button);
        mImageView=findViewById(R.id.fullscreen_image);



        Picasso.get().load(imageUrl).placeholder(R.drawable.loading).into(mImageView);


        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

}
