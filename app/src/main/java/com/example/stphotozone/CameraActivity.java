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

    // cloud 앵커를 위함
    FirebaseManager firebaseManager = new FirebaseManager(); // firebase에 클라우드 앵커 저장하기

    FirebaseStorage storage;
    StorageReference model1, model2, model3;
    ArrayList<File> file = new ArrayList<File>(); // 파일 위치 저장

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

        // 지도에서 위치 정보 받기
        Intent intent = getIntent();
        checkPlace = intent.getStringExtra("place");

        // 카메라 촬영을 위한..
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);

            arFragment = (ArFragment) getSupportFragmentManager().findFragmentById(R.id.arArea);
        }

        //  ar코어를 사용하기 위한 hardware check
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

        // Firebase를 위함
        FirebaseApp.initializeApp(this);
        storage = FirebaseStorage.getInstance();

        // 모델 관련
        // 모델 파일 불러옥
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

            // 종료 되면 삭제
            file.get(0).deleteOnExit();
            file.get(1).deleteOnExit();
            file.get(2).deleteOnExit();
        } catch (
                IOException e) {
            e.printStackTrace();
        }

        /*초기화*/
        take_photo = (ImageButton) findViewById(R.id.take_photo);
        gallery = (ImageButton) findViewById(R.id.gallery);
        map = (ImageButton) findViewById(R.id.map);
        challenge = (ImageButton) findViewById(R.id.challenge);
        more = (ImageButton) findViewById(R.id.more);
        setting = (ImageButton) findViewById(R.id.setting);
        change = (ImageButton) findViewById(R.id.change);

        /* 버튼 클릭 처리 부분*/
        map.setOnClickListener(new View.OnClickListener() { // 지도 액티비티
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), MapActivity.class);
                startActivity(intent);
            }
        });
        challenge.setOnClickListener(new View.OnClickListener() { // 도전과제 액티비티
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), LoginActivity.class); // 먼저 로그인이 되어야 함
                startActivity(intent);
            }
        });

        more.setOnClickListener(new View.OnClickListener() { // 더보기 액티비티
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), CloudActivity.class); // 클라우드 앵커 만들기
                startActivity(intent);
            }
        });

        gallery.setOnClickListener(new View.OnClickListener() { // 갤러리 액티비티
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_PICK); // 갤러리에 접근하도록 선택하게 설정
                intent.setType(android.provider.MediaStore.Images.Media.CONTENT_TYPE);
                intent.setType("video/*"); // 이미지 저장하는 애들만 열도록!!!
                startActivity(intent);
//                Intent intent = new Intent();
//                intent.setType("image/*");
//                intent.setAction(Intent.ACTION_GET_CONTENT);
//                startActivity(intent);
            }
        });

        // 임시 코드
        setting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (cloudAnchor != null) { // cloud 앵커가 그대로 있으면 지우라고 요구
                    Toast.makeText(getApplicationContext(), "Please clear the anchor", Toast.LENGTH_SHORT).show();
                    return;
                }
                j = 10;

                // 짧은 코드로 reolve할 모델 찾기!!
                dialog = new ResolveDialogFragment();
                dialog.setOkListener(new ResolveDialogFragment.OkListener() {
                    @Override
                    public void onOkPressed(String dialogValue) {
                        int shortCode = Integer.parseInt(dialogValue); // 입력 받은 shortCode

                        firebaseManager.getCloudAnchorId(shortCode, cloudAnchorId -> { // firebase에서 찾아보세용~
                            if(cloudAnchorId == null || cloudAnchorId.isEmpty()){
                                Toast.makeText(getApplicationContext(), "A Cloud Anchor ID for the short code " + shortCode + " was not found.", Toast.LENGTH_SHORT).show(); // 찾을 수 없다!!
                                return;
                            }

                            cloudAnchor = arFragment.getArSceneView().getSession().resolveCloudAnchor(cloudAnchorId); // 해당 위치로 Anchor 가져오기

                        }, model -> {
                            createModel(cloudAnchor, model); // 모델 만들어!
                        });

                        Toast.makeText(getApplicationContext(), "Now resolving anchor...", Toast.LENGTH_SHORT).show();
                    }
                });
                dialog.show(getSupportFragmentManager(), "Resolve");
            }
        });
        // 모델 제거
        change.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cloudAnchor.detach();
                cloudAnchor = null;
                j = 0;
            }
        });


        // 사진 촬영
        take_photo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                    // 모델 불러오기
                    // 짧은 코드로 reolve할 모델 찾기!!
                    shortCode = selectModel(checkPlace);
                    if(j != 10){
                        firebaseManager.getCloudAnchorId(shortCode, cloudAnchorId -> { // firebase에서 찾아보세용~
                            if(cloudAnchorId == null || cloudAnchorId.isEmpty()){
                                Toast.makeText(getApplicationContext(), "A Cloud Anchor ID for the short code" + " was not found.", Toast.LENGTH_SHORT).show(); // 찾을 수 없다!!
                                return;
                            }

                            cloudAnchor = arFragment.getArSceneView().getSession().resolveCloudAnchor(cloudAnchorId); // 해당 위치로 Anchor 가져오기

                            }, model -> {
                            createModel(cloudAnchor, model); // 모델 만들어!
                            Toast.makeText(getApplicationContext(), "Now resolving anchor...", Toast.LENGTH_SHORT).show();
                        });
                }

                // 영상 test
                if(videoRecorder == null){
                    videoRecorder = new VideoRecorder();
                    videoRecorder.setSceneView(arFragment.getArSceneView());

                    int orientation = getResources().getConfiguration().orientation;

                    videoRecorder.setVideoQuality(CamcorderProfile.QUALITY_HIGH, orientation);
                }

                boolean isRecording  = videoRecorder.onToggleRecord(); // 녹화 시작

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
        // ArFragment에 각종 설정 해주기
        if (fragment.getId() == R.id.arArea) {
            arFragment = (ArFragment) fragment;
            arFragment.setOnSessionConfigurationListener(this); // 여기서 set!!
            //arCam.setOnTapArPlaneListener(this);
        }

    }

    @Override
    public void onSessionConfiguration(Session session, Config config) { // AR코어 세션에 설정
        if(session.isDepthModeSupported(Config.DepthMode.AUTOMATIC)){ // Depth 모드 지원되니?!
            config.setDepthMode(Config.DepthMode.AUTOMATIC);
        }
        config.setCloudAnchorMode(Config.CloudAnchorMode.ENABLED);
    }

    // 모델 렌더링
    private void createModel(Anchor anchor, int model) {
        ModelRenderable.builder()
                .setSource(this, Uri.parse(file.get(model).getPath()))
                .setIsFilamentGltf(true)
                .setAsyncLoadEnabled(true)
                .build()
                .thenAccept(modelRenderable -> {
                    placeModel(anchor, modelRenderable);
                    checkModel = model; // 모델 확인
                    Log.d("checkModel", checkModel+""+model);
                    update(); // 도전과제 정보 업데이트
                })
                .exceptionally(throwable -> {
                    Toast.makeText(
                            this, "Unable to load model", Toast.LENGTH_LONG).show();
                    checkModel = -1;
                    return null;
                });
    }

    // 모델 앵커로 위치시키기
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

    // 장소에 따른 모델 선택
    public int selectModel(String place){
        if(place == "미래관") return 159;
        if(place == "다산관") return 153;
        if(place == "붕어방") return 152;
        return 158; // 아휴
    }

    // 모델 확인
    public int getCheckModel(){
        return checkModel;
    }

    // 도전과제 정보 업데이트
    public void update(){

    }

}
