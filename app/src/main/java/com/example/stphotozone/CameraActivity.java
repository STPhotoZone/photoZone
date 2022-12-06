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
import android.widget.ImageButton;
import android.widget.Toast;

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

import java.lang.ref.WeakReference;
import java.util.Objects;

public class CameraActivity extends AppCompatActivity implements
        FragmentOnAttachListener,
        BaseArFragment.OnTapArPlaneListener,
        BaseArFragment.OnSessionConfigurationListener {

    // IconButton
    ImageButton gallery, map, challenge, more;

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

        /*초기화*/
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
                Intent intent = new Intent(getApplicationContext(), ChallengeActivity.class);
                startActivity(intent);
            }
        });

    }

    @Override
    public void onAttachFragment(@NonNull FragmentManager fragmentManager, @NonNull Fragment fragment) {
        // ArFragment에 각종 설정 해주기
        if(fragment.getId() == R.id.arArea){
            arCam = (ArFragment) fragment;
            arCam.setOnSessionConfigurationListener(this);
            arCam.setOnTapArPlaneListener(this);
        }

    }

    @Override
    public void onSessionConfiguration(Session session, Config config) {
        if(session.isDepthModeSupported(Config.DepthMode.AUTOMATIC)){ // Depth 모드 지원되니?!
            config.setDepthMode(Config.DepthMode.AUTOMATIC);
        }
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

    @Override
    public void onTapPlane(HitResult hitResult, Plane plane, MotionEvent motionEvent) {
        if (model == null) {
            Toast.makeText(this, "Loading...", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create the Anchor.
        Anchor anchor = hitResult.createAnchor();
        AnchorNode anchorNode = new AnchorNode(anchor);
        anchorNode.setParent(arCam.getArSceneView().getScene());

        // Create the transformable model and add it to the anchor.
        TransformableNode model = new TransformableNode(arCam.getTransformationSystem());
        model.setParent(anchorNode);
        model.setRenderable(this.model)
                .animate(true).start();
        model.select();
    }
}