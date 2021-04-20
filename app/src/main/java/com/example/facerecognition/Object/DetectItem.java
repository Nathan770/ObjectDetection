package com.example.facerecognition.Object;

public class DetectItem {

    private String name;
    private float matched;


    public DetectItem() {
    }

    public DetectItem(String name, float matched) {
        this.name = name;
        this.matched = matched;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public float getMatched() {
        return matched;
    }

    public void setMatched(float matched) {
        this.matched = matched;
    }
}
