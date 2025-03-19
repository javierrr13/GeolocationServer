package com.device.controller;

import com.device.service.DeviceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/device")
public class DeviceController {
    @Autowired
    private DeviceService deviceService;

    @PostMapping("/register")
    public String registerDevice(@RequestParam String deviceId, @RequestParam String ownerId) {
        deviceService.registerDevice(deviceId, ownerId);
        return "Dispositivo registrado";
    }
}