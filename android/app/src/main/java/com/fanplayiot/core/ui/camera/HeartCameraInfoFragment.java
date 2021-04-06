package com.fanplayiot.core.ui.camera;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.PowerManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.airbnb.lottie.LottieAnimationView;

import com.demoproject.R;
import com.fanplayiot.core.foreground.service.FanEngageListener;
import com.fanplayiot.core.foreground.service.FanFitListener;
import com.fanplayiot.core.viewmodel.FanEngageViewModel;
import com.fanplayiot.core.viewmodel.HomeViewModel;
import com.fanplayiot.core.viewmodel.FanFitCommon;
import com.fanplayiot.core.viewmodel.FanFitCommonVMFactory;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import static com.fanplayiot.core.googlefit.ServiceCallbackKt.HEART_RATE_TASK;
import static com.fanplayiot.core.ui.camera.CameraUtils.getSmallestPreviewSize;
import static com.fanplayiot.core.utils.Constant.COUNTDOWNINTERVAL;

@SuppressWarnings("deprecation")
public class HeartCameraInfoFragment extends BottomSheetDialogFragment {

    private static final String TAG = "HeartCameraInfoFragment";
    private static final int CAMERA_MS_INFUTURE = 21000; // Heart rate from camera time out in ms
    private static final String EXTRA_IS_FAN_FIT = "EXTRA_IS_FAN_FIT";
    private FanEngageListener fanEngageListener = null;
    private FanFitListener fanFitListener = null;

    private Button startButton;
    private ImageView cameraImage;
    private TextView textDesc;
    private LottieAnimationView textCountDown;
    private SurfaceView preview;
    private static Camera camera = null;
    private static SurfaceHolder previewHolder = null;
    private static HRPreviewCallback previewCallback = new HRPreviewCallback();
    PowerManager.WakeLock wakeLock;
    private TextView cameraText;
    private boolean isFanFit = false;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param isFanFit Parameter isFanFit should be set to true for Heart rate called from fan fit.
     * @return A new instance of fragment HeartRatePermissionFragment.
     */
    public static HeartCameraInfoFragment newInstance(boolean isFanFit) {
        HeartCameraInfoFragment fragment = new HeartCameraInfoFragment();
        Bundle bundle = new Bundle();
        bundle.putBoolean(EXTRA_IS_FAN_FIT, isFanFit);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_heart_camera_info, container, false);

        cameraImage = view.findViewById(R.id.heart_camera_img);
        startButton = view.findViewById(R.id.heart_modal_start);
        preview = view.findViewById(R.id.preview);
        cameraText = view.findViewById(R.id.heart_camera_modal_title);
//        textCountDown = view.findViewById(R.id.heart_rate_count);
        textCountDown = (LottieAnimationView) view.findViewById(R.id.animation_counter);
        textDesc = view.findViewById(R.id.heart_camera_modal_desc);

        PowerManager pm = (PowerManager) requireActivity().getSystemService(Context.POWER_SERVICE);
        wakeLock = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK, "com.fanplayiot.core:DoNotDimScreen");
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (getActivity() == null) return;
        final HomeViewModel homeViewModel = new ViewModelProvider(requireActivity()).get(HomeViewModel.class);
        FanEngageViewModel feViewModel = new ViewModelProvider(requireActivity()).get(FanEngageViewModel.class);
        previewCallback.setFanEngageListener(feViewModel);

        if (savedInstanceState != null) {
            isFanFit = savedInstanceState.getBoolean(EXTRA_IS_FAN_FIT, false);
        }
        if (getArguments() != null) {
            isFanFit = getArguments().getBoolean(EXTRA_IS_FAN_FIT, false);
        }
        if (isFanFit) {
            FanFitCommonVMFactory commonVMFactory = new FanFitCommonVMFactory(requireActivity().getApplication(), homeViewModel);
            FanFitCommon ffViewModel =  new ViewModelProvider(requireActivity(), commonVMFactory).get(FanFitCommon.class);
            previewCallback.setFanFitListener(ffViewModel);
            previewCallback.setFanEngageListener(null);
        } else {
            previewCallback.setFanEngageListener(feViewModel);
            previewCallback.setFanFitListener(null);

        }
        previewHolder = preview.getHolder();
        previewHolder.addCallback(surfaceCallback);
        previewHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        if (!CameraUtils.checkFlash(requireActivity())) {
            Toast.makeText(requireContext(), R.string.camera_flash, Toast.LENGTH_LONG).show();
            //((MainActivity) requireActivity()).showMessages(R.string.camera_flash, -1);
            stopProgress();
            dismiss();
        }
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                heartRateFromCamera();
            }
        });
    }

    @SuppressLint("WakelockTimeout")
    @Override
    public void onResume() {
        super.onResume();
        wakeLock.acquire();
        try {
            boolean isEnabled = CameraUtils.hasCameraPermission(requireContext());
            if (isEnabled) {
                camera = Camera.open();
            }
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        wakeLock.release();
        if (camera == null) return;
        camera.setPreviewCallback(null);
        camera.stopPreview();
        camera.release();
        camera = null;
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);
        startProgress();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(EXTRA_IS_FAN_FIT, isFanFit);
    }

    private static SurfaceHolder.Callback surfaceCallback = new SurfaceHolder.Callback() {


        /**
         * {@inheritDoc}
         */
        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            try {
                camera.setPreviewDisplay(previewHolder);
                camera.setPreviewCallback(previewCallback);
            } catch (Throwable t) {
                Log.e(TAG, "Exception in setPreviewDisplay()", t);
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            try {
                Camera.Parameters parameters = camera.getParameters();
                parameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
                Camera.Size size = getSmallestPreviewSize(width, height, parameters);
                if (size != null) {
                    parameters.setPreviewSize(size.width, size.height);
                    //Log.d(TAG, "Using width=" + size.width + " height=" + size.height);
                }
                camera.setParameters(parameters);
                camera.startPreview();
            } catch (Exception e) {
                Log.e(TAG, "error ", e);
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            // Ignore
        }
    };

    public void heartRateFromCamera() {
        try {
            if (getContext() == null) return;
            //heartRateDialog.show();
            cameraImage.setVisibility(View.INVISIBLE);
            textDesc.setVisibility(View.INVISIBLE);
            startButton.setVisibility(View.INVISIBLE);

            preview.setVisibility(View.VISIBLE);
            textCountDown.setVisibility(View.VISIBLE);
            cameraText.setText("Please cover the camera fully with finger");

            if (camera == null) camera = Camera.open();
            previewCallback.resetStartTime();
//            textCountDown.cancelAnimation();
            textCountDown.playAnimation();
            startProgress();
            new CountDownTimer(CAMERA_MS_INFUTURE, COUNTDOWNINTERVAL) {

                @Override
                public void onTick(long l) {
//                    textCountDown.setText(String.valueOf(l / COUNTDOWNINTERVAL));
                }

                @Override
                public void onFinish() {
//                    textCountDown.setText(R.string.done);
                    textCountDown.cancelAnimation();
                    stopPreview();
                }
            }.start();

        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
        }
    }

    private void stopPreview() {
        cameraImage.setVisibility(View.VISIBLE);
        preview.setVisibility(View.INVISIBLE);
        if (camera == null) return;
        camera.setPreviewCallback(null);
        camera.stopPreview();
        dismiss();
    }

    private void startProgress() {
        if (fanEngageListener != null) fanEngageListener.onPreExecute(HEART_RATE_TASK);
        if (fanFitListener != null) fanFitListener.onPreExecute(HEART_RATE_TASK);
    }

    private void stopProgress() {
        if (fanEngageListener != null) fanEngageListener.onPostExecute(HEART_RATE_TASK);
        if (fanFitListener != null) fanFitListener.onPostExecute(HEART_RATE_TASK);
    }
}