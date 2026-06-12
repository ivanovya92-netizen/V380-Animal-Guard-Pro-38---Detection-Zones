package com.yani.v380animalguardpro38.model;

public class EventRecord {
    public long id;
    public long recTime;
    public int count;
    public String imageUrl;
    public String analysis;

    public EventRecord(long id, long recTime, int count, String imageUrl, String analysis) {
        this.id = id;
        this.recTime = recTime;
        this.count = count;
        this.imageUrl = imageUrl == null ? "" : imageUrl;
        this.analysis = analysis == null ? "" : analysis;
    }
}
