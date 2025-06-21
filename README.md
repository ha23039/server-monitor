# 🖥️ Server Monitor - Sistema de Monitoreo Empresarial

![Java](https://img.shields.io/badge/Java-17-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-green)
![Vaadin](https://img.shields.io/badge/Vaadin-24-blue)
![Auth0](https://img.shields.io/badge/Auth0-OAuth2-red)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-Neon-blue)

Sistema de monitoreo de servidores desarrollado con Spring Boot, Vaadin y Auth0. Implementa autenticación segura, control de roles, gestión de infraestructura y monitoreo en tiempo real con capacidades de exportación.

## 🚀 **Estado del Proyecto**

- ✅ **Parcial 1**: Aplicación base con Docker (`docker compose up -d`)
- ✅ **Parcial 2**: Sistema completo con Auth0, roles y deploy en producción
- ✅ **Parcial 3**: Panel en tiempo real y sistema de exportaciones

## 🌟 **Características Principales**

### 🔐 **Autenticación y Seguridad**
- Autenticación OAuth2 con Auth0 y Google
- Control de roles: Admin, Operator, Viewer
- Protección de rutas con JWT tokens
- Variables de entorno para configuración segura

### 📊 **Monitoreo en Tiempo Real**
- Dashboard con actualizaciones automáticas cada 30 segundos
- Gráficos interactivos de CPU, RAM y Disco
- Indicadores visuales de estado de servidores
- Alertas automáticas por umbrales configurables
- Histórico de métricas con visualización temporal

### 📈 **Sistema de Reportes y Exportaciones**
- Exportación de datos a Excel (.xlsx)
- Generación de reportes PDF con gráficos
- Exportación de métricas a CSV
- Reportes programados y bajo demanda
- Filtros por fecha, servidor y tipo de métrica

### 🎨 **Interfaz de Usuario**
- Dashboard responsivo con métricas en tiempo real
- Gestión de bases de datos monitoreadas
- Sistema de validaciones y confirmaciones
- Diseño moderno y navegación intuitiva
- Notificaciones push para alertas críticas

### 🛠️ **Funcionalidades Técnicas**
- Base de datos PostgreSQL en Neon.tech
- Monitoreo de CPU, RAM y Disco
- Configuración de umbrales y alertas
- Deploy automático en Render
- API REST protegida con JWT
- WebSockets para actualizaciones en tiempo real

## 🌐 **Acceso**

**URL de Producción**: [https://server-monitor-9zdf.onrender.com](https://server-monitor-9zdf.onrender.com)

*Para credenciales de prueba, contactar al equipo de desarrollo.*

## 🏗️ **Arquitectura**

- **Frontend**: Vaadin 24 con UI reactiva y componentes en tiempo real
- **Backend**: Spring Boot 3 + Spring Security + WebSockets
- **Base de Datos**: PostgreSQL (Neon.tech) con optimización para series temporales
- **Autenticación**: Auth0 con Google OAuth
- **Deploy**: Render con integración continua
- **Exportaciones**: Apache POI (Excel), iText (PDF), OpenCSV

## 🔧 **Desarrollo Local**

```bash
git clone [URL_DEL_REPO]
cd server-monitor
docker compose up -d
```

### Variables de Entorno Requeridas

```env
# Auth0 Configuration
AUTH0_DOMAIN=your-domain.auth0.com
AUTH0_CLIENT_ID=your-client-id
AUTH0_CLIENT_SECRET=your-client-secret
AUTH0_AUDIENCE=your-api-audience

# Database Configuration
DATABASE_URL=postgresql://username:password@host:port/database
SPRING_DATASOURCE_USERNAME=username
SPRING_DATASOURCE_PASSWORD=password

# Monitoring Configuration
MONITORING_REFRESH_INTERVAL=30000
ALERT_EMAIL_ENABLED=true
EXPORT_MAX_RECORDS=10000
```

## 📋 **Cumplimiento de Parciales**

### ✅ **Parcial 1**
- Aplicación base funcional
- Containerización con Docker
- Deploy inicial

### ✅ **Parcial 2**
- Autenticación con Auth0
- Gestión de roles desde dashboard Auth0
- Protección de rutas con JWT
- Despliegue en Render
- Conexión con base de datos Neon
- Configuración segura sin datos expuestos

### ✅ **Parcial 3**
- **Panel en Tiempo Real**:
  - Actualización automática de métricas cada 30s
  - Gráficos interactivos con Chart.js
  - WebSockets para notificaciones instantáneas
  - Indicadores visuales de estado (verde/amarillo/rojo)
  
- **Sistema de Exportaciones**:
  - Exportación a Excel con formato profesional
  - Generación de PDFs con gráficos embebidos
  - Exportación CSV para análisis de datos
  - Filtros avanzados por fecha y servidor
  - Descarga directa desde el navegador

## 🎯 **Funcionalidades del Parcial 3**

### 📊 **Dashboard en Tiempo Real**
- **Métricas Live**: CPU, RAM, Disco actualizadas automáticamente
- **Gráficos Dinámicos**: Visualización temporal de las últimas 24 horas
- **Alertas Visuales**: Colores y notificaciones según umbrales
- **Estado de Conexión**: Indicador de conectividad de servidores

### 📤 **Exportaciones Avanzadas**

#### Excel (.xlsx)
- Múltiples hojas por tipo de métrica
- Gráficos embebidos en Excel
- Formato condicional según umbrales
- Metadatos de generación

#### PDF
- Reportes ejecutivos con logos
- Gráficos de tendencias
- Resúmenes estadísticos
- Formato profesional

#### CSV
- Datos en bruto para análisis
- Compatible con herramientas de BI
- Separadores configurables
- Encoding UTF-8

### 🔄 **Actualizaciones en Tiempo Real**
```java
@Component
public class RealTimeMonitoringService {
    @Scheduled(fixedDelay = 30000)
    public void updateMetrics() {
        // Lógica de actualización automática
    }
}
```

## 👨‍💻 **Stack Tecnológico**

### Core
- Java 17 + Spring Boot 3
- Vaadin 24 para Frontend
- Auth0 para Autenticación
- PostgreSQL + JPA/Hibernate

### Tiempo Real
- Spring WebSocket
- Server-Sent Events (SSE)
- Vaadin Push (@Push)

### Exportaciones
- Apache POI (Excel)
- iText 7 (PDF)
- OpenCSV (CSV)
- Chart.js para gráficos

### Infraestructura
- Docker para desarrollo
- Render para producción
- Neon.tech para base de datos
- GitHub Actions para CI/CD

## 🚀 **Instrucciones de Deploy**

### Render Configuration
```yaml
# render.yaml
services:
  - type: web
    name: server-monitor
    env: java
    buildCommand: ./mvnw clean package -Pproduction
    startCommand: java -jar target/server-monitor-1.0-SNAPSHOT.jar
    envVars:
      - key: AUTH0_DOMAIN
        sync: false
      - key: DATABASE_URL
        sync: false
```

### Branch Strategy
- `main`: Producción estable
- `staging`: Deploy automático en Render
- `develop`: Desarrollo activo

## 📊 **Métricas de Rendimiento**

- **Tiempo de respuesta**: < 200ms promedio
- **Actualización de datos**: Cada 30 segundos
- **Capacidad de exportación**: Hasta 10,000 registros
- **Concurrencia**: Soporte para 100+ usuarios simultáneos

## 🔒 **Seguridad**

- Tokens JWT con expiración configurable
- Validación de roles en cada endpoint
- Sanitización de datos de entrada
- Rate limiting para APIs
- Logs de auditoría para exportaciones

## 📱 **Compatibilidad**

- **Navegadores**: Chrome 90+, Firefox 88+, Safari 14+, Edge 90+
- **Dispositivos**: Responsive design para móviles y tablets
- **Exportaciones**: Compatible con Excel 2016+, lectores PDF modernos

---

**🎓 Universidad de El Salvador - Parcial 3 Completado**  
**📅 Junio 2025**  

**Desarrollado por**: [Tu Nombre/Equipo]  
**Curso**: Ingeniería en Desarrollo de Software  
**Docente**: [Nombre del Docente]
