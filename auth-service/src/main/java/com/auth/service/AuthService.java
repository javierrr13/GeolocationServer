package com.auth.service;

import com.auth.model.DevicePermission;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class AuthService {
    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);
    private final MongoTemplate mongoTemplate;
    private static final String SECRET_KEY = "your-secret-key-must-be-at-least-32-chars";

    public AuthService(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    public String generateToken(String deviceId) {
        logger.info("Generando token para deviceId: {}", deviceId);
        String token = Jwts.builder()
            .subject(deviceId)
            .issuedAt(new Date())
            .expiration(new Date(System.currentTimeMillis() + 86400000)) // 24 horas
            .signWith(Keys.hmacShaKeyFor(SECRET_KEY.getBytes()))
            .compact();
        logger.info("Token generado: {}", token);
        return token;
    }

    public boolean validateToken(String token, String deviceId) {
        try {
            String subject = Jwts.parser()
                .verifyWith(Keys.hmacShaKeyFor(SECRET_KEY.getBytes()))
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
            boolean isValid = subject.equals(deviceId);
            logger.info("Validando token para deviceId: {}. Subject: {}. Resultado: {}", deviceId, subject, isValid);
            return isValid;
        } catch (Exception e) {
            logger.error("Error validando token para deviceId: {}. Excepción: {}", deviceId, e.getMessage());
            return false;
        }
    }

    @RabbitListener(queues = "permission-check-queue", messageConverter = "jsonMessageConverter")
    public Boolean checkPermission(String message) {
        logger.info("Mensaje recibido en permission-check-queue: {}", message);
        String[] parts = message.split(",");
        if (parts.length != 2) {
            logger.error("Formato de mensaje inválido: {}", message);
            return false;
        }

        String deviceId = parts[0];
        String token = parts[1];

        DevicePermission permission = mongoTemplate.findById(deviceId, DevicePermission.class);
        if (permission == null) {
            logger.warn("No se encontró permiso para deviceId: {}", deviceId);
            return false;
        }

        boolean hasPermission = permission.isAllowed() && validateToken(token, deviceId);
        logger.info("Permiso para {}: allowed={}, token válido={}. Resultado final: {}", 
            deviceId, permission.isAllowed(), validateToken(token, deviceId), hasPermission);
        return hasPermission;
    }

    public void grantPermission(String deviceId, String ownerId) {
        logger.info("Otorgando permiso para deviceId: {}, ownerId: {}", deviceId, ownerId);
        DevicePermission permission = new DevicePermission();
        permission.setDeviceId(deviceId);
        permission.setOwnerId(ownerId);
        permission.setAllowed(true);
        mongoTemplate.save(permission);
        logger.info("Permiso guardado para deviceId: {}", deviceId);
    }
}