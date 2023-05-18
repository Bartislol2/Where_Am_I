package pl.gawryszewski.am_projekt;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentManager;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.OnSuccessListener;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import android.Manifest;
import android.widget.Toast;


public class ProfileActivity extends AppCompatActivity implements SettingsFragment.onSettingsChangeListener {

    public static final int DEFAULT_UPDATE_INTERVAL = 5;
    public static final int FASTEST_UPDATE_INTERVAL = 3;
    public static final int PERMISSIONS_FINE_LOCATION = 10;
    private ImageView profilePic;
    private TextView helloMessage;
    private User user;
    private LocationFragment locationFragment;
    private SettingsFragment settingsFragment;
    private PostFragment postFragment;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        setTitle("Profile");
        locationFragment = new LocationFragment();
        settingsFragment = new SettingsFragment();
        Bundle fragmentState = new Bundle();
        settingsFragment.setArguments(fragmentState);
        settingsFragment.setOnSettingsChangeListener(this);
        postFragment = new PostFragment();
        profilePic = findViewById(R.id.fbProfilePic);
        helloMessage = findViewById(R.id.helloMessage);
        AccessToken accessToken = AccessToken.getCurrentAccessToken();
        GraphRequest graphRequest = GraphRequest.newMeRequest(accessToken, new GraphRequest.GraphJSONObjectCallback() {
            @Override
            public void onCompleted(@Nullable JSONObject jsonObject, @Nullable GraphResponse graphResponse) {
                Log.d("RequestResult", jsonObject.toString());
                try {
                    user = new User(jsonObject.getString("name"), jsonObject.getString("id"),
                            "https://graph.facebook.com/" + jsonObject.getString("id") + "/picture?type=large");
                    Picasso.get().load(user.getProfilePicPath()).into(profilePic);
                    String hello = "Hello, " + user.getName() + ".";
                    helloMessage.setText(hello);
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            }
        });
        Bundle bundle = new Bundle();
        bundle.putString("fields", "name, id, first_name, last_name");
        graphRequest.setParameters(bundle);
        graphRequest.executeAsync();
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(ProfileActivity.this);
        LocationRequest.Builder builder = new LocationRequest.Builder(1000 * DEFAULT_UPDATE_INTERVAL);
        builder.setMaxUpdateDelayMillis(1000 * FASTEST_UPDATE_INTERVAL);
        builder.setPriority(Priority.PRIORITY_BALANCED_POWER_ACCURACY);
        locationRequest = builder.build();
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                super.onLocationResult(locationResult);
                Location location = locationResult.getLastLocation();
                if(location!=null)
                    locationFragment.updateInfo(location);
            }
        };

    }

    public void launchSettings(View v) {

        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.fragmentContainer, settingsFragment, null)
                .setReorderingAllowed(true).addToBackStack("name").commit();
    }

    public void launchPost(View v) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.fragmentContainer, postFragment, null)
                .setReorderingAllowed(true).addToBackStack("name").commit();

    }

    public void launchLocation(View v) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.fragmentContainer, locationFragment, null)
                .setReorderingAllowed(true).addToBackStack("name").commit();
        updateGps();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case PERMISSIONS_FINE_LOCATION:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    updateGps();
                } else {
                    Toast.makeText(this, "This app requires permission to be granted in order to work properly",
                            Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    @Override
    public void onSettingsChange(Bundle settings) {
        Log.d("ProfileActivity", "received settings change");
        if (settings != null) {
            if (settings.containsKey("priority")) {
                Log.d("ProfileActivity", "priority change: " + settings.getInt("priority"));
                int priority = settings.getInt("priority");
                LocationRequest.Builder builder = new LocationRequest.Builder(locationRequest.getIntervalMillis())
                        .setMaxUpdateDelayMillis(locationRequest.getMaxUpdateDelayMillis())
                        .setPriority(priority);
                locationRequest = builder.build();

            } else if (settings.containsKey("updates")) {
                if (settings.getBoolean("updates")) {
                    Log.d("ProfileActivity", "location updates enabled");
                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        return;
                    }
                    fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, null);
                    updateGps();
                }
                else
                {
                    Log.d("ProfileActivity", "location updates disabled");
                    fusedLocationProviderClient.removeLocationUpdates(locationCallback);
                    updateGps();
                }
            }
        }
    }

    public void updateGps()
    {
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
        {
            fusedLocationProviderClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    if(location == null)
                        Toast.makeText(ProfileActivity.this, "Turn on your GPS to access location tracking and try again", Toast.LENGTH_SHORT).show();
                    else
                        locationFragment.updateInfo(location);
                }
            });
        }
        else
        {
            requestPermissions(new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSIONS_FINE_LOCATION);
        }
    }
}