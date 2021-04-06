package com.fanplayiot.core.ui.camera;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.demoproject.R;
import com.fanplayiot.core.ui.camera.CameraUtils;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link HeartRatePermissionFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HeartRatePermissionFragment extends BottomSheetDialogFragment {

    private Button yesHeartModal;
    private TextView permissionText;
    private TextView usageText;
    private Button noHeartModal;

    public HeartRatePermissionFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment HeartRatePermissionFragment.
     */
    public static HeartRatePermissionFragment newInstance(String param1, String param2) {
        HeartRatePermissionFragment fragment = new HeartRatePermissionFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_heart_rate_permission, container, false);

        yesHeartModal = view.findViewById(R.id.yes_heart_modal);
        noHeartModal = view.findViewById(R.id.no_heart_modal);
        permissionText= view.findViewById(R.id.heart_modal_permission_text);
        usageText = view.findViewById(R.id.heart_camera_modal_desc);

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        yesHeartModal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CameraUtils.checkPermission(requireActivity());
                dismiss();
            }
        });

        noHeartModal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
    }
}