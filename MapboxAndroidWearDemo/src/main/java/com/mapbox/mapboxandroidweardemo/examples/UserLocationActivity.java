package com.mapbox.mapboxandroidweardemo.examples;


import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.wearable.activity.WearableActivity;
import android.view.View;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.mapbox.mapboxandroidweardemo.R;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.location.LocationSource;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.services.android.telemetry.location.LocationEngine;
import com.mapbox.services.android.telemetry.location.LocationEngineListener;
import com.mapbox.services.android.telemetry.permissions.PermissionsListener;
import com.mapbox.services.android.telemetry.permissions.PermissionsManager;

import java.util.List;

public class UserLocationActivity extends WearableActivity implements PermissionsListener {
  private MapView mapView;
  private MapboxMap map;
  private LocationEngine locationEngine;
  private LocationEngineListener locationEngineListener;
  private ToggleButton toggleLocation;
  private PermissionsManager permissionsManager;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // Mapbox access token is configured here. This needs to be called either in your application
    // object or in the same activity which contains the mapview.
    Mapbox.getInstance(this, getString(R.string.access_token));

    // This contains the MapView in XML and needs to be called after the account manager
    setContentView(R.layout.activity_location_user);

    // Get the location engine object for later use.
    locationEngine = LocationSource.getLocationEngine(this);
    locationEngine.activate();

    mapView = (MapView) findViewById(R.id.mapView);
    mapView.onCreate(savedInstanceState);
    mapView.getMapAsync(new OnMapReadyCallback() {
      @Override
      public void onMapReady(MapboxMap mapboxMap) {
        map = mapboxMap;
        toggleLocation.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View v) {
            toggleGps(!map.isMyLocationEnabled());
          }
        });
      }
    });

    toggleLocation = (ToggleButton) findViewById(R.id.toggleLocationButton);
  }

  @Override
  public void onResume() {
    super.onResume();
    mapView.onResume();
  }

  @Override
  public void onPause() {
    super.onPause();
    mapView.onPause();
  }

  @Override
  public void onLowMemory() {
    super.onLowMemory();
    mapView.onLowMemory();
  }

  @Override
  protected void onStart() {
    super.onStart();
    mapView.onStart();
  }

  @Override
  protected void onStop() {
    super.onStop();
    mapView.onStop();
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    mapView.onDestroy();
    if (locationEngineListener != null) {
      locationEngine.removeLocationEngineListener(locationEngineListener);
    }
  }

  @Override
  protected void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
    mapView.onSaveInstanceState(outState);
  }

  @Override
  public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
    permissionsManager.onRequestPermissionsResult(requestCode, permissions, grantResults);
  }

  @Override
  public void onExplanationNeeded(List<String> permissionsToExplain) {
    Toast.makeText(this, "This app needs location permissions in order to show its functionality.",
      Toast.LENGTH_LONG).show();
  }

  @Override
  public void onPermissionResult(boolean granted) {
    if (granted) {
      enableLocation(true);
    } else {
      Toast.makeText(this, "You didn't grant location permissions.",
        Toast.LENGTH_LONG).show();
      finish();
    }
  }

  private void toggleGps(boolean enableGps) {
    if (enableGps) {
      // Check if user has granted location permission
      permissionsManager = new PermissionsManager(this);
      if (!PermissionsManager.areLocationPermissionsGranted(this)) {
        permissionsManager.requestLocationPermissions(this);
      } else {
        enableLocation(true);
      }
    } else {
      enableLocation(false);
    }
  }

  private void enableLocation(boolean enabled) {
    if (enabled) {
      // If we have the last location of the user, we can move the camera to that position.
      Location lastLocation = locationEngine.getLastLocation();
      if (lastLocation != null) {
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lastLocation), 16));
      }

      locationEngineListener = new LocationEngineListener() {
        @Override
        public void onConnected() {
          // No action needed here.
        }

        @Override
        public void onLocationChanged(Location location) {
          if (location != null) {
            // Move the map camera to where the user location is and then remove the
            // listener so the camera isn't constantly updating when the user location
            // changes. When the user disables and then enables the location again, this
            // listener is registered again and will adjust the camera once again.
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location), 16));
            locationEngine.removeLocationEngineListener(this);
          }
        }
      };
      locationEngine.addLocationEngineListener(locationEngineListener);
    }
    // Enable or disable the location layer on the map
    map.setMyLocationEnabled(enabled);
  }
}