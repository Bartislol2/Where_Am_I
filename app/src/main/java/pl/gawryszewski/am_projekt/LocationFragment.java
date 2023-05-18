package pl.gawryszewski.am_projekt;

import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


import java.util.List;

public class LocationFragment extends Fragment {
    TextView val_lat, val_lon, val_alt, val_acc, val_spd, val_address;
    public LocationFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_location, container, false);
        val_lat = view.findViewById(R.id.val_latitude);
        val_lon = view.findViewById(R.id.val_longitude);
        val_alt = view.findViewById(R.id.val_altitude);
        val_acc = view.findViewById(R.id.val_accuracy);
        val_spd = view.findViewById(R.id.val_speed);
        val_address = view.findViewById(R.id.val_address);
        return view;
    }

    public void updateInfo(Location location)
    {
        if(getView()!=null) {
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
            Geocoder geocoder = new Geocoder(getActivity());
            try{
                List<Address> addressList = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                val_address.setText(addressList.get(0).getAddressLine(0));
            } catch (Exception e){
                val_address.setText("Unavailable");
            }

        }
    }
}