package pl.gawryszewski.am_projekt;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;

import java.util.List;

public class MyPostsActivity extends AppCompatActivity {
    private EditText etFilter;
    private ListView lvPosts;
    private DataBaseHandler dataBaseHandler;
    private ArrayAdapter<PostModel> postArrayAdapter;

    private final ActivityResultLauncher<Intent> postDisplayActivityResultLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                    new ActivityResultCallback<ActivityResult>() {
                        @Override
                        public void onActivityResult(ActivityResult result) {
                            if (result.getResultCode() == RESULT_OK) {
                                List<PostModel> posts = dataBaseHandler.getAll();
                                postArrayAdapter.clear();
                                postArrayAdapter.addAll(posts);
                                postArrayAdapter.notifyDataSetChanged();
                            }
                        }
                    });
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_posts);
        setTitle("My posts");
        dataBaseHandler = new DataBaseHandler(this);
        etFilter = findViewById(R.id.et_filter);
        lvPosts = findViewById(R.id.lv_postList);
        List<PostModel> posts = dataBaseHandler.getAll();
        postArrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, posts);
        lvPosts.setAdapter(postArrayAdapter);
        lvPosts.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                PostModel clickedPost = (PostModel) adapterView.getItemAtPosition(i);
                Intent intent = new Intent(MyPostsActivity.this, PostDisplayActivity.class);
                intent.putExtra("postId", clickedPost.getId());
                postDisplayActivityResultLauncher.launch(intent);
            }
        });
    }

    public void filterByUserInput(View v)
    {
        String userInput = etFilter.getText().toString();
        List<PostModel> posts = dataBaseHandler.getFilteredByAddress(userInput);
        postArrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, posts);
        lvPosts.setAdapter(postArrayAdapter);
    }
}