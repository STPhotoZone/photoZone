package com.example.stphotozone;

import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.helper.FirebaseManager;
import com.example.helper.ResolveDialogFragment;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.ar.core.Anchor;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.rendering.Renderable;
import com.google.ar.sceneform.ux.TransformableNode;
import com.google.firebase.FirebaseApp;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;


public class CloudActivity extends AppCompatActivity {

    CloudAnchorFragment arFragment;
    Anchor cloudAnchor;
    AppAnchorState appAnchorState;
    Button btn_clear, btn_resolve, btn_ahyu, btn_tech, btn_fly;

    FirebaseManager firebaseManager = new FirebaseManager(); // firebase에 클라우드 앵커 저장하기
    ResolveDialogFragment dialog;

    FirebaseStorage storage;
    StorageReference modelRef;
    File file;

    boolean isPlaced = false;

    public enum AppAnchorState {
        NONE,
        HOSTING,
        HOSTED,
        RESOLVING,
        RESOLVED
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cloud);

        // Firebase를 위함
        FirebaseApp.initializeApp(this);
        storage = FirebaseStorage.getInstance();

        arFragment = (CloudAnchorFragment) getSupportFragmentManager().findFragmentById(R.id.ar_fragment);


        // 터치 했을 때 anchor 호스팅하기!!
        arFragment.setOnTapArPlaneListener((hitResult, plane, motionEvent) -> {
            if(!isPlaced){
                cloudAnchor = arFragment.getArSceneView().getSession().hostCloudAnchor(hitResult.createAnchor()); // 클라우드 앵커 hosting하기!!
                appAnchorState = AppAnchorState.HOSTING;
                Toast.makeText(this, "Hosting...", Toast.LENGTH_SHORT).show();

                createModel(cloudAnchor); // 모델 보여주기

                isPlaced = true; // 대체 되었다!! 한 번만 작동!!
            }

        });


        // 업데이트 될 때마다 실행!!
        arFragment.getArSceneView().getScene().addOnUpdateListener(frameTime -> {
            if(appAnchorState != AppAnchorState.HOSTING) return;

            Anchor.CloudAnchorState cloudAnchorState = cloudAnchor.getCloudAnchorState(); // cloud 앵커 상태 조사

            if (appAnchorState == AppAnchorState.HOSTING) { // HOSTING 되었으면
                if (cloudAnchorState.isError()) { // 근데 에러 뜨면
                    Toast.makeText(getApplicationContext(), "Error hosting anchor...", Toast.LENGTH_SHORT).show();
                    appAnchorState = AppAnchorState.NONE;
                } else if (cloudAnchorState == Anchor.CloudAnchorState.SUCCESS) { // 성공이면
                    String cloudAnchorId = cloudAnchor.getCloudAnchorId(); // id 얻고
                    Toast.makeText(getApplicationContext(), cloudAnchorId , Toast.LENGTH_SHORT).show();

                    // firebase에 추가!
                    firebaseManager.nextShortCode(shortCode -> {
                        if(shortCode != null){
                            firebaseManager.storeUsingShortCode(shortCode, cloudAnchorId); // 클라우드 앵커 저장 근데 shortCode를 곁들인
                            Toast.makeText(this, "Cloud Anchor Hosted. Short code: "+ shortCode, Toast.LENGTH_SHORT).show();
                        }
                    }); // 다음 짧은 코드 생성

                    appAnchorState = AppAnchorState.HOSTED;
                }
            } else if (appAnchorState == AppAnchorState.RESOLVING) { // 다시 불러오기 하고 있다면!
                if (cloudAnchorState.isError()) {
                    Toast.makeText(getApplicationContext(), "Error resolving anchor...", Toast.LENGTH_SHORT).show();
                    appAnchorState = AppAnchorState.NONE;

                } else if (cloudAnchorState == Anchor.CloudAnchorState.SUCCESS) {
                    Toast.makeText(getApplicationContext(), "Anchor resolved...", Toast.LENGTH_SHORT).show();
                    appAnchorState = AppAnchorState.RESOLVED;

                }
            }
        });

        btn_clear = (Button) findViewById(R.id.btn_clear);
        // 초기화
        btn_clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cloudAnchor.detach();
                cloudAnchor = null;
                appAnchorState = AppAnchorState.NONE;
                isPlaced = false; // 또 둘 수 있다!!
            }
        });

        // 다시 불러오기
        btn_resolve = (Button) findViewById(R.id.btn_resolve); // 모델 불러오기!!!
        btn_resolve.setOnClickListener(view -> {
            if (cloudAnchor != null) { // cloud 앵커가 그대로 있으면 지우라고 요구
                Toast.makeText(getApplicationContext(), "Please clear the anchor", Toast.LENGTH_SHORT).show();
                return;
            }

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

                        cloudAnchor = arFragment.getArSceneView().getSession().resolveCloudAnchor(cloudAnchorId); // 해당 위치로 Anchor 가져오기
                    });

                    createModel(cloudAnchor); // 모델 만들어!

                    Toast.makeText(getApplicationContext(), "Now resolving anchor...", Toast.LENGTH_SHORT).show();
                }
            });
            dialog.show(getSupportFragmentManager(), "Resolve");
        });

        // 객체 불러오기
        btn_ahyu = (Button) findViewById(R.id.btn_ahyu);
        btn_tech = (Button) findViewById(R.id.btn_tech);
        btn_fly = (Button) findViewById(R.id.btn_fly);
        btn_ahyu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                modelRef = storage.getReference().child("ahyu.glb");
                try {
                    file = File.createTempFile("ahyu", "glb");
                    modelRef.getFile(file).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                            Toast.makeText(getApplicationContext(), "아휴 나왔다 뿅", Toast.LENGTH_SHORT).show();
                        }
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        btn_tech.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                modelRef = storage.getReference().child("tech.glb");
                try {
                    file = File.createTempFile("tech", "glb");
                    modelRef.getFile(file).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                            Toast.makeText(getApplicationContext(), "tech 나왔다 뿅", Toast.LENGTH_SHORT).show();
                        }
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        btn_fly.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                modelRef = storage.getReference().child("fly.glb");
                try {
                    file = File.createTempFile("fly", "glb");
                    modelRef.getFile(file).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                            Toast.makeText(getApplicationContext(), "날라다닌다 뿅", Toast.LENGTH_SHORT).show();
                        }
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }


    // 모델 렌더링
    private void createModel(Anchor anchor){
        ModelRenderable.builder()
                .setSource(this, Uri.parse(file.getPath()))
                .setIsFilamentGltf(true)
                .setAsyncLoadEnabled(true)
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

    }




}
