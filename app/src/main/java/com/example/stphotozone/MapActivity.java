package com.example.stphotozone;

import static com.google.android.gms.location.LocationRequest.PRIORITY_HIGH_ACCURACY;

import android.content.Intent;
import android.location.LocationRequest;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;

import java.util.ArrayList;
import java.util.List;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback {
    private List<String> list = new ArrayList<>();
    private Spinner spinner1, spinner2;
    private SpinnerAdapter adapter;
    private String selectedItem;
    private ImageButton imgBtnBack;

    // 지도 관련
    GoogleMap gMap;
    MapFragment mapFragment;

    // 현재 위치 update 및 마지막 위치를 얻기 위함
    private FusedLocationProviderClient mFusedLocationClient;
    LocationRequest locationRequest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        // 지도 연결
        mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


        // 각종 findViewById 불러옥
        imgBtnBack = (ImageButton) findViewById(R.id.imgBtnBack);
        spinner1 = (Spinner) findViewById(R.id.spinner_start);
        spinner2 = (Spinner) findViewById(R.id.spinner_end);

        // item 추가
        list.add("붕어방");
        list.add("다산관");
        list.add("미래관");

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

        /* 버튼 클릭 처리 부분*/
        imgBtnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), CameraActivity.class);
                startActivity(intent);
            }
        });

    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) { // 지도가 사용할 준비가 되면 ㅣ 콜백 호출
        gMap = googleMap;

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
}


