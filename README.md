# Geolocation App

**Geolocation App** es una aplicación distribuida diseñada para gestionar dispositivos (como repartidores), asignar permisos, recibir actualizaciones de ubicación en tiempo real y actualizar el estado de los dispositivos. Utiliza una arquitectura de microservicios con Spring Boot, MongoDB, RabbitMQ y WebSocket, todo orquestado mediante Docker.

## Autor
- **Javier Rioseco Rodríguez**

## Funcionalidades
- **Registro de Dispositivos**: Permite registrar dispositivos con un identificador único (`deviceId`) y un propietario (`ownerId`).
- **Gestión de Permisos**: Asigna permisos a dispositivos para enviar ubicaciones, almacenados en MongoDB.
- **Autenticación con JWT**: Genera tokens JWT para autenticar dispositivos.
- **Actualizaciones en Tiempo Real**: Recibe ubicaciones vía WebSocket y las procesa en tiempo real.
- **Distribución Asíncrona**: Usa RabbitMQ para validar permisos y distribuir ubicaciones entre servicios.
- **Persistencia**: Almacena dispositivos, permisos y ubicaciones en MongoDB.
- **Actualización de Estado**: Cambia el estado de los dispositivos (por ejemplo, de `INACTIVE` a `ACTIVE`) basado en las ubicaciones recibidas.

## Arquitectura
La aplicación se compone de tres microservicios y un módulo compartido:

1. **`auth-service` (Puerto 8082)**:
   - Maneja autenticación, generación de tokens JWT y validación de permisos.
   - Endpoints: `/auth/grant`, `/auth/token`.

2. **`device-service` (Puerto 8083)**:
   - Registra y actualiza dispositivos en MongoDB.
   - Endpoint: `/device/register`.
   - Escucha `location-update-queue` para actualizar estados.

3. **`geolocation-service` (Puerto 8081)**:
   - Recibe ubicaciones vía WebSocket (`ws://localhost:8081/location/ws`).
   - Valida permisos y distribuye ubicaciones a través de RabbitMQ.

4. **`shared-models`**:
   - Contiene el modelo `Location` compartido entre servicios.

### Tecnologías
- **Java 17** con **Spring Boot 3.2.3**.
- **MongoDB**: Base de datos NoSQL para almacenamiento persistente.
- **RabbitMQ**: Sistema de mensajería para comunicación asíncrona.
- **WebSocket**: Protocolo para actualizaciones en tiempo real.
- **Docker**: Contenerización de servicios.
- **Maven**: Gestión de dependencias y construcción.

### Flujo de Funcionamiento
1. Un cliente registra un dispositivo en `device-service`.
2. Solicita permisos y un token JWT a `auth-service`.
3. Envía una ubicación vía WebSocket a `geolocation-service`.
4. `geolocation-service` valida el permiso con `auth-service` a través de RabbitMQ.
5. Si es válido, guarda la ubicación y la publica a `device-service`, que actualiza el estado del dispositivo.

## Requisitos Previos
- **Java 17** instalado.
- **Maven** instalado.
- **Docker** y **Docker Compose** instalados.
- Acceso a una terminal para ejecutar comandos.

## Instalación y Ejecución
1. **Clonar el Repositorio**:
   ```bash
   git clone https://github.com/<tu-usuario>/geolocation-app.git
   cd geolocation-app
