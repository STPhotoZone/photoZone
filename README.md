# 🤳ST AR PhotoZone📸

---

## Introduction
STPhotozone은 서울과학기술대학교 캠퍼스에서 ST FREINDS AR 캐릭터와 사진을 찍을 수 있는 애플리케이션.           
*ST AR PhotoZone is an Application to take pictures with ST FREINDS AR characters on the campus of SeoulTech(Seoul National University of Science and Technology)*
ST Freinds와 함께 서울과학기술대학교 캠퍼스에 관한 특색 있는 사진을 가질 수 있게한다.                           
<img src="https://github.com/STPhotoZone/photoZone/assets/63052097/e09b1beb-55f9-44f2-bd25-9c0e2e35ae1e" width=400 />
<img src="https://github.com/STPhotoZone/photoZone/assets/63052097/0ca74092-a778-4b7a-a38a-7b67a821d1a2" width=400 /> 

<br>

**🤔 [What is ST FREINDS?](http://newsletter.seoultech.ac.kr/enewspaper/articleview.php?aid=2312&mvid=355)**      
>  <figure class="align-center"><img src="https://github.com/STPhotoZone/photoZone/assets/63052097/b7e6424a-e2b1-40de-bb19-53a57539bd8b" width=400 alt="ST FREINDS"></figure>               
 서울과학기술대학교의 홍보 캐릭터로 '테크', '소무니', '자니', '밥', '티나', '아휴'로 구성되어 있음.             
> <img src="https://github.com/STPhotoZone/photoZone/assets/63052097/441d014b-75b9-43c6-85b5-f4f99c604964" width=400 /> 테크 사진 

<br>
<br>

## 주요 Process
**1. Build ARCore Environment**    
- ARCore Session을 configure하여 AR 환경을 빌드
- 이때, lighting estimation, depth Mode를 configuration에 설정정

> `Session`이란    
 : motion tracking, environmental understanding, and lighting estimation과 같은 모든 AR 프로세스는 ARCore Session 내에서 발생     
 : ARCore API의 기본 진입점    
 : AR 시스템 상태를 관리하고 Session lifeCycle을 처리하여 앱이 Session create, configure, start, stop할 수 있도록 함시 해당 좌표에 anchor가 생성되고 원하는 model을 render한 후 해당 anchor에 transform.
 : 앱에서 camera image and device pose.에 액세스할 수 있는 frame을 얻을 수 있음.

```java
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

```

<br>

**3. Capture Photo**
session과 연결한 fragment의 view를 가져와서 이미지로 저장 

```java
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
```

<br>
<br>

## Demo
[YouTube Link](https://www.youtube.com/shorts/XF2PNxWLRiY)

<br>
<br>

## Features
![demo1 (2)](https://github.com/STPhotoZone/photoZone/assets/63052097/a6e9bea5-8ee4-4c2d-b6be-2229084f90af)

<br>

![demo3](https://github.com/STPhotoZone/photoZone/assets/63052097/431fb917-c407-446c-b98c-6331fc4e504e)
1. Model 선택 : "테크", "아휴" 중 원하는 모델을 선택.

<br>

![demo4](https://github.com/STPhotoZone/photoZone/assets/63052097/3c3bf182-53fb-4faf-83bf-88546b66a721)
2. 원하는 위치에 모델 렌더링 : 원하는 위치에서 화면을 탭하여 Anchor를 생성하고, 해당 Anchor에 모델이 렌더링.    

<br>

![demo5](https://github.com/STPhotoZone/photoZone/assets/63052097/8f94d069-d966-46b0-9642-4b47bb0c716f)
<img src-"https://github.com/STPhotoZone/photoZone/assets/63052097/c14bfc85-7dc4-49f9-9346-3a87e1e3c7ac" width=400 />

3. Camera 기능 : 사진 촬영 후 Preview 이미지를 보고 갤러리에 저장.     

<br>
<br>

## Tech Stack
- Android, Java : 모바일 애플리케이션을 구축하기 위해 사용   
- ARCore : AR 환경을 구축하기 위한 SDK
- Sceneform : ARCore API를 사용할 때 OpenGL을 배우지 않고도 사용할 수 있게 도와주는 플러그

<br>
<br>

## Reference
[ARCore 공식문서](https://developers.google.com/ar/develop/fundamentals?hl=ko)  
[ARCore codelab](https://codelabs.developers.google.com/?category=ar)
[How to Build a Simple Augmented Reality Android App?](https://www.geeksforgeeks.org/how-to-build-a-simple-augmented-reality-android-app/)       
[Android 앱에서 깊이 사용](https://developers.google.com/ar/develop/java/depth/developer-guide?hl=ko)       
[적절한 조명 사용하기](https://developers.google.com/ar/develop/lighting-estimation?hl=ko)       
ChatGPT 4o
