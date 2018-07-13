package vn.edu.hcmus.fit.cntn15.bookswap;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class DrawerActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private FirebaseAuth mAuth;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private boolean mLocationPermissionGranted;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private Location mLastKnownLocation;

    Retrofit retrofit = new Retrofit.Builder()
            .baseUrl("http://171.244.43.48:13097")
            .addConverterFactory(GsonConverterFactory.create())
            .build();

    ServerAPI mServer = retrofit.create(ServerAPI.class);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drawer);


        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mAuth = FirebaseAuth.getInstance();

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        displaySelectedScreen(R.id.nav_map);

        getLocationPermission();

        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.drawer, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
//            Log.i("clicked", "clicked");
            CustomDialogClass customDialog = new CustomDialogClass(DrawerActivity.this);
            customDialog.show();

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void displaySelectedScreen(int itemId) {

        //creating fragment object
        Fragment fragment = null;

        //initializing the fragment object which is selected
        switch (itemId) {
            case R.id.nav_history:
//                fragment = new History();
                startActivity(new Intent(DrawerActivity.this, HistoryActivity.class));
                break;
            case R.id.nav_logout: {
                mAuth.signOut();
                startActivity(new Intent(DrawerActivity.this, LoginActivity.class));
            }
                break;
            case R.id.nav_map:
                fragment = new Map();
                break;
        }

        //replacing the fragment
        if (fragment != null) {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.content_frame, fragment);
            ft.commit();
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {

        displaySelectedScreen(item.getItemId());
        return true;
    }

    public class CustomDialogClass extends Dialog {

        public Activity activity;
        public Button yes, no;
        public EditText droppedTitle, droppedDesc;

        public CustomDialogClass(Activity a) {
            super(a);
            // TODO Auto-generated constructor stub
            this.activity = a;
        }

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            requestWindowFeature(Window.FEATURE_NO_TITLE);
            setContentView(R.layout.drop_dialog);

            yes = findViewById(R.id.drop_yes);
            no = findViewById(R.id.drop_no);
            droppedTitle = findViewById(R.id.DropTitleInput);
            droppedDesc = findViewById(R.id.DropDescInput);

            yes.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        if (mLocationPermissionGranted) {
                            Task<Location> locationResult = mFusedLocationProviderClient.getLastLocation();
                            locationResult.addOnCompleteListener(new OnCompleteListener<Location>() {
                                @Override
                                public void onComplete(@NonNull Task<Location> task) {
                                    if (task.isSuccessful()) {
                                        // Set the map's camera position to the current location of the device.
                                        mLastKnownLocation = task.getResult();
                                        Log.i("Location", String.format("(%f, %f)", mLastKnownLocation.getLatitude(),
                                                mLastKnownLocation.getLongitude()));
                                        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                                        Call<BookURI> cGiveBook = mServer.giveBook(new SentBook(user.getUid(), droppedTitle.getText().toString(), droppedDesc.getText().toString(),
                                                mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude()));
                                        cGiveBook.enqueue(new Callback<BookURI>() {
                                            @Override
                                            public void onResponse(Call<BookURI> call, Response<BookURI> response) {
                                                BookURI bookURI = response.body();
                                                if (bookURI.URI.equals("NULL")) {
                                                    Toast.makeText(getContext(), "Your book is not accepted", Toast.LENGTH_SHORT).show();
                                                } else {
                                                    Toast.makeText(getContext(), "Your book is accepted", Toast.LENGTH_SHORT).show();
                                                }
                                            }

                                            @Override
                                            public void onFailure(Call<BookURI> call, Throwable t) {
                                                call.cancel();
                                                Toast.makeText(getContext(), "Failed to send request to server", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                    } else {
                                        //Log.d(TAG, "Current location is null. Using defaults.");
                                        //Log.e(TAG, "Exception: %s", task.getException());
                                        Toast.makeText(getContext(), "Unable to get current location", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        } else {
                            Toast.makeText(getContext(), "Unable to get current location", Toast.LENGTH_SHORT).show();
                        }
                        //String skeleton = "[{\"name\": \"" + droppedTitle + "\", \"description\": \"" + droppedDesc + "\"}]";
//                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
//                    String uid = user.getUid();
//                    Log.i("clicked", uid);
                    } catch (SecurityException e) {
                        Log.e("Exception: %s", e.getMessage());
                    }
                }
            });

            no.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dismiss();
                }
            });

        }

    }

    private void getLocationPermission() {
        /*
         * Request location permission, so that we can get the location of the
         * device. The result of the permission request is handled by a callback,
         * onRequestPermissionsResult.
         */
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        mLocationPermissionGranted = false;
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mLocationPermissionGranted = true;
                }
            }
        }
    }

}
