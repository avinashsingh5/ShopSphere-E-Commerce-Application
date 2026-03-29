# Discovery Service (Eureka Server)

## Overview
The Discovery Service acts as a **Service Registry** for the ShopSphere microservices architecture. It is built using **Spring Cloud Netflix Eureka Server**.

## Why a Service Registry?
In a microservices architecture, services need to communicate with each other. Without a service registry, every service would need to know the exact IP address and port of every other service — this creates **tight coupling** and makes the system brittle.

### How It Works
1. **Registration**: When a microservice (Auth, Catalog, Order) starts up, it registers itself with the Eureka Server, providing its name, IP address, and port.
2. **Discovery**: When one service needs to communicate with another, it queries the Eureka Server to find the current location of the target service.
3. **Health Monitoring**: Eureka continuously monitors registered services via heartbeats. If a service stops sending heartbeats, it is removed from the registry.

### Benefits
- **No hardcoded IPs**: Services find each other dynamically through the registry
- **Load Balancing**: Multiple instances of a service can register, and clients can load-balance across them
- **Fault Tolerance**: If a service instance goes down, it is automatically deregistered
- **Scalability**: New instances can be added without any configuration changes to other services

## Configuration
- **Port**: `8761`
- **Self-Registration**: Disabled (`register-with-eureka: false`) since this is the server itself
- **Fetch Registry**: Disabled (`fetch-registry: false`) since this is the single source of truth

## Running
```bash
cd discovery-service
mvn spring-boot:run
```

Access the Eureka Dashboard at: [http://localhost:8761](http://localhost:8761)
