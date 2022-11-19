package com.example.stphotozone;

import static android.content.ContentValues.TAG;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Map;

public class ChallengeActivity extends AppCompatActivity {

    private GridView mission_grid;
    private GridAdapter mission_gridAdt;

    public enum Character{ BlackDragon, Tech, Ahyu};

    RadioGroup missionGroup;
    ImageButton imgBtnBack;

    private FirebaseAuth firebaseAuth;
    FirebaseFirestore db;

    TextView nickName;
    TextView missionCount;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.challenge);

        firebaseAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        missionGroup = (RadioGroup) findViewById(R.id.rgMission);
        imgBtnBack = (ImageButton) findViewById(R.id.imgBtnBack);

        nickName =  (TextView) findViewById(R.id.txtNickname);
        missionCount = (TextView) findViewById(R.id.txtCount);

        setUserInfo();

        mission_grid = (GridView) findViewById(R.id.grid_mission);
        mission_gridAdt = new GridAdapter(this);

        mission_gridAdt.setItem( getString(R.string.BD_name), getString(R.string.BD_description), Character.BlackDragon);
        mission_gridAdt.setItem( getString(R.string.Tech_name), getString(R.string.Tech_description), Character.Tech);
        mission_gridAdt.setItem( getString(R.string.Ahyu_name), getString(R.string.Ahyu_description), Character.Ahyu);


        for(int j=0; j <mission_gridAdt.getArrayCount(); j++)
        {
            GridItem temp = mission_gridAdt.getArrayItem(j);
            mission_gridAdt.setVisible_item(temp);

        }

        mission_grid.setAdapter(mission_gridAdt);

        imgBtnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
                finish();

            }
        });

        missionGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                switch (missionGroup.getCheckedRadioButtonId())
                {
                    case R.id.rdBDragon:
                        System.out.println("BDragon");
                        mission_gridAdt.clearVisible_item();
                        for(int j=0; j <mission_gridAdt.getArrayCount(); j++)
                        {
                            GridItem temp = mission_gridAdt.getArrayItem(j);

                            if(temp.character == Character.BlackDragon)
                            {
                                mission_gridAdt.setVisible_item(temp);
                            }
                        }
                        mission_grid.setAdapter(mission_gridAdt);
                        break;
                    case R.id.rdTech:
                        System.out.println("Tech");
                        mission_gridAdt.clearVisible_item();
                        for(int j=0; j <mission_gridAdt.getArrayCount(); j++)
                        {
                            GridItem temp = mission_gridAdt.getArrayItem(j);

                            if(temp.character == Character.Tech)
                            {
                                mission_gridAdt.setVisible_item(temp);
                            }
                        }
                        mission_grid.setAdapter(mission_gridAdt);
                        break;
                    case R.id.rdAhyu:
                        System.out.println("Ahyu");
                        mission_gridAdt.clearVisible_item();
                        for(int j=0; j <mission_gridAdt.getArrayCount(); j++)
                        {
                            GridItem temp = mission_gridAdt.getArrayItem(j);

                            if(temp.character == Character.Ahyu)
                            {
                                mission_gridAdt.setVisible_item(temp);
                            }
                        }
                        mission_grid.setAdapter(mission_gridAdt);
                        break;
                    case R.id.rdTotal:
                        System.out.println("total!");
                        mission_gridAdt.clearVisible_item();
                        for(int j=0; j <mission_gridAdt.getArrayCount(); j++)
                        {
                            GridItem temp = mission_gridAdt.getArrayItem(j);
                            mission_gridAdt.setVisible_item(temp);

                        }
                        mission_grid.setAdapter(mission_gridAdt);
                        break;

                }
            }
        });

        System.out.println("oncreate");
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(intent);
        finish();
    }

    public void setUserInfo() {
        CollectionReference productRef = db.collection("users");
        productRef
                .whereEqualTo("email", firebaseAuth.getCurrentUser().getEmail())
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful())
                        {
                            QuerySnapshot document =  task.getResult();
                            Map<String, Object> data = document.getDocuments().get(0).getData();

                            String name = data.get("nickname").toString();
                            String num = data.get("mission_num").toString();

                            nickName.setText(name);
                            missionCount.setText(num);
                        }
                    }
                });

    }
}
