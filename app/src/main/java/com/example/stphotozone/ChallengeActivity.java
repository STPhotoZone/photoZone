package com.example.stphotozone;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.RadioGroup;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class ChallengeActivity extends AppCompatActivity {

    private GridView mission_grid;
    private GridAdapter mission_gridAdt;

    public enum Character{ BlackDragon, Tech, Ahyu};

    RadioGroup missionGroup;
    ImageButton imgBtnBack;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.challenge);

        missionGroup = findViewById(R.id.rgMission);
        imgBtnBack = findViewById(R.id.imgBtnBack);

        mission_grid = findViewById(R.id.grid_mission);
        mission_gridAdt = new GridAdapter(this);

        System.out.println("after Adapter");
        mission_gridAdt.setItem( getString(R.string.BD_name), getString(R.string.BD_description), Character.BlackDragon);
        mission_gridAdt.setItem( getString(R.string.Tech_name), getString(R.string.Tech_description), Character.Tech);
        mission_gridAdt.setItem( getString(R.string.Ahyu_name), getString(R.string.Ahyu_description), Character.Ahyu);

        System.out.println("after setItem");

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
}
