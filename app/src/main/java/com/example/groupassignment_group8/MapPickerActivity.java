package com.example.groupassignment_group8;

import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class MapPickerActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private LatLng selectedLatLng;
    private String selectedAddress;

    public static final String EXTRA_LATITUDE = "EXTRA_LATITUDE";
    public static final String EXTRA_LONGITUDE = "EXTRA_LONGITUDE";
    public static final String EXTRA_ADDRESS = "EXTRA_ADDRESS";
    private static final String TAG = "MapPickerActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_picker);

        ExtendedFloatingActionButton confirmButton = findViewById(R.id.button_confirm_location);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        // Initialize the Places SDK
        if (!Places.isInitialized()) {
            // Use the correct string resource name for the API key
            Places.initialize(getApplicationContext(), getString(R.string.google_maps_key));
        }

        // Initialize the AutocompleteSupportFragment.
        AutocompleteSupportFragment autocompleteFragment = (AutocompleteSupportFragment)
                getSupportFragmentManager().findFragmentById(R.id.autocomplete_fragment);

        if (autocompleteFragment != null) {
            // Specify the types of place data to return.
            autocompleteFragment.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG, Place.Field.ADDRESS));

            // Set up a PlaceSelectionListener to handle the response.
            autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
                @Override
                public void onPlaceSelected(@NonNull Place place) {
                    Log.i(TAG, "Place selected: " + place.getName() + ", " + place.getLatLng());
                    if (place.getLatLng() != null) {
                        // A place was selected. Move the map camera to the selected location.
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(place.getLatLng(), 15f));
                    }
                }

                @Override
                public void onError(@NonNull Status status) {
                    // Handle the error.
                    Log.e(TAG, "An error occurred during place selection: " + status);
                    Toast.makeText(MapPickerActivity.this, "Search error: " + status.getStatusMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }

        // Set up the confirm button click listener
        confirmButton.setOnClickListener(v -> {
            Intent resultIntent = new Intent();
            if (selectedLatLng != null) {
                resultIntent.putExtra(EXTRA_LATITUDE, selectedLatLng.latitude);
                resultIntent.putExtra(EXTRA_LONGITUDE, selectedLatLng.longitude);
                resultIntent.putExtra(EXTRA_ADDRESS, selectedAddress);
                setResult(RESULT_OK, resultIntent);
            } else {
                setResult(RESULT_CANCELED);
            }
            finish();
        });
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     */
    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setZoomControlsEnabled(true);

        // Get initial location from intent, or default to a location (e.g., Shah Alam)
        double initialLat = getIntent().getDoubleExtra(EXTRA_LATITUDE, 3.0738); // Default to Shah Alam
        double initialLng = getIntent().getDoubleExtra(EXTRA_LONGITUDE, 101.5183);
        LatLng startPosition = new LatLng(initialLat, initialLng);

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(startPosition, 15f));
        updateLocationDetails(startPosition); // Perform initial address lookup

        // Set a listener for when the camera stops moving
        mMap.setOnCameraIdleListener(() -> {
            LatLng center = mMap.getCameraPosition().target;
            updateLocationDetails(center);
        });
    }

    /**
     * Updates the selected LatLng and performs a reverse geocode to get the address.
     * The address is then displayed in the search bar.
     * @param latLng The coordinates of the map's center.
     */
    private void updateLocationDetails(LatLng latLng) {
        selectedLatLng = latLng;
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        AutocompleteSupportFragment autocompleteFragment = (AutocompleteSupportFragment)
                getSupportFragmentManager().findFragmentById(R.id.autocomplete_fragment);

        try {
            // Get the address from the coordinates
            List<Address> addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                selectedAddress = address.getAddressLine(0); // Get the full address line

                // Set the address text in the Autocomplete fragment's EditText
                if (autocompleteFragment != null && autocompleteFragment.getView() != null) {
                    EditText searchInput = autocompleteFragment.getView().findViewById(com.google.android.libraries.places.R.id.places_autocomplete_search_input);
                    searchInput.setText(selectedAddress);
                }
            } else {
                selectedAddress = "Address not found";
            }
        } catch (IOException e) {
            selectedAddress = "Geocoder service not available";
            Log.e(TAG, "Geocoder service failed", e);
            Toast.makeText(this, "Could not get address. Check connection.", Toast.LENGTH_SHORT).show();
        }
    }
}
