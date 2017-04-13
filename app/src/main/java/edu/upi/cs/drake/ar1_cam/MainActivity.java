package edu.upi.cs.drake.ar1_cam;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.hardware.Camera;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class MainActivity extends AppCompatActivity {

    SurfaceView cameraView;
    SurfaceHolder viewHolder;
    Camera camera;
    boolean inPreview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        inPreview = false;

        cameraView = (SurfaceView) findViewById(R.id.cameraView);
        viewHolder = cameraView.getHolder();
        viewHolder.addCallback(surfaceCallback);
        viewHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);


    }

    @Override
    public void onResume(){
        super.onResume();
        camera = Camera.open();
    }

    @Override
    public void onPause(){
        super.onPause();
        if(inPreview){
            camera.stopPreview();
        }

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
}
