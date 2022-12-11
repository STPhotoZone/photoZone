package com.example.stphotozone;

import static com.google.android.gms.location.LocationRequest.PRIORITY_HIGH_ACCURACY;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.location.LocationRequest;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.telephony.CarrierConfigManager;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.List;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback {
    private List<String> list = new ArrayList<>();
    private Spinner spinner1, spinner2;
    private SpinnerAdapter adapter;
    private String selectedItem;
    private ImageButton imgBtnBack, imgBtnRefresh;

    // 지도 관련
    GoogleMap gMap;
    MapFragment mapFragment;
    double longitude;
    double latitude;
    MarkerOptions currentMarkerOptions;
    Marker currentMarker;
    private GpsTracker gpsTracker;

    private static final int GPS_ENABLE_REQUEST_CODE = 2001;
    private static final int PERMISSIONS_REQUEST_CODE = 100;
    String[] REQUIRED_PERMISSIONS = {Manifest.permission.ACCESS_FINE_LOCATION,
    Manifest.permission.ACCESS_COARSE_LOCATION};

    // 현재 위치 update 및 마지막 위치를 얻기 위함
    private FusedLocationProviderClient mFusedLocationClient;
    LocationRequest locationRequest;

    String place;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        // 지도 연결
        mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


        // 각종 findViewById 불러옥
        imgBtnBack = (ImageButton) findViewById(R.id.imgBtnBack);
        imgBtnRefresh = (ImageButton) findViewById(R.id.imgBtnRefresh);
        spinner1 = (Spinner) findViewById(R.id.spinner_start);
        spinner2 = (Spinner) findViewById(R.id.spinner_end);

        // item 추가
        list.add("붕어방");
        list.add("다산관");
        list.add("미래관");

        //지도 기능
        if (!checkLocationServicesStatus()) {
            showDialogForeLocationServiceSetting();
            Log.d("checkLocation : ", "false");
        } else {
            checkRunTimePermission();
            Log.d("checkLocation : ", "true");
        }


        gpsTracker = new GpsTracker(this, this);

        longitude = gpsTracker.getLongitude();
        Log.d("Map1, longitude : ", String.valueOf(longitude));
        latitude = gpsTracker.getLatitude();
        Log.d("Map1, latitude : ", String.valueOf(latitude));
        Toast.makeText(this, "longitude: " + longitude + "latitude : "+ latitude, Toast.LENGTH_SHORT).show();

        // 스피너에 붙일 어댑터 초기화
        adapter = new SpinnerAdapter(this, list);
        spinner1.setAdapter(adapter);
        spinner2.setAdapter(adapter);

        spinner1.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                //selectedItem = adapter.getItem(); // 스피너에서 선택한 item 이름 받아ㅗ기
                adapter.flag = true;
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                // 아무것도 선택 안 했을 때
            }
        });

        /* 뒤로가기 버튼 클릭 처리 부분*/
        imgBtnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), CameraActivity.class);
                intent.putExtra("place", place);
                startActivity(intent);
            }
        });

        // 갱신
        imgBtnRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                gpsTracker = new GpsTracker(MapActivity.this, MapActivity.this);

                longitude = gpsTracker.getLongitude();
                Log.d("Map1, longitude : ", String.valueOf(longitude));
                latitude = gpsTracker.getLatitude();
                Log.d("Map1, latitude : ", String.valueOf(latitude));
                Toast.makeText(MapActivity.this, "longitude: " + longitude + "latitude : "+ latitude, Toast.LENGTH_SHORT).show();

                LatLng latlng = new LatLng(latitude, longitude);

                gMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latlng, 18));
                setCurrentMarker(latlng);
                currentMarker = gMap.addMarker(currentMarkerOptions);

                wherePlace(latlng); // 해당 위치로 주소 찾ㄱ
            }
        });

    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) { // 지도가 사용할 준비가 되면 ㅣ 콜백 호출
        LatLng latLng = new LatLng(latitude, longitude);

        gMap = googleMap;
        gMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 18));
        gMap.getUiSettings().setZoomControlsEnabled(true);
        setCurrentMarker(latLng);
        currentMarker = gMap.addMarker(currentMarkerOptions);
        gMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
    }

    public void setCurrentMarker(LatLng latLng)
    {
        if (currentMarker != null)
        {
            currentMarker.remove();
        }
        Log.d("marker", "currentMarker");
        currentMarkerOptions = new MarkerOptions();
        currentMarkerOptions.position(latLng)
                .title("현위치")
                .snippet("지금 여기 있어요~")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN));



    }

    private void setupLocClient() {
        mFusedLocationClient =
                LocationServices.getFusedLocationProviderClient(this);
    }

    private void setLocationRequest() {
//        locationRequest= new LocationRequest();
//        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
//        locationRequest.setInterval(5000);
//        locationRequest.setFastestInterval(1000);

    }


    private void getCurrentLocation(){

    }

    @SuppressLint("MissingSuperCall")
    @Override
    public void onRequestPermissionsResult(int permsRequestCode, @NonNull String[] permissions, @NonNull int[] grandResults) {
        if (permsRequestCode == PERMISSIONS_REQUEST_CODE && grandResults.length == REQUIRED_PERMISSIONS.length) {
            boolean check_result = true;

            for (int result : grandResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    check_result = false;
                    break;
                }
            }

            if (check_result) {
                ;
            } else {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, REQUIRED_PERMISSIONS[0])
                        || ActivityCompat.shouldShowRequestPermissionRationale(this, REQUIRED_PERMISSIONS[1])) {
                    Toast.makeText(this, "퍼미션이 거부되었습니다. 퍼미션을 허용해주세요.", Toast.LENGTH_LONG).show();
                    finish();
                } else {
                    Toast.makeText(this, "퍼미션이 거부되었습니다. 설정(앱 정보)에서 퍼미션을 허용해야 합니다. ", Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    void checkRunTimePermission(){

        //런타임 퍼미션 처리
        //1. 위치 퍼미션을 가지고 있는지 체크합니다.
        int hasFineLocationPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        int hasCoarseLocationPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION);

        if (hasFineLocationPermission == PackageManager.PERMISSION_GRANTED &&
        hasCoarseLocationPermission == PackageManager.PERMISSION_GRANTED){

            //2. 이미 퍼미션을 가지고 있다면
            // (안드로이드 6.0 이하 버전은 런타임 퍼미션이 필요없어 이미 허용된 것으로 인식)

            // 3. 위치 값을 가져올 수 있음

        } else { //2. 퍼미션 요청을 허용한 적이 없다면 퍼미션 요청이 필요합니다. 2가지 경우(3-1, 4-1)가 있습니다.

            // 3-1. 사용자가 퍼미션 거부를 한 적이 있는 경우에는
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, REQUIRED_PERMISSIONS[0])){

                // 3-2 요청을 징행하기 전에 사용자에게 퍼미션이 필요한 이유 설명
                Toast.makeText(this, "이 앱을 실행하려면 위치 접근 권한이 필요합니다.", Toast.LENGTH_LONG).show();

                // 3-3. 사용자에게 퍼미션 요청을 합니다. 요청 결과는 onRequestPermissionResult에서 수신됩니다.
                ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, PERMISSIONS_REQUEST_CODE);

            } else {
                // 4-1. 사용자가 퍼미션 거부를 한 적이 없는 경우에는 퍼미션 요청을 바로 합니다.
                // 요청 결과는 onRequestPermissionResult에서 수신됩니다.
                ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, PERMISSIONS_REQUEST_CODE);
            }
        }
    }

    private void showDialogForeLocationServiceSetting() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("위치 서비스 비활성화");
        builder.setMessage("앱을 사용하기 위해서는 위치 서비스가 필요합니다 \n"
        + "위치 설정을 수정하실래요?");
        builder.setCancelable(true);
        builder.setPositiveButton("설정", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Intent callGPSSettingIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivityForResult(callGPSSettingIntent, GPS_ENABLE_REQUEST_CODE);
            }
        });
        builder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
            }
        });
        builder.create().show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case GPS_ENABLE_REQUEST_CODE:

                //사용자가 GPS 활성 시켰는지 검사
                if (checkLocationServicesStatus()) {
                    if (checkLocationServicesStatus()) {

                        Log.d("@@@", "onAcitivityResult : GPS 활성화 되있음");
                        checkRunTimePermission();
                        return;
                    }
                }

                break;
        }
    }

    public boolean checkLocationServicesStatus() {
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    // 해당 위치 확인 하는 함수
    public String wherePlace(LatLng latlng){
        this.place = "미래관"; // 설정
        return this.place;
    }
}


