package vn.edu.hcmus.fit.cntn15.bookswap;

import android.Manifest;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.gson.Gson;

import java.util.HashMap;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;


public class Map extends Fragment implements GoogleMap.OnMarkerClickListener, OnMapReadyCallback {

    private static final String TAG = Map.class.getSimpleName();

    MapView mMapView;
    Handler mapFetcher = new Handler();
    private GoogleMap mMap;
    private HashMap<String, Marker> bookMarkers = new HashMap<>();
    private boolean mLocationPermissionGranted;
    protected Location mLastKnownLocation;
    private final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private final LatLng mDefaultLocation = new LatLng(-33.8523341, 151.2106085);
    private static final int DEFAULT_ZOOM = 15;

    Retrofit retrofit = new Retrofit.Builder()
            .baseUrl("http://171.244.43.48:13097")
            .addConverterFactory(GsonConverterFactory.create())
            .build();
    ServerAPI mServer = retrofit.create(ServerAPI.class);

    Runnable updateBookData = new Runnable() {
        @Override
        public void run() {
            Call<BookInfo[]> cBooks = mServer.fetchBookData();
            cBooks.enqueue(new Callback<BookInfo[]>() {
                @Override
                public void onResponse(Call<BookInfo[]> call, Response<BookInfo[]> response) {
                    BookInfo[] books = response.body();

                    for (BookInfo book : books) {
                        if (book.taken == 1) {
                            Marker marker = bookMarkers.get(book.id);
                            if (marker != null) {
                                marker.remove();
                                bookMarkers.remove(book.id);
                            }
                        } else {
                            if (bookMarkers.get(book.id) == null) {
                                LatLng point = new LatLng(book.Lat, book.Lng);
                                Marker marker = mMap.addMarker(new MarkerOptions()
                                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.book))
                                        .anchor((float)0.5, (float)0.5)
                                        .position(point)
                                        .title(book.name)
                                        .snippet(book.description));
                                marker.setTag(book.id);
                                bookMarkers.put(book.id, marker);
                            }
                        }
                    }
                }

                @Override
                public void onFailure(Call<BookInfo[]> call, Throwable t) {
                    call.cancel();
                }
            });
            /*
            String skeleton = "[{\"id\": \"3\", \"name\": \"Compedium\", \"description\": \"Vampire\", \"Lat\": -31.952854, \"Lng\": 115.857342}]";
            Gson gson = new Gson();
            BookInfo[] books = gson.fromJson(skeleton, BookInfo[].class);
            for (BookInfo book : books) {
                if (!bookMarkers.containsKey(book.id)) {
                    LatLng point = new LatLng(book.Lat, book.Lng);
                    Marker marker = mMap.addMarker(new MarkerOptions().position(point).title(book.name).snippet(book.description));
                    bookMarkers.put(book.id, marker);
                }
            }
            */
            mapFetcher.postDelayed(this, 5000);
        }
    };

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        //returning our layout file
        //change R.layout.yourlayoutfilename for each of your fragments
        View rootView = inflater.inflate(R.layout.activity_maps, container, false);

        mMapView = rootView.findViewById(R.id.map);
        mMapView.onCreate(savedInstanceState);

        // Construct a FusedLocationProviderClient.
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getActivity());

        mMapView.onResume();
        try {
            MapsInitializer.initialize(getActivity().getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }
        mMapView.getMapAsync(this);
        return rootView;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        updateLocationUI();
        getLocationPermission();

        mMap.setInfoWindowAdapter(new CustomInfoWindowAdapter());
        mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(final Marker marker) {
                new AlertDialog.Builder(getActivity())
                        .setTitle("Borrow?")
                        .setMessage("Do you wanna build a snowman?")
                        .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //Borrow
                                String book_id = (String)marker.getTag();
                                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                                String uid = user.getUid();

                                Call<GetStatus> cTakeBook = mServer.takeBook(new TakenBook(uid, book_id));
                                cTakeBook.enqueue(new Callback<GetStatus>() {
                                    @Override
                                    public void onResponse(Call<GetStatus> call, Response<GetStatus> response) {
                                        GetStatus status = response.body();
                                        if (status.status.equals("ok")) {
                                            Toast.makeText(getContext(), "Book looted.", Toast.LENGTH_SHORT).show();
                                        } else {
                                            Toast.makeText(getContext(), "Loot failed", Toast.LENGTH_SHORT).show();
                                        }
                                    }

                                    @Override
                                    public void onFailure(Call<GetStatus> call, Throwable t) {
                                        call.cancel();
                                    }
                                });
                            }
                        })
                        .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //nope
                            }
                        })
                        .create()
                        .show();
            }
        });
        // Add a marker in Sydney and move the camera
        initBookLocation(mMap);
        /*
        LatLng sydney = new LatLng(-34, 151);
        //mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney").snippet("This is a very long text in order to test compatible. Please don't read this. Or you will regret spending 5 mininutes of your life wasted."));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
        */
        getDeviceLocation();
        mapFetcher.postDelayed(updateBookData, 5000);
    }

    private void getDeviceLocation() {
        /*
         * Get the best and most recent location of the device, which may be null in rare
         * cases when a location is not available.
         */
        try {
            if (mLocationPermissionGranted) {
                Task<Location> locationResult = mFusedLocationProviderClient.getLastLocation();
                locationResult.addOnCompleteListener(new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        if (task.isSuccessful()) {
                            // Set the map's camera position to the current location of the device.
                            mLastKnownLocation = task.getResult();
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                                    new LatLng(mLastKnownLocation.getLatitude(),
                                            mLastKnownLocation.getLongitude()), DEFAULT_ZOOM));
                        } else {
                            Log.d(TAG, "Current location is null. Using defaults.");
                            Log.e(TAG, "Exception: %s", task.getException());
                            mMap.moveCamera(CameraUpdateFactory
                                    .newLatLngZoom(mDefaultLocation, DEFAULT_ZOOM));
                        }
                    }
                });
            }
        } catch (SecurityException e) {
            Log.e("Exception: %s", e.getMessage());
        }
        updateLocationUI();
    }

    private void updateLocationUI() {
        if (mMap == null) {
            return;
        }
        try {
            if (mLocationPermissionGranted) {
                mMap.setMyLocationEnabled(true);
                mMap.getUiSettings().setMyLocationButtonEnabled(true);
            } else {
                mMap.setMyLocationEnabled(false);
                mMap.getUiSettings().setMyLocationButtonEnabled(false);
                mLastKnownLocation = null;
                getLocationPermission();
            }
        } catch (SecurityException e)  {
            Log.e("Exception: %s", e.getMessage());
        }
    }

    public void getLocationPermission() {
        if (ContextCompat.checkSelfPermission(getContext(),
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                new AlertDialog.Builder(getActivity())
                        .setTitle("")
                        .setMessage("")
                        .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //Prompt the user once explanation has been shown
                                ActivityCompat.requestPermissions(getActivity(), new String[]
                                        {Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
                            }
                        })
                        .create()
                        .show();


            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(getActivity(),
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
            }
        } else {
            mLocationPermissionGranted = true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // location-related task you need to do.
                    if (ContextCompat.checkSelfPermission(getActivity(),
                            Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {

                        mLocationPermissionGranted = true;
                        updateLocationUI();
                    }

                }
                return;
            }

        }
    }

    void initBookLocation(GoogleMap map) {
        ServerAPI server = retrofit.create(ServerAPI.class);
        Call<BookInfo[]> cBooks = server.fetchBookData();
        cBooks.enqueue(new Callback<BookInfo[]>() {
            @Override
            public void onResponse(Call<BookInfo[]> call, Response<BookInfo[]> response) {
                BookInfo[] books = response.body();
                for (BookInfo book : books) {
                    if (book.taken == 0) {
                        LatLng point = new LatLng(book.Lat, book.Lng);
                        Marker marker = mMap.addMarker(new MarkerOptions()
                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.book))
                                .anchor((float)0.5, (float)0.5)
                                .position(point)
                                .title(book.name)
                                .snippet(book.description));
                        marker.setTag(book.id);
                        bookMarkers.put(book.id, marker);
                    }
                }
            }

            @Override
            public void onFailure(Call<BookInfo[]> call, Throwable t) {
                call.cancel();
            }
        });
        /*
        String skeleton = "[{\"id\": \"1\", \"name\": \"Harry Potter\", \"description\": \"meo meo\", \"Lat\": -27.47093, \"Lng\": 153.0235, \"taken\": 0}," +
                "{\"id\": \"2\", \"name\": \"LOTR\", \"description\": \"sad\", \"Lat\": -37.81319, \"Lng\": 144.96298, \"taken\": 0}]";

        Gson gson = new Gson();
        BookInfo[] books = gson.fromJson(skeleton, BookInfo[].class);
        for (BookInfo book : books) {
            if (book.taken == 0) {
                LatLng point = new LatLng(book.Lat, book.Lng);
                Marker marker = map.addMarker(new MarkerOptions().position(point).title(book.name).snippet(book.description));
                bookMarkers.put(book.id, marker);
            }
        }
        */
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //you can set the title for your toolbar here for different fragments different titles
        getActivity().setTitle("Where are books?");
    }

    @Override
    public void onResume() {
        super.onResume();
        mMapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mMapView.onPause();
    }

    @Override
    public void onDestroy() {
        mapFetcher.removeCallbacks(updateBookData);
        super.onDestroy();
        mMapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mMapView.onLowMemory();
    }

    @Override
    public boolean onMarkerClick(final Marker marker) {

        // We return false to indicate that we have not consumed the event and that we wish
        // for the default behavior to occur (which is for the camera to move such that the
        // marker is centered and for the marker's info window to open, if it has one).
        return true;
    }

    class CustomInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {

        // These are both viewgroups containing an ImageView with id "badge" and two TextViews with id
        // "title" and "snippet".
        private final View mWindow;

        private final View mContents;

        CustomInfoWindowAdapter() {
            mWindow = getLayoutInflater().inflate(R.layout.custom_info_window, null);
            mContents = getLayoutInflater().inflate(R.layout.custom_info_contents, null);
        }

        @Override
        public View getInfoWindow(Marker marker) {
            render(marker, mWindow);
            return mWindow;
        }

        @Override
        public View getInfoContents(Marker marker) {
            render(marker, mContents);
            return mContents;
        }


        private void render(Marker marker, View view) {

            String title = marker.getTitle();
            TextView titleUi = view.findViewById(R.id.title);
            if (title != null) {
                // Spannable string allows us to edit the formatting of the text.
                SpannableString titleText = new SpannableString(title);
                titleText.setSpan(new ForegroundColorSpan(Color.RED), 0, titleText.length(), 0);
                titleUi.setText(titleText);
            } else {
                titleUi.setText("");
            }

            String snippet = marker.getSnippet();
            TextView snippetUi = view.findViewById(R.id.snippet);
            if (snippet != null && snippet.length() > 12) {
                SpannableString snippetText = new SpannableString(snippet);
                snippetText.setSpan(new ForegroundColorSpan(Color.MAGENTA), 0, 10, 0);
                snippetText.setSpan(new ForegroundColorSpan(Color.BLUE), 12, snippet.length(), 0);
                snippetUi.setText(snippetText);
            } else {
                snippetUi.setText("");
            }
        }
    }


}

