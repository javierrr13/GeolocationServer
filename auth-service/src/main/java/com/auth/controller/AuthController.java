package com.auth.controller;

import com.auth.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {
    @Autowired
    private AuthService authService;

    @PostMapping("/grant")
    public String grantPermission(@RequestParam String deviceId, @RequestParam String ownerId) {
        authService.grantPermission(deviceId, ownerId);
        return "Permiso otorgado";
    }

    @GetMapping("/token")
    public String getToken(@RequestParam String deviceId) {
        return authService.generateToken(deviceId);
    }
}