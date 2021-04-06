package com.demoproject;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.facebook.react.ReactActivity;
import com.facebook.react.ReactInstanceManager;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DefaultHardwareBackBtnHandler;
import com.facebook.react.modules.core.DeviceEventManagerModule;
import com.facebook.react.modules.core.PermissionAwareActivity;
import com.facebook.react.modules.core.PermissionListener;
import com.fanplayiot.core.db.local.entity.Device;
import com.fanplayiot.core.db.local.entity.HeartRate;
import com.fanplayiot.core.db.local.entity.User;
import com.fanplayiot.core.foreground.service.DeviceServiceUtils;
import com.fanplayiot.core.foreground.service.FitnessService;
import com.fanplayiot.core.ui.AppLinksKt;
import com.fanplayiot.core.ui.camera.CameraUtils;
import com.fanplayiot.core.ui.camera.HeartCameraInfoFragment;
import com.fanplayiot.core.ui.camera.HeartRatePermissionFragment;
import com.fanplayiot.core.utils.Constant;
import com.fanplayiot.core.utils.SessionUtilsKt;
import com.fanplayiot.core.utils.UIUtilsKt;
import com.fanplayiot.core.viewmodel.FanEngageVMFactory;
import com.fanplayiot.core.viewmodel.FanEngageViewModel;
import com.fanplayiot.core.viewmodel.FanFitCommon;
import com.fanplayiot.core.viewmodel.FanFitCommonVMFactory;
import com.fanplayiot.core.viewmodel.HomeViewModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;
import com.razorpay.Checkout;
import com.razorpay.PaymentResultListener;

import kotlin.Unit;
import kotlin.jvm.functions.Function1;

import static com.fanplayiot.core.db.local.entity.FitnessKt.DEVICE_BAND;
import static com.fanplayiot.core.db.local.entity.FitnessKt.GOOGLE_FIT;
import static com.fanplayiot.core.db.local.entity.FitnessKt.PHONE;
import static com.fanplayiot.core.remote.FirebaseService.FAN_OF_HOUR;
import static com.fanplayiot.core.utils.Constant.CAMERA_REQUEST_CODE;
import static com.fanplayiot.core.utils.Constant.COUNTDOWNINTERVAL;
import static com.fanplayiot.core.utils.Constant.LOCATION_REQUEST_CODE;
import static com.fanplayiot.core.utils.Constant.REQUEST_IMAGE_CAPTURE;
import static com.fanplayiot.core.utils.Constant.REQUEST_PERMISSIONS_REQUEST_CODE;
import static com.fanplayiot.core.utils.UIUtilsKt.FANENGAGE_TAB_INDEX;

//import androidx.navigation.NavController;
//import androidx.navigation.fragment.NavHostFragment;
/*import com.fanplayiot.core.ui.auth.LoginActivity;
import com.fanplayiot.core.ui.bandfeature.AlarmFragment;
import com.fanplayiot.core.ui.bandfeature.DisplaySettingsFrag;
import com.fanplayiot.core.ui.bandfeature.FeatureDelegate;
import com.fanplayiot.core.ui.bandfeature.NotifyFragment; */
//import com.fanplayiot.core.ui.devices.ListDeviceModal;
//import com.fanplayiot.core.ui.home.PushNotificationDialog;
//import com.fanplayiot.core.ui.home.fanEngagement.BottomModalFragment;
//import com.fanplayiot.core.ui.home.social.SessionProgressDialog;

public class MainActivity extends ReactActivity implements NavigationView.OnNavigationItemSelectedListener, DefaultHardwareBackBtnHandler, PermissionAwareActivity, PaymentResultListener {
    /*
     * Get the ReactInstanceManager, AKA the bridge between JS and Android
     * We use a singleton here so we can reuse the instance throughout our app
     * instead of constantly re-instantiating and re-downloading the bundle
     */
    private ReactInstanceManager mReactInstanceManager;

    //, AuthenticationHandler
    private static final String TAG = "MainDrawer >>>";

    public static final int AUTO_DISMISS_MILLIS = 5000;

    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.IdTokenListener idTokenListener;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private HomeViewModel viewModel;
    private FanFitCommon fanFitCommon;
    private FanEngageViewModel fanEngageViewModel;
    //private FeatureDelegate featureDelegate;
    DeviceServiceUtils serviceUtils = DeviceServiceUtils.INSTANCE;
    private MenuItem versionMenu, pairPhoneMenu, featuresGroup;

    @Override
    public void requestPermissions(String[] permissions, int requestCode, PermissionListener listener) {

    }

    public interface MessageBoxListener {
        void onActionClick();
    }

    @SuppressLint({"RestrictedApi", "MissingPermission"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //setContentView(R.layout.activity_main_drawer);

//        Toolbar
        /*
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setDisplayHomeAsUpEnabled(false);
        */
//        Drawer Layout setup
        /*drawerLayout = findViewById(R.id.drawer_layout);
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
        NavController navController = null;
        if (navHostFragment != null) {
            navController = navHostFragment.getNavController();

        }

        navigationView = findViewById(R.id.nav_view);
        if (navigationView != null) {
            navigationView.setItemIconTintList(null);
            navigationView.setNavigationItemSelectedListener(this);
            Menu menu = navigationView.getMenu();
            featuresGroup = menu.findItem(R.id.device_setting_grp);
            pairPhoneMenu = menu.findItem(R.id.action_pair_phone);
            versionMenu = menu.findItem(R.id.action_version);

            Log.d(TAG, "onCreate:" +
                    navigationView.getMenu() +
                    "  ");
        }
        */
        // Initialize
        //featureDelegate = new FeatureDelegate(this);

        //FacebookSdk.sdkInitialize(getApplicationContext());
        //firebaseAuth = FirebaseAuth.getInstance();
        //NotificationHub.start(getApplication(), Constant.HubName, Constant.HubListenConnectionString);

        /*ImageView homeIv = (ImageView) findViewById(R.id.hamburger_icon);
        homeIv.setOnClickListener(v -> {
            if (drawerLayout.isDrawerOpen(Gravity.RIGHT)) {
                drawerLayout.closeDrawer(Gravity.RIGHT);
            } else {
                drawerLayout.openDrawer(Gravity.RIGHT);
            }
            if (featuresGroup != null) featuresGroup.setVisible(featureDelegate.isConnected());
            TextView versionText = versionMenu.getActionView().findViewById(R.id.menu_firmware_version);
            if (versionText != null) versionText.setVisibility(
                    featureDelegate.isConnected()? View.VISIBLE: View.INVISIBLE);
        });*/

        ViewModelProvider.AndroidViewModelFactory avmf = new ViewModelProvider.AndroidViewModelFactory(getApplication());
        viewModel = avmf.create(HomeViewModel.class); //new ViewModelProvider(this).get(HomeViewModel.class);
        FanFitCommonVMFactory commonVMFactory = new FanFitCommonVMFactory(getApplication(), viewModel);
        fanFitCommon = new ViewModelProvider(this, commonVMFactory).get(FanFitCommon.class);
        FanEngageVMFactory feVMFactory = new FanEngageVMFactory(getApplication(), viewModel);
        fanEngageViewModel = new ViewModelProvider(this, feVMFactory).get(FanEngageViewModel.class);
        /*if (versionMenu != null) {
            TextView menuVerText = (TextView) versionMenu.getActionView().findViewById(R.id.menu_version);
            displayVersion(menuVerText);
        }*/

        // Login check
        Intent inputIntent = getIntent();
        final Bundle extra;
        if (inputIntent.hasExtra(Constant.LOGIN_EXTRA)) {
            extra = inputIntent.getBundleExtra(Constant.LOGIN_EXTRA);
        } else {
            extra = new Bundle();
        }
        idTokenListener = new FirebaseAuth.IdTokenListener() {
            @Override
            public void onIdTokenChanged(@NonNull FirebaseAuth firebaseAuth) {
                checkAndUpdate(firebaseAuth, extra);
            }
        };
        checkAndUpdateMock();
//      React Native
        mReactInstanceManager =
                ((MainApplication) getApplication()).getReactNativeHost().getReactInstanceManager();


        viewModel.storeUrl.observe(this, new Observer<String>() {
            @Override
            public void onChanged(String url) {
                if (url != null) {
                    // do nothing
                }
            }
        });
        viewModel.affStoreLink.observe(this, new Observer<String>() {
            @Override
            public void onChanged(String url) {
                if (url != null) {
                    // do nothing
                }
            }
        });
        viewModel.userLive.observe(this, new Observer<User>() {
            @Override
            public void onChanged(User user) {
                if (user != null) {
                    // Register
                    /*InstallationTemplate testTemplate = new InstallationTemplate();
                    testTemplate.setBody("{\"data\":{\"message\":\"Notification Hub test notification: $myTextProp\"}}");
                    NotificationHub.setTemplate("testTemplate", testTemplate);
                    if (user.getSid() != null) {
                        NotificationHub.setUserId(String.valueOf(user.getSid()));
                    }
                    if (user.getSid() != null && !user.getTokenId().isEmpty()) {
                        viewModel.registerUser(user.getTokenId());
                    }*/

                }
            }
        });
        viewModel.teamIdServerLive.observe(this, new Observer<Long>() {
            @Override
            public void onChanged(Long teamIdServer) {
                if (teamIdServer != null) {
                    viewModel.updateTeamStoreUrl();
                    User user = viewModel.userLive.getValue();
                    if (user != null && user.getSid() != null && !user.getTokenId().isEmpty()) {
                        viewModel.registerUser(user.getTokenId());
                    }
                }
            }
        });
        UIUtilsKt.observeEvent(viewModel.tokenExpireTs, this, ts -> {
            if (ts != null && ts > 0L) {
                viewModel.checkTokenExpired();
            }
            return null;
        });

        /*ImageView referIv = (ImageView) findViewById(R.id.refer_icon);
        referIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewModel.inviteFriends(getApplicationContext());
            }
        });*/
        UIUtilsKt.observeEvent(viewModel.getGotoStartCamera(), this, new Function1<Boolean, Unit>() {
            @Override
            public Unit invoke(Boolean value) {
                if (value != null && value) {
                    startFeHeartRate(fanEngageViewModel.modeLive.getValue() != null
                            ? fanEngageViewModel.modeLive.getValue().intValue()
                            : 0);
                }
                return null;
            }
        });

        // Analytics
        viewModel.analyticsService.init(FirebaseAnalytics.getInstance(this));


        // FanFit service
        serviceUtils.getServiceRunning().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean isRunning) {
                if (isRunning != null && isRunning) {
                    if (serviceUtils.getReBind()) {
                        serviceUtils.stopObserver(getApplicationContext(), fanFitCommon);
                    } else {
                        serviceUtils.startHr(FitnessService.Caller.FanFit);
                    }
                } else if (isRunning != null) {
                    serviceUtils.stopObserver(getApplicationContext(), fanFitCommon);
                }
            }
        });
        serviceUtils.getCurrentCommand().observe(this, new Observer<DeviceServiceUtils.Command>() {
            @Override
            public void onChanged(DeviceServiceUtils.Command command) {
                if (command != null && command == DeviceServiceUtils.Command.STOP) {
                    fetchLastLocation();
                    serviceUtils.stopObserver(getApplicationContext(), fanFitCommon);
                    viewModel.setStopHeartRateReading(true);
                    SessionUtilsKt.emitEvent(mReactInstanceManager, false, -1);

                } else if (command != null && command == DeviceServiceUtils.Command.REBIND_STOP) {
                    fetchLastLocation();
                    viewModel.setStopHeartRateReading(true);
                    serviceUtils.stopObserver(getApplicationContext(), fanFitCommon);
                    serviceUtils.reBindServices(getApplicationContext(), fanFitCommon.getType());
                }
            }
        });
        fanFitCommon.startedSession.observe(this, new Observer<Long>() {
            @Override
            public void onChanged(Long sessionId) {
                if(sessionId != null && sessionId > 0L) {
                    serviceUtils.setSessionId(sessionId);
                    /*
                    if (serviceUtils.getProgressLive() != null) {
                        viewModel.progressLive.postValue(serviceUtils.getProgressLive().getValue());
                        showSessionDialog();
                    }*/
                }
            }
        });

        UIUtilsKt.observeEvent(fanFitCommon.getDeviceReady(), this, ready -> {
            if (ready != null && ready && fanFitCommon.startedSession.getValue() != null) {
                Log.d(TAG, "Active session " + fanFitCommon.startedSession.getValue());

            }
            return null;
        });
        UIUtilsKt.observeEvent(viewModel.getPaymentStatus(), this, status -> {
            if (status != null && status) {
                showMessages(R.string.payment_success, 1);
                // Trigger RN event to refresh challenges listing
                WritableMap payload = Arguments.createMap();
                // Put data to map
                payload.putBoolean("paymentStatus", true);
                mReactInstanceManager.getCurrentReactContext().
                        getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class).
                        emit("onPaymentSuccess", payload);
            }
            return null;
        });
        /*fanFitCommon.getPedometerEnabled().observe( this, enabled -> {
            pairPhoneMenu.setVisible(enabled != null && enabled);
        });
        fanFitCommon.getFirmwareVersion().observe(this, version -> {
            if (version != null && versionMenu != null) {
                TextView versionText = versionMenu.getActionView().findViewById(R.id.menu_firmware_version);
                if (versionText != null) {
                    versionText.setText(getString(R.string.firmware_version, version));
                    versionText.setVisibility(View.VISIBLE);
                }
            } else if (versionMenu != null) {
                TextView versionText = versionMenu.getActionView().findViewById(R.id.menu_firmware_version);
                if (versionText != null) versionText.setVisibility(View.INVISIBLE);
            }
        });*/
        fanFitCommon.getModeLive().observe(this, new Observer<Long>() {
            @Override
            public void onChanged(Long value) {
                if (value != null && value > 0) {
                    int type = value.intValue();
                    boolean hasPermission = fanFitCommon.getService().checkPermissions(getApplicationContext());
                    serviceUtils.unBindSCDService(getApplicationContext());
                    if (type == HeartRate.GOOGLE_FIT && !hasPermission) {
                        fanFitCommon.getService().requestPermissions(MainActivity.this, Constant.REQUEST_PERMISSIONS_REQUEST_CODE);
                        return;
                    } else if (type == HeartRate.GOOGLE_FIT) {
                        serviceUtils.bindSCDServices(getApplicationContext(), GOOGLE_FIT);
                    } else if (type == HeartRate.CAMERA) {
                        serviceUtils.bindSCDServices(getApplicationContext(), PHONE);
                    } else if (type == HeartRate.DEVICE_BAND) {
                        serviceUtils.bindSCDServices(getApplicationContext(), DEVICE_BAND);
                    }
                    //featureDelegate.setMode(value.intValue());
                }

            }
        });
        viewModel.pairPhone.observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean bPairPhone) {
                if (bPairPhone != null && bPairPhone) {
                    serviceUtils.unBindSCDService(getApplicationContext());
                    serviceUtils.bindSCDServices(getApplicationContext(), PHONE);
                    viewModel.pairPhone.setValue(false);
                }
            }
        });
        fanEngageViewModel.modeLive.observe(this, new Observer<Long>() {
            @Override
            public void onChanged(Long aLong) {

            }
        });
        fanEngageViewModel.bandLive.observe(this, new Observer<Device>() {
            @Override
            public void onChanged(Device device) {
                if (device != null) {
                    serviceUtils.setBandAddress(device.getAddress());
                }
            }
        });

        // Razor pay
        Checkout.preload(getApplicationContext());

        Uri appLinkData = inputIntent.getData();
        AppLinksKt.handleDeepLinks(appLinkData, viewModel);
        serviceUtils.onIntent(inputIntent.getAction(), inputIntent.getExtras());
    }

    @Override
    public void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Uri appLinkData = intent.getData();
        AppLinksKt.handleDeepLinks(appLinkData, viewModel);
        serviceUtils.onIntent(intent.getAction(), intent.getExtras());
    }

    private void checkAndUpdate(@NonNull FirebaseAuth firebaseAuth, final Bundle bundle) {
        // Get signedIn user
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user != null) {
            //final String displayName = user.getDisplayName();
            final String displayName = bundle.getString(Constant.EXTRA_DISPLAY_NAME, user.getDisplayName());
            final int loginType = bundle.getInt(Constant.EXTRA_LOGIN_TYPE, User.ID_PROVIDER_LOGIN);
            user.getIdToken(true).addOnCompleteListener(new OnCompleteListener<GetTokenResult>() {
                @Override
                public void onComplete(@NonNull Task<GetTokenResult> task) {
                    if (task.isSuccessful() && task.getResult() != null) {
                        String idToken = task.getResult().getToken();
                        long expires = task.getResult().getExpirationTimestamp() * 1000;
                        // check and store in DB
                        viewModel.insertOrUpdate(idToken, expires, displayName, loginType);
                        checkPermissions();
                    } else {
                        Log.d(TAG, "Token error ", task.getException());
                    }
                }
            });
        } else {
            //Intent intent = new Intent(MainDrawer.this, LoginActivity.class);
            //startActivity(intent);
            finish();
        }
    }

    private void checkAndUpdateMock() {
        // check and store in DB
        String idToken = "eyJhbGciOiJSUzI1NiIsImtpZCI6ImY4NDY2MjEyMTQxMjQ4NzUxOWJiZjhlYWQ4ZGZiYjM3ODYwMjk5ZDciLCJ0eXAiOiJKV1QifQ.eyJpc3MiOiJodHRwczovL3NlY3VyZXRva2VuLmdvb2dsZS5jb20vZmFucGxheS1kZXYiLCJhdWQiOiJmYW5wbGF5LWRldiIsImF1dGhfdGltZSI6MTYxNzAyNDM5NiwidXNlcl9pZCI6InlCQ2F1dUxGRkpWS1hUdnFwMTVhckNsbkp6UDIiLCJzdWIiOiJ5QkNhdXVMRkZKVktYVHZxcDE1YXJDbG5KelAyIiwiaWF0IjoxNjE3MDI0Mzk2LCJleHAiOjE2MTcwMjc5OTYsImVtYWlsIjoicm9iZXJ0QGdtYWlsLmNvbSIsImVtYWlsX3ZlcmlmaWVkIjpmYWxzZSwiZmlyZWJhc2UiOnsiaWRlbnRpdGllcyI6eyJlbWFpbCI6WyJyb2JlcnRAZ21haWwuY29tIl19LCJzaWduX2luX3Byb3ZpZGVyIjoicGFzc3dvcmQifX0.aEqMmZIBXHYUJ7lInOn5x-rOXczqTe9PY-q593EBH_AeTx9iK1Vktor99NPQDlpneC84Jo0Jxe8fbNX3xsKWaImEPHnDZbBMY3VjM-cNCh8spVu7O5fnOxx4GAvGdrlCSwsw50DHAFncFuQ1iv6Ang4kH0qJQHU6arBpZmIZUzTPpSqjY6V0i6EiPEyelhVva7sesK_7L5VBopbhlOlyAQV0gSRist6IY0B18EespUVn59StXo0Fn2NEqxKre9a2nXfXV01QH9s2une8Xd-plJlyVypAj3ytz3Ko7x1aD44vj9B6vbk02W3V2SuqPzmvsaUtyBHjCUJ7glcj15uDAg";
        long expires = System.currentTimeMillis() + (60 * 60 * 1000);
        String displayName = "Robert";
        int loginType = 1;
        viewModel.insertOrUpdate(idToken, expires, displayName, loginType);
        checkPermissions();
    }
    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            /*PushNotificationDialog pushNotification = PushNotificationDialog.getInstance(
                    intent.getBundleExtra(EXTRA_BUNDLE_FCM));
            pushNotification.show(getSupportFragmentManager(), "Notify Dialog");*/
        }
    };

    @Override
    protected void onStart() {
        super.onStart();
        /*if (idTokenListener != null) {
            firebaseAuth.addIdTokenListener(idTokenListener);
        }*/
        LocalBroadcastManager.getInstance(this).registerReceiver((mMessageReceiver), new IntentFilter(FAN_OF_HOUR));
        /*FirebaseMessaging.getInstance().subscribeToTopic(FAN_OF_HOUR);
        FirebaseMessaging.getInstance().getToken().addOnSuccessListener(this, info -> {
            viewModel.saveFCMToken(info);
        });
        InAppButtonListener listener = new InAppButtonListener(viewModel);
        FirebaseInAppMessaging.getInstance().addClickListener(listener);
        FirebaseService.createChannelAndHandleNotifications(this);
*/
        // Other App notification service
        //featureDelegate.doStartNotificationService(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        /*if (idTokenListener != null) {
            firebaseAuth.removeIdTokenListener(idTokenListener);
        }*/
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);
        //FirebaseMessaging.getInstance().unsubscribeFromTopic(FAN_OF_HOUR);
    }

    public void restartNotificationService() {
        //featureDelegate.doStopNotificationService(this);
        //featureDelegate.doStartNotificationService(this);
    }

    private void displayVersion(@Nullable TextView menuVerText) {
        if (menuVerText != null) {
            menuVerText.setText(getString(R.string.version_info, BuildConfig.VERSION_NAME));
        }
    }

    private void checkPermissions() {
        if (getApplicationContext() == null) return;

        String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};
        boolean isEnabled = ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;

        if (!isEnabled) {
            ActivityCompat.requestPermissions(this, permissions, LOCATION_REQUEST_CODE);
        } else {
            fetchLastLocation();
        }
    }

    private void fetchLastLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        viewModel.getLastLocation();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (getApplicationContext() == null) return;
        int grantedCount = 0;
        if (requestCode == LOCATION_REQUEST_CODE) {
            if (grantResults.length > 0) {
                for (int grantResult : grantResults) {
                    if (grantResult == PackageManager.PERMISSION_GRANTED) {
                        grantedCount++;
                    }
                }
                if (grantedCount > 0) {
                    viewModel.getLastLocation();
                }
            }
        }
        if (requestCode == CAMERA_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (viewModel.isHRForFanFit.getValue() != null) {
                    showCamera(viewModel.isHRForFanFit.getValue());
                } else {
                    showCamera(false);
                }
            }
        }
        if (requestCode == 14002) {
            Log.d(TAG, "onRequestPermissionsResult:camera");
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                mReactInstanceManager.getCurrentReactContext().getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class).emit("onCameraPermission", true);
            } else {
                mReactInstanceManager.getCurrentReactContext().getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class).emit("onCameraPermission", false);
            }
        }
        if (requestCode == 14001) {
            Log.d(TAG, "onRequestPermissionsResult: gallery");
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                mReactInstanceManager.getCurrentReactContext().getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class).emit("onGalleryPermission", true);
            } else {
                mReactInstanceManager.getCurrentReactContext().getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class).emit("onGalleryPermission", false);
            }
        }
        if (requestCode == Constant.REQUEST_STORAGE_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openImageChooser();
            }
        }
        if (requestCode == REQUEST_PERMISSIONS_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                accessGoogleFit();
            }
        }
        if (requestCode == Constant.PHONE_STATE_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openNofityBandDialog(requestCode);
            }
        }
        if (requestCode == Constant.SMS_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openNofityBandDialog(requestCode);
            }
        }
    }

    public void showHeartRateOptions(boolean isFanFit) {
        Log.d(TAG, "showHeartRateOptions");
        /*BottomModalFragment bottomModalFragment = BottomModalFragment.newInstance(isFanFit);
        FragmentManager fm = getSupportFragmentManager();
        Fragment frag = fm.findFragmentByTag("Dialog");
        if (frag != null) {
            fm.popBackStack();
        }
        bottomModalFragment.show(getSupportFragmentManager(), "Dialog");*/
        viewModel.setTabPosition(-1);
    }

    public void showListDevice() {
        /*ListDeviceModal modalFragment = new ListDeviceModal();
        FragmentManager fm = getSupportFragmentManager();
        Fragment frag = fm.findFragmentByTag("List Device");
        if (frag != null) {
            fm.popBackStack();
        }
        modalFragment.show(getSupportFragmentManager(), "List Device");*/
        viewModel.setTabPosition(-1);
    }

    public void showCamera(boolean isFanFit) {
        HeartCameraInfoFragment cameraFragment = HeartCameraInfoFragment.newInstance(isFanFit);
        FragmentManager fm = getSupportFragmentManager();
        Fragment frag = fm.findFragmentByTag("Camera");
        if (frag != null) {
            fm.popBackStack();
        }
        cameraFragment.show(getSupportFragmentManager(), "Camera");
        viewModel.setTabPosition(-1);
    }

    public void showCameraPermission() {
        HeartRatePermissionFragment heartCameraInfoFragment = new HeartRatePermissionFragment();
        FragmentManager fm = getSupportFragmentManager();
        Fragment frag = fm.findFragmentByTag("Camera Perm");
        if (frag != null) {
            fm.popBackStack();
        }
        heartCameraInfoFragment.show(getSupportFragmentManager(), "Camera Perm");
        viewModel.setTabPosition(-1);
    }

    private void openNofityBandDialog(int requestCode) {
        /*NotifyFragment notifyFragment = NotifyFragment.newInstance(requestCode);
        FragmentManager fm = getSupportFragmentManager();
        Fragment frag = fm.findFragmentByTag("Notify");
        if (frag != null) {
            fm.popBackStack();
        }
        notifyFragment.show(fm, "Notify");*/
        viewModel.setTabPosition(-1);

    }

    /*public void showSessionDialog() {
        SessionProgressDialog progressDialog = new SessionProgressDialog();
        FragmentManager fm = getSupportFragmentManager();
        Fragment frag = fm.findFragmentByTag("SessionPopup");
        if (frag != null) {
            fm.popBackStack();
        }
        progressDialog.setCancelable(false);
        progressDialog.show(getSupportFragmentManager(), "SessionPopup");
        viewModel.setTabPosition(-1);
    }
*/
    public void openImageChooser() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(Intent.createChooser(intent, getString(R.string.choose_action)), Constant.IMAGE_REQUEST_CODE);
    }

    public void accessGoogleFit() {
        fanEngageViewModel.updateHRPref(HeartRate.GOOGLE_FIT);
        fanEngageViewModel.fitService.buildFitnessClient(this, Constant.REQUEST_PERMISSIONS_REQUEST_CODE);
        fanFitCommon.getService().buildFitnessClient(this, Constant.REQUEST_PERMISSIONS_REQUEST_CODE);
    }

    /**
     * @param message     string
     * @param messageType 1 is info | -1 is error | 0 alert
     */
    public void showMessagesStr(String message, int messageType) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(this, R.style.MessageDialog));
        View msgView = View.inflate(getApplicationContext(), R.layout.view_snackbar, null);
        TextView msgText = (TextView) msgView.findViewById(R.id.snackbar_message);
        msgText.setTextColor(ContextCompat.getColor(getApplicationContext(),
                R.color.Black));
        ImageView dialogIcon = (ImageView) msgView.findViewById(R.id.snackbar_icon);
        if (messageType == 1) {
            dialogIcon.setImageResource(R.drawable.ic_icon_info);
        } else if (messageType == -1) {
            dialogIcon.setImageResource(R.drawable.ic_icon_error);
        } else {
            dialogIcon.setImageResource(R.drawable.ic_icon_alert);
        }
        ImageView closeIcon = (ImageView) msgView.findViewById(R.id.snackbar_close);
        TextView descText = (TextView) msgView.findViewById(R.id.snackbar_desc);
        descText.setVisibility(View.GONE);
        if (msgText != null) msgText.setText(message);
        builder.setView(msgView);
        final AlertDialog msgDialog = builder.create();
        Window window = msgDialog.getWindow();
        if (window != null) {
            WindowManager.LayoutParams wlp = window.getAttributes();
            wlp.gravity = Gravity.TOP;
            wlp.windowAnimations = R.style.DialogAnimation;
            window.setAttributes(wlp);
            window.setBackgroundDrawableResource(R.drawable.bg_white_rounded);
        }
        if (closeIcon != null) closeIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (getApplicationContext() == null) return;
                msgDialog.dismiss();
            }
        });
        if (!isFinishing() && !isDestroyed()) msgDialog.show();
        new CountDownTimer(AUTO_DISMISS_MILLIS, COUNTDOWNINTERVAL) {

            @Override
            public void onTick(long l) {
            }

            @Override
            public void onFinish() {
                if (getApplicationContext() == null) return;
                try {
                    if (msgDialog != null && msgDialog.isShowing()) {
                        msgDialog.dismiss();
                    }
                } catch (Exception ignore) {

                }
            }
        }.start();
    }

    /**
     * @param messageId
     * @param messageType 1 is info | -1 is error | 0 alert
     */
    public void showMessages(@StringRes int messageId, int messageType) {
        showMessagesStr(getString(messageId), messageType);
    }

    public void showIoTMessages(@StringRes int messageId, @StringRes int actionMsgId,
                                @DrawableRes int iconId, @Nullable final MessageBoxListener listener) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(this, R.style.MessageDialog));
        View msgView = View.inflate(getApplicationContext(), R.layout.view_snackbar, null);
        TextView msgText = (TextView) msgView.findViewById(R.id.snackbar_message);
        msgText.setTextColor(ContextCompat.getColor(getApplicationContext(),
                R.color.Black));
        TextView descText = (TextView) msgView.findViewById(R.id.snackbar_desc);
        ImageView snackBarIcon = (ImageView) msgView.findViewById(R.id.snackbar_icon);
        ImageView closeIcon = (ImageView) msgView.findViewById(R.id.snackbar_close);
        snackBarIcon.setImageResource(iconId);
        if (msgText != null) msgText.setText(messageId);
        if (descText != null) {
            descText.setText(actionMsgId);
            if (listener != null) {
                descText.setTextColor(ContextCompat.getColor(getApplicationContext(),
                        R.color.colorPrimaryLight));
                descText.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        listener.onActionClick();
                    }
                });
            }
        }
        builder.setView(msgView);
        final AlertDialog msgDialog = builder.create();
        Window window = msgDialog.getWindow();
        if (window != null) {
            WindowManager.LayoutParams wlp = window.getAttributes();
            wlp.gravity = Gravity.TOP;
            wlp.windowAnimations = R.style.DialogAnimation;
            window.setAttributes(wlp);
            // android.R.color.transparent
            window.setBackgroundDrawableResource(R.drawable.bg_white_rounded);
        }
        if (closeIcon != null) closeIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (getApplicationContext() == null) return;
                msgDialog.dismiss();
            }
        });
        if (!isFinishing() && !isDestroyed()) msgDialog.show();
        new CountDownTimer(AUTO_DISMISS_MILLIS, COUNTDOWNINTERVAL) {

            @Override
            public void onTick(long l) {
            }

            @Override
            public void onFinish() {
                if (getApplicationContext() == null) return;
                msgDialog.dismiss();
            }
        }.start();
    }

    public WritableMap getMapForUri(Uri uri, WritableMap map) {
        map.putString("uri", uri.toString());

        String mimeType = getContentResolver().getType(uri);
        map.putString("mimeType", mimeType);

        Cursor returnCursor =
                getContentResolver().query(uri, null, null, null, null);
        /*
         * Get the column indexes of the data in the Cursor,
         * move to the first row in the Cursor, get the data,
         * and display it.
         */
        int pathIndex = returnCursor.getColumnIndex(MediaStore.Images.Media.DATA);
        int nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
        int sizeIndex = returnCursor.getColumnIndex(OpenableColumns.SIZE);
        returnCursor.moveToFirst();

        String fileName = returnCursor.getString(nameIndex);
        String fileSize = Long.toString(returnCursor.getLong(sizeIndex));
        String path = returnCursor.getString(pathIndex);

        map.putString("name", fileName);
        map.putString("size", fileSize);
        map.putString("path", path);

        return map;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

//        IMAGE FROM CAMERA
        if (requestCode == 13001) {
            Log.d(TAG, "onActivityResult: req" + requestCode);
            if (resultCode == Activity.RESULT_OK) {
                viewModel.cameraResult2.postValue(true);
            }
        }

//        IMAGE FROM GALLERY
        if (requestCode == 13002) {
            WritableMap map = Arguments.createMap();

            if (data == null || data.getData() == null|| data.getData().getPath() == null) {
                mReactInstanceManager.getCurrentReactContext()
                        .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                        .emit("onGallery", map);
                return;
            }

            Uri uri = data.getData();
            this.getMapForUri(uri, map);

            mReactInstanceManager.getCurrentReactContext().getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class).emit("onGallery", map);
        }

//        VIDEO FROM GALLERY
        if (requestCode == 13003) {
            WritableMap map = Arguments.createMap();

            if (data.getData() == null && data.getData().getPath() == null) {
                mReactInstanceManager.getCurrentReactContext()
                        .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                        .emit("onGalleryVideo", map);
                return;
            }

            Uri uri = data.getData();
            this.getMapForUri(uri, map);

            mReactInstanceManager.getCurrentReactContext().getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class).emit("onGalleryVideo", map);
        }

//        IMAGE FROM native android code
        if (requestCode == Constant.IMAGE_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                if (data == null) return;
                if (data.getData() != null && data.getData().getPath() != null) {
                    Log.d(TAG, "onActivityResult: post value");
                    viewModel.imageUri.postValue(data.getData());
                }
                //Log.d("Profile", "path ");
            }
        } else if (requestCode == Constant.MOMENTS_IMAGE_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {

                if (data == null) return;
                if (data.getData() != null && data.getData().getPath() != null) {
                    viewModel.momentsImageUri.postValue(data.getData());
                }
                //Log.d("Profile", "path ");
            }
        } else if (requestCode == 23) {
            if (resultCode == Activity.RESULT_OK) {
                Log.d(TAG, " data " + data.getData().toString());
            }
        } else if (requestCode == REQUEST_IMAGE_CAPTURE) {
            if (resultCode == Activity.RESULT_OK) {
                viewModel.cameraResult.postValue(true);
            }
        }
    }
    /*
    @Override
    public void onAuthFinished(AuthenticationResult result) {
        if(result.isSuccessful()) {
            Log.d(TAG, result.getAccessToken().getAccessToken());
        }
    } */

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        Log.d(TAG, "onNavigationItemSelected: " + item);
        /*drawerLayout.closeDrawer(Gravity.RIGHT);
        int itemId = item.getItemId();
        if (itemId == R.id.action_fan_band) {
            fanEngageViewModel.updateHRPref(HeartRate.DEVICE_BAND);
            showListDevice();
//            BottomModalFragment bottomModalFragment = new BottomModalFragment();
//            FragmentManager fm = getSupportFragmentManager();
//            bottomModalFragment.show(fm, "Dialog");
            return true;
        } else if (itemId == R.id.action_store) {
            String storeUrl = viewModel.storeUrl.getValue();
            String affStoreUrl = viewModel.affStoreLink.getValue();
            storeUrl = (affStoreUrl != null && !affStoreUrl.trim().isEmpty()) ? affStoreUrl.trim() :
                    (storeUrl != null && !storeUrl.trim().isEmpty()) ? storeUrl.trim() : STORE_URL;
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(storeUrl)));
            return true;
        } else if (itemId == R.id.action_logout) {
            FirebaseAuth.getInstance().signOut();
            return true;
        } else if (itemId == R.id.action_fan_heart) {
            fanEngageViewModel.updateHRPref(HeartRate.CAMERA);
            boolean isEnabled = CameraUtils.hasCameraPermission(this);
            if (isEnabled) {
                if (viewModel.isHRForFanFit.getValue() != null) {
                    showCamera(viewModel.isHRForFanFit.getValue());
                } else {
                    showCamera(false);
                }
            } else {
                showCameraPermission();
            }
            return true;
        } else if (itemId == R.id.action_fan_fit) {
            //fanEngageViewModel.updateHRPref(HeartRate.GOOGLE_FIT);
            if (fanEngageViewModel.fitService.checkPermissions(getApplicationContext())) {
                fanEngageViewModel.updateHRPref(HeartRate.GOOGLE_FIT);
                fanEngageViewModel.fitService.buildFitnessClient(this, REQUEST_PERMISSIONS_REQUEST_CODE);
            } else {
                showMessages(R.string.google_fit_perm, 0);
                fanEngageViewModel.fitService.requestPermissions(this, REQUEST_PERMISSIONS_REQUEST_CODE);
            }
            return true;
        } else if (itemId == R.id.action_pair_phone) {
            viewModel.pairPhone.setValue(true);
            viewModel.setGotoTabPosition(1, -1);
        } else if (itemId == R.id.action_sedentary_reminder) {
            featureDelegate.onSedentary(this);
            return true;
        } else if (itemId == R.id.action_search_band) {
            featureDelegate.onSearchBand();
            return true;
        } else if (itemId == R.id.action_alarm) {
            AlarmFragment alarmFragment = new AlarmFragment();
            FragmentManager fm = getSupportFragmentManager();
            Fragment frag = fm.findFragmentByTag("Alarm");
            if (frag != null) {
                fm.popBackStack();
            }
            alarmFragment.show(fm, "Alarm");
            viewModel.setTabPosition(-1);
            return true;
        } else if (itemId == R.id.action_notify) {
            openNofityBandDialog(-1);
            return true;
        } else if (itemId == R.id.action_display_setting) {
            DisplaySettingsFrag displaySettingsFrag = new DisplaySettingsFrag();
            FragmentManager fm = getSupportFragmentManager();
            Fragment frag = fm.findFragmentByTag("Display_Setting");
            if (frag != null) {
                fm.popBackStack();
            }
            displaySettingsFrag.show(fm, "Display_Setting");
            viewModel.setTabPosition(-1);
            return true;
        } else if (itemId == R.id.action_refer) {
            viewModel.inviteFriends(getApplicationContext());
            return true;
        } else if (itemId == R.id.gdpr_policy) {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.gdpr_link)));
            startActivity(browserIntent);
            return true;
        }*/
        return false;
    }

    /**
     * start or stop Service for SCD and Heart Rate Session
     *
     * @param caller caller can be FanEngage or FanFit
     * @param stop   stops if true stop the service otherwise starts / restarts service
     * @return true if success
     */
    public boolean startStopHr(FitnessService.Caller caller, boolean stop) {
        Log.d(TAG, "startStopHr called " + caller.name() + " isStop " + stop);
        Long value = fanFitCommon.getModeLive().getValue();
        if (value != null && value > 0 && !checkMode(value.intValue())) {
            return false;
        }
        if (value == null || value == 0 || value == -1) {
            fetchLastLocation();
            showHeartRateOptions(caller == FitnessService.Caller.FanFit);
            return false;
        } else {

            // Set the listener for caller

            int mode = value.intValue();
            if (mode == PHONE) {
                showCamera(caller == FitnessService.Caller.FanFit);
            }
            boolean isRunning = (serviceUtils.getServiceRunning().getValue() == null) ? false :
                    serviceUtils.getServiceRunning().getValue();
            if (isRunning && stop) {
                fetchLastLocation();
                serviceUtils.stopObserver(getApplicationContext(), fanFitCommon);
            }
            if (!stop) {
                fetchLastLocation();
                serviceUtils.observerDeviceType(
                        getApplicationContext(),
                        fanFitCommon, mode);
            }
            return true;
        }
    }

    public boolean checkMode(int type) {
        boolean success = false;
        switch (type) {
            case DEVICE_BAND: {
                if (!fanEngageViewModel.isBluetoothEnabled()) {
                    success = false;
                    showMessages(R.string.enable_bluetooth, 0);
                    break;
                }
                Device device = fanEngageViewModel.bandLive.getValue();
                if (device != null && device.getAddress() != null) {
                    serviceUtils.setBandAddress(device.getAddress());
                    if (fanEngageViewModel.isConnected()) {
                        // wait for device to connect
                        success = true;
                    } else {
                        showMessages(R.string.band_not_connected, 0);
                    }
                } else {
                    showListDevice();
                }
                break;
            }
            case GOOGLE_FIT: {
                if (fanFitCommon.getService().checkPermissions(this)) {
                    fanFitCommon.getService().buildFitnessClient(this, Constant.REQUEST_PERMISSIONS_REQUEST_CODE);
                    success = true;
                } else {
                    showMessages(R.string.google_fit_perm, 0);
                    fanFitCommon.getService().requestPermissions(this, Constant.REQUEST_PERMISSIONS_REQUEST_CODE);
                }
                break;
            }
            case PHONE: {
                boolean isEnabled = CameraUtils.hasCameraPermission(this);
                if (isEnabled) {
                    success = true;
                } else {
                    showCameraPermission();
                }
                break;
            }
            default: {
                success = false;
                break;
            }
        }
        return success;
    }

    public boolean startFeHeartRate(int type) {
        boolean success = false;
        switch (type) {
            case DEVICE_BAND:
            case GOOGLE_FIT: {
                showMessages(R.string.dedicate_hr, 1);
                viewModel.setGotoTabPosition(FANENGAGE_TAB_INDEX, 0);
                success = true;
                break;
            }
            case PHONE:
            default: {
                boolean isEnabled = CameraUtils.hasCameraPermission(this);
                if (isEnabled) {
                    showCamera(false);
                } else {
                    showCameraPermission();
                }
                fanEngageViewModel.updateHRPref(HeartRate.CAMERA);
                success = true;
                break;
            }
        }
        return success;
    }

    /*@Override
    public void showFeatureMessage(@StringRes int resId) {
        showMessages(resId, 1);
    }

    @Override
    public void showErrorMessage(@StringRes int resId) {
        showMessages(resId, -1);
    }*/

    @Override
    public void onPaymentSuccess(String razorpayPaymentID) {
        Log.d(TAG, "onPaymentSuccess " +  razorpayPaymentID);
        viewModel.onPaymentSuccess(razorpayPaymentID);
        // showMessages(R.string.payment_success, 1);
    }

    @Override
    public void onPaymentError(int code, String response) {
        Log.d(TAG, "onPaymentError " + code + " "  + response);
        showMessagesStr(viewModel.processResponse(response), -1);
        viewModel.onPaymentFailure(response);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_MENU && mReactInstanceManager != null
                && BuildConfig.DEBUG) {
            mReactInstanceManager.showDevOptionsDialog();
            return true;
        }
        return super.onKeyUp(keyCode, event);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mReactInstanceManager != null) {
            mReactInstanceManager.onHostPause();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mReactInstanceManager != null) {
            mReactInstanceManager.onHostResume(this, this);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mReactInstanceManager != null) {
            mReactInstanceManager.onHostDestroy(this);
        }
        serviceUtils.onDestroy(getApplicationContext());
//        if (mReactRootView != null) {
//            mReactRootView.unmountReactApplication();
//        }
    }

    @Override
    public void onBackPressed() {
        Intent startMain = new Intent(Intent.ACTION_MAIN);
        startMain.addCategory(Intent.CATEGORY_HOME);
        startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(startMain);
    }

    @Override
    public void invokeDefaultOnBackPressed() {
        Intent startMain = new Intent(Intent.ACTION_MAIN);
        startMain.addCategory(Intent.CATEGORY_HOME);
        startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(startMain);
    }

    /**
     * Returns the name of the main component registered from JavaScript. This is used to schedule
     * rendering of the component.
     */
    @Override
    protected String getMainComponentName() {
        return "DemoProject";
    }
}