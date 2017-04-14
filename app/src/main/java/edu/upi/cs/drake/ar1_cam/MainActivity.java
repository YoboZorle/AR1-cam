package edu.upi.cs.drake.ar1_cam;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.hardware.Camera;
import android.util.Log;
import android.util.StringBuilderPrinter;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class MainActivity extends AppCompatActivity {

    //view variables
    SurfaceView cameraView;
    SurfaceHolder viewHolder;
    Camera camera;
    boolean inPreview;
    final static String Tag = "PAAR";

    //sensor variables
    SensorManager sensorManager;

    //orientation variables
    int orientationSensor;
    float headingAngle;
    float pitchAngle;
    float rollAngle;

    //accelerometer variables
    int accelerometerSensor;
    float xAxis;
    float yAxis;
    float zAxis;

    //GPS variables
    LocationManager locationManager;
    double longitude;
    double latitude;
    double altitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        inPreview = false;

        cameraView = (SurfaceView) findViewById(R.id.cameraView);
        viewHolder = cameraView.getHolder();
        viewHolder.addCallback(surfaceCallback);
        viewHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        accelerometerSensor = Sensor.TYPE_ACCELEROMETER;
        orientationSensor = Sensor.TYPE_ORIENTATION;
        sensorManager.registerListener(sensorEventListener,
                sensorManager.getDefaultSensor(accelerometerSensor),
                sensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(sensorEventListener,
            sensorManager.getDefaultSensor(orientationSensor),
            SensorManager.SENSOR_DELAY_NORMAL);
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000, 2, locationListener);
    }

    @Override
    public void onResume(){
        super.onResume();
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000, 2, locationListener);
        sensorManager.registerListener(sensorEventListener,
                sensorManager.getDefaultSensor(orientationSensor), SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(sensorEventListener,
                sensorManager.getDefaultSensor(accelerometerSensor), SensorManager.SENSOR_DELAY_NORMAL);
        camera = Camera.open();
    }

    @Override
    public void onPause(){
        super.onPause();
        if(inPreview){
            camera.stopPreview();
        }
        locationManager.removeUpdates(locationListener);
        sensorManager.unregisterListener(sensorEventListener);
        camera.release();
        camera = null;
        inPreview = false;
    }

    private Camera.Size getBestPreviewSize(int width, int height, Camera.Parameters parameters){
        Camera.Size result = null;

        for (Camera.Size size : parameters.getSupportedPreviewSizes()){
            if(size.width<=width && size.height<=height){
                if(result==null){
                    result = size;
                }else{
                    int resultArea = result.width*result.height;
                    int newArea = size.width*size.height;
                    if(newArea>resultArea){
                        result = size;
                    }
                }
            }
        }

        return result;
    }

    SurfaceHolder.Callback surfaceCallback = new SurfaceHolder.Callback(){
        public void surfaceCreated(SurfaceHolder holder){
            try{
                camera.setPreviewDisplay(viewHolder);
            }catch (Throwable t){
                Log.e("MainActivity", "Error in setPreviewDisplay");
            }
        }

        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height){
            Camera.Parameters parameters = camera.getParameters();
            Camera.Size size = getBestPreviewSize(width, height, parameters);
            if(size != null){
                parameters.setPreviewSize(size.width, size.height);
                camera.setParameters(parameters);
                camera.startPreview();
                inPreview = true;
            }
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {

        }
    };

    final SensorEventListener sensorEventListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent sensorEvent) {
            if(sensorEvent.sensor.getType() == Sensor.TYPE_ORIENTATION){
                headingAngle = sensorEvent.values[0];
                pitchAngle = sensorEvent.values[1];
                rollAngle = sensorEvent.values[2];

                Log.d(Tag, "Heading: " + String.valueOf(headingAngle));
                Log.d(Tag, "Pitch: " + String.valueOf(pitchAngle));
                Log.d(Tag, "Roll" + String.valueOf(rollAngle));
            }else if(sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER){
                xAxis = sensorEvent.values[0];
                yAxis = sensorEvent.values[1];
                zAxis = sensorEvent.values[2];

                Log.d(Tag, "xAxis: " + String.valueOf(xAxis));
                Log.d(Tag, "yAxis: " + String.valueOf(yAxis));
                Log.d(Tag, "zAxis: " + String.valueOf(zAxis));
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    };

    LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            longitude = location.getLongitude();
            latitude = location.getLatitude();
            altitude = location.getAltitude();

            Log.d(Tag, "Longitude: " + longitude);
            Log.d(Tag, "Latitude: " + latitude);
            Log.d(Tag, "Altitude: " + altitude);
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
    };
}
