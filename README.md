# TikTok Mod (prototipo Android)

Módulo **experimental** de Xposed para cargar opciones en TikTok instalado. Este repositorio no contiene TikTok ni descarga, modifica o redistribuye su APK. Tampoco incluye un APK de TikTok ya modificado: se necesita una copia obtenida por el usuario y un framework compatible.

## Nueva vía: parches Morphe

Además del prototipo Xposed descrito más abajo, se añadió una integración con una revisión fijada de Metra que ya contiene parches específicos para biografías, comentarios y enlaces. Construye un paquete `.mpp`, no distribuye TikTok. Consulta [morphe/README.md](morphe/README.md) para compilarlo, usarlo, sus licencias y sus limitaciones. Esta vía tampoco se ha validado en un dispositivo.

## Qué se investigó

- [LSPatch](https://github.com/JingMatrix/LSPatch) describe cómo incorporar un cargador y módulos Xposed en un APK por aplicación, sin root, para Android 9 o superior. Soporta módulos Xposed clásicos como este. Su documentación explica los modos «manager» e «integrated».
- [LSPosed](https://github.com/LSPosed/LSPosed) es otra vía para cargar módulos Xposed sobre aplicaciones instaladas en dispositivos compatibles. La configuración del framework depende del dispositivo.
- [NukeApp-OSS](https://github.com/dartcv/NukeApp-OSS) demuestra un módulo con entrada en ajustes para **Douyin**, pero su propia documentación excluye TikTok internacional. No se ha copiado su código ni se asume que sus hooks funcionen en TikTok.
- [Metra TikTok Patches](https://github.com/icysymmetra/tiktok-patches-for-morphe) ofrece otra vía más completa mediante Morphe: su documentación de desarrollo enumera parches de color de acento, copiar biografía, copiar comentarios sin usuario y abrir enlaces externos en el navegador predeterminado. El canal de desarrollo apunta a TikTok global 47.4.3 y declara pruebas de ejecución Android pendientes; la versión estable apunta a 46.2.3. No se ha incorporado su código GPL ni verificado aquí su funcionamiento. Para abrir en Chrome específicamente hay que configurarlo como navegador predeterminado. Seguir las instrucciones y requisitos de versiones de ese proyecto si se elige esta alternativa.

Ninguna de estas fuentes garantiza que TikTok acepte un APK parcheado ni que la interfaz interna sea estable. El parcheo cambia la firma: normalmente no permite actualizar la instalación original y puede requerir desinstalarla. Esto puede borrar datos locales; haz una copia de seguridad y revisa las implicaciones de seguridad y las condiciones del servicio antes de intentarlo. Las distribuciones divididas (split APK/APKS) requieren comprobar el soporte específico del parcheador. Usa únicamente herramientas y paquetes de fuentes confiables.

## Estado y límites importantes

**No está validado en una versión concreta de TikTok ni en un teléfono.** Los identificadores de vistas de `Matchers.java` son candidatos descriptivos, no identificadores verificados del APK. TikTok puede usar nombres ofuscados, vistas personalizadas o Compose, por lo que cualquiera o todas las funciones podrían no activarse. Los hooks solo apuntan al proceso principal de `com.zhiliaoapp.musically` o `com.ss.android.ugc.trill`; ninguna versión concreta de esos paquetes ha sido certificada. No hay promesa de compatibilidad con futuras actualizaciones.

- La entrada **TikTok Mod** se superpone en la esquina superior derecha cuando se encuentra un título visible «Ajustes y privacidad», «Configuración y privacidad» o «Settings and privacy». No es todavía una fila nativa en la primera posición y puede no aparecer, o tapar contenido, según el diseño de TikTok.
- En el menú hay un único botón, **Apariencia**, que contiene el selector de color y las extensiones. El color afecta al menú del módulo, **no** a todo TikTok. El **perfil modo Instagram aún no está implementado**; aparece como «Próximamente» y no modifica perfiles.
- Mantener pulsado copia el texto de una vista de biografía o comentario solo si su ID coincide exactamente con uno de los candidatos. El comentario se copia directamente de la vista del texto, sin concatenar una vista de autor separada; si TikTok incorpora el @ dentro de la misma vista, no se elimina automáticamente. Esto no modifica la opción de copiar nativa de TikTok.
- Un clic en una vista de enlace de perfil identificada abre Chrome si el texto contiene una URL HTTP(S) completa y Chrome está disponible. Si no, se conserva el comportamiento original. Los enlaces gestionados mediante spans, vistas personalizadas o URLs sin esquema pueden no detectarse.
- Los interruptores se guardan localmente en las preferencias privadas de TikTok, no en la aplicación lanzadora. Están activados por defecto; no hay recopilación ni envío de datos por el módulo. Las pulsaciones reconocidas reemplazan la acción original mientras estén activadas.

Para soporte real de una versión concreta hay que analizar sus vistas y flujos con permiso, identificar selectores estables, adaptar los hooks, y probarlos en dispositivo. En especial falta implementar el perfil estilo Instagram y el cambio global de acento.

## Compilar

Se necesita JDK 17, Android SDK 35 y Gradle 8.9. Con Android Studio se puede sincronizar el proyecto Gradle; también se puede ejecutar:

```sh
gradle testDebugUnitTest assembleDebug
```

El resultado es `app/build/outputs/apk/debug/app-debug.apk`. El flujo [Android](.github/workflows/android.yml) ejecuta los tests unitarios y publica el APK de depuración como artefacto de GitHub Actions. Un APK de depuración es solo para pruebas y no es una versión de producción.

## Probar con LSPosed

1. Instala TikTok por un canal legítimo y configura un framework compatible con Xposed clásico en el dispositivo.
2. Instala el APK de este módulo y actívalo en el gestor, limitando su alcance a TikTok. Sigue la documentación del framework para reiniciar el proceso o el dispositivo.
3. Fuerza el cierre de TikTok, vuelve a abrirlo y entra en **Ajustes y privacidad**. Si se reconoce la pantalla, pulsa **TikTok Mod → Apariencia**.
4. Prueba las opciones en biografías, comentarios y enlaces. Si la entrada o una función no aparecen, no implica que funcionen: revisa los registros del framework y los límites anteriores.

## Alternativa con LSPatch

Sigue las instrucciones oficiales de [LSPatch](https://github.com/JingMatrix/LSPatch) para parchear una copia legítima compatible de TikTok e incluir el APK del módulo. Instala el resultado firmado y, si el modo elegido lo requiere, activa el módulo en el gestor. Esta ruta no ha sido probada con TikTok en este proyecto; algunos dispositivos, paquetes, comprobaciones de integridad o inicios de sesión pueden impedir su uso.

## Descargar o publicar el módulo

Las [releases](https://github.com/lyanvalentinmail-prog/TiktokMod/releases) pueden contener `TikTok-Mod-modulo-debug.apk`, un APK de **módulo Xposed**, no un APK de TikTok. Instálalo en Android, activa el módulo en LSPosed para TikTok y reinicia TikTok como se explica arriba. La aplicación lanzadora solo muestra instrucciones; las funciones siguen siendo experimentales y dependen de la versión de TikTok.

El flujo manual **Publicar módulo Android** ejecuta los tests, compila el APK de depuración, genera `SHA256SUMS.txt` y crea una prerelease con una etiqueta nueva, por ejemplo `v0.1.0`. También deja el APK como artefacto del workflow. Cada runner firma el APK con su propia clave de depuración: una compilación posterior puede no instalarse encima de otra y requerir desinstalar el módulo anterior. Para actualizaciones de producción se necesitaría una clave de firma persistente protegida. El flujo no obtiene ni redistribuye TikTok ni genera un APK de TikTok parcheado. El paquete Morphe es un producto separado, explicado en [morphe/README.md](morphe/README.md).
