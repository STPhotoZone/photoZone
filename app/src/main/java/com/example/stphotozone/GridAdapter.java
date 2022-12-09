package com.example.stphotozone;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.media.Image;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import java.util.ArrayList;

public class GridAdapter extends BaseAdapter {
    private Context context;
    private ArrayList<GridItem> array_item;
    private ArrayList<GridItem> visible_item;

    public GridAdapter(Context context) {
        this.context = context;
        this.array_item = new ArrayList<GridItem>();
        this.visible_item = new ArrayList<GridItem>();
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
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        convertView = inflater.inflate(R.layout.grid_item, parent, false);

        System.out.println("Here is getView");
        LinearLayout missionCard = (LinearLayout) convertView.findViewById(R.id.notSubmitted);
        ImageView imgMission = (ImageView) convertView.findViewById(R.id.imgMission);

        if(visible_item.get(position).isChecked)
        {
            missionCard.setVisibility(View.INVISIBLE);
            imgMission.setClickable(true);
        }

        TextView missionName = (TextView) convertView.findViewById(R.id.textMissionName);
        missionName.setText(visible_item.get(position).getItemName());
        switch (visible_item.get(position).character ) { // 색 바꿔 주기
            case BlackDragon:
                missionName.setTextColor(ContextCompat.getColor(context, R.color.black));
                break;
            case Tech:
                missionName.setTextColor(ContextCompat.getColor(context, R.color.yellow));
                break;
            case Ahyu:
                missionName.setTextColor(ContextCompat.getColor(context, R.color.ahyu));
                break;
        }

        TextView missionDescription = (TextView) convertView.findViewById(R.id.textMissionDescription);
        missionDescription.setText(visible_item.get(position).getItemDescription());



        imgMission.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String str = visible_item.get(position).getItemName();
                Toast.makeText(context, str, Toast.LENGTH_SHORT).show();
            }
        });

        return convertView;
    }

    public void setItem(String name, String description , ChallengeActivity.Character character){
        this.array_item.add(new GridItem(name, description, character));
    }

    public void checkedMission(String _name, Image _missionImg) {
        int index = 99;
        boolean alreadyChecked = true;

        for(int i =0; i < array_item.size(); i++) {
            if(array_item.get(i).name == _name) {
                index = i;
                alreadyChecked = array_item.get(i).isChecked;
                break;
            }
        }

        if(!alreadyChecked)
        {
            GridItem curItem =array_item.get(index);
            curItem.image = _missionImg;
            curItem.isChecked = true;


        }
    }
}
