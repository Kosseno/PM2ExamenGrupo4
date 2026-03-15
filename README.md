# PM2 Examen Grupo 4 - Gestión de Contactos con Video y GPS

Este proyecto es una aplicación Android nativa desarrollada en Java que permite gestionar una lista de contactos. Cada contacto almacenado incluye su nombre, teléfono, ubicación GPS (latitud y longitud) y un video capturado desde la cámara.

## 🚀 Componentes del Proyecto

- **MainActivity:** Pantalla de inicio de la aplicación.
- **video.java:** Interfaz para capturar un nuevo contacto, obtener la ubicación GPS automática y grabar un video. Incluye feedback visual al completar la captura.
- **contactos.java:** Listado de contactos guardados. Permite buscar, eliminar, actualizar datos y reproducir el video asociado a cada contacto.
- **MapaActivity.java / Google Maps:** Permite visualizar la ubicación del contacto seleccionado directamente en Google Maps.
- **Models/Contacto.java:** Modelo de datos para el manejo de la información.
- **Config.java:** Archivo centralizado para configurar la dirección IP del servidor API.

## 🛠️ Configuración del Servidor (Node.js)

El proyecto incluye un servidor backend sencillo en Node.js para almacenar y gestionar los contactos.

### Pasos para ejecutar el servidor:

1. **Abrir la terminal o CMD.**
2. **Navegar a la carpeta del servidor:**
   Reemplaza `%user%` con tu nombre de usuario de Windows:
   ```bash
   cd C:\Users\%user%\AndroidStudioProjects\PM2ExamenGrupo4\app\src\main\java\com\example\pm2examengrupo4
   ```
3. **Instalar dependencias (si es la primera vez):**
   ```bash
   npm install
   ```
4. **Ejecutar el servidor:**
   ```bash
   node server.js
   ```

## 📱 Configuración de la App Android

Antes de compilar la aplicación, asegúrate de actualizar la dirección IP de tu máquina en el archivo de configuración para que la app pueda comunicarse con el servidor:

1. Abre el archivo: `app/src/main/java/com/example/pm2examengrupo4/Config.java`.
2. Cambia la constante `BASE_URL` con tu IP local:
   ```java
   public static final String BASE_URL = "http://TU_IP_LOCAL:3000/";
   ```

---
*Desarrollado como parte del examen de Programación Móvil 2.*
