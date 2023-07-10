package mg.rivolink.app.aruco;

import static mg.rivolink.app.aruco.renderer.utils.CameraParameters.setLoad;

import android.app.Activity;
import android.content.Intent;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import android.util.Log;
import android.view.WindowManager;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import mg.rivolink.app.aruco.renderer.Renderer3D;
import mg.rivolink.app.aruco.renderer.utils.CameraParameters;
import mg.rivolink.app.aruco.view.PortraitCameraLayout;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.aruco.Aruco;
import org.opencv.aruco.DetectorParameters;
import org.opencv.aruco.Dictionary;
import org.opencv.calib3d.Calib3d;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDouble;
import org.opencv.core.MatOfInt;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.MatOfPoint3f;
import org.opencv.core.Point;
import org.opencv.core.Point3;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;
import org.rajawali3d.view.SurfaceView;


public class MainActivity extends AppCompatActivity implements CvCameraViewListener2 {
	public static final float SIZE = 0.04f;

	private Mat cameraMatrix;
	private MatOfDouble distCoeffs;

	public Mat rgb;
	private boolean isRunning = false;
	private Mat gray;

	private Mat rvecs;
	private Mat tvecs;

	private MatOfInt ids;
	private List<Mat> corners;
	private Dictionary dictionary;
	private DetectorParameters parameters;

	private Renderer3D renderer;
	private CameraBridgeViewBase camera;

	private final BaseLoaderCallback loaderCallback = new BaseLoaderCallback(this){
        @Override
        public void onManagerConnected(int status){
			if(status == LoaderCallbackInterface.SUCCESS){
				Activity activity = MainActivity.this;

				cameraMatrix = Mat.eye(3, 3, CvType.CV_64FC1);
				distCoeffs = new MatOfDouble(Mat.zeros(5, 1, CvType.CV_64FC1));
				setLoad();

				double[] cmdata = new double[]{
					1237.5201888491065,0.0,549.2419674255135,0.0,1340.8775773435984,1072.538177539563,0.0,0.0,1.0
				};
				double[] dcdata = new double[]{
					0.02101134272881212,0.6981242376794832,-0.061819162040075315,-8.069517868665383E-4,-1.373537063628747
				};

				cameraMatrix.put(0, 0, cmdata);
				distCoeffs.put(0, 0, dcdata);

				camera.enableView();
			}
			else {
				super.onManagerConnected(status);
			}
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState){

        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.main_layout);

        camera = ((PortraitCameraLayout)findViewById(R.id.camera_layout)).getCamera();
        camera.setVisibility(SurfaceView.VISIBLE);
        camera.setCvCameraViewListener(this);

		renderer = new Renderer3D(this);

		// add in multi-threading here, and the frame processing

		SurfaceView surface = (SurfaceView)findViewById(R.id.main_surface);
		surface.setTransparent(true);
		surface.setSurfaceRenderer(renderer);

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data){
		CameraParameters.onActivityResult(this, requestCode, resultCode, data, cameraMatrix, distCoeffs);
	}

	@Override
    public void onResume(){
        super.onResume();

		if(OpenCVLoader.initDebug())
			loaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
		else
			Toast.makeText(this, getString(R.string.error_native_lib), Toast.LENGTH_LONG).show();
    }

	@Override
    public void onPause(){
        super.onPause();

        if(camera != null)
            camera.disableView();
    }

	@Override
    public void onDestroy(){
        super.onDestroy();

        if (camera != null)
            camera.disableView();
    }

	@Override
	public void onCameraViewStarted(int width, int height){
		rgb = new Mat();
		ids = new MatOfInt();
		corners = new LinkedList<>();
		parameters = DetectorParameters.create();
		dictionary = Aruco.getPredefinedDictionary(Aruco.DICT_6X6_50);
	}

	@Override
	public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame){
		Imgproc.cvtColor(inputFrame.rgba(), rgb, Imgproc.COLOR_RGBA2RGB);
		gray = inputFrame.gray();

		if (!isRunning){
			isRunning = true;
			t1.start();
		}
//		Log.i("DBG-THD", "TC:: " + Thread.currentThread());
		return rgb;
	}

	private void arucoDetectionAsync(Mat cameraMatrix, MatOfDouble distCoeffs, Dictionary dictionary, DetectorParameters parameters, MatOfInt ids, List<Mat> corners) {
		//TODO: start MT here
//		corners.clear();
		Aruco.detectMarkers(gray, dictionary, corners, ids, parameters);
		rvecs = new Mat();
		tvecs = new Mat();
		Aruco.drawDetectedMarkers(rgb, corners, ids);
		if(corners.size()>0){
			Aruco.estimatePoseSingleMarkers(corners, SIZE, cameraMatrix, distCoeffs, rvecs, tvecs);
			for(int i = 0;i<ids.toArray().length;i++){
//				draw3dCube(rgb, cameraMatrix, distCoeffs, rvecs.row(i), tvecs.row(i), new Scalar(255, 0, 0));
				// this point dictates the "cube" rendering through image processing
				Aruco.drawAxis(rgb, cameraMatrix, distCoeffs, rvecs.row(i), tvecs.row(i), SIZE/2.0f);
			}
		}
		camera.deliverAndDrawFrame(rgb);
	}

	@Override
	public void onCameraViewStopped(){
		rgb.release();
		isRunning = false;
	}

	public void draw3dCube(Mat frame, Mat cameraMatrix, MatOfDouble distCoeffs, Mat rvec, Mat tvec, Scalar color){
		double halfSize = SIZE/2.0;

		List<Point3> points = new ArrayList<Point3>();
		points.add(new Point3(-halfSize, -halfSize, 0));
		points.add(new Point3(-halfSize,  halfSize, 0));
		points.add(new Point3( halfSize,  halfSize, 0));
		points.add(new Point3( halfSize, -halfSize, 0));
		points.add(new Point3(-halfSize, -halfSize, SIZE));
		points.add(new Point3(-halfSize,  halfSize, SIZE));
		points.add(new Point3( halfSize,  halfSize, SIZE));
		points.add(new Point3( halfSize, -halfSize, SIZE));

		MatOfPoint3f cubePoints = new MatOfPoint3f();
		cubePoints.fromList(points);

		MatOfPoint2f projectedPoints = new MatOfPoint2f();
		Calib3d.projectPoints(cubePoints, rvec, tvec, cameraMatrix, distCoeffs, projectedPoints);

		List<Point> pts = projectedPoints.toList();

	    for(int i=0; i<4; i++){
	        Imgproc.line(frame, pts.get(i), pts.get((i+1)%4), color, 2);
	        Imgproc.line(frame, pts.get(i+4), pts.get(4+(i+1)%4), color, 2);
	        Imgproc.line(frame, pts.get(i), pts.get(i+4), color, 2);
	    }
	}

	private void transformModel(final Mat tvec, final Mat rvec){
		runOnUiThread(new Runnable(){
			@Override
			public void run(){
				renderer.transform(
					tvec.get(0, 0)[0]*50,
					-tvec.get(0, 0)[1]*50,
					-tvec.get(0, 0)[2]*50,

					rvec.get(0, 0)[2], //yaw
					rvec.get(0, 0)[1], //pitch
					rvec.get(0, 0)[0] //roll
				);
			}
		});
	}
	
	Thread t1 = new Thread(new Runnable() {
		@Override
		public void run() {
			Thread.currentThread().setPriority(10);
			while (true) {
				arucoDetectionAsync(cameraMatrix, distCoeffs, dictionary, parameters, ids, corners);
			}
		}
	});
}


