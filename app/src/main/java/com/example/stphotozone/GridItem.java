package com.example.stphotozone;

public class GridItem {
    public String name;
    public String description;
    public ChallengeActivity.Character character;


    public GridItem(String name, String description, ChallengeActivity.Character character) {
        this.name = name;
        this.description = description;
        this.character = character;
    }

    public String getItemName() {
        return this.name;
    }

    public String getItemDescription() {
        return this.description;
    }
}
