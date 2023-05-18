package pl.gawryszewski.am_projekt;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class UpdatePostActivity extends AppCompatActivity {

    private DataBaseHandler dataBaseHandler;
    private TextView tv_id, tv_url;
    private EditText et_lat, et_long, et_address;
    private Button btnUpdate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_post);
        setTitle("Post update");
        dataBaseHandler = new DataBaseHandler(this);
        tv_id = findViewById(R.id.val_postId2);
        tv_url = findViewById(R.id.val_url2);
        et_lat = findViewById(R.id.et_postLat);
        et_long = findViewById(R.id.et_postLong);
        et_address = findViewById(R.id.et_postAddress);
        String id = getIntent().getStringExtra("postId");
        if(id!=null)
        {
            PostModel post = dataBaseHandler.getOne(id);
            if(post!=null)
            {
                tv_id.setText(post.getId());
                tv_url.setText(post.getUrl());
                et_lat.setText(String.valueOf(post.getLatitude()));
                et_long.setText(String.valueOf(post.getLongitude()));
                et_address.setText(post.getAddress());
            }
        }
        btnUpdate = findViewById(R.id.updateButton2);
        btnUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bundle values = new Bundle();
                values.putString("latitude", et_lat.getText().toString());
                values.putString("longitude", et_long.getText().toString());
                values.putString("address", et_address.getText().toString());
                if(dataBaseHandler.updatePost(tv_id.getText().toString(), values))
                {
                    Toast.makeText(UpdatePostActivity.this, "Post updated successfully", Toast.LENGTH_SHORT).show();
                    Intent resultIntent = new Intent();
                    setResult(RESULT_OK, resultIntent);
                    finish();
                }
                else
                {
                    Toast.makeText(UpdatePostActivity.this, "Updating post failed", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }
}