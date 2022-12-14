package com.example.stphotozone;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentOnAttachListener;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraDevice;
import android.media.CamcorderProfile;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.helper.FirebaseManager;
import com.example.helper.ResolveDialogFragment;
import com.example.helper.VideoRecorder;
import com.google.ar.core.Anchor;
import com.google.ar.core.Config;
import com.google.ar.core.Session;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.Camera;
import com.google.ar.sceneform.Sceneform;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.ux.ArFragment;
import com.google.ar.sceneform.ux.BaseArFragment;
import com.google.ar.sceneform.ux.TransformableNode;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.firebase.FirebaseApp;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Objects;

public class CameraActivity extends AppCompatActivity implements
        FragmentOnAttachListener,
        BaseArFragment.OnSessionConfigurationListener{

    // Video Recorder
    private VideoRecorder videoRecorder;
    public Camera c;

    // cloud ????????? ??????
    FirebaseManager firebaseManager = new FirebaseManager(); // firebase??? ???????????? ?????? ????????????

    FirebaseStorage storage;
    StorageReference model1, model2, model3;
    ArrayList<File> file = new ArrayList<File>(); // ?????? ?????? ??????

    // IconButton
    ImageButton gallery, map, challenge, more, take_photo, setting, change;

    // object of ArFragment Class
    private ArFragment arFragment;
    Anchor cloudAnchor;

    // check model
    public int checkModel;

    // check place
    public String checkPlace;
    int i = 0;
    int j = 0;
    int k = 0;
    int shortCode;
    ResolveDialogFragment dialog;

    // check the phone' hardware
    // checking whether the API version of the running Android >=24
    public static boolean checkCameraSystem(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            String openGlVersion = ((ActivityManager) Objects.requireNonNull(activity.getSystemService(Context.ACTIVITY_SERVICE))).getDeviceConfigurationInfo().getGlEsVersion();
            // Checking whether the OpenGL version >= 3.0
            if (Double.parseDouble(openGlVersion) >= 3.0) {
                return true;
            } else {
                Toast.makeText(activity, "App needs OpenGl Version 3.0 or later", Toast.LENGTH_SHORT).show();
                activity.finish();
                return false;
            }
        } else {
            Toast.makeText(activity, "App does not support required Build Version", Toast.LENGTH_SHORT).show();
            activity.finish();
            return false;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        // ???????????? ?????? ?????? ??????
        Intent intent = getIntent();
        checkPlace = intent.getStringExtra("place");
       // Log.d("c", checkPlace);


        // ????????? ????????? ??????..
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);

            arFragment = (ArFragment) getSupportFragmentManager().findFragmentById(R.id.arArea);
        }

        //  ar????????? ???????????? ?????? hardware check
        if (checkCameraSystem(this)) {

            getSupportFragmentManager().addFragmentOnAttachListener(this);
            //ArFragment is linked up with its respective id used in the activity_main.xml

            if (savedInstanceState == null) {
                if (Sceneform.isSupported(this)) {
                    getSupportFragmentManager().beginTransaction()
                            .add(R.id.arArea, ArFragment.class, null)
                            .commit();
                }
            }

        } else { // checkCameraSystem false
            return;
        }

        // Firebase??? ??????
        FirebaseApp.initializeApp(this);
        storage = FirebaseStorage.getInstance();

        // ?????? ??????
        // ?????? ?????? ?????????
        model1 = storage.getReference().child("ahyu.glb");
        model2 = storage.getReference().child("tech.glb");
        model3 = storage.getReference().child("fly.glb");

        try {
            file.add(File.createTempFile("ahyu", "glb"));
            file.add(File.createTempFile("tech", "glb"));
            file.add(File.createTempFile("fly", "glb"));

            model1.getFile(file.get(0));
            model2.getFile(file.get(1));
            model3.getFile(file.get(2));

            // ?????? ?????? ??????
            file.get(0).deleteOnExit();
            file.get(1).deleteOnExit();
            file.get(2).deleteOnExit();
        } catch (
                IOException e) {
            e.printStackTrace();
        }

        /*?????????*/
        take_photo = (ImageButton) findViewById(R.id.take_photo);
        gallery = (ImageButton) findViewById(R.id.gallery);
        map = (ImageButton) findViewById(R.id.map);
        challenge = (ImageButton) findViewById(R.id.challenge);
        more = (ImageButton) findViewById(R.id.more);
        setting = (ImageButton) findViewById(R.id.setting);
        change = (ImageButton) findViewById(R.id.change);

        /* ?????? ?????? ?????? ??????*/
        map.setOnClickListener(new View.OnClickListener() { // ?????? ????????????
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), MapActivity.class);
                startActivity(intent);
            }
        });
        challenge.setOnClickListener(new View.OnClickListener() { // ???????????? ????????????
            @Override
            public void onClick(View view) {
                    Intent intent = new Intent(getApplicationContext(), LoginActivity.class); // ?????? ???????????? ????????? ???
                    startActivity(intent);
            }
        });

        more.setOnClickListener(new View.OnClickListener() { // ????????? ????????????
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), CloudActivity.class); // ???????????? ?????? ?????????
                startActivity(intent);
            }
        });

        gallery.setOnClickListener(new View.OnClickListener() { // ????????? ????????????
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_PICK); // ???????????? ??????????????? ???????????? ??????
                intent.setType(android.provider.MediaStore.Images.Media.CONTENT_TYPE);
                intent.setType("video/*"); // ????????? ???????????? ????????? ?????????!!!
                startActivity(intent);
//                Intent intent = new Intent();
//                intent.setType("image/*");
//                intent.setAction(Intent.ACTION_GET_CONTENT);
//                startActivity(intent);
            }
        });

        // ?????? ??????
        setting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (cloudAnchor != null) { // cloud ????????? ????????? ????????? ???????????? ??????
                    Toast.makeText(getApplicationContext(), "Please clear the anchor", Toast.LENGTH_SHORT).show();
                    return;
                }

                // ?????? ????????? reolve??? ?????? ??????!!
                dialog = new ResolveDialogFragment();
                dialog.setOkListener(new ResolveDialogFragment.OkListener() {
                    @Override
                    public void onOkPressed(String dialogValue) {
                        int shortCode = Integer.parseInt(dialogValue); // ?????? ?????? shortCode

                        firebaseManager.getCloudAnchorId(shortCode, cloudAnchorId -> { // firebase?????? ???????????????~
                            if(cloudAnchorId == null || cloudAnchorId.isEmpty()){
                                Toast.makeText(getApplicationContext(), "A Cloud Anchor ID for the short code " + shortCode + " was not found.", Toast.LENGTH_SHORT).show(); // ?????? ??? ??????!!
                                return;
                            }

                            cloudAnchor = arFragment.getArSceneView().getSession().resolveCloudAnchor(cloudAnchorId); // ?????? ????????? Anchor ????????????

                        }, model -> {
                            createModel(cloudAnchor, model); // ?????? ?????????!
                        });

                        Toast.makeText(getApplicationContext(), "Now resolving anchor...", Toast.LENGTH_SHORT).show();
                    }
                });
                dialog.show(getSupportFragmentManager(), "Resolve");
            }
        });
        // ?????? ??????
        change.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cloudAnchor.detach();
                cloudAnchor = null;
                j = 0;
            }
        });


        // ?????? ??????
        take_photo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                    // ?????? ????????????
                    // ?????? ????????? reolve??? ?????? ??????!!
                    shortCode = selectModel(checkPlace);
                    Log.d("check place", checkPlace+"");
                        firebaseManager.getCloudAnchorId(shortCode, cloudAnchorId -> { // firebase?????? ???????????????~
                            if(cloudAnchorId == null || cloudAnchorId.isEmpty()){
                                Toast.makeText(getApplicationContext(), "A Cloud Anchor ID for the short code" + " was not found.", Toast.LENGTH_SHORT).show(); // ?????? ??? ??????!!
                                return;
                            }

                            cloudAnchor = arFragment.getArSceneView().getSession().resolveCloudAnchor(cloudAnchorId); // ?????? ????????? Anchor ????????????

                            }, model -> {
                            createModel(cloudAnchor, model); // ?????? ?????????!
                            Toast.makeText(getApplicationContext(), "Now resolving anchor...", Toast.LENGTH_SHORT).show();
                            Log.d("resolve", model+"");
                        });
                // ?????? test
                if(videoRecorder == null){
                    videoRecorder = new VideoRecorder();
                    videoRecorder.setSceneView(arFragment.getArSceneView());

                    int orientation = getResources().getConfiguration().orientation;

                    videoRecorder.setVideoQuality(CamcorderProfile.QUALITY_HIGH, orientation);
                }

                boolean isRecording  = videoRecorder.onToggleRecord(); // ?????? ??????

                if(isRecording)
                    Toast.makeText(getApplicationContext(), "Start Recording", Toast.LENGTH_SHORT).show();
                else{
                    Toast.makeText(getApplicationContext(), "Recording stopped", Toast.LENGTH_SHORT).show();
                }

            }

        });

    }


    @Override
    public void onAttachFragment(@NonNull FragmentManager fragmentManager, @NonNull Fragment fragment) {
        // ArFragment??? ?????? ?????? ?????????
        if (fragment.getId() == R.id.arArea) {
            arFragment = (ArFragment) fragment;
            arFragment.setOnSessionConfigurationListener(this); // ????????? set!!
            //arCam.setOnTapArPlaneListener(this);
        }

    }

    @Override
    public void onSessionConfiguration(Session session, Config config) { // AR?????? ????????? ??????
        if(session.isDepthModeSupported(Config.DepthMode.AUTOMATIC)){ // Depth ?????? ?????????????!
            config.setDepthMode(Config.DepthMode.AUTOMATIC);
        }
        config.setCloudAnchorMode(Config.CloudAnchorMode.ENABLED);
    }

    // ?????? ?????????
    private void createModel(Anchor anchor, int model) {
        Log.d("call create", "uccess");
        ModelRenderable.builder()
                .setSource(this, Uri.parse(file.get(model).getPath()))
                .setIsFilamentGltf(true)
                .setAsyncLoadEnabled(true)
                .build()
                .thenAccept(modelRenderable -> {
                    placeModel(anchor, modelRenderable);
                    checkModel = model; // ?????? ??????
                    Log.d("create Model", model+""+checkModel);
                    update(checkModel); // ???????????? ?????? ????????????
                })
                .exceptionally(throwable -> {
                    return null;
                });
    }

    // ?????? ????????? ???????????????
    private void placeModel(Anchor anchor, ModelRenderable modelRenderable) {
        // Create the Anchor.
        AnchorNode anchorNode = new AnchorNode(anchor);
        anchorNode.setParent(arFragment.getArSceneView().getScene());

        // Create the transformable model and add it to the anchor.
        TransformableNode model = new TransformableNode(arFragment.getTransformationSystem());
        model.setParent(anchorNode);
        model.setRenderable(modelRenderable)
                .animate(true).start();
        arFragment.getArSceneView().getScene().addChild(anchorNode);
        model.select();
    }

    // ????????? ?????? ?????? ??????
    public int selectModel(String place){
        if(place == "?????????") return 159;
        if(place == "?????????") return 153;
        if(place == "?????????") return 163;
        return 163; // ??????
    }

    // ?????? ??????
    public int getCheckModel(){
        return checkModel;
    }

    // ???????????? ?????? ????????????
    public void update(int _checkModel){
        // ????????? ????????????
        ((ChallengeActivity)ChallengeActivity.context).addMissionCount(_checkModel);
        Log.d("uypate", checkModel+"");
        // ?????? ?????????
        // ?????? ??????
    }

}
