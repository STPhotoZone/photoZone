package com.example.stphotozone;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.widget.Toast;

import com.google.ar.core.Anchor;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.ux.ArFragment;
import com.google.ar.sceneform.ux.TransformableNode;

import java.util.Objects;

public class CameraActivity extends AppCompatActivity {

    // object of ArFragment Class
    private ArFragment arCam;

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

        // hardware check
        if(checkCameraSystem(this)){

            arCam = (ArFragment) getSupportFragmentManager().findFragmentById(R.id.arArea);
            //ArFragment is linked up with its respective id used in the activity_main.xml
            arCam.setOnTapArPlaneListener((hitResult, plane, motionEvent) -> { // 화면을 터치하면 해당 위치에 앵커 생성
                clickNo++;

                if(clickNo == 1)
                {   // 모델을 화면 상에 보이도록 하자.
                    Anchor anchor = hitResult.createAnchor(); // 터치한 부분에 anchor 생성
                    ModelRenderable.builder()
                            .setSource(this, R.raw.gfg_gold_text_stand_2) // raw 폴더에 있는 오브젝트 가져오기
                            .setIsFilamentGltf(true) // 파일이 gltf 형식인가?
                            .build()
                            .thenAccept(modelRenderable -> addModel(anchor, modelRenderable))
                            .exceptionally(throwable -> { // 예외 처리
                                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                                builder.setMessage("Something is not right" + throwable.getMessage()).show();
                                return null;
                            });
                }
            });
        } else { // checkCameraSystem false
            return;
        }

    }

    // Model 추가하는 코드
    private void addModel(Anchor anchor, ModelRenderable modelRenderable) {

        // Creating a AnchorNode with a specific anchor
        AnchorNode anchorNode = new AnchorNode(anchor); // anchor는 사용자가 화면에 touch한 위치임

        // attaching the anchorNode with the ArFragment
        anchorNode.setParent(arCam.getArSceneView().getScene()); // 우리 화면에 붙이기

        // attaching the anchorNode with the TransformableNode
        TransformableNode model = new TransformableNode(arCam.getTransformationSystem()); // 드래그 동작을 사용하여 이 노드를 변환하는 컨트롤러를 반환
        model.setParent(anchorNode);

        // attaching the 3d model with the TransformableNode
        // that is already attached with the node
        model.setRenderable(modelRenderable); // 해당 노드에 렌더링
        model.select();
    }


}