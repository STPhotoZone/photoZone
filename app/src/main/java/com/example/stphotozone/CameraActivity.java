package com.example.stphotozone;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentOnAttachListener;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import com.example.helper.FirebaseManager;
import com.example.helper.ResolveDialogFragment;
import com.google.ar.core.Anchor;
import com.google.ar.core.Config;
import com.google.ar.core.HitResult;
import com.google.ar.core.Plane;
import com.google.ar.core.Session;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.ArSceneView;
import com.google.ar.sceneform.Node;
import com.google.ar.sceneform.Sceneform;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.rendering.Renderable;
import com.google.ar.sceneform.rendering.ViewRenderable;
import com.google.ar.sceneform.ux.ArFragment;
import com.google.ar.sceneform.ux.BaseArFragment;
import com.google.ar.sceneform.ux.TransformableNode;
import com.google.firebase.FirebaseApp;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.lang.ref.WeakReference;
import java.util.Objects;

public class CameraActivity extends AppCompatActivity implements
        FragmentOnAttachListener,
        BaseArFragment.OnSessionConfigurationListener {
    // cloud 앵커를 위함
    ResolveDialogFragment dialog;
    FirebaseManager firebaseManager = new FirebaseManager();

    // IconButton
    ImageButton gallery, map, challenge, more, take_photo;

    // object of ArFragment Class
    private ArFragment arCam;
    private Renderable model;

    private int clickNo = 0;

    // check the phone' hardware
    // checking whether the API version of the running Android >=24
    public static boolean checkCameraSystem(Activity activity){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){
            String openGlVersion = ((ActivityManager) Objects.requireNonNull(activity.getSystemService(Context.ACTIVITY_SERVICE))).getDeviceConfigurationInfo().getGlEsVersion();

            // Checking whether the OpenGL version >= 3.0
            if(Double.parseDouble(openGlVersion) >= 3.0){
                return true;
            }
            else{
                Toast.makeText(activity, "App needs OpenGl Version 3.0 or later", Toast.LENGTH_SHORT).show();
                activity.finish();
                return false;
            }
        }else {
            Toast.makeText(activity, "App does not support required Build Version", Toast.LENGTH_SHORT).show();
            activity.finish();
            return false;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        // Firebase를 위함
        FirebaseApp.initializeApp(this);
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference modelRef = storage.getReference();

        /*초기화*/
        take_photo = (ImageButton) findViewById(R.id.take_photo);
        gallery = (ImageButton) findViewById(R.id.gallery);
        map = (ImageButton) findViewById(R.id.map);
        challenge = (ImageButton) findViewById(R.id.challenge);
        more = (ImageButton) findViewById(R.id.more);

        // hardware check
        if(checkCameraSystem(this)){

            getSupportFragmentManager().addFragmentOnAttachListener(this);
            //ArFragment is linked up with its respective id used in the activity_main.xml

            if (savedInstanceState == null) {
                if (Sceneform.isSupported(this)) {
                    getSupportFragmentManager().beginTransaction()
                            .add(R.id.arArea, ArFragment.class, null)
                            .commit();
                }
            }

            loadModels();

        } else { // checkCameraSystem false
            return;
        }

        /* 버튼 클릭 처리 부분*/
        map.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), MapActivity.class);
                startActivity(intent);
            }
        });
        challenge.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), LoginActivity.class); // 먼저 로그인이 되어야 함
                startActivity(intent);
            }
        });

        take_photo.setOnClickListener(view -> {
            // 짧은 코드로 reolve할 모델 찾기!!
            dialog = new ResolveDialogFragment();
            dialog.setOkListener(new ResolveDialogFragment.OkListener() {
                @Override
                public void onOkPressed(String dialogValue) {
                    int shortCode = Integer.parseInt(dialogValue); // 입력 받은 shortCode
                    firebaseManager.getCloudAnchorId(shortCode, cloudAnchorId -> { // firebase에서 찾어
                        if(cloudAnchorId == null || cloudAnchorId.isEmpty()){
                            Toast.makeText(getApplicationContext(), "A Cloud Anchor ID for the short code " + shortCode + " was not found.", Toast.LENGTH_SHORT).show(); // 찾을 수 없다!!
                            return;
                        }

                        Anchor c = arCam.getArSceneView().getSession().resolveCloudAnchor(cloudAnchorId); // 해당 위치로 Anchor 가져오기
                    });

                    //loadModels(c); // 모델 만들어!

                    Toast.makeText(getApplicationContext(), "Now resolving anchor...", Toast.LENGTH_SHORT).show();
                }
            });
            dialog.show(getSupportFragmentManager(), "Resolve");
        });
    }

    @Override
    public void onAttachFragment(@NonNull FragmentManager fragmentManager, @NonNull Fragment fragment) {
        // ArFragment에 각종 설정 해주기
        if(fragment.getId() == R.id.arArea){
            arCam = (ArFragment) fragment;
            arCam.setOnSessionConfigurationListener(this);
            //arCam.setOnTapArPlaneListener(this);
        }

    }

    @Override
    public void onSessionConfiguration(Session session, Config config) {
        if(session.isDepthModeSupported(Config.DepthMode.AUTOMATIC)){ // Depth 모드 지원되니?!
            config.setDepthMode(Config.DepthMode.AUTOMATIC);
        }
        config.setCloudAnchorMode(Config.CloudAnchorMode.ENABLED);
    }

    // 모델 불러오기
    public void loadModels() {
        WeakReference<CameraActivity> weakActivity = new WeakReference<CameraActivity>(this);
        ModelRenderable.builder()
                .setSource(this, R.raw.ahyu)
                .setIsFilamentGltf(true)
                .setAsyncLoadEnabled(true)
                .build()
                .thenAccept(model -> {
                    CameraActivity activity = weakActivity.get();
                    if (activity != null) {
                        activity.model = model;
                    }
                })
                .exceptionally(throwable -> {
                    Toast.makeText(
                            this, "Unable to load model", Toast.LENGTH_LONG).show();
                    return null;
                });
    }

//    @Override
//    public void onTapPlane(HitResult hitResult, Plane plane, MotionEvent motionEvent) {
//        if (model == null) {
//            Toast.makeText(this, "Loading...", Toast.LENGTH_SHORT).show();
//            return;
//        }
//
//        // Create the Anchor.
//        Anchor anchor = hitResult.createAnchor();
//        AnchorNode anchorNode = new AnchorNode(anchor);
//        anchorNode.setParent(arCam.getArSceneView().getScene());
//
//        // Create the transformable model and add it to the anchor.
//        TransformableNode model = new TransformableNode(arCam.getTransformationSystem());
//        model.setParent(anchorNode);
//        model.setRenderable(this.model)
//                .animate(true).start();
//        model.select();
//    }

    // 모델 앵커로 위치시키기
    private void placeModel(Anchor anchor, ModelRenderable modelRenderable){
        // Create the Anchor.
        AnchorNode anchorNode = new AnchorNode(anchor);
        anchorNode.setParent(arCam.getArSceneView().getScene());

        // Create the transformable model and add it to the anchor.
        TransformableNode model = new TransformableNode(arCam.getTransformationSystem());
        model.setParent(anchorNode);
        model.setRenderable(modelRenderable)
                .animate(true).start();
        arCam.getArSceneView().getScene().addChild(anchorNode);
        model.select();

    }
}