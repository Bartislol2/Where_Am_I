package pl.gawryszewski.am_projekt;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

public class PostDisplayActivity extends AppCompatActivity {

    private DataBaseHandler dataBaseHandler;
    private TextView tv_id, tv_url, tv_lat, tv_long, tv_address;
    private Button btnGo, btnUpdate, btnDelete;
    private final ActivityResultLauncher<Intent> updatePostActivityResultLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                    new ActivityResultCallback<ActivityResult>() {
                        @Override
                        public void onActivityResult(ActivityResult result) {
                            if (result.getResultCode() == RESULT_OK) {
                                String id = tv_id.getText().toString();
                                PostModel post = dataBaseHandler.getOne(id);
                                if(post!=null)
                                {
                                    updateValues(post);
                                    Intent resultIntent = new Intent();
                                    setResult(RESULT_OK, resultIntent);
                                }
                            }
                        }
                    });
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_display);
        setTitle("Post display");
        tv_id = findViewById(R.id.val_postId);
        tv_url = findViewById(R.id.val_url);
        tv_lat = findViewById(R.id.val_postLat);
        tv_long = findViewById(R.id.val_postLong);
        tv_address = findViewById(R.id.val_postAddress);
        btnGo = findViewById(R.id.seePostButton);
        btnGo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String url = tv_url.getText().toString();
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(url));
                startActivity(intent);
            }
        });
        btnUpdate = findViewById(R.id.updateButton);
        btnUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String id = tv_id.getText().toString();
                Intent intent = new Intent(PostDisplayActivity.this, UpdatePostActivity.class);
                intent.putExtra("postId", id);
                updatePostActivityResultLauncher.launch(intent);
            }
        });
        btnDelete = findViewById(R.id.deleteButton);
        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(dataBaseHandler.deletePost(tv_id.getText().toString())) {
                    Toast.makeText(PostDisplayActivity.this, "Post deleted successfully!",
                            Toast.LENGTH_SHORT).show();
                    Intent resultIntent = new Intent();
                    setResult(RESULT_OK, resultIntent);
                }
                else {
                    Toast.makeText(PostDisplayActivity.this, "Could not delete post!",
                            Toast.LENGTH_SHORT).show();
                }
                finish();
            }
        });
        dataBaseHandler = new DataBaseHandler(this);
        Intent intent = getIntent();
        String id = intent.getStringExtra("postId");
        if(id!=null)
        {
            PostModel post = dataBaseHandler.getOne(id);
            updateValues(post);
        }
    }
    public void updateValues(PostModel post)
    {
        if(post!=null)
        {
            tv_id.setText(post.getId());
            tv_url.setText(post.getUrl());
            tv_lat.setText(String.valueOf(post.getLatitude()));
            tv_long.setText(String.valueOf(post.getLongitude()));
            tv_address.setText(post.getAddress());
        }
    }
}