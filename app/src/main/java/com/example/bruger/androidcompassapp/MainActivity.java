package com.example.bruger.androidcompassapp;


import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.TextView;

//LATITUDE AND LONGITUDE ONLY WORKS ON ANDROID DEVICES NOT EMULATORS
public class MainActivity extends AppCompatActivity implements SensorEventListener {
    static final int REQUEST_LOCATION = 1;                                                          //Variable to be able to request a location from location class
    LocationManager locationManager;                                                                //declaring a variable of the type LocationManager

    private ImageView imageView;                                                                    //Declaring a variable with the type imageView
    private float[] mGravity = new float[3];                                                        //Creating a new float array for data from the accelerometer
    private float[] mGeomagnetic = new float[3];                                                    //Creating a new float array for data from the geomagnetic sensor
    private float azimuth = 0f;                                                                     //Creating a varible for rotation
    private float currentAzimuth = 0f;                                                              //Creating a varible for the current rotation in the z-axis
    private SensorManager mSensorManager;                                                           //Declaring a variable of the type SensorManager

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);             //Returning a reference for a certain system service object, in this case a LocationManager objcet
        getLocation();

        imageView = findViewById(R.id.compass);                                                     //Defining the variable imageView from the R file, to give it the right ID
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);                          //Determining if the sensor is present and available to be used
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {                                                    //method creating in order to get results after being granted permission
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);                   //LATITUDE AND LONGITUDE ONLY WORKS ON ANDROID DEVICES NOT EMULATORS

        switch (requestCode) {                                                                      //Variable for the switch case
            case REQUEST_LOCATION:                                                                  //Only does it if it is equal to the request_location
                getLocation();                                                                      //If it is equal to the request_location it'll run the getLocation method
                break;                                                                              //Ends the switch case
        }
    }

    void getLocation() {
        if ((ActivityCompat.checkSelfPermission(this, Manifest.permission.                  //if statement to check if the app has permission to use location from the mobile
                ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) && (ActivityCompat.
                checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED)) {

            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.        //If it does not, it will request access to the location
                    ACCESS_FINE_LOCATION}, REQUEST_LOCATION);

        } else {
            Location location = locationManager.getLastKnownLocation(LocationManager.               //if it does, it will get the latitude and longitude variable from locattion service
                    NETWORK_PROVIDER);
            if (location != null) {
                double latitude = location.getLatitude();                                           //variable to store latitude
                double longitude = location.getLongitude();                                         //variable to store longitude

                ((TextView) findViewById(R.id.etLocationLat)).setText(
                        getString(R.string.latitude_message) + latitude);                           //setting a certain textview with casting to a R string and the latitude variable

                ((TextView) findViewById(R.id.etLocationLong)).setText
                        (getString(R.string.longitude_message) + longitude);                        //setting a certain textview with casting to a R string and the longitude variable

            } else {
                ((TextView) findViewById(R.id.etLocationLat)).setText                               //If the device does not grant access it will display an error message in the textView
                        (R.string.uncorrect_location_message);
                ((TextView) findViewById(R.id.etLocationLong)).setText                              //If the device does not grant access it will display an error message in the textView
                        (R.string.uncorrect_location_message);
            }
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor               //Registers listeners to be able to update the magnetic field sensor when being inside the app
                (Sensor.TYPE_MAGNETIC_FIELD), SensorManager.SENSOR_DELAY_GAME);                     //How fast the manager should update, set to GAME for the magnetic field sensor
        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor               //Registers listeners to be able to update the accelerometer sensor when being inside the app
                (Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_UI);                        //How fast the manager should update, set to UI for the acclerometer


    }

    @Override
    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this);                                                    //Unregisters the listener when you are not inside the app, would waste processor power otherwise
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        final float alpha = 0.97f;                                                                  //Declaring and initializing the float variable alpha
        synchronized (this) {                                                                       //Limiting this if-statement to only be accessed by one thread, as it would cause too many updates for the app to handle and use a lot of processing power
            if (sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {                        //if-statement if the sensor is equal to the acceleraometer
                mGravity[0] = alpha * mGravity[0] + (1 - alpha) * sensorEvent.values[0];            //Storing the acceleration in the x-axis in the first spot of the array, calculation of the values from documentation from android
                mGravity[1] = alpha * mGravity[1] + (1 - alpha) * sensorEvent.values[1];            //Storing the acceleration in the y-axis in the second spot of the array, calculation of the values from documentation from android
                mGravity[2] = alpha * mGravity[2] + (1 - alpha) * sensorEvent.values[2];            //Storing the acceleration in the z-axis in the third spot of the array, calculation of the values from documentation from android
            }
            if (sensorEvent.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {                       //if-statement if the sensor is euqal to the magnetic field sensor
                mGeomagnetic[0] = alpha * mGeomagnetic[0] + (1 - alpha) * sensorEvent.values[0];    //Storing the geomagnetic field strength along the x-axis in the first spot of the array, calculation of the values from documentation from android
                mGeomagnetic[1] = alpha * mGeomagnetic[1] + (1 - alpha) * sensorEvent.values[1];    //Storing the geomagnetic field strength along the first spot of the array, calculation of the values from documentation from android
                mGeomagnetic[2] = alpha * mGeomagnetic[2] + (1 - alpha) * sensorEvent.values[2];    //Storing the geomagnetic field strength along the first spot of the array, calculation of the values from documentation from android
            }
            float R[] = new float[9];       //initializing a new float array R
            float I[] = new float[9];       //initializing a new float array I
            boolean success = SensorManager.getRotationMatrix(R, I, mGravity, mGeomagnetic);        //initialzing a new boolean variable, which is equal to the arguemtn of R, I, mGravity and mGeomagnetic
            if (success) {                                                                          //If-statement if it is true
                float orientation[] = new float[3];                                                 //initializing a new float array called orientation
                SensorManager.getOrientation(R, orientation);                                       //Gets the orientation and puts it into the array of R and orientation
                azimuth = (float) Math.toDegrees(orientation[0]);                                   //Calculates the values of orientation to degrees and sets azimuth equal to it
                azimuth = (azimuth + 360) % 360;                                                    //Calculates the new value of azimuth

                //
                Animation anim = new RotateAnimation(-currentAzimuth, -azimuth,                     //Make a new rotation object which is negative in the rotate, as it has to rotate the opposite way that you are tilting the phone
                        Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,    //It turens around itself in the middle of the screen at x at 0.5f and y at 0.5f
                        0.5f);
                currentAzimuth = azimuth;                                                           //Set the currentAzimuth to azimuth value

                anim.setDuration(500);                                                              //Duration of the animation
                anim.setRepeatCount(0);                                                             //How many times it should repeat the animation, 0 times in this case
                anim.setFillAfter(true);                                                            //Makes sure the animation persists after it has finished

                imageView.startAnimation(anim);                                                     //Says that imageView should start the animation


            }

        }
        TextView direction = findViewById(R.id.textView3);                                          //Defining the variable direction and reference it to textView3
        if (azimuth >= 350 || azimuth <= 10) {                                                      //Start of the if-statement, where it checks what the current rotation is
            direction.setText(R.string.north);                                                                 //And changes the direction name according to the current rotation
        } else if (azimuth < 350 && azimuth > 280) {
            direction.setText(R.string.northwest);
        } else if (azimuth <= 280 && azimuth > 260) {
            direction.setText(R.string.west);
        } else if (azimuth <= 260 && azimuth > 190) {
            direction.setText(R.string.southwest);
        } else if (azimuth <= 190 && azimuth > 170) {
            direction.setText(R.string.south);
        } else if (azimuth <= 170 && azimuth > 100) {
            direction.setText(R.string.southeast);
        } else if (azimuth <= 100 && azimuth > 80) {
            direction.setText(R.string.east);
        } else if (azimuth <= 80 && azimuth > 10) {
            direction.setText(R.string.northeast);
        }


        TextView degrees = findViewById(R.id.textView2);                                            //Defining the variable degrees and reference it to textView2
        degrees.setText((int) azimuth + getString(R.string.degrees_symbol));                                                       //sets the text in textView 2 to the variable azimuth + R.string.degrees_symbol value

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }


}
