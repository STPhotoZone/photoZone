package com.example.stphotozone;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.media.Image;
import android.media.MediaScannerConnection;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.PixelCopy;
import android.view.View;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.SpinnerAdapter;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.ar.core.Anchor;
import com.google.ar.core.Config;
import com.google.ar.core.Session;
import com.google.ar.core.exceptions.NotYetAvailableException;
import com.google.ar.core.exceptions.UnavailableApkTooOldException;
import com.google.ar.core.exceptions.UnavailableArcoreNotInstalledException;
import com.google.ar.core.exceptions.UnavailableDeviceNotCompatibleException;
import com.google.ar.core.exceptions.UnavailableSdkTooOldException;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.ArSceneView;
import com.google.ar.sceneform.rendering.Color;
import com.google.ar.sceneform.rendering.MaterialFactory;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.ux.ArFragment;
import com.google.ar.sceneform.ux.TransformableNode;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Objects;


public class MainActivity extends AppCompatActivity {
    private ArFragment arFragment;
    private int modelN = 0;
    private Session session;
    final int GET_IMAGE = 200;
    private ActivityResultLauncher<Intent> startActivityForResultLauncher;

    public static boolean checkSystemSupport(Activity activity) {

        // checking whether the API version of the running Android >= 24
        // that means Android Nougat 7.0
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            String openGlVersion = ((ActivityManager) Objects.requireNonNull(activity.getSystemService(Context.ACTIVITY_SERVICE))).getDeviceConfigurationInfo().getGlEsVersion();

            // checking whether the OpenGL version >= 3.0
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

        // 카메라 권한 확인
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED && checkSystemSupport(this)){
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }
        else{
            // Second -> Main
            startActivityForResultLauncher = registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == RESULT_OK) {
                            arFragment.destroySession(); // 초기화
                        }
                    });

            /* 버튼 처리 */
            ImageButton captureButton = findViewById(R.id.take_photo);
            ImageButton selectModel = findViewById(R.id.more);
            ImageButton gallery = findViewById(R.id.gallery);

            captureButton.setOnClickListener(v -> {
                capturePhoto();
            }); // 카메라 버튼

            selectModel.setOnClickListener( // 모델 선택
                    view -> {
                        showSelectMenu(view);
                    }
            );
            gallery.setOnClickListener(view -> {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
                startActivityForResult(intent,GET_IMAGE);
            });

            arFragment = (ArFragment) getSupportFragmentManager().findFragmentById(R.id.arArea);
            createSession();
            arFragment.setSession(session);
//        arFragment.getArSceneView().getScene().addOnUpdateListener(frameTime -> {
//            if (arFragment.getArSceneView().getSession() == null) {
//                return;
//            }
//        });

            arFragment.setOnTapArPlaneListener((hitResult, plane, motionEvent) -> {
                Anchor anchor = hitResult.createAnchor();

                if(modelN == 0){
                    Toast.makeText(MainActivity.this, "모델을 먼저 선택해주세요", Toast.LENGTH_SHORT).show();
                }
                else{
                    createModel(anchor, modelN);
                }

            });
        }


    }

    /* Session 정의 */
    public void createSession() {
        // Create a new ARCore session.
        try {
            session = new Session(this);
        } catch (UnavailableArcoreNotInstalledException e) {
            throw new RuntimeException(e);
        } catch (UnavailableApkTooOldException e) {
            throw new RuntimeException(e);
        } catch (UnavailableSdkTooOldException e) {
            throw new RuntimeException(e);
        } catch (UnavailableDeviceNotCompatibleException e) {
            throw new RuntimeException(e);
        }

        // Create a session config.
        Config config = new Config(session);

        // Do feature-specific operations here, such as enabling depth or turning on
        // support for Augmented Faces.
        config.setDepthMode(Config.DepthMode.RAW_DEPTH_ONLY);
        config.setFocusMode(Config.FocusMode.AUTO);
        config.setLightEstimationMode(Config.LightEstimationMode.ENVIRONMENTAL_HDR);

        // Configure the session.
        session.configure(config);
    }

    /* model rendering */
    private void createModel(Anchor anchor, int modelN){
        ModelRenderable.builder()
                .setSource(this, modelN)
                .setAsyncLoadEnabled(true)
                .setIsFilamentGltf(true)
                .build()
                .thenAccept(modelRenderable -> {
                    placeModel(anchor, modelRenderable);
                })
                .exceptionally(throwable -> {
                    Toast.makeText(
                            this, "Unable to load model", Toast.LENGTH_LONG).show();
                    return null;
                });
    }

    // 모델 앵커로 위치시키기
    private void placeModel(Anchor anchor, ModelRenderable modelRenderable){
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

        // Set the depth test shader on the model
        applyDepthTestShader(modelRenderable);

    }

    private void applyDepthTestShader(ModelRenderable renderable) {
        // Load the depth test shader
        MaterialFactory.makeOpaqueWithColor(this, new Color(android.graphics.Color.RED))
                .thenAccept(material -> {
                    renderable.setMaterial(material);
                    material.setFloat("depthThreshold", 0.1f);
                });
    }

    /* Camrea */
    private void capturePhoto() {
        final ArSceneView view = arFragment.getArSceneView();

        // Ensure ARCore is ready
        if (view.getArFrame() == null) {
            return;
        }

        final Bitmap bitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
        final HandlerThread handlerThread = new HandlerThread("PixelCopier");
        handlerThread.start();
        PixelCopy.request(view, bitmap, (copyResult) -> {
            if (copyResult == PixelCopy.SUCCESS) {
                if (bitmap != null) {
                    // 이미지를 내부 저장소에 저장
                    File file = new File(getFilesDir(), "shared_image.png");
                    FileOutputStream fos = null;
                    try {
                        fos = new FileOutputStream(file);
                        bitmap.compress(Bitmap.CompressFormat.PNG, 90, fos);
                        fos.close();

                        Intent sendPrev = new Intent(MainActivity.this, PreviewActivity.class);
                        // 파일 경로를 Intent에 추가하여 SecondActivity 시작
                        sendPrev.putExtra("sendImg", file.getAbsolutePath());
                        startActivityForResultLauncher.launch(sendPrev);

                    } catch (FileNotFoundException e) {
                        throw new RuntimeException(e);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                } else {
                    Toast.makeText(this, "No image", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Failed to copy pixels: " + copyResult, Toast.LENGTH_LONG).show();
            }
            handlerThread.quitSafely();
        }, new Handler(handlerThread.getLooper()));
    }

    /* side 메뉴 선택 시 */
    private void showSelectMenu(View view) {
        PopupMenu popupMenu = new PopupMenu(this, view);
        MenuInflater inflater = popupMenu.getMenuInflater();
        inflater.inflate(R.menu.select_model, popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.item1:
                        Toast.makeText(MainActivity.this, "테크가 선택되었어요", Toast.LENGTH_SHORT).show();
                        modelN = R.raw.tech;
                        return true;
                    case R.id.item2:
                        Toast.makeText(MainActivity.this, "아휴가 선택되었어요", Toast.LENGTH_SHORT).show();
                        modelN = R.raw.ahyu;
                        return true;
                    default:
                        return false;
                }
            }
        });
        popupMenu.show();
    }

    @Override
    protected void onDestroy() { // 앱 종료 시 세션도 같이 종료 -> 앱의 네이티브 메모리
        super.onDestroy();
        session.close();
    }

}
