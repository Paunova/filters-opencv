package mk.ukim.finki.filter;

import android.app.Activity;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.Button;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfInt;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

public class MainActivity extends Activity implements CameraBridgeViewBase.CvCameraViewListener2{
    private static final int CANNY_THRESHOLD = 150;
    // threshold value for canny hysteresis
    private static final int THRESHOLD_DIFFERENCE = 20;

    private static final int NORMAL = 1;
    private static final int GRAYSCALE = 2;
    private static final int EDGE = 3;
    private static final int BLUR = 4;
    private static final int SHARPEN = 5;
    private int selectedView = NORMAL;

    private CameraBridgeViewBase mOpenCvCameraView;

    private static final String TAG = "MainActivity";
    static {
        if(!OpenCVLoader.initDebug()){
            Log.d(TAG, "OpenCV not loaded");
        } else {
            Log.d(TAG, "OpenCV loaded");
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_main);

        mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.HelloOpenCvView);
        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
        mOpenCvCameraView.setCvCameraViewListener(this);

        initButtons();
    }

    private void initButtons() {
        Button normal = (Button)findViewById(R.id.btn_filter_normal);
        normal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectedView = NORMAL;
            }
        });
        Button gray = (Button)findViewById(R.id.btn_filter_gray);
        gray.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectedView = GRAYSCALE;
            }
        });
        Button edge = (Button)findViewById(R.id.btn_filter_edge);
        edge.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectedView = EDGE;
            }
        });
        Button blur = (Button)findViewById(R.id.btn_filter_blur);
        blur.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectedView = BLUR;
            }
        });
        Button sharp = (Button)findViewById(R.id.btn_filter_sharp);
        sharp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectedView = SHARPEN;
            }
        });

    }

    @Override
    public void onPause()
    {
        super.onPause();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    public void onDestroy() {
        super.onDestroy();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i(TAG, "OpenCV loaded successfully");
                    mOpenCvCameraView.enableView();
                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };

    @Override
    public void onResume()
    {
        super.onResume();
        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_6, this, mLoaderCallback);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        menu.add("Menu item 1");
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
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCameraViewStarted(int width, int height) {

    }

    @Override
    public void onCameraViewStopped() {

    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        if (selectedView == GRAYSCALE) {
            Log.d("SELECTED_VIEW", "GRAYSCALE");
            return inputFrame.gray();
        } else if (selectedView == EDGE) {
            Log.d("SELECTED_VIEW", "EDGE");
            Mat gray = inputFrame.gray();
            Mat result = new Mat(gray.size(), gray.type());
            Imgproc.Canny(gray, result, CANNY_THRESHOLD, CANNY_THRESHOLD + THRESHOLD_DIFFERENCE);
            return result;
        } else if (selectedView == BLUR) {
            Log.d("SELECTED_VIEW", "BLUR");
            Mat rgba = inputFrame.rgba();
            Mat result = new Mat(rgba.size(), rgba.type());
            Imgproc.blur(rgba, result, new Size(7, 7));
            return result;
        } else if (selectedView == SHARPEN) {
            Log.d("SELECTED_VIEW", "SHARPEN");
            Mat img = inputFrame.rgba();
            Mat sharpened = new Mat();
            Imgproc.filter2D(img, sharpened, -1, new MatOfInt(
                     0, -1,  0,
                    -1,  5, -1,
                     0, -1,  0
            ));
            return sharpened;
        }

        Log.d("SELECTED_VIEW", "NORMAL");
        return inputFrame.rgba();
    }
}
