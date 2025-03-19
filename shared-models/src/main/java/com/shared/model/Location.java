package com.shared.model;

import lombok.Data;

@Data
public class Location {
    private String deviceId;
    private double latitude;
    private double longitude;
    private long timestamp;
}