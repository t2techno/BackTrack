package com.example.tt.backtrack;

import android.Manifest;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.tt.backtrack.data.ItemContract;
import com.example.tt.backtrack.data.ItemDbHelper;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.io.File;

/*makes a connection to GoogleApiClient for location data, check to verify that location permissions
 *are granted, implements a number of Dialog fragments found in Dialogs file. User can click one of
 *three buttons:
 *
 *Set Location: Asks for the name of place to be saved, then allows user to take picture of location
 *   Saves location to SQLite database, Saves picture with a unique name(iterator number cat'd to end)
 *   to Device storage/Android/data/com.example.tt.backtrack/files/Pictures/BackTrackImages
 *
 *Find Item: Brings up a list of saved location with name in center, and picture to left if taken
 *   Clicking brings up dialog box asking if you would like driving or walking direction, as well
 *   as a full size image of picture if taken.
 *   Long-Clicking deletes the picture from storage, the item info from database, and reloads updated
 *   list
 *
 *Help: Brings up a list of instructions on how to use the app
 */
public class MainActivity extends AppCompatActivity implements LocationListener ,GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, Dialogs.NameDialog.NoticeDialogListener,
        Dialogs.CameraDialog.NoticeDialogListener, Dialogs.GPSDialog.NoticeDialogListener,
        Dialogs.PermDialog.NoticeDialogListener{

    private static final String TAG = "TAG!";
    Context context = this;

    FragmentManager fm = getSupportFragmentManager();

    //ImageStuff
    private static final int IMAGE_CAPTURE = 102;
    String name;//global string variable for name of item being saved
    int counter = 1;

    //LocationStuff
    GoogleApiClient mGoogleApiClient;
    Location mLastLocation;

    //private LocationClient mLocationClient;

    private LocationRequest mLocationRequest;

    static final int LOCATION_PERMISSION_REQUEST = 1;
    double currentLongitude;
    double currentLatitude;
    String permissions[] = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};

    //DatabaseStuff

    //creates an instance of my database helper
    ItemDbHelper mDbHelper = new ItemDbHelper(this);

    //instance of Asynctask class for opening database off main thread
    openDB open = new openDB();

    //global database
    SQLiteDatabase itemDb;

    //bool for determining if a picture is taken
    boolean pictureTaken;

    boolean gps_on = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        myRequest(permissions,LOCATION_PERMISSION_REQUEST);

        Button set_location = (Button) findViewById(R.id.location_button);
        Button find_location = (Button) findViewById(R.id.find_button);
        Button help = (Button) findViewById(R.id.help_button);
        if(!JustCheckGPS()){
            checkGPSDialog();
        }
        //create an instance of GoogleAPIClient for location finding
        if(mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }

        open.execute();

        mGoogleApiClient.connect();



        set_location.setOnClickListener(
                new Button.OnClickListener() {
                    public void onClick(View v) {
                        LocationSet();
                    }
                }
        );

        find_location.setOnClickListener(
                new Button.OnClickListener() {
                    public void onClick(View v) {
                        BackTrack();
                    }
                }
        );

        help.setOnClickListener(
                new Button.OnClickListener() {
                    public void onClick(View v) {
                        SendHelp();
                    }
                }
        );
    }

    //connects to google api client
    @Override
    protected void onStart(){
        mGoogleApiClient.connect();
        super.onStart();
    }

    //disconnects googleApi client if it's active
    @Override
    protected void onPause() {
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
        super.onPause();
    }

    //reconnects google api client if necessary
    @Override
    protected void onResume(){
        if(mGoogleApiClient != null){
            mGoogleApiClient.connect();
        }
        super.onResume();
    }

    //disconnects google api client
    @Override
    protected void onStop(){
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    //sets up location check
    //checks for location permissions
    //checks if location services are turned on
    @Override
    public void onConnected(Bundle connectionHint) {
        int goAhead = ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION);
        if (goAhead == PackageManager.PERMISSION_GRANTED) {
            mLocationRequest = LocationRequest.create()
                    .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                    .setInterval(1000*1000)
                    .setFastestInterval(1000);
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);

            Log.v(TAG, "yay permission!");
            mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            Log.v(TAG, "location found - " + String.valueOf(mLastLocation));
        } else {
            if(ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                showExplanation();
            } else {
                myRequest(permissions, LOCATION_PERMISSION_REQUEST);
            }

        }
    }

    //Gets new location
    @Override
    public void onLocationChanged(Location location){
        Log.v(TAG, "Le Update");
        mLastLocation = location;
    }

    //if they have rejected permission multiple times
    private void showExplanation() {
        Dialogs.PermDialog perm = new Dialogs.PermDialog();
        perm.show(fm, "permission");
    }

    //Calls myRequest
    @Override
    public void onPermDialogPositiveClick(){
        myRequest(permissions, LOCATION_PERMISSION_REQUEST);
    }

    //gives them the permission request
    private void myRequest(String permissions[], int permissionRequestCode) {
        Log.v(TAG, "Requesting Permission....");
        ActivityCompat.requestPermissions(this,
                permissions, permissionRequestCode);
    }

    //if they agreed, get location, carry on
    //if rejected, do nothing, but they'll be asked again as soon as they try to do something
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults){
        switch(requestCode){
            case LOCATION_PERMISSION_REQUEST: {
                ContextCompat.checkSelfPermission(this,
                        android.Manifest.permission.ACCESS_FINE_LOCATION);
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
                    Log.v(TAG, "location found - " + String.valueOf(mLastLocation));
                }
            }
        }
    }


    //logs the connection was suspended
    @Override
    public void onConnectionSuspended(int i) {
        Log.i(TAG, "connection suspended");
    }

    //logs the connection failed
    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.i(TAG, "connection failed");
    }

    //opens database on background thread and assigns to global db var for writing to
    public class openDB extends AsyncTask<Void, Void, SQLiteDatabase> {

        @SafeVarargs
        @Override
        protected final SQLiteDatabase doInBackground(Void... voids) {
            SQLiteDatabase db = mDbHelper.getWritableDatabase();
            return db;
        }

        @Override
        protected void onPostExecute(SQLiteDatabase db) {
            itemDb = db;
        }
    }


    //asks for the name of thing being saved via alert
    //calls CameraAsk, passes the name entered
    private void LocationSet() {
        if(JustCheckGPS()){
            Dialogs.NameDialog input = new Dialogs.NameDialog();
            input.show(fm, "input");
        } else {
            checkGPSDialog();
        }
    }

    //assigns input to global name variable for later use
    //only activated if user finished alert
    @Override
    public void onInputDialogPositiveClick(String input) {
       // Log.v(TAG, "INPUT - " + input);
        name = input;
        //Log.v(TAG, "after first click - " + name);
        CameraAsk();
    }


    //asks if a picture should be taken, if camera is available
    //calls launchCamera with true arg if yes
    //calls launchCamera with false arg if no
    private void CameraAsk() {
        if (hasCamera() && isExternalStorageWritable()) {
            //Log.v(TAG, "CAMERASK - ");

            Dialogs.CameraDialog cameraDialog = new Dialogs.CameraDialog();
            cameraDialog.show(fm, "camera");
        }
    }

    //runs function launchCamera with boolean determining if the user affirmed or denied camera
    @Override
    public void onCameraDialogPositiveClick(boolean b){
        launchCamera(b);
    }

    //runs function launchCamera with boolean determining if the user affirmed or denied camera
    @Override
    public void onCameraDialogNegativeClick(boolean b){
        launchCamera(b);
    }


    //if arg is false, pictureTaken var = false and does nothing else
    //if arg is true, calls hasCamera to check if its available
    //if available, sends intent to camera for picture taking
    //saves file source to global var fileUri pictureTaken = true
    //image is named after the item name saved in onInputDialogPositiveClick
    private void launchCamera(boolean b) {
        Log.v(TAG, "LAUNCHCAMERA - " + b + " - " + name );
        Uri fileUri;
        String path = "null";
        if (b) {
            String AlbumName = "BackTrackImages";
            File mediaFile = new File(getAlbumStorageDir(context, AlbumName), name + counter + ".jpg");
            counter++;//iterates global variable, ensuring unique path names, given same user entered names
            path = mediaFile.getPath();
            fileUri = Uri.fromFile(mediaFile);
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

            intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
            startActivityForResult(intent, IMAGE_CAPTURE);
        }
        pictureTaken = b;
        SaveItem(name, path);
    }

    //Saves the name, lat, and long to the database
    //Saves image path as string if pictureTaken,
    //Path is "null" if not
    void SaveItem(String item, String path) {
        Log.v(TAG, "SAVEITEM - " + (item != null) + "\n- " + (mLastLocation != null));
        if (item != null) {
            ContentValues values = new ContentValues();
            currentLongitude = mLastLocation.getLongitude();
            currentLatitude = mLastLocation.getLatitude();

            values.put(ItemContract.ItemLocation.ITEM_NAME, item + counter);
            counter++;
            values.put(ItemContract.ItemLocation.COLUMN_COORD_LAT, currentLatitude);
            values.put(ItemContract.ItemLocation.COLUMN_COORD_LONG, currentLongitude);

            Log.v(TAG, "Saving... " + path);
            values.put(ItemContract.ItemLocation.COLUMN_IMAGE_PATH, path);
            counter++;

            //send values to asynctask for insertion in database
            long newRowId;
            if (itemDb != null) {
                newRowId = itemDb.insert(ItemContract.ItemLocation.TABLE_NAME, null, values);
                Log.v(TAG, String.valueOf(newRowId));
            } else {
                Log.e(TAG, "Database null");
            }
        }
    }

    //checks to see if there is a camera available
    //returns true if so, false if not
    private boolean hasCamera() {
        if (getPackageManager().hasSystemFeature(
                PackageManager.FEATURE_CAMERA)) {
            return true;
        } else {
            return false;
        }
    }

    //called when Find Item button clicked
    //Sends intent to ItemList activity for listing items that can be directed to
    private void BackTrack() {
        if(JustCheckGPS()){
            Intent intent = new Intent(this, ItemList.class);
            startActivity(intent);
        } else{
            checkGPSDialog();
            mGoogleApiClient.reconnect();
        }
    }

    //called when help button clicked
    private void SendHelp(){
        Intent intent = new Intent(this, Help.class);
        startActivity(intent);
    }

    //gives a toast feedback if image taken
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == IMAGE_CAPTURE) {
            if (resultCode == RESULT_OK) {
                Toast.makeText(MainActivity.this, "Image Saved", Toast.LENGTH_SHORT)
                        .show();
            } else if (resultCode == RESULT_CANCELED) {
                Toast.makeText(MainActivity.this, "Image Capture canceled", Toast.LENGTH_SHORT)
                        .show();
            } else {
                Toast.makeText(MainActivity.this, "Image Capture failed", Toast.LENGTH_SHORT)
                        .show();
            }
        }
    }

    // Checks if external storage is available for read and write
    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    // Checks if external storage is available to at least read
    public boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            return true;
        }
        return false;
    }

    //creates the necessary directory for my app images if first time run
    public File getAlbumStorageDir(Context context, String albumName) {
        // Get the directory for the app's private pictures directory.
        File file = null;
        if (isExternalStorageReadable()) {
            file = new File(context.getExternalFilesDir(
                    Environment.DIRECTORY_PICTURES), albumName);
            if (!file.mkdirs()) {
                Log.e(TAG, "Directory not created");
            }
        } else {
            Log.e(TAG, "Directory not readable");
        }
        return file;
    }

    //gives dialog asking for location services to be turned on
    public void checkGPSDialog () {
        Dialogs.GPSDialog ask = new Dialogs.GPSDialog();
        ask.show(fm, "gps");
    }

    //checks if location permission granted, if so, returns true or false depending on if location
    //service are available

    public boolean JustCheckGPS () {
        LocationManager lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        boolean network_enabled;
        int goAhead = ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION);
        if (goAhead != PackageManager.PERMISSION_GRANTED){
            myRequest(permissions, LOCATION_PERMISSION_REQUEST);
        }
        gps_on = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);

        network_enabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        Log.v(TAG, "gps - " + gps_on + "\nnetwork - " + network_enabled);
        if (!gps_on || !network_enabled ) {
            return false;
        } else {
            return true;
        }
    }

    //sends them to location service screen to turn them on
    @Override
    public void onGPSDialogPositiveClick(){
        Intent myIntent = new Intent( Settings.ACTION_LOCATION_SOURCE_SETTINGS );
        this.startActivity(myIntent);
        Log.v(TAG, "SET TO ON");
    }
}
