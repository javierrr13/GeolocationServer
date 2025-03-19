package com.device.service;

import com.device.model.Device;
import com.shared.model.Location;  // Cambiado
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

@Service
public class DeviceService {
    private static final Logger logger = LoggerFactory.getLogger(DeviceService.class);

    @Autowired
    private MongoTemplate mongoTemplate;

    @RabbitListener(queues = "location-update-queue")
    public void updateDeviceStatus(Location location) {
        logger.info("Recibida actualización de ubicación: {}", location);
        logger.info("Actualizando estado del dispositivo con ubicación: {}", location);
        Device device = mongoTemplate.findById(location.getDeviceId(), Device.class);
        if (device != null) {
            device.setStatus("ACTIVE");
            mongoTemplate.save(device);
            logger.info("Dispositivo {} actualizado a ACTIVE", device.getDeviceId());
        } else {
            logger.warn("Dispositivo {} no encontrado", location.getDeviceId());
        }
    }

    public void registerDevice(String deviceId, String ownerId) {
        Device device = new Device();
        device.setDeviceId(deviceId);
        device.setOwnerId(ownerId);
        device.setStatus("INACTIVE");
        mongoTemplate.save(device);
        logger.info("Dispositivo {} registrado con propietario {}", deviceId, ownerId);
    }
}