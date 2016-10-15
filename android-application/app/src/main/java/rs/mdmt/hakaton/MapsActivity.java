package rs.mdmt.hakaton;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Camera;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.location.LocationListener;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.gson.JsonObject;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,
        GoogleMap.OnMarkerClickListener, GoogleMap.OnMapClickListener, GoogleMap.OnMapLongClickListener, View.OnClickListener, ServerHandler.ResponseCallback
{

    private GoogleMap mMap;

    private LocationManager mManager;
    private LocationListener mLocation;

    private Map<Marker, MarkerAttributes> mMarkersHash;

    private String userId;
    private Boolean callType;
    private int userType;

    private Handler handler;
    private int routeSetting = 0;

    private Route newRoute;
    private GMapV2Direction directions;

    private Marker myMarker, newMarker;

    public static class Mode {

        public static final int NEW = 0;
        public static final int EDIT = 1;
        public static final int VIEW = 2;

        private Mode () {}
    }

    private int mode = Mode.VIEW;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        ServerConnection.getHandler().setResponseCallback(this);

        mMarkersHash = new HashMap<Marker, MarkerAttributes>();
        directions = new GMapV2Direction();

        handler = new Handler();
        final SharedPreferences prefs = Application.getContext()
                .getSharedPreferences(Application.SHARED_PREF_KEY, Context.MODE_PRIVATE);
        userId = prefs.getString("userId", "Null");

        callType = getIntent().getBooleanExtra("callType", false); // false = all shelters, true = new shelter

        FloatingActionButton addNewRouteButton = (FloatingActionButton) findViewById(R.id.add_new_route);

        userType = prefs.getInt("mode", Application.NEED_HELP);
        if(userType == Application.HELPER)
        {
            addNewRouteButton.setOnClickListener(MapsActivity.this);
            addNewRouteButton.setVisibility(View.VISIBLE);
        }
        else
        {
            addNewRouteButton.setOnClickListener(null);
            addNewRouteButton.setVisibility(View.INVISIBLE);
        }

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        mMap.setOnMapLongClickListener(this);

        mManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        myMarker = mMap.addMarker(new MarkerOptions().position(new LatLng(43.331685, 21.892441))
                .icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_person)));

        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(myMarker.getPosition(), 10));

        mManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 2000, 100, new android.location.LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                LatLng mLatLng = new LatLng(location.getLatitude(), location.getLongitude());

                myMarker.setPosition(mLatLng);
                mMap.moveCamera(CameraUpdateFactory.newLatLng(mLatLng));
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(mLatLng, 20));
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }
        });
        if(callType)
        {
            mMap.setOnMapClickListener(MapsActivity.this);
        }
        else
        {
            mMap.setOnMarkerClickListener(MapsActivity.this);
            Task.Data task = new Task.Data(new Task.Data.DataReadyCallback()
            {
                @Override
                public void dataReady()
                {
                    JSONArray data = getData();

                    shelterUpdate(data);
                }
            });

            Task.Data routeTask = new Task.Data(new Task.Data.DataReadyCallback()
            {
                @Override
                public void dataReady()
                {

                    JSONArray data = getData();

                    routeUpdate(data);

                }
            })
            {
                @Override
                public void executeImpl()
                {
                    ServerConnection.getRoutes();
                }
            };

            TaskManager.getInstance().execute(task);
            TaskManager.getInstance().execute(routeTask);
        }


    }

    @Override
    public boolean onMarkerClick(Marker marker)
    {
        if (newMarker != null
                && marker.getPosition().latitude == newMarker.getPosition().latitude
                && marker.getPosition().longitude == newMarker.getPosition().longitude)
        {
            Intent i = new Intent(this, NewShelterActivity.class);

            i.putExtra("type", Mode.NEW);
            i.putExtra("latitude", marker.getPosition().latitude);
            i.putExtra("longitude", marker.getPosition().longitude);
            i.putExtra("userId", userId);

            startActivity(i);
            return true;
        }
        else
        {
            MarkerAttributes markerAttr = mMarkersHash.get(marker);
            String markerId = markerAttr.getMarkerId();
            String markerUserId = markerAttr.getUserId();

            if (routeSetting == 1)
            {
                newRoute = new Route();
                newRoute.setFromId(markerId);
                routeSetting = 2;
            }
            else if (routeSetting == 2)
            {
                newRoute.setToId(markerId);

//            cDialog dialog = new cDialog(this.getApplicationContext());

//            cDialog.DialogListener listener = new cDialog.DialogListener() {
//                @Override
//                public void okButtonClick(int seats, String time) {
//                    sendRoute(newRoute, seats, time);
//                }
//            };

                final Dialog dialog = new Dialog(this);
                dialog.setContentView(R.layout.route_dialog);
                dialog.setTitle("Route");

                Button okButton = (Button) dialog.findViewById(R.id.okButton);
                final EditText seats = (EditText) dialog.findViewById(R.id.seatsText);
                final EditText time = (EditText) dialog.findViewById(R.id.timeText);
                okButton.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View view)
                    {
                        sendRoute(newRoute, Integer.parseInt(seats.getText().toString()),
                                time.getText().toString());
                        dialog.dismiss();
                    }
                });

                dialog.show();

                routeSetting = 0;
            }
            else
            {
                Intent i = new Intent(this, NewShelterActivity.class);
                i.putExtra("type", Mode.VIEW);
                i.putExtra("shelterId", markerId);
                startActivity(i);
            }
        }
        return true;
    }

    private void sendRoute(Route newRoute, int seats, String time) {

        final JsonObject json = new JsonObject();

        json.addProperty("type", ServerConnection.Request.INSERT_ROUTE);

        JsonObject data = new JsonObject();

        data.addProperty("fromId", newRoute.getFromId());
        data.addProperty("toId", newRoute.getToId());
        data.addProperty("seats", seats);
        data.addProperty("departure", time);

        json.addProperty("data", data.toString());

        Task task = new Task(Task.GENERAL) {

            @Override
            public void execute() {
                ServerConnection.send(json.toString());
            }
        };

        TaskManager.getInstance().execute(task);
    }

    @Override
    public void onMapClick(LatLng latLng) {
        Intent i = new Intent(this, NewShelterActivity.class);

        i.putExtra("type", Mode.NEW);
        i.putExtra("latitude", latLng.latitude);
        i.putExtra("longitude", latLng.longitude);
        i.putExtra("userId", userId);

        startActivity(i);
    }

    // Route Action Button onClick
    @Override
    public void onClick(View v) {
        if(routeSetting == 0) {
            routeSetting = 1;
            Toast.makeText(getApplicationContext(), "Route setting enabled", Toast.LENGTH_SHORT).show();
        }
        else {
            routeSetting = 0;
            Toast.makeText(getApplicationContext(), "Route setting disabled", Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    public void onMapLongClick(LatLng latLng)
    {
        newMarker = mMap.addMarker(new MarkerOptions().position(latLng));
        newMarker.setDraggable(true);
    }

    public void RouteHandling(Document doc)
    {
        ArrayList<LatLng> directionPoint = directions.getDirection(doc);

        PolylineOptions rectLine = new PolylineOptions().width(10).color(
                Color.CYAN);

        for (int j = 0; j < directionPoint.size(); j++) {
            rectLine.add(directionPoint.get(j));
        }
        Polyline polylin = mMap.addPolyline(rectLine);
    }

    public void shelterUpdate(JSONArray data)
    {
        try
        {
            for (int i = 0; i < data.length(); ++i)
            {
                JSONObject obj = (JSONObject) data.get(i);

                MarkerOptions mOptions = new MarkerOptions().position(new LatLng(
                        obj.getDouble("latitude"), obj.getDouble("longitude")));

                String type = obj.getString("type");

                if (obj.has("userId") && userId.equals(obj.getString("userId")))
                    type = "my" + type;

                int id = Application.getContext().getResources().getIdentifier(type, "mipmap",
                        Application.getContext().getPackageName());
                BitmapDescriptor desc = BitmapDescriptorFactory.fromResource(id);

                mOptions.icon(desc);

                Marker marker = mMap.addMarker(mOptions);

                mMarkersHash.put(marker, new MarkerAttributes(obj.getString("userId"), obj.get("_id").toString()));
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public void routeUpdate(JSONArray data)
    {
        try
        {
            for (int i = 0; i < data.length(); i += 2)
            {
                JSONObject obj = (JSONObject) data.get(i);
                JSONObject shelter = (JSONObject) obj.get("shelter");
                final LatLng from = new LatLng(shelter.getDouble("latitude"), shelter.getDouble("longitude"));

                obj = (JSONObject) data.get(i + 1);
                shelter = (JSONObject) obj.get("shelter");
                final LatLng to = new LatLng(shelter.getDouble("latitude"), shelter.getDouble("longitude"));

                Thread t = new Thread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        final Document doc = directions.getDocument(from, to, GMapV2Direction.MODE_DRIVING);

                        handler.post(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                RouteHandling(doc);
                            }
                        });
                    }
                });

                t.start();
            }
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
    }
}
