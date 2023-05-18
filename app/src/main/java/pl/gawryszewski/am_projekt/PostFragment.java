package pl.gawryszewski.am_projekt;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

public class PostFragment extends Fragment {
    private ImageButton newPostButton;
    private ImageButton myPostsButton;


    public PostFragment() {
        // Required empty public constructor
    }

    public static PostFragment newInstance(String param1, String param2) {
        return new PostFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_post, container, false);
        newPostButton = view.findViewById(R.id.newPostButton);

        newPostButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                launchNewPost();
            }
        });
        myPostsButton = view.findViewById(R.id.myPostsButton);
        myPostsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                launchMyPosts();
            }
        });
        return view;
    }
    public void launchNewPost()
    {
        Intent intent = new Intent(getActivity(), NewPostActivity.class);
        startActivity(intent);
    }

    public void launchMyPosts()
    {
        Intent intent = new Intent(getActivity(), MyPostsActivity.class);
        startActivity(intent);
    }

}