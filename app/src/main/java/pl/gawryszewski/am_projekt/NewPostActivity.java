package pl.gawryszewski.am_projekt;

import static pl.gawryszewski.am_projekt.ProfileActivity.PERMISSIONS_FINE_LOCATION;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.share.Sharer;
import com.facebook.share.model.ShareLinkContent;
import com.facebook.share.widget.ShareButton;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.List;

public class NewPostActivity extends AppCompatActivity {

    private CallbackManager callbackManager;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private ShareButton shareButton;
    private DataBaseHandler dataBaseHandler;
    private TextView val_lat, val_lon, val_alt, val_acc, val_spd, val_address;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_post);
        setTitle("New post");
        dataBaseHandler = new DataBaseHandler(this);
        callbackManager = CallbackManager.Factory.create();
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        shareButton = findViewById(R.id.newPostButton);
        val_lat = findViewById(R.id.val_latitude);
        val_lon = findViewById(R.id.val_longitude);
        val_alt = findViewById(R.id.val_altitude);
        val_acc = findViewById(R.id.val_accuracy);
        val_spd = findViewById(R.id.val_speed);
        val_address = findViewById(R.id.val_address);
        if(ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
        {
            fusedLocationProviderClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                        setValues(location);
                        setPostContent(location);
                    }
            });
        }
        else
        {
            requestPermissions(new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSIONS_FINE_LOCATION);
        }
    }
    private void setValues(Location location) {
        if (location == null)
            Toast.makeText(NewPostActivity.this, "Turn on your GPS to access location tracking and try again", Toast.LENGTH_SHORT).show();
        else {
            val_lat.setText(String.valueOf(location.getLatitude()));
            val_lon.setText(String.valueOf(location.getLongitude()));
            if (location.hasAltitude()) {
                val_alt.setText(String.valueOf(location.getAltitude()));
            } else {
                val_alt.setText("Unavailable");
            }
            val_acc.setText(String.valueOf(location.getAccuracy()));
            if (location.hasSpeed()) {
                val_spd.setText(String.valueOf(location.getSpeed()));
            } else {
                val_spd.setText("Unavailable");
            }
            Geocoder geocoder = new Geocoder(NewPostActivity.this);
            try {
                List<Address> addressList = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                val_address.setText(addressList.get(0).getAddressLine(0));
            } catch (Exception e) {
                val_address.setText("Unavailable");
            }
        }
    }

    private void setPostContent(Location location)
    {
        String gmapsLink = "http://maps.google.com/maps?q=" +
                location.getLatitude() +
                "," +
                location.getLongitude();
        ShareLinkContent content = new ShareLinkContent.Builder()
                .setContentUrl(Uri.parse(gmapsLink)).build();
        shareButton.setShareContent(content);
        shareButton.registerCallback(callbackManager, new FacebookCallback<Sharer.Result>() {
            @Override
            public void onSuccess(Sharer.Result result) {
                Log.d("FbPost", "Post Successful!");
                Toast.makeText(NewPostActivity.this, "Post added!", Toast.LENGTH_SHORT).show();
                String address;
                Geocoder geocoder = new Geocoder(NewPostActivity.this);
                try {
                    List<Address> addressList = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                    address = addressList.get(0).getAddressLine(0);
                } catch (Exception e) {
                    address = "Unavailable";
                }
                dataBaseHandler.addPostToDatabase(location, address);
                finish();
            }

            @Override
            public void onCancel() {
                Log.d("FbPost", "Post Cancelled!");
                Toast.makeText(NewPostActivity.this, "Post cancelled", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(@NonNull FacebookException e) {
                Log.d("FbPost", "Post Error!");
                Toast.makeText(NewPostActivity.this, "Failed to add post", Toast.LENGTH_SHORT).show();
            }
        });
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }
}