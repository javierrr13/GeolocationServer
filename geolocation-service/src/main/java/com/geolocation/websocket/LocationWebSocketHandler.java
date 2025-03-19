package com.geolocation.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shared.model.Location; 
import com.geolocation.service.LocationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@Component
public class LocationWebSocketHandler extends TextWebSocketHandler {
    private static final Logger logger = LoggerFactory.getLogger(LocationWebSocketHandler.class);

    private final LocationService locationService;
    private final Set<WebSocketSession> sessions = Collections.synchronizedSet(new HashSet<>());
    private final ObjectMapper objectMapper = new ObjectMapper();

    public LocationWebSocketHandler(LocationService locationService) {
        this.locationService = locationService;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        logger.info("Nueva conexión WebSocket establecida: {}", session.getId());
        String token = session.getHandshakeHeaders().getFirst("Authorization");
        logger.info("Encabezados recibidos: {}", session.getHandshakeHeaders());
        if (token == null || token.isEmpty()) {
            logger.warn("Conexión rechazada: falta el encabezado Authorization");
            session.close(CloseStatus.NOT_ACCEPTABLE.withReason("Authorization header required"));
            return;
        }
        logger.info("Token recibido: {}", token);
        sessions.add(session);
        session.sendMessage(new TextMessage("Conexión establecida correctamente"));
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        logger.info("Mensaje recibido: {}", message.getPayload());
        String token = session.getHandshakeHeaders().getFirst("Authorization");
        Location location;
        try {
            location = objectMapper.readValue(message.getPayload(), Location.class);
            logger.info("Ubicación deserializada: {}", location);
        } catch (Exception e) {
            logger.error("Error al deserializar el mensaje: {}", e.getMessage(), e);
            session.sendMessage(new TextMessage("Formato de mensaje inválido"));
            return;
        }

        try {
            String result = locationService.processLocation(location, token);
            logger.info("Resultado del procesamiento: {}", result);
            session.sendMessage(new TextMessage(result));
            if ("Ubicación actualizada".equals(result)) {
                String locationJson = objectMapper.writeValueAsString(location);
                for (WebSocketSession s : sessions) {
                    if (s.isOpen() && s != session) {
                        s.sendMessage(new TextMessage(locationJson));
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Error al procesar el mensaje: {}", e.getMessage(), e);
            session.sendMessage(new TextMessage("Error interno: " + e.getMessage()));
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        logger.info("Conexión cerrada: {} con estado {}", session.getId(), status);
        sessions.remove(session);
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        logger.error("Error de transporte en WebSocket: {}", exception.getMessage(), exception);
        session.sendMessage(new TextMessage("Error interno: " + exception.getMessage()));
        session.close(CloseStatus.SERVER_ERROR.withReason("Error interno: " + exception.getMessage()));
    }
}