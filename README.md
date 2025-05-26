# 🖥️ Server Monitor - Sistema de Monitoreo Empresarial

![Java](https://img.shields.io/badge/Java-17-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-green)
![Vaadin](https://img.shields.io/badge/Vaadin-24-blue)
![Auth0](https://img.shields.io/badge/Auth0-OAuth2-red)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-Neon-blue)

Sistema de monitoreo de servidores desarrollado con Spring Boot, Vaadin y Auth0. Implementa autenticación segura, control de roles y gestión de infraestructura.

## 🚀 **Estado del Proyecto**

- ✅ **Parcial 1**: Aplicación base con Docker (`docker compose up -d`)
- ✅ **Parcial 2**: Sistema completo con Auth0, roles y deploy en producción

## 🌟 **Características Principales**

### 🔐 **Autenticación y Seguridad**
- Autenticación OAuth2 con Auth0 y Google
- Control de roles: Admin, Operator, Viewer
- Protección de rutas con JWT tokens
- Variables de entorno para configuración segura

### 🎨 **Interfaz de Usuario**
- Dashboard responsivo con métricas en tiempo real
- Gestión de bases de datos monitoreadas
- Sistema de validaciones y confirmaciones
- Diseño moderno y navegación intuitiva

### 🛠️ **Funcionalidades Técnicas**
- Base de datos PostgreSQL en Neon.tech
- Monitoreo de CPU, RAM y Disco
- Configuración de umbrales y alertas
- Deploy automático en Render

## 🌐 **Acceso**

**URL de Producción**: [https://server-monitor-9zdf.onrender.com](https://server-monitor-9zdf.onrender.com)

*Para credenciales de prueba, contactar al equipo de desarrollo.*

## 🏗️ **Arquitectura**

- **Frontend**: Vaadin 24 con UI reactiva
- **Backend**: Spring Boot 3 + Spring Security
- **Base de Datos**: PostgreSQL (Neon.tech)
- **Autenticación**: Auth0 con Google OAuth
- **Deploy**: Render con integración continua

## 🔧 **Desarrollo Local**

```bash
git clone [URL_DEL_REPO]
cd server-monitor
docker compose up -d
```

## 📋 **Cumplimiento Parcial 2**

✅ Autenticación con Auth0  
✅ Gestión de roles desde dashboard Auth0  
✅ Protección de rutas con JWT  
✅ Despliegue en Render  
✅ Conexión con base de datos Neon  
✅ Configuración segura sin datos expuestos  

## 👨‍💻 **Stack Tecnológico**

- Java 17 + Spring Boot 3
- Vaadin 24 para Frontend
- Auth0 para Autenticación
- PostgreSQL + JPA/Hibernate
- Docker para desarrollo
- Render para producción

---

**🎓 Universidad de El Salvador - Parcial 2**  
**📅 Mayo 2025**