package com.example.stphotozone;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class GridAdapter extends BaseAdapter {
    public static Context gridContext;
    private ArrayList<GridItem> array_item;
    private ArrayList<GridItem> visible_item;

    //firebase
    FirebaseAuth firebaseAuth;
    FirebaseFirestore db;

    public GridAdapter(Context gridContext) {
        this.gridContext = gridContext;
        this.array_item = new ArrayList<GridItem>();
        this.visible_item = new ArrayList<GridItem>();

        firebaseAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
    }

    @Override
    public int getCount() {
        return this.visible_item.size();
    }

    @Override
    public GridItem getItem(int position) {
        return this.visible_item.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    public ArrayList<GridItem> getVisible_item() {
        return this.visible_item;
    }

    public GridItem getArrayItem(int position) {
        return this.array_item.get(position);
    }

    public int getArrayCount() {
        return this.array_item.size();
    }

    public void clearVisible_item() {
        visible_item.clear();
    }

    public void setVisible_item(GridItem item) {
        this.visible_item.add(item);
    }

    // 실제 화면 배치할 때 쓰는 메소드
    @SuppressLint("ViewHolder")
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) gridContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        convertView = inflater.inflate(R.layout.grid_item, parent, false);

        System.out.println("Here is getView");
        LinearLayout missionCard = (LinearLayout) convertView.findViewById(R.id.notSubmitted);
        ImageView imgMission = (ImageView) convertView.findViewById(R.id.imgMission);

        if(missionCheck(visible_item.get(position).modelId))
        {
            imgMission.setVisibility(View.VISIBLE);
        }

        TextView missionName = (TextView) convertView.findViewById(R.id.textMissionName);
        missionName.setText(visible_item.get(position).getItemName());
        switch (visible_item.get(position).character ) { // 색 바꿔 주기
            case BlackDragon:
                missionName.setTextColor(ContextCompat.getColor(gridContext, R.color.black));
                break;
            case Tech:
                missionName.setTextColor(ContextCompat.getColor(gridContext, R.color.yellow));
                break;
            case Ahyu:
                missionName.setTextColor(ContextCompat.getColor(gridContext, R.color.ahyu));
                break;
        }

        TextView missionDescription = (TextView) convertView.findViewById(R.id.textMissionDescription);
        missionDescription.setText(visible_item.get(position).getItemDescription());



        imgMission.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String str = visible_item.get(position).getItemName();
                Toast.makeText(gridContext, str, Toast.LENGTH_SHORT).show();
            }
        });

        return convertView;
    }

    public void setItem(String name, String description , ChallengeActivity.Character character, int modelId){
        this.array_item.add(new GridItem(name, description, character , modelId));
    }


    // model 체크 됐는지 확인
    public boolean missionCheck(int _modelId)
    {

        final boolean[] result = {false};
        if(firebaseAuth != null)
        {
            CollectionReference productRef = db.collection("users");
            productRef
                    .whereEqualTo("email", firebaseAuth.getCurrentUser().getEmail())
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    Map<String, Object> data = document.getData();

                                    result[0] = Boolean.parseBoolean(data.get("mission_Id"+String.valueOf(_modelId)).toString());
                                }

                            }
                        }
                    });
        }

        return result[0];
    }

    public boolean checkedMission(int _modelId) {
        int index = 99;
        boolean alreadyChecked = true;

        for(int i =0; i < array_item.size(); i++) {
            if(array_item.get(i).modelId == _modelId) {
                index = i;
                alreadyChecked = array_item.get(i).isChecked;
                break;
            }
        }

        if(!alreadyChecked)
        {
            GridItem curItem =array_item.get(index);
            curItem.isChecked = true;
        }

        return alreadyChecked;
    }
}
