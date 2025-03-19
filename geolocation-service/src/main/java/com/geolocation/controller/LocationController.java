package com.geolocation.controller;

import com.shared.model.Location; 
import com.geolocation.service.LocationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/location")
public class LocationController {
    @Autowired
    private LocationService locationService;

    @PostMapping("/update")
    public String updateLocation(@RequestBody Location location, @RequestHeader("Authorization") String token) {
        return locationService.processLocation(location, token);
    }
}