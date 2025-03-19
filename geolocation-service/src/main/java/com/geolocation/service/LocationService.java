package com.geolocation.service;

import com.shared.model.Location;  // Cambiado
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

@Service
public class LocationService {
    private static final Logger logger = LoggerFactory.getLogger(LocationService.class);

    private final MongoTemplate mongoTemplate;
    private final RabbitTemplate rabbitTemplate;

    public LocationService(MongoTemplate mongoTemplate, RabbitTemplate rabbitTemplate) {
        this.mongoTemplate = mongoTemplate;
        this.rabbitTemplate = rabbitTemplate;
    }

    public String processLocation(Location location, String token) {
        try {
            String message = location.getDeviceId() + "," + token;
            logger.info("Enviando mensaje a permission-exchange: {}", message);
            Boolean hasPermission = (Boolean) rabbitTemplate.convertSendAndReceive("permission-exchange", "permission.check", message);

            if (hasPermission != null && hasPermission) {
                logger.info("Guardando ubicación en MongoDB: {}", location);
                mongoTemplate.save(location);
                logger.info("Enviando actualización a location-exchange: {}", location);
                rabbitTemplate.convertAndSend("location-exchange", "", location);
                return "Ubicación actualizada";
            } else {
                logger.warn("Permiso denegado para el dispositivo: {}", location.getDeviceId());
                return "Permiso denegado";
            }
        } catch (Exception e) {
            logger.error("Error al procesar la ubicación: {}", e.getMessage(), e);
            throw new RuntimeException("Error al procesar la ubicación: " + e.getMessage(), e);
        }
    }
}