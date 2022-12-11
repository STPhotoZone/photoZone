package com.example.stphotozone;

import android.media.Image;

public class GridItem {
    public String name;
    public String description;
    public ChallengeActivity.Character character;
    public boolean isChecked = false;
    public Image image;
    public int modelId;


    public GridItem(String name, String description, ChallengeActivity.Character character, int modelId) {
        this.name = name;
        this.description = description;
        this.character = character;
        this.modelId = modelId;
    }

    public String getItemName() {
        return this.name;
    }

    public String getItemDescription() {
        return this.description;
    }

}
